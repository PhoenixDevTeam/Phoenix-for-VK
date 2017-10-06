package biz.dealnote.messenger.service.operations;

import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.os.RemoteException;

import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService;
import com.google.gson.Gson;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.ApiException;
import biz.dealnote.messenger.api.model.Error;
import biz.dealnote.messenger.api.model.response.VkReponse;
import biz.dealnote.messenger.db.RecordNotFoundException;
import biz.dealnote.messenger.exception.ApiServiceException;
import biz.dealnote.messenger.exception.DatabaseServiceException;
import biz.dealnote.messenger.exception.OtherServiceException;
import biz.dealnote.messenger.exception.ServiceException;
import biz.dealnote.messenger.service.ErrorLocalizer;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import retrofit2.HttpException;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

public abstract class AbsApiOperation implements RequestService.Operation {

    protected static final String TAG = AbsApiOperation.class.getSimpleName();

    public static final String EXTRA_START_ID = "start_id";

    public static final String NAME_CASE_NOM = "nom";
    public static final String NAME_CASE_GEN = "gen";
    public static final String NAME_CASE_DAT = "dat";
    public static final String NAME_CASE_ACC = "acc";
    public static final String NAME_CASE_INS = "ins";
    public static final String NAME_CASE_ABL = "abl";

    public static final String EXTRA_CHAT_IDS = "chat_ids";

    public static final String EXTRA_LINK_ID = "link_id";
    public static final String EXTRA_AUDIO = "audio";

    public static final String EXTRA_DESCRIPTION = "description";

    public static final String EXTRA_MESSAGE_IDS = "message_ids";

    public static final String EXTRA_FROM_GROUP = "from_group";
    public static final String EXTRA_PRIVACY_VIEW = "privacy_view";
    public static final String EXTRA_PRIVACY_COMMENT = "privacy_comment";
    public static final String EXTRA_UPLOAD_BY_ADMINS_ONLY = "upload_by_admins_only";
    public static final String EXTRA_COMMENTS_DISABLE = "comments_disabled";

    public static final String EXTRA_NAME = "name";

    public static final String EXTRA_SIGNED = "signed";

    public static final String EXTRA_ANSWER_ID = "answer_id";
    public static final String EXTRA_PROFILES = "profiles";

    public static final String OUT_DOCUMENT = "document";

    @Override
    public final Bundle execute(Context context, Request request) throws DataException, CustomRequestException {
        int accountId;

        if(request.contains(Extra.ACCOUNT_ID)){
            accountId = request.getInt(Extra.ACCOUNT_ID);
        } else {
            accountId = Settings.get()
                    .accounts()
                    .getCurrent();
        }

        if(accountId == ISettings.IAccountsSettings.INVALID_ID){
            throw new OtherServiceException(context.getString(R.string.error_token_is_null));
        }

        try {
            return execute(context, request, accountId);
        } catch (Exception e){
            e.printStackTrace();
            throw prepareException(context, e);
        }
    }

    public static ServiceException prepareException(Context context, Throwable throwable){
        if(throwable instanceof SQLiteConstraintException){
            return new DatabaseServiceException(throwable.getMessage());
        }

        if(throwable instanceof RecordNotFoundException){
            return new DatabaseServiceException(throwable.getMessage());
        }

        if(throwable instanceof RuntimeException){
            RuntimeException runtimeException = (RuntimeException) throwable;

            if(nonNull(runtimeException.getCause())){
                return prepareException(context, runtimeException.getCause());
            }
        }

        if(throwable instanceof ApiException){
            return from(context, ((ApiException) throwable).getError());
        }

        if(throwable instanceof HttpException){
            return from(context, (HttpException) throwable);
        }

        if(throwable instanceof ServiceException){
            return (ServiceException) throwable;
        }

        if(throwable instanceof RemoteException || throwable instanceof OperationApplicationException){
            return new DatabaseServiceException(throwable.getMessage());
        }

        return new OtherServiceException(throwable.getMessage());
    }

    public abstract Bundle execute(Context context, Request request, int accountId) throws Exception;

    protected Bundle buildSimpleSuccessResult(boolean success){
        Bundle bundle = new Bundle();
        bundle.putBoolean(Extra.SUCCESS, success);
        return bundle;
    }

    private static ServiceException from(Context context, Error error){
        String message = ErrorLocalizer.api().getMessage(context, error.errorCode, error.errorMsg);
        ApiServiceException.Captcha captcha = null;

        if(!safeIsEmpty(error.captchaImg) && !safeIsEmpty(error.captchaSid)){
            captcha = new ApiServiceException.Captcha(error.captchaSid, error.captchaImg);
        }

        return new ApiServiceException(message, error.errorCode, captcha);
    }

    protected static ServiceException from(Context context, HttpException e){
        if(e.code() == 401){
            return new OtherServiceException(context.getString(R.string.unauthorized));
        }

        try {
            String errorBody = e.response()
                    .errorBody()
                    .string();

            VkReponse response = new Gson().fromJson(errorBody, VkReponse.class);

            if(nonNull(response.error)){
                return from(context, response.error);
            }
        } catch (Exception ignored) {
        }

        return new OtherServiceException(e.getMessage());
    }
}

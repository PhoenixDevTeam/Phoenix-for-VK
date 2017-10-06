package biz.dealnote.messenger.api.impl;

import com.google.gson.JsonArray;

import java.util.Collection;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.TokenType;
import biz.dealnote.messenger.api.interfaces.IPollsApi;
import biz.dealnote.messenger.api.model.VKApiPoll;
import biz.dealnote.messenger.api.services.IPollsService;
import io.reactivex.Single;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
class PollsApi extends AbsApi implements IPollsApi {

    PollsApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<VKApiPoll> create(String question, Boolean isAnonymous, Integer ownerId, Collection<String> addAnswers) {
        JsonArray array = new JsonArray();
        for (String answer : addAnswers) {
            array.add(answer);
        }

        return provideService(IPollsService.class, TokenType.USER)
                .flatMap(service -> service
                        .create(question, integerFromBoolean(isAnonymous), ownerId, array.toString())
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> deleteVote(Integer ownerId, int pollId, int answerId, Boolean isBoard) {
        return provideService(IPollsService.class, TokenType.USER)
                .flatMap(service -> service.deleteVote(ownerId, pollId, answerId, integerFromBoolean(isBoard))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> addVote(Integer ownerId, int pollId, int answerId, Boolean isBoard) {
        return provideService(IPollsService.class, TokenType.USER)
                .flatMap(service -> service.addVote(ownerId, pollId, answerId, integerFromBoolean(isBoard))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<VKApiPoll> getById(Integer ownerId, Boolean isBoard, Integer pollId) {
        return provideService(IPollsService.class, TokenType.USER)
                .flatMap(service -> service.getById(ownerId, integerFromBoolean(isBoard), pollId)
                        .map(extractResponseWithErrorHandling()));
    }
}

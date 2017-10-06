package biz.dealnote.messenger.api.interfaces;

import biz.dealnote.messenger.api.model.VKApiWikiPage;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public interface IPagesApi {

    Single<VKApiWikiPage> get(int ownerId, int pageId, Boolean global, Boolean sitePreview,
                              String title, Boolean needSource, Boolean needHtml);

}

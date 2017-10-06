package biz.dealnote.messenger.api.services;

import java.util.List;

import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.api.model.response.BaseResponse;
import biz.dealnote.messenger.api.model.server.VkApiDocsUploadServer;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public interface IDocsService {

    //https://vk.com/dev/docs.delete
    @FormUrlEncoded
    @POST("docs.delete")
    Single<BaseResponse<Integer>> delete(@Field("owner_id") Integer ownerId,
                                         @Field("doc_id") int docId);

    /**
     * Copies a document to a user's or community's document list.
     *
     * @param ownerId   ID of the user or community that owns the document. Use a negative value to designate a community ID.
     * @param docId     Document ID.
     * @param accessKey Access key. This parameter is required if access_key was returned with the document's data.
     * @return the ID of the created document.
     */
    @FormUrlEncoded
    @POST("docs.add")
    Single<BaseResponse<Integer>> add(@Field("owner_id") int ownerId,
                                      @Field("doc_id") int docId,
                                      @Field("access_key") String accessKey);

    /**
     * Returns information about documents by their IDs.
     *
     * @param ids Document IDs. Example: 66748_91488,66748_91455.
     *            List of comma-separated words, required parameter
     * @return an array of objects describing documents
     */
    @FormUrlEncoded
    @POST("docs.getById")
    Single<BaseResponse<List<VkApiDoc>>> getById(@Field("docs") String ids);

    /**
     * Returns a list of documents matching the search criteria.
     *
     * @param query  Search query string.
     * @param count  Number of results to return.
     * @param offset Offset needed to return a specific subset of results.
     * @return Returns the total results number in count field and an array of objects describing documents in items field
     */
    @FormUrlEncoded
    @POST("docs.search")
    Single<BaseResponse<Items<VkApiDoc>>> search(@Field("q") String query,
                                                 @Field("count") Integer count,
                                                 @Field("offset") Integer offset);

    /**
     * Saves a document after uploading it to a server.
     *
     * @param file  This parameter is returned when the file is uploaded to the server
     * @param title Document title
     * @param tags  Document tags.
     * @return Returns an array of uploaded document objects.
     */
    @FormUrlEncoded
    @POST("docs.save")
    Single<BaseResponse<List<VkApiDoc>>> save(@Field("file") String file,
                                              @Field("title") String title,
                                              @Field("tags") String tags);

    /**
     * Returns the server address for document upload.
     *
     * @param groupId Community ID (if the document will be uploaded to the community).
     * @param type    type of document, null or "audio_message" (undocumented option)
     * @return an object with an upload_url field. After the document is uploaded, use the {@link #save} method.
     */
    @FormUrlEncoded
    @POST("docs.getUploadServer")
    Single<BaseResponse<VkApiDocsUploadServer>> getUploadServer(@Field("group_id") Integer groupId,
                                                                @Field("type") String type);

    /**
     * Returns detailed information about user or community documents.
     *
     * @param ownerId ID of the user or community that owns the documents.
     *                Use a negative value to designate a community ID.
     *                Current user id is used by default
     * @param count   Number of documents to return. By default, all documents.
     * @param offset  Offset needed to return a specific subset of documents.
     * @param type    Document type. Possible values:
     *                1 — text documents;
     *                2 — archives;
     *                3 — gif;
     *                4 — images;
     *                5 — audio;
     *                6 — video;
     *                7 — e-books;
     *                8 — unknown.
     * @return Returns the total results number in count field and an array of objects describing documents in items field
     */
    @FormUrlEncoded
    @POST("docs.get")
    Single<BaseResponse<Items<VkApiDoc>>> get(@Field("owner_id") Integer ownerId,
                                              @Field("count") Integer count,
                                              @Field("offset") Integer offset,
                                              @Field("type") Integer type);

}

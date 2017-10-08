package biz.dealnote.messenger.domain.impl;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.CommentsDto;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VKApiTopic;
import biz.dealnote.messenger.api.model.VKApiVideo;
import biz.dealnote.messenger.api.model.response.NewsfeedCommentsResponse;
import biz.dealnote.messenger.domain.INewsfeedInteractor;
import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.IOwnersBundle;
import biz.dealnote.messenger.model.NewsfeedComment;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoWithOwner;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.Topic;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.model.VideoWithOwner;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.VKOwnIds;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by admin on 08.05.2017.
 * phoenix
 */
public class NewsfeedInteractor implements INewsfeedInteractor {

    private final INetworker networker;
    private final IOwnersInteractor ownersInteractor;

    public NewsfeedInteractor(INetworker networker, IOwnersInteractor ownersInteractor) {
        this.networker = networker;
        this.ownersInteractor = ownersInteractor;
    }

    @Override
    public Single<Pair<List<NewsfeedComment>, String>> getNewsfeedComments(int accountId, int count, String startFrom, String filter) {
        return networker.vkDefault(accountId)
                .newsfeed()
                .getComments(count, filter, null, null, null,
                        1, startFrom, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    VKOwnIds ownIds = new VKOwnIds();

                    List<NewsfeedCommentsResponse.Dto> dtos = Utils.listEmptyIfNull(response.items);

                    for (NewsfeedCommentsResponse.Dto dto : dtos) {
                        if (dto instanceof NewsfeedCommentsResponse.PostDto) {
                            VKApiPost post = ((NewsfeedCommentsResponse.PostDto) dto).post;
                            ownIds.append(post);
                            ownIds.append(post.comments);
                        } else if (dto instanceof NewsfeedCommentsResponse.PhotoDto) {
                            ownIds.append(((NewsfeedCommentsResponse.PhotoDto) dto).photo.comments);
                            ownIds.append(((NewsfeedCommentsResponse.PhotoDto) dto).photo.owner_id);
                        } else if (dto instanceof NewsfeedCommentsResponse.TopicDto) {
                            VKApiTopic topic = ((NewsfeedCommentsResponse.TopicDto) dto).topic;
                            ownIds.append(topic.comments);
                            ownIds.append(topic);
                        } else if (dto instanceof NewsfeedCommentsResponse.VideoDto) {
                            ownIds.append(((NewsfeedCommentsResponse.VideoDto) dto).video.comments);
                            ownIds.append(((NewsfeedCommentsResponse.VideoDto) dto).video.owner_id);
                        } else {
                            // TODO: 08.05.2017
                        }
                    }

                    return ownersInteractor.findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersInteractor.MODE_ANY, owners)
                            .map(bundle -> {
                                List<NewsfeedComment> comments = new ArrayList<>(dtos.size());

                                for (NewsfeedCommentsResponse.Dto dto : dtos) {
                                    NewsfeedComment comment = createFrom(dto, bundle);

                                    if (nonNull(comment)) {
                                        comments.add(comment);
                                    }
                                }

                                return Pair.create(comments, response.nextFrom);
                            });
                });
    }

    private static Comment oneCommentFrom(Commented commented, CommentsDto dto, IOwnersBundle bundle) {
        if (nonNull(dto) && nonEmpty(dto.list)) {
            return Dto2Model.buildComment(commented, dto.list.get(0), bundle);
        }

        return null;
    }

    private static NewsfeedComment createFrom(NewsfeedCommentsResponse.Dto dto, IOwnersBundle bundle) {
        if (dto instanceof NewsfeedCommentsResponse.PhotoDto) {
            VKApiPhoto photoDto = ((NewsfeedCommentsResponse.PhotoDto) dto).photo;
            Photo photo = Dto2Model.transform(photoDto);
            Commented commented = Commented.from(photo);

            Owner photoOwner = bundle.getById(photo.getOwnerId());
            return new NewsfeedComment(new PhotoWithOwner(photo, photoOwner))
                    .setComment(oneCommentFrom(commented, photoDto.comments, bundle));
        }

        if (dto instanceof NewsfeedCommentsResponse.VideoDto) {
            VKApiVideo videoDto = ((NewsfeedCommentsResponse.VideoDto) dto).video;
            Video video = Dto2Model.transform(videoDto);
            Commented commented = Commented.from(video);

            Owner videoOwner = bundle.getById(video.getOwnerId());
            return new NewsfeedComment(new VideoWithOwner(video, videoOwner))
                    .setComment(oneCommentFrom(commented, videoDto.comments, bundle));
        }

        if (dto instanceof NewsfeedCommentsResponse.PostDto) {
            VKApiPost postDto = ((NewsfeedCommentsResponse.PostDto) dto).post;
            Post post = Dto2Model.transform(postDto, bundle);
            Commented commented = Commented.from(post);
            return new NewsfeedComment(post).setComment(oneCommentFrom(commented, postDto.comments, bundle));
        }

        if (dto instanceof NewsfeedCommentsResponse.TopicDto) {
            VKApiTopic topicDto = ((NewsfeedCommentsResponse.TopicDto) dto).topic;
            Topic topic = Dto2Model.transform(topicDto, bundle);
            Commented commented = Commented.from(topic);
            return new NewsfeedComment(topic).setComment(oneCommentFrom(commented, topicDto.comments, bundle));
        }

        return null;
    }
}
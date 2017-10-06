package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.text.TextUtils;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.model.PhotoSize;
import biz.dealnote.messenger.model.PhotoSizes;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.Types;
import biz.dealnote.messenger.model.WikiPage;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Objects;

public class DocLink {

    private int type;
    public AbsModel attachment;

    public DocLink(AbsModel attachment) {
        this.attachment = attachment;
        this.type = typeOf(attachment);
    }

    private static int typeOf(AbsModel model){
        if(model instanceof Document){
            return Types.DOC;
        }

        if(model instanceof Post){
            return Types.POST;
        }

        if(model instanceof Link){
            return Types.LINK;
        }

        if(model instanceof Poll){
            return Types.POLL;
        }

        if(model instanceof WikiPage){
            return Types.WIKI_PAGE;
        }

        throw new IllegalArgumentException();
    }

    public int getType() {
        return type;
    }

    public String getImageUrl() {
        switch (type) {
            case Types.DOC:
                Document doc = (Document) attachment;
                return doc.getPreviewWithSize(PhotoSize.M, false);

            case Types.POST:
                return ((Post) attachment).getAuthorPhoto();

            case Types.LINK:
                Link link = (Link) attachment;

                if(Objects.nonNull(link.getPhoto()) && Objects.nonNull(link.getPhoto().getSizes())){
                    PhotoSizes sizes = link.getPhoto().getSizes();
                    return sizes.getUrlForSize(PhotoSize.M, false);
                }

                return null;
        }

        return null;
    }

    public String getTitle(Context context) {
        String title;
        switch (type) {
            case Types.DOC:
                return ((Document) attachment).getTitle();

            case Types.POST:
                return ((Post) attachment).getAuthorName();

            case Types.LINK:
                title = ((Link) attachment).getTitle();
                if (TextUtils.isEmpty(title)) {
                    title = "[" + context.getString(R.string.attachment_link).toLowerCase() + "]";
                }
                return title;

            case Types.POLL:
                Poll poll = (Poll) attachment;
                return context.getString(poll.isAnonymous() ? R.string.anonymous_poll : R.string.open_poll);

            case Types.WIKI_PAGE:
                return context.getString(R.string.wiki_page);
        }
        return null;
    }

    private static final String URL = "URL";
    private static final String W = "WIKI";

    public String getExt() {
        switch (type) {
            case Types.DOC:
                return ((Document) attachment).getExt();
            case Types.POST:
                return null;
            case Types.LINK:
                return URL;
            case Types.WIKI_PAGE:
                return W;
        }
        return null;
    }

    public String getSecondaryText(Context context) {
        switch (type) {
            case Types.DOC:
                return AppTextUtils.getSizeString((int) ((Document) attachment).getSize());

            case Types.POST:
                Post post = (Post) attachment;
                return post.hasText() ? post.getText() : "[" + context.getString(R.string.wall_post) + "]";

            case Types.LINK:
                return ((Link) attachment).getUrl();

            case Types.POLL:
                return ((Poll) attachment).getQuestion();

            case Types.WIKI_PAGE:
                return ((WikiPage)attachment).getTitle();
        }
        return null;
    }
}

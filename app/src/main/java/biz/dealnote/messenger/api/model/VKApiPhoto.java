//
//  Copyright (c) 2014 VK.com
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy of
//  this software and associated documentation files (the "Software"), to deal in
//  the Software without restriction, including without limitation the rights to
//  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
//  the Software, and to permit persons to whom the Software is furnished to do so,
//  subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in all
//  copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
//  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
//  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
//  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
//  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//

package biz.dealnote.messenger.api.model;

import java.util.List;

/**
 * Describes a photo object from VK.
 */
public class VKApiPhoto implements VKApiAttachment, Commentable, Likeable, Copyable {

    /**
     * Photo ID, positive number
     */
    public int id;

    /**
     * Photo album ID.
     */
    public int album_id;

    /**
     * ID of the user or community that owns the photo.
     */
    public int owner_id;

    /**
     * Width (in pixels) of the original photo.
     */
    public int width;

    /**
     * Height (in pixels) of the original photo.
     */
    public int height;

    /**
     * Text describing the photo.
     */
    public String text;

    /**
     * Date (in Unix time) the photo was added.
     */
    public long date;

    /**
     * Information whether the current user liked the photo.
     */
    public boolean user_likes;

    /**
     * Whether the current user can comment on the photo
     */
    public boolean can_comment;

    /**
     * Number of likes on the photo.
     */
    public int likes;

    /**
     * Number of comments on the photo.
     */
    public CommentsDto comments;

    /**
     * Number of tags on the photo.
     */
    public int tags;

    /**
     * An access key using for get information about hidden objects.
     */
    public String access_key;

    public int post_id;

    public List<PhotoSizeDto> sizes;

    /**
     * Creates empty Photo instance.
     */
    public VKApiPhoto() {

    }

    @Override
    public String getType() {
        return TYPE_PHOTO;
    }
}
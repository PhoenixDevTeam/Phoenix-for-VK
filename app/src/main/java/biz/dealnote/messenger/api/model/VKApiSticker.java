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

/**
 * Describes a photo object from VK.
 */
public class VKApiSticker implements VKApiAttachment {

    /**
     * Sticker ID, positive number
     */
    public int id;

    /**
     * Width (in pixels) of the original photo.
     */
    public int width;

    /**
     * Height (in pixels) of the original photo.
     */
    public int height;

    /**
     * URL of image with maximum size 64x64px.
     */
    public String photo_64;

    /**
     * URL of image with maximum size 128x128px.
     */
    public String photo_128;

    /**
     * URL of image with maximum size 256x256px.
     */
    public String photo_256;

    @Override
    public String getType() {
        return TYPE_STICKER;
    }
}
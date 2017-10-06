/*
 * Copyright 2014 Ankush Sachdeva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.dealnote.messenger.view.emoji;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.settings.Settings;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com).
 */
public class EmojiconEditText extends AppCompatEditText {

    private int mEmojiconSize;

    public EmojiconEditText(Context context) {
        this(context, null);
    }

    public EmojiconEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    private void init(AttributeSet attrs) {
        mEmojiconSize = (int) getTextSize();

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon);
        mEmojiconSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize, getTextSize());
        a.recycle();
        setText(getText());
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (!isInEditMode() && !Settings.get().ui().isSystemEmoji()) {
            EmojiconHandler.addEmojis(getContext(), getText(), mEmojiconSize);
        }
    }

    /**
     * Set the size of emojicon in pixels.
     */
    public void setEmojiconSize(int pixels) {
        mEmojiconSize = pixels;
    }
}

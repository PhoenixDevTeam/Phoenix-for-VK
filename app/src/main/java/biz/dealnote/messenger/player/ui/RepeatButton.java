/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package biz.dealnote.messenger.player.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.player.MusicPlaybackService;
import biz.dealnote.messenger.player.util.MusicUtils;
import biz.dealnote.messenger.settings.CurrentTheme;

public class RepeatButton extends AppCompatImageButton implements OnClickListener {

    public RepeatButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        MusicUtils.cycleRepeat();
        updateRepeatState();
    }

    public void updateRepeatState() {
        switch (MusicUtils.getRepeatMode()) {
            case MusicPlaybackService.REPEAT_ALL:
                setColorFilter(CurrentTheme.getIconColorActive(getContext()));
                setImageDrawable(getResources().getDrawable(R.drawable.repeat));
                break;
            case MusicPlaybackService.REPEAT_CURRENT:
                setColorFilter(CurrentTheme.getIconColorActive(getContext()));
                setImageDrawable(getResources().getDrawable(R.drawable.repeat_once));
                break;
            case MusicPlaybackService.REPEAT_NONE:
                setColorFilter(CurrentTheme.getIconColorStatic(getContext()));
                setImageDrawable(getResources().getDrawable(R.drawable.repeat_off));
                break;
            default:
                break;
        }
    }
}

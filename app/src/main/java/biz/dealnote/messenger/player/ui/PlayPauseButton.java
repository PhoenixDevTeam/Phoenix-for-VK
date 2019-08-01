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
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.player.util.MusicUtils;

public class PlayPauseButton extends FloatingActionButton implements OnClickListener {

    public PlayPauseButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        MusicUtils.playOrPause();
        updateState();
    }

    public void updateState() {
        if (MusicUtils.isPlaying()) {
            setImageResource(R.drawable.pause);
        } else {
            setImageResource(R.drawable.play);
        }
    }

}

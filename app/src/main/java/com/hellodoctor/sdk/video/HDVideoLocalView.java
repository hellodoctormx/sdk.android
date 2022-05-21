package com.hellodoctor.sdk.video;


import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.twilio.video.VideoScaleType;
import com.twilio.video.VideoTextureView;

public class HDVideoLocalView extends VideoTextureView {
    public HDVideoLocalView(Context context) {
        super(context);

        setMirror(true);
        setVideoScaleType(VideoScaleType.ASPECT_FIT);

        FrameLayout.LayoutParams aspectRatioParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        aspectRatioParams.gravity = Gravity.CENTER;

        setLayoutParams(aspectRatioParams);
    }
}

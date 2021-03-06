package com.hellodoctormx.sdk.video

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import com.twilio.video.VideoScaleType
import com.twilio.video.VideoTextureView

class LocalParticipantView(context: Context) : VideoTextureView(context) {
    init {
        mirror = true
        videoScaleType = VideoScaleType.ASPECT_FIT
        layoutParams = run {
            val matchParent = FrameLayout.LayoutParams.MATCH_PARENT

            FrameLayout.LayoutParams(matchParent, matchParent).apply {
                gravity = Gravity.CENTER
            }
        }
    }
}
package com.hellodoctor.sdk.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hellodoctor.sdk.video.managers.SoundPoolManager;
import com.twilio.video.CameraCapturer;
import com.twilio.video.ConnectOptions;
import com.twilio.video.EncodingParameters;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.Video;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoTextureView;
import com.twilio.video.VideoView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tvi.webrtc.Camera1Enumerator;
import tvi.webrtc.VideoSink;

public class HDVideo {
    public static String TAG = "HDVideo";

    private static HDVideo instance;

    private final Context currentContext;
    private final Activity currentActivity;

    private Room mRoom;
    private LocalVideoTrack mLocalVideoTrack;
    private LocalAudioTrack mLocalAudioTrack;
    private VideoTextureView mLocalParticipantView;

    private AudioManager audioManager;
    private SoundPoolManager soundPoolManager;
    private ToneGenerator dtmfGenerator;
    private Handler outboundRingingHandler;
    private Runnable outboundRingingRunnable;

    private int originalAudioMode = AudioManager.MODE_NORMAL;
    private AudioFocusRequest focusRequest;

    private Map<String, HDVideoRemoteView> mRemoteVideoViews = new HashMap<>();
    private RemoteParticipant.Listener remoteParticipantListener;

    private VideoTextureView portalView;
    private RemoteParticipant portalParticipant;
    private RemoteVideoTrack portalParticipantTrack;

    private CameraCapturer cameraCapturer;
    private final Camera1Enumerator camera1Enumerator = new Camera1Enumerator();

    HDVideo(Context context, Activity activity) {
        currentContext = context;
        currentActivity = activity;

        soundPoolManager = new SoundPoolManager(context);

        remoteParticipantListener = new TwilioRemoteParticipantListener(this);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        dtmfGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, ToneGenerator.MAX_VOLUME);

        instance = this;
    }

    public static HDVideo getInstance(Context context, Activity activity) {
        if (instance == null) {
            instance = new HDVideo(context, activity);
        }

        return instance;
    }

    RemoteParticipant.Listener remoteParticipantListener() {
        return remoteParticipantListener;
    }

    void setRemoteParticipantVideoView(HDVideoRemoteView videoView, @Nullable String participantIdentity) {
        if (participantIdentity == null) {
            return;
        }

        mRemoteVideoViews.put(participantIdentity, videoView);

        tryRenderRemoteParticipant(videoView, participantIdentity);
    }

    void tryRenderRemoteParticipant(@NonNull VideoView remoteView, String participantIdentity) {
        if (currentActivity == null) {
            Log.w(TAG, "Can't add remote video renderer: no current activity found");
            return;
        }

        for (RemoteParticipant remoteParticipant : mRoom.getRemoteParticipants()) {
            if (!remoteParticipant.getIdentity().equals(participantIdentity)) {
                continue;
            }

            for (RemoteVideoTrackPublication remoteVideoTrackPublication : remoteParticipant.getRemoteVideoTracks()) {
                if (!remoteVideoTrackPublication.isTrackSubscribed()) {
                    continue;
                }

                RemoteVideoTrack remoteVideoTrack = remoteVideoTrackPublication.getRemoteVideoTrack();

                if (remoteVideoTrack == null) {
                    continue;
                }

                HDVideo self = this;

                currentActivity.runOnUiThread(() -> {
                    remoteVideoTrack.addSink(remoteView);

                    Bundle data = new Bundle();
                    data.putString("action", "renderedParticipant");
                    data.putString("participantIdentity", participantIdentity);

                    self.sendEvent("participantVideoEvent", data);
                });

                break;
            }
        }
    }

    HDVideoRemoteView getParticipantVideoView(String participantIdentity) {
        return mRemoteVideoViews.get(participantIdentity);
    }

    void setLocalView(@NonNull HDVideoLocalView localVideoView) {
        prepareLocalVideo();

        mLocalParticipantView = localVideoView;

        for (VideoSink sink : mLocalVideoTrack.getSinks()) {
            mLocalVideoTrack.removeSink(sink);
        }

        mLocalVideoTrack.addSink(mLocalParticipantView);

        Bundle data = new Bundle();
        data.putString("status", "ready");

        this.sendEvent("localViewStatus", data);
    }

    public void prepareLocalMedia() {
        prepareLocalAudio();
        prepareLocalVideo();
    }

    public void prepareLocalAudio() {
        if (mLocalAudioTrack == null) {
            mLocalAudioTrack = LocalAudioTrack.create(currentContext, true);
        }
    }

    public void prepareLocalVideo() {
        if (cameraCapturer == null) {
            cameraCapturer = new CameraCapturer(currentContext, getFrontCameraId());
        }

        if (mLocalVideoTrack == null) {
            VideoFormat videoFormat = new VideoFormat(VideoDimensions.VGA_VIDEO_DIMENSIONS, 24);

            mLocalVideoTrack = LocalVideoTrack.create(currentContext, true, cameraCapturer, videoFormat);
        }
    }

    public void startLocalCapture() {
        if (mLocalParticipantView != null) {
            setLocalView((HDVideoLocalView) mLocalParticipantView);
        }
    }

    public void stopLocalCapture() {
        if (cameraCapturer != null) {
            cameraCapturer.stopCapture();
            cameraCapturer = null;
            mLocalVideoTrack = null;
        }
    }

    public Room connect(String roomName, String accessToken) {
        prepareLocalMedia();

        ConnectOptions connectOptions = new ConnectOptions.Builder(accessToken)
                .roomName(roomName)
                .audioTracks(Collections.singletonList(mLocalAudioTrack))
                .videoTracks(Collections.singletonList(mLocalVideoTrack))
                .enableAutomaticSubscription(true)
                .encodingParameters(new EncodingParameters(16, 0))
                .build();

        mRoom = Video.connect(currentContext, connectOptions, new TwilioRoomListener(this));

        LocalParticipant localParticipant = mRoom.getLocalParticipant();

        if (localParticipant != null && mLocalVideoTrack != null) {
            localParticipant.publishTrack(mLocalVideoTrack);
        }

        if (mLocalParticipantView != null && mLocalVideoTrack != null && mLocalVideoTrack.getSinks().size() == 0) {
            mLocalVideoTrack.addSink(mLocalParticipantView);
        }

        setScreenAlwaysOn(true);

        return mRoom;
    }

    public boolean isConnectedToRoom(String roomName) {
        return mRoom != null && mRoom.getName().equals(roomName);
    }

    public List<RemoteParticipant> getRemoteParticipants() {
        if (mRoom == null) {
            return Collections.emptyList();
        } else {
            return mRoom.getRemoteParticipants();
        }
    }

    public void disconnect() {
        this.unsetAudioFocus();

        setScreenAlwaysOn(false);

        stopLocalCapture();

        if (mRoom != null) {
            mRoom.disconnect();
            mRoom = null;
        }

        if (mLocalAudioTrack != null) {
            mLocalAudioTrack.release();
            mLocalAudioTrack = null;
        }

        if (mLocalVideoTrack != null) {
            mLocalVideoTrack.release();
            mLocalVideoTrack = null;
        }

        mRemoteVideoViews.clear();
    }

    public void reject() {
        SoundPoolManager.getInstance(currentContext).stopRinging();
        Bundle args = new Bundle();
        sendEvent("connectionDidDisconnect", args);
    }

    public void setVideoPublished(Boolean published) throws Exception {
        if (mRoom == null || mRoom.getLocalParticipant() == null || mLocalVideoTrack == null) {
            throw new Exception("no room and/or local participant and/or local video track");
        }

        LocalParticipant localParticipant = mRoom.getLocalParticipant();

        if (published) {
            localParticipant.publishTrack(mLocalVideoTrack);
        } else {
            localParticipant.unpublishTrack(mLocalVideoTrack);
        }
    }

    public void setVideoEnabled(Boolean enabled) throws Exception {
        if (mLocalVideoTrack == null) {
            throw new Exception("local video track was destroyed");
        }

        mLocalVideoTrack.enable(enabled);
    }

    public void setAudioEnabled(Boolean enabled) throws Exception {
        if (mLocalAudioTrack == null) {
            throw new Exception("local audio track was destroyed");
        }

        mLocalAudioTrack.enable(enabled);
    }

    public void flipCamera() {
        String currentCameraID = cameraCapturer.getCameraId();

        if (currentCameraID.equals(getFrontCameraId()) && getBackCameraId() != null) {
            cameraCapturer.switchCamera(getBackCameraId());
        } else if (getFrontCameraId() != null){
            cameraCapturer.switchCamera(getFrontCameraId());
        }
    }

    public boolean setSpeakerPhone(Boolean value) {
        if (audioManager == null) {
            return false;
        }

        setAudioFocus();
        audioManager.setSpeakerphoneOn(value);

        return true;
    }

    public void setRingtonePlaying(Boolean shouldPlayRingtone) {
         if (shouldPlayRingtone) {
             soundPoolManager.playRinging();
         } else {
             soundPoolManager.stopRinging();
         }
    }

    public void wakeMainActivity() {
        String packageName = currentContext.getApplicationContext().getPackageName();
        Intent focusIntent = currentContext.getPackageManager().getLaunchIntentForPackage(packageName).cloneFilter();
        focusIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        focusIntent.setAction(Intent.ACTION_MAIN);

        if (currentActivity != null) {
            currentActivity.startActivity(focusIntent);
        } else {
            focusIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            focusIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            currentContext.startActivity(focusIntent);
        }
    }

    void setScreenAlwaysOn(Boolean alwaysOn) {
        if (currentActivity == null) {
            Log.w(TAG, "Can't add remote video renderer: no current activity found");
            return;
        }

        currentActivity.runOnUiThread(() -> {
            Log.i(TAG, String.format("setting screen always on %b", alwaysOn));

            if (alwaysOn) {
                currentActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                currentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }

    private void setAudioFocus() {
        originalAudioMode = audioManager.getMode();
        // Request audio focus before making any device switch
        if (Build.VERSION.SDK_INT >= 26) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int i) {
                        }
                    })
                    .build();
            audioManager.requestAudioFocus(focusRequest);
        } else {
            audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            );
        }
        /*
         * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
         * required to be in this mode when playout and/or recording starts for
         * best possible VoIP performance. Some devices have difficulties with speaker mode
         * if this is not set.
         */
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    void unsetAudioFocus() {
        if (audioManager == null) {
            return;
        }
        audioManager.setMode(originalAudioMode);
        if (Build.VERSION.SDK_INT >= 26) {
            if (focusRequest != null) {
                audioManager.abandonAudioFocusRequest(focusRequest);
            }
        } else {
            audioManager.abandonAudioFocus(null);
        }
    }

    String getFrontCameraId() {
        for (String deviceName : camera1Enumerator.getDeviceNames()) {
            if (camera1Enumerator.isFrontFacing(deviceName)) {
                return deviceName;
            }
        }

        return null;
    }

    String getBackCameraId() {
        for (String deviceName : camera1Enumerator.getDeviceNames()) {
            if (camera1Enumerator.isBackFacing(deviceName)) {
                return deviceName;
            }
        }

        return null;
    }

    void sendEvent(String eventName, @Nullable Bundle params) {

    }
}

package to.popin.androidsdk.call;

import android.content.Context;

import com.twilio.video.EncodingParameters;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.VideoView;

import to.popin.androidsdk.common.CameraCapturerCompat;
import to.popin.androidsdk.common.Device;


public class CallLocalTrackManager {
    public static final String PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT = "0";
    public static final String PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT = "0";
    private static final String LOCAL_AUDIO_TRACK_NAME = "mic";
    private static final String LOCAL_VIDEO_TRACK_NAME = "camera";
    private final Context context;
    private final CameraCapturerCompat cameraCapturerCompat;
    private final EncodingParameters encodingParameters;
    private final Device device;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;


    public CallLocalTrackManager(Context context, Device device) {
        this.context = context;
        this.device = device;
        cameraCapturerCompat = new CameraCapturerCompat(context, CameraCapturerCompat.Source.FRONT_CAMERA);
        final int maxAudioBitrate =
                Integer.parseInt(PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT);
        final int maxVideoBitrate =
                Integer.parseInt(PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT);
        this.encodingParameters = new EncodingParameters(maxAudioBitrate, maxVideoBitrate);
    }

    public void releaseLocalAudioAndVideTracks() {
        if (localAudioTrack != null) {
            localAudioTrack.release();
            localAudioTrack = null;
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
            localVideoTrack = null;
        }
    }

    public void createAudioAndVideoTracks(AddVideoSinkListener addVideoSinkListener) {
        //     VideoFormat videoFormat = new VideoFormat(VideoDimensions.HD_720P_VIDEO_DIMENSIONS, 24);
        localAudioTrack = LocalAudioTrack.create(context, true, LOCAL_AUDIO_TRACK_NAME);

        localVideoTrack = LocalVideoTrack.create(context, true, cameraCapturerCompat, LOCAL_VIDEO_TRACK_NAME);

        addVideoSinkListener.addVideoSink(localVideoTrack, false);
    }

    public void changeVideoSink(VideoView fromView, VideoView toView) {
        if (localVideoTrack != null) {
            localVideoTrack.removeSink(fromView);
            localVideoTrack.addSink(toView);
        }
    }

    public void switchCamera(SwitchCameraListener switchCameraListener) {
        if (cameraCapturerCompat != null) {
            CameraCapturerCompat.Source cameraSource = cameraCapturerCompat.getCameraSource();
            cameraCapturerCompat.switchCamera();
            switchCameraListener.cameraSwitched(cameraSource == CameraCapturerCompat.Source.FRONT_CAMERA);
        }
    }

    public void updateZoom(float newZoom) {
        if (cameraCapturerCompat != null) {
            cameraCapturerCompat.setZoom(newZoom);
        }
    }

    public void toggleCamera(ToggleCameraListener toggleCameraListener) {
        if (localVideoTrack != null) {
            boolean enable = !localVideoTrack.isEnabled();
            localVideoTrack.enable(enable);
            toggleCameraListener.cameraToggled(enable);
        }
    }

    public void toggleMute(ToggleMuteListener toggleMuteListener) {
        if (localAudioTrack != null) {
            boolean enable = !localAudioTrack.isEnabled();
            localAudioTrack.enable(enable);
            toggleMuteListener.muteToggled(enable);
        }
    }

    public LocalAudioTrack getLocalAudioTrack() {
        return localAudioTrack;
    }

    public LocalVideoTrack getLocalVideoTrack() {
        return localVideoTrack;
    }

    public EncodingParameters getEncodingParameters() {
        return encodingParameters;
    }

    public interface AddVideoSinkListener {
        void addVideoSink(LocalVideoTrack localVideoTrack, boolean mirror);
    }

    public interface ChangeSinkListener {
        void changedSink(boolean mirror);
    }

    public interface SwitchCameraListener {
        void cameraSwitched(boolean isFrontCam);
    }

    public interface ToggleCameraListener {
        void cameraToggled(boolean enable);
    }

    public interface ToggleMuteListener {
        void muteToggled(boolean enable);
    }
}

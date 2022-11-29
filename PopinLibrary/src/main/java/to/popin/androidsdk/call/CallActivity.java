package to.popin.androidsdk.call;

import android.Manifest;
import android.app.PictureInPictureParams;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.twilio.audioswitch.AudioDevice;
import com.twilio.audioswitch.AudioSwitch;
import com.twilio.video.ConnectOptions;
import com.twilio.video.H264Codec;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LogLevel;
import com.twilio.video.OpusCodec;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.Video;
import com.twilio.video.VideoScaleType;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoView;
import com.twilio.video.Vp8Codec;
import com.twilio.video.Vp9Codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import to.popin.androidsdk.BuildConfig;
import to.popin.androidsdk.R;
import to.popin.androidsdk.common.Analytics;
import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.common.MainThreadBus;
import tvi.webrtc.VideoSink;


public class CallActivity extends AppCompatActivity implements CallActivityView {



    VideoView primaryVideoView;
    VideoView thumbnailVideoView;
    LinearLayout lytWaiting;
    FloatingActionButton connectActionFab;
    FloatingActionButton switchCameraActionFab;
    FloatingActionButton localVideoActionFab;
    FloatingActionButton muteActionFab;
    FloatingActionButton chatFab;
    TextView textTrackDisabled;


    private String accessToken;
    private Room room;
    private LocalParticipant localParticipant;
    private int savedVolumeControlStream;
    private VideoSink localVideoSink;
    private boolean disconnectedFromOnDestroy;
    private boolean enableAutomaticSubscription = true;
    private boolean isUsingFrontCamera = true;
    private boolean localIsInPrimary = false;
    private String remoteParticipantIdentity;

    private boolean isThumbnailClicked = false;

    private VideoTrack remoteVideoTrack;

    private CallAudioManager callAudioManager;
    private CallLocalTrackManager callLocalTrackManager;
    private CallRemoteParticipantManager callRemoteParticipantManager;
    private CallRoomManager callRoomManager;
    private Analytics analytics;
    private int call_id;
    private boolean mutedAudio = false, mutedVideo = false, remoteParticipantPresent = false;

    private CallPresenter callPresenter;
    private Device device;

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        device = new Device(this);
        callPresenter = new CallPresenter(this, this, new CallInteractor(this,device ), device, new MainThreadBus());
        loadViews();
        analytics = new Analytics();
        if (BuildConfig.DEBUG) {
            Video.setLogLevel(LogLevel.DEBUG);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getLifecycle().addObserver(callPresenter);

        List<Class<? extends AudioDevice>> preferredDevices = new ArrayList<>();
        preferredDevices.add(AudioDevice.BluetoothHeadset.class);
        preferredDevices.add(AudioDevice.WiredHeadset.class);
        preferredDevices.add(AudioDevice.Speakerphone.class);
        callAudioManager = new CallAudioManager(new AudioSwitch(getApplicationContext(), false, focusChange -> {
        }, preferredDevices));
        callLocalTrackManager = new CallLocalTrackManager(this, device);
        callRemoteParticipantManager = new CallRemoteParticipantManager(new CallRemoteParticipantManager.RemoteParticipantListener() {
            @Override
            public void addVideo(RemoteVideoTrack remoteVideoTrack) {
                addRemoteParticipantVideo(remoteVideoTrack);
                runOnUiThread(() -> textTrackDisabled.setVisibility(View.GONE));
            }

            @Override
            public void removeVideo(RemoteVideoTrack remoteVideoTrack) {
                removeParticipantVideo(remoteVideoTrack);
                runOnUiThread(() -> textTrackDisabled.setVisibility(View.VISIBLE));

                //Ashwin

            }

            @Override
            public void videoFail() {
                runOnUiThread(() -> textTrackDisabled.setVisibility(View.VISIBLE));
            }
        });
        callRoomManager = new CallRoomManager(new CallRoomManager.RoomUpdateListener() {
            @Override
            public void onConnected(Room room) {
                localParticipant = room.getLocalParticipant();
                setTitle(room.getName());

                for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {
                    addRemoteParticipant(remoteParticipant);
                    break;
                }
            }

            @Override
            public void showProgressBar(boolean visible) {
                lytWaiting.setVisibility(visible ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onConnectFail(Room room) {
                callAudioManager.deactivate();
                initializeUI();
            }

            @Override
            public void onDisconnected(Room room) {
                localParticipant = null;
                lytWaiting.setVisibility(View.GONE);
                // Only reinitialize the UI if disconnect was not called from onDestroy()
                if (!disconnectedFromOnDestroy) {
                    callAudioManager.deactivate();
                    initializeUI();
                    moveLocalVideoToPrimaryView();
                }
            }

            @Override
            public void onRemoteConnected(RemoteParticipant remoteParticipant) {
                addRemoteParticipant(remoteParticipant);
                runOnUiThread(() -> textTrackDisabled.setVisibility(View.GONE));
            }

            @Override
            public void onRemoteDisconnected(RemoteParticipant remoteParticipant) {
                removeRemoteParticipant(remoteParticipant);
                runOnUiThread(() -> textTrackDisabled.setVisibility(View.VISIBLE));

                try { //ASHWIN
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        savedVolumeControlStream = getVolumeControlStream();
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        call_id = getIntent().getIntExtra("CALL_ID", 0);
        if (!checkPermissionForCameraMicrophoneAndBluetooth()) {
            analytics.logEvent("popin_in_call_fail_permission", "call_id", call_id);
            Toast.makeText(this, "Camera permission needed.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            createAudioAndVideoTracks();
            analytics.logEvent("popin_in_call_start", "call_id", call_id);
            callPresenter.getAccessToken();
        }
        initializeUI();

    }


    private boolean checkPermissionForCameraMicrophoneAndBluetooth() {
        return true;
//        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
//        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            int resultBluetooth = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
//            return resultCamera == PackageManager.PERMISSION_GRANTED
//                    && resultMic == PackageManager.PERMISSION_GRANTED
//                    && resultBluetooth == PackageManager.PERMISSION_GRANTED;
//        } else {
//            return resultCamera == PackageManager.PERMISSION_GRANTED
//                    && resultMic == PackageManager.PERMISSION_GRANTED;
//        }
    }

    private void loadViews() {
        primaryVideoView = findViewById(R.id.primary_video_view);
        thumbnailVideoView= findViewById(R.id.thumbnail_video_view);
        lytWaiting= findViewById(R.id.lytWaiting);
        connectActionFab= findViewById(R.id.connect_action_fab);
        switchCameraActionFab= findViewById(R.id.switch_camera_action_fab);
        localVideoActionFab= findViewById(R.id.local_video_action_fab);
        muteActionFab= findViewById(R.id.mute_action_fab);
        chatFab= findViewById(R.id.action_chat);
        textTrackDisabled= findViewById(R.id.textTrackDisabled);

        connectActionFab.setOnClickListener(view -> {
            analytics.logEvent("popin_in_call_disconnect_click", "call_id", call_id);
            if (room != null) {
                room.disconnect();
                finish();
            }
            callPresenter.disconnectCall();
        });

        switchCameraActionFab.setOnClickListener(view -> {
            analytics.logEvent("popin_in_call_switch_camera", "call_id", call_id);
            callLocalTrackManager.switchCamera(isFrontCamera -> {
                isUsingFrontCamera = !isUsingFrontCamera;
                if (localIsInPrimary) {
                    primaryVideoView.setMirror(isUsingFrontCamera);
                    thumbnailVideoView.setMirror(false);
                } else {
                    primaryVideoView.setMirror(false);
                    thumbnailVideoView.setMirror(isUsingFrontCamera);
                }
            });
        });

        localVideoActionFab.setOnClickListener(view -> {
            analytics.logEvent("popin_in_call_toggle_camera", "call_id", call_id);
            callLocalTrackManager.toggleCamera(enable -> {
                mutedVideo = !enable;
                int icon;
                if (enable) {
                    icon = R.drawable.ic_videocam_white_24dp;
                    switchCameraActionFab.show();
                } else {
                    icon = R.drawable.ic_camera_off;
                    switchCameraActionFab.hide();
                }
                localVideoActionFab.setImageDrawable(ContextCompat.getDrawable(CallActivity.this, icon));
            });
        });

        muteActionFab.setOnClickListener(view -> {
            analytics.logEvent("popin_in_call_mute_audio", "call_id", call_id);
            callLocalTrackManager.toggleMute(enable -> {
                mutedAudio = !enable;
                int icon = enable ? R.drawable.ic_mic_white_24dp : R.drawable.ic_mic_off;
                muteActionFab.setImageDrawable(ContextCompat.getDrawable(CallActivity.this, icon));
            });
        });

        thumbnailVideoView.setOnClickListener(view -> {
            analytics.logEvent("popin_in_call_interchange_primary", "call_id", call_id);
            if (!isThumbnailClicked) {
                //move local to primary
                localIsInPrimary = true;
                callLocalTrackManager.changeVideoSink(thumbnailVideoView, primaryVideoView);
                localVideoSink = primaryVideoView;
                if (remoteVideoTrack != null) {
                    primaryVideoView.setMirror(isUsingFrontCamera);
                    thumbnailVideoView.setMirror(false);
                    remoteVideoTrack.removeSink(primaryVideoView);
                    remoteVideoTrack.addSink(thumbnailVideoView);
                }

            } else {
                //move local to thumbnail
                localIsInPrimary = false;
                callLocalTrackManager.changeVideoSink(primaryVideoView, thumbnailVideoView);
                if (remoteVideoTrack != null) {
                    //move remote to primary
                    primaryVideoView.setMirror(false);
                    thumbnailVideoView.setMirror(isUsingFrontCamera);
                    primaryVideoView.setMirror(false);
                    remoteVideoTrack.removeSink(thumbnailVideoView);
                    remoteVideoTrack.addSink(primaryVideoView);
                }
            }

            isThumbnailClicked = !isThumbnailClicked;
        });

        chatFab.setOnClickListener(view -> {
            analytics.logEvent("popin_in_call_pip", "call_id", call_id);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enterPip();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        analytics.logEvent("popin_in_call_resume", "call_id", call_id);
        if (!mutedAudio) {
            callLocalTrackManager.getLocalAudioTrack().enable(true);
        }
        if (!mutedVideo) {
            callLocalTrackManager.getLocalVideoTrack().enable(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isInPictureInPictureMode()) {
            analytics.logEvent("popin_in_call_pause", "call_id", call_id);
            callLocalTrackManager.getLocalAudioTrack().enable(false);
            callLocalTrackManager.getLocalVideoTrack().enable(false);
        }

    }

    @Override
    protected void onDestroy() {
        analytics.logEvent("popin_in_call_destroy", "call_id", call_id);
        callAudioManager.stop();
        setVolumeControlStream(savedVolumeControlStream);
        if (room != null && room.getState() != Room.State.DISCONNECTED) {
            room.disconnect();
            disconnectedFromOnDestroy = true;
        }
        callLocalTrackManager.releaseLocalAudioAndVideTracks();
        super.onDestroy();
    }

    private void createAudioAndVideoTracks() {
        callLocalTrackManager.createAudioAndVideoTracks((localVideoTrack, mirror) -> {
            if (mirror) {
                primaryVideoView.setMirror(mirror);
            }
            localVideoTrack.addSink(primaryVideoView);
        });

        localVideoSink = primaryVideoView;
    }


    /*
     * The initial state when there is no active room.
     */
    private void initializeUI() {

        switchCameraActionFab.show();
        localVideoActionFab.show();
        muteActionFab.show();


    }

    private void setDisconnectAction() {
        connectActionFab.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_call_end_white_24px));
        connectActionFab.show();
    }

    private void addRemoteParticipant(RemoteParticipant remoteParticipant) {
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            //Multiple participants are not currently support in this UI
            Toast.makeText(this, "Multiple participants error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        remoteParticipantIdentity = remoteParticipant.getIdentity();
        if (remoteParticipant.getRemoteVideoTracks().size() > 0) {
            RemoteVideoTrackPublication remoteVideoTrackPublication =
                    remoteParticipant.getRemoteVideoTracks().get(0);
            if (remoteVideoTrackPublication.isTrackSubscribed()) {
                addRemoteParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
            }
        }

        remoteParticipant.setListener(callRemoteParticipantManager);
        analytics.logEvent("popin_in_call_connected", "call_id", call_id);
    }

    private void addRemoteParticipantVideo(VideoTrack videoTrack) {
        this.remoteVideoTrack = videoTrack;
        if (thumbnailVideoView.getVisibility() == View.GONE) {
            thumbnailVideoView.setVisibility(View.VISIBLE);
            callLocalTrackManager.changeVideoSink(primaryVideoView, thumbnailVideoView);
            localVideoSink = thumbnailVideoView;
            Log.e("MOVE_REMOTE_TO_PRIMARY", ">ISFRONTCAM" + isUsingFrontCamera);
            thumbnailVideoView.setMirror(isUsingFrontCamera);
        }
        primaryVideoView.setMirror(false);
        videoTrack.addSink(primaryVideoView);
        analytics.logEvent("popin_in_call_remote_shown", "call_id", call_id);
    }


    private void removeRemoteParticipant(RemoteParticipant remoteParticipant) {
        if (!remoteParticipant.getIdentity().equals(remoteParticipantIdentity)) {
            return;
        }

        if (!remoteParticipant.getRemoteVideoTracks().isEmpty()) {
            RemoteVideoTrackPublication remoteVideoTrackPublication =
                    remoteParticipant.getRemoteVideoTracks().get(0);
            if (remoteVideoTrackPublication.isTrackSubscribed()) {
                removeParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
            }
        }
        moveLocalVideoToPrimaryView();
        analytics.logEvent("popin_in_call_remote_gone", "call_id", call_id);
    }

    private void removeParticipantVideo(VideoTrack videoTrack) {
        videoTrack.removeSink(primaryVideoView);
    }

    private void moveLocalVideoToPrimaryView() {
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            thumbnailVideoView.setVisibility(View.GONE);
            this.localIsInPrimary = true;
            callLocalTrackManager.changeVideoSink(thumbnailVideoView, primaryVideoView);
            localVideoSink = primaryVideoView;
            Log.e("LOCAL_IN_PRIMARY", ">IS_FRONT_CAM" + isUsingFrontCamera);
            if (isUsingFrontCamera) {
                primaryVideoView.setMirror(true);
            }
        }
    }

    @Override
    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    @Override
    public void connectRoom(String roomName) {
        analytics.logEvent("popin_in_call_start_room", "call_id", call_id);
        callAudioManager.activate();
        primaryVideoView.setVideoScaleType(VideoScaleType.ASPECT_BALANCED);


        ConnectOptions.Builder connectOptionsBuilder =
                new ConnectOptions.Builder(accessToken).roomName(roomName);

        if (callLocalTrackManager.getLocalAudioTrack() != null) {
            connectOptionsBuilder.audioTracks(Collections.singletonList(callLocalTrackManager.getLocalAudioTrack()));
        }

        if (callLocalTrackManager.getLocalVideoTrack() != null) {

            connectOptionsBuilder.videoTracks(Collections.singletonList(callLocalTrackManager.getLocalVideoTrack()));
        }
//resolution: 960x540 (quater of FullHD)
        connectOptionsBuilder.preferAudioCodecs(Collections.singletonList(new OpusCodec()));
        connectOptionsBuilder.preferVideoCodecs(Arrays.asList(new Vp9Codec(), new H264Codec(),
                new Vp8Codec()));
        connectOptionsBuilder.encodingParameters(callLocalTrackManager.getEncodingParameters());
        connectOptionsBuilder.enableAutomaticSubscription(enableAutomaticSubscription);


        room = Video.connect(this, connectOptionsBuilder.build(), callRoomManager);
        setDisconnectAction();


    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        analytics.logEvent("popin_in_call_pip_change", "call_id", call_id);
        if (isInPictureInPictureMode) {
            // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.

            int size = dpToPx(60);
            thumbnailVideoView.getLayoutParams().height = size;
            thumbnailVideoView.getLayoutParams().width = size;
            thumbnailVideoView.requestLayout();

            switchCameraActionFab.hide();
            muteActionFab.hide();
            localVideoActionFab.hide();
            connectActionFab.hide();
            chatFab.hide();


        } else {
            int size = dpToPx(96);
            thumbnailVideoView.getLayoutParams().height = size;
            thumbnailVideoView.getLayoutParams().width = size;
            thumbnailVideoView.requestLayout();

            switchCameraActionFab.show();
            muteActionFab.show();
            localVideoActionFab.show();
            connectActionFab.show();
            chatFab.show();


        }

        if (getLifecycle().getCurrentState() == Lifecycle.State.CREATED) {
            callPresenter.disconnectCall();
            finishAndRemoveTask();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        }
    }

    @Override
    public void closeActivity() {
        finish();
    }

    @Override
    public void showMessage(String message) {
        runOnUiThread(() -> Toast.makeText(CallActivity.this, message, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onBackPressed() {
        analytics.logEvent("popin_in_call_back_press", "call_id", call_id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (room.getState().equals(Room.State.CONNECTED) ||
                    room.getState().equals(Room.State.CONNECTING) ||
                    room.getState().equals(Room.State.RECONNECTING)) {
                enterPip();
            }
        } else {
            finish();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onUserLeaveHint() {
        enterPip();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void enterPip() {

        Display d = getWindowManager()
                .getDefaultDisplay();
        Point p = new Point();
        d.getSize(p);
        int width = p.x;
        int height = p.y;

        Rational ratio
                = new Rational(width, height);
        PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
        pipBuilder
                .setAspectRatio(ratio)
                .build();
        enterPictureInPictureMode(pipBuilder.build());
    }
}


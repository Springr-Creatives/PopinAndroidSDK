package to.popin.androidsdk;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.otto.Subscribe;

import java.util.List;

import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.common.MainThreadBus;
import to.popin.androidsdk.events.CallCancelEvent;
import to.popin.androidsdk.models.FastCallModel;
import to.popin.androidsdk.schedule.ScheduleInteractor;
import to.popin.androidsdk.schedule.SchedulePresenter;
import to.popin.androidsdk.session.PopinSession;

public class Popin {
    private Context context;
    private PopinSession popinSession;
    private ConnectionWorker connectionWorker;

    private ConferenceWorker conferenceWorker;
    private PopinEventsListener popinEventsListener;
    private MainThreadBus mainThreadBus;
    private SchedulePresenter schedulePresenter;
    private CallAcceptanceWaitHandler waitHandler;
    private static Popin popin;
    private boolean hideDisconnectButton = false;
    private boolean hideScreenShareButton = false;
    private boolean hideFlipCameraButton = false;
    private boolean hideMuteVideoButton = false;
    private boolean hideMuteAudioButton = false;
    private boolean hideBackButton = false;

    public static synchronized Popin init(Context context) {
        if (popin == null) {
            popin = new Popin(context, "", "");
        }
        return popin;
    }

    public static synchronized Popin init(Context context, String userName, String contactInfo) {
        if (popin == null) {
            popin = new Popin(context, userName, contactInfo);
        } else {
            popin.popinSession.updateSession(userName, contactInfo, () -> {
                popin.connectionWorker = new ConnectionWorker(popin.popinSession.getContext(), popin.popinSession.getDevice());
            });
        }
        return popin;
    }

    public static synchronized Popin init(Context context, String userName, String contactInfo, PopinInitListener initListener) {
        if (popin == null) {
            popin = new Popin(context, userName, contactInfo, initListener);
        } else {
            popin.popinSession.updateSession(userName, contactInfo, () -> {
                popin.connectionWorker = new ConnectionWorker(popin.popinSession.getContext(), popin.popinSession.getDevice());
                if (initListener != null) {
                    initListener.onInitComplete();
                }
            });
        }
        return popin;
    }

    public static Popin getInstance() {
        if (popin == null) {
            Log.e("Exception", "PopinSDK Not Initialised");
            throw new RuntimeException("PopinSDK Not Initialised");
        }
        return popin;
    }


    public Popin(Context context, String name, String mobile) {
        this(context, name, mobile, null);
    }

    public Popin(Context context, String name, String mobile, PopinInitListener initListener) {
        try {
            Log.e("PACKAGE", "INITIALISATION");
            this.context = context;
            Device device = new Device(context);
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            int apiKey = applicationInfo.metaData.getInt("to.popin.androidsdk.POPIN_TOKEN");
            device.setSeller(apiKey);
            mainThreadBus = MainThreadBus.getInstance(); // Use the singleton instance
            popinSession = new PopinSession(context, device, name, mobile);
            popinSession.createSession(() -> {
                connectionWorker = new ConnectionWorker(popinSession.getContext(), popinSession.getDevice());
                conferenceWorker = new ConferenceWorker(popinSession.getContext(),popinSession.getDevice());
                if (initListener != null) {
                    initListener.onInitComplete();
                }
            });
            schedulePresenter = new SchedulePresenter(new ScheduleInteractor(context, device));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Dexter.withContext(context)
                        .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.BLUETOOTH_CONNECT)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                            }
                        }).check();
            } else {
                Dexter.withContext(context)
                        .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                            }
                        }).check();
            }
            mainThreadBus.register(this);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void cancelCall() {
        try {
            waitHandler.stopWaitingForAcceptance();
            connectionWorker.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public void setDisconnectButtonVisibility(boolean visible) {
        hideDisconnectButton = !visible;
    }

    public void setHideDisconnectButton(boolean hideDisconnectButton) {
        this.hideDisconnectButton = hideDisconnectButton;
    }

    public void setHideScreenShareButton(boolean hideScreenShareButton) {
        this.hideScreenShareButton = hideScreenShareButton;
    }

    public void setHideFlipCameraButton(boolean hideFlipCameraButton) {
        this.hideFlipCameraButton = hideFlipCameraButton;
    }

    public void setHideMuteVideoButton(boolean hideMuteVideoButton) {
        this.hideMuteVideoButton = hideMuteVideoButton;
    }

    public void setHideMuteAudioButton(boolean hideMuteAudioButton) {
        this.hideMuteAudioButton = hideMuteAudioButton;
    }

    public void setHideBackButton(boolean hideBackButton) {
        this.hideBackButton = hideBackButton;
    }

    public void startConference(int agentID, String slug, String xApiKey,PopinConferenceEventListener popinConferenceEventListener) {
        conferenceWorker.joinConference(slug, agentID, xApiKey, new ConferenceWorker.ConferenceJoinListener() {
            @Override
            public void onConferenceJoined(FastCallModel fastCallModel) {
                popinConferenceEventListener.onConferenceJoined();
                Intent intent = new Intent(context, to.popin.androidsdk.call.CallActivity.class);
                intent.putExtra("CALL", fastCallModel);
                intent.putExtra("HIDE_DISCONNECT_BUTTON", hideDisconnectButton);
                intent.putExtra("HIDE_SCREEN_SHARE_BUTTON", hideScreenShareButton);
                intent.putExtra("HIDE_FLIP_CAMERA_BUTTON", hideFlipCameraButton);
                intent.putExtra("HIDE_MUTE_VIDEO_BUTTON", hideMuteVideoButton);
                intent.putExtra("HIDE_MUTE_AUDIO_BUTTON", hideMuteAudioButton);
                intent.putExtra("HIDE_BACK_BUTTON", hideBackButton);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            @Override
            public void onConferenceJoinFailed() {
                popinConferenceEventListener.onConferenceFailed();
            }
        });
    }
    public void startCall(PopinEventsListener popinEventsListener) {
        this.popinEventsListener = popinEventsListener;
        connectionWorker.startConnection(new ConnectionWorker.CreateConnectionListener() {
            @Override
            public void onConnectionStarted(int call_queue_id) {
                popinEventsListener.onCallStart();

                waitHandler = new CallAcceptanceWaitHandler(context, popin.popinSession.getDevice(), Looper.getMainLooper(), call_queue_id, new CallAcceptanceWaitHandler.CallAcceptanceListener() {
                    @Override
                    public void onQueuePositionChange(int position) {
                        popinEventsListener.onQueuePositionChanged(position);
                    }

                    @Override
                    public void onCallAccepted(int call_id) {
                        startCall(call_id);
                    }

                    @Override
                    public void onExpertBusy() {
                        popinEventsListener.onAllExpertsBusy();
                    }
                });
                waitHandler.startWaitingForAcceptance();
            }

            @Override
            public void onConnectionFailed() {
                popinEventsListener.onCallFailed();
            }
        });
    }

    @Subscribe
    public void onCallEnd(CallCancelEvent callCancelEvent) {
        popinEventsListener.onCallDisconnected();
    }


    public void setRating(int rating) {

    }

    public void getAvailableScheduleSlots(PopinScheduleListener popinScheduleListener) {
        schedulePresenter.getScheduleSlots(popinScheduleListener);
    }

    public void createSchedule(String time, PopinCreateScheduleListener popinCreateScheduleListener) {
        schedulePresenter.createSchedule(time, popinCreateScheduleListener);
    }

    private void startCall(int call_id) {
        connectionWorker.getCallDetails(call_id, new ConnectionWorker.CallDetailsListener() {
            @Override
            public void onCallDetails(FastCallModel fastCallModel) {
                /*private boolean hideScreenShareButton = false;
    private boolean hideFilpCameraButton = false;
    private boolean hideMuteVideoButton = false;
    private boolean hideMuteAudioButton = false;
    private boolean hideBackButton = false;
    */

                popinEventsListener.onCallConnected();
                Intent intent = new Intent(context, to.popin.androidsdk.call.CallActivity.class);
                intent.putExtra("CALL", fastCallModel);
                intent.putExtra("HIDE_DISCONNECT_BUTTON", hideDisconnectButton);
                intent.putExtra("HIDE_SCREEN_SHARE_BUTTON", hideScreenShareButton);
                intent.putExtra("HIDE_FLIP_CAMERA_BUTTON", hideFlipCameraButton);
                intent.putExtra("HIDE_MUTE_VIDEO_BUTTON", hideMuteVideoButton);
                intent.putExtra("HIDE_MUTE_AUDIO_BUTTON", hideMuteAudioButton);
                intent.putExtra("HIDE_BACK_BUTTON", hideBackButton);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            @Override
            public void onCallDetailsFail() {
                popinEventsListener.onCallFailed();
            }
        });

    }
}

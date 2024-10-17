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
    private PopinEventsListener popinEventsListener;
    private MainThreadBus mainThreadBus;
    private SchedulePresenter schedulePresenter;
    private CallAcceptanceWaitHandler waitHandler;
    private static Popin popin;


    public static synchronized Popin init(Context context) {
        if (popin == null) {
            popin = new Popin(context);
        }
        return popin;
    }

    public static synchronized Popin init(Context context, String userName, String contactInfo) {
        if (popin == null) {
            popin = new Popin(context);
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


    public Popin(Context context) {
        try {
            Log.e("PACKAGE", "INITIALISATION");
            this.context = context;
            Device device = new Device(context);
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null) {
                int apiKey = applicationInfo.metaData.getInt("to.popin.androidsdk.POPIN_TOKEN");
                device.setSeller(apiKey);
                mainThreadBus = new MainThreadBus();
                popinSession = new PopinSession(context, device);
                popinSession.updateSession(() -> {
                    connectionWorker = new ConnectionWorker(popinSession.getContext(), popinSession.getDevice());
                });
                schedulePresenter = new SchedulePresenter(new ScheduleInteractor(context, device));
            }
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
        } catch (Exception e) {

        }
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
                popinEventsListener.onCallConnected();
                Intent intent = new Intent(context, to.popin.androidsdk.call.CallActivity.class);
                intent.putExtra("CALL", fastCallModel);
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

package to.popin.androidsdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.common.MainThreadBus;
import to.popin.androidsdk.session.PopinSession;

public class Popin {

    private PopinSession popinSession;
    private PusherWorker pusherWorker;
    private ConnectionWorker connectionWorker;
    private PopinEventsListener popinEventsListener;
    private MainThreadBus mainThreadBus;
    private static Popin popin;

    public static synchronized Popin initialize(Context context) {
        if (popin == null) {
            popin = new Popin(context);
        }
        return popin;
    }

    public static Popin getInstance() {
        return popin;
    }


    public Popin(Context context) {
        try {
            Device device = new Device(context);
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null) {
                int apiKey = applicationInfo.metaData.getInt("to.popin.androidsdk.POPIN_TOKEN");
                device.setSeller(apiKey);
                mainThreadBus = new MainThreadBus();
                popinSession = new PopinSession(context, device);
                popinSession.updateSession();

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void startConnection(PopinEventsListener popinEventsListener) {
        this.popinEventsListener = popinEventsListener;
        pusherWorker = new PusherWorker(popinSession.getContext(), popinSession.getDevice(), () -> connectionWorker.startConnection(), popinEventsListener);

    }


    public static void startCall(PopinEventsListener popinEventsListener) {

    }
}

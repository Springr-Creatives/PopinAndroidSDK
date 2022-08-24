package to.popin.androidsdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.session.PopinSession;

public class Popin {

    private static PopinSession popinSession;

    public static void initialize(Context context) {
        try {
            Device device = new Device(context);
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null) {
                int apiKey = applicationInfo.metaData.getInt("to.popin.androidsdk.POPIN_TOKEN");
                device.setSeller(apiKey);
                popinSession = new PopinSession(context, device);
                popinSession.updateSession();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startCall(PopinEventsListener popinEventsListener) {


    }
}

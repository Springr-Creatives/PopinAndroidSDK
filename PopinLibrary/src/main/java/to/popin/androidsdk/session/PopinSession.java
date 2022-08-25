package to.popin.androidsdk.session;

import android.content.Context;

import to.popin.androidsdk.common.Device;

public class PopinSession {
    private final PopinSessionInteractor popinSessionInteractor;
    private final Device device;
    private final Context context;

    public PopinSession(Context context , Device device) {
        this.device = device;
        this.context=context;
        this.popinSessionInteractor = new PopinSessionInteractor(context, device);
    }

    public void updateSession() {
        if (device.getToken().length() == 0) {
            popinSessionInteractor.registerForToken(device.getSeller());
        }
    }


    public Device getDevice() {
        return device;
    }

    public Context getContext() {
        return context;
    }
}

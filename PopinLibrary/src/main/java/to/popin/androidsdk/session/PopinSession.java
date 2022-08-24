package to.popin.androidsdk.session;

import android.content.Context;

import to.popin.androidsdk.common.Device;

public class PopinSession {
    private final PopinSessionInteractor popinSessionInteractor;
    private final Device device;

    public PopinSession(Context context , Device device) {
        this.device = device;
        this.popinSessionInteractor = new PopinSessionInteractor(context, device);
    }

    public void updateSession() {
        if (device.getToken().length() == 0) {
            popinSessionInteractor.registerForToken(device.getSeller());
        }
    }
}

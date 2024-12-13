package to.popin.androidsdk.session;

import android.content.Context;

import to.popin.androidsdk.common.Device;

public class PopinSession {
    private final PopinSessionInteractor popinSessionInteractor;
    private final Device device;
    private final Context context;
    private final String name;
    private final String mobile;

    public PopinSession(Context context, Device device, String name, String mobile) {
        this.device = device;
        this.context = context;
        this.name = name;
        this.mobile = mobile;
        this.popinSessionInteractor = new PopinSessionInteractor(context, device);
    }

    public void updateSession(PopinSessionInteractor.RegistrationListener registrationListener) {
        if (!name.isEmpty()) {
            popinSessionInteractor.registerForToken(device.getSeller(), name, mobile, registrationListener);
        } else {
            registrationListener.onRegistered();
        }
    }


    public Device getDevice() {
        return device;
    }

    public Context getContext() {
        return context;
    }
}

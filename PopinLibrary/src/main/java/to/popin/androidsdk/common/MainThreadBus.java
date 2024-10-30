package to.popin.androidsdk.common;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class MainThreadBus extends Bus {
    private static MainThreadBus instance;  // Singleton instance
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    // Private constructor to prevent direct instantiation
    private MainThreadBus() {
    }

    // Public method to get the singleton instance
    public static synchronized MainThreadBus getInstance() {
        if (instance == null) {
            instance = new MainThreadBus();
        }
        return instance;
    }

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mHandler.post(() -> MainThreadBus.super.post(event));
        }
    }
}

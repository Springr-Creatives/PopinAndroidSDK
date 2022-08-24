package to.popin.androidsdk.call;

import android.content.Context;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.squareup.otto.Subscribe;

import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.common.MainThreadBus;
import to.popin.androidsdk.events.CallCancelEvent;
import to.popin.androidsdk.models.LiveModel;
import to.popin.androidsdk.models.TalkModel;


public class CallPresenter implements LifecycleObserver {
    private final CallInteractor callInteractor;

    private final Context context;
    private final Device device;
    private final MainThreadBus mainThreadBus;
    private CallActivityView callActivityView;
    private int call_id;
    public CallPresenter(Context context, CallActivityView callActivityView, CallInteractor callInteractor, Device device, MainThreadBus mainThreadBus) {
        this.callActivityView = callActivityView;
        this.callInteractor = callInteractor;
        this.context = context;
        this.device = device;
        this.mainThreadBus = mainThreadBus;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        mainThreadBus.unregister(this);
        callActivityView = null;

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        mainThreadBus.register(this);
    }

    @Subscribe
    public void callCancelled(CallCancelEvent callCancelEvent) {
        if (callActivityView != null) {
            if (callCancelEvent.call_id == call_id) {
                callActivityView.showMessage("Call disconnected");
                callActivityView.closeActivity();
            }
        }
    }

    public void loadLiveStream(LiveModel liveModel) {
        callActivityView.setAccessToken(liveModel.token);
        callActivityView.connectRoom(liveModel.room);
    }

    public void getAccessToken(int _call_id) {
        call_id = _call_id;
        callInteractor.getAccessToken(call_id, new CallInteractor.AccessTokenListener() {
            @Override
            public void onAccessToken(TalkModel talkModel) {
                if (callActivityView != null) {
                    callActivityView.setAccessToken(talkModel.access_token);
                    callActivityView.connectRoom(talkModel.room);
                }
            }

            @Override
            public void onInvalidCall(int status) {
                if (callActivityView != null) {
                    callActivityView.showMessage("Call expired, status: " + status);
                    callActivityView.closeActivity();
                }
            }

            @Override
            public void onNetworkError() {
                if (callActivityView != null) {
                    callActivityView.showMessage("Please check your network and try again later");
                    callActivityView.closeActivity();
                }
            }
        });
    }



    public void disconnectCall() {
        callInteractor.disconnectCall(call_id, () -> {
            if (callActivityView != null) {
                callActivityView.closeActivity();
            }
        });
    }


}

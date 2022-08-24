package to.popin.androidsdk;

public interface PopinEventsListener {
    void onConnectionEstablished();
    void onAllExpertsBusy();
    void onCallConnected();
    void onCallDisconnected();
    void onCallFail();
}

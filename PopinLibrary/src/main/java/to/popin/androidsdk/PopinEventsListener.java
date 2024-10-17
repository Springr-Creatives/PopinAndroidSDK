package to.popin.androidsdk;

public interface PopinEventsListener {
    void onCallStart();
    void onQueuePositionChanged(int position);
    void onAllExpertsBusy();
    void onCallConnected();
    void onCallFailed();
    void onCallDisconnected();
}

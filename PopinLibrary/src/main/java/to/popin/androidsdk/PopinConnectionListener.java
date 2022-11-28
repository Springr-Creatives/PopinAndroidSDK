package to.popin.androidsdk;

interface PopinConnectionListener {
 void onExpertsBusy();
 void onConnectionEstablished();
 void onCallDisconnected(int call_id);
}

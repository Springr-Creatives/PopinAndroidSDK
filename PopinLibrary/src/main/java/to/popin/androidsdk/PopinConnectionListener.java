package to.popin.androidsdk;

interface PopinConnectionListener {
 void onExpertsBusy();
 void onConnectionEstablished(int call_id);
 void onCallDisconnected(int call_id);
}

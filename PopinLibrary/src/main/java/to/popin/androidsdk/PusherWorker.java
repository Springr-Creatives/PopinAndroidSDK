package to.popin.androidsdk;

import android.content.Context;
import android.util.Log;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import to.popin.androidsdk.common.Device;

public class PusherWorker {
    private final Context context;
    private final Device device;
    private Pusher pusher;
    private Channel privateChannel;
    private final PusherConnectionListener pusherConnectionWorker;
    private final PopinConnectionListener popinConnectionListener;

    public PusherWorker(Context context, Device device, PusherConnectionListener pusherConnectionWorker,PopinConnectionListener popinConnectionListener) {
        this.context =context;
        this.device = device;
        this.pusherConnectionWorker = pusherConnectionWorker;
        this.popinConnectionListener = popinConnectionListener;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + device.getToken());
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept", "application/json");
        HttpAuthorizer authorizer = new HttpAuthorizer(context.getString(R.string.server_url) + "/api/v1/user/channel/authenticate");
        authorizer.setHeaders(headers);
        PusherOptions options = new PusherOptions().setAuthorizer(authorizer).setCluster("ap2");
        try {
            pusher = new Pusher(context.getString(R.string.pusher_key), options);
            pusher.connect(new ConnectionEventListener() {
                @Override
                public void onConnectionStateChange(ConnectionStateChange change) {
                    if (change.getCurrentState() == ConnectionState.CONNECTED) {
                        pusherConnectionWorker.pusherConnected();
                        subscribeChannel();
                    }
                }

                @Override
                public void onError(String message, String code, Exception e) {
                    Log.e("Pusher", "There was a problem connecting! " +
                            "\ncode: " + code +
                            "\nmessage: " + message +
                            "\nException: " + e
                    );
                }
            }, ConnectionState.ALL);

        } catch (Exception e) {
            Log.e("ERROR_PUSHER", ">" + e.getMessage());
        }
    }

    private void subscribeChannel() {
        if (pusher.getPrivateChannel(device.getChannel()) == null) {
            privateChannel = pusher.subscribePrivate(device.getChannel(), new PrivateChannelEventListener() {
                @Override
                public void onAuthenticationFailure(String message, Exception e) {
                    Log.e("Pusher", "Subscribe fail>" + message + e.getMessage());
                }

                @Override
                public void onSubscriptionSucceeded(String channelName) {
                    Log.e("SUBSCRIBE", "SUCCESS");
                    bindChannel();
                }

                @Override
                public void onEvent(PusherEvent event) {
                    Log.e("Pusher", "Event1>" + event.toString());
                }
            });
        }
    }

    private void bindChannel() {
        privateChannel.bind("user.message", new PrivateChannelEventListener() {
            @Override
            public void onAuthenticationFailure(String message, Exception e) {
                Log.e("Pusher", "PEvent1");
            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {
                Log.e("Pusher", "PEvent2");
            }

            @Override
            public void onEvent(PusherEvent event) {
                Log.e("POPIN","MSG");
                if (event.getEventName().equals("user.message")) {
                    try {
                        JSONObject eventObj = new JSONObject(event.getData());
                        JSONObject message = eventObj.getJSONObject("message");
                        int type = message.getInt("type");
                        if (type == 3) { //connected
                            Log.e("POPIN","CONNECTED");
                            popinConnectionListener.onConnectionEstablished();
                        } else if (type == 15) {
                            popinConnectionListener.onExpertsBusy();
                        }

                    } catch (JSONException e) {
                        Log.e("POPIN","MESG_ERR");
                        e.printStackTrace();
                    }
                }
            }
        });


        privateChannel.bind("user.call_cancel", new PrivateChannelEventListener() {

            @Override
            public void onEvent(PusherEvent event) {
                Log.e("EVENT", "call_cancel RECEIVED");
                if (event.getEventName().equals("user.call_cancel")) {
                    try {
                        JSONObject eventObj = new JSONObject(event.getData());
                        int call_id = eventObj.getInt("call_id");
                        popinConnectionListener.onCallDisconnected(call_id);
                    } catch (Exception e) {
                        Log.e("ERROR", "CALL_CANCEL_PUSHER_ERR");
                    }
                }
            }

            @Override
            public void onSubscriptionSucceeded(String channelName) {

            }

            @Override
            public void onAuthenticationFailure(String message, Exception e) {

            }
        });
    }
    interface PusherConnectionListener {
        void pusherConnected();
    }
}

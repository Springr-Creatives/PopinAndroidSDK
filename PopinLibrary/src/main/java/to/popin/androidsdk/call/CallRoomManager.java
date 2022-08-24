package to.popin.androidsdk.call;

import android.util.Log;

import androidx.annotation.NonNull;

import com.twilio.video.RemoteParticipant;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;

public class CallRoomManager implements Room.Listener {
    private final String TAG = "CALL_ROOM_MANAGER";
    private final RoomUpdateListener roomUpdateListener;

    public CallRoomManager(RoomUpdateListener roomUpdateListener) {
        this.roomUpdateListener = roomUpdateListener;
    }

    @Override
    public void onConnected(Room room) {
        roomUpdateListener.onConnected(room);
    }

    @Override
    public void onReconnecting(
            @NonNull Room room, @NonNull TwilioException twilioException) {
        roomUpdateListener.showProgressBar(true);
    }

    @Override
    public void onReconnected(@NonNull Room room) {
        roomUpdateListener.showProgressBar(false);
    }

    @Override
    public void onConnectFailure(Room room, TwilioException e) {
        roomUpdateListener.onConnectFail(room);
    }

    @Override
    public void onDisconnected(Room room, TwilioException e) {
        roomUpdateListener.onDisconnected(room);
    }

    @Override
    public void onParticipantConnected(Room room, RemoteParticipant remoteParticipant) {
        roomUpdateListener.onRemoteConnected(remoteParticipant);
    }

    @Override
    public void onParticipantDisconnected(@NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
        roomUpdateListener.onRemoteDisconnected(remoteParticipant);
    }

    @Override
    public void onRecordingStarted(@NonNull Room room) {
        /*
         * Indicates when media shared to a Room is being recorded. Note that
         * recording is only available in our Group Rooms developer preview.
         */
        Log.d(TAG, "onRecordingStarted");
    }

    @Override
    public void onRecordingStopped(@NonNull Room room) {
        /*
         * Indicates when media shared to a Room is no longer being recorded. Note that
         * recording is only available in our Group Rooms developer preview.
         */
        Log.d(TAG, "onRecordingStopped");
    }

    public interface RoomUpdateListener {
        void onConnected(Room room);

        void showProgressBar(boolean visible);

        void onConnectFail(Room room);

        void onDisconnected(Room room);

        void onRemoteConnected(RemoteParticipant remoteParticipant);

        void onRemoteDisconnected(RemoteParticipant remoteParticipant);
    }

}


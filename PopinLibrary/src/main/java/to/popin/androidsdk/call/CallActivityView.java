package to.popin.androidsdk.call;

public interface CallActivityView {
    void setAccessToken(String token);

    void connectRoom(String roomName);

    void closeActivity();

    void showMessage(String message);
}

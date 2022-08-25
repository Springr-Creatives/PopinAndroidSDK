package to.popin.androidsdk.common;

import android.content.Context;
import android.content.SharedPreferences;

public class Device {
    private final Context context;
    private final SharedPreferences settings;
    private int seller;
    public Device(Context context) {
        this.context = context;
        this.settings = context.getSharedPreferences("POPIN_CALL", Context.MODE_PRIVATE);
    }

    public void setSeller(int seller) {
        this.seller = seller;
    }

    public int getSeller() {
        return seller;
    }

    public String getToken() {
        return settings.getString("token", "");
    }

    public void saveToken(String token) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public String getChannel() {
        return settings.getString("channel", "");
    }

    public void saveChannel(String channel) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("channel", channel);
        editor.apply();
    }
}

package to.popin.androidsdk.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public class Device {
    private final Context context;
    private final SharedPreferences settings;
    private int seller;

    public Device(Context context) {
        this.context = context;
        this.settings = context.getSharedPreferences("POPIN_CALL", Context.MODE_PRIVATE);
    }

    public void setSeller(int seller) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("seller_id", seller);
        editor.apply();
    }

    public int getSeller() {
        return settings.getInt("seller_id", 0);
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

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}

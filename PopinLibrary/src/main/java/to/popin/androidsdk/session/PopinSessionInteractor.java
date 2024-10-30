package to.popin.androidsdk.session;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;


import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import to.popin.androidsdk.R;
import to.popin.androidsdk.common.APIInterface;
import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.models.UserModel;

public class PopinSessionInteractor {
    private final Device myPhone;
    private final APIInterface apiInterface;

    public PopinSessionInteractor(Context context, Device myPhone) {
        this.myPhone = myPhone;
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.server_url) + "/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        this.apiInterface = retrofit.create(APIInterface.class);
    }

    public void registerForToken(int seller, String name, String mobile, RegistrationListener registrationListener) {
        Call<UserModel> call = apiInterface.registerUser(seller, 1, "Test Device", name, mobile);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                if (response.code() == 200) {
                    UserModel userModel = response.body();
                    if (userModel != null && userModel.status == 1) {
                         myPhone.saveToken(userModel.token);
                        myPhone.saveChannel(userModel.channel);
                        registrationListener.onRegistered();
                        return;
                    }
                }
                Log.e("POPIN", "TOKEN_END");
            }

            @Override
            public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {
                Log.e("ERR", t.getMessage());

            }
        });
    }

    public interface RegistrationListener {
        void onRegistered();
    }

}

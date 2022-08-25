package to.popin.androidsdk.session;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.chuckerteam.chucker.api.ChuckerInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import to.popin.androidsdk.common.APIInterface;
import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.models.UserModel;

public class PopinSessionInteractor {
    private final Device myPhone;
    private final APIInterface apiInterface;

    public PopinSessionInteractor(Context context, Device myPhone) {
        this.myPhone = myPhone;
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(new ChuckerInterceptor(context));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://test.popin.to/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        this.apiInterface = retrofit.create(APIInterface.class);
    }

    public void registerForToken(int seller) {
        Call<UserModel> call = apiInterface.registerUser(seller, 1, "Test Device");
        call.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(@NonNull Call<UserModel> call, @NonNull Response<UserModel> response) {
                if (response.code() == 200) {
                    UserModel userModel = response.body();
                    if (userModel != null && userModel.status == 1) {
                        myPhone.saveToken(userModel.token);
                        myPhone.saveChannel(userModel.channel);
                        return;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserModel> call, @NonNull Throwable t) {
                Log.e("ERR", t.getMessage());

            }
        });
    }


}
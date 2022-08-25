package to.popin.androidsdk;

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
import to.popin.androidsdk.models.StatusModel;
import to.popin.androidsdk.models.UserModel;

public class ConnectionWorker {

    private final Device myPhone;
    private final APIInterface apiInterface;

    public ConnectionWorker(Context context , Device myPhone, APIInterface apiInterface) {
        this.myPhone = myPhone;
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(new ChuckerInterceptor(context));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://test.popin.to/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        this.apiInterface = retrofit.create(APIInterface.class);
    }


    public void startConnection() {
        Call<StatusModel> call = apiInterface.startConnection(myPhone.getSeller());
        call.enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(@NonNull Call<StatusModel> call, @NonNull Response<StatusModel> response) {
                if (response.code() == 200) {
                    StatusModel statusModel = response.body();
                    if (statusModel != null && statusModel.status == 1) {
                        return;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatusModel> call, @NonNull Throwable t) {
                Log.e("ERR", t.getMessage());

            }
        });
    }

}

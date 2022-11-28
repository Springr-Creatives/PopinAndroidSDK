package to.popin.androidsdk.call;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import to.popin.androidsdk.common.APIInterface;
import to.popin.androidsdk.common.AuthInterceptor;
import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.models.StatusModel;
import to.popin.androidsdk.models.TalkModel;

public class CallInteractor {
    private final Device myPhone;
    private final APIInterface apiInterface;


    public CallInteractor(Context context, Device myPhone) {
        this.myPhone = myPhone;
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor(myPhone));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dev.popin.to/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        this.apiInterface = retrofit.create(APIInterface.class);
    }

    public void getAccessToken(AccessTokenListener accessTokenListener) {
        Call<TalkModel> call = apiInterface.createCall(myPhone.getSeller());
        call.enqueue(new Callback<TalkModel>() {
            @Override
            public void onResponse(@NonNull Call<TalkModel> call, @NonNull Response<TalkModel> response) {
                if (response.code() == 200) {
                    TalkModel talkModel = response.body();
                    if (talkModel != null) {
                        if (talkModel.status == 1) {
                            accessTokenListener.onAccessToken(talkModel);
                        } else {
                            accessTokenListener.onInvalidCall(talkModel.status);
                        }
                        return;
                    }
                }
                accessTokenListener.onNetworkError();
            }

            @Override
            public void onFailure(@NonNull Call<TalkModel> call, @NonNull Throwable t) {
                Log.e("ERR", t.getMessage());
                accessTokenListener.onNetworkError();
            }
        });
    }



    public void disconnectCall(int call_id, CallDisconnectListener callDisconnectListener) {
        Call<StatusModel> call = apiInterface.setCallEnded(call_id);
        call.enqueue(new Callback<StatusModel>() {
            @Override
            public void onResponse(@NonNull Call<StatusModel> call, @NonNull Response<StatusModel> response) {
                if (response.code() == 200) {
                    StatusModel statusModel = response.body();
                    if (statusModel != null) {
                        callDisconnectListener.onDisconnect();
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<StatusModel> call, @NonNull Throwable t) {
                Log.e("ERR", t.getMessage());

            }
        });
    }

    interface CallDisconnectListener {
        void onDisconnect();
    }

    interface AccessTokenListener {
        void onAccessToken(TalkModel talkModel);

        void onInvalidCall(int status);

        void onNetworkError();
    }
}

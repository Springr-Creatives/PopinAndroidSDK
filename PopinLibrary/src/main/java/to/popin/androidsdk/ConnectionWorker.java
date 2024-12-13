package to.popin.androidsdk;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;


import java.util.UUID;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import to.popin.androidsdk.common.APIInterface;
import to.popin.androidsdk.common.AuthInterceptor;
import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.models.CreateConnectionModel;
import to.popin.androidsdk.models.FastCallModel;
import to.popin.androidsdk.models.StatusModel;
import to.popin.androidsdk.models.UserModel;

public class ConnectionWorker {

    private final Device myPhone;
    private final Context context;
    private APIInterface apiInterface;

    public ConnectionWorker(Context context, Device myPhone) {
        this.myPhone = myPhone;
        this.context = context;
        loadApiClient(myPhone);
    }

    private void loadApiClient(Device myPhone) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor(myPhone));
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.server_url) + "/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        this.apiInterface = retrofit.create(APIInterface.class);
    }

    public void startConnection(final CreateConnectionListener createConnectionListener) {
        loadApiClient(myPhone);
        final String uuid = UUID.randomUUID().toString();
        Call<CreateConnectionModel> call = apiInterface.startConnection(myPhone.getSeller(), uuid);
        call.enqueue(new Callback<CreateConnectionModel>() {
            @Override
            public void onResponse(@NonNull Call<CreateConnectionModel> call, @NonNull Response<CreateConnectionModel> response) {
                if (response.code() == 200) {
                    CreateConnectionModel createConnectionModel = response.body();
                    if (createConnectionModel != null && createConnectionModel.status == 1) {
                        createConnectionListener.onConnectionStarted(createConnectionModel.call_queue_id);
                        return;
                    }
                    createConnectionListener.onConnectionFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreateConnectionModel> call, @NonNull Throwable t) {
                Log.e("ERR", t.getMessage());
                createConnectionListener.onConnectionFailed();
            }
        });
    }

    public void getCallDetails(int call_id, final CallDetailsListener callDetailsListener) {
        Call<FastCallModel> call = apiInterface.getCallDetails(call_id);
        call.enqueue(new Callback<FastCallModel>() {
            @Override
            public void onResponse(@NonNull Call<FastCallModel> call, @NonNull Response<FastCallModel> response) {
                if (response.code() == 200) {
                    FastCallModel fastCallModel = response.body();
                    if (fastCallModel != null && fastCallModel.status == 1) {
                        callDetailsListener.onCallDetails(fastCallModel);
                        return;
                    }
                    callDetailsListener.onCallDetailsFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<FastCallModel> call, @NonNull Throwable t) {
                Log.e("ERR", t.getMessage());

            }
        });

    }

    interface CreateConnectionListener {
        void onConnectionStarted(int call_queue_id);

        void onConnectionFailed();
    }


    interface CallDetailsListener {
        void onCallDetails(FastCallModel fastCallModel);

        void onCallDetailsFail();
    }

}

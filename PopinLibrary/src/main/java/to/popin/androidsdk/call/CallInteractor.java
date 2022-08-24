package to.popin.androidsdk.call;


import android.util.Log;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import to.popin.androidsdk.common.APIInterface;
import to.popin.androidsdk.common.Device;
import to.popin.androidsdk.models.StatusModel;
import to.popin.androidsdk.models.TalkModel;

public class CallInteractor {
    private final Device myPhone;
    private final APIInterface apiInterface;

    public CallInteractor(Device myPhone, APIInterface apiInterface) {
        this.myPhone = myPhone;
        this.apiInterface = apiInterface;
    }

    public void getAccessToken(int call_id, AccessTokenListener accessTokenListener) {
        Call<TalkModel> call = apiInterface.getCallAccessToken(call_id);
        call.enqueue(new Callback<TalkModel>() {
            @Override
            public void onResponse(@NonNull Call<TalkModel> call, @NonNull Response<TalkModel> response) {
                if (response.code() == 200) {
                    TalkModel talkModel = response.body();
                    if (talkModel != null) {
                        if (talkModel.status == 0 || talkModel.status == 1) {
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

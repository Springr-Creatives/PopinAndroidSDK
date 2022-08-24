package to.popin.androidsdk.common;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Add token to header
 */
public class AuthInterceptor implements Interceptor {

    private final Device myPhone;

    public AuthInterceptor(Device myPhone) {
        this.myPhone = myPhone;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + myPhone.getToken())
                .build();
        return chain.proceed(newRequest);
    }

}


package to.popin.androidsdk.call

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import to.popin.androidsdk.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitHelper {


    fun getInstance(context: Context): Retrofit {
        val APPLICATION_KEY = "Popin"
        val settings =context.getSharedPreferences(APPLICATION_KEY, Context.MODE_PRIVATE)
        val token: String? = settings.getString("token", "");
        val httpClient = OkHttpClient.Builder()

            //create anonymous interceptor in the lambda and override intercept
            // passing in Interceptor.Chain parameter
            .addInterceptor { chain ->
                //return response
                chain.proceed(
                    //create request
                    chain.request()
                        .newBuilder()
                        //add headers to the request builder
                        .also {
                            it.addHeader("Authorization", "Bearer $token")
                        }
                        .build()
                )
            }.also { okHttpClient ->



                if (BuildConfig.DEBUG) {

                    okHttpClient.addInterceptor(ChuckerInterceptor(context))
                }
            }
            .build()

        return Retrofit.Builder().baseUrl("https://test.popin.to" + "/api/")
            .addConverterFactory(GsonConverterFactory.create())
            // we need to add converter factory to
            .client(httpClient)
            // convert JSON object to Java object
            .build()
    }
}
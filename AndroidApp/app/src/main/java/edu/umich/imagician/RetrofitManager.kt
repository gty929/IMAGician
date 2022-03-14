package edu.umich.imagician

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.Retrofit
import okhttp3.Interceptor

import okhttp3.OkHttpClient
import java.io.IOException


/**
 * Created by Tianyao Gu on 2022/3/12.
 */
object RetrofitManager {


    lateinit var retrofit: Retrofit

    lateinit var networkAPIs: NetworkAPIs

    val retrofitExCatcher = CoroutineExceptionHandler { _, error ->
        Log.e("Retrofit exception", error.localizedMessage ?: "NETWORKING ERROR")
    }

    init {
        update()
    }

    fun update(cookie: String? = null) {
        val httpClient = OkHttpClient.Builder()
        if (cookie != null) {
            httpClient.addInterceptor { chain ->
                Log.d("Okhttp client cookie added", cookie)
                chain.proceed(
                    chain.request().newBuilder().addHeader("Cookie", cookie).build()
                )
            }
        }
        retrofit = Retrofit.Builder().baseUrl("http://ec2-3-84-195-179.compute-1.amazonaws.com/").client(httpClient.build()).build()
        networkAPIs = retrofit.create(NetworkAPIs::class.java)
        Log.d("Okhttp client updated", cookie?:"")
    }
}
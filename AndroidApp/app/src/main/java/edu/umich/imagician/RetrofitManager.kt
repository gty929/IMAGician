package edu.umich.imagician

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.Retrofit

/**
 * Created by Tianyao Gu on 2022/3/12.
 */
object RetrofitManager {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://8.8.8.8/")
        .build()

    val networkAPIs = retrofit.create(NetworkAPIs::class.java)

    val retrofitExCatcher = CoroutineExceptionHandler { _, error ->
        Log.e("Retrofit exception", error.localizedMessage ?: "NETWORKING ERROR")
    }
}
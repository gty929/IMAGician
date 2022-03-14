package edu.umich.imagician;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST;

/**
 * Created by Tianyao Gu on 2022/3/12.
 */
interface NetworkAPIs {
    @POST("accounts/login/")
    suspend fun login(@Body requestBody:RequestBody): Response<ResponseBody>
    @POST("accounts/create/")
    suspend fun signup(@Body requestBody:RequestBody): Response<ResponseBody>
    @GET("accounts/info/")
    suspend fun getUserInfo(): Response<ResponseBody>
    @POST("accounts/edit/")
    suspend fun updateUserInfo(@Body requestBody:RequestBody): Response<ResponseBody>
    @POST("postWatermark/")
    suspend fun postWatermark(@Body requestBody:RequestBody): Response<ResponseBody>
    @GET("requestWatermark/")
    suspend fun requestWatermark(@Body requestBody: RequestBody): Response<ResponseBody>
}

package edu.umich.imagician;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.*

/**
 * Created by Tianyao Gu on 2022/3/12.
 */
interface NetworkAPIs {
    // login
    @POST("accounts/login/")
    suspend fun login(@Body requestBody:RequestBody): Response<ResponseBody>
    @POST("accounts/create/")
    suspend fun signup(@Body requestBody:RequestBody): Response<ResponseBody>
    @GET("accounts/info/")
    suspend fun getUserInfo(): Response<ResponseBody>
    @POST("accounts/edit/")
    suspend fun updateUserInfo(@Body requestBody:RequestBody): Response<ResponseBody>

    // MVP
    @POST("images/post_tag/")
    suspend fun postWatermark(@Body requestBody:RequestBody): Response<ResponseBody>
    @GET("images/get_tag/{tag}")
    suspend fun getWatermark(@Path("tag") tag: String): Response<ResponseBody>
    @GET("uploads/{folder}")
    suspend fun getFolder(@Path("folder") tag: String): Response<ResponseBody>

    // authorization
    // creator side
    @GET("images/my_creation")
    suspend fun getCreations(): Response<ResponseBody>
    @GET("images/my_creation/{tag}")
    suspend fun getCreationDetail(@Path("tag") tag: String): Response<ResponseBody>
    @GET("requests/received_request/{reqid}")
    suspend fun getRcvRequestDetail(@Path("reqid") tag: String): Response<ResponseBody>
    @POST("requests/received_request")
    suspend fun handleRequest(): Response<ResponseBody>
    // viewer side
    @GET("requests/sent_request")
    suspend fun getRequests(): Response<ResponseBody>
    @GET("requests/sent_request/{reqid}")
    suspend fun getSentRequestDetail(@Path("reqid") tag: String): Response<ResponseBody>
    @POST("requests/post_request")
    suspend fun postRequest(): Response<ResponseBody>
}

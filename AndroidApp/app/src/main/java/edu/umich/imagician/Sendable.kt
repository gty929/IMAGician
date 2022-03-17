package edu.umich.imagician

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * Created by Tianyao Gu on 2022/3/14.
 * For classes that directly interact with network api
 */
interface Sendable {

    enum class Mode {
        // BODY BUILD MODE
        IDLE, EMPTY, FULL, LAZY
        // IDLE do nothing
        // EMPTY waiting to be filled
        // FULL upload content
        // LAZY ...
    }
    /* generate a MultipartBody Builder for post requests
     can be omitted for get requests */
    fun getRequestBodyBuilder(): MultipartBody.Builder = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("dummy", "data")

    /* parse the responseBody
      can be omitted for post requests */
    fun parse(responseData: String) {}

    /* call the api in RetrofitManager*/
    suspend fun send(request: RequestBody): Response<ResponseBody>?
}
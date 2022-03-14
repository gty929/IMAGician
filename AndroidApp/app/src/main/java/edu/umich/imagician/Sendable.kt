package edu.umich.imagician

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * Created by Tianyao Gu on 2022/3/14.
 */
interface Sendable {
    fun getRequestBodyBuilder(): MultipartBody.Builder
    fun parse(jsonObjectStr: String)
    suspend fun send(request: RequestBody): Response<ResponseBody>?
}
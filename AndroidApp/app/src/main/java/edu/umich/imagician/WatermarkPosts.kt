package edu.umich.imagician

import android.util.Log
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response


/**
 * Created by Tianyao Gu on 2022/3/16.
 * Each object in my creation
 */
class WatermarkPosts: Sendable {
    val posts = arrayListOf<WatermarkPost?>()

    override suspend fun send(request: RequestBody): Response<ResponseBody>? {
        return RetrofitManager.networkAPIs.getPosts()
    }

    override fun parse(responseData: String) {
        Log.d("CreationsParser", "Parsing posts")
        clear()
        try {
            val objs = JSONObject(responseData).getJSONArray("result")
            for (i in 0 until objs.length()) {
                val watermarkPost = WatermarkPost()
                watermarkPost.mode = Sendable.Mode.EMPTY
                watermarkPost.parse(objs.getJSONObject(i).getJSONObject("image").toString())
                posts.add(watermarkPost)
            }

        } catch (e: Exception) {
            Log.e("UserInfo", "cannot parse JSON string $responseData", e)
        }
    }

    fun clear() {
        posts.clear()
    }


}
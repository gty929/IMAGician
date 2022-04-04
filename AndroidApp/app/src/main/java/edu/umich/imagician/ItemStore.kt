package edu.umich.imagician

import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Exception

object ItemStore {

    val watermarkPosts= WatermarkPosts()
    val watermarkRequests= WatermarkRequests()

    // TODO 3/26
    fun getPostDetail(index: Int, successCallback: (() -> Unit), failureCallback: (() -> Unit)? = null) {
        // get the requests to the post
        val watermarkPost = watermarkPosts.posts[index]!!
        Log.d("show history", "post tag ${watermarkPost.tag}")
        watermarkPost.mode = Sendable.Mode.LAZY
        httpCall(watermarkPost) { returncode ->
            if (returncode != 200) {
                if (failureCallback != null) {
                    failureCallback()
                }
                Log.e("watermark post", "get watermarkRequests failed")
            } else {
                successCallback()
            }
        }
    }

    // TODO 3/26
    fun getRequestDetail(index: Int) {
        val watermarkRequest = watermarkRequests.requests[index]
    }

    /** http wrapper for both post and get during login state, cookie must be provided
     * should add a callback to handle the return code
     * the fields within data can be modified
     * */
    fun httpCall(data: Sendable, callback: (returncode : Int) -> Unit) {
        MainScope().launch {
            withContext(RetrofitManager.retrofitExCatcher) {
                // Use Retrofit's suspending POST request and wait for the response
                var returnCode = 0
                var response: Response<ResponseBody>? = null
                try {
                    val request = data.getRequestBodyBuilder().build()
                    response = data.send(request)
//                    Log.d("Receives response", response?.body()?.string() ?: "") // why cannot add the line?
                } catch (e: Exception) {
                    Log.e("send", "send failed", e)
                }
                if (response != null) {
                    returnCode = response.code()
                    try {
                        if (response.isSuccessful) {
                            data.parse(response.body()?.string()?:"")
                        } else {
                            Log.e("http failure response", response.errorBody()?.string()?:"")
                        }

                    } catch (e: Exception) {
                        Log.e("response parse exception", e.toString())
                    }

                }
                callback(returnCode)

            }
        }
    }

    fun clear() {
        watermarkPosts.clear()
        watermarkRequests.clear()
    }

    fun refreshWatermarkPosts(successCallback: (() -> Unit), failureCallback: (() -> Unit)? = null) {
        httpCall(watermarkPosts) { returncode ->
            if (returncode != 200) {
                if (failureCallback != null) {
                    failureCallback()
                }
                Log.e("watermark posts", "get watermarkPosts failed")
            } else {
                successCallback()
            }
        }
    }

    fun refreshWatermarkRequests(successCallback: (() -> Unit), failureCallback: (() -> Unit)? = null) {
        httpCall(watermarkRequests) { returncode ->
            if (returncode != 200) {
                if (failureCallback != null) {
                    failureCallback()
                }
                Log.e("watermark requests", "get watermarkRequests failed")
            } else {
                successCallback()
            }
        }
    }
}

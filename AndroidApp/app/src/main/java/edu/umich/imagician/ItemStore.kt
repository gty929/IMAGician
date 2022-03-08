package edu.umich.imagician

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley.newRequestQueue
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.reflect.full.declaredMemberProperties

object RequestStore {
    val requests = arrayListOf<ImgRequest?>()

    private val nFields = ImgRequest::class.declaredMemberProperties.size

    private lateinit var queue: RequestQueue
    private const val serverUrl = "https://35.192.222.203/"

    fun fakeRequests() {
        requests.clear()
        for (i in 0 until 3) {
            requests.add(ImgRequest(
                imgPost = ImgPost(filename = "fake.bmp"),
                timestamp = "yyyy/dd/mm, time",
                status = "Granted"
            ))
        }
        for (i in 0 until 3) {
            requests.add(ImgRequest(
                imgPost = ImgPost(filename = "fake.bmp"),
                timestamp = "yyyy/dd/mm, time",
                status = "Rejected"
            ))
        }
        for (i in 0 until 3) {
            requests.add(ImgRequest(
                imgPost = ImgPost(filename = "fake.bmp"),
                timestamp = "yyyy/dd/mm, time",
                status = "Pending"
            ))
        }
    }

    fun getRequests(context: Context, completion: () -> Unit) {
        val getRequest = JsonObjectRequest(serverUrl+"getrequests/",
            { response ->
                requests.clear()
                val requestsReceived = try { response.getJSONArray("requests") } catch (e: JSONException) { JSONArray() }
                for (i in 0 until requestsReceived.length()) {
                    val requestEntry = requestsReceived[i] as JSONArray
                    if (requestEntry.length() == nFields) {
                        requests.add(ImgRequest(
                            message = requestEntry[1].toString(),
                            timestamp = requestEntry[2].toString()))
                    } else {
                        Log.e("getRequests", "Received unexpected number of fields: " + requestEntry.length().toString() + " instead of " + nFields.toString())
                    }
                }
                completion()
            }, { completion() }
        )

        if (!this::queue.isInitialized) {
            queue = newRequestQueue(context)
        }
        queue.add(getRequest)
    }
}
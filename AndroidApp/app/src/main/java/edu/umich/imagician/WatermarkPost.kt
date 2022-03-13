package edu.umich.imagician

import java.sql.Timestamp

// Class of post

class WatermarkPost (var id: Int? = null,
                     var username: String? = null,
                     var filename: String? = null,
                     var phoneNumber: String? = null,
                     var
                     var uri: String? = null,
                     var message: String? = null,
                     var timestamp: String? = null,
                     var numPending: Int? = null,
                     var detailed: Boolean = false) {
    private lateinit var pendingRequestList: ArrayList<WatermarkRequest>

    fun getPendingNum() : Int? {
        numPending = 0
        if (this::pendingRequestList.isInitialized) {
            numPending = pendingRequestList.size
        }
        return numPending
    }

}
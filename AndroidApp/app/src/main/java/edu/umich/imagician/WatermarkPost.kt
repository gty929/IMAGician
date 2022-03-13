package edu.umich.imagician

import java.sql.Timestamp

// Class of post

data class WatermarkPost (var id: Int? = null,
                     var username: String? = null,
                     var realName: String? = null,
                     var filename: String? = null,
                     var email: String? = null,
                     var phoneNumber: String? = null,
                     var uri: String? = null,
                     var message: String? = null,
                     var timestampFlag: Boolean = false,
                     var usernameFlag: Boolean = false, /** set as false for anonymous posting*/
                     var realNameFlag: Boolean = false,
                     var emailFlag: Boolean = false,
                     var phoneFlag: Boolean = false,
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
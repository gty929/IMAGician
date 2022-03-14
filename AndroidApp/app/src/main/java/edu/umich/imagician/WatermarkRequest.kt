package edu.umich.imagician

// Class of request

data class WatermarkRequest (var id: Int? = null,
                        var watermarkPost: WatermarkPost? = null,
                        var sender: String? = null,
                        var message: String? = null,
                        var timestamp: String? = null,
                        var status: String = "PENDING",
                        var detailed: Boolean = false)
package edu.umich.imagician

// Class of request

class WatermarkRequest (var watermarkPost: WatermarkPost? = null,
                        var sender: String? = null,
                        var message: String? = null,
                        var timestamp: String? = null,
                        var status: String? = null)
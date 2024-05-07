package com.example.serayularanganapp.model

import java.io.Serializable

class TourData : Serializable {

    var name: String? = null
    var desc: String? = null
    var info: String? = null
    var img: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var totalVisits: Int = 0

    constructor(name: String?, desc: String?, info: String?, img: String?, latitude: Double?, longitude: Double?) {

        this.name = name
        this.desc = desc
        this.info = info
        this.img = img
        this.latitude = latitude
        this.longitude = longitude

    }
    constructor() {

    }
}
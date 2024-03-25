package com.example.serayularanganapp.model

import java.io.Serializable

class TourData : Serializable {

    var id: String? = null 
    var name: String? = null
    var desc: String? = null
    var info: String? = null
    var img: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var visitorData: MutableMap<String, VisitorData> = mutableMapOf()

    constructor(id: String?, name: String?, desc: String?, info: String?, img: String?, latitude: Double?, longitude: Double?, visitorData: MutableMap<String, VisitorData>) {

        this.id = id
        this.name = name
        this.desc = desc
        this.info = info
        this.img = img
        this.latitude = latitude
        this.longitude = longitude
        this.visitorData = visitorData

    }
    constructor() {

    }
}
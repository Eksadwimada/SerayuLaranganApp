//Tour Data
package com.example.serayularanganapp.model

import java.io.Serializable

class TourData : Serializable {

    var name: String? = null
    var desc: String? = null
    var info: String? = null
    var img: String? = null

    // Add any other fields you need

    constructor(name: String?, desc: String?, info: String?, img: String?) {
        this.name = name
        this.desc = desc
        this.info = info
        this.img = img
    }

    constructor() {

    }
}
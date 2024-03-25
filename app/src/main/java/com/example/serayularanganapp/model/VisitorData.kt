package com.example.serayularanganapp.model

import java.io.Serializable

data class VisitorData(
    var date: String? = null,
    var visitorCount: Int = 0,
    var totalVisitor: Int = 0
) : Serializable
package com.example.hybridcryptographywithfirebase.models

//data class ViewPagerItem(val broadcastMessage: SingleItem = SingleItem("one", Status.NOT_ACKNOWLEDGED.ordinal))

data class SingleItem(val htmlPage: String = "0", var status: Int = Status.NOT_ACKNOWLEDGED.ordinal)

enum class Status {
    NOT_ACKNOWLEDGED,
    ACKNOWLEDGED
}
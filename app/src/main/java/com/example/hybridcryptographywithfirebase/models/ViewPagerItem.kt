package com.example.hybridcryptographywithfirebase.models

//data class ViewPagerItem(val broadcastMessage: SingleItem = SingleItem("one", Status.NOT_ACKNOWLEDGED.ordinal))

const val HTML_ONE = "<!DOCTYPE html> <html> <head> <title>Page Title</title> </head> <body>  <h1>This is a Heading</h1> <p>This is a paragraph.</p>  </body> </html>"

data class SingleItem(val htmlPage: String = HTML_ONE, var status: Int = Status.NOT_ACKNOWLEDGED.ordinal)

enum class Status {
    NOT_ACKNOWLEDGED,
    ACKNOWLEDGED
}
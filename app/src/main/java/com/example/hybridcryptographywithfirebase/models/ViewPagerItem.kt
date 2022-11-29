package com.example.hybridcryptographywithfirebase.models

val itemOne: String = "<!DOCTYPE html>\n" +
        "<html>\n" +
        "<head>\n" +
        "<title>Page Title</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "\n" +
        "<h1>This is a Heading</h1>\n" +
        "<p>This is a paragraph.</p>\n" +
        "\n" +
        "</body>\n" +
        "</html>"

data class ViewPagerItem(val broadcastMessages: List<String> = listOf(itemOne))
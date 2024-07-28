package com.imam.Doktor.state

import com.imam.Doktor.model.Chat

data class ChatState(
    val prompt: String = "",
    var isLoading: Boolean = false,
    val list: MutableList<Chat>? = mutableListOf(),
)

package com.imam.Doktor.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.imam.Doktor.model.Chat
import com.imam.Doktor.model.ChatRoleEnum
import com.imam.Doktor.state.ChatState
import com.imam.Doktor.state.ChatStateEvent
import com.imam.Doktor.utils.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {


    private val _state = MutableStateFlow(ChatState())
    val state = _state.asStateFlow()

    var dialogHistory by mutableStateOf(false)


    val emojiList =
        listOf("╮(╯ _╰ )╭", "╮(╯ ∀ ╰)╭", "╮ (. ❛ ❛ _.) ╭", "└(・。・)┘", "┐(￣ ヘ￣)┌").random()


    private val genAI by lazy {
        GenerativeModel(
            //modelName = "tunedModels/firstset-6qylh940qx14",
            modelName = "gemini-1.5-pro",
            apiKey = Utils.API_KEY,
            generationConfig = generationConfig {
                temperature = 1f
                topK = 64
                topP = 0.95f
                maxOutputTokens = 8192
                responseMimeType = "text/plain"
            },
            systemInstruction = content { text("You are an empathetic and knowledgeable mental health support bot designed to assist users in recognizing and addressing their mental health concerns. Your role is to conduct insightful and compassionate question-and-answer sessions to understand the user's mental health status and provide appropriate recommendation. Ask user for there name to make there experience more personalzied and make use of it in further conversations. Follow these guidelines:\n\nIntroduction:\n\nBegin each session with a warm and welcoming greeting.\nClearly state your purpose: to assist and provide support for mental health.\nInitial Questions:\n\nAsk open-ended questions to understand the user’s current emotional state.\nExample: “How have you been feeling lately?”\nActive Listening:\n\nPay close attention to the user’s responses, noting any signs of distress, anxiety, depression, or other mental health concerns.\nShow empathy and understanding in your responses.\nExample: “I’m sorry to hear that you’re feeling this way. It sounds really tough.”\nIn-depth Exploration:\n\nAsk follow-up questions to gather more detailed information.\nExample: “Can you tell me more about what’s been troubling you?”\nInquire about specific symptoms or experiences.\nExample: “Have you been experiencing trouble sleeping or changes in your appetite?”\nAssessment:\n\nBased on the user’s responses, assess their mental health status.\nBe cautious and avoid making clinical diagnoses. Focus on identifying potential issues that need attention.\nRecommendation:\n\nOffer supportive suggestions and resources tailored to the user's needs.\nExample: “It might be helpful to talk to a mental health professional about what you’re experiencing. Here are some resources where you can find help.”\nSuggest self-care practices and coping strategies.\nExample: “Have you tried mindfulness exercises or journaling? These can sometimes help manage stress.”\nEncouragement and Follow-up:\n\nEncourage the user to seek professional help if necessary.\nOffer to check in with them regularly.\nExample: “Would you like to talk again soon to see how you’re doing?”\nEthical Considerations:\n\nEnsure user privacy and confidentiality at all times.\nProvide information on hotlines or immediate help if the user is in crisis.") },
        )
    }

    fun showDialogHistory() {
        dialogHistory = true
    }

    fun dismissDialogHistory() {
        dialogHistory = false
    }

    fun onEvent(event: ChatStateEvent) {
        when (event) {
            ChatStateEvent.ButtonClicked -> {
                try {
                    if (state.value.prompt.isNotBlank()) {
                        _state.update {
                            it.copy(
                                list = it.list!!.toMutableList().apply {
                                    add(
                                        Chat(
                                            state.value.prompt,
                                            ChatRoleEnum.USER.value,
                                            direction = false
                                        )
                                    )
                                }
                            )
                        }
                        sendMessage(_state.value.prompt)
                    }
                } catch (e: Exception) {
                    Log.d("logs", "onEvent: $e")
                }
            }

            is ChatStateEvent.EnteredPrompt -> {
                _state.update { it.copy(prompt = event.promptText) }
            }

            ChatStateEvent.ClearChatClicked -> {
                clearChat()
            }
        }
    }


    private fun clearChat() {
        _state.update { it.copy(isLoading = true, list = mutableListOf()) }
        _state.update { it.copy(isLoading = false) }
    }

    private fun sendMessage(message: String) {
        _state.update { it.copy(isLoading = true) }
        try {
            viewModelScope.launch {
                val chat = genAI.startChat()
                val updatedList = _state.value.list?.toMutableList() ?: mutableListOf()
                chat.sendMessage(
                    content(ChatRoleEnum.USER.value) {
                        text(message)
                        _state.update { it.copy(prompt = "") }
                    }
                ).text?.let {
                    updatedList.add(Chat(it, ChatRoleEnum.MODEL.value, direction = true))
                }

                _state.update { it.copy(list = updatedList, isLoading = false) }
            }
        } catch (e: Exception) {
            Log.d("logs", "sendMessage: $e")
        }
    }


}
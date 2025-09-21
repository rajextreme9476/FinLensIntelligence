package com.android.hdfcintelligence

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileInputStream

class BakingViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =

        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.apiKey
    )

    fun sendPrompt(
        bitmap: Bitmap,
        prompt: String
    ) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)

                    }
                )
                response.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    fun sendPromptText(
        prompt: String
    ) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        Log.d("GeminiAI", "Response: $prompt")
                        val promptAI ="Generate a concise response (5 lines or fewer)  "+prompt+" My location: Latitude 19.0882, Longitude 73.0707."
                        text(promptAI)
                    }
                )
                response.text?.let { outputContent ->
                    Log.d("GeminiAI", "Response: $outputContent")

                    _uiState.value = UiState.Success(outputContent)

                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }


    fun sendPromptTextHDFC(
        prompt: String
    ) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        Log.d("GeminiAI", "Response: $prompt")
                        text("Promt:"+prompt)
                    }
                )
                response.text?.let { outputContent ->
                    Log.d("GeminiAI", "Response: $outputContent")

                    _uiState.value = UiState.Success(outputContent)

                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    fun sendPromptFile1(
        uri: String,
        mimeType: String,
        prompt: String
    ) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(
                    content {
                        fileData(uri, mimeType)  // Passing the content URI string
                        text(prompt)
                    }
                )

                response.text?.let { outputContent ->
                    Log.d("HDFC_AI_XXXX", "Response: $outputContent")
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                Log.e("HDFC AI _XXXX", "Error: ${e.localizedMessage}", e)
                _uiState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }


    fun sendPromptFile(
        filePath: String,
        mimeType: String,
        prompt: String
    ) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Convert file to Base64 string
                val file = File(filePath)
                val base64Content = fileToBase64(file)

                // Send base64 content instead of file path
                val response = generativeModel.generateContent(
                    content {
                        fileData(base64Content, mimeType)  // Base64 string instead of file path
                        Log.d("GeminiAI", "Response: $prompt")
                        text(prompt)
                    }
                )

                response.text?.let { outputContent ->
                    Log.d("GeminiAIXXXX", "Response: $outputContent")
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                Log.e("GeminiAI", "Error: ${e.localizedMessage}", e)
                _uiState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    private fun fileToBase64(file: File): String {
        val inputStream = FileInputStream(file)
        val bytes = inputStream.readBytes()
        inputStream.close()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }


    fun sendPromptFile2(file: File, mimeType: String, prompt: String) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {

                val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())

                val part = MultipartBody.Part.createFormData("file", file.name, requestBody) // "file" is the parameter name Gemini expects

                val promptBody = prompt.toRequestBody("text/plain".toMediaTypeOrNull()) // Prompt as text part
                val promptPart = MultipartBody.Part.createFormData("prompt", prompt, promptBody)

                val requestBody1 = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(part)
                    .addPart(promptPart)
                    .build()

                val response = generativeModel.generateContent(
                    content {
                        fileData("part", mimeType)  // Base64 string instead of file path
                        text(prompt)
                    }
                )



            } catch (e: Exception) {
                Log.e("GeminiAI", "Error: ${e.localizedMessage}", e)
                _uiState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    private fun tfileToBase64(file: File): String {
        val inputStream = FileInputStream(file)
        val bytes = inputStream.readBytes()
        inputStream.close()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    // Chunk size (adjust as needed)
    private val CHUNK_SIZE = 1024 * 1024 // 1MB chunks (example)

}
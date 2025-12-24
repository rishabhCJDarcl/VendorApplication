package com.example.vendorapplication

import android.app.VoiceInteractor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel


import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * FILE: OpenAiSingleFile.kt
 * DIRECT OPENAI API CALL
 * (For testing / internal use)
 */

class OpenAiActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                OpenAiScreen()
            }
        }
    }
}

class OpenAiViewModel : ViewModel() {

    var result by mutableStateOf("")
        private set

    private val client = OkHttpClient()

    // 🔴 PUT YOUR OPENAI API KEY HERE
    private val OPENAI_API_KEY = "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"

    fun generate(prompt: String) {
        viewModelScope.launch {
            try {
                val message = JSONObject()
                message.put("role", "user")
                message.put("content", prompt)

                val messages = JSONArray()
                messages.put(message)

                val json = JSONObject()
                json.put("model", "gpt-4o-mini")
                json.put("messages", messages)
                json.put("temperature", 0.2)
                json.put("max_tokens", 500)

                val body = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $OPENAI_API_KEY")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()


                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val obj = JSONObject(responseBody)
                    val text =
                        obj.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")

                    result = text
                } else {
                    result = "No response"
                }

            } catch (e: Exception) {
                result = "Error: ${e.message}"
            }
        }
    }
}

@Composable
fun OpenAiScreen() {
    val vm = remember { OpenAiViewModel() }
    var prompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("OpenAI Codex", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            label = { Text("Enter prompt") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { vm.generate(prompt) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = vm.result,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            readOnly = true,
            label = { Text("Output") }
        )
    }
}

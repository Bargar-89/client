package com.bargarapp.testks

import android.util.Log
import io.ktor.client.*
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText

object WebSocketUtils {

    suspend fun sendResultsToServer(client: HttpClient,
                                    ip: String,
                                    port:String,
                                    result: String) {
        try {
            client.webSocket(method = HttpMethod.Get,
                host = ip,
                port = port.toInt(),
                path = "/gestures"
            ) {
                Log.d("WebSocket", "SENNDED: $result")
                // Отправляем сообщение серверу
                send(Frame.Text(result))

                while (true) {
                    when (val frame = incoming.receive()) {
                        is Frame.Text -> {
                            val receivedMessage = frame.readText()
                            // Обработка полученного сообщения
                            Log.d("WebSocket", "Received message: $receivedMessage")
                            if(receivedMessage.length <4){
                                MainActivity.instance.myId = receivedMessage
                            }
                            val parsedGesture = parseGestureString(receivedMessage)
                            if(parsedGesture?.first == "up"){
                                AccessibilityServiceHelper.getInstance().performSwipeUp(parsedGesture.second.toLong())
                            }
                            if(parsedGesture?.first == "down") {
                                AccessibilityServiceHelper.getInstance().performSwipeDown(parsedGesture.second.toLong())
                            }
                        }
                        // Обработка других типов фреймов
                        is Frame.Binary -> Log.d("WebSocket", "Binary")
                        is Frame.Close -> {
                            Log.d("WebSocket", "Closed")
                            break // Выходим из цикла при закрытии соединения
                        }
                        is Frame.Ping -> Log.d("WebSocket", "ping")
                        is Frame.Pong -> Log.d("WebSocket", "pong")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "Error SendResult: ${e.message}")
        } finally {
            // Обработчик события onDisconnect
            Log.d("WebSocket", "Disconnected")
        }
    }
    private fun parseGestureString(input: String): Pair<String, Int>? {
        val regex = Regex("Swipe (\\w+) for (\\d+) ms")
        val matchResult = regex.find(input)

        return matchResult?.let { result ->
            val direction = result.groupValues[1]
            val duration = result.groupValues[2].toIntOrNull()
            if (duration != null) {
                Pair(direction, duration)
            } else {
                null
            }
        }
    }
}
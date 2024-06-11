package com.bargarapp.testks

import android.content.Context
import android.content.Intent
import android.util.Log
import io.ktor.client.*
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object WebSocketUtils {

    suspend fun sendResultsToServer(client: HttpClient,
                                    ip: String,
                                    port:String,
                                    result: String,
                                    accessibilityServiceHelper: AccessibilityServiceHelper, mContext:Context) {
        try {
            client.webSocket(method = HttpMethod.Get,
                host = ip,
                port = port.toInt(),
                path = "/gestures"
            ) {
                Log.d("WebSocket", "SENNDED: $result")

                // Отправляем сообщение серверу
                send(Frame.Text(result))

                // Дополнительная обработка, если необходимо
                while (true) {
                    val frame = incoming.receive()
                    when (frame) {
                        is Frame.Text -> {
                            val receivedMessage = frame.readText()
                            // Обработка полученного сообщения
                            Log.d("WebSocket", "Received message: $receivedMessage")
                            val parsedGesture = parseGestureString(receivedMessage)
                            accessibilityServiceHelper.setSendResultCallback {callback ->
                                val result1 = "Result = $callback"
                                GlobalScope.launch {
                                    sendResultsToServer(client,ip,port,result1,accessibilityServiceHelper, mContext)
                                }

                            }
                            val intent = Intent("com.bargarapp.testks.ACTIVATE_METHOD")


                            if(parsedGesture?.first == "up"){
                                println("up")
                                intent.putExtra("Duration", parsedGesture.second.toLong())
                                intent.putExtra("Direction", parsedGesture.first)
                                mContext.sendBroadcast(intent)


                            }
                            if(parsedGesture?.first == "down") {
                                println("down")
                                intent.putExtra("Duration", parsedGesture.second.toLong())
                                intent.putExtra("Direction", parsedGesture.first)
                                mContext.sendBroadcast(intent)
                            }
                        }
                        // Обработка других типов фреймов, если необходимо
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
            Log.e("WebSocket", "Error: ${e.message}")
        } finally {
            // Обработчик события onDisconnect
            Log.d("WebSocket", "Disconnected")
        }
    }

    private fun handleServerMessage(message: String, accessibilityServiceHelper: AccessibilityServiceHelper) {
        when {
            message.startsWith("Swipe up") -> {
                val duration = message.substringAfter("swipeUp").toLongOrNull() ?: 500L
                accessibilityServiceHelper.performSwipeUp(duration)
            }
            message.startsWith("Swipe down") -> {
                val duration = message.substringAfter("swipeDown").toLongOrNull() ?: 500L
                accessibilityServiceHelper.performSwipeDown(duration)
            }
            // Обработка других команд, если необходимо
        }
    }
    fun parseGestureString(input: String): Pair<String, Int>? {
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
package com.bargarapp.testks

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets

import kotlinx.coroutines.Job


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClientUI()
        }
        checkAccessibilityServiceEnabled(this)
    }
}

@Composable
fun ClientUI() {
    val mContext = LocalContext.current
    val isChromeOpened = remember { mutableStateOf(false) }
    val client = remember { HttpClient(CIO) { install(WebSockets) } }
    val scope = rememberCoroutineScope()
    var serverIp by remember { mutableStateOf(SharedPreferencesHelper.readFromSP("Server_IP",mContext)) }
    var serverPort by remember { mutableStateOf(SharedPreferencesHelper.readFromSP("Server_Port",mContext)) }
    var isConnected by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var showConfig by remember { mutableStateOf(false) }
    val accessibilityServiceHelper =
        AccessibilityServiceHelper().apply {
            setSendResultCallback { callback ->
                scope.launch {
                    WebSocketUtils.sendResultsToServer(client,serverIp,serverPort,callback, this@apply,mContext)


                }
            }
        }


    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { showConfig = !showConfig }) {
            Text("Config")
        }
        if(showConfig){
            TextField(
                value = serverIp,
                onValueChange = { serverIp = it },
                label = { Text("Server IP") }
            )
            TextField(
                value = serverPort,
                onValueChange = { serverPort = it },
                label = { Text("Server Port") }
            )
            Button(onClick = {
                showConfig = !showConfig
                // Сохранить IP и порт
                SharedPreferencesHelper.writeToSP(serverIp,"Server_IP",mContext)
                SharedPreferencesHelper.writeToSP(serverPort,"Server_Port", mContext)
            }) {
                Text("Save")
            }
        }
        Row {
            Button(onClick = {
                var job: Job? = null
                if(!isConnected){
                    job = scope.launch {
                        WebSocketUtils.sendResultsToServer(
                            client,
                            serverIp,
                            serverPort,
                            "conect",
                            accessibilityServiceHelper,
                            mContext
                        )

                    }
                }else{
                    job?.cancel()
                }
                isConnected = !isConnected
            }) {
                Text(if (isConnected) "Disconnect" else "Connect")
            }
        }
        Button(onClick = {
            if(!isRunning && isConnected){
                isChromeOpened.value = true
                scope.launch {
                    openChrome(mContext,"https://www.bing.com/search?FORM=U523DF&PC=U536&q=%D0%B6%D0%B8%D0%B7%D0%BD%D1%8C+%D0%BF%D1%80%D0%B5%D1%80%D0%B0%D1%81%D0%BD%D0%B0")
                    WebSocketUtils.sendResultsToServer(client,serverIp,serverPort,"start",accessibilityServiceHelper,mContext)
                }


            }else{
                scope.launch {
                    WebSocketUtils.sendResultsToServer(client, serverIp,serverPort,"pause",accessibilityServiceHelper,mContext)
                }
            }
            isRunning = !isRunning
        }) {
            Text(if (isRunning) "Pause" else "Start")
        }
    }


}
fun openChrome(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        setPackage("com.android.chrome")
    }
    context.startActivity(intent)
}

private fun checkAccessibilityServiceEnabled(mContext: Context): Boolean {
    val accessibilitySettingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    mContext.startActivity(accessibilitySettingsIntent)
    return true
}
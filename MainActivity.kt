package com.bargarapp.testks

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class MainActivity : ComponentActivity() {
    companion object {
        lateinit var instance: MainActivity
    }
    private lateinit var client: HttpClient
    var myId: String = "null"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this // Сохраняем ссылку на текущий экземпляр
        client = HttpClient(CIO) {
            install(WebSockets)
        }
        setContent {
            ClientUI(client)
        }

        checkAccessibilityServiceEnabled(this)

    }
    fun sendResultToServerAfterGestures(result:String){
        CoroutineScope(Dispatchers.Main).launch {
            WebSocketUtils.sendResultsToServer(client,
                SharedPreferencesHelper.readFromSP("Server_IP", instance),
                SharedPreferencesHelper.readFromSP("Server_Port", instance),
                result
            )
        }

    }
}

@Composable
fun ClientUI(client: HttpClient) {
    val mContext = LocalContext.current
    val isChromeOpened = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var serverIp by remember { mutableStateOf(SharedPreferencesHelper.readFromSP("Server_IP",mContext)) }
    var serverPort by remember { mutableStateOf(SharedPreferencesHelper.readFromSP("Server_Port",mContext)) }
    var isConnected by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var showConfig by remember { mutableStateOf(false) }




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
                SharedPreferencesHelper.writeToSP(serverIp,"Server_IP",mContext)
                SharedPreferencesHelper.writeToSP(serverPort,"Server_Port", mContext)
            }) {
                Text("Save")
            }
        }
        Row {
            Button(onClick = {
                if(!isConnected){
                    scope.launch {
                        WebSocketUtils.sendResultsToServer(client,serverIp,serverPort,"firstcConnect,"+MainActivity.instance.myId)
                    }
                }else if(!isRunning){
                    scope.launch {
                        WebSocketUtils.sendResultsToServer(client, serverIp,serverPort,"pause,"+MainActivity.instance.myId)
                        WebSocketUtils.sendResultsToServer(client,serverIp,serverPort,"disconnect,"+MainActivity.instance.myId)
                        MainActivity.instance.myId = "null"
                        isRunning = false
                    }

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
                    WebSocketUtils.sendResultsToServer(client,serverIp,serverPort,"start,"+MainActivity.instance.myId)
                }


            }else{
                scope.launch {
                    WebSocketUtils.sendResultsToServer(client, serverIp,serverPort,"pause,"+MainActivity.instance.myId)
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
    try {
        context.startActivity(intent)
    }catch (e:Exception){
        e.printStackTrace()
        Log.d("open chrome", "error")
    }

}

private fun checkAccessibilityServiceEnabled(mContext: Context): Boolean {
    val accessibilitySettingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    mContext.startActivity(accessibilitySettingsIntent)
    return true
}
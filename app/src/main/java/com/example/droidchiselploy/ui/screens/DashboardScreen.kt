package com.example.droidchiselploy.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.droidchiselploy.data.PreferencesManager
import com.example.droidchiselploy.service.ServerService
import com.example.droidchiselploy.ui.components.CustomButton
import com.example.droidchiselploy.ui.components.CustomField
import com.example.droidchiselploy.ui.components.CustomText
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsManager = remember { PreferencesManager(context) }

    var commandText by remember { mutableStateOf(prefsManager.getChiselCommand()) }
    var statusMessage by remember { mutableStateOf("Not running") }
    var isDeploying by remember { mutableStateOf(false) }

    // Set up broadcast receiver for server status updates
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val message = intent?.getStringExtra(ServerService.EXTRA_STATUS_MESSAGE)
                if (message != null) {
                    statusMessage = message
                    if(! statusMessage.contains("started")) {
                        isDeploying = false;
                    }

                }
            }
        }

        val filter = IntentFilter(ServerService.ACTION_SERVER_STATUS)
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)

        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color.Black)
            .padding(horizontal = 12.dp)
            .padding(top = 16.dp)
        ,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {


            Spacer(modifier = Modifier.height(10.dp))

            // Runtime Config Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp)
                ) {
                    CustomText("Chisel parameters")
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomField(
                        label = "Example:\nclient --auth USER:PASS SERVER_IP:PORT socks",
                        value = commandText,
                        onValueChange = { commandText = it },
                        placeholder = "client ..."
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Card - Show server URL when available
            if (true) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            "🚀 Service status:",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        )

                        if (statusMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                statusMessage,
                                fontSize = 12.sp,
                                color = if (statusMessage.contains("failed") || statusMessage.contains("exited")) Color.Yellow else Color.Green
                            )
                        }

                    }
                }
            }
        }
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            CustomButton(
                text = if (isDeploying) "STOP SERVICE" else "START SERVICE",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                enabled = true,
                colors = if (isDeploying) {
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Yellow,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.LightGray,
                        disabledContentColor = Color.DarkGray
                    )
                },
                onClick = {
                    if (isDeploying) {
                        val intent = Intent(context, ServerService::class.java)
                        context.stopService(intent)
                        isDeploying = false
                        statusMessage = "Server stopped"
                    } else {
                        if (true) {
                            prefsManager.saveChiselCommand(commandText)

                            val intent = Intent(context, ServerService::class.java).apply {
                                putExtra("COMMAND", commandText)
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                            isDeploying = true
                            statusMessage = "Starting service..."
                        }
                    }
                }
            )

        }

    }

}

package com.cesoft.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.cesoft.bluetooth.ui.theme.BluetoothTheme
import java.util.Timer
import java.util.TimerTask

//private val LocalKeyEventHandlers= compositionLocalOf<MutableList<KeyEventHandler>> {
//    error("LocalKeyEventHandlers is not provided")
//}
//typealias KeyEventHandler = (Int, KeyEvent) -> Boolean
class MainActivity : ComponentActivity() {

    /// ENABLE BT----------------------------------------------
    private val enableBt =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res: ActivityResult ->
            android.util.Log.e(
                "AAA",
                "StartActivityForResult-----------${res.resultCode} / ${res.data} "
            )
            if (res.resultCode == RESULT_OK) {
                connectBluetooth()
            } else {
                setContent {
                    BluetoothTheme {
                        ActivateBluetooth()
                    }
                }
            }
        }

    @Composable
    fun ActivateBluetooth() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Active el Bluetooth para que la app pueda escanear codigos")
                Button(onClick = { enableBluetooth() }) {
                    Text("Activar Bluetooth")
                }
            }
        }
    }

    @Preview
    @Composable
    fun ActivateBluetooth_preview() {
        BluetoothTheme {
            ActivateBluetooth()
        }
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBt.launch(enableBtIntent)
    }

    /// PERMISSION----------------------------------------------
    private val grantPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted: Boolean ->
            android.util.Log.e("AAA", "RequestPermission-----------$granted")
            if (!granted) {
                android.util.Log.e("AAA", "RequestPermission----------- A")
                setContent {
                    BluetoothTheme {
                        GrantPermission()
                    }
                }
            } else {
                connectBluetooth()
            }
        }

    @Composable
    fun GrantPermission() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Los permisos son necesarios para el funcionamiento de la app")
                Button(onClick = { connectBluetooth() }) {
                    Text("Activar Permisos")
                }
            }
        }
    }

    @Preview
    @Composable
    fun GrantPermission_preview() {
        BluetoothTheme {
            GrantPermission()
        }
    }

    private fun Context.hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /// CONNECT BT ---------------------------------------------------------------------------------
    private fun connectBluetooth() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
        if (VERSION.SDK_INT >= VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                grantPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
                return
            }
        }
        if (!bluetoothAdapter.isEnabled) {
            enableBluetooth()
            return
        }
        setContent {
//            CompositionLocalProvider(LocalKeyEventHandlers provides keyEventHandlers) {
//                BluetoothTheme {
//                    Content()
//                }
//            }
            BluetoothTheme {
                Content2()
            }
        }
    }

    /// MAIN ---------------------------------------------------------------------------------------
//    private val keyEventHandlers = mutableListOf<KeyEventHandler>()
//
//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        //android.util.Log.e("AAA", "onKeyDown----- ${event.displayLabel}  /// $keyCode / $event")
//        return keyEventHandlers.reversed().any { it(keyCode, event) } || super.onKeyDown(keyCode, event)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectBluetooth()
    }

    //https://stackoverflow.com/questions/70838476/onkeyevent-without-focus-in-jetpack-compose
//    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
//        android.util.Log.e("AAA", "onKeyUp----- $keyCode / $event")
//        event?.let {
//            addSeparator(event.displayLabel)
//        }
//        return false
//    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////
//    @Composable
//    fun ListenKeyEvents(handler: KeyEventHandler) {
//        val handlerState = rememberUpdatedState(handler)
//        val eventHandlers = LocalKeyEventHandlers.current
//        DisposableEffect(handlerState) {
//            val localHandler: KeyEventHandler = { code, event ->
//                handlerState.value(code, event)
//            }
//            eventHandlers.add(localHandler)
//            onDispose {
//                eventHandlers.remove(localHandler)
//            }
//        }
//    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @Composable
    private fun Content2() {
        val focusRequester = remember { FocusRequester() }
        val hasFocus = remember { mutableStateOf(false) }
        val list = remember { mutableStateOf("") }
        Box(
            Modifier
                .focusRequester(focusRequester)
                .onFocusChanged {
                    hasFocus.value = it.hasFocus
                }
                .focusable()
                .onKeyEvent {
                    if (it.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) return@onKeyEvent true
                    android.util.Log.e("AA", "onKeyEvent-------------${it.nativeKeyEvent}")
                    if (it.key != Key.ShiftLeft) {
                        addSeparator(it.nativeKeyEvent.displayLabel, list)
                    }
                    true
                }
        ) {
            Text(
                text = list.value,
                minLines = 10,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (!hasFocus.value) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
//    @Composable
//    private fun Content() {
//        val list = remember { mutableStateOf("") }
//        ListenKeyEvents { code, event ->
//            //android.util.Log.e("AAA", "Content:ListenKeyEvents------ ${event.displayLabel}  /// $code, $event")
//            addSeparator(event.displayLabel, list)
//            true
//        }
//        Text(
//            text = list.value,
//            minLines = 10,
//            textAlign = TextAlign.Start,
//            modifier = Modifier.fillMaxSize()
//        )
//    }
//    @Preview
//    @Composable
//    private fun Content_preview() {
//        BluetoothTheme {
//            Content()
//        }
//    }
}


private const val Separator = ",\r\n"
private var timerTask: TimerTask? = null
private var timer: Timer? = null
private var time = System.currentTimeMillis()
private var line = StringBuffer()
private fun addSeparator(c: Char, list: MutableState<String>) {
    val offset = System.currentTimeMillis() - time
    time = System.currentTimeMillis()
    android.util.Log.e("AA", "addSeparator------ $c / $line / time=$offset")
    line.append(c)
    timerTask?.cancel()
    timer?.cancel()
    timer = Timer()
    timerTask = object : TimerTask() {
        override fun run() {
            android.util.Log.e("AA", "addSeparator------ *** $line")
            list.value += line.toString() + Separator//text.value + Separator
            line = StringBuffer()
        }
    }
    timer?.schedule(timerTask, 150)
}

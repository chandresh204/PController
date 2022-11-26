package chad.orionsoft.pcontroller

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import chad.orionsoft.pcontroller.databinding.ActivityBlankScreenBinding
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class BlankScreen : AppCompatActivity() {

    private lateinit var binding : ActivityBlankScreenBinding
    private lateinit var mainScope: CoroutineScope
    private lateinit var mainUSBMap : HashMap<Int, String>
    private var usbShiftKey = -1
    private lateinit var shiftUSBMap : HashMap<Int, String>
    private var usbProfile = ""
    private var isUSBShift = false
    private var usbKeyCapturedWithShift = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlankScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainScope = CoroutineScope(Dispatchers.Main)
        mainUSBMap = MainActivity.mainUSBMap
        usbShiftKey = MainActivity.usbShiftKey
        shiftUSBMap = MainActivity.shiftUSBMap
        usbProfile = MainActivity.usbProfile
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or
                        WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION )
        }
        binding.textViewOnBlank.animate().apply {
            duration = 10000
            alpha(0f)
        }
        binding.blankLayout.setOnClickListener {
            binding.textViewOnBlank.clearAnimation()
            if (binding.textViewOnBlank.alpha == 0f) {
                binding.textViewOnBlank.alpha = 1f
                binding.textViewOnBlank.animate().apply {
                    duration = 10000
                    alpha(0f)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 4) {
            return super.onKeyDown(keyCode, event)
        }
        if (usbProfile == "keyboard") {
            val value = Dictionary.convertUSBKeyboardValue(keyCode, MainActivity.os)
            mainScope.launch {
                sendKeyAsync(value, 1).await()
            }
            return true
        }
        if (keyCode == usbShiftKey) {
            isUSBShift = true
            return true
        }
        mainScope.launch {

            if (!isUSBShift) {
                val mainKey = mainUSBMap[keyCode]
                if (mainKey != null) {
                    sendKeyAsync(mainUSBMap[keyCode]!!, 1).await()
                    return@launch
                }
                //       sendKeyAsync(keyCode.toString(), 1).await()
            } else {
                val shiftedKey = shiftUSBMap[keyCode]
                if (shiftedKey != null) {
                    sendKeyAsync(shiftUSBMap[keyCode]!!, 1).await()
                    usbKeyCapturedWithShift = keyCode
                    return@launch
                }
                val mainKey = mainUSBMap[keyCode]
                if (mainKey != null) {
                    sendKeyAsync(mainUSBMap[keyCode]!!, 1).await()
                    return@launch
                }
                //  sendKeyAsync(keyCode.toString(), 1).await()
            }
        }
        return true
    }
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 4) {
            return super.onKeyUp(keyCode, event)
        }
        if (MainActivity.usbProfile == "keyboard") {
            val value = Dictionary.convertUSBKeyboardValue(keyCode, MainActivity.os)
            mainScope.launch {
                sendKeyAsync(value, 0).await()
            }
            return true
        }
        if (keyCode == MainActivity.usbShiftKey) {
            isUSBShift = false
            // release key captured by shift
            if (usbKeyCapturedWithShift > 0) {
                mainScope.launch {
                    sendKeyAsync(shiftUSBMap[usbKeyCapturedWithShift]!!, 0).await()
                    usbKeyCapturedWithShift = -1
                    return@launch
                }
            }
            return true
        }
        mainScope.launch {

            if (!isUSBShift) {
                val mainKey = mainUSBMap[keyCode]
                if (mainKey != null) {
                    sendKeyAsync(mainUSBMap[keyCode]!!, 0).await()
                    return@launch
                }
                //     sendKeyAsync(keyCode.toString(), 1).await()
            } else {
                val shiftedKey =shiftUSBMap[keyCode]
                if (shiftedKey != null) {
                    sendKeyAsync(shiftUSBMap[keyCode]!!, 0).await()
                    return@launch
                }
                val mainKey = mainUSBMap[keyCode]
                if (mainKey != null) {
                    sendKeyAsync(mainUSBMap[keyCode]!!, 0).await()
                    return@launch
                }
                //        sendKeyAsync(keyCode.toString(), 0).await()
            }
        }
        return true
    }

    private suspend fun sendKeyAsync(key:String, operation: Int) =
        coroutineScope {
            async (Dispatchers.IO) {
                val event = "#key@$key*$operation"
                try {
                    val dSocket = DatagramSocket()
                    val bytes = event.toByteArray()
                    val dPacket = DatagramPacket(bytes, bytes.size, InetAddress.getByName(
                        MainActivity.ip
                    ), MainActivity.port
                    )
                    dSocket.send(dPacket)
                    dSocket.close()
                } catch (e: Exception) {
                //    toastHandler.obtainMessage(0, "Error: $e").sendToTarget()
                }
            }
        }
}
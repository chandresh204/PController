package chad.orionsoft.pcontroller

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chad.orionsoft.pcontroller.Dictionary.Companion.getKeyNameFromValue
import chad.orionsoft.pcontroller.Dictionary.Companion.getValueFromKeyName
import chad.orionsoft.pcontroller.databinding.GamepadLayoutBinding
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONException
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.math.atan

class GamepadFullscreen : AppCompatActivity() {

    private lateinit var binding : GamepadLayoutBinding
    private lateinit var mainScope: CoroutineScope
    private var mapping = false
    private lateinit var mapJson: JSONArray
    private lateinit var appDir: File
    private lateinit var prefs: SharedPreferences
    private lateinit var currentProf:String
    private lateinit var toastHandler: Handler
    private var mouseSens =5
    private var curPadBtns = intArrayOf(0,0,0,0)
    private var os = MainActivity.os

   /* private var isPressedU = false
    private var isPressedR = false
    private var isPressedD = false
    private var isPressedL = false */

    override fun onResume() {
        super.onResume()
        os = MainActivity.os
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GamepadLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainScope = CoroutineScope(Dispatchers.Main)
        appDir = File(applicationContext.getExternalFilesDir("Gamepad"), "Profiles")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        mapping = intent.getBooleanExtra(START_MAPPING, false)
        // Toast.makeText(applicationContext, "mapping : $mapping", Toast.LENGTH_SHORT).show()
        currentProf = prefs.getString(PROFILE_IN_PREFS, "null")!!
        mouseSens = prefs.getInt(MOUSE_SPEED_GPAD, 5)
        if (os == Dictionary.OS_WINDOWS) {
            binding.osImage.setImageResource(R.drawable.windows_icon)
        }
        toastHandler = Handler(mainLooper) {
            val msg = it.obj as String
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
            true
        }
        if (currentProf == "null") {

            // create default profiles here
            val fpsProf = JSONArray()
            fpsProf.put(D_UP, getValueFromKeyName("w", os))
            fpsProf.put(D_RIGHT, getValueFromKeyName("s", os))
            fpsProf.put(D_DOWN, getValueFromKeyName("d", os))
            fpsProf.put(D_LEFT, getValueFromKeyName("a", os))
            fpsProf.put(START, getValueFromKeyName(ENTER, os))
            fpsProf.put(SELECT, getValueFromKeyName(LSHIFT, os))
            fpsProf.put(BTN_A, getValueFromKeyName(SPACE, os))
            fpsProf.put(BTN_B, getValueFromKeyName("e", os))
            fpsProf.put(BTN_X, getValueFromKeyName(LCTRL, os))
            fpsProf.put(BTN_Y, getValueFromKeyName("q", os))
            fpsProf.put(BTN_L1, getValueFromKeyName(MOUSE_L, os))
            fpsProf.put(BTN_L2, getValueFromKeyName(ESC, os))
            fpsProf.put(BTN_R1, getValueFromKeyName(MOUSE_DOWN, os))
            fpsProf.put(BTN_R2, getValueFromKeyName(MOUSE_UP, os))
            fpsProf.put(BTN_G1, getValueFromKeyName("x", os))
            fpsProf.put(BTN_G2, getValueFromKeyName("b", os))

            val newProfileFile = File(appDir, "fps")
            val outputStreamWriter = OutputStreamWriter(newProfileFile.outputStream())
            outputStreamWriter.write(fpsProf.toString())
            outputStreamWriter.flush()
            outputStreamWriter.close()

            val adProf = JSONArray()
            adProf.put(D_UP, getValueFromKeyName(UP, os))
            adProf.put(D_RIGHT, getValueFromKeyName(RIGHT, os))
            adProf.put(D_DOWN, getValueFromKeyName(DOWN, os))
            adProf.put(D_LEFT, getValueFromKeyName(LEFT, os))
            adProf.put(START, getValueFromKeyName(ENTER, os))
            adProf.put(SELECT, getValueFromKeyName(LSHIFT, os))
            adProf.put(BTN_A, getValueFromKeyName("a", os))
            adProf.put(BTN_B, getValueFromKeyName("b", os))
            adProf.put(BTN_X, getValueFromKeyName("x", os))
            adProf.put(BTN_Y, getValueFromKeyName("y", os))
            adProf.put(BTN_L1, getValueFromKeyName("q", os))
            adProf.put(BTN_L2, getValueFromKeyName(ESC, os))
            adProf.put(BTN_R1, getValueFromKeyName("e", os))
            adProf.put(BTN_R2, getValueFromKeyName("r", os))
            adProf.put(BTN_G1, getValueFromKeyName(MOUSE_L, os))
            adProf.put(BTN_G2, getValueFromKeyName(MOUSE_R, os))

            val newProfileFile2 = File(appDir, "adventurer")
            val outputStreamWriter2 = OutputStreamWriter(newProfileFile2.outputStream())
            outputStreamWriter2.write(adProf.toString())
            outputStreamWriter2.flush()
            outputStreamWriter2.close()

            startActivity(Intent(applicationContext, GPadProfiles::class.java))
            finish()
        } else {
            showKeyCodesonKeys()
            if (mapping) {
                startMapping()
            } else {
                turnGamePadON()
            }
        }
        binding.menuImage.setOnClickListener {
            val popupMenu = PopupMenu(applicationContext, binding.menuImage)
            popupMenu.inflate(R.menu.menu_gamepad)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.menu_profile_set -> {
                        val i = Intent(applicationContext, GPadProfiles::class.java)
                        i.putExtra("edit", false)
                        startActivity(i)
                        finish()
                    }
                    R.id.menu_edit_profiles -> {
                        val i = Intent(applicationContext, GPadProfiles::class.java)
                        i.putExtra("edit", true)
                        startActivity(i)
                        finish()
                    }
                    R.id.menu_speed -> {
                        val builder = AlertDialog.Builder(this@GamepadFullscreen).apply {
                            val view = layoutInflater.inflate(R.layout.mouse_speed_seek, LinearLayout(this@GamepadFullscreen), false)
                            val seekBar : SeekBar = view.findViewById(R.id.seekBarMS)
                            seekBar.progress = mouseSens - 1
                            setView(view)
                            setPositiveButton("OK") { _, _ ->
                                val prog = seekBar.progress
                                prefs.edit().putInt(MOUSE_SPEED_GPAD, (prog+1)).apply()
                                Toast.makeText(applicationContext, "Mouse movement speed changed", Toast.LENGTH_SHORT).show()
                                mouseSens = prog + 1
                            }
                            setNegativeButton("CANCEL") { _, _ -> }
                        }
                        val dialog = builder.create()
                        dialog.setTitle("Mouse movement speed")
                        dialog.show()
                    }
                }
                true
            }
            popupMenu.show()
        }
    }

    private fun operateDButton(quad:Int, angle:Float) {
        when (quad) {
            1 -> {
                if (0 < angle && angle < 0.54) {
                    performDpadOperation(intArrayOf(0,1,0,0))
                } else if (1.048 < angle && angle < 1.572) {
                    performDpadOperation(intArrayOf(0,0,1,0))
                } else {
                    performDpadOperation(intArrayOf(0,1,1,0))
                }
            }
            2 -> {
                if (0 > angle && angle > -0.54) {
                    performDpadOperation(intArrayOf(0,0,0,1))
                } else if (-1.048 > angle && angle > -1.572) {
                    performDpadOperation(intArrayOf(0,0,1,0))
                } else {
                    performDpadOperation(intArrayOf(0,0,1,1))
                }
            }
            3 -> {
                if (0 < angle && angle < 0.54) {
                    performDpadOperation(intArrayOf(0,0,0,1))
                } else if (1.048 < angle && angle < 1.572) {
                    performDpadOperation(intArrayOf(1,0,0,0))
                } else {
                    performDpadOperation(intArrayOf(1,0,0,1))
                }
            }
            4 -> {
                if (0 > angle && angle > -0.54) {
                    performDpadOperation(intArrayOf(0,1,0,0))
                } else if (-1.048 > angle && angle > -1.572) {
                    performDpadOperation(intArrayOf(1,0,0,0))
                } else {
                    performDpadOperation(intArrayOf(1,1,0,0))
                }
            }
        }
    }

    private fun performDpadOperation(DpadUpdate: IntArray) {
        mainScope.launch {
            if (curPadBtns[0] != DpadUpdate[0]) {
                val change = DpadUpdate[0] - curPadBtns[0]
                if (change > 0) {
                    sendKeyAsync(getValueFromKeyName(mapJson.get(D_UP).toString(), os), 1).await()
                } else {
                    sendKeyAsync(getValueFromKeyName(mapJson.get(D_UP).toString(), os), 0).await()
                }
            }
            if (curPadBtns[1] != DpadUpdate[1]) {
                val change = DpadUpdate[1] - curPadBtns[1]
                if (change > 0) {
                    sendKeyAsync(getValueFromKeyName(mapJson.get(D_RIGHT).toString(), os), 1).await()
                } else {
                    sendKeyAsync(getValueFromKeyName(mapJson.get(D_RIGHT).toString(), os), 0).await()
                }
            }
            if (curPadBtns[2] != DpadUpdate[2]) {
                val change = DpadUpdate[2] - curPadBtns[2]
                if (change > 0) {
                    sendKeyAsync(getValueFromKeyName(mapJson.get(D_DOWN).toString(), os), 1).await()
                } else {
                    sendKeyAsync(getValueFromKeyName(mapJson.get(D_DOWN).toString(), os), 0).await()
                }
            }
            if (curPadBtns[3] != DpadUpdate[3]) {
                val change = DpadUpdate[3] - curPadBtns[3]
                if (change > 0) {
                    sendKeyAsync(getValueFromKeyName(mapJson.get(D_LEFT).toString(), os), 1).await()
                } else {
                    sendKeyAsync(getValueFromKeyName(mapJson.get(D_LEFT).toString(), os), 0).await()
                }
            }
            curPadBtns = DpadUpdate
        }

    }

  /*  suspend fun pressDPadButtonAsync(btnCode: Int) =
        coroutineScope {
            async (Dispatchers.IO) {
                when (btnCode) {
                    D_UP -> {
                        isPressedU = true
                        sendKeyAsync(getValueFromKeyName(mapJson.get(D_UP).toString(), os), 1).await()
                    }
                    D_RIGHT -> {
                        isPressedR = true
                        sendKeyAsync(getValueFromKeyName(mapJson.get(D_RIGHT).toString(), os), 1).await()
                    }
                    D_DOWN -> {
                        isPressedD = true
                        sendKeyAsync(getValueFromKeyName(mapJson.get(D_DOWN).toString(), os), 1).await()
                    }
                    D_LEFT -> {
                        isPressedL = true
                        sendKeyAsync(getValueFromKeyName(mapJson.get(D_LEFT).toString(), os), 1).await()
                    }
                }
            }
        }

    private suspend fun releaseDButtonsAsync() =
        coroutineScope {
            async (Dispatchers.IO) {
                if (isPressedU) {
                    isPressedU = false
                    sendKeyAsync(getValueFromKeyName(mapJson.get(D_UP).toString(), os), 0).await()
                }
                if (isPressedR) {
                    isPressedR = false
                    sendKeyAsync(getValueFromKeyName(mapJson.get(D_RIGHT).toString(), os), 0).await()
                }
                if (isPressedD) {
                    isPressedD = false
                    sendKeyAsync(getValueFromKeyName(mapJson.get(D_DOWN).toString(), os), 0).await()
                }
                if (isPressedL) {
                    isPressedL = false
                    sendKeyAsync(getValueFromKeyName(mapJson.get(D_LEFT).toString(), os), 0).await()
                }
            }
        }  */

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun turnGamePadON() {
        binding.menuImage.visibility = View.VISIBLE
        binding.FScreen.text = "EXIT"
        binding.FScreen.setOnClickListener {
            finish()
        }

        var initX = 0f
        var initY = 0f

        binding.dPadImage.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initX = event.rawX
                    initY = event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX - initX
                    val newY = event.rawY - initY
                    val angle = atan(newY/newX)

                    val quad = if (newX < 0) {
                        if (newY < 0) {
                            3
                        } else
                            2
                    } else {
                        if (newY < 0 ) {
                            4
                        } else {
                            1
                        }
                    }
                    operateDButton(quad, angle)
                }
                MotionEvent.ACTION_UP -> {
                    performDpadOperation(intArrayOf(0,0,0,0))
                }
            }
            true
        }

        binding.buttonST.setOnTouchListener { view, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.setBackgroundResource(R.drawable.btn_in_selection_active)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(4).toString(), 1).await()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    view.setBackgroundResource(R.drawable.btn_in_selection)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(4).toString(), 0).await()
                    }
                }
            }
            true
        }
        binding.buttonSL.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection_active)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(5).toString(), 1).await()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(5).toString(), 0).await()
                    }
                }
            }
            true
        }
        binding.buttonA.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection_active)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(6).toString(), 1).await()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(6).toString(), 0).await()
                    }
                }
            }
            true
        }
        binding.buttonB.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection_active)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(7).toString(), 1).await()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(7).toString(), 0).await()
                    }
                }
            }
            true
        }
        binding.buttonX.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection_active)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(8).toString(), 1).await()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(8).toString(), 0).await()
                    }
                }
            }
            true
        }
        binding.buttonY.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection_active)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(9).toString(), 1).await()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(9).toString(), 0).await()
                    }
                }
            }
            true
        }
        binding.buttonL1.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection_active)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(BTN_L1).toString(), 1).await()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(BTN_L1).toString(), 0).await()
                    }
                }
            }
            true
        }
        binding.buttonL2.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection_active)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(BTN_L2).toString(), 1).await()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(BTN_L2).toString(), 0).await()
                    }
                }
            }
            true
        }
        binding.buttonR1.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection_active)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(BTN_R1).toString(), 1).await()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(BTN_R1).toString(), 0).await()
                    }
                }
            }
            true
        }
        binding.buttonR2.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection_active)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(BTN_R2).toString(), 1).await()

                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(BTN_R2).toString(), 0).await()
                    }
                }
            }
            true
        }
        binding.buttonG1.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection_active)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(BTN_G1).toString(), 1).await()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(BTN_G1).toString(), 0).await()
                    }
                }
            }
            true
        }
        binding.buttonG2.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection_active)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(BTN_G2).toString(), 1).await()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_in_selection)
                    mainScope.launch {
                        sendKeyAsync(mapJson.get(BTN_G2).toString(), 0).await()
                    }
                }
            }
            true
        }

        binding.mouseWheel.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    initX = event.rawX
                    initY = event.rawY
                    mainScope.launch {
                        val event1 = "#mouseDW@0"
                        sendEventAsync(event1).await()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = (event.rawX - initX)
                    val newY = (event.rawY - initY)
                    val event1 = "#mouseMV@${newX*mouseSens} ${newY*mouseSens}"
                    mainScope.launch {
                        sendEventAsync(event1).await()
                    }
                }
            }
            true
        }
    }

    private suspend fun sendEventAsync(event: String) =
        coroutineScope {
            async (Dispatchers.IO) {
                try {
                    val dSocket = DatagramSocket()
                    val bytes = event.toByteArray()
                    val dPacket = DatagramPacket(bytes, bytes.size, InetAddress.getByName(MainActivity.ip), MainActivity.port)
                    dSocket.send(dPacket)
                    dSocket.close()
                } catch (e: Exception) {
                    toastHandler.obtainMessage(0, "Error: $e").sendToTarget()
                }
            }
        }

    private suspend fun sendKeyAsync(key:String, operation: Int) =
        coroutineScope {
            async (Dispatchers.IO) {
                val event = "#key@$key*$operation"
                try {
                    val dSocket = DatagramSocket()
                    val bytes = event.toByteArray()
                    val dPacket = DatagramPacket(bytes, bytes.size, InetAddress.getByName(MainActivity.ip), MainActivity.port)
                    dSocket.send(dPacket)
                    dSocket.close()
                } catch (e: Exception) {
                    toastHandler.obtainMessage(0, "Error: $e").sendToTarget()
                }
            }
        }

    @SuppressLint("SetTextI18n")
    private fun startMapping() {
        binding.FScreen.text = "DONE"
        binding.menuImage.visibility = View.GONE
        val file = File(appDir, currentProf)
        mapJson = if (file.exists()) {
            val fileInputStream = file.inputStream()
            val reader = InputStreamReader(fileInputStream)
            val available = reader.readText()
            Toast.makeText(applicationContext, "map: $available", Toast.LENGTH_SHORT).show()
            JSONArray(available)
        } else {
            JSONArray()
        }
        binding.FScreen.setOnClickListener {
            val builder = AlertDialog.Builder(this@GamepadFullscreen).apply {
                val editText = EditText(this@GamepadFullscreen)
                editText.gravity = Gravity.CENTER
                editText.setText(currentProf)
                setView(editText)
                setMessage("Please give a name to this profile..")
                setNegativeButton("CANCEL") { _, _ ->  }
                setPositiveButton("OK") { _, _ ->
                    if (editText.text.isNullOrEmpty() || editText.text.toString() == NEW_PROF)  {
                        Toast.makeText(applicationContext, "Failed, Not a proper name, Please try again", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val name = editText.text.toString()
                    val file1 = File(appDir, name)
                    if (mapJson.length() < 16) {
                        for (i in mapJson.length() until 16) {
                            mapJson.put(i, "null")
                        }
                    }
                    OutputStreamWriter(file1.outputStream()).apply {
                        write(mapJson.toString())
                        flush()
                        close()
                    }
                    Toast.makeText(applicationContext, "Profile saved", Toast.LENGTH_SHORT).show()
                    prefs.edit().putString(PROFILE_IN_PREFS, name).apply()
                    Handler(mainLooper).postDelayed( {
                        startActivity(Intent(applicationContext, GamepadFullscreen::class.java))
                    }, 500)
                    finish()
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        binding.dPadImage.setOnClickListener {
            askKeyCodeDialog(D_PAD)
        }
        binding.buttonST.setOnClickListener {
            askKeyCodeDialog(START)
        }
        binding.buttonSL.setOnClickListener {
            askKeyCodeDialog(SELECT)
        }
        binding.buttonA.setOnClickListener {
            askKeyCodeDialog(BTN_A)
        }
        binding.buttonB.setOnClickListener {
            askKeyCodeDialog(BTN_B)
        }
        binding.buttonX.setOnClickListener {
            askKeyCodeDialog(BTN_X)
        }
        binding.buttonY.setOnClickListener {
            askKeyCodeDialog(BTN_Y)
        }
        binding.buttonL1.setOnClickListener {
            askKeyCodeDialog(BTN_L1)
        }
        binding.buttonL2.setOnClickListener {
            askKeyCodeDialog(BTN_L2)
        }
        binding.buttonR1.setOnClickListener {
            askKeyCodeDialog(BTN_R1)
        }
        binding.buttonR2.setOnClickListener {
            askKeyCodeDialog(BTN_R2)
        }
        binding.buttonG1.setOnClickListener {
            askKeyCodeDialog(BTN_G1)
        }
        binding.buttonG2.setOnClickListener {
            askKeyCodeDialog(BTN_G2)
        }
    }

    private fun askKeyCodeDialog(padButton : Int) {
        if (padButton == D_PAD) {
            val builder = AlertDialog.Builder(this@GamepadFullscreen).apply {
                setMessage("select how you want to map movement keys")

                setPositiveButton("WSAD") { _, _ ->
                    mapJson.put(D_UP, "w")
                    mapJson.put(D_RIGHT, "d")
                    mapJson.put(D_DOWN, "s")
                    mapJson.put(D_LEFT, "a")
                    showButtonMapped(padButton)
                }
                setNegativeButton("Arrow Keys") { _, _ ->
                    mapJson.put(D_UP, UP)
                    mapJson.put(D_RIGHT, RIGHT)
                    mapJson.put(D_DOWN, DOWN)
                    mapJson.put(D_LEFT, LEFT)
                    showButtonMapped(padButton)
                }
            }
            val dialog = builder.create()
            dialog.show()
            return
        }
        val builder = AlertDialog.Builder(this@GamepadFullscreen).apply {
            val recyLayout = layoutInflater.inflate(R.layout.button_list_recycler_landscape, LinearLayout(applicationContext), false)
            val recy: RecyclerView = recyLayout.findViewById(R.id.keyListRecycler)
            val click1:Button = recyLayout.findViewById(R.id.click1Btn)
            val click2 :Button = recyLayout.findViewById(R.id.click2Btn)
            val click3 : Button = recyLayout.findViewById(R.id.click3Btn)
            val click4 : Button = recyLayout.findViewById(R.id.click4Btn)
            val click5 : Button = recyLayout.findViewById(R.id.click5Btn)
            val keyList = ArrayList<String>()
            for (i in 0 until 10) {
                keyList.add(i.toString())
            }
            for (i in 'a' until ('z'+1)) {
                keyList.add(i.toString())
            }
            keyList.add("`")
            keyList.add("-")
            keyList.add("=")
            keyList.add("[")
            keyList.add("]")
            keyList.add("\\")
            keyList.add(";")
            keyList.add(QOT)
            keyList.add(",")
            keyList.add(".")
            keyList.add("/")
            keyList.add(ESC)
            keyList.add(PRINT)
            keyList.add(BKSPACE)
            keyList.add(TAB)
            keyList.add(CAPS)
            keyList.add(ENTER)
            keyList.add(LSHIFT)
            keyList.add(RSHIFT)
            keyList.add(LCTRL)
            keyList.add(LALT)
            keyList.add(SPACE)
            keyList.add(RALT)
            keyList.add(RCTRL)
            keyList.add(INSERT)
            keyList.add(HOME)
            keyList.add(PAGEUP)
            keyList.add(DELETE)
            keyList.add(END)
            keyList.add(PAGEDOWN)
            keyList.add(UP)
            keyList.add(LEFT)
            keyList.add(DOWN)
            keyList.add(RIGHT)
            recy.layoutManager = GridLayoutManager(applicationContext, 5)
            recy.adapter = KAdapter(keyList, padButton)
            setView(recyLayout)
            /*        val editText = EditText(applicationContext)
                    editText.setEms(6)
                    editText.gravity = Gravity.CENTER
                    try {
                        val oldValue = mapJson.get(padButton).toString()
                        editText.setText(oldValue)
                    } catch (e:Exception) { } */
            setMessage("Please assign a key.")
            selectedValueForBtn = ""
            click1.setOnClickListener {
                mapJson.put(padButton, getValueFromKeyName(MOUSE_L, os))
                showButtonMapped(padButton)
                selectedValueForBtn = getValueFromKeyName(MOUSE_L, os)
            }
            click2.setOnClickListener {
                mapJson.put(padButton, getValueFromKeyName(MOUSE_M, os))
                showButtonMapped(padButton)
                selectedValueForBtn = getValueFromKeyName(MOUSE_L, os)
            }
            click3.setOnClickListener {
                mapJson.put(padButton, getValueFromKeyName(MOUSE_R, os))
                showButtonMapped(padButton)
                selectedValueForBtn = getValueFromKeyName(MOUSE_L, os)
            }
            click4.setOnClickListener {
                mapJson.put(padButton, getValueFromKeyName(MOUSE_UP, os))
                showButtonMapped(padButton)
                selectedValueForBtn = getValueFromKeyName(MOUSE_L, os)
            }
            click5.setOnClickListener {
                mapJson.put(padButton, getValueFromKeyName(MOUSE_DOWN, os))
                showButtonMapped(padButton)
                selectedValueForBtn = getValueFromKeyName(MOUSE_L, os)
            }
            setNegativeButton("CANCEL") { _, _ ->  }
        }
        val dialog = builder.create()
        dialog.show()
        mainScope.launch {
            stayOnDialogAsync().await()
            dialog.cancel()
        }
    }

    inner class KAdapter(private val kList: ArrayList<String>, private val padButton: Int) : RecyclerView.Adapter<KAdapter.KHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KHolder {
            return KHolder(layoutInflater.inflate(R.layout.key_layout, parent, false))
        }

        override fun getItemCount(): Int = kList.size

        override fun onBindViewHolder(holder: KHolder, position: Int) {
            val button = holder.button
            button.text = kList[position]
            button.setOnClickListener {
                selectedValueForBtn = getValueFromKeyName(button.text.toString(), os)
                mapJson.put(padButton, selectedValueForBtn)
                showButtonMapped(padButton)
            }
        }

        inner class KHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val button : Button = itemView.findViewById(R.id.keyText)
        }
    }

    private suspend fun stayOnDialogAsync() =
        coroutineScope {
            async (Dispatchers.IO) {
                while (selectedValueForBtn == "")  {
                    // wait
                }
            }
        }


    @SuppressLint("SetTextI18n")
    private fun showButtonMapped(padButton: Int) {
        when (padButton) {

            D_PAD -> {
                if ((getKeyNameFromValue(mapJson.get(D_UP).toString(), os) == UP)) {
                    binding.dPadKeyCode.text = "Arrow Keys"
                } else {
                    binding.dPadKeyCode.text = "WSAD"
                }
            }
            START -> {
                binding.buttonST.setBackgroundResource(R.drawable.finished_icon_small)
                binding.STRKeyCode.text = getKeyNameFromValue(mapJson.get(padButton).toString(), os)
            }
            SELECT -> {
                binding.buttonSL.setBackgroundResource(R.drawable.finished_icon_small)
                binding.SLCTKeyCode.text = getKeyNameFromValue(mapJson.get(padButton).toString(), os)
            }
            BTN_A -> {
                binding.buttonA.setBackgroundResource(R.drawable.finished_icon_small)
                binding.AKeyCode.text = getKeyNameFromValue(mapJson.get(padButton).toString(), os)
            }
            BTN_B -> {
                binding.buttonB.setBackgroundResource(R.drawable.finished_icon_small)
                binding.BKeyCode.text = getKeyNameFromValue(mapJson.get(padButton).toString(), os)
            }
            BTN_X -> {
                binding.buttonX.setBackgroundResource(R.drawable.finished_icon_small)
                binding.XKeyCode.text = getKeyNameFromValue(mapJson.get(padButton).toString(), os)
            }
            BTN_Y -> {
                binding.buttonY.setBackgroundResource(R.drawable.finished_icon_small)
                binding.YKeyCode.text = getKeyNameFromValue(mapJson.get(padButton).toString(), os)
            }
            BTN_L1 -> {
                binding.buttonL1.setBackgroundResource(R.drawable.finished_icon_small)
                binding.L1KeyCode.text = getKeyNameFromValue(mapJson.get(padButton).toString(), os)
            }
            BTN_L2 -> {
                binding.buttonL2.setBackgroundResource(R.drawable.finished_icon_small)
                binding.L2KeyCode.text = getKeyNameFromValue(mapJson.get(padButton).toString(), os)
            }
            BTN_R1 -> {
                binding.buttonR1.setBackgroundResource(R.drawable.finished_icon_small)
                binding.R1KeyCode.text = getKeyNameFromValue(mapJson.get(padButton).toString(), os)
            }
            BTN_R2 -> {
                binding.buttonR2.setBackgroundResource(R.drawable.finished_icon_small)
                binding.R2KeyCode.text = getKeyNameFromValue(mapJson.get(padButton).toString(), os)
            }
            BTN_G1 -> {
                binding.buttonG1.setBackgroundResource(R.drawable.finished_icon_small)
                binding.G1KeyCode.text = getKeyNameFromValue(mapJson.get(padButton).toString(), os)
            }
            BTN_G2 -> {
                binding.buttonG2.setBackgroundResource(R.drawable.finished_icon_small)
                binding.G2KeyCode.text = getKeyNameFromValue(mapJson.get(padButton).toString(), os)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showKeyCodesonKeys() {
        val file = File(appDir, currentProf)
        if (file.exists()) {
            val fileInputStream = file.inputStream()
            val reader = InputStreamReader(fileInputStream)
            val available = reader.readText()
            Toast.makeText(applicationContext, "map: $available", Toast.LENGTH_SHORT).show()
            mapJson = JSONArray(available)
            try {
                if ((getKeyNameFromValue(mapJson.get(D_UP).toString(), os)) == UP) {
                    binding.dPadKeyCode.text = "Arrow keys"
                } else {
                    binding.dPadKeyCode.text = "WSAD"
                }
                binding.STRKeyCode.text = getKeyNameFromValue(mapJson[4].toString(), os)
                binding.SLCTKeyCode.text = getKeyNameFromValue(mapJson[5].toString(), os)
                binding.AKeyCode.text = getKeyNameFromValue(mapJson[6].toString(), os)
                binding.BKeyCode.text = getKeyNameFromValue(mapJson[7].toString(), os)
                binding.XKeyCode.text = getKeyNameFromValue(mapJson[8].toString(), os)
                binding.YKeyCode.text = getKeyNameFromValue(mapJson[9].toString(), os)
                binding.L1KeyCode.text = getKeyNameFromValue(mapJson[10].toString(), os)
                binding.L2KeyCode.text = getKeyNameFromValue(mapJson[11].toString(), os)
                binding.R1KeyCode.text = getKeyNameFromValue(mapJson[12].toString(), os)
                binding.R2KeyCode.text = getKeyNameFromValue(mapJson[13].toString(), os)
                binding.G1KeyCode.text = getKeyNameFromValue(mapJson[14].toString(), os)
                binding.G2KeyCode.text = getKeyNameFromValue(mapJson[15].toString(), os)

            } catch (e: JSONException) {}
        } else {
            Toast.makeText(applicationContext, "profile: $currentProf not found", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        private var selectedValueForBtn = ""

        const val PROFILE_IN_PREFS = "profile"
        const val START_MAPPING = "mapping"
        const val NEW_PROF = "EnterName"
        const val MOUSE_SPEED_GPAD = "GPadMouseSpeed"

        //special keys
        const val ESC = "esc"
        const val PRINT = "print"
        const val BKSPACE = "bkspace"
        const val TAB = "tab"
        const val CAPS = "caps"
        const val ENTER = "enter"
        const val LSHIFT = "lshift"
        const val RSHIFT = "rshift"
        const val LCTRL = "lcntrl"
        const val LALT = "lalt"
        const val SPACE = "space"
        const val RALT = "ralt"
        const val RCTRL = "lctl"
        const val INSERT = "insert"
        const val HOME = "home"
        const val PAGEUP = "pageup"
        const val DELETE = "delete"
        const val END = "end"
        const val PAGEDOWN = "pagedown"
        const val UP = "a_up"
        const val LEFT = "a_left"
        const val DOWN = "a_down"
        const val RIGHT = "a_right"
        const val MOUSE_L = "mouseL"
        const val MOUSE_M = "mouseM"
        const val MOUSE_R = "mouseR"
        const val MOUSE_UP = "mouseSclUP"
        const val MOUSE_DOWN = "mouseSclDown"
        const val QOT = "'"

        // string values of buttons
        const val D_PAD = -1
        const val D_UP =0
        const val D_RIGHT = 1
        const val D_DOWN = 2
        const val D_LEFT = 3
        const val START = 4
        const val SELECT = 5
        const val BTN_A = 6
        const val BTN_B = 7
        const val BTN_X = 8
        const val BTN_Y = 9
        const val BTN_L1 = 10
        const val BTN_L2 = 11
        const val BTN_R1 = 12
        const val BTN_R2 = 13
        const val BTN_G1 = 14
        const val BTN_G2 = 15

    }
}
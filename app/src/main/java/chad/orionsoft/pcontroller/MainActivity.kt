package chad.orionsoft.pcontroller

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import chad.orionsoft.pcontroller.databinding.ActivityControllerBinding
import chad.orionsoft.pcontroller.databinding.PartnerSearchLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityControllerBinding
    private lateinit var mainScope: CoroutineScope
    private var mSectionStateAdapter : FragmentStateAdapter? = null
    private lateinit var toastHandler: Handler
    private lateinit var generateComputerListHandler: Handler
    private lateinit var computerSetHandler : Handler
    private lateinit var noConnectionAttentionHandler : Handler
    private lateinit var usbTextView : TextView
    private var isUSBShift = false
    private var usbKeyCapturedWithShift = -1
    private var isErrorShown = false
    private var keepSearching = true


    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        mainScope = CoroutineScope(Dispatchers.Main)
        toastHandler = Handler(mainLooper) {
            if (!isErrorShown) {
                val msg = it.obj as String
                Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                isErrorShown = true
            }
            true
        }
        noConnectionAttentionHandler = Handler(mainLooper) {
            binding.noConnectionText.setBackgroundColor(Color.YELLOW)
            toastHandler.postDelayed( {
                binding.noConnectionText.setBackgroundColor(Color.RED)
            },500)
        }
        BottomSheetDialog(this@MainActivity).apply {
            setCancelable(false)
            val sheetBinding = PartnerSearchLayoutBinding.inflate(layoutInflater)
            setContentView(sheetBinding.root)
            sheetBinding.cancelButton.setOnClickListener {
                binding.noConnectionText.visibility = View.VISIBLE
                this.dismiss()
            }
            val computerList = ArrayList<Array<*>>()
            generateComputerListHandler = Handler(mainLooper) {
                val array1 = it.obj as Array<*>
                if (computerList.isEmpty()) {
                    val connectText = TextView(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                        text = "Click on the name to connect"
                    }
                    val itemBtn = Button(this@MainActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                        text = array1[0].toString()
                    }
                    itemBtn.setOnClickListener {
                        pcName = array1[0].toString()
                        ip = array1[1].toString()
                        binding.noConnectionText.visibility = View.GONE
                    }
                    sheetBinding.computerListLayout.addView(connectText)
                    sheetBinding.computerListLayout.addView(itemBtn)
                    computerList.add(array1)
                } else {
                    for (ary1 in computerList) {
                        val pcName1 = ary1[0]
                        val ip1 = ary1[1]
                        if (pcName1 != array1[0] || ip1 != array1[1]) {
                            val itemBtn = Button(this@MainActivity).apply {
                                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT)
                                text = array1[0].toString()
                            }
                            itemBtn.setOnClickListener {
                                pcName = array1[0].toString()
                                ip = array1[1].toString()
                                binding.noConnectionText.visibility = View.GONE
                            }
                            sheetBinding.computerListLayout.addView(itemBtn)
                            computerList.add(array1)
                        }
                    }
                }
                true
            }
            computerSetHandler = Handler(mainLooper) {
                this.dismiss()
                true
            }
            CoroutineScope(Dispatchers.Main).launch {
               searchComputerAsync().await()
            }
            show()
        }
        usbTextView = TextView(applicationContext)
        usbTextView.text = resources.getString(R.string.app_name)
        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        //  ip = prefs.getString("ip", "192.168.")!!
        os = prefs.getInt(Dictionary.PREFS_OS, Dictionary.OS_WINDOWS)
        usbProfile = prefs.getString(Dictionary.USBPROF, "keyboard")!!
        loadUSBKeyMap(applicationContext)
        mouseSensMain = prefs.getInt(MOUSE_SPEED_MAIN, 1)
        mSectionStateAdapter = SectionStateAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.

        binding.container.adapter = mSectionStateAdapter
        binding.container.isUserInputEnabled = false

        TabLayoutMediator(binding.tabs, binding.container) { tab, position ->
            when(position) {
                0 -> tab.text="KEYBOARD"
                1 -> tab.text="MOUSE"
                2 -> tab.text="GAMEPAD"
                3 -> tab.text= "WIRELESS USB"
            }
        }.attach()
    }

    private suspend fun searchComputerAsync() : Deferred<Unit> =
        coroutineScope {
            async (Dispatchers.IO) {
                val dSocket = DatagramSocket()
                dSocket.soTimeout = 3000
                val buff = ByteArray(1024)
                while (keepSearching) {
                    for (i in 0 until 255) {
                        try {
                            val dPacket = DatagramPacket(HANDSHAKE_SIG, HANDSHAKE_SIG.size,
                                    InetAddress.getByName("192.168.$i.255"), port)
                            dSocket.send(dPacket)
                            val rPacket = DatagramPacket(buff, buff.size)
                            dSocket.receive(rPacket)
                            val msg = String(buff, 0, buff.size)
                            // toastHandler.obtainMessage(0, msg).sendToTarget()
                            if (msg.contains(OKAY_SIG)) {
                                val hostnameIndex = msg.indexOf("/*")
                                val endIndex = msg.indexOf("*/")
                                val hostname = msg.substring(hostnameIndex+2, endIndex)
                                val address = rPacket.address
                                val addressString = address.toString()
                                val realAddress = addressString.substring(1)
                                generateComputerListHandler.obtainMessage(0, arrayOf(hostname, realAddress)).sendToTarget()
                                if (hostname == pcName && realAddress == ip) {
                                    val okaySig = OKAY_SIG.toByteArray()
                                    val okPacket = DatagramPacket(okaySig, okaySig.size, address, port)
                                    dSocket.send(okPacket)
                                    keepSearching = false
                                    computerSetHandler.obtainMessage(0,0).sendToTarget()
                                }
                            }
                            delay(3000)
                        } catch (e: IOException) {  }
                    }
                }
            }
        }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 4) {
            return super.onKeyDown(keyCode, event)
        }
        if (usbProfile == "keyboard") {
            val value = Dictionary.convertUSBKeyboardValue(keyCode, os)
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
        if (usbProfile == "keyboard") {
            val value = Dictionary.convertUSBKeyboardValue(keyCode, os)
            mainScope.launch {
                sendKeyAsync(value, 0).await()
            }
            return true
        }
        if (keyCode == usbShiftKey) {
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
                if (ip == "null") {
                    noConnectionAttentionHandler.obtainMessage(0,0).sendToTarget()
                    return@async
                }
                val event = "#key@$key*$operation"
                try {
                    val dSocket = DatagramSocket()
                    val bytes = event.toByteArray()
                    val dPacket = DatagramPacket(bytes, bytes.size, InetAddress.getByName(ip), port)
                    dSocket.send(dPacket)
                    dSocket.close()
                } catch (e: Exception) {
                    toastHandler.obtainMessage(0, "Error: $e").sendToTarget()
                }
            }
        }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_controller, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        when (item.itemId) {
            R.id.menu_mouse_main -> {
                val builder = AlertDialog.Builder(this@MainActivity).apply {
                    val view = layoutInflater.inflate(R.layout.mouse_speed_seek, LinearLayout(this@MainActivity), false)
                    val seekBar : SeekBar = view.findViewById(R.id.seekBarMS)
                    seekBar.progress = mouseSensMain - 1
                    setView(view)
                    setPositiveButton("OK") { _, _ ->
                        val prog = seekBar.progress
                        prefs.edit().putInt(MOUSE_SPEED_MAIN, (prog+1)).apply()
                        Toast.makeText(applicationContext, "Mouse movement speed changed", Toast.LENGTH_SHORT).show()
                        mouseSensMain = prog + 1
                    }
                    setNegativeButton("CANCEL") { _, _ -> }
                }
                val dialog = builder.create()
                dialog.setTitle("Mouse movement speed")
                dialog.show()
            }
            R.id.menu_os_main -> {
                val builder = AlertDialog.Builder(this@MainActivity).apply {
                    val view = layoutInflater.inflate(R.layout.os_selection_layout, LinearLayout(this@MainActivity), false)
                    val linuxBtn :ImageView = view.findViewById(R.id.linux_image)
                    val windowsBtn : ImageView = view.findViewById(R.id.windows_image)
                    setView(view)
                    if (os == Dictionary.OS_LINUX) {
                        linuxBtn.setBackgroundResource(R.drawable.mousepad)
                        windowsBtn.setBackgroundResource(0)
                    }
                    linuxBtn.setOnClickListener {
                        os = Dictionary.OS_LINUX
                        linuxBtn.setBackgroundResource(R.drawable.mousepad)
                        windowsBtn.setBackgroundResource(0)
                    }
                    windowsBtn.setOnClickListener {
                        os = Dictionary.OS_WINDOWS
                        linuxBtn.setBackgroundResource(0)
                        windowsBtn.setBackgroundResource(R.drawable.mousepad)
                    }
                    setMessage("Select the os on which you want to operate this App")
                    setPositiveButton("OKAY") { _,_ ->
                        prefs.edit().putInt(Dictionary.PREFS_OS, os).apply()
                    }
                    setNegativeButton("CANCEL") { _, _ -> }
                }
                val dialog = builder.create()
                dialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    inner class SectionStateAdapter(fragmentM: FragmentManager) : FragmentStateAdapter(fragmentM, lifecycle) {

        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> KeyBoardFragment.newInstance(applicationContext, mainScope)
                1 -> MouseFragment.newInstance(applicationContext, toastHandler, mainScope)
                2 -> GamepadFragment.newInstance(applicationContext)
                else -> USBFragment.newInstance(applicationContext, usbTextView)
            }
        }

    }

    class KeyBoardFragment : Fragment() {

        private lateinit var toastHandler: Handler
        private var isErrorShow = false
        private lateinit var ctx:Context
        private lateinit var mainScope: CoroutineScope

        @SuppressLint("ClickableViewAccessibility")
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.keyboard_layout, container, false)
            val textBox: EditText = rootView.findViewById(R.id.keyboardText)
            val sendButton : Button = rootView.findViewById(R.id.sendTextButton)
            val backSButton :Button = rootView.findViewById(R.id.sendBackSButton)
            val entButton : Button = rootView.findViewById(R.id.sendReturnButton)
            val delButton : Button = rootView.findViewById(R.id.sendDelButton)
            val altButton : Button = rootView.findViewById(R.id.sendALTButton)
            val escButton : Button = rootView.findViewById(R.id.senESCButton)
            val tabButton : Button = rootView.findViewById(R.id.sendTABButton)
            val ctrlButton : Button = rootView.findViewById(R.id.sendCTRLButton)
            val startButton : Button = rootView.findViewById(R.id.sendStartButton)
            val spaceButton : Button = rootView.findViewById(R.id.sendSpaceButton)
            val undoButton : Button = rootView.findViewById(R.id.sendUndoButton)

            toastHandler = Handler(Looper.getMainLooper()) {
                if (!isErrorShow) {
                    val msg = it.obj as String
                    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                    isErrorShow = true
                }
                true
            }
            sendButton.setOnClickListener {
                val event = "#text@${textBox.text}"
                mainScope.launch {
                    sendTextAsync(event).await()
                }
                textBox.setText("")
            }
            backSButton.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection_active)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.BKSPACE, os), 1).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.BKSPACE, os), 0).await()
                        }
                    }
                }
                true
            }
            entButton.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection_active)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.ENTER, os), 1).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.ENTER, os), 0).await()
                        }
                    }
                }
                true
            }
            delButton.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection_active)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.DELETE, os), 1).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.DELETE, os), 0).await()
                        }
                    }
                }
                true
            }
            altButton.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection_active)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.LALT, os), 1).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.LALT, os), 0).await()
                        }
                    }
                }
                true
            }
            escButton.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection_active)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.ESC, os), 1).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.ESC, os), 0).await()
                        }
                    }
                }
                true
            }
            tabButton.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection_active)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.TAB, os), 1).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.TAB, os), 0).await()
                        }
                    }
                }
                true
            }
            ctrlButton.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection_active)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.LCTRL, os), 1).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.LCTRL, os), 0).await()
                        }
                    }
                }
                true
            }
            startButton.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection_active)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.START, os), 1).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.START, os), 0).await()
                        }
                    }
                }
                true
            }
            spaceButton.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection_active)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.SPACE, os), 1).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        v.setBackgroundResource(R.drawable.btn_in_selection)
                        mainScope.launch {
                            sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.SPACE, os), 0).await()
                        }
                    }
                }
                true
            }
            undoButton.setOnClickListener {
                mainScope.launch {
                    sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.LCTRL, os), 1).await()
                    sendKeyAsync("z", 1).await()
                    delay(100)
                    sendKeyAsync("z", 0).await()
                    sendKeyAsync(Dictionary.getValueFromKeyName(Dictionary.LCTRL, os), 0).await()
                }
            }
            return rootView
        }

        private suspend fun sendTextAsync(event: String) =
            coroutineScope {
                async(Dispatchers.IO) {
                    try {
                        val dSocket = DatagramSocket()
                        val bytes = event.toByteArray()
                        val dPacket =
                            DatagramPacket(bytes, bytes.size, InetAddress.getByName(ip), port)
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
                        val dPacket = DatagramPacket(bytes, bytes.size, InetAddress.getByName(ip), port)
                        dSocket.send(dPacket)
                        dSocket.close()
                    } catch (e: Exception) {
                        toastHandler.obtainMessage(0, "Error: $e").sendToTarget()
                    }
                }
            }

        companion object {

            fun newInstance(ctx:Context, mainScope: CoroutineScope) : KeyBoardFragment {
                val fragment = KeyBoardFragment()
                fragment.ctx = ctx
                fragment.mainScope = mainScope
                return fragment
            }
        }
    }

    class MouseFragment : Fragment() {

        private lateinit var ctx:Context
        private lateinit var toastHandler: Handler
        private lateinit var mainScope: CoroutineScope

        @SuppressLint("ClickableViewAccessibility")
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.mouse_layout, container, false)
            val mouseL : Button = rootView.findViewById(R.id.mouseL)
            val mouseM : Button = rootView.findViewById(R.id.mouseM)
            val mouseR : Button = rootView.findViewById(R.id.mouseR)
            val mouseScrl : ImageView = rootView.findViewById(R.id.mouseScroll)
            val mousePad :ImageView = rootView.findViewById(R.id.mousePad)

            mouseL.setOnTouchListener { v, event1 ->
                when(event1.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val event = "#mouseL@1"
                        v.setBackgroundResource(R.drawable.btn_in_selection_active)
                        mainScope.launch {
                            sendEventAsync(event).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val event = "#mouseL@0"
                        v.setBackgroundResource(R.drawable.btn_in_selection)
                        mainScope.launch {
                            sendEventAsync(event).await()
                        }
                    }
                }
                true
            }

            mouseM.setOnTouchListener { v, event1 ->
                when(event1.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val event = "#mouseM@1"
                        v.setBackgroundResource(R.drawable.btn_in_selection_active)
                        mainScope.launch {
                            sendEventAsync(event).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val event = "#mouseM@0"
                        v.setBackgroundResource(R.drawable.btn_in_selection)
                        mainScope.launch {
                            sendEventAsync(event).await()
                        }
                    }
                }
                true
            }

            mouseR.setOnTouchListener { v, event1 ->
                when(event1.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val event = "#mouseR@1"
                        v.setBackgroundResource(R.drawable.btn_in_selection_active)
                        mainScope.launch {
                            sendEventAsync(event).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val event = "#mouseR@0"
                        v.setBackgroundResource(R.drawable.btn_in_selection)
                        mainScope.launch {
                            sendEventAsync(event).await()
                        }
                    }
                }
                true
            }

            var initY = 0f
            var steps =0
            mouseScrl.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        steps = 4
                        initY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        steps++
                        if (steps % 5 == 0) {
                            val newY = initY - event.rawY
                            if (newY > 0) {
                                val event1 = "#mouseSCR@1"
                                mainScope.launch {
                                    sendEventAsync(event1).await()
                                }
                            } else {
                                val event1 = "#mouseSCR@0"
                                mainScope.launch {
                                    sendEventAsync(event1).await()
                                }
                            }
                        }
                    }
                }
                true
            }

            var initX = 0f
            var touchStarted = 0L
            mousePad.setOnTouchListener { _, event ->
                when (event.action) {

                    MotionEvent.ACTION_DOWN -> {
                        initX = event.rawX
                        initY = event.rawY
                        touchStarted = System.currentTimeMillis()
                        mainScope.launch {
                            val event1 = "#mouseDW@0"
                            sendEventAsync(event1).await()
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newX = event.rawX - initX
                        val newY = event.rawY - initY
                        val event1 = "#mouseMV@${newX* mouseSensMain} ${newY* mouseSensMain}"
                        mainScope.launch {
                            sendEventAsync(event1).await()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val touchFinished = System.currentTimeMillis()
                        if ((touchFinished - touchStarted) < 200) {
                            val event1 = "#mouseL@1"
                            val event2 = "#mouseL@0"
                            mainScope.launch {
                                sendEventAsync(event1).await()
                                delay(100)
                                sendEventAsync(event2).await()
                            }
                        }
                    }
                }
                true
            }
            return rootView
        }

        private suspend fun sendEventAsync(event:String) =
            coroutineScope {
                async (Dispatchers.IO) {
                    try {
                        val dSocket = DatagramSocket()
                        val bytes = event.toByteArray()
                        val dPacket = DatagramPacket(bytes, bytes.size, InetAddress.getByName(ip), port)
                        dSocket.send(dPacket)
                        dSocket.close()
                    } catch (e: Exception) {
                        toastHandler.obtainMessage(0, "Error: $e").sendToTarget()
                    }
                }
            }

        companion object {

            fun newInstance(ctx:Context, toastHandler: Handler, mainScope: CoroutineScope) : MouseFragment {
                val fragment = MouseFragment()
                fragment.ctx = ctx
                fragment.toastHandler = toastHandler
                fragment.mainScope = mainScope
                return fragment
            }
        }
    }

    class GamepadFragment : Fragment() {

        lateinit var ctx: Context

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.game_fragment, container, false)
            val fScreenBtn : Button = rootView.findViewById(R.id.startGPADBtn)
            fScreenBtn.setOnClickListener {
                startActivity(Intent(ctx, GamepadFullscreen::class.java))
            }
            return rootView
        }

        companion object {

            fun newInstance(ctx:Context) : GamepadFragment {
                val fragment = GamepadFragment()
                fragment.ctx = ctx
                return fragment
            }
        }
    }

    class USBFragment : Fragment() {

        lateinit var ctx: Context
        lateinit var textView : TextView
        private lateinit var profileListLayout: LinearLayout

        @SuppressLint("SetTextI18n")
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = layoutInflater.inflate(R.layout.usb_fragment_layout, container, false)
            val usbInfoText1 : TextView = view.findViewById(R.id.usbInfoText)
            usbInfoText1.text = "${resources.getString(R.string.wusb_info)} \n ${resources.getString(R.string.usb_keyboard_info)}"
            profileListLayout= view.findViewById(R.id.usb_profile_list)
            val newPBtn : Button = view.findViewById(R.id.usbNewBtn)
            val saveEButton : Button = view.findViewById(R.id.saveEnergy)
            newPBtn.setOnClickListener {
                startActivity(Intent(ctx, USBProfileAct::class.java))
            }
            saveEButton.setOnClickListener {
                startActivity(Intent(ctx, BlankScreen::class.java))
            }
            return view
        }

        override fun onResume() {
            setViews()
            super.onResume()
        }

        @SuppressLint("SetTextI18n")
        private fun setViews() {
            profileListLayout.removeAllViews()
            val defaultProfileBtn = Button(ctx)
            defaultProfileBtn.text = "keyboard"
            defaultProfileBtn.setBackgroundResource(R.drawable.btn_in_selection)
            if (usbProfile == "keyboard") {
                defaultProfileBtn.setBackgroundResource(R.drawable.btn_in_selection_active)
            }
            defaultProfileBtn.setOnClickListener {
                usbProfile = "keyboard"
                prefs.edit().putString(Dictionary.USBPROF, "keyboard").apply()
                Toast.makeText(ctx, "Profile selected", Toast.LENGTH_SHORT).show()
                setViews()
            }
            profileListLayout.addView(defaultProfileBtn)
            val appDir = ctx.getExternalFilesDir("USB_profiles")
            if (appDir!!.exists()) {
                val fileNames = appDir.list()
                for (i in fileNames!!) {
                    val btn = Button(ctx)
                    btn.text = i
                    btn.setBackgroundResource(R.drawable.btn_in_selection)
                    if (usbProfile == i) {
                        btn.setBackgroundResource(R.drawable.btn_in_selection_active)
                    }
                    btn.setOnClickListener {
                        usbProfile = i
                        prefs.edit().putString(Dictionary.USBPROF, i).apply()
                        Toast.makeText(ctx, "Profile selected", Toast.LENGTH_SHORT).show()
                        loadUSBKeyMap(ctx)
                        setViews()
                    }
                    btn.setOnLongClickListener {
                        val popup = PopupMenu(ctx,it)
                        popup.menuInflater.inflate(R.menu.menu_usb_profile, popup.menu)
                        popup.setOnMenuItemClickListener { item ->
                            when(item.itemId) {
                                R.id.menu_select_usb_prof -> {
                                    usbProfile = i
                                    prefs.edit().putString(Dictionary.USBPROF, i).apply()
                                    Toast.makeText(ctx, "Profile selected", Toast.LENGTH_SHORT).show()
                                    setViews()
                                }
                                R.id.menu_edit_usb_profile -> {
                                    val i1 = Intent(ctx, USBProfileAct::class.java).apply {
                                        putExtra(Dictionary.USBPROF, i)
                                    }
                                    startActivity(i1)
                                }
                                R.id.menu_delete_usb_profile -> {
                                    if (usbProfile != i)  {
                                        val thisFile = File(appDir, i)
                                        if (thisFile.delete()) {
                                            Toast.makeText(ctx, "Profile deleted", Toast.LENGTH_SHORT).show()
                                            setViews()
                                        } else {
                                            Toast.makeText(ctx, "Failed to delete profile", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(ctx, "Profile in use, can not delete", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            true
                        }
                        popup.show()
                        true
                    }
                    profileListLayout.addView(btn)
                }
            }
        }

        companion object {

            fun newInstance(ctx: Context, textView: TextView) : USBFragment {
                val fragment = USBFragment()
                fragment.ctx = ctx
                fragment.textView = textView
                return fragment
            }
        }
    }

    companion object {
        var os = Dictionary.OS_WINDOWS
        const val port = 2020
        val HANDSHAKE_SIG = "PController_connection_signal".toByteArray()
        const val OKAY_SIG = "PController_okay"
        var pcName : String = "null"
        var ip:String = "null"
        private lateinit var prefs : SharedPreferences
        const val MOUSE_SPEED_MAIN = "MouseSpeedMain"
        var mouseSensMain = 1
        var usbProfile = ""
        private lateinit var usbKeyMap : USBProfileAct.Companion.KeyArray
        lateinit var mainUSBMap : HashMap<Int, String>
        var usbShiftKey = -1
        lateinit var shiftUSBMap : HashMap<Int, String>

        fun loadUSBKeyMap(ctx: Context) {
            val appDir = ctx.getExternalFilesDir("USB_profiles")
            val proFile = File(appDir, usbProfile)
            if (proFile.exists()) {
                val oIn = ObjectInputStream(proFile.inputStream())
                usbKeyMap = oIn.readObject() as USBProfileAct.Companion.KeyArray
                usbShiftKey = usbKeyMap.shiftKey
                mainUSBMap = usbKeyMap.mainKeyArray
                shiftUSBMap = usbKeyMap.shiftKeyArray
                // Toast.makeText(ctx, "USB Profile loaded successfully", Toast.LENGTH_SHORT).show()
            } else {
                // Toast.makeText(ctx, "default usb profile loaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
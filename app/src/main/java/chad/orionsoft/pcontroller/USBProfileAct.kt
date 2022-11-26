package chad.orionsoft.pcontroller

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chad.orionsoft.pcontroller.databinding.ActivityUsbprofileBinding
import kotlinx.coroutines.*

class USBProfileAct : AppCompatActivity() {

    private lateinit var mainScope: CoroutineScope
    private var keyArray = KeyArray()
    private lateinit var prefs: SharedPreferences
    private lateinit var binding : ActivityUsbprofileBinding
    private var usbProfile :String? = ""
    private var activeKeyCode = 0
    private var assignValueToKeyCode = "null"
    private var selectingShift = false
    private var isShiftActive = false
    private lateinit var textHandler : Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsbprofileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        mainScope = CoroutineScope(Dispatchers.Main)
        prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        usbProfile = intent.getStringExtra(Dictionary.USBPROF)
        Toast.makeText(applicationContext, "USB profile: $usbProfile", Toast.LENGTH_SHORT).show()
        textHandler = Handler(mainLooper) {
            showKeyValues()
            true
        }
        if (!usbProfile.isNullOrEmpty()) {
            loadKeyValues()
        }
        keyArray.os = usb_OS
        val builder = AlertDialog.Builder(this@USBProfileAct).apply {
            val view1 = layoutInflater.inflate(R.layout.os_selection_layout, LinearLayout(this@USBProfileAct), false)
            val linuxBtn : ImageView = view1.findViewById(R.id.linux_image)
            val windowsBtn : ImageView = view1.findViewById(R.id.windows_image)
            setView(view1)
            if (usb_OS == Dictionary.OS_LINUX) {
                linuxBtn.setBackgroundResource(R.drawable.mousepad)
                windowsBtn.setBackgroundResource(0)
            }
            linuxBtn.setOnClickListener {
                usb_OS = Dictionary.OS_LINUX
                linuxBtn.setBackgroundResource(R.drawable.mousepad)
                windowsBtn.setBackgroundResource(0)
            }
            windowsBtn.setOnClickListener {
                usb_OS = Dictionary.OS_WINDOWS
                linuxBtn.setBackgroundResource(0)
                windowsBtn.setBackgroundResource(R.drawable.mousepad)
            }
            setMessage("Select the os on which you want to operate this App")
            setPositiveButton("OKAY") { _,_ ->
                keyArray.os = usb_OS
            }
            setNegativeButton("CANCEL") { _, _ -> }
        }
        val dialog = builder.create()
        dialog.show()
        binding.usbShiftSelect.setOnClickListener {
            if (isShiftActive) {
                toggleSwiftButton(false)
                return@setOnClickListener
            }
            Toast.makeText(applicationContext, "Press a button to select it as Shift button", Toast.LENGTH_SHORT).show()
            keyArray.shiftKey = -1
            selectingShift = true
            toggleSwiftButton(true)
        }
        binding.finishUSBMap.setOnClickListener {
            val builder1 = AlertDialog.Builder(this@USBProfileAct).apply {
                val editText = EditText(this@USBProfileAct)
                editText.hint = "enter profile name here"
                if (!usbProfile.isNullOrEmpty()) {
                    editText.setText(usbProfile)
                }
                setView(editText)
                setNegativeButton("CANCEL") { _, _ -> }
                setPositiveButton("OKAY") { _, _ ->
                    if (editText.text.isNullOrEmpty()) {
                        Toast.makeText(applicationContext, "failed!. not a perfect name", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val appDir = applicationContext.getExternalFilesDir("USB_profiles")
                    if (!appDir!!.exists())
                        appDir.mkdirs()
                    val filename = if (keyArray.os == Dictionary.OS_WINDOWS) {
                        "${editText.text}(windows)"
                    } else {
                        "${editText.text}(linux)"
                    }
                    val file = File(appDir, filename)
                    val objectOutputStream = ObjectOutputStream(FileOutputStream(file))
                    objectOutputStream.writeObject(keyArray)
                    objectOutputStream.flush()
                    objectOutputStream.close()
                    finish()
                    /*      usbProfile = editText.text.toString()
                          prefs.edit().putString(Dictionary.USBPROF, usbProfile).apply()  */
                    finish()
                }
            }
            val dialog1 = builder1.create()
            dialog1.show()
        }

        binding.finishUSBMap.setOnLongClickListener {
            true
        }
    }

    private fun toggleSwiftButton(isActive: Boolean) {
        if (isActive) {
            isShiftActive = true
            binding.shiftStatus.visibility = View.VISIBLE
        } else {
            isShiftActive = false
            binding.shiftStatus.visibility = View.GONE
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyUp(keyCode, event)
        }
        var dialog : AlertDialog? =null
        if (selectingShift) {
            keyArray.shiftKey = keyCode
            Toast.makeText(applicationContext, "${KeyEvent.keyCodeToString(keyCode)} selected as Shift Key", Toast.LENGTH_SHORT).show()
            selectingShift = false
            toggleSwiftButton(true)
            isShiftActive = true
            showKeyValues()
            return true
        }
        activeKeyCode = keyCode
        val builder = AlertDialog.Builder(this@USBProfileAct).apply {
            val recyLayout = layoutInflater.inflate(R.layout.button_list_recycler, LinearLayout(applicationContext), false)
            val recy: RecyclerView = recyLayout.findViewById(R.id.keyListRecycler)
            val click1: Button = recyLayout.findViewById(R.id.click1Btn)
            val click2 :Button = recyLayout.findViewById(R.id.click2Btn)
            val click3 : Button = recyLayout.findViewById(R.id.click3Btn)
            val click4 : Button = recyLayout.findViewById(R.id.click4Btn)
            val click5 : Button = recyLayout.findViewById(R.id.click5Btn)
            val mouseMVup : Button = recyLayout.findViewById(R.id.moveUPBtn)
            val mouseMVDown : Button = recyLayout.findViewById(R.id.moveDownBtn)
            val mouseMVRght : Button = recyLayout.findViewById(R.id.moveRightBtn)
            val mouseMVLeft : Button = recyLayout.findViewById(R.id.moveLeftBtn)
            val os = keyArray.os
            click1.setOnClickListener {
                if (!isShiftActive) {
                    keyArray.mainKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_L, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                } else {
                    keyArray.shiftKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_L, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                }
            }
            click2.setOnClickListener {
                if (!isShiftActive) {
                    keyArray.mainKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_M, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                } else {
                    keyArray.shiftKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_M, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                }
            }
            click3.setOnClickListener {
                if (!isShiftActive) {
                    keyArray.mainKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_R, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                } else {
                    keyArray.shiftKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_R, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                }
            }
            click4.setOnClickListener {
                if (!isShiftActive) {
                    keyArray.mainKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_UP, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                } else {
                    keyArray.shiftKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_UP, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                }
            }
            click5.setOnClickListener {
                if (!isShiftActive) {
                    keyArray.mainKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_UP, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                } else {
                    keyArray.shiftKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_UP, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                }
            }
            mouseMVup.setOnClickListener {
                if (!isShiftActive) {
                    keyArray.mainKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_MV_UP, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                } else {
                    keyArray.shiftKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_MV_UP, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                }
            }
            mouseMVDown.setOnClickListener {
                if (!isShiftActive) {
                    keyArray.mainKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_MV_DOWN, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                } else {
                    keyArray.shiftKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_MV_DOWN, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                }
            }
            mouseMVRght.setOnClickListener {
                if (!isShiftActive) {
                    keyArray.mainKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_MV_RIGHT, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                } else {
                    keyArray.shiftKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_MV_RIGHT, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                }
            }
            mouseMVLeft.setOnClickListener {
                if (!isShiftActive) {
                    keyArray.mainKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_MV_LEFT, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                } else {
                    keyArray.shiftKeyArray[keyCode] = Dictionary.getValueFromKeyName(Dictionary.MOUSE_MV_LEFT, os)
                    showKeyValues()
                    dialog?.dismiss()
                    return@setOnClickListener
                }
            }
            val customeEdit : EditText = recyLayout.findViewById(R.id.CustomeKeyCodeEdit)
            val setCustom : Button = recyLayout.findViewById(R.id.setCustomeKeyCode)
            setCustom.setOnClickListener {
                val keyCode1 = customeEdit.text.toString()
                Toast.makeText(applicationContext, "KeyCode: $keyCode", Toast.LENGTH_SHORT).show()
                if (isShiftActive) {
                    keyArray.shiftKeyArray[keyCode] = keyCode1
                } else {
                    keyArray.mainKeyArray[keyCode] = keyCode1
                }
                showKeyValues()
                dialog?.dismiss()
            }
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
            keyList.add(Dictionary.QOT)
            keyList.add(",")
            keyList.add(".")
            keyList.add("/")
            keyList.add(Dictionary.ESC)
            keyList.add(Dictionary.PRINT)
            keyList.add(Dictionary.BKSPACE)
            keyList.add(Dictionary.TAB)
            keyList.add(Dictionary.CAPS)
            keyList.add(Dictionary.ENTER)
            keyList.add(Dictionary.LSHIFT)
            keyList.add(Dictionary.RSHIFT)
            keyList.add(Dictionary.LCTRL)
            keyList.add(Dictionary.LALT)
            keyList.add(Dictionary.SPACE)
            keyList.add(Dictionary.RALT)
            keyList.add(Dictionary.RCTRL)
            keyList.add(Dictionary.INSERT)
            keyList.add(Dictionary.HOME)
            keyList.add(Dictionary.PAGEUP)
            keyList.add(Dictionary.DELETE)
            keyList.add(Dictionary.END)
            keyList.add(Dictionary.PAGEDOWN)
            keyList.add(Dictionary.UP)
            keyList.add(Dictionary.LEFT)
            keyList.add(Dictionary.DOWN)
            keyList.add(Dictionary.RIGHT)
            recy.layoutManager = GridLayoutManager(applicationContext, 5)
            recy.adapter = KAdapter(keyList)
            setView(recyLayout)
        }
        dialog = builder.create().apply {
            setTitle(KeyEvent.keyCodeToString(keyCode))
            show()
        }
        mainScope.launch {
            waitForKeyAssignmentAsync(dialog).await()
        }
        return true
    }

    private suspend fun waitForKeyAssignmentAsync(dia : AlertDialog) =
        coroutineScope {
            async (Dispatchers.IO) {
                while (assignValueToKeyCode == "null")  {
                    // wait
                }
                val os = keyArray.os
                if (isShiftActive) {
                    keyArray.shiftKeyArray[activeKeyCode] = assignValueToKeyCode
                    val msg = "\nShift + ${KeyEvent.keyCodeToString(activeKeyCode)} ($activeKeyCode) : ${Dictionary.getKeyNameFromValue(assignValueToKeyCode, os)}"
                    textHandler.obtainMessage(0, msg).sendToTarget()
                } else {
                    keyArray.mainKeyArray[activeKeyCode] = assignValueToKeyCode
                    val msg= "\n${KeyEvent.keyCodeToString(activeKeyCode)} ($activeKeyCode) : ${Dictionary.getKeyNameFromValue(assignValueToKeyCode, os)}"
                    textHandler.obtainMessage(0, msg).sendToTarget()
                }
                activeKeyCode = 0
                assignValueToKeyCode = "null"
                dia.dismiss()
            }
        }

    inner class KAdapter(private val kList: ArrayList<String>) : RecyclerView.Adapter<KAdapter.KHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KHolder {
            return KHolder(layoutInflater.inflate(R.layout.key_layout, parent, false))
        }

        override fun getItemCount(): Int = kList.size

        override fun onBindViewHolder(holder: KHolder, position: Int) {
            val button = holder.button
            button.text = kList[position]
            button.setOnClickListener {
                if (activeKeyCode != 0 ) {
                    assignValueToKeyCode = Dictionary.getValueFromKeyName(kList[position], keyArray.os)
                }
            }
        }

        inner class KHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val button : Button = itemView.findViewById(R.id.keyText)
        }
    }

    private fun showKeyValues() {
        var text = ""
        val os = keyArray.os
        for (i in 0 until 400) {
            val got = keyArray.mainKeyArray[i]
            if (got != null) {
                text += "\n${KeyEvent.keyCodeToString(i)} : ${Dictionary.getKeyNameFromValue(got.toString(), os)}"
            }
        }
        for (i in 0 until 400) {
            val got = keyArray.shiftKeyArray[i]
            if (got != null) {
                text += "\nShift + ${KeyEvent.keyCodeToString(i)} : ${Dictionary.getKeyNameFromValue(got.toString(), os)}"
            }
        }
        val shiftKey = keyArray.shiftKey
        if (shiftKey != -1) {
            text += "\nShift Key = ${KeyEvent.keyCodeToString(shiftKey)}"
        }
        binding.keysText.text = text
    }

    private fun loadKeyValues() {
        var keysText1 = ""
        val appDir = applicationContext.getExternalFilesDir("USB_profiles")
        val file = File(appDir, usbProfile!!)
        val objectInputStream = ObjectInputStream(file.inputStream())
        keyArray = objectInputStream.readObject() as KeyArray
        objectInputStream.close()
        val os = keyArray.os
        for (i in 0 until 400) {
            val got = keyArray.mainKeyArray[i]
            if (got != null) {
                keysText1 += "\n${KeyEvent.keyCodeToString(i)} : ${Dictionary.getKeyNameFromValue(got.toString(), os)}"
            }
        }
        for (i in 0 until 400) {
            val got = keyArray.shiftKeyArray[i]
            if (got != null) {
                keysText1 += "\nShift + ${KeyEvent.keyCodeToString(i)} : ${Dictionary.getKeyNameFromValue(got.toString(), os)}"
            }
        }
        val shiftKey = keyArray.shiftKey
        if (shiftKey != -1) {
            keysText1 += "\nShift Key = ${KeyEvent.keyCodeToString(shiftKey)}"
        }
        binding.keysText.text = keysText1
    }

    companion object {
        var usb_OS = Dictionary.OS_WINDOWS
        class KeyArray : Serializable {
            var os = Dictionary.OS_WINDOWS
            var shiftKey = -1
            var mainKeyArray = HashMap<Int, String>()
            var shiftKeyArray = HashMap<Int, String>()
        }
    }
}

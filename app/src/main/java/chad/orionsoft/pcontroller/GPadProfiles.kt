package chad.orionsoft.pcontroller

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chad.orionsoft.pcontroller.databinding.ActivityGpadProfilesBinding
import java.io.File

class GPadProfiles : AppCompatActivity() {

    private lateinit var binding : ActivityGpadProfilesBinding
    private var editing = false
    private lateinit var appDir : File
    private val profList = ArrayList<File>()
    private lateinit var prefs : SharedPreferences
    private lateinit var currentProf: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGpadProfilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        currentProf = prefs.getString(GamepadFullscreen.PROFILE_IN_PREFS, "null")!!
        appDir = File(applicationContext.getExternalFilesDir("Gamepad"), "Profiles")
        editing = intent.getBooleanExtra("edit", false)
        if (editing) {
            binding.addNewProfileBtn.visibility = View.VISIBLE
            binding.addNewProfileBtn.setOnClickListener {
                prefs.edit().putString(GamepadFullscreen.PROFILE_IN_PREFS, GamepadFullscreen.NEW_PROF).apply()
                val i = Intent(applicationContext, GamepadFullscreen::class.java)
                i.putExtra(GamepadFullscreen.START_MAPPING, true)
                startActivity(i)
                finish()
            }
        }

        profList.addAll(appDir.listFiles()!!)

        if (currentProf == "null") {
            val builder = AlertDialog.Builder(this@GPadProfiles).apply {
                setPositiveButton("OK") { _, _ ->
                    startActivity(Intent(applicationContext,GamepadFullscreen::class.java))
                    finish()
                }
                setMessage(resources.getString(R.string.gamepad_welcome_msg))
            }
            val dialog = builder.create()
            dialog.show()
            currentProf = "fps"
            prefs.edit().putString(GamepadFullscreen.PROFILE_IN_PREFS, "fps").apply()
        }
        binding.profileRecycler.layoutManager = LinearLayoutManager(applicationContext)
        binding.profileRecycler.adapter = PAdapter(profList)
        binding.profileRecycler.recycledViewPool.setMaxRecycledViews(0,0)
    }

    inner class PAdapter(private val pList: ArrayList<File>) : RecyclerView.Adapter<PAdapter.PHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PHolder {
            return PHolder(layoutInflater.inflate(R.layout.profile_item_view, parent, false))
        }

        override fun getItemCount(): Int = pList.size

        override fun onBindViewHolder(holder: PHolder, position: Int) {
            val nameText = holder.nameText
            val editImage = holder.editImage
            val setImage = holder.setImage
            nameText.text = pList[position].name
            if (editing) {
                editImage.visibility = View.VISIBLE
                editImage.setOnClickListener {
                    currentProf = pList[position].name
                    prefs.edit().putString(GamepadFullscreen.PROFILE_IN_PREFS, currentProf).apply()
                    val i = Intent(applicationContext, GamepadFullscreen::class.java)
                    i.putExtra(GamepadFullscreen.START_MAPPING, true)
                    startActivity(i)
                    finish()
                }
            } else {
                editImage.visibility = View.GONE
                nameText.setOnClickListener {
                    currentProf = pList[position].name
                    prefs.edit().putString(GamepadFullscreen.PROFILE_IN_PREFS, currentProf).apply()
                    notifyDataSetChanged()
                }
            }
            if (pList[position].name == currentProf) {
                setImage.visibility = View.VISIBLE
            } else {
                setImage.visibility = View.INVISIBLE
            }
        }

        inner class PHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameText : TextView = itemView.findViewById(R.id.profileText)
            val editImage: ImageView = itemView.findViewById(R.id.profile_edit)
            val setImage : ImageView = itemView.findViewById(R.id.profile_set)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext,GamepadFullscreen::class.java))
        finish()
    }
}

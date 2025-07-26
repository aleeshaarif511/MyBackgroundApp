package np.com.bimalkafle.mybackgroundapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class VoiceInsulinActivity : AppCompatActivity() {

    private lateinit var tvVoiceResult: TextView
    private lateinit var btnStartVoice: Button
    private lateinit var btnViewEntries: Button
    private lateinit var dbHelper: InsulinDbHelper

    private val voiceRequestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_input)

        tvVoiceResult = findViewById(R.id.tv_voice_result)
        btnStartVoice = findViewById(R.id.btn_start_voice)
        btnViewEntries = findViewById(R.id.btn_view_entries)

        dbHelper = InsulinDbHelper(this)

        btnStartVoice.setOnClickListener {
            startVoiceRecognition()
        }

        btnViewEntries.setOnClickListener {
            startActivity(Intent(this, InsulinEntriesActivity::class.java))
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say: 'Hey IMS, I am going to take 3 units of insulin.'")
        startActivityForResult(intent, voiceRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == voiceRequestCode && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""
            tvVoiceResult.text = spokenText // <-- Immediately show recognized text on screen

            if (!spokenText.lowercase().contains("hey ims")) {
                Toast.makeText(this, "❌ Please say 'Hey IMS' at the start.", Toast.LENGTH_SHORT).show()
                return
            }

            val regex = Regex("(\\d+) units", RegexOption.IGNORE_CASE)
            val match = regex.find(spokenText)

            if (match != null) {
                val units = match.groupValues[1].toInt()
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                dbHelper.insertEntry(units, timestamp)
                Toast.makeText(this, "✅ Saved to DB: $units units", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "❌ Could not understand units.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

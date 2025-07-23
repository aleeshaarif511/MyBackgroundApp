package np.com.bimalkafle.mybackgroundapp

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var btnOpenSettings: Button
    private lateinit var btnEnableBt: Button
    private lateinit var btnVoiceInput: Button
    private lateinit var btnViewEntries: Button
    private lateinit var tvInfo: TextView

    private lateinit var powerManager: PowerManager
    private var bluetoothAdapter: BluetoothAdapter? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothDevice.ACTION_FOUND == intent?.action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device?.name ?: ""
                if (deviceName.startsWith("IMS")) {
                    tvInfo.text = "✅ Found IMS device: $deviceName\nGoing to home..."
                    bluetoothAdapter?.cancelDiscovery()
                    try {
                        unregisterReceiver(this)
                    } catch (_: Exception) {}
                    goToHome()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenSettings = findViewById(R.id.btn_open_settings)
        btnEnableBt = findViewById(R.id.btn_enable_bt)
        btnVoiceInput = findViewById(R.id.btn_voice_input)
        btnViewEntries = findViewById(R.id.btn_view_entries)
        tvInfo = findViewById(R.id.tv_info)

        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        btnOpenSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
        }

        btnEnableBt.setOnClickListener {
            handleBluetoothAndLocationFlow()
        }

        btnVoiceInput.setOnClickListener {
            startVoiceInput()
        }

        btnViewEntries.setOnClickListener {
            startActivity(Intent(this, InsulinEntriesActivity::class.java))
        }

        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()

        if (isIgnoringBatteryOptimizations()) {
            if (bluetoothAdapter?.isEnabled == true && isLocationEnabled()) {
                if (checkAllPermissionsGranted()) {
                    startDiscovery()
                } else {
                    requestAllPermissions()
                }
            }
        }
    }

    private fun updateUI() {
        if (!isIgnoringBatteryOptimizations()) {
            btnOpenSettings.visibility = View.VISIBLE
            btnEnableBt.visibility = View.GONE
            btnVoiceInput.visibility = View.GONE
            btnViewEntries.visibility = View.GONE
            tvInfo.text = "Please allow background permission first."
        } else {
            btnOpenSettings.visibility = View.GONE
            btnEnableBt.visibility = View.VISIBLE
            btnVoiceInput.visibility = View.VISIBLE
            btnViewEntries.visibility = View.VISIBLE
            tvInfo.text = "✅ Background granted.\nNow enable Bluetooth and Location to continue."
        }
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun handleBluetoothAndLocationFlow() {
        if (bluetoothAdapter == null) {
            tvInfo.text = "❌ Bluetooth not supported."
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            val intentBt = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intentBt)
            return
        }

        if (!isLocationEnabled()) {
            val intentLoc = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intentLoc)
            return
        }

        requestAllPermissions()
    }

    private fun requestAllPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            2001
        )
    }

    private fun startDiscovery() {
        btnEnableBt.visibility = View.GONE

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
        try {
            bluetoothAdapter?.startDiscovery()
            tvInfo.text = "🔍 Scanning for IMS devices..."
        } catch (e: SecurityException) {
            e.printStackTrace()
            tvInfo.text = "❌ Cannot start discovery: permission missing."
        }
    }

    private fun checkAllPermissionsGranted(): Boolean {
        val locationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val bluetoothGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else true

        return locationGranted && bluetoothGranted
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say: 'Hey IMS, I am going to take 3 units of insulin.'")

        try {
            startActivityForResult(intent, 2002)
        } catch (e: Exception) {
            tvInfo.text = "❌ Voice input not supported on this device."
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2002 && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""

            if (spokenText.lowercase().contains("hey ims") && spokenText.lowercase().contains("insulin")) {
                val regex = Regex("take (\\d+) units", RegexOption.IGNORE_CASE)
                val match = regex.find(spokenText)

                if (match != null) {
                    val units = match.groupValues[1].toInt()
                    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                    val dbHelper = InsulinDbHelper(this)
                    dbHelper.insertEntry(units, timestamp)

                    tvInfo.text = "✅ Insulin entry saved: $units units at $timestamp"
                } else {
                    tvInfo.text = "❌ Could not understand units. Example: 'Hey IMS, I am going to take 3 units of insulin.'"
                }
            } else {
                tvInfo.text = "❌ Command not recognized. Please say: 'Hey IMS, I am going to take 3 units of insulin.'"
            }
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2001) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startDiscovery()
            } else {
                tvInfo.text = "❌ Permissions denied. Cannot scan devices."
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (_: Exception) {}
    }
}

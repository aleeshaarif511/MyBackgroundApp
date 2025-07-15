package np.com.bimalkafle.mybackgroundapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnOpenSettings: Button
    private lateinit var btnEnableBt: Button
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
            tvInfo.text = "Please allow background permission first."
        } else {
            btnOpenSettings.visibility = View.GONE
            btnEnableBt.visibility = View.VISIBLE
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
        btnEnableBt.visibility = View.GONE // 👈 Hide button when starting discovery

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

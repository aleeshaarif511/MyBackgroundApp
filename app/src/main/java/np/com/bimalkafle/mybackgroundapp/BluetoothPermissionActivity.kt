package np.com.bimalkafle.mybackgroundapp

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class BluetoothPermissionActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null

    private val bluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                goToHome()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        val button = Button(this)
        button.text = "Enable Bluetooth"

        button.setOnClickListener {
            if (bluetoothAdapter == null) {
                button.text = "Bluetooth not supported"
            } else {
                if (!bluetoothAdapter!!.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    bluetoothLauncher.launch(enableBtIntent)
                } else {
                    goToHome()
                }
            }
        }

        setContentView(button)
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}

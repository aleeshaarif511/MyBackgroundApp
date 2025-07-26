package np.com.bimalkafle.mybackgroundapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = "🎉 Connected and Welcome to Home Screen!"
        tv.textSize = 24f
        tv.gravity = android.view.Gravity.CENTER

        setContentView(tv)
    }
}

package np.com.bimalkafle.mybackgroundapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class InsulinEntriesActivity : AppCompatActivity() {

    private lateinit var dbHelper: InsulinDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insulin_entries)

        val listView = findViewById<ListView>(R.id.listView_entries)
        dbHelper = InsulinDbHelper(this)

        val entries = dbHelper.getAllEntries()
        val formattedEntries = entries.map { "Units: ${it.first}\nTime: ${it.second}" }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, formattedEntries)
        listView.adapter = adapter
    }
}

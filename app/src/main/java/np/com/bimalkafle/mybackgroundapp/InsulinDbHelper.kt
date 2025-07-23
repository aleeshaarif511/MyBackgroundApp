package np.com.bimalkafle.mybackgroundapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class InsulinDbHelper(context: Context) : SQLiteOpenHelper(context, "insulin_db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE insulin_entries (id INTEGER PRIMARY KEY AUTOINCREMENT, units INTEGER, timestamp TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS insulin_entries")
        onCreate(db)
    }

    fun insertEntry(units: Int, timestamp: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("units", units)
            put("timestamp", timestamp)
        }
        db.insert("insulin_entries", null, values)
        db.close()
    }

    fun getAllEntries(): List<Pair<Int, String>> {
        val entries = mutableListOf<Pair<Int, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT units, timestamp FROM insulin_entries", null)
        if (cursor.moveToFirst()) {
            do {
                val units = cursor.getInt(0)
                val timestamp = cursor.getString(1)
                entries.add(Pair(units, timestamp))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return entries
    }
}

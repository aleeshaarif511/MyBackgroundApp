package np.com.bimalkafle.mybackgroundapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GlucoseDbHelper(context: Context) : SQLiteOpenHelper(context, "glucose.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE glucose_entries (id INTEGER PRIMARY KEY AUTOINCREMENT, level INTEGER, timestamp TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS glucose_entries")
        onCreate(db)
    }

    fun insertLevel(level: Int, timestamp: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("level", level)
            put("timestamp", timestamp)
        }
        db.insert("glucose_entries", null, values)
    }

    fun getLatestLevel(): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT level FROM glucose_entries ORDER BY id DESC LIMIT 1", null)
        val level = if (cursor.moveToFirst()) cursor.getInt(0) else null
        cursor.close()
        return level
    }
}

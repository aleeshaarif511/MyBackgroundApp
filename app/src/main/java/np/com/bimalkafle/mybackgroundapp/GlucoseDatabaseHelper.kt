package np.com.bimalkafle.mybackgroundapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GlucoseDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "glucose.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE glucose(id INTEGER PRIMARY KEY, level INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS glucose")
        onCreate(db)
    }

    fun insertGlucoseLevel(level: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("level", level)
        }
        db.insert("glucose", null, values)
    }

    fun getLatestGlucoseLevel(): Int? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT level FROM glucose ORDER BY id DESC LIMIT 1", null)
        return if (cursor.moveToFirst()) {
            val level = cursor.getInt(0)
            cursor.close()
            level
        } else {
            cursor.close()
            null
        }
    }
}

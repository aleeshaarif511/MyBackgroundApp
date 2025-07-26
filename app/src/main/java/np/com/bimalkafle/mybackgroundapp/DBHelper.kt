package np.com.bimalkafle.mybackgroundapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "InsulinDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE Insulin(id INTEGER PRIMARY KEY AUTOINCREMENT, units INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Insulin")
        onCreate(db)
    }

    fun insertInsulin(units: Int): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("units", units)
        val result = db.insert("Insulin", null, contentValues)
        db.close()
        return result != -1L
    }
    fun getAllInsulin(): List<Pair<Int, Int>> {
        val insulinList = mutableListOf<Pair<Int, Int>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Insulin", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val units = cursor.getInt(cursor.getColumnIndexOrThrow("units"))
                insulinList.add(Pair(id, units))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return insulinList
    }

}

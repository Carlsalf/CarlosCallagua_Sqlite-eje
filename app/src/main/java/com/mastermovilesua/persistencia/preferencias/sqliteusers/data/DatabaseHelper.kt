package com.mastermovilesua.persistencia.preferencias.sqliteusers.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USUARIOS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT NOT NULL UNIQUE,
                $COL_PASSWORD TEXT NOT NULL,
                $COL_FULLNAME TEXT,
                $COL_EMAIL TEXT
            )
        """.trimIndent()

        db.execSQL(createTable)

        // Usuario demo inicial
        val cv = ContentValues().apply {
            put(COL_USERNAME, "lolo")
            put(COL_PASSWORD, "1234")
            put(COL_FULLNAME, "Lolo Martinez")
            put(COL_EMAIL, "lolo@mail.com")
        }
        db.insert(TABLE_USUARIOS, null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Para este ejercicio: recrear
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        onCreate(db)
    }

    // ============ LOGIN / LECTURA ============

    fun checkLogin(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
                SELECT 1 FROM $TABLE_USUARIOS
                WHERE $COL_USERNAME=? AND $COL_PASSWORD=?
                LIMIT 1
            """.trimIndent(),
            arrayOf(username, password)
        )

        val ok = cursor.use { it.moveToFirst() }
        db.close()
        return ok
    }

    fun getUserByCredentials(username: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
                SELECT $COL_ID, $COL_USERNAME, $COL_PASSWORD, $COL_FULLNAME, $COL_EMAIL
                FROM $TABLE_USUARIOS
                WHERE $COL_USERNAME=? AND $COL_PASSWORD=?
                LIMIT 1
            """.trimIndent(),
            arrayOf(username, password)
        )

        val user = cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                    username = it.getString(it.getColumnIndexOrThrow(COL_USERNAME)),
                    password = it.getString(it.getColumnIndexOrThrow(COL_PASSWORD)),
                    fullName = it.getString(it.getColumnIndexOrThrow(COL_FULLNAME)) ?: "",
                    email = it.getString(it.getColumnIndexOrThrow(COL_EMAIL)) ?: ""
                )
            } else null
        }

        db.close()
        return user
    }

    // ============ CRUD ============

    fun insertUser(user: User): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_USERNAME, user.username)
            put(COL_PASSWORD, user.password)
            put(COL_FULLNAME, user.fullName)
            put(COL_EMAIL, user.email)
        }
        val id = db.insert(TABLE_USUARIOS, null, cv)
        db.close()
        return id
    }

    fun updateUser(user: User): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_PASSWORD, user.password)
            put(COL_FULLNAME, user.fullName)
            put(COL_EMAIL, user.email)
        }
        val rows = db.update(
            TABLE_USUARIOS,
            cv,
            "$COL_ID=?",
            arrayOf(user.id.toString())
        )
        db.close()
        return rows
    }

    fun deleteUserById(id: Long): Int {
        val db = writableDatabase
        val rows = db.delete(
            TABLE_USUARIOS,
            "$COL_ID=?",
            arrayOf(id.toString())
        )
        db.close()
        return rows
    }

    fun getAllUsers(): List<User> {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
                SELECT $COL_ID, $COL_USERNAME, $COL_PASSWORD, $COL_FULLNAME, $COL_EMAIL
                FROM $TABLE_USUARIOS
                ORDER BY $COL_ID DESC
            """.trimIndent(),
            null
        )

        val list = mutableListOf<User>()
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    User(
                        id = it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(COL_USERNAME)),
                        password = it.getString(it.getColumnIndexOrThrow(COL_PASSWORD)),
                        fullName = it.getString(it.getColumnIndexOrThrow(COL_FULLNAME)) ?: "",
                        email = it.getString(it.getColumnIndexOrThrow(COL_EMAIL)) ?: ""
                    )
                )
            }
        }

        db.close()
        return list
    }

    // ============ DEBUG ============

    fun logDbInfo() {
        val db = readableDatabase
        Log.d("DB_CHECK", "DB path: ${db.path}")

        val cTables: Cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name",
            null
        )
        cTables.use {
            while (it.moveToNext()) {
                Log.d("DB_CHECK", "Tabla: ${it.getString(0)}")
            }
        }

        val cCount = db.rawQuery("SELECT COUNT(*) FROM $TABLE_USUARIOS", null)
        cCount.use {
            if (it.moveToFirst()) {
                Log.d("DB_CHECK", "Filas en Usuarios: ${it.getInt(0)}")
            }
        }

        db.close()
    }

    companion object {
        private const val DATABASE_NAME = "usuarios.db"

        // IMPORTANTÍSIMO: subir versión para que se ejecute onUpgrade y se regenere tabla
        private const val DATABASE_VERSION = 2

        const val TABLE_USUARIOS = "Usuarios"
        const val COL_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_PASSWORD = "password"
        const val COL_FULLNAME = "full_name"
        const val COL_EMAIL = "email"

        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

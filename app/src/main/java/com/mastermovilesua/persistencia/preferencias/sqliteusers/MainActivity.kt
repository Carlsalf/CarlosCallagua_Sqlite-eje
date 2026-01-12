package com.mastermovilesua.persistencia.preferencias.sqliteusers

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mastermovilesua.persistencia.preferencias.sqliteusers.data.DatabaseHelper

class MainActivity : AppCompatActivity() {

    private lateinit var etUser: EditText
    private lateinit var etPass: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnClose: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etUser = findViewById(R.id.etUser)
        etPass = findViewById(R.id.etPass)
        btnLogin = findViewById(R.id.btnLogin)
        btnClose = findViewById(R.id.btnClose)

        // Debug opcional
        DatabaseHelper.getInstance(this).logDbInfo()

        btnLogin.setOnClickListener {
            val username = etUser.text.toString().trim()
            val password = etPass.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Rellena usuario y password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = DatabaseHelper.getInstance(this)

            if (db.checkLogin(username, password)) {
                val user = db.getUserByCredentials(username, password)

                val intent = Intent(this, UserDataActivity::class.java).apply {
                    putExtra("username", user?.username ?: username)
                    putExtra("fullname", user?.fullName ?: "")
                    putExtra("email", user?.email ?: "")
                }
                startActivity(intent)

                // Opcional: si no quieres volver al login con BACK
                // finish()
            } else {
                Toast.makeText(this, "Error usuario/password incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        btnClose.setOnClickListener { finish() }
    }
}

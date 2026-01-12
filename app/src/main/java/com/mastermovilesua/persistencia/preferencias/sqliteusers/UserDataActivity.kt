package com.mastermovilesua.persistencia.preferencias.sqliteusers

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class UserDataActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_data)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbarUser)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "User Data"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvWelcome = findViewById(R.id.tvWelcome)
        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail)
        btnBack = findViewById(R.id.btnBackUserData)

        val username = intent.getStringExtra("username") ?: ""
        val fullname = intent.getStringExtra("fullname") ?: ""
        val email = intent.getStringExtra("email") ?: ""

        tvWelcome.text = "Welcome\n$fullname"
        tvUsername.text = "User Name   $username"
        tvEmail.text = "E-mail   $email"

        btnBack.setOnClickListener { finish() }
    }

    // 1) Inflar menú (los 3 puntitos)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // 2) Manejar acciones del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            android.R.id.home -> { // Flecha "Up"
                finish()
                true
            }

            R.id.action_create_backup -> {
                startActivity(Intent(this, BackupActivity::class.java))
                true
            }

            R.id.action_restore_backup -> {
                startActivity(Intent(this, RestoreActivity::class.java))
                true
            }

            R.id.action_manage_users -> {
                startActivity(Intent(this, ManageUsersActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}

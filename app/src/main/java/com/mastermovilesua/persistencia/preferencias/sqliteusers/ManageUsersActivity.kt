package com.mastermovilesua.persistencia.preferencias.sqliteusers

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mastermovilesua.persistencia.preferencias.sqliteusers.data.DatabaseHelper
import com.mastermovilesua.persistencia.preferencias.sqliteusers.data.User

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnBack: Button
    private lateinit var listView: ListView

    private lateinit var db: DatabaseHelper
    private var users: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_users)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper.getInstance(this)

        etUsername = findViewById(R.id.etNewUsername)
        etPassword = findViewById(R.id.etNewPassword)
        etFullName = findViewById(R.id.etNewFullName)
        etEmail = findViewById(R.id.etNewEmail)
        btnAdd = findViewById(R.id.btnAddUser)
        btnBack = findViewById(R.id.btnBack)
        listView = findViewById(R.id.listUsers)

        refreshList()

        btnAdd.setOnClickListener {
            val u = etUsername.text.toString().trim()
            val p = etPassword.text.toString()
            val f = etFullName.text.toString().trim()
            val e = etEmail.text.toString().trim()

            if (u.isBlank() || p.isBlank()) {
                Toast.makeText(this, "Username y Password son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User(
                id = 0L,
                username = u,
                password = p,
                fullName = f,
                email = e
            )

            val id = db.insertUser(newUser)
            if (id == -1L) {
                Toast.makeText(this, "No se pudo insertar (¿username duplicado?)", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Usuario creado (id=$id)", Toast.LENGTH_SHORT).show()
                clearForm()
                refreshList()
            }
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val user = users[position]
            confirmDelete(user)
            true
        }

        btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun confirmDelete(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar usuario")
            .setMessage("¿Eliminar a '${user.username}'?\n\nEsto no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                val rows = db.deleteUserById(user.id)
                if (rows > 0) {
                    Toast.makeText(this, "Borrado: ${user.username}", Toast.LENGTH_SHORT).show()
                    refreshList()
                } else {
                    Toast.makeText(this, "No se pudo borrar", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun refreshList() {
        users = db.getAllUsers()
        val items = users.map { u ->
            "${u.id} - ${u.username} | ${u.fullName} | ${u.email}"
        }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    }

    private fun clearForm() {
        etUsername.setText("")
        etPassword.setText("")
        etFullName.setText("")
        etEmail.setText("")
        etUsername.requestFocus()
    }
}

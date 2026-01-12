package com.mastermovilesua.persistencia.preferencias.sqliteusers

import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mastermovilesua.persistencia.preferencias.sqliteusers.data.DatabaseHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupActivity : AppCompatActivity() {

    private lateinit var tvInfo: TextView
    private lateinit var btnCreate: Button
    private lateinit var lvBackups: ListView

    private val dbName = "usuarios.db"
    private val backupDirName = "backups"

    private var backupFiles: List<File> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)

        tvInfo = findViewById(R.id.tvInfo)
        btnCreate = findViewById(R.id.btnCreateBackup)
        lvBackups = findViewById(R.id.lvBackups)

        btnCreate.setOnClickListener {
            try {
                val backupFile = createBackup()
                Toast.makeText(this, "Backup creado: ${backupFile.name}", Toast.LENGTH_LONG).show()
                refreshList()
            } catch (e: Exception) {
                Toast.makeText(this, "Error creando backup: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        lvBackups.setOnItemLongClickListener { _, _, position, _ ->
            val file = backupFiles[position]
            confirmDelete(file)
            true
        }

        showPathInfo()
        refreshList()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
        showPathInfo()
    }

    private fun showPathInfo() {
        val base = getExternalFilesDir(null) ?: filesDir
        val backupsDir = File(base, backupDirName)
        tvInfo.text = "Backups en:\n${backupsDir.absolutePath}"
    }

    private fun refreshList() {
        backupFiles = listBackupFiles()
        val names = backupFiles.map { it.name }
        lvBackups.adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            names
        )
    }

    private fun listBackupFiles(): List<File> {
        val base = getExternalFilesDir(null) ?: filesDir
        val backupsDir = File(base, backupDirName)
        if (!backupsDir.exists()) backupsDir.mkdirs()

        return backupsDir
            .listFiles { f -> f.isFile && f.name.endsWith(".db", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    private fun createBackup(): File {
        // Cierra DB para evitar copiar a mitad
        DatabaseHelper.getInstance(this).close()

        val dbFile = getDatabasePath(dbName)
        if (!dbFile.exists()) {
            throw IllegalStateException("No existe la BD: ${dbFile.absolutePath}")
        }

        val base = getExternalFilesDir(null) ?: filesDir
        val backupsDir = File(base, backupDirName)
        if (!backupsDir.exists()) backupsDir.mkdirs()

        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupFile = File(backupsDir, "usuarios_backup_$ts.db")

        copyFile(dbFile, backupFile)
        return backupFile
    }

    private fun confirmDelete(file: File) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar backup")
            .setMessage("¿Eliminar este archivo?\n\n${file.name}")
            .setPositiveButton("Eliminar") { _, _ ->
                val ok = file.delete()
                if (ok) {
                    Toast.makeText(this, "Backup eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show()
                }
                refreshList()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun copyFile(from: File, to: File) {
        FileInputStream(from).use { input ->
            FileOutputStream(to).use { output ->
                input.copyTo(output)
            }
        }
    }
}

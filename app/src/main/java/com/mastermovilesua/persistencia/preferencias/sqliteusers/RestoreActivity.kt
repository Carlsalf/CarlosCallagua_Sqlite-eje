package com.mastermovilesua.persistencia.preferencias.sqliteusers

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mastermovilesua.persistencia.preferencias.sqliteusers.data.DatabaseHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class RestoreActivity : AppCompatActivity() {

    private lateinit var tvInfo: TextView
    private lateinit var lvBackups: ListView

    private val dbName = "usuarios.db"
    private val backupDirName = "backups"

    private var backupFiles: List<File> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restore)

        tvInfo = findViewById(R.id.tvInfoRestore)
        lvBackups = findViewById(R.id.lvBackupsRestore)

        showPathInfo()
        refreshList()

        lvBackups.setOnItemClickListener { _, _, position, _ ->
            val file = backupFiles[position]
            confirmRestore(file)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
        showPathInfo()
    }

    private fun showPathInfo() {
        val base = getExternalFilesDir(null) ?: filesDir
        val backupsDir = File(base, backupDirName)
        tvInfo.text = "Selecciona un backup para restaurar:\n${backupsDir.absolutePath}"
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

    private fun confirmRestore(file: File) {
        AlertDialog.Builder(this)
            .setTitle("Restaurar backup")
            .setMessage("¿Restaurar este backup?\n\n${file.name}\n\nEsto reemplazará la BD actual.")
            .setPositiveButton("Restaurar") { _, _ ->
                try {
                    restoreBackup(file)
                    Toast.makeText(this, "Backup restaurado: ${file.name}", Toast.LENGTH_LONG).show()
                    restartApp()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error restaurando: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun restoreBackup(backupFile: File) {
        DatabaseHelper.getInstance(this).close()

        val dbFile = getDatabasePath(dbName)

        // Limpia WAL/SHM si existieran
        File(dbFile.absolutePath + "-wal").delete()
        File(dbFile.absolutePath + "-shm").delete()

        dbFile.parentFile?.let { if (!it.exists()) it.mkdirs() }

        copyFile(backupFile, dbFile)
    }

    private fun copyFile(from: File, to: File) {
        FileInputStream(from).use { input ->
            FileOutputStream(to).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        finishAffinity()
    }
}

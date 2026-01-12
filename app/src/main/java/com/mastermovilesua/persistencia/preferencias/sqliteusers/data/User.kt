package com.mastermovilesua.persistencia.preferencias.sqliteusers.data

data class User(
    val id: Long,
    val username: String,
    val password: String,
    val fullName: String,
    val email: String
)

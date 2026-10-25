package com.example.costostock


data class User(
    val correo: String,
    val password: String,
    val role: String,
    val nombre: String = "",
    val telefono: String = ""
)

object UserRepository {

    private val usersList = mutableListOf<User>(
        User("admin", "1234", "ADMINISTRADOR")
    )

    fun registerNewUser(
        correo: String,
        password: String,
        role: String,
        nombre: String,
        telefono: String
    ): User? {
        val userExists = usersList.any { it.correo.lowercase() == correo.lowercase() }
        if (userExists) return null

        val newUser = User(
            correo = correo.trim(),
            password = password,
            role = role,
            nombre = nombre.trim(),
            telefono = telefono.trim()
        )
        usersList.add(newUser)
        return newUser
    }

    fun validateLogin(correo: String, password: String): User? {
        return usersList.find {
            it.correo.lowercase() == correo.lowercase() && it.password == password
        }
    }
}
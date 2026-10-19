package com.example.costostock


data class User(
    val correo: String,
    val password: String,
    val role: String
)

object UserRepository {

    private val usersList = mutableListOf<User>(
        User("admin", "1234", "ADMINISTRADOR")
    )

    fun registerNewUser(correo: String, password: String, role: String): Boolean {
        val userExists = usersList.any { it.correo.lowercase() == correo.lowercase() }

        if (userExists) {
            return false
        } else {
            usersList.add(User(correo, password, role))
            return true
        }
    }

    fun validateLogin(correo: String, password: String): User? {
        return usersList.find {
            it.correo.lowercase() == correo.lowercase() && it.password == password
        }
    }
}
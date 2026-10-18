package com.example.costostock


data class User(
    val username: String,
    val password: String,
    val role: String
)

object UserRepository {

    private val usersList = mutableListOf<User>(
        User("admin", "1234", "ADMINISTRADOR")
    )

    fun registerNewUser(username: String, password: String, role: String): Boolean {
        val userExists = usersList.any { it.username.lowercase() == username.lowercase() }

        if (userExists) {
            return false
        } else {
            usersList.add(User(username, password, role))
            return true
        }
    }

    fun validateLogin(username: String, password: String): User? {
        return usersList.find {
            it.username.lowercase() == username.lowercase() && it.password == password
        }
    }
}
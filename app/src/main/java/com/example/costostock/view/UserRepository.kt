package com.example.costostock

import androidx.compose.runtime.mutableStateListOf
enum class AccountStatus { APROBADA, PENDIENTE, RECHAZADA }

data class User(
    val correo: String,
    val password: String,
    val role: String,
    val nombre: String = "",
    val telefono: String = "",
    val status: AccountStatus = AccountStatus.APROBADA,
    val requestedAtMillis: Long = 0L
)

const val DEMO_ADMIN_EMAIL = "administrador@costostock.cl"
const val DEMO_ADMIN_PASSWORD = "admin123"

object UserRepository {
    private val usersList = mutableStateListOf(
        User("admin", "1234", "ADMINISTRADOR"),
        User(
            correo = DEMO_ADMIN_EMAIL,
            password = DEMO_ADMIN_PASSWORD,
            role = "ADMINISTRADOR",
            nombre = "Administrador Costo Stock",
            telefono = "+56 9 1234 5678"
        )
    )

    fun requiresApproval(role: String): Boolean =
        role == "VENDEDOR" || role == "ADMINISTRADOR"

    fun findByEmail(correo: String): User? =
        usersList.find { it.correo.equals(correo.trim(), ignoreCase = true) }

    fun registerNewUser(
        correo: String,
        password: String,
        role: String,
        nombre: String,
        telefono: String
    ): User? {
        val existingIndex = usersList.indexOfFirst { it.correo.equals(correo.trim(), ignoreCase = true) }
        if (existingIndex >= 0 && usersList[existingIndex].status != AccountStatus.RECHAZADA) return null

        val needsApproval = requiresApproval(role)
        val newUser = User(
            correo = correo.trim(),
            password = password,
            role = role,
            nombre = nombre.trim(),
            telefono = telefono.trim(),
            status = if (needsApproval) AccountStatus.PENDIENTE else AccountStatus.APROBADA,
            requestedAtMillis = if (needsApproval) System.currentTimeMillis() else 0L
        )

        if (existingIndex >= 0) {
            usersList[existingIndex] = newUser
        } else {
            usersList.add(newUser)
        }
        return newUser
    }

    fun validateLogin(correo: String, password: String): User? {
        return usersList.find {
            it.correo.lowercase() == correo.trim().lowercase() && it.password == password
        }
    }

    fun updateProfile(correo: String, nombre: String, telefono: String): User? {
        val index = usersList.indexOfFirst { it.correo.lowercase() == correo.lowercase() }
        if (index < 0) return null

        val updated = usersList[index].copy(
            nombre = nombre.trim(),
            telefono = telefono.trim()
        )
        usersList[index] = updated
        return updated
    }

    fun approvalRequests(): List<User> =
        usersList
            .filter { it.requestedAtMillis > 0L && requiresApproval(it.role) }
            .sortedByDescending { it.requestedAtMillis }

    fun pendingRequestsCount(): Int =
        usersList.count { it.status == AccountStatus.PENDIENTE }

    fun approveRequest(correo: String): Boolean = setStatus(correo, AccountStatus.APROBADA)

    fun rejectRequest(correo: String): Boolean = setStatus(correo, AccountStatus.RECHAZADA)

    private fun setStatus(correo: String, status: AccountStatus): Boolean {
        val index = usersList.indexOfFirst { it.correo.equals(correo, ignoreCase = true) }
        if (index < 0) return false
        usersList[index] = usersList[index].copy(status = status)
        return true
    }
}

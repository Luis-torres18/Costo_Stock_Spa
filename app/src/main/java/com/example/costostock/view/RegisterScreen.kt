package com.example.costostock.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.costostock.AccountStatus
import com.example.costostock.User
import com.example.costostock.UserRepository

const val NAME_MAX_LENGTH = 40

fun validateName(name: String): String? {
    val clean = name.trim()
    return when {
        clean.length < 2 ->
            "El nombre debe tener al menos 2 letras"
        clean.length > NAME_MAX_LENGTH ->
            "El nombre puede tener como máximo $NAME_MAX_LENGTH caracteres"
        !Regex("""^\p{L}+( \p{L}+)*$""").matches(clean) ->
            "El nombre solo puede contener letras y espacios"
        else -> null
    }
}

fun validatePassword(password: String): String? = when {
    password.length < 8 ->
        "La contraseña debe tener al menos 8 caracteres"
    password.none { it.isLowerCase() } ->
        "La contraseña debe incluir al menos una letra minúscula"
    password.none { it.isUpperCase() } ->
        "La contraseña debe incluir al menos una letra mayúscula"
    password.none { !it.isLetterOrDigit() && !it.isWhitespace() } ->
        "La contraseña debe incluir al menos un carácter especial (por ejemplo ! @ # -)"
    else -> null
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: (User) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    val roles = listOf("CLIENTE", "VENDEDOR", "ADMINISTRADOR")
    var selectedRole by remember { mutableStateOf(roles[0]) }
    var pendingUser by remember { mutableStateOf<User?>(null) }
    val focusManager = LocalFocusManager.current

    fun attemptRegistration() {
        when {
            username.isBlank() || correo.isBlank() || numero.isBlank() || password.isEmpty() ->
                errorMessage = "Por favor completa todos los campos"

            validateName(username) != null ->
                errorMessage = validateName(username)

            !android.util.Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() ->
                errorMessage = "Ingresa un correo válido"

            numero.count { it.isDigit() } < 8 ->
                errorMessage = "Ingresa un teléfono válido"

            validatePassword(password) != null ->
                errorMessage = validatePassword(password)

            password != confirmPassword ->
                errorMessage = "Las contraseñas no coinciden"

            UserRepository.findByEmail(correo)?.status == AccountStatus.PENDIENTE ->
                errorMessage = "Ya hay una solicitud pendiente con este correo"

            else -> {
                val newUser = UserRepository.registerNewUser(
                    correo = correo,
                    password = password,
                    role = selectedRole,
                    nombre = username,
                    telefono = numero
                )

                if (newUser != null) {
                    focusManager.clearFocus()
                    if (newUser.status == AccountStatus.PENDIENTE) {
                        // Vendedor: no entra todavía, se muestra el aviso de solicitud enviada
                        pendingUser = newUser
                    } else {
                        onRegisterSuccess(newUser)
                    }
                } else {
                    errorMessage = "El correo electronico ya esta en uso"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Crear Nueva Cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Selecciona el tipo de cuenta:", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            roles.forEach { role ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .selectable(
                            selected = (role == selectedRole),
                            onClick = { selectedRole = role },
                            role = Role.RadioButton
                        )
                        .padding(4.dp)
                ) {
                    RadioButton(
                        selected = (role == selectedRole),
                        onClick = null
                    )
                    Text(
                        text = role.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        if (UserRepository.requiresApproval(selectedRole)) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Las cuentas de ${selectedRole.lowercase()} deben ser aprobadas por un administrador antes de poder usarse.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                // Solo letras y espacios, con un máximo de caracteres
                if (it.length <= NAME_MAX_LENGTH && it.all { c -> c.isLetter() || c == ' ' }) {
                    username = it
                }
                errorMessage = null
            },
            label = { Text("Nombre y Apellido") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                errorMessage = null
            },
            label = { Text("Correo Electronico") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = numero,
            onValueChange = {
                numero = it
                errorMessage = null
            },
            label = { Text("Numero de Telefono") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Contraseña") },
            supportingText = {
                Text("Mínimo 8 caracteres, con mayúscula, minúscula y un carácter especial")
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
            },
            label = { Text("Confirmar Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { attemptRegistration() }
            ),
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (!errorMessage.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { attemptRegistration() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarse")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { onNavigateToLogin() }) {
            Text("¿Ya tienes cuenta? Inicia Sesion Aqui")
        }
    }

    pendingUser?.let { user ->
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Solicitud enviada") },
            text = {
                Text(
                    "Tu cuenta de ${user.role.lowercase()} (${user.correo}) quedó pendiente de aprobación.\n\n" +
                            "Podrás iniciar sesión cuando un administrador la apruebe."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingUser = null
                        onNavigateToLogin()
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}
package com.example.costostock.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.costostock.User
import kotlinx.coroutines.launch

private const val PHONE_MAX_LENGTH = 20
private const val PHONE_ALLOWED_SYMBOLS = "+ -()"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    onBack: () -> Unit,
    onSave: (nombre: String, telefono: String) -> Boolean,
    onLogout: () -> Unit,
    approvalRequests: List<User> = emptyList(),
    onApproveRequest: (User) -> Unit = {},
    onRejectRequest: (User) -> Unit = {}
) {
    var nombre by remember(user) { mutableStateOf(user?.nombre.orEmpty()) }
    var telefono by remember(user) { mutableStateOf(user?.telefono.orEmpty()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val hasChanges = user != null &&
            (nombre.trim() != user.nombre || telefono.trim() != user.telefono)

    fun saveChanges() {
        when {
            user == null ->
                errorMessage = "No hay una sesión activa."

            validateName(nombre) != null ->
                errorMessage = validateName(nombre)

            telefono.count { it.isDigit() } < 8 ->
                errorMessage = "Ingresa un teléfono válido (mínimo 8 dígitos)"

            else -> {
                if (onSave(nombre.trim(), telefono.trim())) {
                    errorMessage = null
                    focusManager.clearFocus()
                    scope.launch { snackbarHostState.showSnackbar("Datos actualizados") }
                } else {
                    errorMessage = "No se pudieron guardar los cambios."
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay una sesión activa.",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = user.displayName(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = user.role,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Datos de la cuenta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = {
                                if (it.length <= NAME_MAX_LENGTH && it.all { c -> c.isLetter() || c == ' ' }) {
                                    nombre = it
                                }
                                errorMessage = null
                            },
                            label = { Text("Nombre y Apellido") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = user.correo,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Correo electrónico") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            supportingText = { Text("El correo identifica tu cuenta y no se puede modificar") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = telefono,
                            onValueChange = {
                                telefono = it
                                    .filter { c -> c.isDigit() || c in PHONE_ALLOWED_SYMBOLS }
                                    .take(PHONE_MAX_LENGTH)
                                errorMessage = null
                            },
                            label = { Text("Número de teléfono") },
                            placeholder = { Text("Ej: +56 9 1234 5678") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { if (hasChanges) saveChanges() else focusManager.clearFocus() }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        errorMessage?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = { saveChanges() },
                            enabled = hasChanges,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (user.role == "ADMINISTRADOR") {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    ApprovalRequestsSection(
                        requests = approvalRequests,
                        onApprove = { request ->
                            onApproveRequest(request)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("Cuenta de ${request.displayName()} aprobada")
                            }
                        },
                        onReject = { request ->
                            onRejectRequest(request)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("Solicitud de ${request.displayName()} rechazada")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

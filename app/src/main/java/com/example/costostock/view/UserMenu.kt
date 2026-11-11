package com.example.costostock.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.costostock.User

fun User?.displayName(): String = when {
    this == null -> "Mi cuenta"
    nombre.isNotBlank() -> nombre
    else -> correo
}

@Composable
fun UserMenuButton(
    user: User?,
    role: String,
    onOpenProfile: () -> Unit,
    onLogout: () -> Unit,
    pendingRequests: Int = 0,
    pendingQuestions: Int = 0,
    onOpenPendingQuestions: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            BadgedBox(
                badge = {
                    val totalPending = pendingRequests + pendingQuestions
                    if (totalPending > 0) {
                        Badge { Text(totalPending.toString()) }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Mi cuenta",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 220.dp, max = 280.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = user.displayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Text(
                        when (pendingRequests) {
                            0 -> "Mi perfil"
                            1 -> "Mi perfil · 1 solicitud"
                            else -> "Mi perfil · $pendingRequests solicitudes"
                        }
                    )
                },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                onClick = {
                    expanded = false
                    onOpenProfile()
                }
            )
            if (pendingQuestions > 0) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (pendingQuestions == 1) {
                                "1 pregunta por aprobar"
                            } else {
                                "$pendingQuestions preguntas por aprobar"
                            }
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onOpenPendingQuestions()
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    expanded = false
                    onLogout()
                }
            )
        }
    }
}

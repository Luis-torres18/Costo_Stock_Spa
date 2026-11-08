package com.example.costostock.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.costostock.AccountStatus
import com.example.costostock.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ApprovedGreen = Color(0xFF2E7D32)

private fun formatRequestDate(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))

@Composable
fun ApprovalRequestsSection(
    requests: List<User>,
    onApprove: (User) -> Unit,
    onReject: (User) -> Unit
) {
    val pending = requests.filter { it.status == AccountStatus.PENDIENTE }
    val reviewed = requests.filter { it.status != AccountStatus.PENDIENTE }

    var showHistory by remember { mutableStateOf(false) }
    var rejectTarget by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.HowToReg,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Solicitudes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (pending.isNotEmpty()) {
                Badge { Text(pending.size.toString()) }
            }
        }
        Text(
            text = "Cuentas de vendedor que esperan tu aprobación.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (pending.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "No hay solicitudes pendientes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        } else {
            pending.forEach { request ->
                key(request.correo) {
                    RequestCard(
                        request = request,
                        onApprove = { onApprove(request) },
                        onReject = { rejectTarget = request }
                    )
                }
            }
        }

        if (reviewed.isNotEmpty()) {
            TextButton(onClick = { showHistory = !showHistory }) {
                Text(if (showHistory) "Ocultar historial" else "Ver historial (${reviewed.size})")
            }
            if (showHistory) {
                reviewed.forEach { request ->
                    key(request.correo) {
                        RequestCard(request = request, onApprove = null, onReject = null)
                    }
                }
            }
        }
    }

    rejectTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            title = { Text("¿Rechazar solicitud?") },
            text = {
                Text("${target.displayName()} no podrá usar su cuenta de ${target.role.lowercase()}.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReject(target)
                        rejectTarget = null
                    }
                ) {
                    Text("Rechazar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectTarget = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun RequestCard(
    request: User,
    onApprove: (() -> Unit)?,
    onReject: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.displayName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Solicita cuenta de ${request.role.lowercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(status = request.status)
            }

            RequestInfoRow(icon = Icons.Default.Email, text = request.correo)
            RequestInfoRow(icon = Icons.Default.Phone, text = request.telefono.ifBlank { "Sin teléfono" })
            RequestInfoRow(
                icon = Icons.Default.Schedule,
                text = "Solicitado el ${formatRequestDate(request.requestedAtMillis)}"
            )

            if (onApprove != null && onReject != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rechazar")
                    }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aprobar")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: AccountStatus) {
    val (label, color) = when (status) {
        AccountStatus.PENDIENTE -> "Pendiente" to MaterialTheme.colorScheme.primary
        AccountStatus.APROBADA -> "Aprobada" to ApprovedGreen
        AccountStatus.RECHAZADA -> "Rechazada" to MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun RequestInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

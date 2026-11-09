package com.example.costostock.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.costostock.MAX_QUESTIONS_PER_PRODUCT
import com.example.costostock.ProductQuestion
import com.example.costostock.QuestionStatus
import com.example.costostock.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val QUESTION_MIN_LENGTH = 5
const val QUESTION_MAX_LENGTH = 200
const val ANSWER_MAX_LENGTH = 300

private val ApprovedColor = Color(0xFF2E7D32)

private fun formatQuestionDate(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))

@Composable
fun ProductQuestionsSection(
    questions: List<ProductQuestion>,
    user: User?,
    onAsk: (String) -> Boolean,
    onReview: (ProductQuestion, QuestionStatus) -> Unit,
    onAnswer: (ProductQuestion, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val role = user?.role
    val email = user?.correo.orEmpty()
    val isClient = role == "CLIENTE"
    val isAdmin = role == "ADMINISTRADOR"
    val isSeller = role == "VENDEDOR"

    fun isOwn(question: ProductQuestion): Boolean =
        email.isNotBlank() && question.askerEmail.equals(email, ignoreCase = true)

    val visibleQuestions = questions
        .filter { isAdmin || it.status == QuestionStatus.APROBADA || (isClient && isOwn(it)) }
        .sortedByDescending { it.createdAtMillis }

    val used = questions.count { isOwn(it) && it.status != QuestionStatus.RECHAZADA }
    val remaining = (MAX_QUESTIONS_PER_PRODUCT - used).coerceAtLeast(0)
    val pendingCount = questions.count { it.status == QuestionStatus.PENDIENTE }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.QuestionAnswer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Preguntas y respuestas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (isAdmin && pendingCount > 0) {
                    Badge { Text(pendingCount.toString()) }
                }
            }

            if (isClient) {
                AskQuestionForm(remaining = remaining, onAsk = onAsk)
            }

            if (isAdmin) {
                Text(
                    text = "Aprueba o rechaza las preguntas antes de que el vendedor pueda responderlas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSeller) {
                Text(
                    text = "Puedes responder las preguntas aprobadas por el administrador.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            if (visibleQuestions.isEmpty()) {
                Text(
                    text = "Aún no hay preguntas para este producto.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                visibleQuestions.forEachIndexed { index, question ->
                    key(question.id) {
                        val own = isOwn(question)
                        val author = when {
                            isAdmin -> question.askerName
                            own -> "Tú"
                            else -> question.askerName.trim().substringBefore(' ').ifBlank { "Cliente" }
                        }
                        QuestionItem(
                            question = question,
                            author = author,
                            showStatus = isAdmin || own,
                            canReview = isAdmin,
                            canAnswer = isSeller,
                            onReview = { status -> onReview(question, status) },
                            onAnswer = { answer -> onAnswer(question, answer) }
                        )
                    }
                    if (index < visibleQuestions.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun AskQuestionForm(
    remaining: Int,
    onAsk: (String) -> Boolean
) {
    var text by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (remaining > 0) {
                "Te quedan $remaining de $MAX_QUESTIONS_PER_PRODUCT preguntas en este producto."
            } else {
                "Ya usaste tus $MAX_QUESTIONS_PER_PRODUCT preguntas en este producto."
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        if (remaining > 0) {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    if (it.length <= QUESTION_MAX_LENGTH) text = it
                    errorMessage = null
                    infoMessage = null
                },
                label = { Text("Escribe tu pregunta") },
                supportingText = {
                    Text("${text.length}/$QUESTION_MAX_LENGTH · Un administrador la revisará antes de publicarla")
                },
                isError = errorMessage != null,
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    when {
                        text.trim().length < QUESTION_MIN_LENGTH ->
                            errorMessage = "La pregunta debe tener al menos $QUESTION_MIN_LENGTH caracteres."

                        onAsk(text) -> {
                            text = ""
                            errorMessage = null
                            infoMessage = "Pregunta enviada. Queda pendiente de revisión."
                        }

                        else -> errorMessage = "No se pudo enviar la pregunta."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Enviar pregunta")
            }
        }

        infoMessage?.let {
            Text(
                text = it,
                color = ApprovedColor,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun QuestionItem(
    question: ProductQuestion,
    author: String,
    showStatus: Boolean,
    canReview: Boolean,
    canAnswer: Boolean,
    onReview: (QuestionStatus) -> Unit,
    onAnswer: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = author,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatQuestionDate(question.createdAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showStatus) {
                QuestionStatusChip(question.status)
            }
        }

        Text(text = question.text, style = MaterialTheme.typography.bodyMedium)

        when {
            question.isAnswered -> {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Respuesta del vendedor",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = question.answer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            question.status == QuestionStatus.APROBADA && canAnswer ->
                AnswerForm(questionId = question.id, onAnswer = onAnswer)

            question.status == QuestionStatus.APROBADA ->
                Text(
                    text = "Aún sin respuesta del vendedor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
        }

        if (canReview && question.status == QuestionStatus.PENDIENTE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onReview(QuestionStatus.RECHAZADA) },
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
                    onClick = { onReview(QuestionStatus.APROBADA) },
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

@Composable
private fun AnswerForm(
    questionId: String,
    onAnswer: (String) -> Unit
) {
    var answerText by remember(questionId) { mutableStateOf("") }
    var errorMessage by remember(questionId) { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = answerText,
            onValueChange = {
                if (it.length <= ANSWER_MAX_LENGTH) answerText = it
                errorMessage = null
            },
            label = { Text("Tu respuesta") },
            supportingText = { Text("${answerText.length}/$ANSWER_MAX_LENGTH") },
            isError = errorMessage != null,
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                if (answerText.isBlank()) {
                    errorMessage = "Escribe una respuesta."
                } else {
                    onAnswer(answerText.trim())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Responder")
        }
    }
}

@Composable
private fun QuestionStatusChip(status: QuestionStatus) {
    val (label, color) = when (status) {
        QuestionStatus.PENDIENTE -> "En revisión" to MaterialTheme.colorScheme.primary
        QuestionStatus.APROBADA -> "Aprobada" to ApprovedColor
        QuestionStatus.RECHAZADA -> "Rechazada" to MaterialTheme.colorScheme.error
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

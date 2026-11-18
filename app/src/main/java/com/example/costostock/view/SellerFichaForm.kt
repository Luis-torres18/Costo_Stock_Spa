package com.example.costostock.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.costostock.SellerFicha
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val SANITARY_RESOLUTION_REQUIRED = true

fun cleanRut(input: String): String =
    input.filter { it.isDigit() || it == 'k' || it == 'K' }.uppercase()

fun formatRut(input: String): String {
    val clean = cleanRut(input)
    if (clean.length < 2) return clean
    val body = clean.dropLast(1).reversed().chunked(3).joinToString(".").reversed()
    return "$body-${clean.last()}"
}

fun validateRut(input: String): Boolean {
    val clean = cleanRut(input)
    if (clean.length !in 8..9) return false
    val body = clean.dropLast(1)
    if (!body.all { it.isDigit() }) return false

    var sum = 0
    var factor = 2
    for (c in body.reversed()) {
        sum += (c - '0') * factor
        factor = if (factor == 7) 2 else factor + 1
    }
    val remainder = 11 - sum % 11
    val expected = when (remainder) {
        11 -> '0'
        10 -> 'K'
        else -> '0' + remainder
    }
    return clean.last() == expected
}

fun formatDateInput(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(8)
    return buildString {
        digits.forEachIndexed { index, c ->
            if (index == 2 || index == 4) append('/')
            append(c)
        }
    }
}

fun parseFichaDate(text: String): Date? {
    if (text.length != 10) return null
    return try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }.parse(text)
    } catch (e: Exception) {
        null
    }
}

fun businessAgeLabel(dateText: String): String? {
    val start = parseFichaDate(dateText) ?: return null
    val from = Calendar.getInstance().apply { time = start }
    val to = Calendar.getInstance()

    var months = (to.get(Calendar.YEAR) - from.get(Calendar.YEAR)) * 12 +
            (to.get(Calendar.MONTH) - from.get(Calendar.MONTH))
    if (to.get(Calendar.DAY_OF_MONTH) < from.get(Calendar.DAY_OF_MONTH)) months--
    if (months < 0) return null

    val years = months / 12
    val rest = months % 12
    val yearsText = "$years ${if (years == 1) "año" else "años"}"
    val monthsText = "$rest ${if (rest == 1) "mes" else "meses"}"
    return when {
        years == 0 && rest == 0 -> "menos de 1 mes"
        years == 0 -> monthsText
        rest == 0 -> yearsText
        else -> "$yearsText y $monthsText"
    }
}

class SellerFichaDraft {
    var businessRut by mutableStateOf("")
    var legalName by mutableStateOf("")
    var businessActivity by mutableStateOf("")
    var activityStartDate by mutableStateOf("")
    var region by mutableStateOf("")
    var commune by mutableStateOf("")
    var street by mutableStateOf("")
    var municipalPatentNumber by mutableStateOf("")
    var sanitaryResolutionNumber by mutableStateOf("")
    var declarationAccepted by mutableStateOf(false)

    fun toFicha(): SellerFicha = SellerFicha(
        businessRut = formatRut(businessRut),
        legalName = legalName.trim(),
        businessActivity = businessActivity.trim(),
        activityStartDate = activityStartDate,
        region = region,
        commune = commune,
        street = street.trim(),
        municipalPatentNumber = municipalPatentNumber.trim(),
        sanitaryResolutionNumber = sanitaryResolutionNumber.trim()
    )
}

private fun validateActivityStartDate(text: String): String? {
    val date = parseFichaDate(text)
    return when {
        date == null -> "Ingresa una fecha de inicio de actividades válida (dd/mm/aaaa)"
        date.after(Date()) -> "La fecha de inicio de actividades no puede ser futura"
        Calendar.getInstance().apply { time = date }.get(Calendar.YEAR) < 1900 ->
            "La fecha de inicio de actividades no es válida"
        else -> null
    }
}

fun validateSellerFicha(draft: SellerFichaDraft): String? {
    if (!validateRut(draft.businessRut)) {
        return "El RUT del negocio no es válido"
    }
    if (draft.legalName.trim().length < 3) {
        return "Ingresa la razón social o tu nombre completo"
    }
    if (draft.businessActivity.trim().length < 3) {
        return "Ingresa el giro o actividad principal"
    }
    validateActivityStartDate(draft.activityStartDate)?.let { return it }
    if (draft.region.isBlank()) {
        return "Selecciona la región del local"
    }
    if (draft.commune.isBlank()) {
        return "Selecciona la comuna del local"
    }
    if (draft.street.trim().length < 5) {
        return "Ingresa la calle y el número del local"
    }
    if (draft.municipalPatentNumber.trim().length < 3) {
        return "Ingresa el número de patente municipal"
    }
    val sanitary = draft.sanitaryResolutionNumber.trim()
    if (SANITARY_RESOLUTION_REQUIRED && sanitary.isEmpty()) {
        return "Ingresa el número de resolución sanitaria"
    }
    if (sanitary.isNotEmpty() && sanitary.length < 3) {
        return "El número de resolución sanitaria no es válido"
    }
    if (!draft.declarationAccepted) {
        return "Debes aceptar la declaración de veracidad de los datos"
    }
    return null
}

@Composable
fun SellerFichaForm(
    draft: SellerFichaDraft,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        Text(
            text = "Ficha de inscripción del vendedor",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Solo pueden vender negocios formalizados ante el SII. Un administrador revisará estos datos antes de aprobar tu cuenta.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )

        FichaSectionTitle("Datos del negocio")
        FichaField(
            value = draft.businessRut,
            onValueChange = { text ->
                draft.businessRut = text.filter { it.isDigit() || it == '-' || it == '.' || it == 'k' || it == 'K' }
                onEdit()
            },
            label = "RUT del negocio",
            supportingText = "Ejemplo: 76.123.456-0. Si eres persona natural, tu RUT personal",
            maxLength = 12,
            keyboardType = KeyboardType.Ascii
        )
        FichaField(
            value = draft.legalName,
            onValueChange = {
                draft.legalName = it
                onEdit()
            },
            label = "Razón social o nombre completo"
        )
        FichaField(
            value = draft.businessActivity,
            onValueChange = {
                draft.businessActivity = it
                onEdit()
            },
            label = "Giro o actividad principal",
            supportingText = "Debe coincidir con el giro registrado en el SII"
        )
        FichaField(
            value = draft.activityStartDate,
            onValueChange = {
                draft.activityStartDate = formatDateInput(it)
                onEdit()
            },
            label = "Fecha de inicio de actividades",
            supportingText = "dd/mm/aaaa",
            maxLength = 10,
            keyboardType = KeyboardType.Number
        )

        FichaSectionTitle("Dirección del local")
        SelectField(
            label = "Región",
            value = draft.region,
            options = CHILE_REGIONS.keys.toList(),
            onSelect = {
                if (it != draft.region) {
                    draft.region = it
                    draft.commune = ""
                }
                onEdit()
            }
        )
        SelectField(
            label = "Comuna",
            value = draft.commune,
            options = CHILE_REGIONS[draft.region].orEmpty(),
            enabled = draft.region.isNotBlank(),
            onSelect = {
                draft.commune = it
                onEdit()
            }
        )
        FichaField(
            value = draft.street,
            onValueChange = {
                draft.street = it
                onEdit()
            },
            label = "Calle y número",
            supportingText = "Ejemplo: Av. Libertad 1234, local 5"
        )

        FichaSectionTitle("Permisos")
        FichaField(
            value = draft.municipalPatentNumber,
            onValueChange = {
                draft.municipalPatentNumber = it
                onEdit()
            },
            label = "N° de patente municipal",
            maxLength = 20
        )
        FichaField(
            value = draft.sanitaryResolutionNumber,
            onValueChange = {
                draft.sanitaryResolutionNumber = it
                onEdit()
            },
            label = if (SANITARY_RESOLUTION_REQUIRED) {
                "N° de resolución sanitaria (SEREMI de Salud)"
            } else {
                "N° de resolución sanitaria (opcional)"
            },
            maxLength = 30
        )

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = draft.declarationAccepted,
                    onValueChange = {
                        draft.declarationAccepted = it
                        onEdit()
                    },
                    role = Role.Checkbox
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = draft.declarationAccepted, onCheckedChange = null)
            Text(
                text = "Declaro que mi negocio tiene el inicio de actividades vigente ante el SII y que los datos ingresados son verdaderos.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun FichaSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun FichaField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    maxLength: Int = 80,
    keyboardType: KeyboardType = KeyboardType.Text,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { text -> if (text.length <= maxLength) onValueChange(text) },
        label = { Text(label) },
        supportingText = if (supportingText != null) {
            { Text(supportingText) }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SelectField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

package com.example.costostock.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.costostock.ProductOffer
import java.io.File

private const val PRICE_MAX_DIGITS = 7
private const val STOCK_MAX_DIGITS = 6
private const val MINUTES_MAX_DIGITS = 5
private const val DEFAULT_OFFER_MINUTES = "60"

private fun filterDecimal(input: String): String {
    var separatorUsed = false
    return input.filter { c ->
        when {
            c.isDigit() -> true
            (c == '.' || c == ',') && !separatorUsed -> {
                separatorUsed = true
                true
            }
            else -> false
        }
    }.take(8)
}

private fun String.toDecimalOrNull(): Double? = replace(',', '.').toDoubleOrNull()

private fun onlyDigits(input: String, maxDigits: Int): String =
    input.filter { it.isDigit() }.take(maxDigits)

private fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0 -> "$minutes min"
        minutes == 0 -> "$hours h"
        else -> "$hours h $minutes min"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEditScreen(
    product: ProductOffer?,
    onBack: () -> Unit,
    onSave: (ProductOffer) -> Unit
) {
    if (product == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar producto", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Este producto ya no existe.",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        ProductEditForm(product = product, onBack = onBack, onSave = onSave)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductEditForm(
    product: ProductOffer,
    onBack: () -> Unit,
    onSave: (ProductOffer) -> Unit
) {
    val context = LocalContext.current

    val openedAt = remember(product.id) { System.currentTimeMillis() }
    val wasOfferActive = remember(product.id) { product.isOfferActive(openedAt) }
    val initialDuration = remember(product.id) {
        if (wasOfferActive) {
            ((product.offerEndsAtMillis - openedAt + 59_999L) / 60_000L).toString()
        } else {
            DEFAULT_OFFER_MINUTES
        }
    }

    var imageUri by remember(product.id) { mutableStateOf(product.imageUri) }
    var nombre by remember(product.id) { mutableStateOf(product.title) }
    var categoria by remember(product.id) { mutableStateOf(product.category) }
    var descripcion by remember(product.id) { mutableStateOf(product.description) }
    var precioNormal by remember(product.id) { mutableStateOf(product.originalPrice.toString()) }
    var stock by remember(product.id) { mutableStateOf(product.stock.toString()) }
    var tienda by remember(product.id) { mutableStateOf(product.storeName) }
    var direccion by remember(product.id) { mutableStateOf(product.address) }
    var horario by remember(product.id) { mutableStateOf(product.pickupHours) }
    var distancia by remember(product.id) { mutableStateOf(product.distanceKm.toString()) }
    var co2 by remember(product.id) { mutableStateOf(product.co2SavedKg.toString()) }

    var enOferta by remember(product.id) { mutableStateOf(wasOfferActive) }
    var precioOferta by remember(product.id) {
        mutableStateOf(if (wasOfferActive) product.offerPrice.toString() else "")
    }
    var duracionMin by remember(product.id) { mutableStateOf(initialDuration) }

    var errorText by remember { mutableStateOf<String?>(null) }

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri.toString()
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val uri = tempCameraUri
        if (success && uri != null) imageUri = uri.toString()
    }

    fun prepareCameraAndLaunch() {
        val file = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    val normalValue = precioNormal.toIntOrNull()
    val ofertaValue = precioOferta.toIntOrNull()
    val discount: Int? =
        if (normalValue != null && ofertaValue != null && normalValue > 0 && ofertaValue in 1 until normalValue) {
            discountPercent(normalValue, ofertaValue)
        } else {
            null
        }

    val minutosValue = duracionMin.toIntOrNull()
    val keepsCurrentEnd = wasOfferActive && duracionMin == initialDuration
    val durationHint = when {
        minutosValue == null || minutosValue < 1 -> "Mínimo 1 minuto"
        keepsCurrentEnd -> "Tiempo que le queda a la oferta actual (${formatMinutes(minutosValue)})"
        else -> "La oferta durará ${formatMinutes(minutosValue)} desde que guardes"
    }

    fun save() {
        val normal = precioNormal.toIntOrNull() ?: 0
        val stockValue = stock.toIntOrNull()
        val distanciaValue = distancia.toDecimalOrNull()
        val co2Value = co2.toDecimalOrNull()
        val oferta = precioOferta.toIntOrNull() ?: 0
        val minutos = duracionMin.toIntOrNull() ?: 0

        val error = when {
            nombre.isBlank() -> "Ingresa el nombre del producto."
            categoria.isBlank() -> "Ingresa la categoría del producto."
            normal <= 0 -> "Ingresa un precio normal válido."
            stockValue == null -> "Ingresa el stock disponible."
            tienda.isBlank() -> "Ingresa el nombre de la tienda."
            direccion.isBlank() -> "Ingresa la dirección de retiro."
            distanciaValue == null || distanciaValue < 0 -> "Ingresa una distancia válida (km)."
            co2Value == null || co2Value < 0 -> "Ingresa un valor de CO₂ válido (kg)."
            enOferta && oferta <= 0 -> "Ingresa el precio de oferta."
            enOferta && oferta >= normal -> "El precio de oferta debe ser menor al precio normal ($$normal)."
            enOferta && minutos < 1 -> "La oferta debe durar al menos 1 minuto."
            else -> null
        }

        if (error != null || stockValue == null || distanciaValue == null || co2Value == null) {
            errorText = error
            return
        }

        val now = System.currentTimeMillis()
        val keepEnd = enOferta && keepsCurrentEnd && product.offerEndsAtMillis > now

        val updated = product.copy(
            title = nombre.trim(),
            category = categoria.trim(),
            description = descripcion.trim(),
            imageUri = imageUri,
            originalPrice = normal,
            offerPrice = if (enOferta) oferta else normal,
            offerEndsAtMillis = when {
                !enOferta -> 0L
                keepEnd -> product.offerEndsAtMillis
                else -> now + minutos * 60_000L
            },
            stock = stockValue,
            storeName = tienda.trim(),
            address = direccion.trim(),
            pickupHours = horario.trim().ifBlank { "Consultar horario con el local" },
            distanceKm = distanciaValue,
            co2SavedKg = co2Value
        )
        errorText = null
        onSave(updated)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar producto", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    errorText?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Button(
                        onClick = { save() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar cambios", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EditSection(title = "Imagen") {
                if (imageUri.isNotBlank()) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Imagen del producto",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { prepareCameraAndLaunch() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cámara", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galería", fontSize = 12.sp)
                    }
                    if (imageUri.isNotBlank()) {
                        TextButton(onClick = { imageUri = "" }) {
                            Text("Quitar", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }
            }

            EditSection(title = "Datos del producto") {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it.take(60); errorText = null },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it.take(30); errorText = null },
                    label = { Text("Tipo / Categoría") },
                    placeholder = { Text("Ej: Lácteos, Snacks") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it.take(300); errorText = null },
                    label = { Text("Descripción") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            EditSection(title = "Precio y stock") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = precioNormal,
                        onValueChange = { precioNormal = onlyDigits(it, PRICE_MAX_DIGITS); errorText = null },
                        label = { Text("Precio normal ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = onlyDigits(it, STOCK_MAX_DIGITS); errorText = null },
                        label = { Text("Stock") },
                        suffix = { Text("unid.") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (enOferta) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Poner en oferta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Se mostrará en \"Ofertas Destacadas\" del inicio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = enOferta,
                            onCheckedChange = {
                                enOferta = it
                                errorText = null
                            }
                        )
                    }

                    if (enOferta) {
                        OutlinedTextField(
                            value = precioOferta,
                            onValueChange = { precioOferta = onlyDigits(it, PRICE_MAX_DIGITS); errorText = null },
                            label = { Text("Precio de oferta ($)") },
                            placeholder = { Text("Ej: 1990") },
                            singleLine = true,
                            isError = precioOferta.isNotEmpty() && discount == null,
                            supportingText = {
                                if (precioOferta.isNotEmpty() && discount == null) {
                                    Text("Debe ser mayor a 0 y menor al precio normal")
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        DiscountSummary(
                            discount = discount,
                            normalPrice = normalValue,
                            offerPrice = ofertaValue
                        )

                        OutlinedTextField(
                            value = duracionMin,
                            onValueChange = { duracionMin = onlyDigits(it, MINUTES_MAX_DIGITS); errorText = null },
                            label = { Text("Duración de la oferta") },
                            suffix = { Text("minutos") },
                            singleLine = true,
                            supportingText = { Text(durationHint) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            EditSection(title = "Retiro en tienda") {
                OutlinedTextField(
                    value = tienda,
                    onValueChange = { tienda = it.take(60); errorText = null },
                    label = { Text("Tienda") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it.take(80); errorText = null },
                    label = { Text("Dirección") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = horario,
                    onValueChange = { horario = it.take(80); errorText = null },
                    label = { Text("Horario de retiro") },
                    placeholder = { Text("Ej: Lun a Vie 9:00 - 18:00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            EditSection(title = "Otros datos") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = distancia,
                        onValueChange = { distancia = filterDecimal(it); errorText = null },
                        label = { Text("Distancia") },
                        suffix = { Text("km") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = co2,
                        onValueChange = { co2 = filterDecimal(it); errorText = null },
                        label = { Text("CO₂ evitado") },
                        suffix = { Text("kg") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EditSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun DiscountSummary(
    discount: Int?,
    normalPrice: Int?,
    offerPrice: Int?
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (discount == null || normalPrice == null || offerPrice == null) {
            Text(
                text = "Ingresa el precio de oferta para calcular el descuento",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(14.dp)
            )
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Descuento",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "-$discount%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$$normalPrice → $$offerPrice",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Ahorro de $${normalPrice - offerPrice} por unidad",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

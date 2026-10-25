package com.example.costostock.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.costostock.ProductOffer
import kotlinx.coroutines.delay
import java.io.File
import java.util.UUID
import kotlin.collections.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    products: List<ProductOffer>,
    onAddToCart: (ProductOffer) -> Unit,
    onAddProduct: (ProductOffer) -> Unit,
    onUpdateStock: (ProductOffer, Int) -> Unit,
    onDeleteProduct: (ProductOffer) -> Unit,
    role: String
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    var showAddDialog by remember { mutableStateOf(false) }
    var stockTarget by remember { mutableStateOf<ProductOffer?>(null) }

    val now by rememberNowMillis()
    val isSeller = role == "ADMINISTRADOR" || role == "VENDEDOR"
    val visibleProducts = if (isSeller) products else products.filter { it.isVisibleToBuyer(now) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario de Excedentes", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            if (role == "ADMINISTRADOR" || role == "VENDEDOR") {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Agregar") },
                    text = { Text("Nuevo Producto") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            val availableCategories = listOf("Todos") + visibleProducts.map { it.category }.distinct()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.take(4).forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            val filteredList = if (selectedFilter == "Todos") {
                visibleProducts
            } else {
                visibleProducts.filter { it.category == selectedFilter }
            }

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay productos disponibles.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    items(filteredList, key = { it.id }) { offer ->
                        CatalogCard(
                            offer = offer,
                            now = now,
                            onAddToCart = { onAddToCart(offer) },
                            onUpdateStock = { stockTarget = offer },
                            onDelete = { onDeleteProduct(offer) },
                            role = role
                        )
                    }
                }
            }
        }
    }

    stockTarget?.let { target ->
        UpdateStockDialog(
            product = target,
            onDismiss = { stockTarget = null },
            onConfirm = { newStock ->
                onUpdateStock(target, newStock)
                stockTarget = null
            }
        )
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newProduct ->
                onAddProduct(newProduct)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CatalogCard(
    offer: ProductOffer,
    now: Long,
    onAddToCart: () -> Unit,
    onUpdateStock: () -> Unit,
    onDelete: () -> Unit,
    role: String
) {
    val offerActive = offer.isOfferActive(now)
    val timeText = if (offerActive) {
        formatRemaining(offer.offerEndsAtMillis - now)
    } else {
        "Oferta finalizada"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (offer.imageUri.isNotBlank()) {
                AsyncImage(
                    model = offer.imageUri,
                    contentDescription = offer.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = offer.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tipo: ${offer.category}\n${offer.storeName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (role == "ADMINISTRADOR" || role == "VENDEDOR") {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Borrar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (offer.description.isNotBlank()) {
                Text(
                    text = offer.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$${offer.priceAt(now)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (offerActive) {
                        Text(
                            text = "$${offer.originalPrice}",
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = if (offer.stock > 0) "${offer.stock} disp." else "Agotado",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Eco,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "-${offer.co2SavedKg} kg CO₂",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAddToCart,
                enabled = offer.stock > 0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir al Carrito")
            }

            if (role == "ADMINISTRADOR" || role == "VENDEDOR") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onUpdateStock,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualizar stock")
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (ProductOffer) -> Unit
) {
    val context = LocalContext.current

    var tipo by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var horas by remember { mutableStateOf("1") }
    var minutos by remember { mutableStateOf("0") }
    var errorText by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            selectedImageUri = tempCameraUri
        }
    }

    fun prepareCameraAndLaunch() {
        val file = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Plantilla: Nuevo Producto", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Previsualización",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                }

                OutlinedTextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text("Tipo / Categoría") },
                    placeholder = { Text("Ej: Lácteos, Snacks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del producto") },
                    placeholder = { Text("Ej: Pack Yogur") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Ej: Próximo a vencer") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = valor,
                        onValueChange = { if (it.all { c -> c.isDigit() }) valor = it },
                        label = { Text("Valor ($)") },
                        placeholder = { Text("Ej: 1990") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { if (it.all { c -> c.isDigit() }) cantidad = it },
                        label = { Text("Cantidad") },
                        placeholder = { Text("Ej: 10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Duración de la oferta",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = horas,
                        onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) horas = it },
                        label = { Text("Horas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minutos,
                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) minutos = it },
                        label = { Text("Minutos") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val durationMinutes = (horas.toIntOrNull() ?: 0) * 60 + (minutos.toIntOrNull() ?: 0)
                    if (tipo.isBlank() || nombre.isBlank() || descripcion.isBlank() || valor.isBlank() || cantidad.isBlank()) {
                        errorText = "Por favor completa todos los campos de la plantilla."
                    } else if (durationMinutes <= 0) {
                        errorText = "La oferta debe durar al menos 1 minuto."
                    } else if ((cantidad.toIntOrNull() ?: 0) < 1) {
                        errorText = "La cantidad debe ser al menos 1."
                    } else {
                        val parsedPrice = valor.toIntOrNull() ?: 0
                        val parsedStock = cantidad.toIntOrNull() ?: 1
                        val estimatedOriginalPrice = (parsedPrice * 1.5).toInt()

                        val newOffer = ProductOffer(
                            id = "CS-${UUID.randomUUID().toString().take(5).uppercase()}",
                            title = nombre.trim(),
                            description = descripcion.trim(),
                            category = tipo.trim(),
                            storeName = "Sucursal Costo Stock Local",
                            address = "Av. Principal 101",
                            distanceKm = 1.0,
                            originalPrice = estimatedOriginalPrice,
                            offerPrice = parsedPrice,
                            stock = parsedStock,
                            offerEndsAtMillis = System.currentTimeMillis() + durationMinutes * 60_000L,
                            co2SavedKg = 1.20,
                            imageUri = selectedImageUri?.toString() ?: ""
                        )
                        onConfirm(newOffer)
                    }
                }
            ) {
                Text("Guardar Producto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun UpdateStockDialog(
    product: ProductOffer,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var value by remember(product.id) { mutableStateOf(product.stock.toString()) }
    var errorMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Actualizar stock", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(product.title, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                            value = it
                            errorMsg = ""
                        }
                    },
                    label = { Text("Unidades disponibles") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newStock = value.toIntOrNull()
                    if (newStock == null) {
                        errorMsg = "Ingresa un número válido."
                    } else {
                        onConfirm(newStock)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

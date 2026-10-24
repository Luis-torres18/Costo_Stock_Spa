package com.example.costostock.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.costostock.CartItem
import com.example.costostock.PurchaseVoucher
import com.example.costostock.User
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    buyer: User?,
    onIncreaseQuantity: (CartItem) -> Unit,
    onDecreaseQuantity: (CartItem) -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    onClearCart: () -> Unit,
    onGoToProducts: () -> Unit,
    onVoucherIssued: (PurchaseVoucher) -> Unit,
    onOpenVouchers: () -> Unit
) {
    var showCheckoutDialog by remember { mutableStateOf(false) }

    // Los datos del comprador vienen de su cuenta; se pueden corregir antes de emitir.
    var buyerName by remember(buyer) { mutableStateOf(buyer?.nombre.orEmpty()) }
    var buyerEmail by remember(buyer) { mutableStateOf(buyer?.correo.orEmpty()) }
    var buyerPhone by remember(buyer) { mutableStateOf(buyer?.telefono.orEmpty()) }
    var checkoutError by remember { mutableStateOf<String?>(null) }

    val totalAmount = remember(cartItems.toList()) {
        cartItems.sumOf { it.product.offerPrice * it.quantity }
    }

    val totalCo2 = remember(cartItems.toList()) {
        cartItems.sumOf { it.product.co2SavedKg * it.quantity }
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showCheckoutDialog = false
                checkoutError = null
            },
            title = { Text("Confirmar compra de demostración") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Revisa tus datos para emitir el voucher. " +
                                "No se realizará un cobro real."
                    )

                    OutlinedTextField(
                        value = buyerName,
                        onValueChange = { buyerName = it; checkoutError = null },
                        label = { Text("Nombre del comprador") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = buyerEmail,
                        onValueChange = { buyerEmail = it; checkoutError = null },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = buyerPhone,
                        onValueChange = { buyerPhone = it; checkoutError = null },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    checkoutError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when {
                            cartItems.isEmpty() ->
                                checkoutError = "El carrito está vacío."

                            buyerName.isBlank() || buyerEmail.isBlank() || buyerPhone.isBlank() ->
                                checkoutError = "Completa nombre, correo y teléfono."

                            !android.util.Patterns.EMAIL_ADDRESS
                                .matcher(buyerEmail.trim())
                                .matches() ->
                                checkoutError = "Ingresa un correo válido."

                            buyerPhone.count { it.isDigit() } < 8 ->
                                checkoutError = "Ingresa un teléfono válido."

                            else -> {
                                val voucher = buildVoucher(
                                    accountId = buyer?.correo.orEmpty(),
                                    items = cartItems.toList(),
                                    buyerName = buyerName.trim(),
                                    buyerEmail = buyerEmail.trim(),
                                    buyerPhone = buyerPhone.trim()
                                )
                                showCheckoutDialog = false
                                checkoutError = null
                                onVoucherIssued(voucher)
                            }
                        }
                    }
                ) {
                    Text("Confirmar y emitir voucher")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCheckoutDialog = false
                        checkoutError = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrito de Compras", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenVouchers) {
                        Icon(
                            Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = "Mis vouchers"
                        )
                    }
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = onClearCart) {
                            Text("Vaciar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tu carrito está vacío",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Explora las ofertas y añade productos al carrito.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = onGoToProducts) {
                        Text("Ver productos")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = cartItems,
                        key = { it.product.id }
                    ) { item ->
                        CartItemRow(
                            item = item,
                            onIncrease = { onIncreaseQuantity(item) },
                            onDecrease = { onDecreaseQuantity(item) },
                            onRemove = { onRemoveItem(item) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Resumen del pedido",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cantidad total de unidades:")
                            Text(
                                "${cartItems.sumOf { it.quantity }} un.",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Huella de CO₂ evitada:")
                            Text(
                                "${String.format(Locale.US, "%.2f", totalCo2)} kg",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total a pagar:",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$$totalAmount",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                checkoutError = null
                                showCheckoutDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                Icons.Default.ShoppingCartCheckout,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Comprar y emitir voucher",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "$${item.product.offerPrice} c/u • Máx disp: ${item.product.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Subtotal: $${item.product.offerPrice * item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onDecrease,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Restar",
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                FilledTonalIconButton(
                    onClick = onIncrease,
                    enabled = item.quantity < item.product.stock,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Sumar",
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Quitar ítem",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
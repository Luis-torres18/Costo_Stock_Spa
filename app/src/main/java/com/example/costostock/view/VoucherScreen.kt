package com.example.costostock.view

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.costostock.CartItem
import com.example.costostock.PurchaseVoucher
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

const val VOUCHER_TERMS =
    "1. Este voucher acredita una compra para retiro presencial en el/los local(es) indicado(s). " +
            "2. Para retirar, el comprador debe presentar este voucher (código QR o número de " +
            "transacción) dentro del horario de retiro del local.\n" +
            "3. Si el comprador no retira el producto, ello constituye negligencia total del " +
            "comprador, quien asume la responsabilidad por no retirarlo.\n" +
            "4. Costo Stock actúa como medio de contacto para la venta; la entrega del producto " +
            "se realiza en el local del proveedor.\n"


fun formatClp(amount: Int): String =
    "\$" + NumberFormat.getInstance(Locale.forLanguageTag("es-CL")).format(amount)

fun buildVoucher(
    accountId: String,
    items: List<CartItem>,
    buyerName: String,
    buyerEmail: String,
    buyerPhone: String
): PurchaseVoucher {
    val now = Date()
    val dateText = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(now)
    val dayStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(now)

    fun token(length: Int) = UUID.randomUUID()
        .toString()
        .replace("-", "")
        .take(length)
        .uppercase(Locale.ROOT)

    return PurchaseVoucher(
        voucherCode = "CS-${token(10)}",
        transactionNumber = "TX-$dayStamp-${token(6)}",
        accountId = accountId,
        items = items.map { it.copy() },
        purchaseDate = dateText,
        totalPaid = items.sumOf { it.product.offerPrice * it.quantity },
        buyerName = buyerName,
        buyerEmail = buyerEmail,
        buyerPhone = buyerPhone
    )
}

private fun PurchaseVoucher.qrContent(): String =
    listOf("COSTOSTOCK", voucherCode, transactionNumber).joinToString("|")

private fun PurchaseVoucher.itemsByStore(): List<Pair<String, List<CartItem>>> =
    items.groupBy { it.product.storeName + "\n" + it.product.address }.toList()

private fun PurchaseVoucher.toShareText(): String = buildString {
    appendLine("Voucher Costo Stock")
    appendLine("Código: $voucherCode")
    appendLine("N° de transacción: $transactionNumber")
    appendLine("Fecha: $purchaseDate")
    appendLine("Total: ${formatClp(totalPaid)} ($paymentStatus)")
    appendLine()
    itemsByStore().forEach { (_, storeItems) ->
        val product = storeItems.first().product
        appendLine("Retiro en: ${product.storeName}")
        appendLine("Dirección: ${product.address}")
        appendLine("Horario: ${storeItems.map { it.product.pickupHours }.distinct().joinToString(" / ")}")
        storeItems.forEach { appendLine("  ${it.quantity}x ${it.product.title}") }
        appendLine()
    }
    appendLine("Comprador: $buyerName")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherScreen(
    voucher: PurchaseVoucher?,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voucher de compra", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (voucher != null) {
                        IconButton(
                            onClick = {
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Voucher ${voucher.voucherCode}")
                                    putExtra(Intent.EXTRA_TEXT, voucher.toShareText())
                                }
                                context.startActivity(Intent.createChooser(send, "Compartir voucher"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir voucher")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (voucher == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontró el voucher.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "¡Voucher emitido!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Presenta este código en el local para retirar",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(16.dp))

                    // Fondo blanco fijo: el QR debe leerse también en modo oscuro.
                    Box(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        VoucherQrCode(
                            content = voucher.qrContent(),
                            modifier = Modifier.size(200.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = voucher.voucherCode,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }


            VoucherSection("Transacción") {
                LabeledRow("N° de transacción", voucher.transactionNumber)
                LabeledRow("Fecha", voucher.purchaseDate)
                LabeledRow("Estado del pago", voucher.paymentStatus)
                LabeledRow("Total", formatClp(voucher.totalPaid), bold = true)
            }

            voucher.itemsByStore().forEach { (_, storeItems) ->
                val first = storeItems.first().product
                VoucherSection("Retiro en local") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(first.storeName, fontWeight = FontWeight.Bold)
                    }
                    LabeledRow("Dirección", first.address)
                    LabeledRow(
                        "Horario de retiro",
                        storeItems.map { it.product.pickupHours }.distinct().joinToString(" / ")
                    )
                    HorizontalDivider()
                    storeItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${item.quantity}x ${item.product.title}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                formatClp(item.product.offerPrice * item.quantity),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            VoucherSection("Datos del comprador") {
                LabeledRow("Nombre", voucher.buyerName)
                LabeledRow("Correo", voucher.buyerEmail)
                LabeledRow("Teléfono", voucher.buyerPhone)
            }

            VoucherSection("Términos y condiciones") {
                Text(VOUCHER_TERMS, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aceptar y finalizar")
            }
        }
    }
}

@Composable
private fun VoucherSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun LabeledRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VouchersListScreen(
    vouchers: List<PurchaseVoucher>,
    onOpen: (PurchaseVoucher) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis vouchers", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (vouchers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aún no tienes vouchers.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(vouchers, key = { it.voucherCode }) { voucher ->
                    Card(
                        onClick = { onOpen(voucher) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                voucher.voucherCode,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                voucher.purchaseDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${voucher.items.sumOf { it.quantity }} un. • " +
                                        voucher.items.map { it.product.storeName }.distinct()
                                            .joinToString(", "),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                formatClp(voucher.totalPaid),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun VoucherQrCode(
    content: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(content) {
        runCatching {
            val size = 512
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
            val pixels = IntArray(size * size) { index ->
                val x = index % size
                val y = index / size
                if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
            Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Código QR del voucher",
            modifier = modifier
        )
    } else {
        Icon(
            imageVector = Icons.Default.QrCode,
            contentDescription = "Código QR no disponible",
            tint = Color.Black,
            modifier = modifier
        )
    }
}

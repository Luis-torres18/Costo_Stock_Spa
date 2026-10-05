package com.example.costostock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.costostock.ui.theme.CostoStockTheme
import com.example.costostock.view.HomeScreen
import com.example.costostock.view.SecondScreen

data class ProductOffer(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val storeName: String,
    val address: String,
    val distanceKm: Double,
    val originalPrice: Int,
    val offerPrice: Int,
    var stock: Int,
    val initialSecondsRemaining: Long,
    val co2SavedKg: Double
)

data class PurchaseVoucher(
    val voucherCode: String,
    val product: ProductOffer,
    val purchaseDate: String,
    val totalPaid: Int,
    val totalCo2Saved: Double
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CostoStockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val productsList = remember {
                        mutableStateListOf(
                            ProductOffer(
                                id = "CS-001",
                                title = "Pack Yogur Batido 12x120g",
                                description = "Lote refrigerado en óptimo estado, vencimiento a 5 días.",
                                category = "Lácteos",
                                storeName = "Supermercado Central Valparaíso",
                                address = "Av. Pedro Montt 1820",
                                distanceKm = 0.8,
                                originalPrice = 4990,
                                offerPrice = 1990,
                                stock = 8,
                                initialSecondsRemaining = 3600L,
                                co2SavedKg = 1.45
                            ),
                            ProductOffer(
                                id = "CS-002",
                                title = "Caja Galletas Avena & Miel 400g",
                                description = "Excedente de temporada con empaque intacto.",
                                category = "Snacks",
                                storeName = "EcoMarket Viña del Mar",
                                address = "Calle Valparaíso 650",
                                distanceKm = 1.6,
                                originalPrice = 3290,
                                offerPrice = 1290,
                                stock = 4,
                                initialSecondsRemaining = 1800L,
                                co2SavedKg = 0.85
                            ),
                            ProductOffer(
                                id = "CS-003",
                                title = "Queso Gauda Laminado 500g",
                                description = "Corte laminado al vacío, conservación en frío.",
                                category = "Lácteos",
                                storeName = "Distribuidora Costa",
                                address = "Av. Libertad 340",
                                distanceKm = 2.3,
                                originalPrice = 5890,
                                offerPrice = 2790,
                                stock = 12,
                                initialSecondsRemaining = 7200L,
                                co2SavedKg = 2.30
                            )
                        )
                    }

                    var selectedProduct by remember { mutableStateOf<ProductOffer?>(null) }

                    if (selectedProduct == null) {
                        HomeScreen(
                            products = productsList,
                            onProductSelected = { product -> selectedProduct = product },
                            onAddProduct = { newProduct -> productsList.add(0, newProduct) },
                            onDeleteProduct = { productToDelete -> productsList.remove(productToDelete) }
                        )
                    } else {
                        SecondScreen(
                            product = selectedProduct!!,
                            onBack = { selectedProduct = null }
                        )
                    }
                }
            }
        }
    }
}
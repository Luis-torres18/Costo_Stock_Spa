package com.example.costostock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.costostock.ui.theme.CostoStockTheme
import com.example.costostock.view.CartScreen
import com.example.costostock.view.HomeScreen
import com.example.costostock.view.LoginScreen
import com.example.costostock.view.ProductsScreen
import com.example.costostock.view.RegisterScreen
import com.example.costostock.view.VoucherScreen
import com.example.costostock.view.VouchersListScreen

const val HIDE_EXPIRED_OFFERS = false

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
    val stock: Int,
    val offerEndsAtMillis: Long,
    val co2SavedKg: Double,
    val imageUri: String = "",
    val pickupHours: String = "Consultar horario con el local"
) {

    fun isOfferActive(nowMillis: Long = System.currentTimeMillis()): Boolean =
        offerPrice < originalPrice && nowMillis < offerEndsAtMillis
    fun priceAt(nowMillis: Long = System.currentTimeMillis()): Int =
        if (isOfferActive(nowMillis)) offerPrice else originalPrice

    val currentPrice: Int get() = priceAt()

    fun isVisibleToBuyer(nowMillis: Long = System.currentTimeMillis()): Boolean =
        stock > 0 && (!HIDE_EXPIRED_OFFERS || nowMillis < offerEndsAtMillis)
}

data class CartItem(
    val product: ProductOffer,
    var quantity: Int
)
data class VoucherItem(
    val title: String,
    val storeName: String,
    val address: String,
    val pickupHours: String,
    val quantity: Int,
    val unitPrice: Int
)

data class PurchaseVoucher(
    val voucherCode: String,
    val transactionNumber: String,
    val accountId: String,
    val items: List<VoucherItem>,
    val purchaseDate: String,
    val totalPaid: Int,
    val buyerName: String,
    val buyerEmail: String,
    val buyerPhone: String,
    val paymentStatus: String = "PAGO SIMULADO"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CostoStockTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var currentUserRole by remember { mutableStateOf("CLIENTE") }
    var currentUser by remember {
        mutableStateOf<User?>(null)
    }

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
                offerEndsAtMillis = System.currentTimeMillis() + 3_600_000L,
                co2SavedKg = 1.45,
                imageUri = "https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=600&q=80"
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
                offerEndsAtMillis = System.currentTimeMillis() + 1_800_000L,
                co2SavedKg = 0.85,
                imageUri = "https://images.unsplash.com/photo-1558961363-fa8fdf82db35?auto=format&fit=crop&w=600&q=80"
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
                offerEndsAtMillis = System.currentTimeMillis() + 7_200_000L,
                co2SavedKg = 2.30,
                imageUri = "https://media.solotodo.com/media/products/2091794.jpg"
            )
        )
    }

    val cartItems = remember { mutableStateListOf<CartItem>() }
    val vouchers = remember { mutableStateListOf<PurchaseVoucher>() }

    fun changeStock(productId: String, delta: Int) {
        val index = productsList.indexOfFirst { it.id == productId }
        if (index >= 0) {
            val current = productsList[index]
            productsList[index] = current.copy(stock = (current.stock + delta).coerceAtLeast(0))
        }
    }

    fun stockLeft(productId: String): Int =
        productsList.firstOrNull { it.id == productId }?.stock ?: 0

    fun addToCart(product: ProductOffer) {
        val fresh = productsList.firstOrNull { it.id == product.id } ?: return
        if (fresh.stock <= 0) return

        val existingIndex = cartItems.indexOfFirst { it.product.id == fresh.id }
        if (existingIndex >= 0) {
            val currentItem = cartItems[existingIndex]
            cartItems[existingIndex] = currentItem.copy(quantity = currentItem.quantity + 1)
        } else {
            cartItems.add(CartItem(product = fresh, quantity = 1))
        }
        changeStock(fresh.id, -1)
    }

    fun releaseCart() {
        cartItems.forEach { changeStock(it.product.id, it.quantity) }
        cartItems.clear()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != "login" && currentRoute != "register" && currentRoute != null) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute.startsWith("home"),
                        onClick = {
                            navController.navigate("home/$currentUserRole") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.LocalOffer, contentDescription = "Inicio") },
                        label = { Text("Inicio") }
                    )
                    NavigationBarItem(
                        selected = currentRoute.startsWith("products"),
                        onClick = {
                            // Ahora navegamos a productos pasando el rol actual
                            navController.navigate("products/$currentUserRole") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Store, contentDescription = "Productos") },
                        label = { Text("Productos") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "cart",
                        onClick = {
                            navController.navigate("cart") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (cartItems.isNotEmpty()) {
                                        Badge { Text(cartItems.sumOf { it.quantity }.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                            }
                        },
                        label = { Text("Carrito") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            composable("login") {
                LoginScreen(
                    onLoginSuccess = { user ->
                        currentUser = user
                        currentUserRole = user.role
                        navController.navigate("home/${user.role}") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = { user ->
                        currentUser = user
                        currentUserRole = user.role
                        navController.navigate("home/${user.role}") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable("home/{role}") { backStackEntry ->
                val userRole = backStackEntry.arguments?.getString("role") ?: "CLIENTE"

                LaunchedEffect(userRole) {
                    currentUserRole = userRole
                }

                HomeScreen(
                    role = userRole,
                    products = productsList,
                    onAddToCart = { product -> addToCart(product) },
                    onNavigateToProducts = { navController.navigate("products/$userRole") },
                    onNavigateToCart = { navController.navigate("cart") },
                    onLogout = {
                        currentUser = null
                        releaseCart()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                )
            }

            composable("products/{role}") { backStackEntry ->
                val userRole = backStackEntry.arguments?.getString("role") ?: "CLIENTE"

                ProductsScreen(
                    role = userRole,
                    products = productsList,
                    onAddToCart = { product -> addToCart(product) },
                    onAddProduct = { newProduct -> productsList.add(0, newProduct) },
                    onUpdateStock = { product, newStock ->
                        val index = productsList.indexOfFirst { it.id == product.id }
                        if (index >= 0) {
                            productsList[index] = productsList[index]
                                .copy(stock = newStock.coerceAtLeast(0))
                        }
                    },
                    onDeleteProduct = { productToDelete ->
                        productsList.remove(productToDelete)
                        cartItems.removeAll { it.product.id == productToDelete.id }
                    }
                )
            }

            composable("cart") {
                CartScreen(
                    cartItems = cartItems,
                    buyer = currentUser,
                    stockLeft = { productId -> stockLeft(productId) },
                    onIncreaseQuantity = { item -> addToCart(item.product) },
                    onDecreaseQuantity = { item ->
                        val index = cartItems.indexOf(item)
                        if (index >= 0) {
                            if (item.quantity > 1) {
                                cartItems[index] = item.copy(quantity = item.quantity - 1)
                            } else {
                                cartItems.removeAt(index)
                            }
                            changeStock(item.product.id, 1)
                        }
                    },
                    onRemoveItem = { item ->
                        if (cartItems.remove(item)) {
                            changeStock(item.product.id, item.quantity)
                        }
                    },
                    onClearCart = { releaseCart() },
                    onGoToProducts = {
                        navController.navigate("products/$currentUserRole") {
                            launchSingleTop = true
                        }
                    },
                    onVoucherIssued = { voucher ->
                        vouchers.add(0, voucher)
                        cartItems.clear()
                        navController.navigate("voucher/${voucher.voucherCode}") {
                            popUpTo("cart") { inclusive = true }
                        }
                    },
                    onOpenVouchers = { navController.navigate("vouchers") }
                )
            }

            composable("voucher/{code}") { backStackEntry ->
                val code = backStackEntry.arguments?.getString("code")
                VoucherScreen(
                    voucher = vouchers.find { it.voucherCode == code },
                    onClose = { navController.popBackStack() }
                )
            }

            composable("vouchers") {
                val accountId = currentUser?.correo.orEmpty()
                VouchersListScreen(
                    vouchers = vouchers.filter { it.accountId == accountId },
                    onOpen = { voucher ->
                        navController.navigate("voucher/${voucher.voucherCode}")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

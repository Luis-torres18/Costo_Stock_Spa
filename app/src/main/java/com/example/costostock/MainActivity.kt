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
    val co2SavedKg: Double,
    val imageUri: String = ""
)

data class CartItem(
    val product: ProductOffer,
    var quantity: Int
)

data class PurchaseVoucher(
    val voucherCode: String,
    val items: List<CartItem>,
    val purchaseDate: String,
    val totalPaid: Int,
    val totalCo2Saved: Double
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
                initialSecondsRemaining = 1800L,
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
                initialSecondsRemaining = 7200L,
                co2SavedKg = 2.30,
                imageUri = "https://media.solotodo.com/media/products/2091794.jpg"
            )
        )
    }

    val cartItems = remember { mutableStateListOf<CartItem>() }

    fun addToCart(product: ProductOffer) {
        val existingIndex = cartItems.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val currentItem = cartItems[existingIndex]
            if (currentItem.quantity < product.stock) {
                cartItems[existingIndex] = currentItem.copy(quantity = currentItem.quantity + 1)
            }
        } else {
            cartItems.add(CartItem(product = product, quantity = 1))
        }
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
                    onLoginSuccess = { role ->
                        currentUserRole = role
                        navController.navigate("home/$role") {
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
                    onRegisterSuccess = { role ->
                        currentUserRole = role
                        navController.navigate("home/$role") {
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
                    onDeleteProduct = { productToDelete ->
                        productsList.remove(productToDelete)
                        cartItems.removeAll { it.product.id == productToDelete.id }
                    }
                )
            }

            composable("cart") {
                CartScreen(
                    cartItems = cartItems,
                    onIncreaseQuantity = { item ->
                        val index = cartItems.indexOf(item)
                        if (index >= 0 && item.quantity < item.product.stock) {
                            cartItems[index] = item.copy(quantity = item.quantity + 1)
                        }
                    },
                    onDecreaseQuantity = { item ->
                        val index = cartItems.indexOf(item)
                        if (index >= 0) {
                            if (item.quantity > 1) {
                                cartItems[index] = item.copy(quantity = item.quantity - 1)
                            } else {
                                cartItems.removeAt(index)
                            }
                        }
                    },
                    onRemoveItem = { item -> cartItems.remove(item) },
                    onClearCart = { cartItems.clear() },
                    onGoToProducts = {
                        navController.navigate("products/$currentUserRole") {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
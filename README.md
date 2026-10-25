# Marketplace Móvil — Costo Stock SpA 📱🛒

Proyecto desarrollado para la asignatura/módulo correspondiente al **Grupo 5**. Consiste en una aplicación móvil nativa para Android enfocada en el comercio electrónico (marketplace), permitiendo la exploración de productos, gestión de compras mediante carrito, autenticación de usuarios y generación de vouchers con código QR.

---

## 👥 Integrantes y Roles (Grupo 5)

| Integrante | Rol y Contribuciones Principales |
| :--- | :--- |
| **Luis Torres** | • **Diseño Base y Visual:** Estructuración del sistema de diseño (UI/UX), paleta de colores, tipografías y tema base en Jetpack Compose (`ui/theme`).<br>• Maquetación y estructura visual principal de pantallas (`HomeScreen`, `ProductsScreen`, `CartScreen`). |
| **Vicente Cisternas** | • **Autenticación y Formularios:** Implementación de pantallas de Login y Registro (`LoginScreen`, `RegisterScreen`).<br>• Flujo de captura y manejo de datos de usuarios interactuando con el repositorio local (`UserRepository`). |
| **Benjamín Olivares** | • **Voucher y Código QR:** Implementación del comprobante de venta (`VoucherScreen`) detallando información del comprador, vendedor y transacción, con generación dinámica de código QR.<br>• **Validaciones:** Lógica de validación de campos para formularios de acceso y registro (correos, contraseñas y consistencia de datos). |

---

## 🚀 Funcionalidades Principales

1. **Catálogo y Navegación:**
   - Visualización de productos en stock y ofertas temporales (`OfferTime`).
   - Navegación fluida y diseño moderno adaptado a Material Design 3.
2. **Gestión de Carrito de Compras:**
   - Adición de artículos, cálculo de totales y preparación del pedido (`CartScreen`).
3. **Módulo de Usuarios y Seguridad:**
   - Registro de nuevas cuentas y autenticación por credenciales.
   - Validación robusta de campos en tiempo de ejecución.
4. **Voucher Digital con QR:**
   - Generación de resumen de transacción con datos del comprador y vendedor.
   - Código QR representativo de la compra para verificación rápida.

---

## 🛠️ Tecnologías y Herramientas

- **Lenguaje:** Kotlin
- **Framework UI:** Jetpack Compose (Material 3)
- **Entorno de Desarrollo:** Android Studio
- **Gestor de Dependencias y Construcción:** Gradle (Kotlin DSL - `build.gradle.kts`)
- **Control de Versiones:** Git / GitHub

---

## 📂 Estructura del Proyecto

```text
Costo_Stock_Spa/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/costostock/
│   │   │   │   ├── MainActivity.kt        # Punto de entrada y navegación
│   │   │   │   ├── ui/theme/              # Tema, paleta de colores y estilos
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   └── Type.kt
│   │   │   │   └── view/                  # Vistas y lógica de pantallas
│   │   │   │       ├── CartScreen.kt
│   │   │   │       ├── HomeScreen.kt
│   │   │   │       ├── LoginScreen.kt
│   │   │   │       ├── OfferTime.kt
│   │   │   │       ├── ProductsScreen.kt
│   │   │   │       ├── RegisterScreen.kt
│   │   │   │       ├── UserRepository.kt
│   │   │   │       └── VoucherScreen.kt
│   │   │   └── res/                       # Recursos (iconos, logos, layouts)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
└── settings.gradle.kts
```

---

## ⚙️ Instalación y Ejecución

1. **Clonar el repositorio:**
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd Costo_Stock_Spa
   ```

2. **Abrir en Android Studio:**
   - Seleccionar **File > Open...** y elegir la carpeta raíz `Costo_Stock_Spa`.
   - Esperar a que Gradle sincronice las dependencias del proyecto.

3. **Ejecutar la aplicación:**
   - Conectar un dispositivo físico con depuración USB activada o iniciar un emulador con Android 8.0 (API 26) o superior.
   - Presionar el botón **Run** (`Shift + F10` o el ícono verde de reproducción).

---

## 📄 Licencia

Este proyecto fue desarrollado con propósitos académicos dentro del marco de la asignatura de desarrollo móvil.

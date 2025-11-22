package com.example.easyshop.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow // <--- ESTE IMPORT ES EL IMPORTANTE
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.easyshop.pages.CartPage
import com.example.easyshop.pages.FavoritePage
import com.example.easyshop.pages.HomePage
import com.example.easyshop.pages.ProfilePage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// Estructura auxiliar para los items del menú lateral
data class DrawerMenuItem(
    val title: String,
    val icon: ImageVector,
    val action: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Estado para la barra inferior
    var selectedIndex by rememberSaveable { mutableStateOf(0) }

    // Lista de items para el menú lateral
    val drawerItems = listOf(
        DrawerMenuItem("Inicio", Icons.Default.Home, "home"),
        DrawerMenuItem("Mis Pedidos", Icons.Default.List, "orders"),
        DrawerMenuItem("Cerrar Sesión", Icons.Default.ExitToApp, "logout")
    )

    // Estado para saber qué item del menú lateral está seleccionado (visual)
    var selectedDrawerItem by remember { mutableStateOf(drawerItems[0]) }

    // Lista para la barra inferior (BottomBar)
    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Favorite", Icons.Default.Favorite),
        NavItem("Cart", Icons.Default.ShoppingCart),
        NavItem("Profile", Icons.Default.Person),
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerShape = RoundedCornerShape(topEnd = 30.dp, bottomEnd = 30.dp),
                drawerContainerColor = Color.White
            ) {
                // 1. CABECERA CON DEGRADADO
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF002244), Color(0xFF3344CC))
                            )
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3344CC), modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("EasyShop", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = FirebaseAuth.getInstance().currentUser?.email ?: "Invitado",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- ITEMS DEL MENÚ CON ESTILO 3D ---
                drawerItems.forEach { item ->
                    val isSelected = selectedDrawerItem == item && item.action != "logout"

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .fillMaxWidth()
                            .height(50.dp)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        // 1. Sombra brillante (CORREGIDO AQUÍ)
                                        .shadow(
                                            elevation = 6.dp,
                                            shape = RoundedCornerShape(50.dp),
                                            spotColor = Color(0xFFFF3366),
                                            ambientColor = Color(0xFFFF3366)
                                        )
                                        .clip(RoundedCornerShape(50.dp))
                                        // 2. Fondo Degradado
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(Color(0xFFFF758C), Color(0xFFFF0040))
                                            )
                                        )
                                        // 3. Borde de Brillo
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(Color.White.copy(0.5f), Color.Transparent)
                                            ),
                                            shape = RoundedCornerShape(50.dp)
                                        )
                                } else {
                                    Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(Color.Transparent)
                                }
                            )
                            .clickable {
                                scope.launch { drawerState.close() }

                                if (item.action == "logout") {
                                    FirebaseAuth.getInstance().signOut()
                                    navController.navigate("login") { popUpTo(0) }
                                } else {
                                    selectedDrawerItem = item
                                    if (item.action == "orders") navController.navigate("my_orders")
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = item.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.DarkGray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text("v1.0.0", modifier = Modifier.padding(24.dp).align(Alignment.CenterHorizontally), color = Color.LightGray, fontSize = 12.sp)
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "EasyShop",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp // Un poco más grande para que luzca
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    // --- ESTA ES LA PARTE QUE CAMBIA EL COLOR ---
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Black,      // Fondo Negro
                        titleContentColor = Color.White,   // Texto Blanco
                        navigationIconContentColor = Color.White, // Icono Menú Blanco
                        actionIconContentColor = Color.White      // Otros iconos Blancos
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Black, // Fondo Negro para combinar con el estilo Porsche
                    tonalElevation = 0.dp
                ) {
                    navItemList.forEachIndexed { index, navItem ->
                        NavigationBarItem(
                            selected = index == selectedIndex,
                            onClick = { selectedIndex = index },
                            icon = {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = navItem.label
                                )
                            },
                            label = { Text(text = navItem.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF333333), // Círculo gris oscuro al seleccionar
                                selectedIconColor = Color.White,    // Icono blanco
                                selectedTextColor = Color.White,    // Texto blanco
                                unselectedIconColor = Color.Gray,   // Icono gris
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        ) {
            ContentScreen(modifier = modifier.padding(it), selectedIndex)
        }
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex: Int) {
    when (selectedIndex) {
        0 -> HomePage(modifier)
        1 -> FavoritePage(modifier)
        2 -> CartPage(modifier)
        3 -> ProfilePage(modifier)
    }
}

data class NavItem(
    val label: String,
    val icon: ImageVector
)
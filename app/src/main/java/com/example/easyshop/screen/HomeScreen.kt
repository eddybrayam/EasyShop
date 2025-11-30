package com.example.easyshop.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.easyshop.components.DraggableAICharacter
import com.example.easyshop.pages.CartPage
import com.example.easyshop.pages.FavoritePage
import com.example.easyshop.pages.HomePage
import com.example.easyshop.pages.ProfilePage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// PALETA PREMIUM
private object PremiumTheme {
    val DarkBg = Color(0xFF0A0A0A)
    val DarkSurface = Color(0xFF111111)
    val MediumSurface = Color(0xFF1A1A1A)
    val LightSurface = Color(0xFF2A2A2A)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB5B5B5)
    val Divider = Color(0xFF262626)

    val CyanAccent = Color(0xFF00E8FF)
    val CyanLight = Color(0xFF00E8FF).copy(alpha = 0.15f)
}

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
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    val drawerItems = listOf(
        DrawerMenuItem("Inicio", Icons.Default.Home, "home"),
        DrawerMenuItem("Mis Pedidos", Icons.AutoMirrored.Filled.List, "orders"),
        DrawerMenuItem("Cerrar Sesión", Icons.AutoMirrored.Filled.ExitToApp, "logout")
    )
    var selectedDrawerItem by remember { mutableStateOf(drawerItems[0]) }

    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Favorite", Icons.Default.Favorite),
        NavItem("Cart", Icons.Default.ShoppingCart),
        NavItem("Profile", Icons.Default.Person),
    )

    Box(modifier = Modifier.fillMaxSize()) {

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                PremiumDrawerContent(
                    drawerItems = drawerItems,
                    selectedDrawerItem = selectedDrawerItem,
                    onItemClick = { item ->
                        scope.launch { drawerState.close() }
                        if (item.action == "logout") {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("login") { popUpTo(0) }
                        } else {
                            selectedDrawerItem = item
                            if (item.action == "orders") navController.navigate("my_orders")
                        }
                    }
                )
            },
            scrimColor = Color.Black.copy(alpha = 0.7f)
        ) {
            Scaffold(
                topBar = {
                    PremiumTopBar(
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                },
                bottomBar = {
                    PremiumBottomBar(
                        navItemList = navItemList,
                        selectedIndex = selectedIndex,
                        onItemSelected = { selectedIndex = it }
                    )
                },
                containerColor = PremiumTheme.DarkBg
            ) {
                ContentScreen(modifier = modifier.padding(it), selectedIndex)
            }
        }

        // AI CHARACTER FLOTANTE
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 24.dp)
        ) {
            DraggableAICharacter(
                onClick = { navController.navigate("chat_ai") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumTopBar(
    onMenuClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    PremiumTheme.CyanAccent,
                                    PremiumTheme.CyanAccent.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "EasyShop",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = PremiumTheme.TextPrimary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = PremiumTheme.CyanAccent
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = PremiumTheme.DarkSurface,
            titleContentColor = PremiumTheme.TextPrimary,
            navigationIconContentColor = PremiumTheme.CyanAccent,
            actionIconContentColor = PremiumTheme.CyanAccent
        ),
        modifier = Modifier.border(
            width = 1.dp,
            color = PremiumTheme.Divider
        )
    )
}

@Composable
private fun PremiumDrawerContent(
    drawerItems: List<DrawerMenuItem>,
    selectedDrawerItem: DrawerMenuItem,
    onItemClick: (DrawerMenuItem) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = PremiumTheme.DarkBg,
    ) {
        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PremiumTheme.MediumSurface,
                            PremiumTheme.DarkBg
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = PremiumTheme.CyanLight,
                    shape = RoundedCornerShape(bottomEnd = 24.dp)
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Avatar circular con glow
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = PremiumTheme.CyanAccent.copy(alpha = 0.3f)
                        ),
                    shape = CircleShape,
                    color = PremiumTheme.LightSurface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        PremiumTheme.LightSurface,
                                        PremiumTheme.MediumSurface
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = PremiumTheme.CyanAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "EasyShop",
                    color = PremiumTheme.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = FirebaseAuth.getInstance().currentUser?.email ?: "Invitado",
                    color = PremiumTheme.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // MENU ITEMS
        drawerItems.forEach { item ->
            val isSelected = selectedDrawerItem == item && item.action != "logout"

            Surface(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(
                        elevation = if (isSelected) 12.dp else 0.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = if (isSelected) PremiumTheme.CyanAccent.copy(alpha = 0.3f) else Color.Transparent
                    ),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) PremiumTheme.MediumSurface else Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onItemClick(item) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (isSelected) PremiumTheme.CyanAccent else PremiumTheme.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = item.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PremiumTheme.CyanAccent else PremiumTheme.TextSecondary,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // VERSION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "v1.0.0",
                color = PremiumTheme.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PremiumBottomBar(
    navItemList: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = PremiumTheme.Divider
            ),
        containerColor = PremiumTheme.DarkSurface,
        tonalElevation = 0.dp
    ) {
        navItemList.forEachIndexed { index, navItem ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onItemSelected(index) },
                icon = {
                    AnimatedContent(
                        targetState = index == selectedIndex,
                        label = "IconAnimation"
                    ) { selected ->
                        Icon(
                            imageVector = navItem.icon,
                            contentDescription = navItem.label,
                            modifier = Modifier.size(if (selected) 26.dp else 24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = navItem.label,
                        fontSize = 10.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = PremiumTheme.MediumSurface,
                    selectedIconColor = PremiumTheme.CyanAccent,
                    unselectedIconColor = PremiumTheme.TextSecondary,
                    selectedTextColor = PremiumTheme.CyanAccent,
                    unselectedTextColor = PremiumTheme.TextSecondary
                )
            )
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

data class NavItem(val label: String, val icon: ImageVector)

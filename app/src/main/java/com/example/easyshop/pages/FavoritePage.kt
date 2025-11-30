package com.example.easyshop.pages

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.easyshop.AppUtil
import com.example.easyshop.model.ProductModel
import com.example.easyshop.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

// PALETA CYBERPUNK EXTREMA
private object CyberpunkColors {
    val PitchBlack = Color(0xFF000000)
    val DeepDark = Color(0xFF0D0D0D)
    val DarkGray = Color(0xFF1A1A1A)
    val SurfaceDark = Color(0xFF242424)
    val TextWhite = Color(0xFFFAFAFA)
    val TextGray = Color(0xFFAAAAAA)
    val GridBorder = Color(0xFF2F2F2F)

    // Cyberpunk: Neon Rosa + Cyan
    val NeonPink = Color(0xFFFF006E)
    val NeonCyan = Color(0xFF00F5FF)
    val NeonGreen = Color(0xFF39FF14)
    val AccentPrimary = NeonCyan
    val AccentSecondary = NeonPink
}

@Composable
fun FavoritePage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val favoriteProducts = remember { mutableStateListOf<ProductModel>() }
    var isLoading by remember { mutableStateOf(true) }
    val auth = FirebaseAuth.getInstance()

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            Firebase.firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    val user = snapshot?.toObject(UserModel::class.java)
                    val favIds = user?.favoriteItems ?: emptyList()

                    if (favIds.isEmpty()) {
                        favoriteProducts.clear()
                        isLoading = false
                    } else {
                        Firebase.firestore.collection("data").document("stock")
                            .collection("products")
                            .whereIn("id", favIds)
                            .get()
                            .addOnSuccessListener { productsSnapshot ->
                                val products = productsSnapshot.toObjects(ProductModel::class.java)
                                favoriteProducts.clear()
                                favoriteProducts.addAll(products)
                                isLoading = false
                            }
                    }
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberpunkColors.PitchBlack)
    ) {
        // CYBER HEADER
        CyberTopBar()

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            AnimatedContent(targetState = isLoading, label = "Header") { loading ->
                if (!loading) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(32.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                CyberpunkColors.AccentPrimary,
                                                CyberpunkColors.AccentSecondary
                                            )
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "FAVORITOS",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CyberpunkColors.TextWhite,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "◆ ${favoriteProducts.size} items guardados",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberpunkColors.AccentPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> CyberLoadingState()
                favoriteProducts.isEmpty() -> CyberEmptyState()
                else -> CyberProductsGrid(
                    products = favoriteProducts,
                    onRemoveClick = { productId ->
                        AppUtil.toggleFavorite(context, productId)
                    },
                    onAddToCart = { productId ->
                        AppUtil.addToCart(context, productId)
                    }
                )
            }
        }
    }
}

@Composable
private fun CyberTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyberpunkColors.SurfaceDark.copy(alpha = 0.6f),
                        CyberpunkColors.DeepDark
                    )
                )
            )
            .border(
                width = 1.dp,
                color = CyberpunkColors.AccentPrimary.copy(alpha = 0.2f)
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = ">>>",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = CyberpunkColors.AccentPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "COLLECTION",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyberpunkColors.TextWhite,
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
private fun CyberLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                CyberpunkColors.AccentPrimary,
                                CyberpunkColors.AccentSecondary,
                                CyberpunkColors.AccentPrimary
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = CyberpunkColors.AccentPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "[ CARGANDO ]",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CyberpunkColors.AccentPrimary,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun CyberEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    CyberpunkColors.AccentPrimary,
                                    CyberpunkColors.AccentSecondary
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = CyberpunkColors.AccentSecondary,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "[ SIN FAVORITOS ]",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyberpunkColors.TextWhite,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "/ Agrega productos a tu colección",
                    fontSize = 13.sp,
                    color = CyberpunkColors.TextGray,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(48.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    CyberpunkColors.AccentPrimary,
                                    CyberpunkColors.AccentSecondary
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    CyberpunkColors.AccentPrimary.copy(alpha = 0.1f),
                                    CyberpunkColors.AccentSecondary.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "[EXPLORAR]",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberpunkColors.AccentPrimary,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CyberProductsGrid(
    products: List<ProductModel>,
    onRemoveClick: (String) -> Unit,
    onAddToCart: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(
                items = products,
                key = { it.id }
            ) { product ->
                CyberFavoriteItem(
                    product = product,
                    onRemoveClick = { onRemoveClick(product.id) },
                    onAddToCart = { onAddToCart(product.id) }
                )
            }
        }
    }
}

@Composable
private fun CyberFavoriteItem(
    product: ProductModel,
    onRemoveClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        CyberpunkColors.AccentPrimary.copy(
                            alpha = if (isHovered) 1f else 0.3f
                        ),
                        CyberpunkColors.AccentSecondary.copy(
                            alpha = if (isHovered) 1f else 0.3f
                        )
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyberpunkColors.DarkGray.copy(
                            alpha = if (isHovered) 0.8f else 0.4f
                        ),
                        CyberpunkColors.DeepDark
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { isHovered = !isHovered }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // IMAGE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(CyberpunkColors.DeepDark)
            ) {
                AsyncImage(
                    model = product.images.firstOrNull() ?: "",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                if (isHovered) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        CyberpunkColors.AccentPrimary.copy(alpha = 0.15f),
                                        CyberpunkColors.AccentSecondary.copy(alpha = 0.15f)
                                    )
                                )
                            )
                    )
                }
            }

            // CONTENT
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.title.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = CyberpunkColors.TextWhite,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$${product.actualPrice}",
                            color = CyberpunkColors.AccentPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        if (product.price > product.actualPrice) {
                            Text(
                                text = "${product.price}",
                                color = CyberpunkColors.TextGray,
                                fontSize = 10.sp,
                                style = TextStyle(
                                    textDecoration = TextDecoration.LineThrough
                                ),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .border(
                                width = 1.5.dp,
                                color = CyberpunkColors.AccentSecondary,
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .clickable { onRemoveClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = CyberpunkColors.AccentSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // CTA BUTTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    CyberpunkColors.AccentPrimary,
                                    CyberpunkColors.AccentSecondary
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    CyberpunkColors.AccentPrimary.copy(alpha = 0.2f),
                                    CyberpunkColors.AccentSecondary.copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAddToCart() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = CyberpunkColors.AccentPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "[AGREGAR]",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberpunkColors.AccentPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
package com.example.easyshop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// PALETA PREMIUM
private object BannerColors {
    val Black = Color(0xFF000000)
    val DarkBg = Color(0xFF0A0A0A)
    val DarkSurface = Color(0xFF111111)
    val MediumSurface = Color(0xFF1A1A1A)
    val LightSurface = Color(0xFF2A2A2A)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB5B5B5)
    val Divider = Color(0xFF262626)

    val CyanAccent = Color(0xFF00E8FF)
    val CyanLight = Color(0xFF00E8FF).copy(alpha = 0.15f)
    val CyanMuted = Color(0xFF00E8FF).copy(alpha = 0.3f)
}

@Composable
fun BannerView(modifier: Modifier = Modifier) {
    var bannerList by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("data")
            .document("banners")
            .get()
            .addOnCompleteListener {
                bannerList = it.result.get("urls") as List<String>
            }
    }

    Column(
        modifier = modifier
    ) {
        val pagerState = rememberPagerState(initialPage = 0) {
            bannerList.size
        }

        // PAGER CON BANNERS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = BannerColors.CyanAccent.copy(alpha = 0.15f)
                )
        ) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = BannerColors.DarkSurface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = BannerColors.Divider,
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        AsyncImage(
                            model = bannerList[page],
                            contentDescription = "Banner image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Overlay gradiente sutil para profundidad
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.2f)
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // INDICADORES PREMIUM
        if (bannerList.size > 1) {
            PremiumDotsIndicator(
                dotCount = bannerList.size,
                currentPage = pagerState.currentPage
            )
        }
    }
}

@Composable
private fun PremiumDotsIndicator(
    dotCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val isSelected = currentPage == index

            Surface(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .then(
                        if (isSelected) {
                            Modifier
                                .width(24.dp)
                                .height(8.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(4.dp),
                                    spotColor = BannerColors.CyanAccent.copy(alpha = 0.5f)
                                )
                        } else {
                            Modifier.size(8.dp)
                        }
                    ),
                shape = if (isSelected) RoundedCornerShape(4.dp) else CircleShape,
                color = if (isSelected) BannerColors.CyanAccent else BannerColors.LightSurface
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        BannerColors.CyanAccent,
                                        BannerColors.CyanAccent.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}
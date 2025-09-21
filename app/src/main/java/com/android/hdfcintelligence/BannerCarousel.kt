package com.android.hdfcintelligence

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun BannerCarousel(banners: List<BannerModel>) {
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll logic
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000) // 4-second delay
            val nextPage = (pagerState.currentPage + 1) % banners.size
            coroutineScope.launch {
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            count = banners.size,
            state = pagerState,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        ) { page ->
            BannerItem(banner = banners[page])
        }

        // Pager Indicator (dots)
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            activeColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun BannerItem(banner: BannerModel) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val image: Painter = painterResource(id = banner.imageRes)

            Image(
                painter = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(text = banner.title, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = banner.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)

                Button(
                    onClick = { /* TODO: Handle Click */ },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = banner.actionText)
                }
            }
        }
    }
}

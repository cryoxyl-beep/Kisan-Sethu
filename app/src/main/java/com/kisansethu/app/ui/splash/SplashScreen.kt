package com.kisansethu.app.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kisansethu.app.ui.theme.KisanSethuTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000),
        )
    }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000),
        )
        delay(1500.milliseconds) // Keep the splash screen visible briefly (total ~2.5s)
        onSplashFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Rounded.Eco,
            contentDescription = "Kissaan Sync Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(120.dp)
                .alpha(alpha.value)
                .scale(scale.value),
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Kissaan Sync",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Smarter procurement. Simpler for farmers.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value),
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun SplashScreenPreviewSmall() {
    KisanSethuTheme(dynamicColor = false) {
        SplashScreen {}
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7_PRO)
@Composable
fun SplashScreenPreviewNormal() {
    KisanSethuTheme(dynamicColor = false) {
        SplashScreen {}
    }
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
fun SplashScreenPreviewLarge() {
    KisanSethuTheme(dynamicColor = false) {
        SplashScreen {}
    }
}

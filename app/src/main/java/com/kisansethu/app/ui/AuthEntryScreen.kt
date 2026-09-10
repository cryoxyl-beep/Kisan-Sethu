package com.kisansethu.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kisansethu.app.R
import com.kisansethu.app.ui.theme.KisanSethuTheme
import com.kisansethu.app.ui.theme.SecondaryTextColor

private val ScreenBg = Color(0xFFF7F8F6)
private val AccentSage = Color(0xFFD7E5DB)
private val BrandDark = Color(0xFF2F5A3F)
private val BrandMid = Color(0xFF5A8A68)

@Composable
fun AuthEntryScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegistration: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onToggleTheme: (() -> Unit)? = null
) {
    val background = if (isDarkTheme) MaterialTheme.colorScheme.background else ScreenBg
    val titleColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else BrandDark
    val subtitleColor = if (isDarkTheme) MaterialTheme.colorScheme.onSurfaceVariant else SecondaryTextColor
    val blobColor = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else AccentSage

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
    ) {
        SoftWaveBackground(color = blobColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // Top brand + theme toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LeafLogo(modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = titleColor,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("Kissaan ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = BrandMid,
                                    fontWeight = FontWeight.Medium
                                )
                            ) {
                                append("Sync.")
                            }
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp
                    )
                }

                if (onToggleTheme != null) {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme",
                            tint = titleColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Center welcome copy
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.auth_entry_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.auth_entry_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = subtitleColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else BrandDark,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.btn_login),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.auth_entry_new_user_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                val outlineColor = if (isDarkTheme) MaterialTheme.colorScheme.primary else BrandDark
                OutlinedButton(
                    onClick = onNavigateToRegistration,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.5.dp, outlineColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = outlineColor,
                        containerColor = Color.Transparent
                    )
                ) {
                    Text(
                        text = stringResource(R.string.btn_create_account),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SoftWaveBackground(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Top-right organic blob
        val topPath = Path().apply {
            moveTo(w * 0.42f, 0f)
            cubicTo(w * 0.55f, h * 0.08f, w * 0.72f, h * 0.02f, w, h * 0.16f)
            lineTo(w, 0f)
            close()
        }
        drawPath(topPath, color = color.copy(alpha = 0.55f))

        val topInner = Path().apply {
            moveTo(w * 0.58f, 0f)
            cubicTo(w * 0.68f, h * 0.06f, w * 0.82f, h * 0.04f, w, h * 0.10f)
            lineTo(w, 0f)
            close()
        }
        drawPath(topInner, color = color.copy(alpha = 0.75f))

        // Bottom-left organic blob
        val bottomPath = Path().apply {
            moveTo(0f, h * 0.72f)
            cubicTo(w * 0.18f, h * 0.68f, w * 0.28f, h * 0.88f, w * 0.45f, h)
            lineTo(0f, h)
            close()
        }
        drawPath(bottomPath, color = color.copy(alpha = 0.50f))

        val bottomInner = Path().apply {
            moveTo(0f, h * 0.82f)
            cubicTo(w * 0.12f, h * 0.80f, w * 0.20f, h * 0.92f, w * 0.30f, h)
            lineTo(0f, h)
            close()
        }
        drawPath(bottomInner, color = color.copy(alpha = 0.70f))
    }
}

@Composable
private fun LeafLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val leftLeaf = Path().apply {
            moveTo(w * 0.48f, h * 0.15f)
            cubicTo(w * 0.10f, h * 0.25f, w * 0.05f, h * 0.70f, w * 0.42f, h * 0.92f)
            cubicTo(w * 0.55f, h * 0.70f, w * 0.58f, h * 0.35f, w * 0.48f, h * 0.15f)
            close()
        }
        drawPath(leftLeaf, color = BrandDark)

        val rightLeaf = Path().apply {
            moveTo(w * 0.52f, h * 0.10f)
            cubicTo(w * 0.95f, h * 0.22f, w * 0.98f, h * 0.65f, w * 0.58f, h * 0.88f)
            cubicTo(w * 0.48f, h * 0.65f, w * 0.42f, h * 0.30f, w * 0.52f, h * 0.10f)
            close()
        }
        drawPath(rightLeaf, color = BrandMid)

        // Center stem hint
        drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(w * 0.50f, h * 0.28f),
            end = Offset(w * 0.50f, h * 0.78f),
            strokeWidth = 1.5f
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AuthEntryScreenPreview() {
    KisanSethuTheme(dynamicColor = false) {
        AuthEntryScreen(
            onNavigateToLogin = {},
            onNavigateToRegistration = {},
            onToggleTheme = {}
        )
    }
}

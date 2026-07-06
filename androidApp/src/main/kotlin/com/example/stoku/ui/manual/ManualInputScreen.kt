package com.example.stoku.ui.manual

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.stoku.ui.navigation.Routes
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.DangerBg
import com.example.stoku.ui.theme.DangerRed
import com.example.stoku.ui.theme.GreenBg
import com.example.stoku.ui.theme.GreenDark
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.TextMuted

@Composable
fun ManualInputScreen(navController: NavHostController) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
        ) {
            Text(
                text = "Input Manual",
                style = MaterialTheme.typography.headlineLarge,
                color = Ink,
                modifier = Modifier.padding(top = 20.dp, bottom = 24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BigActionCard(
                    symbol = "↓",
                    symbolColor = GreenDark,
                    symbolBg = GreenBg,
                    title = "Input Masuk",
                    subtitle = "Catat stok masuk secara manual",
                    onClick = { navController.navigate(Routes.MANUAL_IN) },
                )
                BigActionCard(
                    symbol = "↑",
                    symbolColor = DangerRed,
                    symbolBg = DangerBg,
                    title = "Input Keluar",
                    subtitle = "Catat stok keluar secara manual",
                    onClick = { navController.navigate(Routes.MANUAL_OUT) },
                )
            }
        }
    }
}

@Composable
private fun BigActionCard(
    symbol: String,
    symbolColor: androidx.compose.ui.graphics.Color,
    symbolBg: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(color = symbolBg, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = symbol,
                        color = symbolColor,
                        fontSize = 26.sp,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = Ink)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
        }
    }
}

package com.example.stoku.ui.main

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.stoku.domain.model.UserRole
import com.example.stoku.ui.navigation.RouteAccess
import com.example.stoku.ui.navigation.Routes
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.GreenBg
import com.example.stoku.ui.theme.GreenDark
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.CashAccent
import com.example.stoku.ui.theme.CashBg
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.SurfaceAlt
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextPlaceholder
import com.example.stoku.ui.theme.TextSecondary

private data class NavItem(val route: String, val label: String, val symbol: String)

// Fixed 2-left + FAB + 2-right layout so the FAB is always at exact screen center.
// Settings is not in the bottom bar — it's accessed via the profile avatar.
private val leftNavItems = listOf(
    NavItem(Routes.DASHBOARD, "Beranda", "⌂"),
    NavItem(Routes.STOCK_LIST, "Stok", "▦"),
)
private val rightNavItems = listOf(
    NavItem(Routes.STATISTICS, "Statistik", "▤"),
    NavItem(Routes.HISTORY, "Riwayat", "↺"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    currentRoute: String,
    role: UserRole,
    content: @Composable (Modifier) -> Unit,
) {
    var showScanSheet by remember { mutableStateOf(false) }
    val canManage = role != UserRole.KASIR
    val navigate: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(Routes.DASHBOARD) { inclusive = false }
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            StokuBottomBar(
                currentRoute = currentRoute,
                role = role,
                onFabClick = { if (canManage) showScanSheet = true else navigate(Routes.KASIR) },
                onNavigate = navigate,
            )
        },
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }

    if (showScanSheet) {
        ModalBottomSheet(
            onDismissRequest = { showScanSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            ScanActionSheetContent(
                onScanMasuk = { showScanSheet = false; navigate(Routes.SCAN_IN) },
                onInputManual = { showScanSheet = false; navigate(Routes.MANUAL_INPUT) },
                onModeKasir = { showScanSheet = false; navigate(Routes.KASIR) },
            )
        }
    }
}

@Composable
private fun ScanActionSheetContent(
    onScanMasuk: () -> Unit,
    onInputManual: () -> Unit,
    onModeKasir: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(bottom = 26.dp),
    ) {
        Text(
            text = "Aksi",
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            modifier = Modifier.padding(bottom = 14.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            ScanSheetAction(
                symbol = "↓",
                symbolColor = GreenDark,
                symbolBg = GreenBg,
                title = "Scan Barang Masuk",
                subtitle = "Tambah stok via barcode",
                onClick = onScanMasuk,
            )
            ScanSheetAction(
                symbol = "✎",
                symbolColor = TextSecondary,
                symbolBg = SurfaceAlt,
                title = "Input Manual",
                subtitle = "Tanpa scan barcode",
                onClick = onInputManual,
            )
            ScanSheetAction(
                symbol = "Rp",
                symbolColor = CashAccent,
                symbolBg = CashBg,
                title = "Mode Kasir",
                subtitle = "Keranjang multi-item + bayar",
                onClick = onModeKasir,
            )
        }
    }
}

@Composable
private fun ScanSheetAction(
    symbol: String,
    symbolColor: Color,
    symbolBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = SurfaceAlt,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Surface(color = symbolBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = symbol, color = symbolColor, fontSize = if (symbol == "Rp") 13.sp else 18.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

@Composable
private fun StokuBottomBar(
    currentRoute: String,
    role: UserRole,
    onFabClick: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    // Outer Box does NOT clip, so the FAB can draw above its top edge freely.
    // The bar Surface and the FAB Surface are siblings here — the bar Surface only
    // clips its own nav-item Row, never the FAB.
    Box(modifier = Modifier.fillMaxWidth()) {
        // Bar background + border + nav items
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = BorderSubtle, shape = RoundedCornerShape(0.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leftNavItems.forEach { item ->
                        NavSlot(
                            item = item,
                            selected = currentRoute == item.route,
                            onClick = { onNavigate(item.route) },
                        )
                    }

                    // Center slot — empty placeholder keeping the FAB at exact center
                    Box(modifier = Modifier.weight(1f))

                    // Allowed right items first (adjacent to FAB), spacers at the outer edge.
                    // For Kasir: [Riwayat][spacer] instead of [spacer][Riwayat].
                    val allowedRight = rightNavItems.filter { RouteAccess.isAllowed(it.route, role) }
                    allowedRight.forEach { item ->
                        NavSlot(
                            item = item,
                            selected = currentRoute == item.route,
                            onClick = { onNavigate(item.route) },
                        )
                    }
                    repeat(rightNavItems.size - allowedRight.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // FAB + label — sibling of the bar Surface so it is NOT clipped by it.
        // The outer Box doesn't clip, so offset(y = -14.dp) draws above Box top without being cut.
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                onClick = onFabClick,
                color = GreenPrimary,
                shape = CircleShape,
                modifier = Modifier.size(52.dp),
                shadowElevation = 8.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "⊡", color = Color.White, fontSize = 22.sp)
                }
            }
            Text(
                text = if (role == UserRole.KASIR) "Kasir" else "Scan",
                style = MaterialTheme.typography.labelSmall,
                color = GreenDark,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun RowScope.NavSlot(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Text(
                text = item.symbol,
                fontSize = 18.sp,
                color = if (selected) GreenDark else TextPlaceholder,
            )
        },
        label = {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) GreenDark else TextPlaceholder,
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = GreenDark,
            selectedTextColor = GreenDark,
            unselectedIconColor = TextPlaceholder,
            unselectedTextColor = TextPlaceholder,
            indicatorColor = GreenBg,
        ),
        modifier = Modifier.weight(1f),
    )
}

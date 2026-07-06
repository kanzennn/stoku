package com.example.stoku.ui.scan

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.stoku.ui.product.AddNewProductForm
import com.example.stoku.ui.product.RestockForm
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.ScanBackground
import com.example.stoku.ui.theme.ScanGreen
import com.example.stoku.ui.theme.TextPlaceholder

@Composable
fun ScanMasukScreen(
    navController: NavHostController,
    viewModel: ScanMasukViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = ScanBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val mode = uiState.mode) {
                is ScanMasukMode.Scanning -> ScanCameraStage(
                    title = "Scan Barang Masuk",
                    onBack = { navController.popBackStack() },
                    onBarcodeScanned = viewModel::onBarcodeScanned,
                )

                is ScanMasukMode.Loading -> Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = ScanGreen)
                }

                is ScanMasukMode.NewProduct -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    Column {
                        ScanSubHeader(title = "Scan Barang Masuk", onBack = { viewModel.scanAgain() })
                        AddNewProductForm(
                            sku = mode.sku,
                            skuEditable = false,
                            showNotesField = false,
                            errorMessage = uiState.errorMessage,
                            isSubmitting = uiState.isSubmitting,
                            onSubmit = { sku, brand, productName, category, cost, sell, qty, notes ->
                                viewModel.submitNewProduct(sku, brand, productName, category, cost, sell, qty, notes)
                            },
                            onCancel = viewModel::scanAgain,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                is ScanMasukMode.Restock -> Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    Column {
                        ScanSubHeader(title = "Scan Barang Masuk", onBack = { viewModel.scanAgain() })
                        RestockForm(
                            product = mode.product,
                            showNotesField = false,
                            errorMessage = uiState.errorMessage,
                            isSubmitting = uiState.isSubmitting,
                            onSubmit = { cost, sell, qty, notes -> viewModel.submitRestock(mode.product.sku, cost, sell, qty, notes) },
                            onCancel = viewModel::scanAgain,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScanCameraStage(
    title: String,
    onBack: () -> Unit,
    onBarcodeScanned: (String) -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize().background(ScanBackground)) {
        // Real camera preview behind the overlay
        BarcodeScanner(onBarcodeScanned = onBarcodeScanned, modifier = Modifier.fillMaxSize())

        // Semi-transparent overlay
        Box(modifier = Modifier.fillMaxSize().background(Color(0x99111317)))

        // Header
        ScanSubHeader(title = title, onBack = onBack, darkTheme = true)

        // Viewfinder
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.15f))
            ScanViewfinder(size = 240.dp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Arahkan kamera ke barcode produk",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPlaceholder,
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ScanSubHeader(title: String, onBack: () -> Unit, darkTheme: Boolean = false) {
    val textColor = if (darkTheme) Color.White else Ink
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onBack,
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    "‹",
                    style = MaterialTheme.typography.headlineLarge,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
            Text(title, style = MaterialTheme.typography.headlineLarge, color = textColor)
        }
    }
}

@Composable
private fun ScanViewfinder(size: Dp) {
    val cornerLength = 24.dp
    val strokeWidth = 3.dp

    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.84f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scanlineY",
    )

    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cl = cornerLength.toPx()
            val sw = strokeWidth.toPx()
            val color = ScanGreen

            // Top-left
            drawLine(color, Offset(0f, cl), Offset(0f, 0f), sw, StrokeCap.Round)
            drawLine(color, Offset(0f, 0f), Offset(cl, 0f), sw, StrokeCap.Round)
            // Top-right
            drawLine(color, Offset(w - cl, 0f), Offset(w, 0f), sw, StrokeCap.Round)
            drawLine(color, Offset(w, 0f), Offset(w, cl), sw, StrokeCap.Round)
            // Bottom-left
            drawLine(color, Offset(0f, h - cl), Offset(0f, h), sw, StrokeCap.Round)
            drawLine(color, Offset(0f, h), Offset(cl, h), sw, StrokeCap.Round)
            // Bottom-right
            drawLine(color, Offset(w - cl, h), Offset(w, h), sw, StrokeCap.Round)
            drawLine(color, Offset(w, h - cl), Offset(w, h), sw, StrokeCap.Round)

            // Animated scan line
            val lineY = scanlineY * h
            drawLine(
                color = ScanGreen.copy(alpha = 0.75f),
                start = Offset(0f, lineY),
                end = Offset(w, lineY),
                strokeWidth = sw * 0.8f,
                cap = StrokeCap.Round,
            )
        }
    }
}

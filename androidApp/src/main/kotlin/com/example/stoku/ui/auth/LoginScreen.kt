package com.example.stoku.ui.auth

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stoku.domain.model.UserRole
import com.example.stoku.ui.theme.BorderInput
import com.example.stoku.ui.theme.BlueScan
import com.example.stoku.ui.theme.BlueScanBg
import com.example.stoku.ui.theme.GreenBg
import com.example.stoku.ui.theme.GreenDark
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.PurpleManual
import com.example.stoku.ui.theme.PurpleManualBg
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextPlaceholder
import com.example.stoku.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .run { this },
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    color = GreenPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(56.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "V",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "StokVape",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                color = Ink,
            )
            Text(
                text = "Manajemen inventori toko vape",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(top = 6.dp, bottom = 34.dp),
            )

            Text(
                text = "Username",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(7.dp))
            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChange,
                placeholder = { Text("username", color = TextPlaceholder) },
                singleLine = true,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderInput,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Password",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Spacer(modifier = Modifier.height(7.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = { Text("••••••", color = TextPlaceholder) },
                singleLine = true,
                enabled = !uiState.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.login(onLoginSuccess) }),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderInput,
                ),
            )

            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = { viewModel.login(onLoginSuccess) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Masuk", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderInput)
                Text(
                    text = "DEMO — pilih peran",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPlaceholder,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderInput)
            }

            Spacer(modifier = Modifier.height(16.dp))

            DemoRoleButton(
                initials = "BD",
                name = "Budi",
                role = "Owner · akses penuh",
                avatarBg = GreenBg,
                avatarColor = GreenDark,
                enabled = !uiState.isLoading,
                onClick = { viewModel.quickLogin("owner", "owner123", onLoginSuccess) },
            )
            Spacer(modifier = Modifier.height(9.dp))
            DemoRoleButton(
                initials = "SR",
                name = "Sari",
                role = "Admin · kelola inventori",
                avatarBg = BlueScanBg,
                avatarColor = BlueScan,
                enabled = !uiState.isLoading,
                onClick = { viewModel.quickLogin("admin", "admin123", onLoginSuccess) },
            )
            Spacer(modifier = Modifier.height(9.dp))
            DemoRoleButton(
                initials = "DW",
                name = "Dewi",
                role = "Kasir · scan keluar",
                avatarBg = PurpleManualBg,
                avatarColor = PurpleManual,
                enabled = !uiState.isLoading,
                onClick = { viewModel.quickLogin("kasir", "kasir123", onLoginSuccess) },
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DemoRoleButton(
    initials: String,
    name: String,
    role: String,
    avatarBg: Color,
    avatarColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(13.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderInput, RoundedCornerShape(13.dp)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp, 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = avatarBg,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials,
                        color = avatarColor,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleSmall, color = Ink)
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
            Text(text = "›", color = TextPlaceholder, style = MaterialTheme.typography.titleLarge)
        }
    }
}

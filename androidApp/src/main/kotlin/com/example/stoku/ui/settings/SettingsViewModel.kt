package com.example.stoku.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.User
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.usecase.auth.GetLoggedInUserUseCase
import com.example.stoku.domain.usecase.auth.LogoutUseCase
import com.example.stoku.domain.usecase.settings.GetGlobalLowStockThresholdUseCase
import com.example.stoku.domain.usecase.settings.SetGlobalLowStockThresholdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val role: UserRole? = null,
    val globalLowStockThreshold: Int = 5,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val loggedOut: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getLoggedInUserUseCase: GetLoggedInUserUseCase,
    private val getGlobalLowStockThresholdUseCase: GetGlobalLowStockThresholdUseCase,
    private val setGlobalLowStockThresholdUseCase: SetGlobalLowStockThresholdUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var cachedUser: User? = null

    init {
        viewModelScope.launch {
            val user = currentUser()
            _uiState.update { it.copy(role = user.role) }
        }
        viewModelScope.launch {
            getGlobalLowStockThresholdUseCase().collect { threshold ->
                _uiState.update { it.copy(globalLowStockThreshold = threshold) }
            }
        }
    }

    fun saveThreshold(threshold: Int) {
        viewModelScope.launch {
            val user = currentUser()
            setGlobalLowStockThresholdUseCase(user.role, threshold).fold(
                onSuccess = { _uiState.update { it.copy(successMessage = "Ambang stok rendah disimpan", errorMessage = null) } },
                onFailure = { error -> _uiState.update { it.copy(errorMessage = error.message ?: "Gagal menyimpan") } },
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update { it.copy(loggedOut = true) }
        }
    }

    private suspend fun currentUser(): User {
        cachedUser?.let { return it }
        val user = requireNotNull(getLoggedInUserUseCase().first()) { "No logged-in user" }
        cachedUser = user
        return user
    }
}

package com.example.stoku.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.User
import com.example.stoku.domain.usecase.auth.GetLoggedInUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface SessionState {
    data object Loading : SessionState
    data class LoggedIn(val user: User) : SessionState
    data object LoggedOut : SessionState
}

@HiltViewModel
class NavGraphViewModel @Inject constructor(
    getLoggedInUserUseCase: GetLoggedInUserUseCase,
) : ViewModel() {
    val sessionState: StateFlow<SessionState> = getLoggedInUserUseCase()
        .map { user -> if (user != null) SessionState.LoggedIn(user) else SessionState.LoggedOut }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState.Loading)
}

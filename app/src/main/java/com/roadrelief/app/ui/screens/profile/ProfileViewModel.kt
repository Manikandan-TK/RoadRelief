package com.roadrelief.app.ui.screens.profile

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadrelief.app.KEY_FIRST_LAUNCH_COMPLETE
import com.roadrelief.app.data.database.dao.UserDao
import com.roadrelief.app.data.database.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val address: String = "",
    val vehicleNumber: String = ""
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDao: UserDao,
    private val sharedPreferences: SharedPreferences // Added SharedPreferences
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // SharedFlow for navigation event
    private val _navigateToHome = MutableSharedFlow<Unit>()
    val navigateToHome = _navigateToHome.asSharedFlow()

    init {
        viewModelScope.launch {
            userDao.getUser().collect { user ->
                _user.value = user
                user?.let {
                    _uiState.value = ProfileUiState(
                        name = it.name,
                        address = it.address,
                        vehicleNumber = it.vehicleNumber
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onVehicleNumberChange(vehicleNumber: String) {
        _uiState.update { it.copy(vehicleNumber = vehicleNumber) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val userToSave = _user.value?.copy(
                name = currentState.name,
                address = currentState.address,
                vehicleNumber = currentState.vehicleNumber
            ) ?: UserEntity(
                name = currentState.name,
                address = currentState.address,
                vehicleNumber = currentState.vehicleNumber
            )
            userDao.insertOrUpdateUser(userToSave)
            // Set first launch complete flag
            sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH_COMPLETE, true).apply()
            // Emit navigation event
            _navigateToHome.emit(Unit)
        }
    }
}
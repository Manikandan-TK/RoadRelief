package com.roadrelief.app.ui.screens.profile

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadrelief.app.AppConstants.KEY_FIRST_LAUNCH_COMPLETE
import com.roadrelief.app.data.database.dao.UserDao
import com.roadrelief.app.data.database.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileScreenObservedState(
    val name: String = "",
    val address: String = "",
    val vehicleNumber: String = "",
    val isInitialSetup: Boolean = true // True if profile setup is mandatory (first launch incomplete)
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDao: UserDao,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)

    private val _uiState = MutableStateFlow(ProfileScreenObservedState())
    val uiState: StateFlow<ProfileScreenObservedState> = _uiState.asStateFlow()

    private val _navigateToHome = MutableSharedFlow<Unit>()
    val navigateToHome = _navigateToHome.asSharedFlow()

    init {
        val isFirstLaunchActuallyComplete = sharedPreferences.getBoolean(KEY_FIRST_LAUNCH_COMPLETE, false)
        _uiState.update { it.copy(isInitialSetup = !isFirstLaunchActuallyComplete) }

        userDao.getUser().onEach { user ->
            _user.value = user
            _uiState.update { currentState ->
                currentState.copy(
                    name = user?.name ?: "",
                    address = user?.address ?: "",
                    vehicleNumber = user?.vehicleNumber ?: ""
                )
            }
        }.launchIn(viewModelScope)
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
                name = currentState.name.trim(),
                address = currentState.address.trim(),
                vehicleNumber = currentState.vehicleNumber.trim()
            ) ?: UserEntity(
                name = currentState.name.trim(),
                address = currentState.address.trim(),
                vehicleNumber = currentState.vehicleNumber.trim()
            )
            if (userToSave.name.isNotEmpty() || userToSave.address.isNotEmpty() || userToSave.vehicleNumber.isNotEmpty() || _user.value == null) {
                userDao.insertOrUpdateUser(userToSave)
            }

            // Update SharedPreferences first, using commit() for a synchronous, guaranteed save.
            sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH_COMPLETE, true).commit()
            // Then update the ViewModel's state to reflect completion
            _uiState.update { it.copy(isInitialSetup = false) }
            // Then navigate
            _navigateToHome.emit(Unit)
        }
    }

    fun skipProfile() {
        viewModelScope.launch {
            if (_user.value == null) {
                userDao.insertOrUpdateUser(UserEntity(name = "", address = "", vehicleNumber = ""))
            }
            // Update SharedPreferences first, using commit() for a synchronous, guaranteed save.
            sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH_COMPLETE, true).commit()
            // Then update the ViewModel's state to reflect completion
            _uiState.update { it.copy(isInitialSetup = false) }
            // Then navigate
            _navigateToHome.emit(Unit)
        }
    }
}

package com.roadrelief.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadrelief.app.data.database.dao.UserDao
import com.roadrelief.app.data.database.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            userDao.getUser().collect { user ->
                _user.value = user
            }
        }
    }

    fun saveUser(name: String, address: String, vehicleNumber: String) {
        viewModelScope.launch {
            val currentUser = _user.value
            val userToSave = if (currentUser == null) {
                UserEntity(name = name, address = address, vehicleNumber = vehicleNumber)
            } else {
                currentUser.copy(name = name, address = address, vehicleNumber = vehicleNumber)
            }
            userDao.insertOrUpdateUser(userToSave)
        }
    }
}

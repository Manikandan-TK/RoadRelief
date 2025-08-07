package com.roadrelief.app.ui.screens.profile

import com.roadrelief.app.data.database.dao.UserDao
import com.roadrelief.app.data.database.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var userDao: UserDao
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userDao = mock()
        viewModel = ProfileViewModel(userDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state is loaded from dao`() = runTest {
        // Given
        val user = UserEntity(id = 1, name = "John Doe", address = "123 Main St", vehicleNumber = "ABC-123")
        whenever(userDao.getUser()).thenReturn(flowOf(user))

        // When
        viewModel = ProfileViewModel(userDao)

        // Then
        val profileState = viewModel.profileState.first()
        assertEquals("John Doe", profileState.name)
        assertEquals("123 Main St", profileState.address)
        assertEquals("ABC-123", profileState.vehicleNumber)
    }

    @Test
    fun `test save profile calls dao`() = runTest {
        // Given
        val name = "Jane Doe"
        val address = "456 Oak Ave"
        val vehicleNumber = "XYZ-789"
        viewModel.profileState.value = viewModel.profileState.value.copy(
            name = name,
            address = address,
            vehicleNumber = vehicleNumber
        )

        // When
        viewModel.saveProfile()

        // Then
        val expectedUser = UserEntity(id = 0, name = name, address = address, vehicleNumber = vehicleNumber)
        verify(userDao).insert(expectedUser)
    }
}
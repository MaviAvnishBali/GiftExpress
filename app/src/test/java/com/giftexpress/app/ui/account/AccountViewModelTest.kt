package com.giftexpress.app.ui.account

import com.giftexpress.app.data.repository.AuthRepository
import com.giftexpress.app.utils.NetworkResult
import com.giftexpress.app.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AccountViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock(AuthRepository::class.java)
        `when`(authRepository.getCurrentUser()).thenReturn(flowOf(null))

        viewModel = AccountViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deleteAccount without password calls authRepository and succeeds`() = runTest(testDispatcher) {
        `when`(authRepository.deleteAccount(null)).thenReturn(NetworkResult.Success(true))

        viewModel.deleteAccount()
        advanceUntilIdle()

        verify(authRepository).deleteAccount(null)
        assertTrue(viewModel.deleteAccountState.value is UiState.Success)
        assertEquals(true, (viewModel.deleteAccountState.value as UiState.Success).data)
    }

    @Test
    fun `deleteAccount without password handles failure correctly`() = runTest(testDispatcher) {
        `when`(authRepository.deleteAccount(null)).thenReturn(NetworkResult.Error("Network failure"))

        viewModel.deleteAccount()
        advanceUntilIdle()

        verify(authRepository).deleteAccount(null)
        assertTrue(viewModel.deleteAccountState.value is UiState.Error)
        assertEquals("Network failure", (viewModel.deleteAccountState.value as UiState.Error).message)
    }

    @Test
    fun `resetDeleteAccountState sets state back to Idle`() = runTest(testDispatcher) {
        `when`(authRepository.deleteAccount(null)).thenReturn(NetworkResult.Success(true))

        viewModel.deleteAccount()
        advanceUntilIdle()

        viewModel.resetDeleteAccountState()
        assertEquals(UiState.Idle, viewModel.deleteAccountState.value)
    }
}

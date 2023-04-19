package com.yapp.gallery.home.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.common.util.webview.NavigatePayload
import com.yapp.gallery.common.util.webview.WebViewState
import com.yapp.gallery.domain.usecase.auth.GetRefreshedTokenUseCase
import com.yapp.gallery.domain.usecase.auth.GetValidTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getValidTokenUseCase: GetValidTokenUseCase
) : ViewModel() {
    private val _calendarSideEffect = Channel<NavigatePayload>()
    val calendarSideEffect = _calendarSideEffect.receiveAsFlow()

    private val _calendarState = MutableStateFlow<WebViewState>(WebViewState.Initial)
    val calendarState : StateFlow<WebViewState>
        get() = _calendarState

    init {
        getRefreshedToken()
    }

    fun getRefreshedToken(){
        getValidTokenUseCase()
            .onEach { _calendarState.value = WebViewState.Connected(it) }
            .catch { _calendarState.value = WebViewState.Disconnected }
            .launchIn(viewModelScope)
    }

    fun setSideEffect(action: String, payload : String?){
        viewModelScope.launch {
            _calendarSideEffect.send(NavigatePayload(action, payload = null))
        }
        Log.e("homeSideEffect", action)
    }
}
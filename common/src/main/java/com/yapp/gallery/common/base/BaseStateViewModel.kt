package com.yapp.gallery.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseStateViewModel<S : ViewModelContract.State, E : ViewModelContract.Event, SE : ViewModelContract.SideEffect>(
    initialState: S,
) : ViewModel() {
    private val _viewState = MutableStateFlow(initialState)
    val viewState : StateFlow<S> = _viewState.asStateFlow()

    private val currentState : S get() = _viewState.value

    protected val _events = MutableSharedFlow<E>()

    private val _sideEffect : Channel<SE> =Channel()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        _events.onEach(::handleEvents)
            .launchIn(viewModelScope)
    }

    protected fun sendSideEffect(effect: SE){
        viewModelScope.launch{
            _sideEffect.send(effect)
        }
    }

    protected fun updateState(reduce: S.() -> S) {
        val newState = currentState.reduce()
        _viewState.update { newState }
    }


    fun sendEvent(event: E) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
    abstract fun handleEvents(event: E)
}
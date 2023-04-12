package com.yapp.gallery.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseStateViewModel<S : ViewModelContract.State, E : ViewModelContract.Event, SE : ViewModelContract.SideEffect> :
    ViewModel()
{
    protected abstract val initialState: S

    protected val _events = Channel<E>()

    val viewState : StateFlow<S> by lazy {
        _events.receiveAsFlow()
            .runningFold(initialState, ::reduceState)
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialState)
    }

    private val _sideEffect : Channel<SE> =Channel()
    val sideEffect = _sideEffect.receiveAsFlow()

    protected fun sendSideEffect(effect: SE){
        viewModelScope.launch{
            _sideEffect.send(effect)
        }
    }

    fun sendEvent(event: E) {
        viewModelScope.launch {
            _events.send(event)
        }
    }
    abstract fun reduceState(current: S, event: E) : S
}
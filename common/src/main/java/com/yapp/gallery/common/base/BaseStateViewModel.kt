package com.yapp.gallery.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseStateViewModel<S : ViewModelContract.State, E : ViewModelContract.Event,
        R : ViewModelContract.Reduce, SE : ViewModelContract.SideEffect>(initialState: S) : ViewModel()
{
    private val _viewState = MutableStateFlow(initialState)
    val viewState: StateFlow<S> = _viewState.asStateFlow()

    private val currentState : S get() = _viewState.value

    private val _events = MutableSharedFlow<E>()
    private val _reduce = MutableSharedFlow<R>()

    private val _sideEffect: Channel<SE> = Channel()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        _events.onEach(::handleEvents)
            .launchIn(viewModelScope)

        _reduce.onEach {
            _viewState.value = reduceState(currentState, it)
        }.launchIn(viewModelScope)
    }

    protected fun sendSideEffect(effect: SE){
        viewModelScope.launch{
            _sideEffect.send(effect)
        }
    }


    fun sendEvent(event: E) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    protected fun updateState(reduce: R){
        viewModelScope.launch {
            _reduce.emit(reduce)
        }
    }

    abstract fun handleEvents(event: E)
    abstract fun reduceState(state: S, reduce: R) : S
}
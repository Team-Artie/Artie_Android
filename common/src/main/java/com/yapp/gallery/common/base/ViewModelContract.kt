package com.yapp.gallery.common.base

sealed interface ViewModelContract{
    interface State: ViewModelContract
    // View에서 발생한 Intent(Event)
    interface Event: ViewModelContract
    // ViewModel에서
    interface Reduce: ViewModelContract
    // 상태 변화 없이 View에서 처리해야 할 액션
    interface SideEffect: ViewModelContract
}
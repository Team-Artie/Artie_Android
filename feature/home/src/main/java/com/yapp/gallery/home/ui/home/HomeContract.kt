package com.yapp.gallery.home.ui.home

import com.yapp.gallery.common.base.ViewModelContract

class HomeContract {
    data class HomeState(
        val idToken: String? = null,
        val connected: Boolean = true
    ) : ViewModelContract.State

    sealed class HomeEvent : ViewModelContract.Event{
        object OnLoadAgain : HomeEvent()
        data class OnWebViewClick (val action: String, val payload: String?) : HomeEvent()
    }

    sealed class HomeReduce : ViewModelContract.Reduce{
        data class Connected(val idToken: String) : HomeReduce()
        object Disconnected : HomeReduce()
    }

    sealed class HomeSideEffect : ViewModelContract.SideEffect{
        object NavigateToRecord : HomeSideEffect()
        object NavigateToProfile : HomeSideEffect()
        data class NavigateToInfo(val postId: Long, val idToken: String?) : HomeSideEffect()
    }
}
package com.yapp.gallery.home.ui.home

import com.yapp.gallery.common.base.ViewModelContract

class HomeContract {
    data class HomeState(
        val afterLogin: Boolean = false,
        val idToken: String? = null,
        val connected: Boolean = true,
    ) : ViewModelContract.State

    sealed class HomeEvent : ViewModelContract.Event{
        object CheckToken : HomeEvent()
        object OnLoadAgain : HomeEvent()
        data class OnWebViewClick (val action: String, val payload: String?) : HomeEvent()
    }

    sealed class HomeReduce : ViewModelContract.Reduce{
        data class UpdateAfterLogin(val afterLogin: Boolean) : HomeReduce()
        data class Connected(val idToken: String) : HomeReduce()
        object Disconnected : HomeReduce()
    }

    sealed class HomeSideEffect : ViewModelContract.SideEffect{
        object NavigateToLogin : HomeSideEffect()
        object NavigateToRecord : HomeSideEffect()
        object NavigateToProfile : HomeSideEffect()
        data class NavigateToInfo(val postId: Long, val idToken: String?) : HomeSideEffect()
    }
}
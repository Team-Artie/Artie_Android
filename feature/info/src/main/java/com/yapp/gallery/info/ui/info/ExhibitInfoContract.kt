package com.yapp.gallery.info.ui.info

import com.yapp.gallery.common.base.ViewModelContract

class ExhibitInfoContract {
    sealed class ExhibitInfoState : ViewModelContract.State {
        object Initial : ExhibitInfoState()
        data class Connected(val idToken: String) : ExhibitInfoState()
        object Disconnected : ExhibitInfoState()
    }

    sealed class ExhibitInfoEvent : ViewModelContract.Event {
        object OnLoadAgain : ExhibitInfoEvent()
        data class OnWebViewClick(val action: String, val payload: String?) : ExhibitInfoEvent()
    }

    sealed class ExhibitInfoReduce : ViewModelContract.Reduce {
        data class Connected(val idToken: String) : ExhibitInfoReduce()
        object Disconnected : ExhibitInfoReduce()
    }

    sealed class ExhibitInfoSideEffect : ViewModelContract.SideEffect {
        data class NavigateToEdit(val data: String) : ExhibitInfoSideEffect()
        data class NavigateToCamera(val exhibitId: Long) : ExhibitInfoSideEffect()
        data class NavigateToGallery(val exhibitId: Long, val count: Int = 0) : ExhibitInfoSideEffect()
        object PopBackStack : ExhibitInfoSideEffect()
        data class ShowWebPage(val url: String) : ExhibitInfoSideEffect()
    }
}
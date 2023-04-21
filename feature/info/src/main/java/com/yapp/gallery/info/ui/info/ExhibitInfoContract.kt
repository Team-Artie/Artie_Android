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
        object NavigateToCamera : ExhibitInfoSideEffect()
        object NavigateToGallery : ExhibitInfoSideEffect()
        object PopBackStack : ExhibitInfoSideEffect()
    }
}
package com.yapp.gallery.profile.screen.profile

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.common.model.BaseState
import com.yapp.gallery.domain.entity.profile.User
import com.yapp.gallery.domain.usecase.profile.GetUserUseCase
import com.yapp.gallery.domain.usecase.record.DeleteRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val deleteRecordUseCase: DeleteRecordUseCase,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private var _userData = MutableStateFlow<BaseState<User>>(BaseState.Loading)
    val userData : StateFlow<BaseState<User>> get() = _userData

    init {
        getUser()
    }

    fun removeInfo(){
        viewModelScope.launch {
            deleteRecordUseCase()
                .catch {
                    Log.e("removeProfile", it.message.toString())
                }
                .collect()
        }

        sharedPreferences.edit().apply {
            remove("idToken").apply()
            remove("loginType").apply()
        }
    }

    private fun getUser(){
        viewModelScope.launch {
            getUserUseCase()
                .catch {
                    _userData.value = BaseState.Error(it.message)
                }
                .collect{
                    _userData.value = BaseState.Success(it)
                }
        }
    }
}
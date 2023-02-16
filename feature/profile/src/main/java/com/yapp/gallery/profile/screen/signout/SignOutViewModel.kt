package com.yapp.gallery.profile.screen.signout

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yapp.gallery.domain.usecase.record.DeleteRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignOutViewModel @Inject constructor(
    private val deleteRecordUseCase: DeleteRecordUseCase,
    private val sharedPreferences: SharedPreferences
) : ViewModel(){

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

}
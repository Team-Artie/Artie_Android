package com.yapp.gallery.domain.usecase.auth

import com.yapp.gallery.domain.repository.AuthRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import javax.inject.Inject

class SetLoginInfoUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(loginType: String, idToken: String, userId: Long) = combine(
        authRepository.setLoginType(loginType),
        authRepository.setIdToken(idToken),
        authRepository.setUserId(userId)
    ) { _, _, _ -> }
}
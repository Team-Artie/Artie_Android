package com.yapp.gallery.domain.usecase.auth

import com.yapp.gallery.domain.repository.AuthRepository
import javax.inject.Inject

class GetUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.getUserId()
}
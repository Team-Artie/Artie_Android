package com.yapp.gallery.data.source.remote.profile

import com.yapp.gallery.data.api.ArtieService
import com.yapp.gallery.data.di.DispatcherModule.IoDispatcher
import com.yapp.gallery.domain.entity.profile.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ProfileRemoteDataSourceImpl @Inject constructor(
    private val artieService: ArtieService,
    @IoDispatcher private val dispatcher : CoroutineDispatcher
) : ProfileRemoteDataSource {
    override fun loadUserData(): Flow<User> = flow {
        emit(artieService.getUserData())
    }.flowOn(dispatcher)

    override fun changeNickname(
        editedName: String
    ): Flow<Boolean> = flow {
        emit(artieService.updateNickname(editedName).isSuccessful)
    }.flowOn(dispatcher)

    override fun signOut(): Flow<Boolean> = flow {
        emit(artieService.signOut().isSuccessful)
    }.flowOn(dispatcher)
}
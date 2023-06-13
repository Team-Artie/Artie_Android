package com.yapp.gallery.data.source.file

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.yapp.gallery.data.di.DispatcherModule.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import javax.inject.Inject

class CameraFileDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher : CoroutineDispatcher
) : CameraFileDataSource {
    override fun saveImageToGallery(
        image: ByteArray, fileName: String
    ) : Flow<Unit> = flow {
        val contentValues = ContentValues()
        contentValues.put(
            MediaStore.Images.Media.DISPLAY_NAME,
            fileName
        ) // 확장자가 붙어있는 파일명 ex) sample.jpg
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

        // 안드로이드 10 이상
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // IS_PENDING을 1로 설정해놓으면, 현재 파일을 업데이트 전까지 외부에서 접근하지 못하도록 할 수 있다.
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        val contentResolver = context.contentResolver
        val uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Failed to create new MediaStore record.")

        val fos = contentResolver.openOutputStream(uri)
        fos?.write(image)
        fos?.close()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear();
            // 파일 저장이 완료되었으니, IS_PENDING을 다시 0으로 설정한다.
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
            // 파일을 업데이트하면, 파일이 보인다.
            contentResolver.update(uri, contentValues, null, null);
        }
        emit(Unit)

    }.flowOn(dispatcher)
}
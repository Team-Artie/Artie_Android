package com.yapp.gallery.camera.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yapp.gallery.camera.R
import com.yapp.gallery.camera.ui.result.ResultContract.ResultRegisterState
import com.yapp.gallery.common.theme.ArtieTheme
import com.yapp.gallery.common.theme.color_black
import com.yapp.gallery.common.theme.color_gray400
import com.yapp.gallery.common.theme.color_gray600
import com.yapp.gallery.common.theme.color_white

@Composable
fun RegisterDialog(
    onDismiss : () -> Unit = {},
    onConfirm : () -> Unit = {},
    isContentEmpty : Boolean = false,
    registerState: ResultRegisterState = ResultRegisterState.RegisterInitial
){
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false,
            dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
                .background(
                    color = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(size = 16.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Close, contentDescription = null,
                    modifier = Modifier.size(20.dp), tint = color_gray400
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = stringResource(id = R.string.post_upload) + if (registerState is ResultRegisterState.RegisterLoading) "중" else "",
                style = MaterialTheme.typography.h2.copy(
                    color = color_white, fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp, lineHeight = 27.sp
                ),
            )


            if (registerState is ResultRegisterState.RegisterLoading){
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp)
                )
            } else {
                val str = when(registerState) {
                    is ResultRegisterState.RegisterError -> stringResource(id = R.string.post_upload_fail)
                    is ResultRegisterState.RegisterLoading -> ""
                    else -> if (isContentEmpty) stringResource(R.string.post_upload_empty_content)
                            else stringResource(R.string.post_upload_content)
                }
                Text(text = str, style = MaterialTheme.typography.h4
                        .copy(color = color_gray600, lineHeight = 16.8.sp), textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(42.dp))



            // 예, 아니오 버튼
            if (registerState !is ResultRegisterState.RegisterLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 29.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(size = 50.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = color_gray600),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "취소", style = MaterialTheme.typography.h2.copy(
                                color = color_black, fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(size = 50.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "등록", style = MaterialTheme.typography.h2.copy(
                                color = color_black, fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterDialogPreview(){
    ArtieTheme {
        RegisterDialog()
    }
}
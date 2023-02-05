package com.yapp.gallery.login.screen

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yapp.gallery.common.theme.*
import com.yapp.gallery.login.R

@Composable
fun LoginScreen(
    naverLogin: () -> Unit,
    googleLogin : () -> Unit,
    kakaoLogin: () -> Unit,
    isLoading : MutableState<Boolean>
){

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(painter = painterResource(id = R.drawable.ic_logo), contentDescription = "logo",
                    alignment = Alignment.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(id = R.string.service_name), fontSize = 18.sp)
                Spacer(modifier = Modifier.height(38.dp))
                Text(text = stringResource(id = R.string.service_slogan), fontSize = 16.sp)
            }
            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                Row {
                    Image(painter = painterResource(id = R.drawable.ic_naver_login), contentDescription = "kakao",
                        modifier = Modifier
                            .size(72.dp)
                            .modifyIf(!isLoading.value) {
                                clickable(onClick = naverLogin)
                            }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Image(painter = painterResource(id = R.drawable.ic_kakao_login), contentDescription = "kakao",
                        modifier = Modifier
                            .size(72.dp)
                            .modifyIf(!isLoading.value) {
                                clickable(onClick = kakaoLogin)
                            }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Image(painter = painterResource(id = R.drawable.ic_google_login), contentDescription = "google",
                        modifier = Modifier
                            .size(72.dp)
                            .modifyIf(!isLoading.value) {
                                clickable(onClick = googleLogin)
                            }

                    )
                }
                Spacer(modifier = Modifier.height(120.dp))
            }

            // 로딩 스크린
            if (isLoading.value){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = color_gray300.copy(alpha = 0.2f)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(50.dp), color = color_mainBlue)
                }
            }


        }
    }
}

fun Modifier.modifyIf(condition: Boolean, modify: Modifier.() -> Modifier) =
    if (condition) modify() else this

@SuppressLint("UnrememberedMutableState")
@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun LoginPreview(){
    LoginScreen(naverLogin = { /*TODO*/}, googleLogin = { /*TODO*/ }, kakaoLogin = { /*TODO*/ }, isLoading = mutableStateOf(false))
}
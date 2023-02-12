package com.yapp.gallery.profile.screen.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yapp.gallery.common.model.BaseState
import com.yapp.gallery.common.theme.color_background
import com.yapp.gallery.common.theme.color_gray600
import com.yapp.gallery.common.widget.CenterTopAppBar
import com.yapp.gallery.common.widget.ConfirmDialog
import com.yapp.gallery.domain.entity.profile.User
import com.yapp.gallery.profile.R

@Composable
fun ProfileScreen(
    navigateToManage: () -> Unit,
    navigateToLegacy: () -> Unit,
    logout: () -> Unit,
    withdrawal: () -> Unit,
    popBackStack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
){
    val user : BaseState<User> by viewModel.userData.collectAsState()

    val logoutDialogShown = remember { mutableStateOf(false) }
    val withdrawalDialogShown = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            CenterTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                title = {
                    Text(
                        text = stringResource(id = R.string.profile_appbar_title),
                        style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = popBackStack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->  
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(38.dp))
            // 기본 닉네임 및 회원정보 row
            Row(modifier = Modifier
                .fillMaxWidth()) {
                Column {
                    Row {
                        Text(text = (user as? BaseState.Success<User>)?.value?.name ?: "",
                            style = MaterialTheme.typography.h1.copy(fontWeight = FontWeight.SemiBold))
                        Text(text = "이", style = MaterialTheme.typography.h1)
                    }
                    Text(text = "기록한 전시", style = MaterialTheme.typography.h1)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "00개의 전시 기록", style = MaterialTheme.typography.h3)
            }
            Spacer(modifier = Modifier.height(32.dp))

            // 카테고리 관리 버튼
            Button(
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.fillMaxWidth(),
                onClick = navigateToManage) {
                Text(text = stringResource(id = R.string.category_manage_btn), style = MaterialTheme.typography.h3.copy(
                    color = color_background, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(vertical = 10.dp))
            }

            // 각 기능 버튼
            Spacer(modifier = Modifier.height(32.dp))
            ProfileFeature(
                featureName = stringResource(id = R.string.feature_profile_edit),
                onFeatureClick = { /*TODO*/ },
                isLast = false
            )
            ProfileFeature(
                featureName = stringResource(id = R.string.feature_announce),
                onFeatureClick = { /*TODO*/ },
                isLast = false
            )
            ProfileFeature(
                featureName = stringResource(id = R.string.feature_legacy),
                onFeatureClick = { navigateToLegacy() },
                isLast = false
            )
            ProfileFeature(
                featureName = stringResource(id = R.string.feature_logout),
                onFeatureClick = { logoutDialogShown.value = true },
                isLast = false
            )
            ProfileFeature(
                featureName = stringResource(id = R.string.feature_withdraw),
                onFeatureClick = { withdrawalDialogShown.value = true },
                isLast = true
            )


            // 로그아웃 다이얼로그
            if (logoutDialogShown.value){
                ConfirmDialog(
                    title = stringResource(id = R.string.logout_dialog_title),
                    subTitle = stringResource(id = R.string.logout_dialog_guide),
                    onDismissRequest = { logoutDialogShown.value = false },
                    onConfirm = {
                        viewModel.removeInfo()
                        logout()
                    }
                )
            }

            // 회원탈퇴 다이얼로그
            if (withdrawalDialogShown.value){
                // Todo : 서버에서 탈퇴까지 구현해야함
                ConfirmDialog(
                    title = stringResource(id = R.string.withdrawal_dialog_title),
                    subTitle = stringResource(id = R.string.withdrawal_dialog_guide),
                    onDismissRequest = { withdrawalDialogShown.value = false },
                    onConfirm = {
                        viewModel.removeInfo()
                        withdrawal()
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileFeature(
    featureName: String,
    onFeatureClick : () -> Unit,
    isLast: Boolean
){
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onFeatureClick)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = featureName, style = MaterialTheme.typography.h3.copy(fontWeight = FontWeight.Medium))
        Spacer(modifier = Modifier.height(16.dp))
        if (!isLast)
            Divider(color = color_gray600, thickness = 0.4.dp, modifier = Modifier.fillMaxWidth())
    }
}
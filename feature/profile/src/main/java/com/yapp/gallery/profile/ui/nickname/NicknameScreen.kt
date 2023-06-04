package com.yapp.gallery.profile.ui.nickname

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yapp.gallery.common.theme.*
import com.yapp.gallery.common.widget.CenterTopAppBar
import com.yapp.gallery.common.widget.ConfirmDialog
import com.yapp.gallery.profile.R
import com.yapp.gallery.profile.ui.nickname.NicknameContract.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NicknameRoute(
    popBackStack: () -> Unit,
    updateNickname: (String) -> Unit,
    viewModel: NicknameViewModel = hiltViewModel(),
){
    val nicknameState : NicknameState by viewModel.viewState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.sideEffect){
        viewModel.sideEffect.collectLatest {
            when(it){
                is NicknameSideEffect.PopBackStack -> {
                    updateNickname(nicknameState.edited)
                    popBackStack()
                }
            }
        }
    }

    if (nicknameState.failureDialogShown){
        // Todo: 취소 없이 예 하나만 있는 다이얼로그로
        ConfirmDialog(
            title = stringResource(id = R.string.edit_nickname_error_title),
            subTitle = stringResource(id = R.string.edit_nickname_error_content),
            onDismissRequest = { viewModel.sendEvent(NicknameEvent.OnCloseFailureDialog) },
            onConfirm = { viewModel.sendEvent(NicknameEvent.OnCloseFailureDialog) },
            important = false
        )
    }

    NicknameScreen(
        nicknameState = nicknameState,
        onEditNickname = { viewModel.sendEvent(NicknameEvent.OnChangeNickname(it))},
        onClickUpdate = { viewModel.sendEvent(NicknameEvent.OnClickUpdate) },
        popBackStack = popBackStack
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NicknameScreen(
    nicknameState : NicknameState,
    onEditNickname: (String) -> Unit,
    onClickUpdate : () -> Unit,
    popBackStack : () -> Unit
){
    Scaffold(
        topBar = {
            CenterTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                title = {
                    Text(
                        text = stringResource(id = R.string.nickname_appbar_title),
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
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    disabledBackgroundColor = color_gray600,
                    disabledContentColor = color_gray900,
                    backgroundColor = color_mainGreen,
                    contentColor = color_black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 53.dp),
                onClick = onClickUpdate,
                enabled = nicknameState.canUpdate
            ) {
                Text(
                    text = stringResource(id = R.string.nickname_appbar_title),
                    modifier = Modifier.padding(vertical = 12.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    fontFamily = pretendard
                )
            }
        }
    ){paddingValues ->
        NicknameContent(
            nicknameState = nicknameState, onEditNickname = onEditNickname,
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun NicknameContent(
    modifier: Modifier = Modifier,
    nicknameState: NicknameState,
    onEditNickname : (String) -> Unit,
    focusManager : FocusManager = LocalFocusManager.current,
    keyboardController : SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
){
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(48.dp))
        BasicTextField(
            value = nicknameState.edited,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = onEditNickname,
            cursorBrush = SolidColor(color_white),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            textStyle = MaterialTheme.typography.h3,
        ){
            TextFieldDefaults.TextFieldDecorationBox(
                value = nicknameState.edited,
                innerTextField = it,
                visualTransformation = VisualTransformation.None,
                interactionSource = MutableInteractionSource(),
                enabled = true,
                singleLine = true,
                contentPadding = PaddingValues(0.dp),
                isError = nicknameState.errorMessage != null,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.nickname_guide), style =
                        MaterialTheme.typography.h3.copy(color = color_gray700)
                    )
                },
                trailingIcon = {
                    Row {
                        Text(
                            text = "${nicknameState.edited.length}",
                            style = MaterialTheme.typography
                                .h4.copy(color = color_mainGreen)
                        )
                        Text(
                            text = "/10",
                            modifier = Modifier.padding(end = 10.dp),
                            style = MaterialTheme.typography
                                .h4.copy(color = color_gray600)
                        )
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Divider(
            color = if (nicknameState.errorMessage != null) MaterialTheme.colors.error else color_gray600,
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.8.dp
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (nicknameState.errorMessage != null){
            Text(text = nicknameState.errorMessage,
                style = MaterialTheme.typography.h4.copy(color = Color.Red),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NicknameScreenPreview(){
    ArtieTheme {
        NicknameScreen(
            nicknameState = NicknameState(originNickname = "닉네임", edited = "닉네임1"),
            onEditNickname = {},
            onClickUpdate = {},
            popBackStack = {}
        )
    }
}
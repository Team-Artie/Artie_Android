package com.yapp.gallery.profile.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yapp.gallery.common.theme.color_gray300
import com.yapp.gallery.common.theme.color_popUpBottom
import com.yapp.gallery.profile.R

@Composable
fun CustomSnackbarHost(
    snackbarHostState : SnackbarHostState
){
    SnackbarHost(
        hostState = snackbarHostState,
    ) { snackbarData ->
        Card(
            shape = RoundedCornerShape(size = 12.dp),
            backgroundColor = color_popUpBottom,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_category_alert),
                    contentDescription = "alert"
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = snackbarData.message, style = MaterialTheme.typography.h3.copy(
                        fontWeight = FontWeight.Medium, color = color_gray300
                    )
                )
            }
        }
    }
}
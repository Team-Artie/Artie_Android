package com.yapp.gallery.camera.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yapp.gallery.camera.R
import com.yapp.gallery.common.theme.ArtieTheme

@Composable
fun EmotionalTag(
    tag: String,
    onDelete: () -> Unit,
){
    Surface(
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 24.dp,
            bottomEnd = 24.dp, bottomStart = 4.dp
        ),
        border = BorderStroke(1.dp, color = MaterialTheme.colors.primary)
    ) {
        Row{
           Text(text = tag, style = MaterialTheme.typography.h4.copy(color = MaterialTheme.colors.primary),
               modifier = Modifier
                   .padding(vertical = 10.dp)
                   .padding(start = 14.dp)
           )

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .padding(6.dp).size(16.dp)
                    .align(CenterVertically)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colors.primary.copy(alpha = 0.2f),
                    modifier = Modifier
                        .clickable(onClick = onDelete)
                        .size(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_emotion_delete),
                        contentDescription = "delete",
                        modifier = Modifier
                            .padding(4.dp)
                            .align(CenterVertically)
                            .size(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmotionalTagPreview(){
    ArtieTheme {
        EmotionalTag(
            tag = "행복",
            onDelete = {}
        )
    }
}
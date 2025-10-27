package com.example.bfdream_android.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bfdream_android.R

@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.help_page),
            contentDescription = "도움말 페이지",
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentScale = ContentScale.FillWidth
        )

        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
//                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color.White
            )
        }
    }
}

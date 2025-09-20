package com.example.bfdream_android.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.bfdream_android.R

@Composable
fun SplashScreen(
    onNavigateLogin: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize().fillMaxWidth()
) {
    Image(
        painter = painterResource(id = R.drawable.splash),
        contentDescription = null,
        modifier = Modifier
    )
}
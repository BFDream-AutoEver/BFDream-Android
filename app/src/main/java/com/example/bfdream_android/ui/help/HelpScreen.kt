package com.example.bfdream_android.ui.help

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.bfdream_android.R
import com.example.bfdream_android.ui.theme.pr_LavenderPurple
import com.example.bfdream_android.ui.theme.pr_White
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    val helpImages = listOf(
        R.drawable.help_page_1,
        R.drawable.help_page_2,
    )

    val helpDescriptions = listOf(
        R.string.help_desc_1,
        R.string.help_desc_2,
    )

    val pagerState = rememberPagerState(pageCount = { helpImages.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.title_help),
                        color = pr_White,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back),
                            tint = pr_White,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = pr_LavenderPurple)
            )
        },
        containerColor = pr_LavenderPurple,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                // 각 페이지마다 별도의 FocusRequester 생성
                val focusRequester = remember { FocusRequester() }
                // 현재 페이지가 '나'인지 확인
                val isSelected = pagerState.currentPage == page
                // 선택된 페이지라면 잠시 후 포커스 강제 요청
                LaunchedEffect(isSelected) {
                    if (isSelected) {
                        delay(300) // 애니메이션 대기
                        try {
                            focusRequester.requestFocus()
                        } catch (e: Exception) {
                            // 예외 처리
                        }
                    }
                }

                Image(
                    painter = painterResource(id = helpImages[page]),
                    contentDescription = stringResource(id = helpDescriptions[page]),
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                        .focusable(),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
}
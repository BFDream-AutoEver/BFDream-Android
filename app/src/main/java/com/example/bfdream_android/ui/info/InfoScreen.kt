package com.example.bfdream_android.ui.info

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bfdream_android.BuildConfig
import com.example.bfdream_android.R
import com.example.bfdream_android.ui.info.components.ClickableInfoRow
import com.example.bfdream_android.ui.info.components.InfoRow
import com.example.bfdream_android.ui.info.components.SwitchInfoRow
import com.example.bfdream_android.ui.theme.pr_LavenderPurple
import com.example.bfdream_android.ui.theme.pr_White
import com.example.bfdream_android.viewmodel.BTViewModel
import com.example.bfdream_android.viewmodel.BTViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    onNavigateBack: () -> Unit,
    btViewModel: BTViewModel = viewModel(
        factory = BTViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val view = LocalView.current

    val uriHandler = LocalUriHandler.current
    var showExternalLinkDialog by remember { mutableStateOf(false) }
    var selectedUrl by remember { mutableStateOf("") }
    var selectedTitle by remember { mutableStateOf("") }

    val inquiryUrl = "https://forms.gle/rnSD44sUEuy1nLaH6"
    val policyUrl = "https://important-hisser-903.notion.site/10-22-ver-29a65f12c44480b6b591e726c5c80f89?pvs=74"

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val isSoundOn by btViewModel.isSoundOn.collectAsState()

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.main_menu_info),
                        color = pr_White,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back),
                            tint = pr_White,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = pr_LavenderPurple
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(pr_LavenderPurple)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(screenHeight * 0.01f))

            Image(
                painter = painterResource(id = R.drawable.info_logo),
                contentDescription = stringResource(R.string.desc_app_logo),
                modifier = Modifier.size(screenHeight * 0.24f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = pr_White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        top = 26.dp,
                        bottom = 26.dp,
                        start = 8.dp,
                        end = 8.dp
                    )
                ) {
                    SwitchInfoRow(
                        text = stringResource(R.string.info_sound_title),
                        subText = stringResource(R.string.info_sound_desc),
                        checked = isSoundOn,
                        onCheckedChange = {
                            btViewModel.toggleSound(it)
                            view.performHapticFeedback(
                                HapticFeedbackConstants.CLOCK_TICK,
                                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                            )
                        }
                    )
                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    InfoRow(
                        text = stringResource(R.string.info_version),
                        value = BuildConfig.VERSION_NAME,
                    )
                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    ClickableInfoRow(
                        text = stringResource(R.string.info_inquiry),
                        onClick = {
                            view.performHapticFeedback(
                                HapticFeedbackConstants.CLOCK_TICK,
                                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                            )
                            selectedUrl = inquiryUrl
                            selectedTitle = "앱 문의"
                            showExternalLinkDialog = true
                        },
                    )
                    Divider(color = Color.LightGray.copy(alpha = 0.4f))

                    ClickableInfoRow(
                        text = stringResource(R.string.info_policy),
                        onClick = {
                            view.performHapticFeedback(
                                HapticFeedbackConstants.CLOCK_TICK,
                                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                            )
                            selectedUrl = policyUrl
                            selectedTitle = "개인정보 처리 방침 및 이용약관"
                            showExternalLinkDialog = true
                        },
                    )
                }
            }

            if (showExternalLinkDialog) {
                AlertDialog(
                    onDismissRequest = { showExternalLinkDialog = false },
                    title = { Text(text = "외부 링크 이동") },
                    text = {
                        Text(text = "'${selectedTitle}' 페이지로 이동하시겠습니까?\n앱을 벗어나 브라우저가 실행됩니다.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                view.performHapticFeedback(
                                    HapticFeedbackConstants.CLOCK_TICK,
                                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                                )
                                // 확인 버튼 클릭 시 실제 이동
                                if (selectedUrl.isNotEmpty()) {
                                    uriHandler.openUri(selectedUrl)
                                }
                                showExternalLinkDialog = false
                            }
                        ) {
                            Text("이동")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                view.performHapticFeedback(
                                    HapticFeedbackConstants.CLOCK_TICK,
                                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                                )
                                showExternalLinkDialog = false
                            }
                        ) {
                            Text("취소")
                        }
                    }
                )
            }
        }
    }
}

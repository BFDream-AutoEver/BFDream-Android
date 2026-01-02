package com.example.bfdream_android.ui.onboarding.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bfdream_android.R
import com.example.bfdream_android.ui.theme.pr_White

data class OnboardPageData(
    val image: Int,
    val title: String,
    val titleHighlight: String,
    val highlightColor: Long,
    val description: String = "",
    val isLast: Boolean = false,
)

@Composable
fun OnboardPage(data: OnboardPageData) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val spacerSize = screenHeight * 0.01f
    val imageSize = screenHeight * 0.35f
    val imageSize2 = screenHeight * 0.22f
    val screenWidth = configuration.screenWidthDp
    val textScale = (screenWidth / 360f).coerceIn(0.4f, 1.3f)

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val annotatedString = buildAnnotatedString {
            append(data.title)
            // 1. 전체 텍스트를 흰색으로 설정
            addStyle(
                style = SpanStyle(color = pr_White),
                start = 0,
                end = data.title.length
            )

            // 2. 강조할 텍스트가 있다면, 해당 부분만 primary 색상으로 덧칠
            val startIndex = data.title.indexOf(data.titleHighlight)
            if (startIndex != -1) {
                addStyle(
                    style = SpanStyle(color = Color(data.highlightColor)),
                    start = startIndex,
                    end = startIndex + data.titleHighlight.length
                )
            }
        }

        if (data.isLast) {
            Image(
                painter = painterResource(id = data.image),
                contentDescription = data.title,
                modifier = Modifier.size(imageSize2)
            )

            Spacer(modifier = Modifier.height(spacerSize*3))
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 36.sp * textScale,
                lineHeight = (36.sp * textScale) * 1.3f
            )

            Spacer(modifier = Modifier.height(spacerSize*4))
            Row(
                modifier = Modifier.width(250.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.location),
                        contentDescription = "위치 아이콘",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(R.string.onboard_loc_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp * textScale,
                        color = pr_White,
                    )
                    Text(
                        text = stringResource(R.string.onboard_loc_desc),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp * textScale,
                        color = pr_White,
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bluetooth),
                        contentDescription = "블루투스 아이콘",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(R.string.onboard_bt_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp * textScale,
                        color = pr_White,
                    )
                    Text(
                        text = stringResource(R.string.onboard_bt_desc),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp * textScale,
                        color = pr_White,
                    )
                }
            }
        } else {
            Image(
                painter = painterResource(id = data.image),
                contentDescription = data.title,
                modifier = Modifier.size(imageSize)
            )

            Spacer(modifier = Modifier.height(spacerSize))
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 36.sp * textScale,
                lineHeight = (36.sp * textScale) * 1.3f
            )

            Spacer(modifier = Modifier.height(spacerSize))
            Text(
                text = data.description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = pr_White,
                fontSize = 14.sp,
                lineHeight = 16.sp
            )
        }
    }
}

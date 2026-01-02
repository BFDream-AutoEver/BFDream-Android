package com.example.bfdream_android.ui.info.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bfdream_android.ui.theme.pr_PeriwinkleBlue
import com.example.bfdream_android.ui.theme.pr_White

@Composable
fun SwitchInfoRow(
    text: String,
    subText: String = "", // 기본값은 빈 문자열
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 텍스트 영역 (제목 + 부연설명)
        Column(
            modifier = Modifier.weight(1f), // 남은 공간을 차지하여 스위치를 오른쪽으로 밀어냄
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = pr_PeriwinkleBlue,
            )

            // 부연 설명이 있을 때만 표시
            if (subText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall, // 작은 글씨 크기
                    color = Color.Gray, // 회색 처리
                    lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.2 // 줄간격 살짝 조정
                )
            }
        }

        // Column에 weight(1f)를 주었으므로 Spacer는 필요 없음

        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = pr_White,
                checkedTrackColor = pr_PeriwinkleBlue,
                uncheckedThumbColor = pr_White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

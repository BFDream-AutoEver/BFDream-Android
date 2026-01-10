package com.example.bfdream_android.ui.info.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bfdream_android.ui.theme.pr_PeriwinkleBlue

@Composable
fun InfoRow(text: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = 16.dp,
            vertical = 20.dp
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = pr_PeriwinkleBlue,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
        )
    }
}

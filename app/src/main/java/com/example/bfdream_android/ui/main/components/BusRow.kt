package com.example.bfdream_android.ui.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bfdream_android.R
import com.example.bfdream_android.data.BusInfo

@Composable
fun BusRow(
    bus: BusInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.bus),
            contentDescription = stringResource(R.string.desc_bus_icon),
            tint = bus.color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row (verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = bus.number,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = bus.color,
                    fontSize = 18.sp
                )
                if (bus.type.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bus.type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Row (verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = bus.arrivalTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                if (bus.congestionStatus.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bus.congestionStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = bus.congestionColor, // 상태별 색상 적용
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = stringResource(
                    id = R.string.bus_direction,
                    bus.adirection
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription =
                if (isSelected) stringResource(R.string.bus_selected)
                else stringResource(R.string.bus_select),
            tint =
                if (isSelected) bus.color
                else Color.LightGray,
            modifier = Modifier.size(32.dp)
        )
    }
}

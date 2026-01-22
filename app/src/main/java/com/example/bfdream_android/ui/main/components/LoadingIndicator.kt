package com.example.bfdream_android.ui.main.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFD0D7FF),
    spokeCount: Int = 8,
    cycleDurationMillis: Int = 800
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FlowerLoader")

    // 해결 1: animateInt 대신 animateFloat 사용 (호환성 문제 해결)
    // 0f 부터 8f(spokeCount)까지 부드럽게 숫자가 올라감
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = spokeCount.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(cycleDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Spin"
    )

    // Float 상태를 Int 단계로 변환
    val currentStep = progress.toInt()

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2

        // 해결 2: 모든 숫자를 Float로 명시적 변환 (Overload ambiguity 해결)
        val spokeLength = radius * 0.35f
        val spokeWidth = radius * 0.2f
        val anglePerSpoke = 360f / spokeCount.toFloat()

        for (i in 0 until spokeCount) {
            // 현재 단계에 따라 투명도 계산
            val index = (i - currentStep + spokeCount) % spokeCount

            // 1f(실수)에서 뺌으로써 계산 타입을 Float로 고정
            val alpha = 1f - (index.toFloat() / spokeCount.toFloat())
            val effectiveAlpha = alpha.coerceIn(0.2f, 1.0f)

            rotate(degrees = i * anglePerSpoke, pivot = center) {
                drawLine(
                    color = color,
                    alpha = effectiveAlpha,
                    // 계산식 내 정수/실수 혼용 방지
                    start = Offset(center.x, center.y - radius + (spokeWidth / 2f)),
                    end = Offset(center.x, center.y - radius + spokeLength),
                    strokeWidth = spokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

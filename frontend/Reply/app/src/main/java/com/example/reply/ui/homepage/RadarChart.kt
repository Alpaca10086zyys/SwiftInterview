package com.example.reply.ui.homepage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    maxValue: Float = 5f,
    colors: List<Color> = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
) {
    // 确保数据和标签数量匹配
    require(values.size == labels.size) { "Values and labels must have the same size" }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f
        val angleStep = 360f / values.size

        // 绘制网格线（同心圆）
        for (i in 1..5) {
            val gridRadius = radius * (i / 5f)
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                center = center,
                radius = gridRadius,
                style = Stroke(width = 1f)
            )
        }

        // 绘制轴线
        for (i in values.indices) {
            val angle = angleStep * i - 90f
            val radian = angle * PI.toFloat() / 180f
            val endX = center.x + radius * cos(radian)
            val endY = center.y + radius * sin(radian)

            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 1f
            )
        }

        // 绘制雷达图数据点
        val points = mutableListOf<Offset>()
        for (i in values.indices) {
            val value = values[i].coerceIn(0f, maxValue)
            val normalizedValue = value / maxValue
            val angle = angleStep * i - 90f
            val radian = angle * PI.toFloat() / 180f
            val x = center.x + radius * normalizedValue * cos(radian)
            val y = center.y + radius * normalizedValue * sin(radian)
            points.add(Offset(x, y))
        }

        // 连接点形成多边形
        for (i in points.indices) {
            val nextIndex = (i + 1) % points.size
            drawLine(
                color = colors[0],
                start = points[i],
                end = points[nextIndex],
                strokeWidth = 3f
            )
        }

        // 填充多边形
        if (points.size >= 3) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                close()
            }
            drawPath(
                path = path,
                color = colors[1].copy(alpha = 0.3f)
            )
        }

        // 绘制数据点
        points.forEach { point ->
            drawCircle(
                color = colors[0],
                center = point,
                radius = 8f
            )
        }

        // 绘制标签 - 优化位置
        for (i in labels.indices) {
            val angle = angleStep * i - 90f
            val radian = angle * PI.toFloat() / 180f
            val labelRadius = radius * 1.15f

            // 计算标签位置
            val x = center.x + labelRadius * cos(radian)
            val y = center.y + labelRadius * sin(radian)

            // 根据角度调整文本对齐方式
            val textAlign = when {
                angle in -45f..45f -> android.graphics.Paint.Align.CENTER // 顶部区域
                angle in 135f..225f -> android.graphics.Paint.Align.CENTER // 底部区域
                angle < 135f -> android.graphics.Paint.Align.LEFT // 右侧区域
                else -> android.graphics.Paint.Align.RIGHT // 左侧区域
            }

            // 根据角度调整垂直偏移
            val verticalOffset = when {
                angle in -45f..45f -> -20f // 顶部标签向上偏移
                angle in 135f..225f -> 20f // 底部标签向下偏移
                else -> 0f
            }

            // 根据角度调整水平偏移
            val horizontalOffset = when {
                angle in 45f..135f -> 10f // 右侧标签向右偏移
                angle in 225f..315f -> -10f // 左侧标签向左偏移
                else -> 0f
            }

            drawContext.canvas.nativeCanvas.drawText(
                labels[i],
                x + horizontalOffset,
                y + verticalOffset,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 24f

                    isAntiAlias = true
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRadarChart() {
    RadarChart(
        values = listOf(4.2f, 3.8f, 4.5f, 3.5f, 4.0f, 4.7f),
        labels = listOf("表达", "项目", "行业", "应变", "专业", "沟通"),
        modifier = Modifier.size(300.dp)
    )
}
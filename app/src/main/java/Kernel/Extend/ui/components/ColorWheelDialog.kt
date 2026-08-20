package Kernel.Extend.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

// 预设高频终端主题颜色模型
private data class PresetColorItem(
    val name: String,
    val color: Color
)

// 16 款精选终端极客配色预设（4大色系整齐排列）
private val PRESET_COLOR_GROUPS = listOf(
    // 经典终端绿系
    PresetColorItem("荧光绿", Color(0xFF00E676)),
    PresetColorItem("黑客绿", Color(0xFF00FF00)),
    PresetColorItem("薄荷绿", Color(0xFF69F0AE)),
    PresetColorItem("翠绿", Color(0xFF4CAF50)),
    // 极客蓝青系
    PresetColorItem("赛博青", Color(0xFF00E5FF)),
    PresetColorItem("电光蓝", Color(0xFF448AFF)),
    PresetColorItem("深海蓝", Color(0xFF2979FF)),
    PresetColorItem("冰晶蓝", Color(0xFF80D8FF)),
    // 活力暖色系
    PresetColorItem("琥珀黄", Color(0xFFFFD54F)),
    PresetColorItem("荧光金", Color(0xFFFFEA00)),
    PresetColorItem("霓虹橙", Color(0xFFFF9100)),
    PresetColorItem("珊瑚橙", Color(0xFFFF6E40)),
    // 个性炫彩系
    PresetColorItem("警示红", Color(0xFFFF5252)),
    PresetColorItem("极客粉", Color(0xFFFF4081)),
    PresetColorItem("霓虹紫", Color(0xFFE040FB)),
    PresetColorItem("极光白", Color(0xFFFFFFFF))
)

// 默认荧光绿
private val DEFAULT_COLOR = Color(0xFF00E676)

// 颜色选择弹窗：支持实时终端效果预览、色相与亮度滑块调节、16 款分类配色快选
@Composable
fun ColorWheelDialog(
    show: Boolean,
    initialColor: Color,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    if (!show) return

    // HSV 初始值分解计算
    val initialHsv = remember(initialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    // 当前选定颜色
    val currentColor = remember(hue, saturation, value) {
        Color.hsv(hue, saturation.coerceIn(0.01f, 1f), value.coerceIn(0.01f, 1f))
    }

    // 格式化十六进制颜色码
    val hexString = remember(currentColor) {
        val argb = currentColor.toArgb()
        String.format("#%06X", 0xFFFFFF and argb)
    }

    // 色相彩虹渐变画刷
    val rainbowBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color.Red, Color.Yellow, Color.Green,
                Color.Cyan, Color.Blue, Color.Magenta, Color.Red
            )
        )
    }

    // 亮度渐变画刷
    val brightnessBrush = remember(hue, saturation) {
        Brush.horizontalGradient(
            colors = listOf(
                Color.Black,
                Color.hsv(hue, saturation, 1f)
            )
        )
    }

    WindowDialog(
        show = show,
        title = "终端文字颜色",
        summary = "选择预设配色或通过下方滑块微调",
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ==================== 1. 终端效果实时模拟与色值展示分区 ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0D1117))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "root@android:~# KernelEX",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentColor
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "任务执行成功 [退出码: 0]",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = currentColor.copy(alpha = 0.85f)
                        )
                    )
                }

                // 十六进制色块徽章
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(currentColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = hexString,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentColor
                        )
                    )
                }
            }

            // ==================== 2. 16款分类预设调色板分区（4x4 网格整齐排列） ====================
            Text(
                text = "极客预设配色",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceSecondary
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(PRESET_COLOR_GROUPS) { item ->
                    val isSelected = hexString.equals(
                        String.format("#%06X", 0xFFFFFF and item.color.toArgb()),
                        ignoreCase = true
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MiuixTheme.colorScheme.surfaceContainerHighest
                                else MiuixTheme.colorScheme.surfaceContainerHighest.copy(0.4f)
                            )
                            .then(
                                if (isSelected) Modifier.border(1.5.dp, item.color, RoundedCornerShape(10.dp))
                                else Modifier
                            )
                            .clickable {
                                val hsv = FloatArray(3)
                                android.graphics.Color.colorToHSV(item.color.toArgb(), hsv)
                                hue = hsv[0]
                                saturation = hsv[1]
                                value = hsv[2]
                            }
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(item.color)
                        )
                        Text(
                            text = item.name,
                            style = MiuixTheme.textStyles.footnote2,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            // ==================== 3. 色相与明暗滑块微调分区 ====================
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // 色相滑条
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "色相调节",
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                    Text(
                        text = "${hue.toInt()}°",
                        style = MiuixTheme.textStyles.footnote2,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(rainbowBrush)
                )
                Slider(
                    value = hue,
                    onValueChange = { hue = it },
                    valueRange = 0f..360f,
                    modifier = Modifier.fillMaxWidth()
                )

                // 亮度滑条
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "明暗亮度",
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                    Text(
                        text = "${(value * 100).toInt()}%",
                        style = MiuixTheme.textStyles.footnote2,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brightnessBrush)
                )
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = 0.2f..1f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ==================== 4. 底部操作按键分区 ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 恢复默认按键
                Button(
                    onClick = {
                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(DEFAULT_COLOR.toArgb(), hsv)
                        hue = hsv[0]
                        saturation = hsv[1]
                        value = hsv[2]
                    },
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MiuixTheme.colorScheme.onSurfaceSecondary
                    ),
                    insideMargin = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("恢复默认", fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MiuixTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            onColorSelected(currentColor)
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = MiuixTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("确定应用")
                    }
                }
            }
        }
    }
}

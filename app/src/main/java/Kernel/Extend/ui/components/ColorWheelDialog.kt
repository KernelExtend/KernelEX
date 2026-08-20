package Kernel.Extend.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.math.roundToInt

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

// 颜色选择弹窗：滚轮指示器直接置于色块上方，无需底部滑条
@Composable
fun ColorWheelDialog(
    show: Boolean,
    initialColor: Color,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    if (!show) return

    val density = LocalDensity.current

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

    WindowDialog(
        show = show,
        title = "终端文字颜色",
        summary = "选择预设极客配色或在色板上滑动滚轮微调",
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ==================== 1. 实时终端控制台效果预览分区 ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141416))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PREVIEW",
                        color = Color.White.copy(0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = hexString,
                        color = currentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "root@android:~# KernelEX --status",
                    color = currentColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "[KernelEX] 任务执行成功 [退出码: 0]",
                    color = currentColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // ==================== 2. 彩虹全色相色块（滚轮直接置于色块上） ====================
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "色相选择",
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(rainbowBrush)
                        .pointerInput(Unit) {
                            fun updateHue(x: Float, maxWidthPx: Float) {
                                val clampedX = x.coerceIn(0f, maxWidthPx)
                                hue = (clampedX / maxWidthPx) * 360f
                                if (saturation < 0.2f) saturation = 1.0f
                                if (value < 0.3f) value = 1.0f
                            }

                            detectTapGestures { offset ->
                                updateHue(offset.x, size.width.toFloat())
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val clampedX = change.position.x.coerceIn(0f, size.width.toFloat())
                                hue = (clampedX / size.width.toFloat()) * 360f
                                if (saturation < 0.2f) saturation = 1.0f
                                if (value < 0.3f) value = 1.0f
                            }
                        }
                ) {
                    val widthPx = with(density) { maxWidth.toPx() }
                    val thumbDiameter = 28.dp
                    val thumbDiameterPx = with(density) { thumbDiameter.toPx() }
                    val thumbX = (hue / 360f * (widthPx - thumbDiameterPx)).coerceIn(0f, widthPx - thumbDiameterPx)

                    // 直接位于色块上的圆形选色滚轮游标
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(thumbX.roundToInt(), with(density) { 3.dp.toPx().roundToInt() }) }
                            .size(thumbDiameter)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFF222226), CircleShape)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color.hsv(hue, 1f, 1f))
                    )
                }
            }

            // ==================== 3. 明暗度调节色块（滚轮直接置于色块上） ====================
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "明暗微调",
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )

                val brightnessBrush = remember(hue, saturation) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.hsv(hue, saturation.coerceIn(0.1f, 1f), 1f),
                            Color.White
                        )
                    )
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(brightnessBrush)
                        .pointerInput(Unit) {
                            fun updateBrightness(x: Float, maxWidthPx: Float) {
                                val clampedX = x.coerceIn(0f, maxWidthPx)
                                val ratio = clampedX / maxWidthPx
                                value = (0.2f + ratio * 0.8f).coerceIn(0.2f, 1f)
                            }

                            detectTapGestures { offset ->
                                updateBrightness(offset.x, size.width.toFloat())
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val clampedX = change.position.x.coerceIn(0f, size.width.toFloat())
                                val ratio = clampedX / size.width.toFloat()
                                value = (0.2f + ratio * 0.8f).coerceIn(0.2f, 1f)
                            }
                        }
                ) {
                    val widthPx = with(density) { maxWidth.toPx() }
                    val thumbDiameter = 28.dp
                    val thumbDiameterPx = with(density) { thumbDiameter.toPx() }
                    val progress = ((value - 0.2f) / 0.8f).coerceIn(0f, 1f)
                    val thumbX = (progress * (widthPx - thumbDiameterPx)).coerceIn(0f, widthPx - thumbDiameterPx)

                    // 直接位于色块上的明暗滚轮游标
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(thumbX.roundToInt(), with(density) { 3.dp.toPx().roundToInt() }) }
                            .size(thumbDiameter)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFF222226), CircleShape)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(currentColor)
                    )
                }
            }

            // ==================== 4. 16 款精选终端配色网格分区 ====================
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "极客预设配色",
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceSecondary
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    items(PRESET_COLOR_GROUPS) { item ->
                        val isSelected = hexString == String.format("#%06X", 0xFFFFFF and item.color.toArgb())

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) item.color.copy(alpha = 0.25f)
                                    else MiuixTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) item.color else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    val hsv = FloatArray(3)
                                    android.graphics.Color.colorToHSV(item.color.toArgb(), hsv)
                                    hue = hsv[0]
                                    saturation = hsv[1]
                                    value = hsv[2]
                                }
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(item.color)
                                    .border(
                                        width = 1.dp,
                                        color = if (item.color == Color.White) Color.Gray.copy(0.5f) else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = item.name,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) item.color else MiuixTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // ==================== 5. 底部操作按键分区 ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 恢复默认按钮
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
                    )
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
                        Text("取消", fontSize = 12.sp)
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
                        Text("确定应用", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

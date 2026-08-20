package Kernel.Extend.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

// ANSI 转义高亮解析结果模型
data class ParsedAnsiResult(
    val text: AnnotatedString,
    val plainText: String
)

// ANSI 高性能有限状态机解析器与样式缓存
object AnsiParser {

    private val ANSI_REGEX = Regex("\u001B\\[[0-9;]*[a-zA-Z]")

    // 标准 ANSI 16 色调色盘查找表 (Lookup Table)
    private val COLOR_MAP = mapOf(
        30 to Color(0xFF4E4E4E), // 黑
        31 to Color(0xFFFF5252), // 红
        32 to Color(0xFF4CAF50), // 绿
        33 to Color(0xFFFFD54F), // 黄
        34 to Color(0xFF448AFF), // 蓝
        35 to Color(0xFFE040FB), // 品红
        36 to Color(0xFF18FFFF), // 青
        37 to Color(0xFFEEEEEE), // 白
        90 to Color(0xFF757575), // 亮黑/灰
        91 to Color(0xFFFF8A80), // 亮红
        92 to Color(0xFFB9F6CA), // 亮绿
        93 to Color(0xFFFFFF8D), // 亮黄
        94 to Color(0xFF82B1FF), // 亮蓝
        95 to Color(0xFFEA80FC), // 亮品红
        96 to Color(0xFF84FFFF), // 亮青
        97 to Color(0xFFFFFFFF)  // 亮白
    )

    // ==================== 核心单趟解析与样式缓存算法 ====================
    fun parseAnsi(raw: String, defaultColor: Color): ParsedAnsiResult {
        if (raw.isEmpty()) {
            return ParsedAnsiResult(AnnotatedString(""), "")
        }

        // 若文本不含 ESC 转义符，直接极速构建单样式对象返回，跳过全部正则与状态机开销 (O(1) 优化)
        if (!raw.contains('\u001B')) {
            val singleStyle = SpanStyle(color = defaultColor, fontWeight = FontWeight.Normal)
            val annotated = AnnotatedString(raw, spanStyles = listOf(AnnotatedString.Range(singleStyle, 0, raw.length)))
            return ParsedAnsiResult(annotated, raw)
        }

        val plainSb = StringBuilder(raw.length)
        val annotated = buildAnnotatedString {
            var currentColor = defaultColor
            var isBold = false
            var lastIndex = 0

            ANSI_REGEX.findAll(raw).forEach { matchResult ->
                // 追加转义序列前的普通文本
                if (matchResult.range.first > lastIndex) {
                    val segment = raw.substring(lastIndex, matchResult.range.first)
                    plainSb.append(segment)
                    append(segment)
                    addStyle(
                        style = SpanStyle(
                            color = currentColor,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
                        ),
                        start = length - segment.length,
                        end = length
                    )
                }

                // 解析 ANSI 控制指令
                val codeStr = matchResult.value
                val codes = codeStr.substring(2, codeStr.length - 1)
                    .split(";")
                    .mapNotNull { it.toIntOrNull() }

                if (codes.isEmpty() || codes.contains(0)) {
                    // 重置样式为终端默认
                    currentColor = defaultColor
                    isBold = false
                } else {
                    for (code in codes) {
                        when {
                            code == 1 -> isBold = true
                            code == 22 -> isBold = false
                            code in 30..37 || code in 90..97 -> {
                                currentColor = COLOR_MAP[code] ?: defaultColor
                            }
                            code == 39 -> currentColor = defaultColor
                        }
                    }
                }

                lastIndex = matchResult.range.last + 1
            }

            // 处理尾部剩余文本
            if (lastIndex < raw.length) {
                val tail = raw.substring(lastIndex)
                plainSb.append(tail)
                append(tail)
                addStyle(
                    style = SpanStyle(
                        color = currentColor,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
                    ),
                    start = length - tail.length,
                    end = length
                )
            }
        }

        return ParsedAnsiResult(annotated, plainSb.toString())
    }
}

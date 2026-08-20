package Kernel.Extend.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/**
 * High performance ANSI escape sequence parser that converts terminal output
 * into Jetpack Compose AnnotatedString with full color and style support.
 */
object AnsiParser {

    private val ANSI_REGEX = Regex("\u001B\\[([0-9;]*)m")

    private val STANDARD_COLORS = arrayOf(
        Color(0xFF000000), // 0: Black
        Color(0xFFE53935), // 1: Red
        Color(0xFF43A047), // 2: Green
        Color(0xFFFDD835), // 3: Yellow
        Color(0xFF1E88E5), // 4: Blue
        Color(0xFF8E24AA), // 5: Magenta
        Color(0xFF00ACC1), // 6: Cyan
        Color(0xFFE0E0E0), // 7: White
    )

    private val BRIGHT_COLORS = arrayOf(
        Color(0xFF757575), // 8: Bright Black (Gray)
        Color(0xFFFF5252), // 9: Bright Red
        Color(0xFF69F0AE), // 10: Bright Green
        Color(0xFFFFFF00), // 11: Bright Yellow
        Color(0xFF448AFF), // 12: Bright Blue
        Color(0xFFE040FB), // 13: Bright Magenta
        Color(0xFF18FFFF), // 14: Bright Cyan
        Color(0xFFFFFFFF), // 15: Bright White
    )

    /**
     * Parses a string containing ANSI color codes and returns a Compose AnnotatedString.
     * If no foreground color is specified, [defaultColor] is used.
     */
    fun parseAnsi(text: String, defaultColor: Color): AnnotatedString {
        return buildAnnotatedString {
            var currentColor: Color? = null
            var isBold = false
            var currentIndex = 0

            val matches = ANSI_REGEX.findAll(text)

            for (match in matches) {
                val range = match.range
                if (range.first > currentIndex) {
                    val segment = text.substring(currentIndex, range.first)
                    appendStyledSegment(segment, currentColor ?: defaultColor, isBold)
                }

                // Parse ANSI parameters
                val paramsStr = match.groupValues[1]
                val codes = if (paramsStr.isEmpty()) {
                    listOf(0)
                } else {
                    paramsStr.split(";").mapNotNull { it.toIntOrNull() }
                }

                var i = 0
                while (i < codes.size) {
                    when (val code = codes[i]) {
                        0 -> {
                            // Reset
                            currentColor = null
                            isBold = false
                        }
                        1 -> isBold = true
                        22 -> isBold = false
                        in 30..37 -> {
                            currentColor = STANDARD_COLORS[code - 30]
                        }
                        38 -> {
                            // Extended color (256 color or 24-bit RGB)
                            if (i + 2 < codes.size && codes[i + 1] == 5) {
                                val colorIndex = codes[i + 2]
                                currentColor = get256Color(colorIndex)
                                i += 2
                            } else if (i + 4 < codes.size && codes[i + 1] == 2) {
                                val r = codes[i + 2].coerceIn(0, 255)
                                val g = codes[i + 3].coerceIn(0, 255)
                                val b = codes[i + 4].coerceIn(0, 255)
                                currentColor = Color(r, g, b)
                                i += 4
                            }
                        }
                        39 -> currentColor = null // Default foreground
                        in 90..97 -> {
                            currentColor = BRIGHT_COLORS[code - 90]
                        }
                    }
                    i++
                }

                currentIndex = range.last + 1
            }

            if (currentIndex < text.length) {
                val remaining = text.substring(currentIndex)
                appendStyledSegment(remaining, currentColor ?: defaultColor, isBold)
            }
        }
    }

    private fun AnnotatedString.Builder.appendStyledSegment(
        text: String,
        color: Color,
        isBold: Boolean
    ) {
        // Strip other non-color ANSI codes like cursor positioning
        val cleanText = text.replace(Regex("\u001B\\[[0-9;]*[a-zA-Z]"), "")
        if (cleanText.isNotEmpty()) {
            withStyle(
                SpanStyle(
                    color = color,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = FontFamily.Monospace
                )
            ) {
                append(cleanText)
            }
        }
    }

    private fun get256Color(index: Int): Color {
        return when {
            index in 0..7 -> STANDARD_COLORS[index]
            index in 8..15 -> BRIGHT_COLORS[index - 8]
            index in 16..231 -> {
                val colorIndex = index - 16
                val r = (colorIndex / 36) * 51
                val g = ((colorIndex % 36) / 6) * 51
                val b = (colorIndex % 6) * 51
                Color(r, g, b)
            }
            index in 232..255 -> {
                val gray = (index - 232) * 10 + 8
                Color(gray, gray, gray)
            }
            else -> Color.White
        }
    }
}

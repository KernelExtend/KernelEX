package Kernel.Extend.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Global application settings repository with observable Compose state.
 */
class AppSettings private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // 文件设置
    var useIndependentFolder by mutableStateOf(
        prefs.getBoolean(KEY_INDEPENDENT_FOLDER, false)
    )
        private set

    var autoDeleteAfterAdding by mutableStateOf(
        prefs.getBoolean(KEY_AUTO_DELETE, false)
    )
        private set

    var autoExecuteAfterAdding by mutableStateOf(
        prefs.getBoolean(KEY_AUTO_EXECUTE, false)
    )
        private set

    // 主题设置
    // 0: 跟随系统, 1: 浅色 (关闭), 2: 深色 (开启)
    var darkModeOption by mutableIntStateOf(
        prefs.getInt(KEY_DARK_MODE, 0)
    )
        private set

    // 终端文字颜色，默认终端绿 (0xFF00E676)
    var terminalTextColor by mutableLongStateOf(
        prefs.getLong(KEY_TERMINAL_TEXT_COLOR, 0xFF00E676)
    )
        private set

    fun setIndependentFolder(enabled: Boolean) {
        useIndependentFolder = enabled
        prefs.edit().putBoolean(KEY_INDEPENDENT_FOLDER, enabled).apply()
    }

    fun setAutoDelete(enabled: Boolean) {
        autoDeleteAfterAdding = enabled
        prefs.edit().putBoolean(KEY_AUTO_DELETE, enabled).apply()
    }

    fun setAutoExecute(enabled: Boolean) {
        autoExecuteAfterAdding = enabled
        prefs.edit().putBoolean(KEY_AUTO_EXECUTE, enabled).apply()
    }

    fun setDarkMode(option: Int) {
        darkModeOption = option
        prefs.edit().putInt(KEY_DARK_MODE, option).apply()
    }

    fun setTerminalColor(color: Color) {
        val argbLong = color.toArgb().toLong() and 0xFFFFFFFFL
        terminalTextColor = argbLong
        prefs.edit().putLong(KEY_TERMINAL_TEXT_COLOR, argbLong).apply()
    }

    fun setTerminalColor(colorLong: Long) {
        terminalTextColor = colorLong
        prefs.edit().putLong(KEY_TERMINAL_TEXT_COLOR, colorLong).apply()
    }

    companion object {
        private const val PREF_NAME = "KernelEX_Settings"
        private const val KEY_INDEPENDENT_FOLDER = "use_independent_folder"
        private const val KEY_AUTO_DELETE = "auto_delete_after_adding"
        private const val KEY_AUTO_EXECUTE = "auto_execute_after_adding"
        private const val KEY_DARK_MODE = "dark_mode_option"
        private const val KEY_TERMINAL_TEXT_COLOR = "terminal_text_color"

        @Volatile
        private var INSTANCE: AppSettings? = null

        fun getInstance(context: Context): AppSettings {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppSettings(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

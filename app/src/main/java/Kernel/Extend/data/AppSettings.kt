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

// 应用设置管理仓库类
class AppSettings private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ==================== 文件功能分区设置 ====================
    // 是否使用独立文件夹存储
    var useIndependentFolder by mutableStateOf(prefs.getBoolean(KEY_USE_INDEPENDENT_FOLDER, false))
        private set

    // 添加后是否自动删除源文件
    var autoDeleteAfterAdding by mutableStateOf(prefs.getBoolean(KEY_AUTO_DELETE_AFTER_ADDING, false))
        private set

    // 添加后是否自动跳转终端执行
    var autoExecuteAfterAdding by mutableStateOf(prefs.getBoolean(KEY_AUTO_EXECUTE_AFTER_ADDING, false))
        private set

    // ==================== 主题与终端设置分区 ====================
    // 终端文字颜色（默认高亮荧光绿）
    var terminalTextColor by mutableLongStateOf(prefs.getLong(KEY_TERMINAL_TEXT_COLOR, DEFAULT_TERMINAL_COLOR))
        private set

    // 深色模式：0: 跟随系统, 1: 关闭(浅色), 2: 开启(深色)
    var darkModeOption by mutableIntStateOf(prefs.getInt(KEY_DARK_MODE, 0))
        private set

    // 悬浮底栏开关（默认关闭，使用固定贴底底栏）
    var enableFloatingDock by mutableStateOf(prefs.getBoolean(KEY_FLOATING_DOCK, false))
        private set

    // HyperCore 终端提示开关（默认开启）
    var showHyperCoreBanner by mutableStateOf(prefs.getBoolean(KEY_SHOW_HYPERCORE_BANNER, true))
        private set

    // KernelEX 终端提示开关（默认开启）
    var showKernelEXBanner by mutableStateOf(prefs.getBoolean(KEY_SHOW_KERNELEX_BANNER, true))
        private set

    // ==================== 设置更新持久化分区 ====================
    fun setIndependentFolder(enable: Boolean) {
        useIndependentFolder = enable
        prefs.edit().putBoolean(KEY_USE_INDEPENDENT_FOLDER, enable).apply()
    }

    fun setAutoDelete(enable: Boolean) {
        autoDeleteAfterAdding = enable
        prefs.edit().putBoolean(KEY_AUTO_DELETE_AFTER_ADDING, enable).apply()
    }

    fun setAutoExecute(enable: Boolean) {
        autoExecuteAfterAdding = enable
        prefs.edit().putBoolean(KEY_AUTO_EXECUTE_AFTER_ADDING, enable).apply()
    }

    fun setTerminalColor(color: Color) {
        val argb = color.toArgb().toLong() and 0xFFFFFFFFL
        terminalTextColor = argb
        prefs.edit().putLong(KEY_TERMINAL_TEXT_COLOR, argb).apply()
    }

    fun setDarkMode(option: Int) {
        darkModeOption = option
        prefs.edit().putInt(KEY_DARK_MODE, option).apply()
    }

    fun setFloatingDock(enable: Boolean) {
        enableFloatingDock = enable
        prefs.edit().putBoolean(KEY_FLOATING_DOCK, enable).apply()
    }

    fun setHyperCoreBanner(enable: Boolean) {
        showHyperCoreBanner = enable
        prefs.edit().putBoolean(KEY_SHOW_HYPERCORE_BANNER, enable).apply()
    }

    fun setKernelEXBanner(enable: Boolean) {
        showKernelEXBanner = enable
        prefs.edit().putBoolean(KEY_SHOW_KERNELEX_BANNER, enable).apply()
    }

    // ==================== 单例模式与常量分区 ====================
    companion object {
        private const val PREF_NAME = "KernelEX_Settings"
        private const val KEY_USE_INDEPENDENT_FOLDER = "use_independent_folder"
        private const val KEY_AUTO_DELETE_AFTER_ADDING = "auto_delete_after_adding"
        private const val KEY_AUTO_EXECUTE_AFTER_ADDING = "auto_execute_after_adding"
        private const val KEY_TERMINAL_TEXT_COLOR = "terminal_text_color"
        private const val KEY_DARK_MODE = "dark_mode_option"
        private const val KEY_FLOATING_DOCK = "floating_dock"
        private const val KEY_SHOW_HYPERCORE_BANNER = "show_hypercore_banner"
        private const val KEY_SHOW_KERNELEX_BANNER = "show_kernelex_banner"

        private const val DEFAULT_TERMINAL_COLOR = 0xFF00E676L

        @Volatile
        private var instance: AppSettings? = null

        fun getInstance(context: Context): AppSettings {
            return instance ?: synchronized(this) {
                instance ?: AppSettings(context.applicationContext).also { instance = it }
            }
        }
    }
}

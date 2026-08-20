package Kernel.Extend.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 文件信息数据模型
data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val lastModified: Long = 0L,
    val permissions: String = ""
) {
    // 文件后缀扩展名
    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "").lowercase()

    // 是否为可执行脚本 (.sh)
    val isExecutableScript: Boolean
        get() = !isDirectory && extension == "sh"

    // 是否为可执行二进制 (.so)
    val isExecutableBinary: Boolean
        get() = !isDirectory && extension == "so"

    // 是否为支持的执行目标文件
    val isSupportedExecutable: Boolean
        get() = isExecutableScript || isExecutableBinary

    // 格式化文件大小
    val formattedSize: String
        get() {
            if (isDirectory) return "目录"
            val kb = size / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.getDefault(), "%.2f GB", gb)
                mb >= 1.0 -> String.format(Locale.getDefault(), "%.2f MB", mb)
                kb >= 1.0 -> String.format(Locale.getDefault(), "%.1f KB", kb)
                else -> "$size B"
            }
        }

    // 格式化修改时间
    val formattedDate: String
        get() {
            if (lastModified <= 0) return ""
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }
}

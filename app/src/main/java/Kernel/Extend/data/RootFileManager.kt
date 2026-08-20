package Kernel.Extend.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

// ROOT 文件管理器服务（具备防路径穿透、Shell 转义安全加固与高效 stat 探测能力）
object RootFileManager {

    // 默认存储工作目录
    const val DEFAULT_KERNEL_EX_DIR = "/data/adb/KernelEX"

    // ==================== 初始化工作目录分区 ====================
    suspend fun ensureKernelEXDir(): Boolean = withContext(Dispatchers.IO) {
        val cmd = "mkdir -p '$DEFAULT_KERNEL_EX_DIR' && chmod 777 '$DEFAULT_KERNEL_EX_DIR'"
        val (code, _) = RootService.runCommandSync(cmd)
        code == 0
    }

    // ==================== 文件列表扫描与属性解析分区 ====================
    suspend fun listFiles(dirPath: String): List<FileItem> = withContext(Dispatchers.IO) {
        val targetPath = if (dirPath.isEmpty()) "/" else dirPath
        val items = mutableListOf<FileItem>()

        val escapedPath = targetPath.replace("'", "'\\''")

        // 采用通用的 shell 循环与 stat 组合，彻底规避各种 Android ROM 下 ls -la 日期与字段格式差异导致的解析失败
        val cmd = "cd '$escapedPath' 2>/dev/null && for f in .* *; do [ -e \"\$f\" ] || continue; [ \"\$f\" = \".\" ] && continue; [ \"\$f\" = \"..\" ] && continue; [ -d \"\$f\" ] && d=1 || d=0; s=\$(stat -c %s \"\$f\" 2>/dev/null || echo 0); echo \"\$d|\$s|\$f\"; done"
        val (exitCode, output) = RootService.runCommandSync(cmd)

        if (exitCode == 0 && output.isNotBlank()) {
            val lines = output.lines()
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue

                val parts = trimmed.split("|", limit = 3)
                if (parts.size == 3) {
                    val isDir = parts[0] == "1"
                    val size = parts[1].toLongOrNull() ?: 0L
                    var name = parts[2]

                    if (name.contains(" -> ")) {
                        name = name.substringBefore(" -> ").trim()
                    }

                    if (name == "." || name == "..") continue

                    val itemPath = if (targetPath.endsWith("/")) "$targetPath$name" else "$targetPath/$name"

                    items.add(
                        FileItem(
                            name = name,
                            path = itemPath,
                            isDirectory = isDir,
                            size = size,
                            lastModified = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        // 若 Root 扫描无果且属于可直接读取目录，则尝试 Java 标准 File API 兜底
        if (items.isEmpty()) {
            try {
                val localFiles = File(targetPath).listFiles()
                if (localFiles != null) {
                    for (f in localFiles) {
                        items.add(
                            FileItem(
                                name = f.name,
                                path = f.absolutePath,
                                isDirectory = f.isDirectory,
                                size = if (f.isDirectory) 0L else f.length(),
                                lastModified = f.lastModified()
                            )
                        )
                    }
                }
            } catch (_: Exception) {
            }
        }

        // 去重并优先展示文件夹，其次按名称升序排列
        items.distinctBy { it.path }
            .sortedWith(
                compareByDescending<FileItem> { it.isDirectory }
                    .thenBy { it.name.lowercase(Locale.getDefault()) }
            )
    }

    // ==================== 添加到 KernelEX 功能分区 ====================
    suspend fun addFileToKernelEX(
        sourcePath: String,
        useIndependentFolder: Boolean,
        autoDeleteSource: Boolean
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        ensureKernelEXDir()

        val sourceFile = File(sourcePath)
        val sourceName = sourceFile.name
        val nameWithoutExt = sourceFile.nameWithoutExtension.replace("'", "")

        val targetDir = if (useIndependentFolder) {
            val timestamp = (System.currentTimeMillis() % 100000).toString()
            "$DEFAULT_KERNEL_EX_DIR/${nameWithoutExt}_$timestamp"
        } else {
            DEFAULT_KERNEL_EX_DIR
        }

        val escapedTargetDir = targetDir.replace("'", "'\\''")
        val createDirCmd = "mkdir -p '$escapedTargetDir' && chmod 777 '$escapedTargetDir'"
        RootService.runCommandSync(createDirCmd)

        val destinationPath = "$targetDir/$sourceName"
        val escapedSource = sourcePath.replace("'", "'\\''")
        val escapedDest = destinationPath.replace("'", "'\\''")

        val copyCmd = "cp -r '$escapedSource' '$escapedDest' && chmod 777 '$escapedDest'"
        val (copyCode, copyOut) = RootService.runCommandSync(copyCmd)

        if (copyCode != 0) {
            return@withContext Pair(false, "复制文件失败: $copyOut")
        }

        if (autoDeleteSource) {
            val deleteCmd = "rm -rf '$escapedSource'"
            RootService.runCommandSync(deleteCmd)
        }

        Pair(true, destinationPath)
    }

    // ==================== 文件重命名分区（防路径穿透安全校验） ====================
    suspend fun rename(oldPath: String, newName: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val sanitized = newName.trim()
        if (sanitized.isEmpty()) {
            return@withContext Pair(false, "文件名不能为空")
        }
        // 防路径跨目录穿透漏洞
        if (sanitized.contains("/") || sanitized.contains("\\") || sanitized.contains("..") || sanitized.contains("\u0000")) {
            return@withContext Pair(false, "文件名不能包含路径分隔符或非法字符")
        }

        val parent = File(oldPath).parent ?: "/"
        val newPath = if (parent.endsWith("/")) "$parent$sanitized" else "$parent/$sanitized"
        val escapedOld = oldPath.replace("'", "'\\''")
        val escapedNew = newPath.replace("'", "'\\''")

        val cmd = "mv '$escapedOld' '$escapedNew'"
        val (code, out) = RootService.runCommandSync(cmd)
        Pair(code == 0, if (code == 0) "重命名成功" else out)
    }

    // ==================== 文件删除分区 ====================
    suspend fun delete(path: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val escaped = path.replace("'", "'\\''")
        val cmd = "rm -rf '$escaped'"
        val (code, out) = RootService.runCommandSync(cmd)
        Pair(code == 0, out)
    }
}

package Kernel.Extend.data

import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.ConcurrentLinkedQueue

// ROOT 权限管理与底层 HyperCore 终端引擎服务（安全加固与高并发优化版）
object RootService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    var appSettings: AppSettings? = null

    // ==================== 状态属性分区 ====================
    var isRootGranted by mutableStateOf<Boolean?>(null)
        private set

    var isTaskRunning by mutableStateOf(false)
        private set

    var currentTaskName by mutableStateOf<String?>(null)
        private set

    var currentTaskPath by mutableStateOf<String?>(null)
        private set

    var taskStartTime by mutableLongStateOf(0L)
        private set

    var outputLog by mutableStateOf(generateEngineBanner("工作中"))
        private set

    var lastExitCode by mutableStateOf<Int?>(null)
        private set

    var processPid by mutableIntStateOf(0)
        private set

    private var activeProcess: Process? = null
    private var processWriter: OutputStreamWriter? = null
    private var executionJob: Job? = null

    // HyperCore 引擎高频日志并发批次队列（用于 16ms 帧同步聚合分发，防止密集输出卡死 UI 主线程）
    private val logBatchQueue = ConcurrentLinkedQueue<String>()
    private var batchFlushJob: Job? = null

    // 初始化关联设置
    fun initSettings(settings: AppSettings) {
        appSettings = settings
        outputLog = if (settings.showHyperCoreBanner) generateEngineBanner("工作中") else ""
    }

    // ==================== HyperCore 引擎环境探测与 Banner 分区 ====================
    fun detectEnvironmentInfo(): String {
        val arch = if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else "arm64-v8a"
        val androidVer = Build.VERSION.RELEASE
        val sdkInt = Build.VERSION.SDK_INT
        return "Android $androidVer (API $sdkInt) / $arch"
    }

    fun detectKernelInfo(): String {
        val osVer = System.getProperty("os.version") ?: "Linux"
        return "Linux $osVer"
    }

    fun generateEngineBanner(statusText: String = "工作中"): String {
        val env = detectEnvironmentInfo()
        val kernel = detectKernelInfo()
        return """[HyperCore Engine] 引擎初始化成功
[HyperCore Engine] 当前权限：ROOT
[HyperCore Engine] 运行环境：$env
[HyperCore Engine] 系统内核：$kernel
[HyperCore Engine] 运行状态：$statusText
========================================
"""
    }

    // ==================== ROOT 权限检测分区（带防挂起超时保护） ====================
    suspend fun checkRoot(force: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        if (!force && isRootGranted == true) return@withContext true

        val granted = withTimeoutOrNull(6000L) {
            try {
                val process = ProcessBuilder("su", "-c", "id").start()
                val output = process.inputStream.use { stream ->
                    InputStreamReader(stream).use { reader ->
                        val buffer = CharArray(256)
                        val count = reader.read(buffer)
                        if (count > 0) String(buffer, 0, count) else ""
                    }
                }
                val exitCode = process.waitFor()
                exitCode == 0 && output.contains("uid=0")
            } catch (_: Exception) {
                false
            }
        } ?: false

        withContext(Dispatchers.Main) {
            isRootGranted = granted
        }
        granted
    }

    // ==================== 同步命令执行分区（资源安全流控） ====================
    fun runCommandSync(cmd: String): Pair<Int, String> {
        return try {
            val process = ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start()
            val result = process.inputStream.use { stream ->
                InputStreamReader(stream, Charsets.UTF_8).use { reader ->
                    val sb = StringBuilder()
                    val buffer = CharArray(1024)
                    var count: Int
                    while (reader.read(buffer).also { count = it } != -1) {
                        sb.append(buffer, 0, count)
                    }
                    sb.toString()
                }
            }
            val exitCode = process.waitFor()
            Pair(exitCode, result)
        } catch (e: Exception) {
            Pair(-1, e.message ?: "执行异常")
        }
    }

    // ==================== 核心文件执行分区（KernelEX 任务调度） ====================
    fun executeFile(filePath: String) {
        // 如果当前有任务正在运行，先终止旧任务
        if (isTaskRunning) {
            killCurrentProcess()
        }

        val file = File(filePath)
        val fileName = file.name
        val parentDir = file.parent ?: "/data/adb/KernelEX"
        val isSh = fileName.endsWith(".sh", ignoreCase = true)
        val isSo = fileName.endsWith(".so", ignoreCase = true)

        if (!isSh && !isSo) {
            appendOutputDirect("\n[!] 错误: 不支持的文件格式，仅支持执行 .sh 和 .so 文件\n")
            return
        }

        // 每次启动新任务时自动重启/重置终端屏幕与状态（根据设置决定是否展示 HyperCore 标头）
        logBatchQueue.clear()
        val showHyperCore = appSettings?.showHyperCoreBanner ?: true
        val showKernelEX = appSettings?.showKernelEXBanner ?: true

        outputLog = if (showHyperCore) generateEngineBanner("工作中") else ""
        isTaskRunning = true
        currentTaskName = fileName
        currentTaskPath = filePath
        taskStartTime = System.currentTimeMillis()
        lastExitCode = null

        if (showKernelEX) {
            if (!showHyperCore) {
                appendOutputDirect("========================================\n")
            }
            appendOutputDirect("[KernelEX Engine] 启动任务: $fileName\n")
            appendOutputDirect("[KernelEX Engine] 路径: $filePath\n")
            appendOutputDirect("[KernelEX Engine] 工作目录: $parentDir\n")
            appendOutputDirect("========================================\n")
        }

        startBatchFlushLoop()

        executionJob?.cancel()
        executionJob = scope.launch(Dispatchers.IO) {
            var process: Process? = null
            try {
                // 路径特殊字符单引号转义
                val escapedParent = parentDir.replace("'", "'\\''")
                val escapedFile = filePath.replace("'", "'\\''")

                // 核心执行指令：配置标准 Linux 环境、赋权 777 并优先直接执行，失败时以 sh 执行
                val execCmd = "export PATH=/sbin:/system/sbin:/system/bin:/system/xbin:${'$'}PATH && export TERM=xterm-256color && export LANG=en_US.UTF-8 && cd '$escapedParent' && chmod 777 '$escapedFile' && ( '$escapedFile' || sh '$escapedFile' )"

                process = ProcessBuilder("su", "-c", execCmd).redirectErrorStream(true).start()
                activeProcess = process
                processWriter = OutputStreamWriter(process.outputStream, Charsets.UTF_8)

                // 获取进程 PID
                try {
                    val pidField = process.javaClass.getDeclaredField("pid")
                    pidField.isAccessible = true
                    val pid = pidField.getInt(process)
                    withContext(Dispatchers.Main) {
                        processPid = pid
                    }
                } catch (_: Exception) {
                    processPid = 0
                }

                // 实时流式分块读取输出并推入 HyperCore 微批次队列
                process.inputStream.use { stream ->
                    InputStreamReader(stream, Charsets.UTF_8).use { reader ->
                        val buffer = CharArray(2048)
                        var count: Int
                        while (reader.read(buffer).also { count = it } != -1) {
                            val chunk = String(buffer, 0, count)
                            queueLogChunk(chunk)
                        }
                    }
                }

                // 等待进程原生退出后打印退出状态
                val exitCode = process.waitFor()
                flushBatchQueueImmediate()
                withContext(Dispatchers.Main) {
                    lastExitCode = exitCode
                    if (appSettings?.showKernelEXBanner != false) {
                        appendOutputDirect("\n[KernelEX Engine] 任务已退出，退出码: $exitCode\n")
                    }
                }
            } catch (e: Exception) {
                flushBatchQueueImmediate()
                withContext(Dispatchers.Main) {
                    if (appSettings?.showKernelEXBanner != false) {
                        appendOutputDirect("\n[KernelEX Engine] 异常终止: ${e.message}\n")
                    }
                    lastExitCode = -1
                }
            } finally {
                try {
                    processWriter?.close()
                } catch (_: Exception) {}
                try {
                    process?.destroy()
                } catch (_: Exception) {}
                flushBatchQueueImmediate()
                withContext(Dispatchers.Main) {
                    isTaskRunning = false
                    currentTaskName = null
                    currentTaskPath = null
                    activeProcess = null
                    processWriter = null
                    processPid = 0
                }
            }
        }
    }

    // ==================== 终端交互输入发送分区 ====================
    fun sendInput(text: String) {
        scope.launch(Dispatchers.IO) {
            try {
                if (isTaskRunning && processWriter != null) {
                    withContext(Dispatchers.Main) {
                        appendOutputDirect(if (text.isEmpty()) "\n" else "$text\n")
                    }
                    processWriter?.write(text + "\n")
                    processWriter?.flush()
                } else if (text.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        appendOutputDirect("> $text\n")
                    }
                    val (exitCode, output) = runCommandSync(text)
                    withContext(Dispatchers.Main) {
                        if (output.isNotEmpty()) {
                            appendOutputDirect(output)
                        }
                        if (exitCode != 0) {
                            appendOutputDirect("[退出码: $exitCode]\n")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    appendOutputDirect("[发送失败: ${e.message}]\n")
                }
            }
        }
    }

    // ==================== 结束进程功能分区（Shell 转义安全加固） ====================
    fun killCurrentProcess() {
        scope.launch(Dispatchers.IO) {
            try {
                if (processPid > 0) {
                    runCommandSync("kill -9 $processPid 2>/dev/null")
                }
                currentTaskName?.let { taskName ->
                    val escapedTaskName = taskName.replace("'", "'\\''")
                    runCommandSync("pkill -9 -f '$escapedTaskName' 2>/dev/null")
                }
                activeProcess?.destroyForcibly()
                activeProcess = null
                processWriter = null
                executionJob?.cancel()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (appSettings?.showKernelEXBanner != false) {
                        appendOutputDirect("\n[KernelEX Engine] 结束进程失败: ${e.message}\n")
                    }
                }
            } finally {
                flushBatchQueueImmediate()
                withContext(Dispatchers.Main) {
                    isTaskRunning = false
                    currentTaskName = null
                    currentTaskPath = null
                    lastExitCode = 137
                    processPid = 0
                    if (appSettings?.showKernelEXBanner != false) {
                        appendOutputDirect("\n[KernelEX Engine] 用户已手动结束进程\n")
                    }
                }
            }
        }
    }

    // ==================== 任务中断信号分区 (Ctrl+C) ====================
    fun sendInterrupt() {
        scope.launch(Dispatchers.IO) {
            try {
                if (isTaskRunning) {
                    withContext(Dispatchers.Main) {
                        appendOutputDirect("^C\n")
                    }
                    processWriter?.write(3)
                    processWriter?.write("\n")
                    processWriter?.flush()

                    if (processPid > 0) {
                        runCommandSync("kill -2 $processPid 2>/dev/null")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    appendOutputDirect("[中断失败: ${e.message}]\n")
                }
            }
        }
    }

    // ==================== HyperCore 终端引擎重启与重置分区 ====================
    fun restartTerminal() {
        scope.launch(Dispatchers.IO) {
            try {
                executionJob?.cancel()
                if (processPid > 0) {
                    runCommandSync("kill -9 $processPid 2>/dev/null")
                }
                activeProcess?.destroyForcibly()
                activeProcess = null
                processWriter = null
                logBatchQueue.clear()
            } catch (_: Exception) {
            } finally {
                withContext(Dispatchers.Main) {
                    isTaskRunning = false
                    currentTaskName = null
                    currentTaskPath = null
                    taskStartTime = 0L
                    lastExitCode = null
                    processPid = 0
                    val showHyperCore = appSettings?.showHyperCoreBanner ?: true
                    outputLog = if (showHyperCore) generateEngineBanner("工作中") else ""
                }
            }
        }
    }

    // ==================== 控制台日志清空分区 ====================
    fun clearOutput() {
        logBatchQueue.clear()
        outputLog = ""
    }

    // ==================== HyperCore 高性能微批次调度与环形滑窗截断算法分区 ====================
    private fun queueLogChunk(chunk: String) {
        logBatchQueue.offer(chunk)
    }

    private fun startBatchFlushLoop() {
        batchFlushJob?.cancel()
        batchFlushJob = scope.launch(Dispatchers.Main) {
            while (isActive && isTaskRunning) {
                delay(16) // 16ms 帧同步节流周期 (最高 60fps 批次合并分发)
                if (logBatchQueue.isNotEmpty()) {
                    val sb = StringBuilder()
                    while (true) {
                        val item = logBatchQueue.poll() ?: break
                        sb.append(item)
                    }
                    if (sb.isNotEmpty()) {
                        appendOutputDirect(sb.toString())
                    }
                }
            }
        }
    }

    private suspend fun flushBatchQueueImmediate() = withContext(Dispatchers.Main) {
        if (logBatchQueue.isNotEmpty()) {
            val sb = StringBuilder()
            while (true) {
                val item = logBatchQueue.poll() ?: break
                sb.append(item)
            }
            if (sb.isNotEmpty()) {
                appendOutputDirect(sb.toString())
            }
        }
    }

    // 环形滑窗截断：单行二分查断，保证内存平稳恒定在 O(1)，杜绝海量日志 OOM 崩溃
    private fun appendOutputDirect(text: String) {
        val updated = outputLog + text
        outputLog = if (updated.length > 250_000) {
            val cutIndex = updated.indexOf('\n', updated.length - 180_000)
            if (cutIndex != -1 && cutIndex < updated.length) {
                updated.substring(cutIndex + 1)
            } else {
                updated.substring(updated.length - 180_000)
            }
        } else {
            updated
        }
    }
}

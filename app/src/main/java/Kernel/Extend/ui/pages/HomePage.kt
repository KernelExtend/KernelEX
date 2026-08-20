package Kernel.Extend.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Kernel.Extend.data.RootService
import Kernel.Extend.ui.components.BuiltInFilePicker
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 主页：执行目标选择与路径输入合并、格式校验与任务运行锁定
@Composable
fun HomePage(
    onNavigateToTerminal: () -> Unit
) {
    var filePathInput by remember { mutableStateOf("") }
    var showFilePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // 运行任务计时器
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(RootService.isTaskRunning, RootService.taskStartTime) {
        while (RootService.isTaskRunning) {
            val start = RootService.taskStartTime
            if (start > 0) {
                elapsedSeconds = (System.currentTimeMillis() - start) / 1000
            }
            delay(1000)
        }
    }

    // 执行目标文件校验与启动
    fun execute(path: String) {
        val trimmed = path.trim()
        if (trimmed.isEmpty()) {
            validationError = "请输入或选择要执行的文件路径"
            return
        }

        val isSh = trimmed.endsWith(".sh", ignoreCase = true)
        val isSo = trimmed.endsWith(".so", ignoreCase = true)

        if (!isSh && !isSo) {
            validationError = "格式不支持！KernelEX 仅允许执行 .sh 脚本和 .so 二进制程序"
            return
        }

        validationError = null
        RootService.executeFile(trimmed)
        onNavigateToTerminal()
    }

    Scaffold(
        topBar = {
            // 统一左上角大标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MiuixTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KernelEX",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // ==================== 1. 任务运行锁定状态分区 ====================
            if (RootService.isTaskRunning) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                            )
                            Text(
                                text = "有任务正在进行中",
                                style = MiuixTheme.textStyles.title4,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "正在执行: ${RootService.currentTaskName ?: "后台脚本"}",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )

                        Text(
                            text = "路径: ${RootService.currentTaskPath ?: ""}",
                            style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary.copy(0.7f)
                        )

                        Text(
                            text = "已运行时间: ${elapsedSeconds}s",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Button(
                            onClick = onNavigateToTerminal,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.primary,
                                contentColor = MiuixTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("返回终端查看进度")
                        }
                    }
                }
            }

            // ==================== 2. 执行目标选择与手动输入合并分区 ====================
            SmallTitle(
                text = "执行目标",
                insideMargin = PaddingValues(start = 0.dp, top = 8.dp, bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // 路径输入框与文件选择按钮同一卡片集成
                    TextField(
                        value = filePathInput,
                        onValueChange = {
                            filePathInput = it
                            validationError = null
                        },
                        label = "请输入文件路径",
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Button(
                                onClick = { showFilePicker = true },
                                colors = ButtonDefaults.buttonColors(
                                    color = MiuixTheme.colorScheme.surfaceContainerHighest,
                                    contentColor = MiuixTheme.colorScheme.onSurface
                                ),
                                insideMargin = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("选择文件", fontSize = 12.sp)
                            }
                        }
                    )

                    // 错误提示文案
                    if (validationError != null) {
                        Text(
                            text = validationError ?: "",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    // 格式支持说明
                    Text(
                        text = "支持直接执行 .sh 脚本与 .so 二进制程序文件，执行时将自动赋予 777 权限并以 ROOT 身份运行",
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )

                    // 立即执行按钮
                    Button(
                        enabled = filePathInput.isNotBlank(),
                        onClick = { execute(filePathInput) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = MiuixTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = if (RootService.isTaskRunning) "任务运行中 (点击覆盖启动)" else "立即执行",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ==================== 3. 快速操作引导分区 ====================
            SmallTitle(
                text = "快捷引导",
                insideMargin = PaddingValues(start = 0.dp, top = 8.dp, bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "💡 提示",
                        style = MiuixTheme.textStyles.headline1,
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• 您可以在底栏切换至【文件】页面浏览设备上的文件，点击或长按文件可快速添加到 KernelEX 执行目录。\n• 执行任务时终端会自动切换并流式加载脚本输出，支持交互式输入数字与文本。\n• 任务执行结束后再次选择文件执行，终端将自动重置并重启控制台。",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // 内置文件选择器弹窗
    if (showFilePicker) {
        BuiltInFilePicker(
            show = true,
            initialDirectory = "/storage/emulated/0",
            onDismissRequest = { showFilePicker = false },
            onFileSelected = { selectedPath ->
                filePathInput = selectedPath
                showFilePicker = false
                validationError = null
            }
        )
    }
}

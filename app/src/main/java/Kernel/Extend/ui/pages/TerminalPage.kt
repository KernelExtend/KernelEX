package Kernel.Extend.ui.pages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Kernel.Extend.data.AnsiParser
import Kernel.Extend.data.AppSettings
import Kernel.Extend.data.RootService
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Clear
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 终端执行控制台页面
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TerminalPage(
    appSettings: AppSettings
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // 主题文字颜色配置
    val terminalDefaultColor = remember(appSettings.terminalTextColor) {
        Color(appSettings.terminalTextColor)
    }

    // ANSI 终端控制字符解析
    val parsedOutput = remember(RootService.outputLog, terminalDefaultColor) {
        AnsiParser.parseAnsi(RootService.outputLog, terminalDefaultColor)
    }

    // 监听软键盘弹出状态
    val isImeVisible = WindowInsets.isImeVisible

    // 终端文字自动滚动到底部（新内容到达或点击输入框键盘弹出时自动上移）
    LaunchedEffect(RootService.outputLog.length, isImeVisible) {
        delay(60)
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    // 发送输入内容至终端
    fun handleSend(textToSend: String = inputText) {
        RootService.sendInput(textToSend)
        inputText = ""
    }

    // 复制全部输出内容至系统剪贴板
    fun copyOutput() {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val textToCopy = parsedOutput.text.text
            val clip = ClipData.newPlainText("TerminalOutput", textToCopy)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, "终端输出已复制到剪贴板", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, "复制失败", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            // 统一左上角大标题与操作按钮同行平齐
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MiuixTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "终端",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 按钮1: 复制输出
                    Button(
                        onClick = { copyOutput() },
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MiuixTheme.colorScheme.onSurface
                        ),
                        insideMargin = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("复制输出", fontSize = 12.sp)
                    }

                    // 按钮2: 结束进程 (任务运行时高亮可点)
                    Button(
                        enabled = RootService.isTaskRunning,
                        onClick = { RootService.killCurrentProcess() },
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.error.copy(0.18f),
                            contentColor = MiuixTheme.colorScheme.error
                        ),
                        insideMargin = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("结束进程", fontSize = 12.sp)
                    }

                    // 按钮3: 重启终端
                    Button(
                        onClick = { RootService.restartTerminal() },
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary.copy(0.15f),
                            contentColor = MiuixTheme.colorScheme.primary
                        ),
                        insideMargin = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("重启终端", fontSize = 12.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ==================== 1. 终端控制台主屏分区 ====================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF141416))
                    .padding(12.dp)
            ) {
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = parsedOutput.text,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                // 运行状态指示器 (右上角微标)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF222226).copy(0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (RootService.isTaskRunning) Color(0xFF00E676)
                                else Color(0xFF757575)
                            )
                    )
                    Text(
                        text = if (RootService.isTaskRunning) "RUNNING" else "IDLE",
                        color = Color.White.copy(0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // ==================== 2. 控制指令操作条分区 ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 中断任务按钮
                Button(
                    enabled = RootService.isTaskRunning,
                    onClick = { RootService.sendInterrupt() },
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.error.copy(0.18f),
                        contentColor = MiuixTheme.colorScheme.error
                    ),
                    insideMargin = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("中断", fontSize = 12.sp)
                }

                // 清屏按钮
                Button(
                    onClick = { RootService.clearOutput() },
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MiuixTheme.colorScheme.onSurface
                    ),
                    insideMargin = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("清屏", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                // 回车快捷键
                Button(
                    onClick = { handleSend("") },
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MiuixTheme.colorScheme.onSurface
                    ),
                    insideMargin = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text("Enter", fontSize = 12.sp)
                }
            }

            // ==================== 3. 终端命令与交互输入框分区 ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = "请输入命令...",
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = { inputText = "" }) {
                                Icon(
                                    imageVector = MiuixIcons.Clear,
                                    contentDescription = "清空输入"
                                )
                            }
                        }
                    }
                )

                Button(
                    onClick = {
                        handleSend(inputText)
                        keyboardController?.hide()
                    },
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = MiuixTheme.colorScheme.onPrimary
                    ),
                    insideMargin = PaddingValues(horizontal = 18.dp, vertical = 11.dp)
                ) {
                    Text("发送", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // 预留足够底部安全间距，避免与悬浮底栏发生重叠
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

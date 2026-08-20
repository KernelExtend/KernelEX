package Kernel.Extend.ui.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Kernel.Extend.data.AppSettings
import Kernel.Extend.data.FileItem
import Kernel.Extend.data.RootFileManager
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import java.io.File

// 文件管理页面：支持浏览内部存储与ROOT全盘、快捷跳转、长按添加到KernelEX/重命名/删除
@Composable
fun FilePage(
    appSettings: AppSettings,
    onExecuteFileAndNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 默认初始目录：内部存储 (/storage/emulated/0)
    var currentDirectory by remember { mutableStateOf("/storage/emulated/0") }
    var fileList by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // 长按操作弹窗状态
    var selectedItem by remember { mutableStateOf<FileItem?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }

    // 重命名与删除弹窗
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 操作结果反馈提示
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // 扫描加载当前目录
    fun refresh() {
        isLoading = true
        scope.launch {
            try {
                fileList = RootFileManager.listFiles(currentDirectory)
            } catch (_: Exception) {
                fileList = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(currentDirectory) {
        RootFileManager.ensureKernelEXDir()
        refresh()
    }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            feedbackMessage = null
        }
    }

    Scaffold(
        topBar = {
            // 统一左上角大标题：文件管理器
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MiuixTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "文件管理器",
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
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            // ==================== 1. 路径导航与快捷跳转分区 ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 当前路径行 + 返回上一级
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val parent = File(currentDirectory).parent ?: "/"
                            currentDirectory = parent
                        },
                        enabled = currentDirectory != "/"
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回上一级",
                            tint = if (currentDirectory != "/") MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.disabledOnSecondaryVariant
                        )
                    }

                    Text(
                        text = currentDirectory,
                        style = MiuixTheme.textStyles.footnote1,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                }

                // 快捷跳转目录按钮行（根目录 / 内部存储 / KernelEX 目录）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 跳转 1: 根目录 (/)
                    Button(
                        onClick = { currentDirectory = "/" },
                        colors = ButtonDefaults.buttonColors(
                            color = if (currentDirectory == "/") MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHighest,
                            contentColor = if (currentDirectory == "/") MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                        ),
                        insideMargin = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("根目录 (/)", fontSize = 12.sp)
                    }

                    // 跳转 2: 内部存储 (/storage/emulated/0)
                    Button(
                        onClick = { currentDirectory = "/storage/emulated/0" },
                        colors = ButtonDefaults.buttonColors(
                            color = if (currentDirectory == "/storage/emulated/0") MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHighest,
                            contentColor = if (currentDirectory == "/storage/emulated/0") MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                        ),
                        insideMargin = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("内部存储", fontSize = 12.sp)
                    }

                    // 跳转 3: KernelEX 目录 (/data/adb/KernelEX)
                    Button(
                        onClick = { currentDirectory = RootFileManager.DEFAULT_KERNEL_EX_DIR },
                        colors = ButtonDefaults.buttonColors(
                            color = if (currentDirectory == RootFileManager.DEFAULT_KERNEL_EX_DIR) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainerHighest,
                            contentColor = if (currentDirectory == RootFileManager.DEFAULT_KERNEL_EX_DIR) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
                        ),
                        insideMargin = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("KernelEX目录", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==================== 2. 文件列表展示分区 ====================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (fileList.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "当前目录为空",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(fileList, key = { index, item -> "${item.path}_$index" }) { _, item ->
                            val isExecutable = item.isExecutableScript || item.isExecutableBinary

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MiuixTheme.colorScheme.surfaceContainer)
                                    .combinedClickable(
                                        onClick = {
                                            if (item.isDirectory) {
                                                currentDirectory = item.path
                                            } else if (isExecutable) {
                                                onExecuteFileAndNavigate(item.path)
                                            } else {
                                                selectedItem = item
                                                showActionDialog = true
                                            }
                                        },
                                        onLongClick = {
                                            selectedItem = item
                                            showActionDialog = true
                                        }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 文件类型徽标
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                item.isDirectory -> MiuixTheme.colorScheme.primary.copy(0.15f)
                                                item.isExecutableScript -> Color(0xFF4CAF50).copy(0.2f)
                                                item.isExecutableBinary -> Color(0xFF2196F3).copy(0.2f)
                                                else -> MiuixTheme.colorScheme.surfaceContainerHighest
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when {
                                            item.isDirectory -> "📁"
                                            item.isExecutableScript -> "SH"
                                            item.isExecutableBinary -> "SO"
                                            else -> "📄"
                                        },
                                        fontSize = if (item.isDirectory || !isExecutable) 16.sp else 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            item.isExecutableScript -> Color(0xFF2E7D32)
                                            item.isExecutableBinary -> Color(0xFF1565C0)
                                            else -> MiuixTheme.colorScheme.onSurfaceSecondary
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MiuixTheme.textStyles.body1,
                                        fontWeight = if (isExecutable) FontWeight.SemiBold else FontWeight.Normal,
                                        color = MiuixTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (item.isDirectory) "文件夹" else item.formattedSize,
                                            style = MiuixTheme.textStyles.footnote2,
                                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                                        )

                                        if (item.permissions.isNotEmpty()) {
                                            Text(
                                                text = item.permissions,
                                                style = MiuixTheme.textStyles.footnote2,
                                                fontFamily = FontFamily.Monospace,
                                                color = MiuixTheme.colorScheme.onSurfaceSecondary.copy(0.7f)
                                            )
                                        }
                                    }
                                }

                                if (isExecutable) {
                                    Button(
                                        onClick = { onExecuteFileAndNavigate(item.path) },
                                        colors = ButtonDefaults.buttonColors(
                                            color = MiuixTheme.colorScheme.primary,
                                            contentColor = MiuixTheme.colorScheme.onPrimary
                                        ),
                                        insideMargin = PaddingValues(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text("执行", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // ==================== 3. 长按功能弹窗分区 ====================
    if (showActionDialog && selectedItem != null) {
        val item = selectedItem!!
        WindowDialog(
            show = true,
            title = item.name,
            onDismissRequest = { showActionDialog = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 功能1: 添加到KernelEX
                Button(
                    onClick = {
                        showActionDialog = false
                        scope.launch {
                            val (success, resultPath) = RootFileManager.addFileToKernelEX(
                                sourcePath = item.path,
                                useIndependentFolder = appSettings.useIndependentFolder,
                                autoDeleteSource = appSettings.autoDeleteAfterAdding
                            )
                            if (success) {
                                feedbackMessage = "已添加到 KernelEX: $resultPath"
                                refresh()
                                if (appSettings.autoExecuteAfterAdding && (item.isExecutableScript || item.isExecutableBinary)) {
                                    onExecuteFileAndNavigate(resultPath)
                                }
                            } else {
                                feedbackMessage = resultPath
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = MiuixTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("添加到KernelEX")
                }

                // 功能2: 重命名
                Button(
                    onClick = {
                        showActionDialog = false
                        renameInput = item.name
                        showRenameDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MiuixTheme.colorScheme.onSurface
                    )
                ) {
                    Text("重命名")
                }

                // 功能3: 删除
                Button(
                    onClick = {
                        showActionDialog = false
                        showDeleteDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.error.copy(0.15f),
                        contentColor = MiuixTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            }
        }
    }

    // ==================== 4. 重命名弹窗分区 ====================
    if (showRenameDialog && selectedItem != null) {
        val item = selectedItem!!
        WindowDialog(
            show = true,
            title = "重命名",
            onDismissRequest = { showRenameDialog = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = "输入新名称",
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { showRenameDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MiuixTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        enabled = renameInput.isNotBlank() && renameInput != item.name,
                        onClick = {
                            val targetName = renameInput.trim()
                            showRenameDialog = false
                            scope.launch {
                                val (success, message) = RootFileManager.rename(item.path, targetName)
                                feedbackMessage = if (success) "重命名成功" else "重命名失败: $message"
                                refresh()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = MiuixTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }

    // ==================== 5. 删除确认弹窗分区 ====================
    if (showDeleteDialog && selectedItem != null) {
        val item = selectedItem!!
        WindowDialog(
            show = true,
            title = "确认删除",
            summary = "您确定要删除 \"${item.name}\" 吗？此操作无法撤销。",
            onDismissRequest = { showDeleteDialog = false }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MiuixTheme.colorScheme.onSurface
                    )
                ) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            val (success, message) = RootFileManager.delete(item.path)
                            feedbackMessage = if (success) "删除成功" else "删除失败: $message"
                            refresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.error,
                        contentColor = MiuixTheme.colorScheme.onError
                    )
                ) {
                    Text("确认删除")
                }
            }
        }
    }
}

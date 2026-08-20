package Kernel.Extend.ui.pages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Kernel.Extend.R
import Kernel.Extend.data.AppSettings
import Kernel.Extend.ui.components.ColorWheelDialog
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlaySpinnerPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

// 设置页面：包含 文件、主题、终端、关于 四大功能分区
@Composable
fun SettingsPage(
    appSettings: AppSettings
) {
    val context = LocalContext.current
    var showColorDialog by remember { mutableStateOf(false) }

    // 深色模式选项：开启 / 关闭 / 跟随系统（顺序按用户要求，无额外介绍）
    val darkModeOptions = remember {
        listOf(
            DropdownItem(text = "开启"),
            DropdownItem(text = "关闭"),
            DropdownItem(text = "跟随系统")
        )
    }

    val selectedDarkModeIndex = when (appSettings.darkModeOption) {
        2 -> 0 // 开启
        1 -> 1 // 关闭
        else -> 2 // 跟随系统
    }

    // 仅通过独立浏览器打开网页链接（避免被 GitHub 官方 App 拦截）
    fun openInBrowserOnly(url: String) {
        try {
            val uri = Uri.parse(url)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://")).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            val resolveInfos = context.packageManager.queryIntentActivities(browserIntent, 0)
            val browserPackages = resolveInfos.map { it.activityInfo.packageName }
                .filter { pkg -> pkg != "com.github.android" && !pkg.contains("github") }

            val targetIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (browserPackages.isNotEmpty()) {
                val preferred = browserPackages.firstOrNull {
                    it.contains("browser") || it.contains("chrome") || it.contains("edge") || it.contains("firefox")
                } ?: browserPackages.first()
                targetIntent.setPackage(preferred)
                context.startActivity(targetIntent)
            } else {
                val chooser = Intent.createChooser(targetIntent, "选择浏览器打开").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        } catch (_: Exception) {
            try {
                val generalIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(generalIntent)
            } catch (_: Exception) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = ClipData.newPlainText("URL", url)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(context, "未能调起浏览器，链接已复制到剪贴板", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = "设置",
                color = MiuixTheme.colorScheme.surface
            )
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
            // ==================== 1. 文件功能分区 ====================
            SmallTitle(
                text = "文件",
                insideMargin = PaddingValues(start = 0.dp, top = 8.dp, bottom = 4.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 功能1: 使用独立文件夹存储
                SwitchPreference(
                    title = "使用独立文件夹存储",
                    summary = "在添加到KernelEX时新建独立文件夹进行存储",
                    checked = appSettings.useIndependentFolder,
                    onCheckedChange = { appSettings.setIndependentFolder(it) }
                )

                // 功能2: 添加后自动删除文件
                SwitchPreference(
                    title = "添加后自动删除文件",
                    summary = "将文件复制到KernelEX后自动清理源文件",
                    checked = appSettings.autoDeleteAfterAdding,
                    onCheckedChange = { appSettings.setAutoDelete(it) }
                )

                // 功能3: 添加后自动执行文件
                SwitchPreference(
                    title = "添加后自动执行文件",
                    summary = "添加到KernelEX后自动跳转终端并开始执行",
                    checked = appSettings.autoExecuteAfterAdding,
                    onCheckedChange = { appSettings.setAutoExecute(it) }
                )
            }

            // ==================== 2. 主题功能分区 ====================
            SmallTitle(
                text = "主题",
                insideMargin = PaddingValues(start = 0.dp, top = 8.dp, bottom = 4.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 功能1: 终端文字颜色 (支持精细调色盘与16款预设)
                ArrowPreference(
                    title = "终端文字颜色",
                    summary = "自定义终端控制台文本的显示高亮颜色",
                    onClick = { showColorDialog = true },
                    endActions = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(appSettings.terminalTextColor))
                        )
                    }
                )

                // 功能2: 深色模式 (OverlaySpinnerPreference 下拉菜单：开启 / 关闭 / 跟随系统)
                OverlaySpinnerPreference(
                    title = "深色模式",
                    items = darkModeOptions,
                    selectedIndex = selectedDarkModeIndex,
                    onSelectedIndexChange = { index ->
                        val mode = when (index) {
                            0 -> 2 // 开启
                            1 -> 1 // 关闭
                            else -> 0 // 跟随系统
                        }
                        appSettings.setDarkMode(mode)
                    }
                )

                // 功能3: 悬浮底栏
                SwitchPreference(
                    title = "悬浮底栏",
                    summary = "切换底部导航栏为悬浮胶囊样式",
                    checked = appSettings.enableFloatingDock,
                    onCheckedChange = { appSettings.setFloatingDock(it) }
                )
            }

            // ==================== 3. 终端功能分区 ====================
            SmallTitle(
                text = "终端",
                insideMargin = PaddingValues(start = 0.dp, top = 8.dp, bottom = 4.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 功能1: HyperCore 终端提示
                SwitchPreference(
                    title = "HyperCore 终端提示",
                    summary = "控制是否在终端显示 HyperCore 引擎初始化及环境检测标头",
                    checked = appSettings.showHyperCoreBanner,
                    onCheckedChange = { appSettings.setHyperCoreBanner(it) }
                )

                // 功能2: KernelEX 终端提示
                SwitchPreference(
                    title = "KernelEX 终端提示",
                    summary = "控制是否在终端显示任务启动、路径及退出状态信息",
                    checked = appSettings.showKernelEXBanner,
                    onCheckedChange = { appSettings.setKernelEXBanner(it) }
                )
            }

            // ==================== 4. 关于功能分区 ====================
            SmallTitle(
                text = "关于",
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
                    // 头部：圆形图标与应用名称版本
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_kernelex),
                            contentDescription = "KernelEX 图标",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "KernelEX",
                                style = MiuixTheme.textStyles.title2,
                                fontWeight = FontWeight.Bold,
                                color = MiuixTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "v1.0.0 (Kernel.Extend)",
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceSecondary,
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    // 说明文本（靠左对齐）
                    Text(
                        text = "KernelEX 是一款专为 Android 打造的高性能 ROOT 执行工具。支持在安全、高效的环境中运行 .sh 脚本与 .so 二进制文件，提供交互式终端、ANSI 着色以及强大的全盘 ROOT 文件管理能力。",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // 链接1: 在浏览器中打开 GitHub 仓库
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                openInBrowserOnly("https://github.com/KernelExtend/KernelEX")
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_github),
                                contentDescription = "GitHub",
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "在 GitHub 上查看源代码",
                                style = MiuixTheme.textStyles.body2,
                                fontWeight = FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = MiuixIcons.Basic.ArrowRight,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceSecondary.copy(0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 链接2: 在浏览器中打开 Telegram 官方频道
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                openInBrowserOnly("https://t.me/KernelEX")
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_telegram),
                                contentDescription = "Telegram",
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "加入我们的 Telegram 频道",
                                style = MiuixTheme.textStyles.body2,
                                fontWeight = FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = MiuixIcons.Basic.ArrowRight,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceSecondary.copy(0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // ==================== 5. 终端颜色选择弹窗分区 ====================
    if (showColorDialog) {
        ColorWheelDialog(
            show = true,
            initialColor = Color(appSettings.terminalTextColor),
            onDismissRequest = { showColorDialog = false },
            onColorSelected = { color ->
                appSettings.setTerminalColor(color)
            }
        )
    }
}

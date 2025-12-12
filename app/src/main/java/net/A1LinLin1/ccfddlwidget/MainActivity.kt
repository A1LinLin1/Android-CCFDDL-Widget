package net.A1LinLin1.ccfddlwidget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.A1LinLin1.ccfddlwidget.data.Conference
import net.A1LinLin1.ccfddlwidget.data.ConferenceRepository
import net.A1LinLin1.ccfddlwidget.ui.theme.CCFDDLWidgetTheme
import net.A1LinLin1.ccfddlwidget.settings.SettingsScreen
import net.A1LinLin1.ccfddlwidget.settings.SettingsStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CCFDDLWidgetTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CcfddlHomeScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun CcfddlHomeScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current

    var showSettings by remember { mutableStateOf(false) }

    var selectedSubs by remember { mutableStateOf(SettingsStore.loadSubs(context)) }
    var selectedRanks by remember { mutableStateOf(SettingsStore.loadRanks(context)) }

    var conferences by remember { mutableStateOf<List<Conference>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val displayFormat = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    }

    // 当 subs 改变时，重新拉取数据
    LaunchedEffect(selectedSubs) {
        if (selectedSubs.isEmpty()) {
            conferences = emptyList()
            isLoading = false
            errorMsg = null
            return@LaunchedEffect
        }

        isLoading = true
        errorMsg = null
        val result = try {
            ConferenceRepository.fetchConferencesCached(
                context = context,
                subs = selectedSubs,
                ranks = selectedRanks
            )
        } catch (e: Exception) {
            e.printStackTrace()
            errorMsg = e.message ?: "加载失败"
            emptyList()
        }
        conferences = result
        isLoading = false
    }

    // 顶部栏 + 内容
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 简易 Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showSettings) "筛选设置" else "CCF DDL 追踪器",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = { showSettings = !showSettings }) {
                Text(if (showSettings) "返回" else "筛选")
            }
        }

        if (showSettings) {
            SettingsScreen(
                selectedSubs = selectedSubs,
                selectedRanks = selectedRanks,
                onToggleSub = { sub ->
                    val next = selectedSubs.toMutableSet().apply {
                        if (contains(sub)) remove(sub) else add(sub)
                    }.toSet()
                    selectedSubs = next
                    SettingsStore.saveSubs(context, next)
                    WidgetRefresh.request(context)
                },
                onToggleRank = { r ->
                    val next = selectedRanks.toMutableSet().apply {
                        if (contains(r)) remove(r) else add(r)
                    }.toSet()
                    selectedRanks = next
                    SettingsStore.saveRanks(context, next)
                    WidgetRefresh.request(context)
                }
            )
            return
        }

        // 主列表（可滚动）
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "方向：${selectedSubs.sorted().joinToString(" · ")}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "等级：${if (selectedRanks.isEmpty()) "全部" else selectedRanks.sorted().joinToString(" · ")}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(12.dp))

            // ranks 过滤：如果用户没选等级，则视为不过滤（全部）
            val filtered = if (selectedRanks.isEmpty()) {
                conferences
            } else {
                conferences.filter { (it.ccfRank ?: "").uppercase() in selectedRanks }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("正在加载…") }
                }

                errorMsg != null -> {
                    Text("加载失败：$errorMsg", color = MaterialTheme.colorScheme.error)
                }

                selectedSubs.isEmpty() -> {
                    Text("请至少选择一个方向（Sub）。")
                }

                filtered.isEmpty() -> {
                    Text("没有符合筛选条件的会议（可能是等级过滤过严或数据暂无）。")
                }

                else -> {
                    filtered.forEach { conf ->
                        val ctx = context
                        val sdf = displayFormat

                        ConferenceCard(
                            title = buildString { append(conf.title); conf.year?.let { append(" $it") } },
                            rank = conf.ccfRank ?: "?",
                            sub = conf.sub ?: "N/A",
                            deadlineText = sdf.format(java.util.Date(conf.deadlineMillis)),
                            ddayText = "D-${conf.dday}",
                            desc = conf.desc ?: "",
                            onClick = {
                                val intent = Intent(ctx, DetailActivity::class.java).apply {
                                    putExtra(DetailActivity.EXTRA_TITLE, buildString {
                                        append(conf.title); conf.year?.let { append(" $it") }
                                    })
                                    putExtra(DetailActivity.EXTRA_RANK, conf.ccfRank ?: "?")
                                    putExtra(DetailActivity.EXTRA_SUB, conf.sub ?: "N/A")
                                    putExtra(DetailActivity.EXTRA_DEADLINE_TEXT, sdf.format(java.util.Date(conf.deadlineMillis)))
                                    putExtra(DetailActivity.EXTRA_DDAY_TEXT, "D-${conf.dday}")
                                    putExtra(DetailActivity.EXTRA_DESC, conf.desc ?: "")
                                    putExtra(DetailActivity.EXTRA_LINK, conf.link ?: "")
                                }
                                ctx.startActivity(intent)
                            }
                        )

                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ConferenceCard(
    title: String,
    rank: String,
    sub: String,
    deadlineText: String,
    ddayText: String,
    desc: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 标题 + 标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "[${rank} · ${sub}]",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Deadline: $deadlineText",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )

            Text(
                text = ddayText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 2.dp)
            )

            if (desc.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = desc,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

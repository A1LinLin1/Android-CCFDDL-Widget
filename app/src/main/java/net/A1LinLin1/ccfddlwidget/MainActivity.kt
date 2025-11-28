package net.A1LinLin1.ccfddlwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.A1LinLin1.ccfddlwidget.ui.theme.CCFDDLWidgetTheme

// 模拟的会议数据，后面会替换成真实 ccfddl 数据
data class ConferenceUi(
    val title: String,
    val rank: String,
    val sub: String,
    val deadline: String,
    val dday: String,
    val desc: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CCFDDLWidgetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    // 这里先用假数据，后面接 Repository + 真数据
    val conferences = listOf(
        ConferenceUi(
            title = "SIGCOMM 2026",
            rank = "A",
            sub = "SC",
            deadline = "2025-09-15 23:59",
            dday = "D-23",
            desc = "ACM International Conference on Data Communication. 这里可以展示更完整的会议信息、重要日期、投稿链接等。"
        ),
        ConferenceUi(
            title = "NDSS 2026",
            rank = "A",
            sub = "SC",
            deadline = "2025-08-01 23:59",
            dday = "D-45",
            desc = "Network and Distributed System Security Symposium. 网络与分布式系统安全领域的顶级会议。"
        ),
        ConferenceUi(
            title = "CCS 2026",
            rank = "A",
            sub = "SC",
            deadline = "2025-07-10 23:59",
            dday = "D-68",
            desc = "ACM Conference on Computer and Communications Security. 计算机与通信安全领域的旗舰会议。"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "CCF DDL 追踪器",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "根据方向 & 等级展示最近的 CCF 会议（当前为示例数据，后续接入 ccfddl 实时数据）。",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 循环展示每个会议
        conferences.forEach { conf ->
            ConferenceCard(conf = conf)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ConferenceCard(conf: ConferenceUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
            // 标题 + 标签行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conf.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "[${conf.rank} · ${conf.sub}]",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Deadline: ${conf.deadline}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Text(
                text = conf.dday,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = conf.desc,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

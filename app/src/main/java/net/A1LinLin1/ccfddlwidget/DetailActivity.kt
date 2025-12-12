package net.A1LinLin1.ccfddlwidget

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.A1LinLin1.ccfddlwidget.ui.theme.CCFDDLWidgetTheme

class DetailActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val rank = intent.getStringExtra(EXTRA_RANK).orEmpty()
        val sub = intent.getStringExtra(EXTRA_SUB).orEmpty()
        val deadlineText = intent.getStringExtra(EXTRA_DEADLINE_TEXT).orEmpty()
        val ddayText = intent.getStringExtra(EXTRA_DDAY_TEXT).orEmpty()
        val desc = intent.getStringExtra(EXTRA_DESC).orEmpty()
        val link = intent.getStringExtra(EXTRA_LINK)

        setContent {
            CCFDDLWidgetTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("会议详情") }
                        )
                    }
                ) { innerPadding ->
                    DetailScreen(
                        modifier = Modifier.padding(innerPadding),
                        title = title,
                        rank = rank,
                        sub = sub,
                        deadlineText = deadlineText,
                        ddayText = ddayText,
                        desc = desc,
                        link = link,
                        onOpenLink = { url ->
                            runCatching {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_RANK = "extra_rank"
        const val EXTRA_SUB = "extra_sub"
        const val EXTRA_DEADLINE_TEXT = "extra_deadline_text"
        const val EXTRA_DDAY_TEXT = "extra_dday_text"
        const val EXTRA_DESC = "extra_desc"
        const val EXTRA_LINK = "extra_link"
    }
}

@Composable
private fun DetailScreen(
    modifier: Modifier,
    title: String,
    rank: String,
    sub: String,
    deadlineText: String,
    ddayText: String,
    desc: String,
    link: String?,
    onOpenLink: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Text("CCF：$rank · $sub", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text("Deadline：$deadlineText", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(ddayText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        if (desc.isNotBlank()) {
            Text("简介", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(desc, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(14.dp))

        if (!link.isNullOrBlank()) {
            Text("链接", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Button(onClick = { onOpenLink(link) }) {
                Text("打开会议主页 / CFP")
            }
        }
    }
}

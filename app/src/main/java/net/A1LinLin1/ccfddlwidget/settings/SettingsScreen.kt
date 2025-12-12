package net.A1LinLin1.ccfddlwidget.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    selectedSubs: Set<String>,
    selectedRanks: Set<String>,
    onToggleSub: (String) -> Unit,
    onToggleRank: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("筛选", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Text("方向（Sub）", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsStore.ALL_SUBS.forEach { sub ->
                FilterChip(
                    selected = selectedSubs.contains(sub),
                    onClick = { onToggleSub(sub) },
                    label = { Text(sub) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("CCF 等级", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsStore.ALL_RANKS.forEach { r ->
                FilterChip(
                    selected = selectedRanks.contains(r),
                    onClick = { onToggleRank(r) },
                    label = { Text(r) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "提示：方向不选时将不加载任何会议；等级不选时将显示“全部等级”。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

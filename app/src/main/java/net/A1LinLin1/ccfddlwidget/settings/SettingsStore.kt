package net.A1LinLin1.ccfddlwidget.settings

import android.content.Context

object SettingsStore {
    private const val PREFS = "ccfddl_prefs"
    private const val KEY_SUBS = "subs_csv"
    private const val KEY_RANKS = "ranks_csv"

    // 你仓库 conference/ 下的目录名（按需增删）
    val ALL_SUBS = listOf("SC", "SE", "DB", "AI", "NW", "SEC", "CG", "HC", "AR", "CT", "DS", "IS")
    val ALL_RANKS = listOf("A", "B", "C")

    fun loadSubs(context: Context): Set<String> {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = sp.getString(KEY_SUBS, null)
        return if (saved.isNullOrBlank()) {
            setOf("SC", "SE", "DB", "AI") // 默认勾选
        } else {
            saved.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        }
    }

    fun loadRanks(context: Context): Set<String> {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = sp.getString(KEY_RANKS, null)
        return if (saved.isNullOrBlank()) {
            setOf("A", "B") // 默认 A/B
        } else {
            saved.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        }
    }

    fun saveSubs(context: Context, subs: Set<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SUBS, subs.joinToString(","))
            .apply()
    }

    fun saveRanks(context: Context, ranks: Set<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RANKS, ranks.joinToString(","))
            .apply()
    }
}

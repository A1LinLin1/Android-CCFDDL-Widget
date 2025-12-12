package net.A1LinLin1.ccfddlwidget.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import net.A1LinLin1.ccfddlwidget.DetailActivity
import net.A1LinLin1.ccfddlwidget.R
import net.A1LinLin1.ccfddlwidget.data.Conference
import net.A1LinLin1.ccfddlwidget.data.ConferenceRepository
import net.A1LinLin1.ccfddlwidget.settings.SettingsStore
import java.text.SimpleDateFormat
import java.util.*

class CcfddlRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private val data = mutableListOf<Conference>()
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val subs = SettingsStore.loadSubs(context)
        val ranks = SettingsStore.loadRanks(context)
        data.clear()
        if (subs.isEmpty()) return

        val list = kotlinx.coroutines.runBlocking {
            ConferenceRepository.fetchConferencesCached(
                context = context,
                subs = subs,
                ranks = ranks,
                ttlHours = 6
            )
        }
        data.addAll(list)
    }


    override fun getCount(): Int = data.size

    override fun getViewAt(position: Int): RemoteViews {
        val c = data[position]
        val rv = RemoteViews(context.packageName, R.layout.widget_ccfddl_item)

        rv.setTextViewText(
            R.id.tv_title,
            buildString {
                append(c.title)
                c.year?.let { append(" $it") }
                append(" [${c.ccfRank ?: "?"} · ${c.sub ?: "N/A"}]")
            }
        )
        rv.setTextViewText(
            R.id.tv_deadline,
            "Deadline: ${sdf.format(Date(c.deadlineMillis))}"
        )
        rv.setTextViewText(R.id.tv_dday, "D-${c.dday}")
        rv.setTextViewText(R.id.tv_desc, c.desc ?: "")

        // 点击某一条 → 打开详情页（带返回栈）
        val intent = Intent().apply {
            putExtra(DetailActivity.EXTRA_TITLE, buildString {
                append(c.title)
                c.year?.let { append(" $it") }
            })
            putExtra(DetailActivity.EXTRA_RANK, c.ccfRank ?: "?")
            putExtra(DetailActivity.EXTRA_SUB, c.sub ?: "N/A")
            putExtra(DetailActivity.EXTRA_DEADLINE_TEXT, sdf.format(Date(c.deadlineMillis)))
            putExtra(DetailActivity.EXTRA_DDAY_TEXT, "D-${c.dday}")
            putExtra(DetailActivity.EXTRA_DESC, c.desc ?: "")
            putExtra(DetailActivity.EXTRA_LINK, c.link ?: "")
        }
        rv.setOnClickFillInIntent(R.id.item_root, intent)


        return rv
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
    override fun onDestroy() {}
}

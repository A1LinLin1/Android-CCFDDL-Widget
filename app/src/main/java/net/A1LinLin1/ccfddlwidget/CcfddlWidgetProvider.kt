package net.A1LinLin1.ccfddlwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.core.app.TaskStackBuilder
import net.A1LinLin1.ccfddlwidget.settings.SettingsStore
import net.A1LinLin1.ccfddlwidget.widget.CcfddlRemoteViewsService

class CcfddlWidgetProvider : AppWidgetProvider() {
    companion object {
        private const val ACTION_OPEN_DETAIL = "net.A1LinLin1.ccfddlwidget.ACTION_OPEN_DETAIL"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        val subs = SettingsStore.loadSubs(context)
        val ranks = SettingsStore.loadRanks(context)

        val filterText = buildString {
            append("CCF: ")
            append(if (ranks.isEmpty()) "ALL" else ranks.sorted().joinToString("/"))
            append(" · ")
            append(if (subs.isEmpty()) "未选择方向" else subs.sorted().joinToString(" · "))
        }

        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_ccfddl_list)

            // 顶部筛选信息
            views.setTextViewText(R.id.tv_filter_info, filterText)

            // 绑定 ListView 的数据源（RemoteViewsService）
            val svcIntent = Intent(context, CcfddlRemoteViewsService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                // 关键：给 intent 一个唯一 Uri，防止多个 widget 复用导致不刷新
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.list_conferences, svcIntent)

            val openIntent = Intent(context, CcfddlWidgetProvider::class.java).apply {
                action = ACTION_OPEN_DETAIL
            }
            val openPI = PendingIntent.getBroadcast(
                context,
                widgetId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.list_conferences, openPI)

            // 更新 widget
            appWidgetManager.updateAppWidget(widgetId, views)

            // 通知 ListView 刷新（会触发 RemoteViewsFactory.onDataSetChanged）
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.list_conferences)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_OPEN_DETAIL) {
            // intent.extras 就是 Factory 里 fillInIntent 塞进来的 extras
            val detailIntent = Intent(context, DetailActivity::class.java).apply {
                putExtras(intent.extras ?: return)
            }

            TaskStackBuilder.create(context).run {
                addNextIntent(Intent(context, MainActivity::class.java))
                addNextIntent(detailIntent)
                startActivities()
            }
        }
    }
}

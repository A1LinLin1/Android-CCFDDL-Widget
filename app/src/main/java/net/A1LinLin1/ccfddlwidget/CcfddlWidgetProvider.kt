package net.A1LinLin1.ccfddlwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class CcfddlWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_ccfddl).apply {
                // 这里先用假数据，后面再替换成真实的 ccfddl 数据

                setTextViewText(R.id.tv_filter_info, "CCF: A/B · SC")

                setTextViewText(R.id.tv_conf1_title, "SIGCOMM 2026 [A · SC]")
                setTextViewText(R.id.tv_conf1_deadline, "Deadline: 2025-09-15 23:59")
                setTextViewText(R.id.tv_conf1_dday, "D-23")
                setTextViewText(R.id.tv_conf1_desc, "ACM International Conference on...")

                setTextViewText(R.id.tv_conf2_title, "NDSS 2026 [A · SC]")
                setTextViewText(R.id.tv_conf2_deadline, "Deadline: 2025-08-01 23:59")
                setTextViewText(R.id.tv_conf2_dday, "D-45")
                setTextViewText(R.id.tv_conf2_desc, "Network and Distributed System Security...")

                setTextViewText(R.id.tv_conf3_title, "CCS 2026 [A · SC]")
                setTextViewText(R.id.tv_conf3_deadline, "Deadline: 2025-07-10 23:59")
                setTextViewText(R.id.tv_conf3_dday, "D-68")
                setTextViewText(R.id.tv_conf3_desc, "ACM Conference on Computer and Communications Security...")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
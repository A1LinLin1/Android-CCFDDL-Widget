package net.A1LinLin1.ccfddlwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object WidgetRefresh {
    fun request(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        val cn = ComponentName(context, CcfddlWidgetProvider::class.java)
        val ids = mgr.getAppWidgetIds(cn)
        if (ids.isEmpty()) return


        val intent = Intent(context, CcfddlWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}

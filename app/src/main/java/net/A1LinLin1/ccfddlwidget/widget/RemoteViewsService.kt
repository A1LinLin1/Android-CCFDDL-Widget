package net.A1LinLin1.ccfddlwidget.widget

import android.content.Intent
import android.widget.RemoteViewsService

class CcfddlRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return CcfddlRemoteViewsFactory(applicationContext)
    }
}

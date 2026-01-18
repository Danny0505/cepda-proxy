package com.cepda.firetv.launcherwrapper

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.cepda.firetv.launcherwrapper.model.AppItem

class AppRepository(private val context: Context) {

    fun getLeanbackUserApps(): List<AppItem> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        return resolveInfos.mapNotNull { info ->
            val activityInfo = info.activityInfo
            val appInfo = activityInfo.applicationInfo

            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                return@mapNotNull null
            }

            if (appInfo.packageName == context.packageName) {
                return@mapNotNull null
            }

            val label = activityInfo.loadLabel(pm).toString()
            val icon = activityInfo.loadIcon(pm)
            val componentName = ComponentName(activityInfo.packageName, activityInfo.name)

            AppItem(label, icon, componentName)
        }
    }
}
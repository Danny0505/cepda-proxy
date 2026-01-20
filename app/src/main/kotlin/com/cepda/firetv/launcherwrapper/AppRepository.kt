package com.cepda.firetv.launcherwrapper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.cepda.firetv.launcherwrapper.model.AppItem

class AppRepository(private val context: Context) {

    companion object {
        private const val TAG = "AppRepository"
    }

    fun getLeanbackUserApps(): List<AppItem> {
        val pm = context.packageManager

        // Primero intentar con LEANBACK_LAUNCHER (Fire TV)
        var apps = getAppsWithCategory(pm, Intent.CATEGORY_LEANBACK_LAUNCHER)
        Log.d(TAG, "Apps encontradas con LEANBACK_LAUNCHER: ${apps.size}")

        // Si no hay apps con Leanback, usar LAUNCHER normal (para emuladores)
        if (apps.isEmpty()) {
            Log.d(TAG, "No hay apps Leanback, buscando con LAUNCHER normal...")
            apps = getAppsWithCategory(pm, Intent.CATEGORY_LAUNCHER)
            Log.d(TAG, "Apps encontradas con LAUNCHER: ${apps.size}")
        }

        return apps
    }

    private fun getAppsWithCategory(pm: PackageManager, category: String): List<AppItem> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(category)
        }

        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        Log.d(TAG, "Total de actividades para categoria '$category': ${resolveInfos.size}")

        return resolveInfos.mapNotNull { info ->
            val activityInfo = info.activityInfo
            val appInfo = activityInfo.applicationInfo

            // Filtrar apps del sistema
            val isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
            val isUpdatedSystem = appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
            
            if (isSystemApp && !isUpdatedSystem) {
                Log.v(TAG, "Ignorando app del sistema: ${appInfo.packageName}")
                return@mapNotNull null
            }

            // Filtrar nuestra propia app
            if (appInfo.packageName == context.packageName) {
                Log.v(TAG, "Ignorando nuestra app: ${appInfo.packageName}")
                return@mapNotNull null
            }

            val label = activityInfo.loadLabel(pm).toString()
            val icon = activityInfo.loadIcon(pm)
            val componentName = ComponentName(activityInfo.packageName, activityInfo.name)

            Log.d(TAG, "App encontrada: $label (${appInfo.packageName})")

            AppItem(label, icon, componentName)
        }
    }
}
package com.cepda.firetv.launcherwrapper

import android.app.Activity
import android.app.ActivityOptions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast

/**
 * AppLauncher - Lanza aplicaciones de manera que Fire OS las reconozca como
 * lanzamientos legítimos desde un launcher de confianza.
 * 
 * Fire OS muestra advertencias de seguridad para apps sideloaded cuando se
 * lanzan desde contextos no autorizados. Al lanzar desde un launcher registrado
 * con CATEGORY_HOME y CATEGORY_LEANBACK_LAUNCHER, el sistema debería reconocer
 * el flujo como legítimo.
 */
object AppLauncher {

    private const val TAG = "AppLauncher"

    /**
     * Lanza una aplicación usando el método estándar de launchers de Android TV.
     * 
     * Estrategias implementadas:
     * 1. Usar Intent con flags específicos de launcher
     * 2. Incluir CATEGORY_LEANBACK_LAUNCHER para Fire TV
     * 3. Usar ActivityOptions para indicar que viene de un launcher
     * 4. Lanzar desde el contexto de Activity (no Application)
     */
    fun launchApp(context: Context, componentName: ComponentName) {
        Log.d(TAG, "=== Iniciando lanzamiento de app ===")
        Log.d(TAG, "Package: ${componentName.packageName}")
        Log.d(TAG, "Activity: ${componentName.className}")
        
        try {
            // Crear Intent como lo haría un launcher nativo
            val launchIntent = createLauncherIntent(context, componentName)
            
            // Obtener ActivityOptions para lanzamiento desde launcher
            val options = createLauncherActivityOptions(context)
            
            // Lanzar la actividad
            if (context is Activity) {
                Log.d(TAG, "Lanzando desde Activity context con options")
                context.startActivity(launchIntent, options)
            } else {
                Log.d(TAG, "Lanzando desde Application context")
                context.startActivity(launchIntent)
            }
            
            Log.d(TAG, "App lanzada exitosamente: ${componentName.flattenToString()}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al lanzar app: ${componentName.flattenToString()}", e)
            handleLaunchError(context, componentName, e)
        }
    }

    /**
     * Crea un Intent configurado como lo haría un launcher nativo de Fire TV.
     */
    private fun createLauncherIntent(context: Context, componentName: ComponentName): Intent {
        return Intent().apply {
            // Acción principal
            action = Intent.ACTION_MAIN
            
            // Componente específico a lanzar
            component = componentName
            
            // Categorías - incluir tanto LAUNCHER como LEANBACK_LAUNCHER
            addCategory(Intent.CATEGORY_LAUNCHER)
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            
            // Flags críticos para lanzamiento desde launcher:
            // - FLAG_ACTIVITY_NEW_TASK: Requerido cuando se lanza desde fuera de una Activity
            // - FLAG_ACTIVITY_RESET_TASK_IF_NEEDED: Comportamiento estándar de launcher
            // - FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY: NO usar, indica que viene del recents
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            
            // Agregar el source bounds (algunos launchers lo usan)
            sourceBounds = null
            
            Log.d(TAG, "Intent creado con flags: $flags")
        }
    }

    /**
     * Crea ActivityOptions que indican que el lanzamiento viene de un launcher.
     */
    private fun createLauncherActivityOptions(context: Context): Bundle? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityOptions.makeBasic().apply {
                    // En API 24+ podemos especificar el launch display
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        launchDisplayId = 0 // Display principal
                    }
                }.toBundle()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron crear ActivityOptions", e)
            null
        }
    }

    /**
     * Método alternativo: Lanzar usando el PackageManager directamente.
     * Este es el método que usa el launcher nativo de Android.
     */
    fun launchAppViaPackageManager(context: Context, packageName: String) {
        Log.d(TAG, "Intentando lanzar via PackageManager: $packageName")
        
        try {
            val pm = context.packageManager
            
            // Obtener el Intent de lanzamiento del PackageManager
            // Este es exactamente el mismo método que usa el launcher nativo
            val launchIntent = pm.getLeanbackLaunchIntentForPackage(packageName)
                ?: pm.getLaunchIntentForPackage(packageName)
            
            if (launchIntent != null) {
                // Agregar flags de launcher
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                
                // Agregar categoría leanback si no está
                val categories = launchIntent.categories
                if (categories == null || !categories.contains(Intent.CATEGORY_LEANBACK_LAUNCHER)) {
                    launchIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                }
                
                context.startActivity(launchIntent)
                Log.d(TAG, "App lanzada via PackageManager: $packageName")
            } else {
                Log.e(TAG, "No se encontró Intent de lanzamiento para: $packageName")
                Toast.makeText(context, "No se puede abrir la aplicación", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al lanzar via PackageManager: $packageName", e)
            Toast.makeText(context, "Error al abrir la aplicación", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Maneja errores de lanzamiento con mensajes apropiados.
     */
    private fun handleLaunchError(context: Context, componentName: ComponentName, error: Exception) {
        val message = when {
            error is SecurityException -> "Permiso denegado para abrir la aplicación"
            error is android.content.ActivityNotFoundException -> "Aplicación no encontrada"
            else -> "No se pudo abrir la aplicación"
        }
        
        Log.e(TAG, "Error de lanzamiento: $message", error)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

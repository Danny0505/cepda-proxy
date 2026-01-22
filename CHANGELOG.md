# Cepeda Proxy - Registro de Cambios

## Resumen
Correcciones realizadas para que el launcher de Fire TV compile y funcione correctamente.

---

## Archivos Modificados

### 1. `AppRepository.kt`
**Ruta:** `app/src/main/kotlin/com/cepda/firetv/launcherwrapper/AppRepository.kt`

| Cambio | Descripción |
|--------|-------------|
| Import `ComponentName` | Faltaba el import necesario para crear ComponentName |
| Logs detallados | TAG: `AppRepository` para diagnóstico |
| Fallback a `CATEGORY_LAUNCHER` | Si no hay apps Leanback, busca apps normales (para emuladores) |
| Mejor filtro de apps del sistema | Permite apps del sistema actualizadas por el usuario |

```diff
+ import android.content.ComponentName
+ import android.util.Log

+ // Fallback: si no hay apps Leanback, buscar apps normales
+ if (apps.isEmpty()) {
+     apps = getAppsWithCategory(pm, Intent.CATEGORY_LAUNCHER)
+ }
```

---

### 2. `AppLauncher.kt`
**Ruta:** `app/src/main/kotlin/com/cepda/firetv/launcherwrapper/AppLauncher.kt`

| Cambio | Descripción |
|--------|-------------|
| Import `PackageManager` | Faltaba para `PackageManager.NameNotFoundException` |
| Eliminado import duplicado | Había un import repetido |

```diff
+ import android.content.pm.PackageManager
```

---

### 3. `MainLauncherFragment.kt` (antes `MainLauncherActivity.kt`)
**Ruta:** `app/src/main/kotlin/com/cepda/firetv/launcherwrapper/MainLauncherFragment.kt`

| Cambio | Descripción |
|--------|-------------|
| Renombrado clase | De `MainLauncherActivity` a `MainLauncherFragment` |
| Imports Leanback | Agregados `Row`, `RowPresenter`, `OnItemViewClickedListener` |
| Click listener | Cambiado override incorrecto a `onItemViewClickedListener` |
| Lifecycle correcto | Movido setup a `onActivityCreated()` |
| Logs detallados | TAG: `MainLauncherFragment` en todo el ciclo de vida |

```diff
- class MainLauncherActivity : BrowseSupportFragment()
+ class MainLauncherFragment : BrowseSupportFragment()

+ import androidx.leanback.widget.OnItemViewClickedListener
+ import androidx.leanback.widget.Row
+ import androidx.leanback.widget.RowPresenter

- override fun onItemClicked(...) { }
+ onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
+     if (item is AppItem) {
+         AppLauncher.launchApp(requireContext(), item.componentName)
+     }
+ }
```

---

### 4. `MainActivity.kt` - **NUEVO ARCHIVO**
**Ruta:** `app/src/main/kotlin/com/cepda/firetv/launcherwrapper/MainActivity.kt`

Activity contenedora que hostea el Fragment de Leanback.

```kotlin
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, MainLauncherFragment())
                .commit()
        }
    }
}
```

---

### 5. `AndroidManifest.xml`
**Ruta:** `app/src/main/AndroidManifest.xml`

| Cambio | Descripción |
|--------|-------------|
| Eliminado `package` | Ya está definido en `build.gradle` como `namespace` |
| Punto de entrada | Cambiado a `.MainActivity` |

```diff
- <manifest xmlns:android="..."
-     package="com.cepda.firetv.launcherwrapper">
+ <manifest xmlns:android="...">

- android:name=".MainLauncherActivity"
+ android:name=".MainActivity"
```

---

## Estructura Final del Proyecto

```
MainActivity.kt              ← Activity contenedora (punto de entrada)
    └── MainLauncherFragment.kt  ← Fragment con UI de Leanback
            ├── AppRepository.kt     ← Obtiene lista de apps instaladas
            ├── AppLauncher.kt       ← Lanza las apps seleccionadas
            └── model/AppItem.kt     ← Modelo de datos
```

---

## Archivos a Eliminar

Si todavía existe `MainLauncherActivity.kt`, debe ser **eliminado** porque:
- Fue renombrado a `MainLauncherFragment.kt`
- El punto de entrada ahora es `MainActivity.kt`

---

## Cómo Ver Logs

1. En Android Studio: `View → Tool Windows → Logcat`
2. Filtrar por tags:
   ```
   tag:AppRepository | tag:MainLauncherFragment
   ```

---

## Compilar e Instalar

```bash
# Compilar
./gradlew assembleDebug

# Instalar via ADB
adb install app/build/outputs/apk/debug/app-debug.apk
```

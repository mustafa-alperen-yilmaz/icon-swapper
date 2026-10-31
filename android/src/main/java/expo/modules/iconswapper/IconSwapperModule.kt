package expo.modules.iconswapper

import android.content.ComponentName
import android.content.pm.PackageManager
import expo.modules.kotlin.functions.Coroutine
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAIN_ACTIVITY_NAME = "MainActivity"

class IconSwapperModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("IconSwapper")

    AsyncFunction("supportsAlternateIconsAsync") { true }

    AsyncFunction("getAppIconNameAsync").Coroutine(this@IconSwapperModule::getAppIconName)
    AsyncFunction("setAlternateAppIconAsync").Coroutine(this@IconSwapperModule::setAlternateAppIcon)
  }

  private suspend fun getAppIconName(): String? = withContext(Dispatchers.Main) {
    val component = appContext.activityProvider?.currentActivity?.componentName ?: return@withContext null
    retrieveIconNameFromComponent(component)
  }

  // Called on the AppState "background" transition (see the JS caller), which is exactly when
  // the current Activity reference can momentarily be null while it's being torn down/paused.
  // The original library force-unwrapped `currentActivity!!` here and crashed with an NPE in
  // that window; every lookup below is a safe call that just no-ops instead.
  private suspend fun setAlternateAppIcon(iconName: String?): String? = withContext(Dispatchers.Main) {
    val currentActivityComponent = appContext.activityProvider?.currentActivity?.componentName
      ?: return@withContext null

    val currentIconName = retrieveIconNameFromComponent(currentActivityComponent)
    if (currentIconName == iconName) return@withContext iconName

    val newActivityComponent = replaceMainActivitySimpleName(currentActivityComponent, iconName)
    val packageManager = appContext.reactContext?.packageManager ?: return@withContext null

    packageManager.setComponentEnabledSetting(
      newActivityComponent,
      PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
      PackageManager.DONT_KILL_APP
    )
    packageManager.setComponentEnabledSetting(
      currentActivityComponent,
      PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
      PackageManager.DONT_KILL_APP
    )

    iconName
  }

  private fun getSimpleName(component: ComponentName): String = component.className.substringAfterLast('.')

  private fun retrieveIconNameFromComponent(component: ComponentName): String? =
    getSimpleName(component).let { simpleName ->
      when {
        simpleName == MAIN_ACTIVITY_NAME -> null
        simpleName.startsWith(MAIN_ACTIVITY_NAME) -> simpleName.substring(MAIN_ACTIVITY_NAME.length)
        else -> null
      }
    }

  private fun replaceMainActivitySimpleName(component: ComponentName, suffix: String?): ComponentName {
    val newSimpleName = "$MAIN_ACTIVITY_NAME${suffix ?: ""}"
    val identifiers = component.className.split('.').toMutableList()
    identifiers[identifiers.size - 1] = newSimpleName
    return ComponentName(component.packageName, identifiers.joinToString("."))
  }
}

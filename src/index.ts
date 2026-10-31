import IconSwapperModule from './IconSwapperModule';

export type { IconSwapperModuleType } from './IconSwapper.types';

/**
 * Whether this device/platform supports switching the app icon at runtime.
 * Safe to call at any time after the app has mounted — the native side only
 * touches UIApplication/the current Activity inside this call, never eagerly
 * at module registration, so it won't crash under the bridgeless New Architecture.
 */
export function supportsAlternateIcons(): Promise<boolean> {
  return IconSwapperModule.supportsAlternateIconsAsync();
}

/** The currently active alternate icon name, or `null` if the default icon is active. */
export function getAppIconName(): Promise<string | null> {
  return IconSwapperModule.getAppIconNameAsync();
}

/**
 * Switches the app icon. Pass `null` to restore the default icon.
 * The name must match one of the alternate icon names registered for the
 * platform (iOS: `CFBundleAlternateIcons` / `ASSETCATALOG_COMPILER_ALTERNATE_APPICON_NAMES`,
 * Android: a `MainActivity<Name>` activity-alias in AndroidManifest.xml).
 */
export function setAlternateAppIcon(iconName: string | null): Promise<string | null> {
  return IconSwapperModule.setAlternateAppIconAsync(iconName);
}

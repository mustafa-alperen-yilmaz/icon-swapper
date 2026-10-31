# icon-swapper

Switch an Expo app's icon at runtime on iOS and Android — safely, under the bridgeless New Architecture.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

## The problem

Existing alternate-icon libraries for Expo/React Native tend to touch platform APIs at the wrong time:

- **iOS.** The native module reads `UIApplication.shared` in a stored property initializer, which runs the instant Expo's module registry instantiates the module — at app launch, before any JavaScript has run. Under Expo SDK 55+, where the bridgeless New Architecture is mandatory, that timing is no longer safe, and the app crashes on every launch.
- **Android.** The native module force-unwraps the current `Activity` (`currentActivity!!`) to read or change the icon. That call is normally wired to run on the `AppState` "background" transition — which is exactly the moment the `Activity` reference can legitimately be `null` while it is being torn down, producing a `NullPointerException`.

Both are real crashes observed in production, not theoretical. `icon-swapper` was built specifically to avoid them:

- **iOS:** every `UIApplication` access lives inside an `@MainActor`-isolated function and only runs when JavaScript explicitly calls it — never eagerly, never at module registration.
- **Android:** every `Activity` / `PackageManager` lookup is a null-safe call (`?.`) that quietly no-ops instead of crashing when the `Activity` reference is momentarily unavailable.

## Features

- Promise-based API: `supportsAlternateIcons`, `getAppIconName`, `setAlternateAppIcon`.
- No eager native calls — nothing runs until you call it.
- Works with Expo's bridgeless New Architecture (mandatory since SDK 55).
- Always resolves to a consistent `Promise`-returning shape on both platforms.
- Tiny surface area, zero runtime dependencies beyond `expo-modules-core`.
- MIT licensed.

## Requirements

- An Expo app with `expo-modules-core` (any app created with `create-expo-app`, or a bare React Native app with Expo modules support installed).
- iOS 16.4+ / a recent Android Gradle Plugin — both are picked up automatically from your project, see [Compatibility](#compatibility).

## Installation

```sh
npx expo install icon-swapper
```

If you're on a bare React Native project (no Continuous Native Generation), also run:

```sh
npx pod-install
```

## Usage

```ts
import { supportsAlternateIcons, getAppIconName, setAlternateAppIcon } from 'icon-swapper';

async function switchToHalloweenIcon() {
  if (!(await supportsAlternateIcons())) {
    return; // e.g. unsupported OS version, or icon switching disabled by the platform
  }

  const current = await getAppIconName(); // null means the default icon is active

  if (current !== 'Halloween') {
    await setAlternateAppIcon('Halloween');
  }
}

async function restoreDefaultIcon() {
  await setAlternateAppIcon(null);
}
```

## API

### `supportsAlternateIcons(): Promise<boolean>`

Resolves to whether the current device/platform supports switching the app icon at runtime. Safe to call at any point after the app has mounted.

### `getAppIconName(): Promise<string | null>`

Resolves to the name of the currently active alternate icon, or `null` if the default icon is active.

### `setAlternateAppIcon(name: string | null): Promise<string | null>`

Switches the app icon to `name`. Pass `null` to restore the default icon. The name must match an icon already registered for the platform (see below). Resolves to the name that was applied.

## Registering alternate icons

`icon-swapper` only *switches* the icon at runtime — it does not generate the native registration for you. Each icon name needs to be registered natively first:

- **iOS:** add the icon to `ASSETCATALOG_COMPILER_ALTERNATE_APPICON_NAMES` (a space-separated list) in your build settings, with a matching `.appiconset` of the same name in `Images.xcassets`.
- **Android:** add a `MainActivity<Name>` `<activity-alias>` per icon in `AndroidManifest.xml`, targeting `.MainActivity`, each with its own `android:icon`.

A config plugin that generates both automatically from `app.config.js` (similar to what `expo-alternate-app-icons` does) is a natural next step for this project — contributions are welcome.

## Compatibility

Neither platform's SDK version is hardcoded in this library:

- **Android:** `compileSdk` / `minSdk` / `targetSdk` are read from your app's own Gradle configuration at build time, so this library always builds against whatever Android SDK your project already targets — no updates needed here when Google raises the required `targetSdkVersion` each year.
- **iOS:** the deployment target in the podspec is a floor, not a ceiling — the library is always compiled with whatever Xcode/iOS SDK your project builds with.

## Contributing

Issues and pull requests are welcome. This is a small, single-purpose library — please keep contributions focused on the icon-switching API rather than expanding scope.

## License

MIT © Mustafa Alperen Yılmaz

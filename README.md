# icon-swapper

Switch the app icon on iOS and Android from an Expo app — without crashing under the bridgeless New Architecture.

## Why this exists

The existing libraries for this (e.g. `expo-alternate-app-icons`) touch `UIApplication.shared` eagerly, in a stored property initializer that runs the moment the native module is instantiated by Expo's module registry at app launch — before any JS has run. Under Expo SDK 55+'s mandatory bridgeless New Architecture, that timing is no longer safe and the app crashes on every launch.

On Android, the same kind of library force-unwraps the current Activity (`currentActivity!!`) to read/change the icon. That call is meant to run on the `AppState` "background" transition — exactly the moment the Activity reference can legitimately be `null` while it's being torn down, producing a `NullPointerException` crash.

`icon-swapper` fixes both:
- **iOS**: every `UIApplication` touch lives inside an `@MainActor` function, only called when JS explicitly asks for it — never at module registration.
- **Android**: every Activity/PackageManager lookup is a safe call (`?.`) that no-ops instead of crashing when the Activity reference is momentarily unavailable.

## Install

```sh
npm install icon-swapper
npx pod-install
```

## Usage

```ts
import { supportsAlternateIcons, getAppIconName, setAlternateAppIcon } from 'icon-swapper';

if (await supportsAlternateIcons()) {
  const current = await getAppIconName(); // null = default icon
  await setAlternateAppIcon('Halloween'); // must match a registered alternate icon name
}
```

### Registering alternate icons

This library only switches the icon — it doesn't generate the native registration. You still need:

- **iOS**: `ASSETCATALOG_COMPILER_ALTERNATE_APPICON_NAMES` build setting + an `.appiconset` per name in `Images.xcassets`.
- **Android**: a `MainActivity<Name>` `<activity-alias>` per icon in `AndroidManifest.xml`, pointing at `.MainActivity` with its own `android:icon`.

A config plugin to generate these automatically (the way `expo-alternate-app-icons` does for `app.config.js`) is a natural next step — contributions welcome.

## API

- `supportsAlternateIcons(): Promise<boolean>`
- `getAppIconName(): Promise<string | null>`
- `setAlternateAppIcon(name: string | null): Promise<string | null>` — pass `null` to restore the default icon.

## License

MIT

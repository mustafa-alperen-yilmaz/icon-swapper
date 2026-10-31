import ExpoModulesCore
import UIKit

/// Switches the app icon via `UIApplication.setAlternateIconName`.
///
/// Every `UIApplication` access below lives inside a function body, never in a
/// stored property initializer. Expo instantiates this module as part of the
/// module registry at app launch — under the bridgeless New Architecture that
/// can happen before `UIApplication.shared` is safe to touch on whatever thread
/// the registry runs on. A property like `private var x = UIApplication.shared.foo`
/// would run then and there and can crash the whole app at launch. Deferring the
/// touch to a `@MainActor` function that only runs when JS explicitly calls it —
/// well after the app has mounted — avoids that entirely.
public class IconSwapperModule: Module {
  public func definition() -> ModuleDefinition {
    Name("IconSwapper")

    AsyncFunction("supportsAlternateIconsAsync") {
      await self.supportsAlternateIcons()
    }

    AsyncFunction("getAppIconNameAsync") {
      await self.currentAppIconName()
    }

    AsyncFunction("setAlternateAppIconAsync") { (iconName: String?) -> String? in
      try await self.applyAlternateIcon(iconName)
      return iconName
    }
  }

  @MainActor
  private func supportsAlternateIcons() -> Bool {
    UIApplication.shared.supportsAlternateIcons
  }

  @MainActor
  private func currentAppIconName() -> String? {
    UIApplication.shared.alternateIconName
  }

  @MainActor
  private func applyAlternateIcon(_ iconName: String?) async throws {
    try await UIApplication.shared.setAlternateIconName(iconName)
  }
}

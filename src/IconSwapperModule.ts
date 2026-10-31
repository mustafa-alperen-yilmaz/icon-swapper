import { NativeModule, requireNativeModule } from 'expo';
import type { IconSwapperModuleType } from './IconSwapper.types';

declare class IconSwapperModule
  extends NativeModule<{}>
  implements IconSwapperModuleType
{
  supportsAlternateIconsAsync(): Promise<boolean>;
  getAppIconNameAsync(): Promise<string | null>;
  setAlternateAppIconAsync(iconName: string | null): Promise<string | null>;
}

export default requireNativeModule<IconSwapperModule>('IconSwapper');

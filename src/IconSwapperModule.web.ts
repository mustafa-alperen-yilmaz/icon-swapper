import { registerWebModule, NativeModule } from 'expo';
import type { IconSwapperModuleType } from './IconSwapper.types';

class IconSwapperModule extends NativeModule<{}> implements IconSwapperModuleType {
  async supportsAlternateIconsAsync(): Promise<boolean> {
    return false;
  }

  async getAppIconNameAsync(): Promise<string | null> {
    return null;
  }

  async setAlternateAppIconAsync(_iconName: string | null): Promise<string | null> {
    return null;
  }
}

export default registerWebModule(IconSwapperModule, 'IconSwapperModule');

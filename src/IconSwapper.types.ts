export interface IconSwapperModuleType {
  supportsAlternateIconsAsync(): Promise<boolean>;
  getAppIconNameAsync(): Promise<string | null>;
  setAlternateAppIconAsync(iconName: string | null): Promise<string | null>;
}

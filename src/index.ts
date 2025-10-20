import { registerPlugin } from '@capacitor/core';

import type { ClerkNativePlugin } from './definitions';

const ClerkNative = registerPlugin<ClerkNativePlugin>('ClerkNative', {
  web: () => import('./web').then(m => new m.ClerkNativeWeb()),
});

export * from './definitions';
export * from './react';
export { ClerkNative };


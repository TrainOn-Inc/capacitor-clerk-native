import { WebPlugin } from '@capacitor/core';

import type { ClerkNativePlugin, ClerkUser } from './definitions';

export class ClerkNativeWeb extends WebPlugin implements ClerkNativePlugin {
  async configure(_options: { publishableKey: string }): Promise<void> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async load(): Promise<{ user: ClerkUser | null }> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async signInWithEmail(_options: { email: string }): Promise<{ requiresCode: boolean }> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async verifyEmailCode(_options: { code: string }): Promise<{ user: ClerkUser }> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async signInWithPassword(_options: {
    email: string;
    password: string;
  }): Promise<{ user: ClerkUser }> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async signUp(_options: {
    emailAddress: string;
    password: string;
    firstName?: string;
    lastName?: string;
  }): Promise<{ user: ClerkUser; requiresVerification: boolean }> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async verifySignUpEmail(_options: { code: string }): Promise<{ user: ClerkUser }> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async getUser(): Promise<{ user: ClerkUser | null }> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async getToken(): Promise<{ token: string | null }> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async signOut(): Promise<void> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async updateUser(_options: {
    firstName?: string;
    lastName?: string;
  }): Promise<{ user: ClerkUser }> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }
}


import { WebPlugin } from '@capacitor/core';

import type {
  ClerkNativePlugin,
  LoadResponse,
  SignInWithEmailResponse,
  VerifyEmailCodeResponse,
  SignInWithPasswordResponse,
  SignUpResponse,
  VerifySignUpEmailResponse,
  GetUserResponse,
  GetTokenResponse,
  UpdateUserResponse,
  RefreshSessionResponse,
  ConfigureOptions,
  SignInWithEmailOptions,
  VerifyEmailCodeOptions,
  SignInWithPasswordOptions,
  SignUpOptions,
  VerifySignUpEmailOptions,
  UpdateUserOptions,
  RequestPasswordResetOptions,
  ResetPasswordOptions,
} from './definitions';

export class ClerkNativeWeb extends WebPlugin implements ClerkNativePlugin {
  async configure(_options: ConfigureOptions): Promise<void> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async load(): Promise<LoadResponse> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async signInWithEmail(_options: SignInWithEmailOptions): Promise<SignInWithEmailResponse> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async verifyEmailCode(_options: VerifyEmailCodeOptions): Promise<VerifyEmailCodeResponse> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async signInWithPassword(_options: SignInWithPasswordOptions): Promise<SignInWithPasswordResponse> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async signUp(_options: SignUpOptions): Promise<SignUpResponse> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async verifySignUpEmail(_options: VerifySignUpEmailOptions): Promise<VerifySignUpEmailResponse> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async getUser(): Promise<GetUserResponse> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async getToken(): Promise<GetTokenResponse> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async signOut(): Promise<void> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async updateUser(_options: UpdateUserOptions): Promise<UpdateUserResponse> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async requestPasswordReset(_options: RequestPasswordResetOptions): Promise<void> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async resetPassword(_options: ResetPasswordOptions): Promise<void> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }

  async refreshSession(): Promise<RefreshSessionResponse> {
    throw this.unimplemented('Clerk Native is only available on iOS and Android');
  }
}


export interface ClerkNativePlugin {
  /**
   * Configure Clerk with publishable key
   */
  configure(options: ConfigureOptions): Promise<void>;

  /**
   * Load Clerk and check for existing session
   */
  load(): Promise<LoadResponse>;

  /**
   * Sign in with email code
   */
  signInWithEmail(options: SignInWithEmailOptions): Promise<SignInWithEmailResponse>;

  /**
   * Verify email code
   */
  verifyEmailCode(options: VerifyEmailCodeOptions): Promise<VerifyEmailCodeResponse>;

  /**
   * Sign in with email and password
   */
  signInWithPassword(options: SignInWithPasswordOptions): Promise<SignInWithPasswordResponse>;

  /**
   * Sign up with email and password
   */
  signUp(options: SignUpOptions): Promise<SignUpResponse>;

  /**
   * Verify email for sign up
   */
  verifySignUpEmail(options: VerifySignUpEmailOptions): Promise<VerifySignUpEmailResponse>;

  /**
   * Get current user
   */
  getUser(): Promise<GetUserResponse>;

  /**
   * Get authentication token
   */
  getToken(): Promise<GetTokenResponse>;

  /**
   * Sign out current user
   */
  signOut(): Promise<void>;

  /**
   * Update user profile
   */
  updateUser(options: UpdateUserOptions): Promise<UpdateUserResponse>;

  /**
   * Request password reset
   */
  requestPasswordReset(options: RequestPasswordResetOptions): Promise<void>;

  /**
   * Reset password with code
   */
  resetPassword(options: ResetPasswordOptions): Promise<void>;

  /**
   * Refresh session token
   */
  refreshSession(): Promise<RefreshSessionResponse>;
}

export interface ClerkUser {
  id: string;
  firstName: string | null;
  lastName: string | null;
  emailAddress: string | null;
  imageUrl: string | null;
  username: string | null;
}

// Response Types
export interface LoadResponse {
  user: ClerkUser | null;
}

export interface SignInWithEmailResponse {
  requiresCode: boolean;
}

export interface VerifyEmailCodeResponse {
  user: ClerkUser;
}

export interface SignInWithPasswordResponse {
  user: ClerkUser;
}

export interface SignUpResponse {
  user: ClerkUser;
  requiresVerification: boolean;
}

export interface VerifySignUpEmailResponse {
  user: ClerkUser;
}

export interface GetUserResponse {
  user: ClerkUser | null;
}

export interface GetTokenResponse {
  token: string | null;
}

export interface UpdateUserResponse {
  user: ClerkUser;
}

export interface RefreshSessionResponse {
  token: string | null;
}

// Parameter Types
export interface ConfigureOptions {
  publishableKey: string;
}

export interface SignInWithEmailOptions {
  email: string;
}

export interface VerifyEmailCodeOptions {
  code: string;
}

export interface SignInWithPasswordOptions {
  email: string;
  password: string;
}

export interface SignUpOptions {
  emailAddress: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export interface VerifySignUpEmailOptions {
  code: string;
}

export interface UpdateUserOptions {
  firstName?: string;
  lastName?: string;
}

export interface RequestPasswordResetOptions {
  email: string;
}

export interface ResetPasswordOptions {
  code: string;
  newPassword: string;
}

// Error Types
export interface ClerkError {
  message: string;
  code?: string;
  statusCode?: number;
}

// State Types
export interface ClerkState {
  isLoaded: boolean;
  isSignedIn: boolean;
  user: ClerkUser | null;
}


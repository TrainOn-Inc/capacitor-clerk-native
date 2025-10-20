export interface ClerkNativePlugin {
  /**
   * Configure Clerk with publishable key
   */
  configure(options: { publishableKey: string }): Promise<void>;

  /**
   * Load Clerk and check for existing session
   */
  load(): Promise<{ user: ClerkUser | null }>;

  /**
   * Sign in with email code
   */
  signInWithEmail(options: { email: string }): Promise<{ requiresCode: boolean }>;

  /**
   * Verify email code
   */
  verifyEmailCode(options: { code: string }): Promise<{ user: ClerkUser }>;

  /**
   * Sign in with email and password
   */
  signInWithPassword(options: { email: string; password: string }): Promise<{ user: ClerkUser }>;

  /**
   * Sign up with email and password
   */
  signUp(options: {
    emailAddress: string;
    password: string;
    firstName?: string;
    lastName?: string;
  }): Promise<{ user: ClerkUser; requiresVerification: boolean }>;

  /**
   * Verify email for sign up
   */
  verifySignUpEmail(options: { code: string }): Promise<{ user: ClerkUser }>;

  /**
   * Get current user
   */
  getUser(): Promise<{ user: ClerkUser | null }>;

  /**
   * Get authentication token
   */
  getToken(): Promise<{ token: string | null }>;

  /**
   * Sign out current user
   */
  signOut(): Promise<void>;

  /**
   * Update user profile
   */
  updateUser(options: {
    firstName?: string;
    lastName?: string;
  }): Promise<{ user: ClerkUser }>;
}

export interface ClerkUser {
  id: string;
  firstName: string | null;
  lastName: string | null;
  emailAddress: string | null;
  imageUrl: string | null;
  username: string | null;
}


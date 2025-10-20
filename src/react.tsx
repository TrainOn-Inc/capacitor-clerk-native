import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { ClerkNative } from './index';
import type { ClerkUser } from './definitions';

interface ClerkContextValue {
  isLoaded: boolean;
  isSignedIn: boolean;
  user: ClerkUser | null;
  signInWithEmail: (email: string) => Promise<{ requiresCode: boolean }>;
  verifyEmailCode: (code: string) => Promise<void>;
  signInWithPassword: (email: string, password: string) => Promise<void>;
  signUp: (options: {
    emailAddress: string;
    password: string;
    firstName?: string;
    lastName?: string;
  }) => Promise<{ requiresVerification: boolean }>;
  verifySignUpEmail: (code: string) => Promise<void>;
  signOut: () => Promise<void>;
  getToken: () => Promise<string | null>;
  updateUser: (options: { firstName?: string; lastName?: string }) => Promise<void>;
}

const ClerkContext = createContext<ClerkContextValue | null>(null);

export interface ClerkProviderProps {
  publishableKey: string;
  children: React.ReactNode;
}

export function ClerkProvider({ publishableKey, children }: ClerkProviderProps) {
  const [isLoaded, setIsLoaded] = useState(false);
  const [user, setUser] = useState<ClerkUser | null>(null);

  useEffect(() => {
    const initClerk = async () => {
      try {
        await ClerkNative.configure({ publishableKey });
        const result = await ClerkNative.load();
        setUser(result.user);
        setIsLoaded(true);
      } catch (error) {
        console.error('Failed to initialize Clerk:', error);
        setIsLoaded(true);
      }
    };

    initClerk();
  }, [publishableKey]);

  const signInWithEmail = useCallback(async (email: string) => {
    const result = await ClerkNative.signInWithEmail({ email });
    return result;
  }, []);

  const verifyEmailCode = useCallback(async (code: string) => {
    const result = await ClerkNative.verifyEmailCode({ code });
    setUser(result.user);
  }, []);

  const signInWithPassword = useCallback(async (email: string, password: string) => {
    const result = await ClerkNative.signInWithPassword({ email, password });
    setUser(result.user);
  }, []);

  const signUp = useCallback(
    async (options: {
      emailAddress: string;
      password: string;
      firstName?: string;
      lastName?: string;
    }) => {
      const result = await ClerkNative.signUp(options);
      if (!result.requiresVerification) {
        setUser(result.user);
      }
      return result;
    },
    []
  );

  const verifySignUpEmail = useCallback(async (code: string) => {
    const result = await ClerkNative.verifySignUpEmail({ code });
    setUser(result.user);
  }, []);

  const signOut = useCallback(async () => {
    await ClerkNative.signOut();
    setUser(null);
  }, []);

  const getToken = useCallback(async () => {
    const result = await ClerkNative.getToken();
    return result.token;
  }, []);

  const updateUser = useCallback(
    async (options: { firstName?: string; lastName?: string }) => {
      const result = await ClerkNative.updateUser(options);
      setUser(result.user);
    },
    []
  );

  const value: ClerkContextValue = {
    isLoaded,
    isSignedIn: user !== null,
    user,
    signInWithEmail,
    verifyEmailCode,
    signInWithPassword,
    signUp,
    verifySignUpEmail,
    signOut,
    getToken,
    updateUser,
  };

  return <ClerkContext.Provider value={value}>{children}</ClerkContext.Provider>;
}

export function useClerk() {
  const context = useContext(ClerkContext);
  if (!context) {
    throw new Error('useClerk must be used within a ClerkProvider');
  }
  return context;
}

export function useUser() {
  const { user, isLoaded, isSignedIn } = useClerk();
  return { user, isLoaded, isSignedIn };
}

export function useAuth() {
  const { isSignedIn, isLoaded, signOut, getToken } = useClerk();
  return { isSignedIn, isLoaded, signOut, getToken };
}

export function useSignIn() {
  const { signInWithEmail, verifyEmailCode, signInWithPassword, isLoaded } = useClerk();
  return {
    isLoaded,
    signIn: {
      create: async ({ identifier, password }: { identifier: string; password?: string }) => {
        // If password is provided, sign in directly with password
        if (password) {
          await signInWithPassword(identifier, password);
          return { status: 'complete', createdSessionId: 'session' };
        }
        // Otherwise, use email code flow
        return signInWithEmail(identifier);
      },
      prepareFirstFactor: async () => {
        // Already handled in signInWithEmail
      },
      attemptFirstFactor: async ({ strategy, code }: { strategy: string; code?: string }) => {
        if (strategy === 'email_code' && code) {
          await verifyEmailCode(code);
          return { status: 'complete', createdSessionId: 'session' };
        }
        return { status: 'needs_verification' };
      },
    },
    setActive: async () => {
      // Session is automatically set in native plugin
    },
  };
}

export function useSignUp() {
  const { signUp, verifySignUpEmail, isLoaded } = useClerk();
  return {
    isLoaded,
    signUp: {
      create: async (options: {
        emailAddress: string;
        password: string;
        firstName?: string;
        lastName?: string;
      }) => {
        await signUp(options);
        return { status: 'needs_verification' };
      },
      prepareVerification: async () => {
        // Already handled in signUp
      },
      attemptVerification: async ({ strategy, code }: { strategy: string; code: string }) => {
        if (strategy === 'email_code') {
          await verifySignUpEmail(code);
          return { status: 'complete', createdSessionId: 'session' };
        }
        return { status: 'needs_verification' };
      },
    },
    setActive: async () => {
      // Session is automatically set in native plugin
    },
  };
}


# capacitor-clerk-native

A Capacitor plugin for native Clerk authentication on iOS and Android using the bridge pattern to seamlessly integrate Clerk's native SDKs with CocoaPods/Gradle-based Capacitor plugins.

## The Problem This Solves

When using Clerk authentication in Capacitor iOS apps, WebView cookie limitations cause authentication failures with the error:

```
Browser unauthenticated (dev_browser_unauthenticated)
```

Additionally, Clerk's iOS SDK is only available via Swift Package Manager (SPM), but Capacitor plugins use CocoaPods, creating a dependency conflict.

## The Solution

This plugin uses a **bridge pattern** to resolve the CocoaPods ↔ SPM conflict:

1. **Plugin (CocoaPods)**: Defines a protocol interface without depending on Clerk
2. **App Target (SPM)**: Implements the bridge using Clerk's native SDK
3. **AppDelegate**: Connects them at runtime

This allows your Capacitor app to use Clerk's native iOS/Android SDKs, avoiding WebView cookie issues entirely.

## Installation

### From GitHub Packages (Recommended)

1. Create a `.npmrc` file in your project root:
```
@trainon-inc:registry=https://npm.pkg.github.com
//npm.pkg.github.com/:_authToken=YOUR_GITHUB_TOKEN
```

2. Install the package:
```bash
npm install @trainon-inc/capacitor-clerk-native
# or
pnpm add @trainon-inc/capacitor-clerk-native
# or
yarn add @trainon-inc/capacitor-clerk-native
```

> **Note**: You'll need a GitHub Personal Access Token with `read:packages` permission. [Create one here](https://github.com/settings/tokens/new?scopes=read:packages)

### From NPM (Future)

Once published to npm:
```bash
npm install capacitor-clerk-native
```

## iOS Setup

### 1. Add Clerk iOS SDK to Your App Target

1. Open your iOS project in Xcode
2. Select your **App** target
3. Go to **"Package Dependencies"** tab
4. Click **"+"** → **"Add Package Dependency"**
5. Enter: `https://github.com/clerk/clerk-ios`
6. Select version `0.69.0` or later
7. Link to your **App** target

### 2. Create the Bridge Implementation

Create a file `ClerkBridgeImpl.swift` in your app's directory:

```swift
import Foundation
import Clerk
import capacitor_clerk_native

@MainActor
class ClerkBridgeImpl: NSObject, ClerkBridge {
    private let clerk = Clerk.shared

    func signIn(withEmail email: String, password: String, completion: @escaping (String?, Error?) -> Void) {
        Task { @MainActor in
            do {
                let signIn = try await SignIn.create(strategy: .identifier(email))
                let result = try await signIn.attemptFirstFactor(strategy: .password(password: password))

                if result.status == .complete {
                    if let user = clerk.user {
                        completion(user.id, nil)
                    } else {
                        completion(nil, NSError(domain: "ClerkBridge", code: -1, userInfo: [NSLocalizedDescriptionKey: "Sign in completed but no user found"]))
                    }
                } else {
                    completion(nil, NSError(domain: "ClerkBridge", code: -1, userInfo: [NSLocalizedDescriptionKey: "Sign in not complete"]))
                }
            } catch {
                completion(nil, error)
            }
        }
    }

    func signUp(withEmail email: String, password: String, completion: @escaping (String?, Error?) -> Void) {
        Task { @MainActor in
            do {
                let signUp = try await SignUp.create(
                    strategy: .standard(emailAddress: email, password: password)
                )

                if let user = clerk.user {
                    completion(user.id, nil)
                } else {
                    completion(nil, NSError(domain: "ClerkBridge", code: -1, userInfo: [NSLocalizedDescriptionKey: "Sign up completed but no user found"]))
                }
            } catch {
                completion(nil, error)
            }
        }
    }

    func signOut(completion: @escaping (Error?) -> Void) {
        Task { @MainActor in
            do {
                try await clerk.signOut()
                completion(nil)
            } catch {
                completion(error)
            }
        }
    }

    func getToken(completion: @escaping (String?, Error?) -> Void) {
        Task { @MainActor in
            do {
                if let session = clerk.session {
                    if let token = try await session.getToken() {
                        completion(token.jwt, nil)
                    } else {
                        completion(nil, NSError(domain: "ClerkBridge", code: -1, userInfo: [NSLocalizedDescriptionKey: "No token available"]))
                    }
                } else {
                    completion(nil, NSError(domain: "ClerkBridge", code: -1, userInfo: [NSLocalizedDescriptionKey: "No active session"]))
                }
            } catch {
                completion(nil, error)
            }
        }
    }

    func getUser(completion: @escaping ([String: Any]?, Error?) -> Void) {
        Task { @MainActor in
            if let user = clerk.user {
                let userDict: [String: Any] = [
                    "id": user.id,
                    "firstName": user.firstName ?? NSNull(),
                    "lastName": user.lastName ?? NSNull(),
                    "emailAddress": user.primaryEmailAddress?.emailAddress ?? NSNull(),
                    "imageUrl": user.imageUrl ?? NSNull(),
                    "username": user.username ?? NSNull()
                ]
                completion(userDict, nil)
            } else {
                completion(nil, nil)
            }
        }
    }

    func isSignedIn(completion: @escaping (Bool, Error?) -> Void) {
        Task { @MainActor in
            completion(clerk.user != nil, nil)
        }
    }
}
```

### 3. Update AppDelegate

```swift
import UIKit
import Capacitor
import Clerk
import capacitor_clerk_native

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    private let clerkBridge = ClerkBridgeImpl()

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Configure Clerk
        if let publishableKey = ProcessInfo.processInfo.environment["CLERK_PUBLISHABLE_KEY"] ?? Bundle.main.infoDictionary?["ClerkPublishableKey"] as? String {
            Clerk.shared.configure(publishableKey: publishableKey)
        }

        // Set up the Clerk bridge for the Capacitor plugin
        ClerkNativePlugin.setClerkBridge(clerkBridge)

        return true
    }

    // ... rest of AppDelegate methods
}
```

### 4. Add ClerkBridgeImpl.swift to Xcode Project

1. In Xcode, right-click your **"App"** folder
2. Select **"Add Files to 'App'..."**
3. Select `ClerkBridgeImpl.swift`
4. **Uncheck** "Copy items if needed"
5. **Check** the "App" target
6. Click **"Add"**

### 5. Configure Clerk Publishable Key

**Option A: Build Settings**
1. Select the **App** target → **Build Settings**
2. Click **"+"** → **"Add User-Defined Setting"**
3. Name: `CLERK_PUBLISHABLE_KEY`
4. Value: Your Clerk publishable key

**Option B: xcconfig File** (Recommended)
1. Create `Config.xcconfig` in your App folder:
```
CLERK_PUBLISHABLE_KEY = pk_test_your_clerk_key_here
```
2. Add it to Xcode project
3. Set it for both Debug and Release configurations in Project Info

### 6. Update Info.plist

Add your Clerk publishable key:

```xml
<key>ClerkPublishableKey</key>
<string>$(CLERK_PUBLISHABLE_KEY)</string>
```

## React/JavaScript Usage

```typescript
import { ClerkProvider, useAuth, useUser, useSignIn, useSignUp } from '@trainon-inc/capacitor-clerk-native';

// Wrap your app
function App() {
  return (
    <ClerkProvider publishableKey="pk_test_...">
      <YourApp />
    </ClerkProvider>
  );
}

// Use in components
function LoginPage() {
  const { signIn } = useSignIn();

  const handleLogin = async () => {
    const result = await signIn.create({
      identifier: email,
      password: password
    });

    if (result.status === 'complete') {
      // Navigate to app
    }
  };
}

// Get auth state
function Profile() {
  const { user } = useUser();
  const { getToken } = useAuth();

  const token = await getToken();
}
```

## Architecture

```
┌─────────────────────────────────────────────────┐
│  JavaScript/React (Capacitor WebView)           │
│  - Uses capacitor-clerk-native hooks            │
└─────────────────┬───────────────────────────────┘
                  │ Capacitor Bridge
┌─────────────────▼───────────────────────────────┐
│  ClerkNativePlugin (CocoaPods Pod)              │
│  - Defines ClerkBridge protocol                 │
│  - Receives calls from JavaScript               │
│  - Delegates to bridge implementation           │
└─────────────────┬───────────────────────────────┘
                  │ Protocol/Delegate
┌─────────────────▼───────────────────────────────┐
│  ClerkBridgeImpl (App Target, SPM)              │
│  - Implements ClerkBridge protocol              │
│  - Uses Clerk iOS SDK directly                  │
│  - Handles all Clerk authentication             │
└─────────────────────────────────────────────────┘
```

## API

### Methods

- `signInWithPassword(email: string, password: string)` - Sign in with email/password
- `signUp(email: string, password: string)` - Create a new account
- `signOut()` - Sign out current user
- `getToken()` - Get the authentication token
- `getUser()` - Get current user data
- `isSignedIn()` - Check if user is signed in

### React Hooks

- `useAuth()` - Authentication state and methods
- `useUser()` - Current user data
- `useSignIn()` - Sign in methods
- `useSignUp()` - Sign up methods
- `useClerk()` - Full Clerk context

## Android Support

Android support is planned. The bridge pattern will work similarly with Gradle and the Clerk Android SDK.

## Contributing

Contributions are welcome! This plugin was created to solve a real problem we encountered, and we'd love to make it better.

## License

MIT

## Credits

Created by the TrainOn Team to solve CocoaPods ↔ SPM conflicts when integrating Clerk authentication in Capacitor iOS apps.

## Support

- [GitHub Issues](https://github.com/Trainon-Inc/capacitor-clerk-native/issues)
- [Clerk Documentation](https://clerk.com/docs)
- [Capacitor Documentation](https://capacitorjs.com/docs)

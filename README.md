# capacitor-clerk-native

A Capacitor plugin for native Clerk authentication on iOS and Android using the bridge pattern to seamlessly integrate Clerk's native SDKs with CocoaPods/Gradle-based Capacitor plugins!

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

This allows your Capacitor app to use Clerk's native iOS/Android SDKs (following the official [Clerk iOS Quickstart](https://clerk.com/docs/ios/getting-started/quickstart)), avoiding WebView cookie issues entirely.

### Why This Approach?

- ✅ Uses **official Clerk iOS SDK** - same as native Swift apps
- ✅ Follows **Clerk's best practices** for iOS integration
- ✅ **No WebView limitations** - native authentication flows
- ✅ **CocoaPods compatible** - works with Capacitor's build system
- ✅ **Reusable** - bridge pattern can be adapted for other native SDKs

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

### Prerequisites

Before starting, ensure you have:
- ✅ A [Clerk account](https://dashboard.clerk.com/sign-up)
- ✅ A Clerk application set up in the dashboard
- ✅ **Native API enabled** in Clerk Dashboard → Settings → Native Applications

> **Important**: You must enable the Native API in your Clerk Dashboard before proceeding. This is required for native iOS integration.

### 1. Register Your iOS App with Clerk

1. Go to the [**Native Applications**](https://dashboard.clerk.com/last-active?path=native-applications) page in Clerk Dashboard
2. Click **"Add Application"**
3. Enter your iOS app details:
   - **App ID Prefix**: Found in your Apple Developer account or Xcode (Team ID)
   - **Bundle ID**: Your app's bundle identifier (e.g., `com.trainon.member`)
4. Note down your **Frontend API URL** (you'll need this for associated domains)

### 2. Add Associated Domain Capability

This is required for seamless authentication flows:

1. In Xcode, select your project → Select your **App** target
2. Go to **"Signing & Capabilities"** tab
3. Click **"+ Capability"** → Add **"Associated Domains"**
4. Under Associated Domains, add: `webcredentials:{YOUR_FRONTEND_API_URL}`
   - Example: `webcredentials:guiding-serval-42.clerk.accounts.dev`
   - Get your Frontend API URL from the Native Applications page in Clerk Dashboard

### 3. Add Clerk iOS SDK to Your App Target

1. Open your iOS project in Xcode
2. Select your **App** target
3. Go to **"Package Dependencies"** tab
4. Click **"+"** → **"Add Package Dependency"**
5. Enter: `https://github.com/clerk/clerk-ios`
6. Select version `0.69.0` or later
7. Link to your **App** target

### 4. Create the Bridge Implementation

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

### 5. Update AppDelegate

> **Note**: Your AppDelegate should configure Clerk when the app launches, just like in the [Clerk iOS Quickstart](https://clerk.com/docs/ios/getting-started/quickstart).

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

### 6. Add ClerkBridgeImpl.swift to Xcode Project

1. In Xcode, right-click your **"App"** folder
2. Select **"Add Files to 'App'..."**
3. Select `ClerkBridgeImpl.swift`
4. **Uncheck** "Copy items if needed"
5. **Check** the "App" target
6. Click **"Add"**

### 7. Configure Clerk Publishable Key

> **Important**: Get your Publishable Key from the [**API Keys**](https://dashboard.clerk.com/last-active?path=api-keys) page in Clerk Dashboard.

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

### 8. Update Info.plist

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

## Troubleshooting

### Common Issues

#### "Browser unauthenticated" error in WebView
✅ **Solved!** This plugin uses native SDKs instead of WebView, eliminating this issue entirely.

#### "Native API not enabled"
- Go to Clerk Dashboard → Settings → Native Applications
- Enable the Native API toggle
- Refresh your app

#### Associated Domain not working
- Verify you added `webcredentials:{YOUR_FRONTEND_API_URL}` correctly
- Get the exact URL from Clerk Dashboard → Native Applications
- Clean build folder and rebuild (Shift+Cmd+K in Xcode)

#### "No such module 'Clerk'" in ClerkBridgeImpl
- Ensure Clerk iOS SDK is added to your App target (not just the project)
- Check Package Dependencies tab shows Clerk linked to your target
- Clean and rebuild

#### Plugin not found or not responding
- Run `npx cap sync` to sync the plugin
- Verify plugin is in node_modules: `@trainon-inc/capacitor-clerk-native`
- Check Podfile includes the plugin after running cap sync

#### Authentication not persisting
- Clerk handles session persistence automatically
- Verify `clerk.load()` is called in AppDelegate
- Check Clerk SDK is properly configured with your publishable key

### Getting Help

- 📖 [Clerk iOS Documentation](https://clerk.com/docs/ios)
- 💬 [Clerk Discord Community](https://clerk.com/discord)
- 🐛 [Report Issues](https://github.com/TrainOn-Inc/capacitor-clerk-native/issues)

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

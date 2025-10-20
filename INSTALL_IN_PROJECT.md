# Installing capacitor-clerk-native in Your TrainOn Project

## Step 1: Configure GitHub Packages Authentication

In your main project (`/Users/faisalnawaz/workspace/trainon/Trainon`), create or update `.npmrc`:

```bash
cd /Users/faisalnawaz/workspace/trainon/Trainon
cat >> .npmrc << 'EOF'
@trainon-inc:registry=https://npm.pkg.github.com
//npm.pkg.github.com/:_authToken=${GITHUB_TOKEN}
EOF
```

Then set your GitHub token as an environment variable:
```bash
export GITHUB_TOKEN=your_github_personal_access_token
```

## Step 2: Remove the Local Workspace Dependency

Update your `apps/member/package.json`:

```json
{
  "dependencies": {
    // Remove this:
    // "@train-on/capacitor-clerk-native": "workspace:*"
    
    // Add this:
    "@trainon-inc/capacitor-clerk-native": "^1.0.0"
  }
}
```

## Step 3: Update Imports

The package name has changed from `@train-on/capacitor-clerk-native` to `@trainon-inc/capacitor-clerk-native`.

Update imports in:
- `apps/member/src/app/app.tsx`
- `apps/member/src/lib/clerk.ts`
- `apps/member/src/components/TRPCProvider.tsx`
- Any other files importing from the old package

```typescript
// Old:
import { ClerkProvider } from '@train-on/capacitor-clerk-native';

// New:
import { ClerkProvider } from '@trainon-inc/capacitor-clerk-native';
```

## Step 4: Update iOS Podfile

Update `apps/member/ios/App/Podfile`:

```ruby
def capacitor_pods
  pod 'Capacitor', :path => '.../@capacitor/ios'
  pod 'CapacitorCordova', :path => '.../@capacitor/ios'
  
  # Old (remove):
  # pod 'TrainOnCapacitorClerkNative', :path => '../../../../libs/capacitor-clerk-native'
  
  # This will be automatically added by capacitor sync:
  # It will reference the installed npm package
end
```

## Step 5: Update Swift Imports

In `apps/member/ios/App/App/ClerkBridgeImpl.swift` and `AppDelegate.swift`:

```swift
// Old:
import TrainOnCapacitorClerkNative

// New:
import ClerkNativeCapacitor
```

## Step 6: Install Dependencies

```bash
cd /Users/faisalnawaz/workspace/trainon/Trainon
pnpm install
```

## Step 7: Sync Capacitor

```bash
npx nx run member:cap:sync
```

## Step 8: Build and Test

```bash
npx nx run member:build -c development
npx nx run member:cap:run:ios -c development
```

## Verification Checklist

- [ ] GitHub token configured in `.npmrc`
- [ ] Package installed from GitHub Packages
- [ ] All imports updated to `@trainon-inc/capacitor-clerk-native`
- [ ] Swift imports updated to `ClerkNativeCapacitor`
- [ ] App builds successfully
- [ ] Authentication works on iOS simulator
- [ ] Can get auth tokens
- [ ] Sign out works

## Troubleshooting

### "Unable to authenticate with GitHub Packages"
- Verify your GitHub token has `read:packages` scope
- Check that `.npmrc` is in the project root
- Ensure `GITHUB_TOKEN` environment variable is set

### "Module not found: @trainon-inc/capacitor-clerk-native"
- Run `pnpm install` again
- Clear node_modules and reinstall: `rm -rf node_modules && pnpm install`

### "No such module 'ClerkNativeCapacitor'" in Xcode
- Clean build folder in Xcode (Shift+Cmd+K)
- Run `pod install` in `apps/member/ios/App`
- Rebuild the project

## Benefits of Using the Package

✅ **Versioned releases** - Stable, tagged versions
✅ **Automated builds** - CI/CD ensures quality
✅ **Easy updates** - `pnpm update @trainon-inc/capacitor-clerk-native`
✅ **Reusable** - Can be used in other projects
✅ **Community ready** - Can be shared with others facing the same issue


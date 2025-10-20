# Release Guide

## Creating a New Release

### Fully Automated Release (Default)

**Just push to main!** 🚀

Every push to the `main` branch automatically:
1. Checks if a tag exists for the current version
2. If tag exists → bumps the **minor version** (e.g., 1.0.0 → 1.1.0)
3. Creates a new tag
4. Builds the package
5. Creates a GitHub Release
6. Publishes to GitHub Packages

**No manual steps required!** The version increments automatically on every merge to main.

### Manual Version Control

If you need to control the version manually:

1. **Update version** in `package.json`:
```bash
# For patch release (1.0.0 -> 1.0.1)
npm version patch -m "chore: bump version to %s [skip ci]"

# For major release (1.0.0 -> 2.0.0)
npm version major -m "chore: bump version to %s [skip ci]"
```

2. **Push to main**:
```bash
git push origin main
```

3. The auto-version workflow will detect the new version and create a release.

### Manual Release

If needed, you can manually create a release:

```bash
# Build
npm run build

# Create tag
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

## Installing from GitHub Packages

### For Users

1. Create `.npmrc` in your project:
```
@trainon-inc:registry=https://npm.pkg.github.com
//npm.pkg.github.com/:_authToken=YOUR_GITHUB_TOKEN
```

2. Install:
```bash
npm install @trainon-inc/capacitor-clerk-native
```

### Creating GitHub Personal Access Token

1. Go to https://github.com/settings/tokens/new
2. Select scopes:
   - `read:packages` (for installing)
   - `write:packages` (for publishing)
3. Generate and save the token
4. Add to `.npmrc` or set as environment variable

## First Release Checklist

- [x] CI/CD workflows configured
- [x] Package scoped to `@trainon-inc`
- [x] README updated with installation instructions
- [ ] Create v1.0.0 tag
- [ ] Verify GitHub Actions runs successfully
- [ ] Test installation from GitHub Packages
- [ ] Update main project to use the package

## Version Strategy

- **Patch (1.0.x)**: Bug fixes, documentation updates
- **Minor (1.x.0)**: New features, backward compatible
- **Major (x.0.0)**: Breaking changes

## Continuous Deployment Flow

### Automatic (Default)

```
Commit & Push to main
    ↓
CI runs (build, test)
    ↓
Auto-version workflow checks tag
    ↓
Tag exists? → Bump minor version
    ↓
Create new tag
    ↓
Build & Create GitHub Release
    ↓
Publish to GitHub Packages
```

### Manual Control

```
Update package.json version [skip ci]
    ↓
Commit & Push to main
    ↓
Auto-version workflow detects new version
    ↓
Create tag for new version
    ↓
Build & Create GitHub Release
    ↓
Publish to GitHub Packages
```

## Skipping Auto-Versioning

To skip the auto-version workflow (e.g., for documentation updates), include `[skip ci]` in your commit message:

```bash
git commit -m "docs: update README [skip ci]"
```



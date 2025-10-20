# Release Guide

## Creating a New Release

### Automated Release (Recommended)

1. **Update version** in `package.json`:
```bash
# For patch release (1.0.0 -> 1.0.1)
npm version patch

# For minor release (1.0.0 -> 1.1.0)
npm version minor

# For major release (1.0.0 -> 2.0.0)
npm version major
```

2. **Update CHANGELOG.md** with release notes

3. **Create and push the tag**:
```bash
git push origin main --tags
```

4. **GitHub Actions will automatically**:
   - Build the package
   - Run tests
   - Create a GitHub Release
   - Publish to GitHub Packages

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

```
main branch
    ↓
Commit & Push
    ↓
CI runs (build, test)
    ↓
npm version [patch|minor|major]
    ↓
git push --tags
    ↓
Release workflow triggers
    ↓
GitHub Release created
    ↓
Published to GitHub Packages
```


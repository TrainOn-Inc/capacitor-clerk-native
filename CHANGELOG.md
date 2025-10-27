# Changelog

## [1.13.0] - 2025-10-27

### Changed
- feat: add password reset functionality and comprehensive type definitions

- Add requestPasswordReset and resetPassword methods
- Add refreshSession method for token management
- Add comprehensive type definitions for all methods and parameters
- Add new usePasswordReset hook
- Add response types for all plugin methods
- Add parameter types for all plugin methods
- Add ClerkError and ClerkState types
- Update React hooks with proper type annotations
- Update Web plugin with proper type annotations


## [1.12.0] - 2025-10-21

### Changed
- test: trigger CI/CD workflow for npm publishing


## [1.11.0] - 2025-10-21

### Changed
- chore: update package publishing configuration to use npm registry instead of GitHub Packages


## [1.10.0] - 2025-10-20

### Changed
- feat: add GitHub Actions workflow for publishing on tag and update release workflow for NPM token usage


## [1.9.0] - 2025-10-20

### Changed
- fix: update package publishing step to use bumped version for verification


## [1.8.0] - 2025-10-20

### Changed
- Merge branch 'main' of https://github.com/TrainOn-Inc/capacitor-clerk-native


## [1.7.0] - 2025-10-20

### Changed
- fix: update README for clarity and add pnpm-lock.yaml for dependency management


## [1.6.0] - 2025-10-20

### Changed
- Bump version from 1.4.0 to 1.5.0


## [1.5.0] - 2025-10-20

### Changed
- feat(): change version at end


All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-01-20

### Added
- Initial release of capacitor-clerk-native
- Bridge pattern implementation to resolve CocoaPods ↔ SPM conflicts
- iOS support with Clerk iOS SDK integration
- Native authentication avoiding WebView cookie issues
- React hooks for authentication (`useAuth`, `useUser`, `useSignIn`, `useSignUp`)
- ClerkProvider component for React apps
- Password-based authentication
- Token management and retrieval
- User session management
- Comprehensive documentation and setup guides
- MIT License
- Contributing guidelines

### iOS Features
- ClerkBridge protocol for clean separation
- Swift implementation using Clerk iOS SDK
- Seamless integration with Capacitor
- Support for sign in, sign up, sign out
- Token retrieval for API calls
- User profile access

### Known Limitations
- Android support not yet implemented
- Email code authentication not implemented
- Social authentication not implemented
- User profile updates not implemented

## [Unreleased]

## [1.2.0] - 2025-10-20

### Changed
- Automated release from main branch


## [1.1.0] - 2025-10-20

### Changed
- Automated release from main branch


### Planned
- Android implementation
- Email code authentication flow
- Social authentication (Google, Apple, etc.)
- User profile update methods
- Better error handling and logging
- Unit and integration tests
- Example apps



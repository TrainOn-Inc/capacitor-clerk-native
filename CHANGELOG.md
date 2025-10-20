# Changelog

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


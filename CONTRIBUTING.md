# Contributing to capacitor-clerk-native

Thank you for your interest in contributing! This plugin was created to solve a real problem we encountered when integrating Clerk authentication in Capacitor iOS apps, and we'd love your help making it better.

## How to Contribute

### Reporting Issues

If you encounter a bug or have a feature request:

1. Check if the issue already exists in [GitHub Issues](https://github.com/trainon/capacitor-clerk-native/issues)
2. If not, create a new issue with:
   - Clear description of the problem or feature
   - Steps to reproduce (for bugs)
   - Expected vs actual behavior
   - Environment details (iOS/Android version, Capacitor version, etc.)
   - Code samples if applicable

### Development Setup

1. Clone the repository:
```bash
git clone https://github.com/trainon/capacitor-clerk-native.git
cd capacitor-clerk-native
```

2. Install dependencies:
```bash
npm install
# or
pnpm install
```

3. Build the plugin:
```bash
npm run build
```

### Making Changes

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make your changes
4. Build and test your changes
5. Commit with a clear message: `git commit -m "feat: add amazing feature"`
6. Push to your fork: `git push origin feature/your-feature-name`
7. Open a Pull Request

### Commit Message Format

We follow conventional commits:

- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `refactor:` Code refactoring
- `test:` Adding tests
- `chore:` Maintenance tasks

Examples:
- `feat: add Android support`
- `fix: resolve token refresh issue`
- `docs: update installation instructions`

### Code Style

- Follow existing code patterns
- Use TypeScript for type safety
- Document public APIs
- Keep the bridge pattern architecture

### Testing

Before submitting a PR:

1. Build the plugin: `npm run build`
2. Test in a real Capacitor app
3. Verify iOS functionality (Android when available)
4. Ensure no TypeScript errors
5. Test the React hooks

### Areas We Need Help With

- **Android Implementation**: The Android bridge needs to be built
- **Documentation**: Improve setup guides, add troubleshooting
- **Testing**: Unit tests, integration tests
- **Features**: Email code authentication, social auth, etc.
- **Examples**: Sample apps demonstrating usage

### Questions?

- Open a [Discussion](https://github.com/trainon/capacitor-clerk-native/discussions)
- Or create an [Issue](https://github.com/trainon/capacitor-clerk-native/issues)

## License

By contributing, you agree that your contributions will be licensed under the MIT License.


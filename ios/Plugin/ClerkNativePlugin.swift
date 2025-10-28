import Foundation
import Capacitor

// Protocol for Clerk bridge - the App target will implement this
@objc public protocol ClerkBridge: AnyObject {
    func signIn(withEmail email: String, password: String, completion: @escaping (String?, Error?) -> Void)
    func signInWithEmailCode(email: String, completion: @escaping (Error?) -> Void)
    func verifyEmailCode(code: String, completion: @escaping (Error?) -> Void)
    func signUp(withEmail email: String, password: String, completion: @escaping (String?, Error?) -> Void)
    func signOut(completion: @escaping (Error?) -> Void)
    func getToken(completion: @escaping (String?, Error?) -> Void)
    func getUser(completion: @escaping ([String: Any]?, Error?) -> Void)
    func isSignedIn(completion: @escaping (Bool, Error?) -> Void)
}

@objc(ClerkNativePlugin)
public class ClerkNativePlugin: CAPPlugin {
    private weak var clerkBridge: ClerkBridge?

    @objc public static func setClerkBridge(_ bridge: ClerkBridge) {
        // Store bridge in a static property accessible to all plugin instances
        ClerkNativePlugin.sharedBridge = bridge
    }

    private static var sharedBridge: ClerkBridge?

    public override func load() {
        super.load()
        clerkBridge = ClerkNativePlugin.sharedBridge
    }

    @objc func configure(_ call: CAPPluginCall) {
        // Configuration is now handled by the bridge in AppDelegate
        call.resolve()
    }

    @objc func load(_ call: CAPPluginCall) {
        guard let bridge = clerkBridge else {
            call.reject("Clerk bridge not configured")
            return
        }

        bridge.getUser { user, error in
            if let error = error {
                call.reject("Failed to load Clerk: \(error.localizedDescription)")
            } else {
                call.resolve(["user": user ?? NSNull()])
            }
        }
    }

    @objc func signInWithPassword(_ call: CAPPluginCall) {
        guard let bridge = clerkBridge else {
            call.reject("Clerk bridge not configured")
            return
        }

        guard let email = call.getString("email"),
              let password = call.getString("password") else {
            call.reject("Must provide email and password")
            return
        }

        bridge.signIn(withEmail: email, password: password) { userId, error in
            if let error = error {
                call.reject("Sign in failed: \(error.localizedDescription)")
            } else {
                // Get the full user after sign in
                bridge.getUser { user, getUserError in
                    if let getUserError = getUserError {
                        call.reject("Sign in succeeded but failed to get user: \(getUserError.localizedDescription)")
                    } else {
                        call.resolve(["user": user ?? NSNull()])
                    }
                }
            }
        }
    }

    @objc func signUp(_ call: CAPPluginCall) {
        guard let bridge = clerkBridge else {
            call.reject("Clerk bridge not configured")
            return
        }

        guard let email = call.getString("emailAddress"),
              let password = call.getString("password") else {
            call.reject("Must provide emailAddress and password")
            return
        }

        bridge.signUp(withEmail: email, password: password) { userId, error in
            if let error = error {
                call.reject("Sign up failed: \(error.localizedDescription)")
            } else {
                // Get the full user after sign up
                bridge.getUser { user, getUserError in
                    if let getUserError = getUserError {
                        call.reject("Sign up succeeded but failed to get user: \(getUserError.localizedDescription)")
                    } else {
                        call.resolve(["user": user ?? NSNull(), "requiresVerification": false])
                    }
                }
            }
        }
    }

    @objc func getUser(_ call: CAPPluginCall) {
        guard let bridge = clerkBridge else {
            call.reject("Clerk bridge not configured")
            return
        }

        bridge.getUser { user, error in
            if let error = error {
                call.reject("Failed to get user: \(error.localizedDescription)")
            } else {
                call.resolve(["user": user ?? NSNull()])
            }
        }
    }

    @objc func getToken(_ call: CAPPluginCall) {
        guard let bridge = clerkBridge else {
            call.reject("Clerk bridge not configured")
            return
        }

        bridge.getToken { token, error in
            if let error = error {
                call.reject("Failed to get token: \(error.localizedDescription)")
            } else {
                call.resolve(["token": token ?? NSNull()])
            }
        }
    }

    @objc func signOut(_ call: CAPPluginCall) {
        guard let bridge = clerkBridge else {
            call.reject("Clerk bridge not configured")
            return
        }

        bridge.signOut { error in
            if let error = error {
                call.reject("Sign out failed: \(error.localizedDescription)")
            } else {
                call.resolve()
            }
        }
    }

    @objc func signInWithEmail(_ call: CAPPluginCall) {
        guard let bridge = clerkBridge else {
            call.reject("Clerk bridge not configured")
            return
        }

        guard let email = call.getString("email") else {
            call.reject("Must provide email")
            return
        }

        bridge.signInWithEmailCode(email: email) { error in
            if let error = error {
                call.reject("Sign in with email failed: \(error.localizedDescription)")
            } else {
                call.resolve(["requiresCode": true])
            }
        }
    }

    @objc func verifyEmailCode(_ call: CAPPluginCall) {
        guard let bridge = clerkBridge else {
            call.reject("Clerk bridge not configured")
            return
        }

        guard let code = call.getString("code") else {
            call.reject("Must provide code")
            return
        }

        bridge.verifyEmailCode(code: code) { error in
            if let error = error {
                call.reject("Email code verification failed: \(error.localizedDescription)")
            } else {
                // Get the full user after verification
                bridge.getUser { user, getUserError in
                    if let getUserError = getUserError {
                        call.reject("Verification succeeded but failed to get user: \(getUserError.localizedDescription)")
                    } else {
                        call.resolve(["user": user ?? NSNull()])
                    }
                }
            }
        }
    }

    @objc func verifySignUpEmail(_ call: CAPPluginCall) {
        // Sign up email verification - not implemented in simplified bridge
        call.reject("Sign up email verification not implemented")
    }

    @objc func updateUser(_ call: CAPPluginCall) {
        // User update - not implemented in simplified bridge
        call.reject("Update user not implemented")
    }

    @objc func requestPasswordReset(_ call: CAPPluginCall) {
        // Password reset request - not implemented in simplified bridge
        call.reject("Password reset not implemented")
    }

    @objc func resetPassword(_ call: CAPPluginCall) {
        // Password reset with code - not implemented in simplified bridge
        call.reject("Password reset with code not implemented")
    }

    @objc func refreshSession(_ call: CAPPluginCall) {
        // Refresh session token - not implemented in simplified bridge
        call.reject("Refresh session not implemented")
    }
}

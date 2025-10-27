#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

CAP_PLUGIN(ClerkNativePlugin, "ClerkNative",
           CAP_PLUGIN_METHOD(configure, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(load, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(signInWithEmail, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(verifyEmailCode, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(signInWithPassword, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(signUp, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(verifySignUpEmail, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getUser, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getToken, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(signOut, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(updateUser, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(requestPasswordReset, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(resetPassword, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(refreshSession, CAPPluginReturnPromise);
)


package com.trainon.capacitor.clerk;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Android stub for Clerk Native plugin.
 * 
 * On Android, authentication is handled by the web Clerk provider (@clerk/clerk-react)
 * because Android WebViews work well with web-based auth (unlike iOS which has cookie issues).
 * 
 * This plugin exists to satisfy Capacitor's plugin registration but all methods
 * will return errors indicating to use the web provider instead.
 */
@CapacitorPlugin(name = "ClerkNative")
public class ClerkNativePlugin extends Plugin {

    private static final String USE_WEB_MESSAGE = "Android uses web Clerk provider. Configure your app to use @clerk/clerk-react on Android.";

    @PluginMethod
    public void configure(PluginCall call) {
        // Allow configure to succeed silently - the web provider will handle auth
        call.resolve();
    }

    @PluginMethod
    public void load(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }

    @PluginMethod
    public void signInWithEmail(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }

    @PluginMethod
    public void verifyEmailCode(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }

    @PluginMethod
    public void signInWithPassword(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }

    @PluginMethod
    public void signUp(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }

    @PluginMethod
    public void verifySignUpEmail(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }

    @PluginMethod
    public void getUser(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }

    @PluginMethod
    public void getToken(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }

    @PluginMethod
    public void signOut(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }

    @PluginMethod
    public void updateUser(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }

    @PluginMethod
    public void requestPasswordReset(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }

    @PluginMethod
    public void resetPassword(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }

    @PluginMethod
    public void refreshSession(PluginCall call) {
        call.reject(USE_WEB_MESSAGE);
    }
}

package com.trainon.capacitor.clerk;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "ClerkNative")
public class ClerkNativePlugin extends Plugin {

    @PluginMethod
    public void configure(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void load(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void signInWithEmail(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void verifyEmailCode(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void signInWithPassword(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void signUp(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void verifySignUpEmail(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void getUser(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void getToken(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void signOut(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void updateUser(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void requestPasswordReset(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void resetPassword(PluginCall call) {
        call.reject("Android not yet implemented");
    }

    @PluginMethod
    public void refreshSession(PluginCall call) {
        call.reject("Android not yet implemented");
    }
}


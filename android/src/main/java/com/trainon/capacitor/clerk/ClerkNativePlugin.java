package com.trainon.capacitor.clerk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Android implementation of Clerk Native plugin.
 * 
 * Makes HTTP requests directly to Clerk's Frontend API, bypassing WebView origin restrictions.
 * This allows the custom Clerk proxy domain to work on Android.
 */
@CapacitorPlugin(name = "ClerkNative")
public class ClerkNativePlugin extends Plugin {

    private static final String TAG = "ClerkNativePlugin";
    private static final String PREFS_NAME = "ClerkNativePrefs";
    private static final String PREF_SESSION_TOKEN = "session_token";
    private static final String PREF_CLIENT_TOKEN = "client_token";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;
    private ExecutorService executor;
    private String publishableKey;
    private String clerkDomain;
    private String clientToken;
    private String sessionToken;
    private JSONObject currentUser;

    @Override
    public void load() {
        super.load();
        client = new OkHttpClient();
        executor = Executors.newSingleThreadExecutor();
        
        // Load saved tokens
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        clientToken = prefs.getString(PREF_CLIENT_TOKEN, null);
        sessionToken = prefs.getString(PREF_SESSION_TOKEN, null);
    }

    private void saveTokens() {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_CLIENT_TOKEN, clientToken);
        editor.putString(PREF_SESSION_TOKEN, sessionToken);
        editor.apply();
    }

    private void clearTokens() {
        clientToken = null;
        sessionToken = null;
        currentUser = null;
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private String getClerkApiUrl(String path) {
        return "https://" + clerkDomain + path;
    }

    private Request.Builder createRequestBuilder(String url) {
        Request.Builder builder = new Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .header("Authorization", publishableKey)
            .header("Origin", "https://app.trainonapp.com")  // Spoof origin to match web
            .header("User-Agent", "Mozilla/5.0 (Linux; Android) ClerkNative/1.0");
        
        if (clientToken != null) {
            builder.header("Cookie", "__client=" + clientToken);
        }
        
        return builder;
    }

    @PluginMethod
    public void configure(PluginCall call) {
        publishableKey = call.getString("publishableKey");
        
        if (publishableKey == null || publishableKey.isEmpty()) {
            call.reject("Publishable key is required");
            return;
        }

        // Extract domain from publishable key (base64 decode)
        try {
            String decoded = new String(android.util.Base64.decode(
                publishableKey.replace("pk_live_", "").replace("pk_test_", ""),
                android.util.Base64.DEFAULT
            ));
            // Remove trailing $ if present
            clerkDomain = decoded.replace("$", "").trim();
            Log.d(TAG, "Configured with domain: " + clerkDomain);
        } catch (Exception e) {
            // Fallback to default domain
            clerkDomain = "clerk.trainonapp.com";
            Log.w(TAG, "Could not decode domain from key, using default: " + clerkDomain);
        }

        call.resolve();
    }

    @PluginMethod
    public void load(PluginCall call) {
        executor.execute(() -> {
            try {
                // First, get or create a client
                if (clientToken == null) {
                    createClient(call);
                } else {
                    // Verify existing session
                    fetchClientAndUser(call);
                }
            } catch (Exception e) {
                Log.e(TAG, "Load error", e);
                call.reject("Failed to load: " + e.getMessage());
            }
        });
    }

    private void createClient(PluginCall call) throws IOException, JSONException {
        String url = getClerkApiUrl("/v1/client?_clerk_js_version=5.117.0");
        
        Request request = createRequestBuilder(url)
            .post(RequestBody.create("{}", JSON))
            .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            Log.d(TAG, "Create client response: " + response.code());
            
            if (!response.isSuccessful()) {
                call.reject("Failed to create client: " + response.code() + " - " + body);
                return;
            }

            JSONObject json = new JSONObject(body);
            JSONObject responseObj = json.optJSONObject("response");
            
            if (responseObj != null) {
                clientToken = responseObj.optString("id", null);
                
                // Check for active session
                JSONArray sessions = responseObj.optJSONArray("sessions");
                if (sessions != null && sessions.length() > 0) {
                    JSONObject session = sessions.getJSONObject(0);
                    sessionToken = session.optString("last_active_token", null);
                    currentUser = session.optJSONObject("user");
                }
                
                saveTokens();
            }

            JSObject result = new JSObject();
            if (currentUser != null) {
                result.put("user", convertUser(currentUser));
            } else {
                result.put("user", JSObject.NULL);
            }
            call.resolve(result);
        }
    }

    private void fetchClientAndUser(PluginCall call) throws IOException, JSONException {
        String url = getClerkApiUrl("/v1/client?_clerk_js_version=5.117.0");
        
        Request request = createRequestBuilder(url)
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            Log.d(TAG, "Fetch client response: " + response.code());
            
            if (!response.isSuccessful()) {
                // Token might be invalid, clear and retry
                clearTokens();
                createClient(call);
                return;
            }

            JSONObject json = new JSONObject(body);
            JSONObject responseObj = json.optJSONObject("response");
            
            if (responseObj != null) {
                // Check for active session
                JSONArray sessions = responseObj.optJSONArray("sessions");
                if (sessions != null && sessions.length() > 0) {
                    JSONObject session = sessions.getJSONObject(0);
                    JSONObject lastToken = session.optJSONObject("last_active_token");
                    if (lastToken != null) {
                        sessionToken = lastToken.optString("jwt", null);
                    }
                    currentUser = session.optJSONObject("user");
                }
                
                saveTokens();
            }

            JSObject result = new JSObject();
            if (currentUser != null) {
                result.put("user", convertUser(currentUser));
            } else {
                result.put("user", JSObject.NULL);
            }
            call.resolve(result);
        }
    }

    @PluginMethod
    public void signInWithPassword(PluginCall call) {
        String email = call.getString("email");
        String password = call.getString("password");

        if (email == null || password == null) {
            call.reject("Email and password are required");
            return;
        }

        executor.execute(() -> {
            try {
                // Step 1: Create sign-in attempt
                String createUrl = getClerkApiUrl("/v1/client/sign_ins?_clerk_js_version=5.117.0");
                
                JSONObject createBody = new JSONObject();
                createBody.put("identifier", email);
                
                Request createRequest = createRequestBuilder(createUrl)
                    .post(RequestBody.create(createBody.toString(), JSON))
                    .build();

                String signInId;
                try (Response response = client.newCall(createRequest).execute()) {
                    String body = response.body().string();
                    Log.d(TAG, "Create sign-in response: " + response.code());
                    
                    if (!response.isSuccessful()) {
                        call.reject("Failed to start sign in: " + body);
                        return;
                    }

                    JSONObject json = new JSONObject(body);
                    JSONObject responseObj = json.optJSONObject("response");
                    signInId = responseObj.optString("id");
                    
                    // Update client token from response
                    JSONObject clientObj = json.optJSONObject("client");
                    if (clientObj != null) {
                        clientToken = clientObj.optString("id", clientToken);
                    }
                }

                // Step 2: Attempt with password
                String attemptUrl = getClerkApiUrl("/v1/client/sign_ins/" + signInId + "/attempt_first_factor?_clerk_js_version=5.117.0");
                
                JSONObject attemptBody = new JSONObject();
                attemptBody.put("strategy", "password");
                attemptBody.put("password", password);
                
                Request attemptRequest = createRequestBuilder(attemptUrl)
                    .post(RequestBody.create(attemptBody.toString(), JSON))
                    .build();

                try (Response response = client.newCall(attemptRequest).execute()) {
                    String body = response.body().string();
                    Log.d(TAG, "Attempt password response: " + response.code());
                    
                    if (!response.isSuccessful()) {
                        JSONObject errorJson = new JSONObject(body);
                        JSONArray errors = errorJson.optJSONArray("errors");
                        if (errors != null && errors.length() > 0) {
                            String message = errors.getJSONObject(0).optString("message", "Sign in failed");
                            call.reject(message);
                        } else {
                            call.reject("Sign in failed: " + body);
                        }
                        return;
                    }

                    JSONObject json = new JSONObject(body);
                    
                    // Extract session and user from response
                    JSONObject clientObj = json.optJSONObject("client");
                    if (clientObj != null) {
                        clientToken = clientObj.optString("id", clientToken);
                        
                        JSONArray sessions = clientObj.optJSONArray("sessions");
                        if (sessions != null && sessions.length() > 0) {
                            JSONObject session = sessions.getJSONObject(0);
                            JSONObject lastToken = session.optJSONObject("last_active_token");
                            if (lastToken != null) {
                                sessionToken = lastToken.optString("jwt", null);
                            }
                            currentUser = session.optJSONObject("user");
                        }
                    }
                    
                    saveTokens();

                    JSObject result = new JSObject();
                    if (currentUser != null) {
                        result.put("user", convertUser(currentUser));
                    } else {
                        result.put("user", JSObject.NULL);
                    }
                    call.resolve(result);
                }
            } catch (Exception e) {
                Log.e(TAG, "Sign in error", e);
                call.reject("Sign in failed: " + e.getMessage());
            }
        });
    }

    @PluginMethod
    public void getUser(PluginCall call) {
        JSObject result = new JSObject();
        if (currentUser != null) {
            try {
                result.put("user", convertUser(currentUser));
            } catch (JSONException e) {
                result.put("user", JSObject.NULL);
            }
        } else {
            result.put("user", JSObject.NULL);
        }
        call.resolve(result);
    }

    @PluginMethod
    public void getToken(PluginCall call) {
        JSObject result = new JSObject();
        result.put("token", sessionToken != null ? sessionToken : JSObject.NULL);
        call.resolve(result);
    }

    @PluginMethod
    public void signOut(PluginCall call) {
        if (clientToken == null) {
            clearTokens();
            call.resolve();
            return;
        }

        executor.execute(() -> {
            try {
                String url = getClerkApiUrl("/v1/client/sessions?_clerk_js_version=5.117.0");
                
                Request request = createRequestBuilder(url)
                    .delete()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    Log.d(TAG, "Sign out response: " + response.code());
                    // Clear tokens regardless of response
                    clearTokens();
                    call.resolve();
                }
            } catch (Exception e) {
                Log.e(TAG, "Sign out error", e);
                clearTokens();
                call.resolve(); // Still resolve since we cleared local state
            }
        });
    }

    @PluginMethod
    public void signInWithEmail(PluginCall call) {
        String email = call.getString("email");

        if (email == null) {
            call.reject("Email is required");
            return;
        }

        executor.execute(() -> {
            try {
                String createUrl = getClerkApiUrl("/v1/client/sign_ins?_clerk_js_version=5.117.0");
                
                JSONObject createBody = new JSONObject();
                createBody.put("identifier", email);
                
                Request request = createRequestBuilder(createUrl)
                    .post(RequestBody.create(createBody.toString(), JSON))
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    String body = response.body().string();
                    Log.d(TAG, "Sign in with email response: " + response.code());
                    
                    if (!response.isSuccessful()) {
                        call.reject("Failed to start sign in: " + body);
                        return;
                    }

                    // TODO: Prepare email code flow
                    // For now, return that code is required
                    JSObject result = new JSObject();
                    result.put("requiresCode", true);
                    call.resolve(result);
                }
            } catch (Exception e) {
                Log.e(TAG, "Sign in with email error", e);
                call.reject("Failed: " + e.getMessage());
            }
        });
    }

    @PluginMethod
    public void verifyEmailCode(PluginCall call) {
        // TODO: Implement email code verification
        call.reject("Email code verification not yet implemented on Android");
    }

    @PluginMethod
    public void signUp(PluginCall call) {
        String email = call.getString("emailAddress");
        String password = call.getString("password");
        String firstName = call.getString("firstName");
        String lastName = call.getString("lastName");

        if (email == null || password == null) {
            call.reject("Email and password are required");
            return;
        }

        executor.execute(() -> {
            try {
                String url = getClerkApiUrl("/v1/client/sign_ups?_clerk_js_version=5.117.0");
                
                JSONObject body = new JSONObject();
                body.put("email_address", email);
                body.put("password", password);
                if (firstName != null) body.put("first_name", firstName);
                if (lastName != null) body.put("last_name", lastName);
                
                Request request = createRequestBuilder(url)
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Sign up response: " + response.code());
                    
                    if (!response.isSuccessful()) {
                        JSONObject errorJson = new JSONObject(responseBody);
                        JSONArray errors = errorJson.optJSONArray("errors");
                        if (errors != null && errors.length() > 0) {
                            String message = errors.getJSONObject(0).optString("message", "Sign up failed");
                            call.reject(message);
                        } else {
                            call.reject("Sign up failed: " + responseBody);
                        }
                        return;
                    }

                    JSONObject json = new JSONObject(responseBody);
                    
                    // Check if email verification is required
                    JSONObject responseObj = json.optJSONObject("response");
                    boolean requiresVerification = false;
                    if (responseObj != null) {
                        JSONArray verifications = responseObj.optJSONArray("verifications");
                        if (verifications != null) {
                            requiresVerification = true;
                        }
                    }

                    JSObject result = new JSObject();
                    result.put("requiresVerification", requiresVerification);
                    if (currentUser != null) {
                        result.put("user", convertUser(currentUser));
                    } else {
                        result.put("user", JSObject.NULL);
                    }
                    call.resolve(result);
                }
            } catch (Exception e) {
                Log.e(TAG, "Sign up error", e);
                call.reject("Sign up failed: " + e.getMessage());
            }
        });
    }

    @PluginMethod
    public void verifySignUpEmail(PluginCall call) {
        call.reject("Sign up email verification not yet implemented on Android");
    }

    @PluginMethod
    public void updateUser(PluginCall call) {
        call.reject("Update user not yet implemented on Android");
    }

    @PluginMethod
    public void requestPasswordReset(PluginCall call) {
        call.reject("Password reset not yet implemented on Android");
    }

    @PluginMethod
    public void resetPassword(PluginCall call) {
        call.reject("Password reset not yet implemented on Android");
    }

    @PluginMethod
    public void refreshSession(PluginCall call) {
        call.reject("Refresh session not yet implemented on Android");
    }

    private JSObject convertUser(JSONObject clerkUser) throws JSONException {
        JSObject user = new JSObject();
        user.put("id", clerkUser.optString("id", null));
        user.put("firstName", clerkUser.optString("first_name", null));
        user.put("lastName", clerkUser.optString("last_name", null));
        user.put("imageUrl", clerkUser.optString("image_url", null));
        user.put("username", clerkUser.optString("username", null));
        
        // Get primary email
        JSONArray emailAddresses = clerkUser.optJSONArray("email_addresses");
        if (emailAddresses != null && emailAddresses.length() > 0) {
            String primaryEmailId = clerkUser.optString("primary_email_address_id", null);
            for (int i = 0; i < emailAddresses.length(); i++) {
                JSONObject emailObj = emailAddresses.getJSONObject(i);
                if (primaryEmailId != null && primaryEmailId.equals(emailObj.optString("id"))) {
                    user.put("emailAddress", emailObj.optString("email_address", null));
                    break;
                }
            }
            // Fallback to first email if no primary found
            if (user.optString("emailAddress", null) == null) {
                user.put("emailAddress", emailAddresses.getJSONObject(0).optString("email_address", null));
            }
        }
        
        return user;
    }
}

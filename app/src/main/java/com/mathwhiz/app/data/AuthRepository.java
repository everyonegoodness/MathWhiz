package com.mathwhiz.app.data;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/** Teacher registration, login and logout (Firebase Authentication, email + password). Teachers only. */
public final class AuthRepository {
    private static final AuthRepository INSTANCE = new AuthRepository();

    public static AuthRepository get() { return INSTANCE; }

    private AuthRepository() {}

    private FirebaseAuth auth() { return FirebaseAuth.getInstance(); }

    public boolean isLoggedIn() { return auth().getCurrentUser() != null; }

    /** Uid of the signed-in teacher, or null. Every saved record carries this uid. */
    public String currentUid() {
        FirebaseUser u = auth().getCurrentUser();
        return u == null ? null : u.getUid();
    }

    public String currentName() {
        FirebaseUser u = auth().getCurrentUser();
        if (u == null) return "";
        if (u.getDisplayName() != null && !u.getDisplayName().isEmpty()) return u.getDisplayName();
        return u.getEmail() == null ? "" : u.getEmail();
    }

    public void logout() { auth().signOut(); }

    public void login(String email, String password, final DataCallback<Void> cb) {
        auth().signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) cb.onSuccess(null);
            else cb.onError(friendly(task.getException()));
        });
    }

    public void register(final String name, final String email, String password, final DataCallback<Void> cb) {
        auth().createUserWithEmailAndPassword(email.trim(), password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                cb.onError(friendly(task.getException()));
                return;
            }
            FirebaseUser user = auth().getCurrentUser();
            if (user == null) {
                cb.onError("Account created, but sign-in failed. Please log in.");
                return;
            }
            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build());

            Map<String, Object> profile = new HashMap<>();
            profile.put("name", name.trim());
            profile.put("email", email.trim());
            profile.put("createdAt", System.currentTimeMillis());
            FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).setValue(profile)
                    .addOnCompleteListener(t2 -> {
                        if (t2.isSuccessful()) cb.onSuccess(null);
                        else cb.onError("Account created, but saving your profile failed: " + friendly(t2.getException()));
                    });
        });
    }

    /** Turns Firebase exceptions into messages a teacher can act on. */
    static String friendly(Exception e) {
        if (e instanceof FirebaseAuthWeakPasswordException) return "That password is too weak. Use at least 6 characters.";
        if (e instanceof FirebaseAuthUserCollisionException) return "An account with this email already exists. Try logging in.";
        if (e instanceof FirebaseAuthInvalidUserException || e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Wrong email or password. Please try again.";
        }
        if (e instanceof FirebaseNetworkException) return "No internet connection. Check the Wi-Fi and try again.";
        if (e != null && e.getMessage() != null) return e.getMessage();
        return "Something went wrong. Please try again.";
    }
}

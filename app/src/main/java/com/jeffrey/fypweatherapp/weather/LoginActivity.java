package com.jeffrey.fypweatherapp.weather;

import static com.google.android.gms.auth.api.signin.GoogleSignIn.getClient;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.jeffrey.fypweatherapp.R;

import org.jetbrains.annotations.Nullable;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText emailEditText, passwordEditText;
    private Button loginButton, signUpButton, btnGoogle;
    private TextView signUpTextview;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hiding the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance();

        // Initialize Views
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        signUpTextview = findViewById(R.id.sign_up_textview);
        btnGoogle = findViewById(R.id.btnGoogle);

        // Initialize Google SignIn
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = getClient(this, gso);


        // Setup click listener for the login button
        loginButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email is required");
            }
            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password is required");
            } else {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = auth.getCurrentUser();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

//        if (auth.getCurrentUser() != null) {
//            // User is already signed in, start the Main Activity
//            startActivity(new Intent(LoginActivity.this, MainActivity.class));
//            finish(); // close the Login Activity
//        }

        signUpTextview.setOnClickListener(view -> {
            // Go to signup activity
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });

        btnGoogle.setOnClickListener(v -> login());
    }

    private void login() {
        Intent LoginIntent = new Intent(mGoogleSignInClient.getSignInIntent());
        startActivityForResult(LoginIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this ,"Google sign in failed", Toast.LENGTH_LONG).show();
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        new AlertDialog.Builder(this)
//                .setTitle("Exit App")
//                .setMessage("Are you sure you want to exit?")
//                .setPositiveButton("Yes", (dialog, which) -> {
//                    finishAffinity();
//                    System.exit(0);
//                })
//                .setNegativeButton("No", null)
//                .show();
//    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("TAG", "signInWithCredential:success ");
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        Toast.makeText(LoginActivity.this, "Login with Google successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w("TAG", "LoginWithCredential:failure", task.getException());
                    }
                });
    }

}
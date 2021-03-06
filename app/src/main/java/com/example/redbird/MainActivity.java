package com.example.redbird;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.File;
import java.util.ArrayList;

import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    EditText rUser;
    EditText rPass;
    TextView error;
    TextView passwordError;
    ToggleButton registerSwitch;
    ToggleButton loginSwitch;
    Button registerButton;
    Button loginButton;
    Context context;
    File directory;
    File accountRepo;
    File user;
    String personEmail;
    String personId;
    private long mLastClickTime = 0;
    private long rLastClickTime = 0;
    private static SecretKeySpec skeySpec;
    private static byte[] key;
    private static TextView et;
    static GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rUser = findViewById(R.id.rUser);
        rPass = findViewById(R.id.rPass);
        registerSwitch = (ToggleButton) findViewById(R.id.registerSwitch);
        registerSwitch.setBackgroundColor(Color.parseColor("#FF1E1C1C"));//initially sets background to gray or enabled
        loginSwitch = (ToggleButton) findViewById(R.id.loginSwitch);
        loginSwitch.setBackgroundColor(Color.parseColor("#0C0C0C"));//initially sets background to black or disabled
        registerButton = (Button) findViewById(R.id.registerButton);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setVisibility(View.INVISIBLE);
        error = findViewById(R.id.errorMessage);
        error.setVisibility(View.INVISIBLE);
        et = findViewById(R.id.errorMessage);//this is a static version of error message
        passwordError = findViewById(R.id.passwordError);
        passwordError.setVisibility(View.INVISIBLE);
        //Internal File storage
        context = getApplicationContext();
        directory = context.getFilesDir();//the root directory (files)
        System.out.println(directory); ///data/user/0/com.example.redbirdpasswordmanager/files
        accountRepo = new File(directory, "Account");//Create Account directory
        if (!accountRepo.exists()) { //checks if it exits or not
            if (accountRepo.mkdir()) { //creates it if it does not

            }
        }
        System.out.println(context.getDir("Account", 0));//Test to see if Account dir was created

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseUser currentUser = mAuth.getCurrentUser();

    }

    private void updateUI(FirebaseUser currentUser) {
        FirebaseUser acct = currentUser;
        if (acct != null) {
            personEmail = acct.getEmail();
            SignIn test = new SignIn(personEmail);
            System.out.println(acct.getEmail());
            //Go to next transition Activity
            Intent intent = new Intent(this, TransitionActivity.class);
            intent.putExtra("username", personEmail.toLowerCase());
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            // ...
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }
//    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
//        try {
//            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
//
//            // Signed in successfully, show authenticated UI.
//            System.out.println(account.getEmail());
//            updateUI(account);
//
//        } catch (ApiException e) {
//            // The ApiException status code indicates the detailed failure reason.
//            // Please refer to the GoogleSignInStatusCodes class reference for more information.
//            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//           // updateUI(null);
//        }
//    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            System.out.println(user.getEmail());
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            //   Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //  updateUI(null);
                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void updateUI(GoogleSignInAccount account) {//come back to this it keeps skipping the signin
        GoogleSignInAccount acct = account;
        if (acct != null) {
            personEmail = acct.getEmail();
            personEmail = personEmail.replace(".", "-");

            SignIn test = new SignIn(personEmail);

            //Go to next transition Activity
            Intent intent = new Intent(this, TransitionActivity.class);
            intent.putExtra("username", personEmail.toLowerCase().replace(".", "-"));
            startActivity(intent);
        }
    }

    public void changeToLogin(View view) {
        registerSwitch.setBackgroundColor(Color.parseColor("#0C0C0C"));
        registerButton.setVisibility(view.INVISIBLE);
        loginButton.setVisibility(view.VISIBLE);
        loginSwitch.setBackgroundColor(Color.parseColor("#FF1E1C1C"));

    }


    public void changeToRegister(View view) {
        registerSwitch.setBackgroundColor(Color.parseColor("#FF1E1C1C"));
        registerButton.setVisibility(view.VISIBLE);
        loginButton.setVisibility(view.INVISIBLE);
        loginSwitch.setBackgroundColor(Color.parseColor("#0C0C0C"));

    }


    public void register(View view) throws Exception { // New Firebase user creation
        passwordError.setVisibility(View.INVISIBLE);
        error.setVisibility(View.INVISIBLE);//sets warning to invisible
        String username = rUser.getText().toString();//gets username from Edit text
        String password = rPass.getText().toString();//gets passworkd from Edit text
        if (!username.isEmpty()) {
            System.out.println("Username field is empty");
            if (minimumPassword(password)) {

                mAuth.createUserWithEmailAndPassword(username, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
                                    //  updateUI(null);
                                }


                            }
                        });
            } else {
                passwordError.setVisibility(View.VISIBLE);
            }
        }
    }

    public void login(View view) throws Exception {

        passwordError.setVisibility(View.INVISIBLE);
        error.setVisibility(View.INVISIBLE);//sets warning to invisible
        String password = rPass.getText().toString();//gets passworkd from Edit text
        String username = rUser.getText().toString();//gets username from Edit text


        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
//                            Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);

                        }


                    }
                });

    }

    public boolean minimumPassword(String pass) {
        ArrayList<Character> chars = new ArrayList<Character>();
        Boolean capital = false;
        Boolean lowercase = false;
        Boolean number = false;
        Boolean notEnoughChars = false;
        if (pass.length() > 7) {
            for (int i = 0; i < pass.length(); i++) {
                if (Character.isUpperCase(pass.charAt(i))) {
                    capital = true;
                }
                if (Character.isLowerCase(pass.charAt(i))) {
                    lowercase = true;
                }
                if (Character.isDigit(pass.charAt(i))) {
                    number = true;
                }
            }
            if (capital && lowercase && number) {
                System.out.println("Password verification passed");
                return true;
            }
        }
        System.out.println("Password verification failed");

        return false;

    }


    private boolean shouldAllowBack() {
        return false;
    }

    @Override
    public void onBackPressed() {
        if (shouldAllowBack()) {
            super.onBackPressed();
        } else {

        }
    }
}
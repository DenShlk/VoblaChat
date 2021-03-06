package com.hyperspere.voblachat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class StartActivity extends AppCompatActivity {
	private static final String TAG = "DEBUG_START_ACTIVITY";

	private FirebaseAuth mAuth;
	private DatabaseReference reference;

	private EditText emailET, passwordET;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		emailET = findViewById(R.id.email_et);
		passwordET = findViewById(R.id.password_et);

		// Initialize Firebase Auth
		mAuth = FirebaseAuth.getInstance();
		//mAuth.logout();


		mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

				FirebaseUser user = firebaseAuth.getCurrentUser();

				if(user!=null) {
					Intent intent = new Intent(StartActivity.this, ChatsListActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					finish();
				}
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		// Check if user is signed in (non-null) and update UI accordingly.
		FirebaseUser user = mAuth.getCurrentUser();

		if(user!=null){
			Intent intent = new Intent(StartActivity.this, ChatsListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
		}
	}

	public void onRegisterClick(View v) {
		if (checkInputs())
			mAuth.createUserWithEmailAndPassword(emailET.getText().toString(), passwordET.getText().toString())
					.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {
							if (task.isSuccessful()) {
								FirebaseUser user = mAuth.getCurrentUser();

								reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

								HashMap<String, String> hashMap = new HashMap<>();
								hashMap.put("id", user.getUid());
								hashMap.put("username", user.getEmail().substring(0, user.getEmail().indexOf("@")));

								reference.setValue(hashMap);
							} else {
								Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
								// If sign in fails, display a message to the user.
								Log.w(TAG, "createUserWithEmail:failure", task.getException());
							}

							// ...
						}
					});
	}

	public void onLoginClick(View v) {
		if (checkInputs())
			mAuth.signInWithEmailAndPassword(emailET.getText().toString(), passwordET.getText().toString())
					.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
						@Override
						public void onComplete(@NonNull Task<AuthResult> task) {
							if (task.isSuccessful()) {
								// Sign in success, update UI with the signed-in user's information
								Log.d(TAG, "signInWithEmail:success");

							} else {
								// If sign in fails, display a message to the user.
								Log.w(TAG, "signInWithEmail:failure", task.getException());

								Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
							}

							// ...
						}
					});
	}

	private boolean checkInputs(){
		String email = emailET.getText().toString();
		String password = passwordET.getText().toString();
		if(email.matches("[a-zA-Z]+.*@.{2,}\\..{2,}")){
			if(password.length() < 6) {
				Toast.makeText(StartActivity.this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
				return false;
			}else{
				return true;
			}
		}
		else{
			Toast.makeText(StartActivity.this, "E-mail incorrect!", Toast.LENGTH_SHORT).show();
			return false;
		}
	}
}

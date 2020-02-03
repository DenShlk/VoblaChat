package com.hyperspere.voblachat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hyperspere.voblachat.Adapter.MessageAdapter;
import com.hyperspere.voblachat.Model.Message;
import com.hyperspere.voblachat.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "DEBUG_MAIN_ACTIVITY";

	private FirebaseAuth mAuth;
	private DatabaseReference reference;
	private FirebaseUser fuser;
	private User user;

	private TextView usernameTV;

	private EditText messageET;
	private Button sendButton;
	private RecyclerView chatRecycle;

	private MessageAdapter messageAdapter;
	private List<Message> messages;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		usernameTV = findViewById(R.id.username_tv2);
		messageET = findViewById(R.id.message_et);
		sendButton = findViewById(R.id.send_button);
		chatRecycle = findViewById(R.id.chat_recycle);

		chatRecycle.setHasFixedSize(true);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
		linearLayoutManager.setStackFromEnd(true);
		chatRecycle.setLayoutManager(linearLayoutManager);

		mAuth = FirebaseAuth.getInstance();

		if(mAuth==null)
			logout();

		fuser = mAuth.getCurrentUser();
		reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				user = dataSnapshot.getValue(User.class);
				usernameTV.setText(user.getUsername());
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		readMessages(fuser.getUid(), "All");
	}

	public void onSendClick(View v){
		if(!messageET.getText().toString().isEmpty()){
			sendMessage(user.getUsername(), "All", messageET.getText().toString());
			messageET.setText("");
		}else
			Toast.makeText(MainActivity.this, "Message is empty!", Toast.LENGTH_SHORT);
	}

	private void sendMessage(String sender, String receiver, String message){
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("sender", sender);
		hashMap.put("receiver", receiver);
		hashMap.put("message", message);

		reference.child("Chats").push().setValue(hashMap);
	}

	public void onLogoutClick(View view){
		logout();
	}

	void logout(){
		if(mAuth!=null){
			mAuth.signOut();
		}

		Intent intent = new Intent(MainActivity.this, StartActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

	private void readMessages(String myId, String chatId){
		messages = new ArrayList<>();

		reference = FirebaseDatabase.getInstance().getReference("Chats");
		messages.clear();
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				messages.clear();

				for(DataSnapshot snapshot: dataSnapshot.getChildren()){
					Message message = snapshot.getValue(Message.class);

					messages.add(message);
				}
				messageAdapter = new MessageAdapter(getApplicationContext(), messages, user.getUsername());
				chatRecycle.setAdapter(messageAdapter);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}
}

package com.hyperspere.voblachat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hyperspere.voblachat.Adapter.UserAdapter;
import com.hyperspere.voblachat.Model.Message;
import com.hyperspere.voblachat.Model.User;

import java.util.ArrayList;
import java.util.List;

public class ChatAddActivity extends AppCompatActivity {

	private FirebaseAuth mAuth;
	private FirebaseUser fuser;
	private User user;

	private TextView usernameTV;
	private RecyclerView usersRecycle;
	private EditText chatName;
	UserAdapter userAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_add);

		usernameTV = findViewById(R.id.username_tv);
		usersRecycle = findViewById(R.id.users_recycle);
		chatName = findViewById(R.id.chat_name_et);

		mAuth = FirebaseAuth.getInstance();

		if(mAuth==null)
			logout();

		fuser = mAuth.getCurrentUser();
		if(fuser==null)
			logout();

		DatabaseReference myReference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

		myReference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				user = dataSnapshot.getValue(User.class);
				usernameTV.setText(user.getUsername());
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("Users");

		usersRecycle.setHasFixedSize(true);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
		linearLayoutManager.setStackFromEnd(false);
		usersRecycle.setLayoutManager(linearLayoutManager);

		usersReference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				List<User> users = new ArrayList<>();
				for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
					User mUser = snapshot.getValue(User.class);
					if(!user.getId().equals(mUser.getId()))
						users.add(mUser);
				}
				userAdapter = new UserAdapter(ChatAddActivity.this, users);
				usersRecycle.setAdapter(userAdapter);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	public void confirmChatClick(View v){
		if(userAdapter!=null && chatName.getText().length()>0) {
			List<User> toAdd = userAdapter.checked;
			toAdd.add(user);

			DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chats").push();
			chatRef.child("Name").setValue(chatName.getText().toString());



			DatabaseReference membersRef = chatRef.child("Members");
			for(User mUser : toAdd){
				membersRef.push().setValue(mUser.getId());

				DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getId());
				userRef.child("MyChats").push().setValue(chatRef.getKey());
			}

			DatabaseReference messagesRef = chatRef.child("Messages");
			messagesRef.push().setValue(new Message(user.getUsername(), chatName.getText().toString(),
					"Chat created by " + user.getUsername(), false, false));

			finish();
		}
	}

	public void exitClick(View v){
		finish();
	}

	void logout(){
		if(mAuth!=null){
			mAuth.signOut();
		}

		Intent intent = new Intent(ChatAddActivity.this, StartActivity.class);
		startActivity(intent);

		mAuth = FirebaseAuth.getInstance();

		if(mAuth==null)
			finish();

		fuser = mAuth.getCurrentUser();

		if(fuser==null)
			finish();
	}
}

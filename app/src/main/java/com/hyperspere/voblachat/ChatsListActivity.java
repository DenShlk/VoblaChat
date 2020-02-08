package com.hyperspere.voblachat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.hyperspere.voblachat.Adapter.ChatAdapter;
import com.hyperspere.voblachat.Model.Chat;
import com.hyperspere.voblachat.Model.User;

import java.util.ArrayList;
import java.util.List;

public class ChatsListActivity extends AppCompatActivity {
	
	private FirebaseAuth mAuth;
	private FirebaseUser fuser;
	private User user;

	private List<Chat> chats;

	private TextView usernameTV;

	private RecyclerView chatsRecycle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chats_list);

		usernameTV = findViewById(R.id.username_tv);
		chatsRecycle = findViewById(R.id.messages_recycle);

		chatsRecycle.setHasFixedSize(true);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
		linearLayoutManager.setStackFromEnd(false);
		chatsRecycle.setLayoutManager(linearLayoutManager);

		mAuth = FirebaseAuth.getInstance();

		if(mAuth==null)
			logout();

		fuser = mAuth.getCurrentUser();
		if(fuser==null)
			logout();

		DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

		myRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				user = dataSnapshot.getValue(User.class);
				if(user!=null)
					usernameTV.setText(user.getUsername());
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		DatabaseReference myChatsRef = myRef.child("MyChats");
		chats = new ArrayList<>();
		myChatsRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				chats = new ArrayList<>();
				final DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("Chats");
				final long myChatsCount = dataSnapshot.getChildrenCount();
				for(DataSnapshot snapshot : dataSnapshot.getChildren()){
					String chatId = snapshot.getValue(String.class);
					chatsRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							chats.add(new Chat(dataSnapshot));
							if(chats.size() == myChatsCount) {
								ChatAdapter chatAdapter = new ChatAdapter(ChatsListActivity.this, chats, user);
								chatsRecycle.setAdapter(chatAdapter);
							}
						}

						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {

						}
					});
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	public void onLogoutClick(View view){
		logout();
	}

	void logout(){
		if(mAuth!=null){
			mAuth.signOut();
		}

		Intent intent = new Intent(ChatsListActivity.this, StartActivity.class);
		startActivity(intent);

		mAuth = FirebaseAuth.getInstance();

		if(mAuth!=null)
			fuser = mAuth.getCurrentUser();
	}

	public void addChatClick(View v){
		Intent intent = new Intent(ChatsListActivity.this, ChatAddActivity.class);
		startActivity(intent);
	}
}

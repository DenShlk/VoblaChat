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
import java.util.HashMap;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class ChatsListActivity extends AppCompatActivity {
	
	private FirebaseAuth mAuth;
	private FirebaseUser fuser;
	private User user;

	private List<Chat> chats;

	private TextView usernameTV;
	private GifImageView loadingGif;
	private RecyclerView chatsRecycle;

	private long lastFill;

	@Override
	protected void onStart() {
		super.onStart();
		fillRecycle();
	}

	@Override
	protected void onResume() {
		super.onResume();
		fillRecycle();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chats_list);

		usernameTV = findViewById(R.id.username_tv);
		chatsRecycle = findViewById(R.id.messages_recycle);
		loadingGif = findViewById(R.id.loading_gif);

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

		fillRecycle();
	}

	void fillRecycle(){
		if(System.currentTimeMillis() - lastFill < 1000)
			return;// что бы во всяких onStart не вызывалось
		lastFill = System.currentTimeMillis();

		final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

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

		DatabaseReference myChatsNamesRef = myRef.child("MyChatsNames");
		myChatsNamesRef.addListenerForSingleValueEvent(new ValueEventListener() {

			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				final HashMap<String, String> myChatNames = new HashMap<>();
				for (DataSnapshot snapshot : dataSnapshot.getChildren()){
					myChatNames.put(snapshot.getKey(), snapshot.getValue(String.class));
				}

				DatabaseReference myChatsRef = myRef.child("MyChats");
				chats = new ArrayList<>();
				myChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						chats = new ArrayList<>();
						final DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("Chats");
						final long myChatsCount = dataSnapshot.getChildrenCount();
						for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
							final String chatId = snapshot.getValue(String.class);
							chatsRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
								@Override
								public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
									chats.add(new Chat(dataSnapshot));
									chats.get(chats.size() - 1).setName(myChatNames.get(dataSnapshot.getKey()));
									if (chats.size() == myChatsCount) {
										ChatAdapter chatAdapter = new ChatAdapter(ChatsListActivity.this, chats, user);
										chatsRecycle.setAdapter(chatAdapter);

										loadingComplete();
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

	void loadingComplete(){
		loadingGif.setVisibility(View.INVISIBLE);
		loadingGif.setEnabled(false);
		loadingGif.setFocusable(false);
	}
}

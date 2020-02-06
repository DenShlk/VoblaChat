package com.hyperspere.voblachat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hyperspere.voblachat.Adapter.MessageAdapter;
import com.hyperspere.voblachat.Model.Chat;
import com.hyperspere.voblachat.Model.Message;
import com.hyperspere.voblachat.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessagingActivity extends AppCompatActivity {
	static private final String CHANNEL_ID = "Vobla chat new messages";

	private static final String TAG = "DEBUG_MAIN_ACTIVITY";

	private FirebaseAnalytics mFirebaseAnalytics;
	private FirebaseAuth mAuth;
	private FirebaseUser fuser;
	private DatabaseReference chatReference;
	private User user;
	private Chat chat;

	private TextView chatnameTV;

	private EditText messageET;
	private RecyclerView messagesRecycle;

	private MessageAdapter messageAdapter;
	private List<Message> messages;

	private ServiceConnection serviceConnection;
	private boolean connected = false;


	@Override
	protected void onPause() {
		super.onPause();
		if(connected)
			unbindService(serviceConnection);
		connected = false;
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(connected)
			unbindService(serviceConnection);
		connected = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(connected)
			unbindService(serviceConnection);
		connected = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(!connected)
			bindService(new Intent(getApplicationContext(), MessageCheckService.class), serviceConnection, BIND_IMPORTANT);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if(!connected)
			bindService(new Intent(getApplicationContext(), MessageCheckService.class), serviceConnection, BIND_IMPORTANT);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_messaging);

		createNotificationChannel();

		serviceConnection = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				connected = true;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				connected = false;
			}
		};

		startService(new Intent(getApplicationContext(), MessageCheckService.class));

		if(!connected)
			bindService(new Intent(getApplicationContext(), MessageCheckService.class), serviceConnection, BIND_AUTO_CREATE);

		chatnameTV = findViewById(R.id.chatname_tv);
		messageET = findViewById(R.id.message_et);
		messagesRecycle = findViewById(R.id.messages_recycle);

		messagesRecycle.setHasFixedSize(true);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
		linearLayoutManager.setStackFromEnd(true);
		messagesRecycle.setLayoutManager(linearLayoutManager);

		mAuth = FirebaseAuth.getInstance();

		if(mAuth==null)
			logout();

		// Obtain the FirebaseAnalytics instance.
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

		fuser = mAuth.getCurrentUser();
		if(fuser==null)
			logout();

		DatabaseReference myReference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

		myReference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				user = dataSnapshot.getValue(User.class);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		final String chatId = getIntent().getStringExtra("ChatId");
		final NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		chatReference = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);

		chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				chat = new Chat(dataSnapshot);
				notificationManager.cancel(chat.getName().hashCode());

				if(chat.getName().length() > 15)
					chatnameTV.setText(chat.getName().substring(0, 15) + "...");
				else
					chatnameTV.setText(chat.getName());

				readMessages();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	public void onSendClick(View v){
		if(!messageET.getText().toString().isEmpty() && chat!=null){
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, new Bundle());

			sendMessage(user.getUsername(), chat.getName(), messageET.getText().toString());
			messageET.setText("");
		}//else
			//Toast.makeText(MessagingActivity.this, "Message is empty!", Toast.LENGTH_SHORT).show();
	}

	private void sendMessage(String sender, String chat, String message){

		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("sender", sender);
		hashMap.put("chat", chat);
		hashMap.put("message", message);
		hashMap.put("viewed", false);// TODO: 06.02.2020
		hashMap.put("delivered", false);

		chatReference.child("Messages").push().setValue(hashMap);
	}

	public void exitClick(View v){
		finish();
	}

	void logout(){
		if(mAuth!=null){
			mAuth.signOut();
		}

		Intent intent = new Intent(MessagingActivity.this, StartActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

	private void readMessages(){
		messages = new ArrayList<>();

		messages.clear();
		chatReference.child("Messages").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (user != null) {
					messages.clear();

					long count = dataSnapshot.getChildrenCount();

					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						if(count-- < 100) {
							Message message = snapshot.getValue(Message.class);

							messages.add(message);
						}
					}
					messageAdapter = new MessageAdapter(getApplicationContext(), messages, user.getUsername());
					messagesRecycle.setAdapter(messageAdapter);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}

	private void createNotificationChannel() {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "Vobla chat messages";
			String description = "New messages!!!";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}
}

package com.hyperspere.voblachat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
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
import com.hyperspere.voblachat.Model.Message;
import com.hyperspere.voblachat.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	static private final String CHANNEL_ID = "Vobla chat new messages";

	private static final String TAG = "DEBUG_MAIN_ACTIVITY";

	private FirebaseAnalytics mFirebaseAnalytics;
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
			bindService(new Intent(getApplicationContext(), NewMessageChecker.class), serviceConnection, BIND_IMPORTANT);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if(!connected)
			bindService(new Intent(getApplicationContext(), NewMessageChecker.class), serviceConnection, BIND_IMPORTANT);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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

		startService(new Intent(getApplicationContext(), NewMessageChecker.class));

		if(!connected)
			bindService(new Intent(getApplicationContext(), NewMessageChecker.class), serviceConnection, BIND_AUTO_CREATE);

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

		// Obtain the FirebaseAnalytics instance.
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, new Bundle());

			sendMessage(user.getUsername(), "All", messageET.getText().toString());
			messageET.setText("");
		}//else
			//Toast.makeText(MainActivity.this, "Message is empty!", Toast.LENGTH_SHORT).show();
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
					chatRecycle.setAdapter(messageAdapter);
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

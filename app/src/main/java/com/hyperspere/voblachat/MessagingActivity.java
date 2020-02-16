package com.hyperspere.voblachat;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hyperspere.voblachat.Adapter.MessageAdapter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.hyperspere.voblachat.Model.*;

import pl.droidsonroids.gif.GifImageView;

public class MessagingActivity extends AppCompatActivity {
	static private final String CHANNEL_ID = "Vobla chat new messages";

	private static final String TAG = "DEBUG_MAIN_ACTIVITY";
	private static final int PICK_IMAGE_REQUEST = 1;

	private FirebaseAnalytics mFirebaseAnalytics;
	private FirebaseAuth mAuth;
	private FirebaseUser fuser;
	private DatabaseReference chatReference;

	private StorageReference storageRef;

	private User user;
	private Chat chat;

	private TextView chatnameTV;

	private EditText messageET;
	private ImageButton photoButton;
	private RecyclerView messagesRecycle;
	private GifImageView loadingGif;

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

		connected = isServiceRunning(MessageCheckService.class);
		if(!connected) {
			startService(new Intent(getApplicationContext(), MessageCheckService.class));
		}
		connected = bindService(new Intent(getApplicationContext(), MessageCheckService.class), serviceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onStart() {
		super.onStart();

		connected = isServiceRunning(MessageCheckService.class);
		if(!connected) {
			startService(new Intent(getApplicationContext(), MessageCheckService.class));
		}
		connected = bindService(new Intent(getApplicationContext(), MessageCheckService.class), serviceConnection, BIND_AUTO_CREATE);
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

		connected = isServiceRunning(MessageCheckService.class);
		if(!connected) {
			startService(new Intent(getApplicationContext(), MessageCheckService.class));
		}
		connected = bindService(new Intent(getApplicationContext(), MessageCheckService.class), serviceConnection, BIND_AUTO_CREATE);

		loadingGif = findViewById(R.id.loading_gif);
		chatnameTV = findViewById(R.id.chatname_tv);
		messageET = findViewById(R.id.message_et);
		photoButton = findViewById(R.id.photo_button);
		messagesRecycle = findViewById(R.id.messages_recycle);

		messageET.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(messageET.getText().length()>0){
					photoButton.setVisibility(View.INVISIBLE);
				}else{
					photoButton.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {}
		});

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

		storageRef = FirebaseStorage.getInstance().getReference();

		DatabaseReference myReference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
		final String chatId = getIntent().getStringExtra("ChatId");

		myReference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				user = dataSnapshot.getValue(User.class);

				//это шиндец 1
				if(chat==null)
					chat = new Chat();
				chat.setName( dataSnapshot.child("MyChatsNames").child(chatId).getValue(String.class));

				if(chat.getName().length() > 15)
					chatnameTV.setText(chat.getName().substring(0, 15) + "...");
				else
					chatnameTV.setText(chat.getName());
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {	}
		});

		final NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		chatReference = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);

		chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				//это шиндец 2
				String chatName = null;
				if(chat!=null)
					chatName = chat.getName();
				chat = new Chat(dataSnapshot);
				if(chatName!=null)
					chat.setName(chatName);

				notificationManager.cancel(chat.getName().hashCode());

				DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getId());
				DatabaseReference myChatsNamesRef = myRef.child("MyChatsNames");


				readMessages();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private boolean isServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public void onSendClick(View v){
		if(!messageET.getText().toString().isEmpty() && chat!=null){
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, new Bundle());

			sendMessage(messageET.getText().toString());
			messageET.setText("");
		}//else
			//Toast.makeText(MessagingActivity.this, "Message is empty!", Toast.LENGTH_SHORT).show();
	}

	public void photoClick(View v){
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, PICK_IMAGE_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Log.d("lol", String.valueOf(requestCode));
		//Log.d("lol", String.valueOf(resultCode));

		if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
		&& data != null && data.getData()!=null) {
			Uri filePath = data.getData();
			try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

				sendMessage(bitmap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendMessage(String message){

		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("sender", user.getUsername());
		hashMap.put("chat", chat.getName());
		hashMap.put("message", message);
		hashMap.put("type", Message.MESSAGE_TYPE_TEXT);
		hashMap.put("viewed", false);// TODO: 06.02.2020
		hashMap.put("delivered", false);

		chatReference.child("Messages").push().setValue(hashMap);
		chatReference.child("lastMessage").setValue(hashMap);
	}

	private void sendMessage(Bitmap image){

		final HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("sender", user.getUsername());
		hashMap.put("chat", chat.getName());
		hashMap.put("type", Message.MESSAGE_TYPE_PHOTO);
		hashMap.put("viewed", false);// TODO: 06.02.2020
		hashMap.put("delivered", false);

		String imagePath = "Images/" + UUID.randomUUID();
		hashMap.put("imagePath", imagePath);

		try {
			final String fileName = imagePath.substring(imagePath.indexOf("/") + 1) + ".jpg";
			OutputStream outStream = getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
			image.compress(Bitmap.CompressFormat.PNG, 25, outStream);
			outStream.close();

			File file = new File(getFilesDir(), fileName);

			StorageReference ref = storageRef.child(imagePath);
			ref.putFile(android.net.Uri.fromFile(file)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
					chatReference.child("Messages").push().setValue(hashMap);
					chatReference.child("lastMessage").setValue(hashMap);
				}
			});
			// TODO: 16.02.2020 Tint!! 
		} catch (IOException e) {
			e.printStackTrace();
		}

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
		messageAdapter = new MessageAdapter(getApplicationContext(), user.getUsername());
		messagesRecycle.setAdapter(messageAdapter);

		messages = messageAdapter.getMessages();

		chatReference.child("Messages").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (user != null) {
					//messages.clear();

					long count = dataSnapshot.getChildrenCount();
					int index = 0;
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

						Message message = snapshot.getValue(Message.class);

						if (index < messages.size()) {
							if (!message.compare(messages.get(index))) {
								messages.set(index, message);
								messageAdapter.notifyItemChanged(index);
							}
						} else {
							messages.add(message);
							messageAdapter.notifyItemInserted(index);
						}

						index++;

					}

					messagesRecycle.scrollToPosition(messages.size() - 1);
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

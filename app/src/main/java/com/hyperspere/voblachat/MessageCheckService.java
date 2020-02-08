package com.hyperspere.voblachat;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hyperspere.voblachat.Model.Message;

public class MessageCheckService extends Service {

	static private final String CHANNEL_ID = "Vobla chat new messages";

	private final IBinder mBinder = new Binder();

	private boolean running = false;

	private FirebaseUser fuser;

	@Override
	public void unbindService(ServiceConnection conn) {
		super.unbindService(conn);

		//Toast.makeText(this, "unbindService", Toast.LENGTH_SHORT).show();

		running = true;
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
		notificate(new Message("system","", "removed", false,false), "0");
	}

	@Override
	public void onCreate() {
		super.onCreate();

		running = true;
		work();
	}

	boolean authorisation(){
		FirebaseAuth mAuth = FirebaseAuth.getInstance();

		if(mAuth ==null){
			running = false;
			return false;
		}

		fuser = mAuth.getCurrentUser();
		if(fuser==null) {
			running = false;
			return false;
		}

		return true;
	}

	private void work() {

		if(authorisation()) {
			DatabaseReference myChatsRef = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid()).child("MyChats");
			myChatsRef.addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
						final String chatId = snapshot.getValue(String.class);
						final DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("Chats").child(chatId).child("Messages");

						messagesRef.addValueEventListener(new ValueEventListener() {
							long message_count = -1;

							@Override
							public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
								if (dataSnapshot.getChildrenCount() != message_count && message_count != -1) {
									DataSnapshot lastMessage = null;
									for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
										lastMessage = snapshot;
									}
									if (lastMessage != null)
										notificate(lastMessage.getValue(Message.class), chatId);
								}
								message_count = dataSnapshot.getChildrenCount();
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
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		running = false;

		Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();

		return Service.START_STICKY;
	}

	public MessageCheckService() {
	}

	@Override
	public boolean onUnbind(Intent intent) {
		//Toast.makeText(MessageCheckService.this, "unbind", Toast.LENGTH_SHORT).show();
		Log.d("unbind", "true");
		running = true;
		return true;
	}

	@Override
	public IBinder onBind(Intent intent) {
		//Toast.makeText(MessageCheckService.this, "bind", Toast.LENGTH_SHORT).show();

		Log.d("onbind", "false");
		running = false;
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);

		//Toast.makeText(MessageCheckService.this, "rebind", Toast.LENGTH_SHORT).show();
		Log.d("rebind", "false");


		running = false;
	}

	private void notificate(Message message, String chatId){
		if(!running)
			return;

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);


		mBuilder.setContentTitle(message.getChat())
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setCategory(NotificationCompat.CATEGORY_MESSAGE)
				.setContentText(message.getSender() + ":\n" + message.getMessage())
				.setSmallIcon(R.mipmap.vobla_logo);

		PendingIntent intent = PendingIntent.getActivity(
				MessageCheckService.this,
				0,
				new Intent(MessageCheckService.this, MessagingActivity.class).putExtra("ChatId", chatId),
				PendingIntent.FLAG_CANCEL_CURRENT);

		mBuilder.setContentIntent(intent);

		notificationManager.notify(message.getChat().hashCode(), mBuilder.build());
	}


}

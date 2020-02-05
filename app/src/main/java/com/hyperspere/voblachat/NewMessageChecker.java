package com.hyperspere.voblachat;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hyperspere.voblachat.Model.Message;

public class NewMessageChecker extends Service {

	static private final String CHANNEL_ID = "Vobla chat new messages";

	private final IBinder mBinder = new Binder();

	private boolean running = false;
	private boolean exitNow = true;

	@Override
	public void unbindService(ServiceConnection conn) {
		super.unbindService(conn);
		running = true;
		exitNow = true;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		running = true;
		work();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void work() {
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
		reference.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				//Toast.makeText(NewMessageChecker.this, "message" + exitNow, Toast.LENGTH_SHORT).show();
				if(exitNow){
					exitNow = false;
					return;
				}
				if (running) {
					Message message = null;
					for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
						message = snapshot.getValue(Message.class);
					}
					if (message != null) {
						notificate(message.getSender(), message.getMessage(), "All");
						// TODO: 05.02.2020
					}
				}

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		running = true;
		work();

		return Service.START_STICKY;
	}

	public NewMessageChecker() {
	}

	@Override
	public boolean onUnbind(Intent intent) {
		//Toast.makeText(NewMessageChecker.this, "unbind", Toast.LENGTH_SHORT).show();

		running = true;
		exitNow = true;
		work();
		return true;
	}

	@Override
	public IBinder onBind(Intent intent) {
		//Toast.makeText(NewMessageChecker.this, "bind", Toast.LENGTH_SHORT).show();

		running = false;
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);

		//Toast.makeText(NewMessageChecker.this, "rebind", Toast.LENGTH_SHORT).show();

		running = false;
	}

	private void notificate(String sender, String text, String chat){
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);


		mBuilder.setContentTitle("New message from " + sender + ":")
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setCategory(NotificationCompat.CATEGORY_MESSAGE)
				.setContentText(text)
				.setSmallIcon(R.mipmap.vobla_logo);

		notificationManager.notify(sender.hashCode(), mBuilder.build());
	}


}

package com.hyperspere.voblachat.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hyperspere.voblachat.Model.Message;
import com.hyperspere.voblachat.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

	private static final int MSG_TYPE_TEXT_MY = 1;
	private static final int MSG_TYPE_TEXT_YOUR = 2;
	private static final int MSG_TYPE_PHOTO_MY = 3;
	private static final int MSG_TYPE_PHOTO_YOUR = 4;

	private Context context;
	private List<Message> messages;
	private Map<String, Integer> user2color;
	private String username;

	private ArrayList<Integer> colors;
	private int colorIndex = 0;

	private StorageReference storageRef;

	public List<Message> getMessages() {
		return messages;
	}

	public MessageAdapter(Context context, String username){
		this.context = context;
		this.messages = new ArrayList<>();
		this.username = username;

		user2color = new HashMap<>();
		user2color.put(username, ContextCompat.getColor(context, R.color.colorMineMessage));

		colors = new ArrayList<>(Arrays.asList(
				Color.parseColor("#E1C340"),
				Color.parseColor("#FA26A0"),
				Color.parseColor("#4CD7D0"),
				Color.parseColor("#F8D210"),
				Color.parseColor("#E09D60"),
				Color.parseColor("#FF8000"),
				Color.parseColor("#B0DB43"),
				Color.parseColor("#9D1B6F")
		));
	}


	@NonNull
	@Override
	public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View view = null;
		switch (viewType){
			case(MSG_TYPE_TEXT_MY):
				view = LayoutInflater.from(context).inflate(R.layout.my_message_item, parent, false);
				break;
			case(MSG_TYPE_TEXT_YOUR):
				view = LayoutInflater.from(context).inflate(R.layout.your_message_item, parent, false);
				break;
			case(MSG_TYPE_PHOTO_MY):
				view = LayoutInflater.from(context).inflate(R.layout.my_message_item_photo, parent, false);
				break;
			case(MSG_TYPE_PHOTO_YOUR):
				view = LayoutInflater.from(context).inflate(R.layout.your_message_item_photo, parent, false);
				break;
		}
		return new MessageAdapter.ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, int position) {
		Message message = messages.get(position);
		holder.usernameTV.setText(message.getSender());

		if(message.getType() == Message.MESSAGE_TYPE_TEXT)
			holder.messageTV.setText(message.getMessage());
		else {
			holder.imageIV.setMinimumHeight(100);
			storageRef = FirebaseStorage.getInstance().getReference();

			StorageReference imageRef = storageRef.child(message.getImagePath());

			final String fileName = message.getImagePath().substring(message.getImagePath().indexOf("/") + 1) + ".jpg";

			//File file = null;//new File(storagePath, fileName);
			//final File file = File.createTempFile("image", "jpg");
			final File file = new File(context.getFilesDir(),  fileName);
			if (file.exists()) {
				holder.imageIV.setImageBitmap(readBitmap(file));
				//Toast.makeText(context, "read from storage", Toast.LENGTH_SHORT).show();
			} else {
				imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
					@Override
					public void onSuccess(final Uri uri) {
						Thread thread = new Thread(new Runnable() {
							Bitmap image = null;

							@Override
							public void run() {
								try {
									InputStream stream = (InputStream) new URL(uri.toString()).getContent();
									image = BitmapFactory.decodeStream(stream);
									stream.close();

									OutputStream outStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
									image.compress(Bitmap.CompressFormat.PNG, 85, outStream);
									outStream.flush();
									outStream.close();

									holder.imageIV.post(new Runnable() {
										@Override
										public void run() {
											holder.imageIV.setImageBitmap(image);
										}
									});
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							@Override
							protected void finalize() throws Throwable {
								super.finalize();
								try {
								}catch (Exception e){
									e.printStackTrace();
								}
							}
						});
						thread.setPriority(Thread.MAX_PRIORITY);
						thread.start();
					}
				});
			/*imageRef.getFile(file).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
				@Override
				public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
					holder.imageIV.setImageBitmap(readBitmap(file));
					holder.imageIV.refreshDrawableState();
				}
			});*/
			}
		}


		if(!user2color.containsKey(message.getSender()))
			user2color.put(message.getSender(), colors.get(colorIndex++ % colors.size()));

		holder.usernameTV.setTextColor(user2color.get(message.getSender()));
	}

	private Bitmap readBitmap(File file){
		Bitmap bitmap = null;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	@Override
	public int getItemCount() {
		return messages.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder{
		public TextView usernameTV, messageTV;

		public ImageView imageIV;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			usernameTV = itemView.findViewById(R.id.username_tv);

			messageTV =  itemView.findViewById(R.id.message_tv);

			imageIV = itemView.findViewById(R.id.image_iv);
		}
	}

	@Override
	public int getItemViewType(int position) {
		if(username.equals(messages.get(position).getSender())){
			if(messages.get(position).getType()==Message.MESSAGE_TYPE_TEXT)
				return MSG_TYPE_TEXT_MY;
			else
				return MSG_TYPE_PHOTO_MY;
		}else{
			if(messages.get(position).getType()==Message.MESSAGE_TYPE_TEXT)
				return MSG_TYPE_TEXT_YOUR;
			else
				return MSG_TYPE_PHOTO_YOUR;
		}
	}
}

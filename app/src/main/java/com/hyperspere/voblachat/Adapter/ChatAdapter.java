package com.hyperspere.voblachat.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hyperspere.voblachat.MessagingActivity;
import com.hyperspere.voblachat.Model.Chat;
import com.hyperspere.voblachat.Model.User;
import com.hyperspere.voblachat.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

	private Activity mActivity;
	private List<Chat> chats;
	private User user;

	private ViewHolder toDelete = null;

	public ChatAdapter(Activity mActivity, List<Chat> chats, User user){
		this.mActivity = mActivity;
		this.chats = chats;
		this.user = user;
	}


	@NonNull
	@Override
	public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mActivity).inflate(R.layout.chat_item, parent, false);
		return new ChatAdapter.ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
		Chat chat = chats.get(position);
		holder.chatNameTV.setText(chat.getName());
		holder.chat = chat;
	}

	@Override
	public int getItemCount() {
		return chats.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder{
		TextView chatNameTV;
		private LinearLayout deleteLayout, chatNameLayout;
		Chat chat;
		private  View root;
		private boolean deleteMode = false;

		ViewHolder(@NonNull View itemView) {
			super(itemView);
			root = itemView;

			chatNameTV = itemView.findViewById(R.id.username_tv);

			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(toDelete!=null)
						toDelete.deleteLayout.performLongClick();
				}
			});

			View.OnLongClickListener listener = new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					deleteMode = !deleteMode;
					if (deleteMode){
						if(toDelete!=null && toDelete!=ViewHolder.this)
							toDelete.root.performLongClick();

						toDelete = ViewHolder.this;
						deleteLayout.setVisibility(View.VISIBLE);
						chatNameLayout.setVisibility(View.INVISIBLE);
					}else{
						deleteLayout.setVisibility(View.INVISIBLE);
						chatNameLayout.setVisibility(View.VISIBLE);
						toDelete = null;
					}

					return true;
				}
			};

			deleteLayout = itemView.findViewById(R.id.delete_layout);
			chatNameLayout = itemView.findViewById(R.id.chat_name_layout);

			chatNameLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(toDelete!=null)
						toDelete.root.performLongClick();

					Intent intent = new Intent(mActivity, MessagingActivity.class);
					intent.putExtra("ChatId", chat.getId());

					mActivity.startActivity(intent);
				}
			});
			chatNameLayout.setOnLongClickListener(listener);

			deleteLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = chats.indexOf(chat);

					DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getId()).child("MyChats");
					userChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							for(DataSnapshot snapshot: dataSnapshot.getChildren()){
								if (snapshot.getValue(String.class).equals(chat.getId())) {
									snapshot.getRef().removeValue();
									return;
								}
							}
						}
						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {	}
					});

					final DatabaseReference chatMembersRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(chat.getId()).child("Members");
					chatMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
							if(dataSnapshot.getChildrenCount()<=1) {
								chatMembersRef.getParent().removeValue();
								// TODO: 09.02.2020 чистить картинки с диска / облака
							}
							for(DataSnapshot snapshot: dataSnapshot.getChildren()){
								if (snapshot.getValue(String.class).equals(user.getId())) {
									snapshot.getRef().removeValue();
									return;
								}
							}
						}
						@Override
						public void onCancelled(@NonNull DatabaseError databaseError) {	}
					});

					chats.remove(pos);
					notifyItemRemoved(pos);
					toDelete = null;
				}
			});
			deleteLayout.setOnLongClickListener(listener);
		}
	}
}

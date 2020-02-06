package com.hyperspere.voblachat.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperspere.voblachat.MessagingActivity;
import com.hyperspere.voblachat.Model.Chat;
import com.hyperspere.voblachat.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

	private Activity mActivity;
	private List<Chat> chats;

	public ChatAdapter(Activity mActivity, List<Chat> chats){
		this.mActivity = mActivity;
		this.chats = chats;
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
		holder.mChat = chat;
	}

	@Override
	public int getItemCount() {
		return chats.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder{
		public TextView chatNameTV;
		Chat mChat;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			chatNameTV = itemView.findViewById(R.id.username_tv);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mActivity, MessagingActivity.class);
					intent.putExtra("ChatId", mChat.getId());

					mActivity.startActivity(intent);
				}
			});
		}
	}
}

package com.hyperspere.voblachat.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.hyperspere.voblachat.Model.Message;
import com.hyperspere.voblachat.Model.User;
import com.hyperspere.voblachat.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

	private Activity mActivity;
	private List<Chat> chats;
	private User user;

	private ViewHolder redacting = null;

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
		holder.chatNameET.setText(chat.getName());
		holder.chat = chat;
		Message lastMessage = chat.getLastMessage();
		holder.lastMessageTV.setText(lastMessage.getSender() + ": " + (lastMessage.getType() == Message.MESSAGE_TYPE_TEXT ?
																		lastMessage.getMessage() : "image"));
	}

	@Override
	public int getItemCount() {
		return chats.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder{
		//TextView chatNameTV;
		private LinearLayout buttonsLayuot, mainLayout;
		private EditText chatNameET;
		private TextView lastMessageTV;
		private Button deleteButton, renameButton, cancelButton;
		Chat chat;

		ViewHolder(@NonNull View itemView) {
			super(itemView);

			chatNameET = itemView.findViewById(R.id.chat_name_et);
			deleteButton = itemView.findViewById(R.id.delete_button);
			cancelButton = itemView.findViewById(R.id.cancel_button);
			renameButton = itemView.findViewById(R.id.rename_button);
			buttonsLayuot = itemView.findViewById(R.id.chat_item_buttons_layout);
			mainLayout = itemView.findViewById(R.id.chat_item_main_layout);
			lastMessageTV = itemView.findViewById(R.id.last_message_tv);

			deleteButton.setOnClickListener(new View.OnClickListener() {
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

					redacting = null;
				}
			});

			cancelButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					redacting = null;

					buttonsLayuot.setVisibility(View.INVISIBLE);
					mainLayout.setVisibility(View.VISIBLE);
				}
			});

			renameButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					buttonsLayuot.setVisibility(View.INVISIBLE);
					mainLayout.setVisibility(View.VISIBLE);

					/*chatNameET.setOnKeyListener(new View.OnKeyListener() {
						@Override
						public boolean onKey(View v, int keyCode, KeyEvent event) {
							if((event.getAction() == KeyEvent.ACTION_DOWN)){
								if(keyCode == KeyEvent.KEYCODE_ENTER){


								}
							}
							return true;
						}
					});*/


					chatNameET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
						String before;
						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							if(hasFocus){
								before = chatNameET.getText().toString();
								mainLayout.setClickable(false);
							}else {
								InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

								mainLayout.setClickable(true);
								if (chatNameET.getText().length() > 0) {

									DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getId());
									DatabaseReference myChatsNamesRef = myRef.child("MyChatsNames");
									myChatsNamesRef.child(chat.getId()).setValue(chatNameET.getText().toString());
								} else {
									chatNameET.setText(before);
								}
								chatNameET.setFocusable(false);
								chatNameET.setFocusableInTouchMode(false);
							}
						}
					});
					chatNameET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
							chatNameET.clearFocus();
							chatNameET.setFocusable(false);
							chatNameET.setFocusableInTouchMode(false);
							return true;
						}
					});


					chatNameET.setFocusableInTouchMode(true);
					chatNameET.setFocusable(true);
					chatNameET.requestFocus();
					InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(chatNameET, InputMethodManager.RESULT_SHOWN);

				}
			});

			chatNameET.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!chatNameET.isFocusable())
						mainLayout.callOnClick();
				}
			});

			chatNameET.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					mainLayout.performLongClick();
					return true;
				}
			});

			lastMessageTV.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mainLayout.callOnClick();
				}
			});

			lastMessageTV.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					mainLayout.performLongClick();
					return true;
				}
			});

			mainLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!mainLayout.isClickable())
						return; // клик во время редактирования имени

					if(redacting !=null)
						redacting.cancelButton.callOnClick();

					Intent intent = new Intent(mActivity, MessagingActivity.class);
					intent.putExtra("ChatId", chat.getId());

					mActivity.startActivity(intent);
				}
			});
			mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					redacting = ViewHolder.this;

					mainLayout.setVisibility(View.INVISIBLE);
					buttonsLayuot.setVisibility(View.VISIBLE);

					return true;
				}
			});
		}
	}
}

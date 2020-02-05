package com.hyperspere.voblachat.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperspere.voblachat.Model.Message;
import com.hyperspere.voblachat.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

	private static final int MSG_TYPE_MY = 1;
	private static final int MSG_TYPE_YOUR = 2;

	private Context mContext;
	private List<Message> messages;
	private Map<String, Integer> user2color;
	private String username;

	private ArrayList<Integer> colors;
	private int colorIndex = 0;

	public MessageAdapter(Context mContext, List<Message> messages, String username){
		this.mContext = mContext;
		this.messages = messages;
		this.username = username;

		user2color = new HashMap<>();
		user2color.put(username, ContextCompat.getColor(mContext, R.color.colorMineMessage));

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
		if(viewType == MSG_TYPE_MY) {
			View view = LayoutInflater.from(mContext).inflate(R.layout.my_message_item, parent, false);
			return new MessageAdapter.ViewHolder(view);
		}else{
			View view = LayoutInflater.from(mContext).inflate(R.layout.your_message_item, parent, false);
			return new MessageAdapter.ViewHolder(view);
		}
	}

	@Override
	public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
		Message message = messages.get(position);
		holder.messageTV.setText(message.getMessage());
		holder.usernameTV.setText(message.getSender());

		if(!user2color.containsKey(message.getSender()))
			user2color.put(message.getSender(), colors.get(colorIndex++ % colors.size()));

		holder.usernameTV.setTextColor(user2color.get(message.getSender()));
	}

	@Override
	public int getItemCount() {
		return messages.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder{
		public TextView usernameTV, messageTV;


		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			usernameTV = itemView.findViewById(R.id.username_tv2);

			messageTV =  itemView.findViewById(R.id.message_tv);
		}
	}

	@Override
	public int getItemViewType(int position) {
		if(username.equals(messages.get(position).getSender())){
			return MSG_TYPE_MY;
		}else{
			return MSG_TYPE_YOUR;
		}
	}
}

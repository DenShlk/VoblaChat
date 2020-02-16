package com.hyperspere.voblachat.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperspere.voblachat.Model.User;
import com.hyperspere.voblachat.R;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

	private Activity mActivity;
	private List<User> users;
	public List<User> checked;

	public UserAdapter(Activity mActivity, List<User> users){
		this.mActivity = mActivity;
		this.users = users;
		checked = new ArrayList<>();
	}


	@NonNull
	@Override
	public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(mActivity).inflate(R.layout.user_item, parent, false);
		return new UserAdapter.ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
		User user = users.get(position);
		holder.usernameTV.setText(user.getUsername());
		holder.user = user;
	}

	@Override
	public int getItemCount() {
		return users.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder{
		private TextView usernameTV;
		private CheckBox checkBox;
		private ConstraintLayout layout;
		User user;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			usernameTV = itemView.findViewById(R.id.username_tv);
			checkBox = itemView.findViewById(R.id.user_add_checkbox);
			layout = itemView.findViewById(R.id.user_item_layout);
			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked)
						if(!checked.contains(user))
							checked.add(user);
					else
						checked.remove(user);
				}
			});

			layout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					checkBox.setChecked(!checkBox.isChecked());
				}
			});
		}
	}
}

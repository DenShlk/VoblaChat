package com.hyperspere.voblachat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class BootBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(context, NewMessageChecker.class));
	}
}

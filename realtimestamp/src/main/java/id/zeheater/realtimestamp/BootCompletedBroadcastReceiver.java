package id.zeheater.realtimestamp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * Implement Boot Broadcast Receiver to pre-emp ntp request as early as possible
 * so that when our application is launched, it is already initiallized
 *
 */

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) { }
}

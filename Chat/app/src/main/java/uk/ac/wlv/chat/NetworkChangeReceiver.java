package uk.ac.wlv.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isNetworkConnected(context)) {
            Toast.makeText(context, "Back to device online", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Network is not available. Unable to upload messages.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}

package com.example.notificationsassignment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.notificationsassignment.MainActivity;
import com.example.notificationsassignment.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

class DownloadTask implements Runnable {

    // Raw feed file IDs used in this offline version of the app
    public static final int[] txtFeeds = {R.raw.reto_meier, R.raw.james_williams, R.raw.dan_galpin};

    private static final String MY_CHANNEL_ID = "Download Status";
    private final int MY_NOTIFICATION_ID = 1;
    private static final String TAG = "DownloaderTask";
    private static final int SIM_NETWORK_DELAY = 3000;
    private final String[] mFeeds = new String[3];
    private final MainActivity mParentActivity;
    private final Context mApplicationContext;

    private final String[] urls;

    // Constructor
    public DownloadTask(MainActivity parentActivity, String... urls) {
        mParentActivity = parentActivity;
        mApplicationContext = parentActivity.getApplicationContext();
        this.urls = urls;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) mApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(MY_CHANNEL_ID, "Progress Update Channel", NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(channel);
        }
    }

    @Override
    public void run() {
        Log.i(TAG, "Entered doInBackground()");
        download(urls);
    }


    // Simulate downloading tweets from the network
    private void download(String[] urlParameters) {

        boolean downloadCompleted = false;

        try {

            for (int idx = 0; idx < urlParameters.length; idx++) {

                InputStream inputStream;
                BufferedReader in;

                try {
                    // Pretend the tweets take a long time to load
                    Thread.sleep(SIM_NETWORK_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                inputStream = mApplicationContext.getResources().openRawResource(txtFeeds[idx]);
                in = new BufferedReader(new InputStreamReader(inputStream));

                String readLine;
                StringBuilder builder = new StringBuilder();

                while ((readLine = in.readLine()) != null) {
                    builder.append(readLine);
                }

                mFeeds[idx] = builder.toString();

                in.close();

            }

            downloadCompleted = true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "Tweet Download Completed:" + downloadCompleted);

        notify(downloadCompleted);

        // Call back to the MainActivity to update the feed display
        if (mParentActivity != null) {

            AppExecutor.getInstance().getMainThread().execute(new Runnable() {
                @Override
                public void run() {
                    mParentActivity.setRefreshed(mFeeds);
                }
            });

        }


    }


    // If necessary, notifies the user that the tweet downloads are complete.
    // Sends an ordered broadcast back to the BroadcastReceiver in MainActivity
    // to determine whether the notification is necessary.

    private void notify(final boolean success) {
        Log.i(TAG, "Entered notify()");

        final Intent restartMainActivityIntent = new Intent(mApplicationContext, MainActivity.class);
        restartMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (success) {
            // Save tweets to a file
            saveTweetsToFile();
        }

        // Sends an ordered broadcast to determine whether MainActivity is
        // active and in the foreground. Creates a new BroadcastReceiver
        // to receive a result indicating the state of MainActivity

        // The Action for this broadcast Intent is  MainActivity.DATA_REFRESHED_ACTION
        // The result MainActivity.IS_ALIVE, indicates that MainActivity is active and in the foreground.

        mApplicationContext.sendOrderedBroadcast(new Intent(MainActivity.DATA_REFRESHED_ACTION), null, new BroadcastReceiver() {

            final String failMsg = "Download has failed. Please retry Later.";
            final String successMsg = "Download completed successfully.";

            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(TAG, "Entered result receiver's onReceive() method");

                if (MainActivity.IS_ALIVE == 1) {

                    createMainNotificationChannel(context);
                    PendingIntent pendingIntent = TaskStackBuilder.create(context)
                            .addNextIntentWithParentStack(restartMainActivityIntent)
                            .getPendingIntent(0, 0);


                    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MY_CHANNEL_ID);
                    builder.setCustomContentView(remoteViews);
                    builder.setContentIntent(pendingIntent);
                    builder.setSmallIcon(R.drawable.ic_launcher_foreground);
                    builder.setAutoCancel(true);
                    remoteViews.setTextViewText(R.id.text, "Download completed successfully");

                    // You will have to set several pieces of information. You can use
                    // android.R.drawable.stat_sys_warning for the small icon.
                    // You should also setAutoCancel(true).

                    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                    notificationManagerCompat.notify(1, builder.build());
                }
            }
        }, null, 0, null, null);
    }

    // Saves the tweets to a file
    private void saveTweetsToFile() {
        PrintWriter writer = null;
        try {
            FileOutputStream fos = mApplicationContext.openFileOutput(MainActivity.TWEET_FILENAME, Context.MODE_PRIVATE);
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos)));
            for (String s : mFeeds) {
                writer.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != writer) {
                writer.close();
            }
        }
    }


    public void createMainNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = context.getString(R.string.main_channel);
            String channelDescription = context.getString(R.string.main_channel_description);
            NotificationChannel notificationChannel =
                    new NotificationChannel(MY_CHANNEL_ID, channelName,
                            NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(channelDescription);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setShowBadge(true);
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }


}
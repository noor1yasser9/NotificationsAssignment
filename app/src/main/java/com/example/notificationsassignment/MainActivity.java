package com.example.notificationsassignment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SelectionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String DATA_REFRESHED_ACTION = "com.example.notificationassignment.DATA_REFRESHED";
    public static final String TWEET_FILENAME = "tweets.txt";
    public static final String[] FRIENDS = {"Reto Meier", "James Willian", "Dan Galpin"};
    public static final int IS_ALIVE = RESULT_FIRST_USER;

    // Actual URLs are unused in this offline version of the app
    private static final String URL_RETO_TWEETS = "https://domain.com/sample1.txt";
    private static final String URL_JAMES_TWEETS = "https://domain.com/sample2.txt";
    private static final String URL_DAN_TWEETS = "https://domain.com/sample3.txt";

    private static final long TWO_MIN = 2 * 60 * 1000;
    private static final int NUM_FRIENDS = 3;
    private static final int UNSELECTED = -1;

    private final String[] mRawFeeds = new String[3];
    private final String[] mProcessedFeeds = new String[3];

    private FragmentManager mFragmentManager;
    private BroadcastReceiver mRefreshReceiver;
    private FeedFragment mFeedFragment;

    private int mFeedSelected = UNSELECTED;
    private boolean mIsFresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentManager = getSupportFragmentManager();

        // Show friends fragment
        addFriendsFragment();

        // The feed is fresh if it was downloaded less than 2 minutes ago
        mIsFresh = (System.currentTimeMillis() - getFileStreamPath(TWEET_FILENAME).lastModified()) < TWO_MIN;

        // Get and show data
        getData();

    }


    /**
     * Register the BroadcastReceiver
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (mRefreshReceiver != null) {
            IntentFilter intentFilter = new IntentFilter(DATA_REFRESHED_ACTION);
            registerReceiver(mRefreshReceiver, intentFilter);

        }

    }

    @Override
    protected void onPause() {

        if (mRefreshReceiver != null) {
            unregisterReceiver(mRefreshReceiver);
        }

        super.onPause();

    }


    /**
     * Called when a Friend is clicked on
     *
     * @param position position of selected item
     */
    @Override
    public void onItemSelected(int position) {

        mFeedSelected = position;
        mFeedFragment = addFeedFragment();

        if (mIsFresh) {
            updateFeed();
        }

    }


    /**
     * Add Friends Fragment to Activity
     */
    private void addFriendsFragment() {

        FriendsFragment mFriendsFragment = new FriendsFragment();
        mFriendsFragment.setArguments(getIntent().getExtras());

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.fragment_container, mFriendsFragment);

        transaction.commit();

    }

    /**
     * Add FeedFragment to Activity
     *
     * @return instance of FeedFragment
     */
    private FeedFragment addFeedFragment() {
        FeedFragment feedFragment = new FeedFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, feedFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        mFragmentManager.executePendingTransactions();
        return feedFragment;
    }


    /**
     * If stored Tweets are not fresh, reload them from network!
     * Otherwise, load them from file
     */
    private void getData() {

        Log.i(TAG, "In ensureData(), mIsFresh:" + mIsFresh);

        if (!mIsFresh) {


            Toast.makeText(getApplicationContext(), "Downloading Tweets from Network", Toast.LENGTH_LONG).show();
            // Start loading new data
            DownloadTask downloadTask = new DownloadTask(this, URL_RETO_TWEETS, URL_JAMES_TWEETS, URL_DAN_TWEETS);
            AppExecutor.getInstance().getNetworkIO().execute(downloadTask);

            // Set up a BroadcastReceiver to receive an Intent when download finishes
            mRefreshReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    Log.i(TAG, "BroadcastIntent received in MainActivity");

                    if (isOrderedBroadcast()){
                        setResultCode(MainActivity.IS_ALIVE);
                    }

                }
            };

        } else {

            loadTweetsFromFile();
            parseJSON();
            updateFeed();

        }

    }

    /**
     * Called when new Tweets have been downloaded
     *
     * @param feeds twitter feeds arrays
     */
    public void setRefreshed(String[] feeds) {

        mRawFeeds[0] = feeds[0];
        mRawFeeds[1] = feeds[1];
        mRawFeeds[2] = feeds[2];

        parseJSON();

        updateFeed();

        mIsFresh = true;

    }

    /**
     * Retrieve feeds text from a file
     * Store them in mRawTextFeed[]
     */
    private void loadTweetsFromFile() {
        BufferedReader reader = null;
        try {
            FileInputStream fis = openFileInput(TWEET_FILENAME);
            reader = new BufferedReader(new InputStreamReader(fis));
            String s;
            int i = 0;
            while (null != (s = reader.readLine()) && i < NUM_FRIENDS) {
                mRawFeeds[i] = s;
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Convert raw Tweet data (in JSON format) into text for display
     */
    private void parseJSON() {

        JSONArray[] JSONFeeds = new JSONArray[NUM_FRIENDS];

        for (int i = 0; i < NUM_FRIENDS; i++) {

            try {
                JSONFeeds[i] = new JSONArray(mRawFeeds[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String name = "";
            String tweet = "";

            JSONArray tmp = JSONFeeds[i];

            // string buffer for twitter feeds
            StringBuilder builder = new StringBuilder("");

            for (int j = 0; j < tmp.length(); j++) {
                try {
                    tweet = tmp.getJSONObject(j).getString("text");
                    JSONObject user = (JSONObject) tmp.getJSONObject(j).get(
                            "user");
                    name = user.getString("name");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                builder.append(name).append(" - ").append(tweet).append("\n\n");
            }

            mProcessedFeeds[i] = builder.toString();

        }

    }

    /**
     * Calls FeedFragment.update, passing in the
     * the tweets for the currently selected friend
     */
    private void updateFeed() {
        if (null != mFeedFragment) {
            mFeedFragment.update(mProcessedFeeds[mFeedSelected]);
        }
    }

}

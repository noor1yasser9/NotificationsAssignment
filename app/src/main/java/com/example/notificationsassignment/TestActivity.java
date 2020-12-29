package com.example.notificationsassignment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TestActivity extends AppCompatActivity {

	private final static long DAWN_OF_TIME = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		Button ageTweetsButton = findViewById(R.id.age_tweets_button);
		ageTweetsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setFileAge(DAWN_OF_TIME);
			}

		});

		Button rejuvTweetsButton = findViewById(R.id.rejuv_tweets_button);
		rejuvTweetsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setFileAge(System.currentTimeMillis());
			}

		});

		Button startMainActivityButton = findViewById(R.id.start_main_button);
		startMainActivityButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(TestActivity.this,
						MainActivity.class));
			}
		});

		createTweetFileIfMissing();

	}

	private void createTweetFileIfMissing() {

		String fileName = TestActivity.this.getFilesDir() + "/" + MainActivity.TWEET_FILENAME;

		File file = new File(fileName);
		if (!file.exists()) {

			PrintWriter out = null;
			BufferedReader in = null;

			try {
				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(openFileOutput(MainActivity.TWEET_FILENAME,Context.MODE_PRIVATE))));
				for (int resId : DownloadTask.txtFeeds) {
					in = new BufferedReader(new InputStreamReader(getResources().openRawResource(resId)));
					String line;
					StringBuffer buffer = new StringBuffer();
					while ((line = in.readLine()) != null) {
						buffer.append(line);
					}
					out.println(buffer);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (Resources.NotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (null != in) {
						in.close();
					}
					if (null != out) {
						out.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private void setFileAge(long timestamp) {
		String filename = TestActivity.this.getFilesDir() + "/" + MainActivity.TWEET_FILENAME;
		File file = new File(filename);
		if (file.exists()) {
			file.setLastModified(timestamp);
		}
	}

}

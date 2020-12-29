package com.example.notificationsassignment;

import android.app.Activity;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.robotium.solo.Solo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class NewFeedTest {

    private Activity activity;

    @Rule
    public ActivityScenarioRule<TestActivity> rule  = new  ActivityScenarioRule<>(TestActivity.class);

    @Test
    public void useAppContext() {

        rule.getScenario().onActivity(new ActivityScenario.ActivityAction<TestActivity>() {
            @Override
            public void perform(TestActivity activity) {

                NewFeedTest.this.activity = activity;

            }
        });

        Solo solo = new Solo(InstrumentationRegistry.getInstrumentation(), activity);

        int shortDelay = 2000;

        // Wait for activity:
        // 'com.example.notificationsassignment.TestActivity'
        solo.waitForActivity(TestActivity.class, shortDelay);

        //solo.sleep(shortDelay);

        // Click on Make Tweets New
        solo.clickOnView(solo.getView(com.example.notificationsassignment.R.id.rejuv_tweets_button));

        //solo.sleep(shortDelay);

        // Click on Start Main Activty
        solo.clickOnView(solo.getView(com.example.notificationsassignment.R.id.start_main_button));

        // Wait for activity: 'com.example.notificationsassignment.MainActivity'
        assertTrue("com.example.notificationsassignment.MainActivity is not found!",
                solo.waitForActivity(com.example.notificationsassignment.MainActivity.class));

        //solo.sleep(shortDelay);

        solo.clickOnView(solo.getView(android.R.id.text1));

        // Assert that: feed_view is shown
        assertTrue("feed_view! feed_view not found", solo.waitForView(solo
                .getView(com.example.notificationsassignment.R.id.feed_view)));

        // Assert that: 'Professional Android 4th Edition is here!' is shown
        assertTrue("'Professional Android 4th Edition is here!' is not shown!",
                solo.searchText("Professional Android 4th Edition is here!"));

        //solo.sleep(shortDelay);

        // Press menu back key
        solo.goBack();

        //solo.sleep(shortDelay);

        solo.clickOnView(solo.getView(android.R.id.text1, 1));

        // Assert that: feed_view is shown
        assertTrue("feed_view! is not shown!", solo.waitForView(solo
                .getView(com.example.notificationsassignment.R.id.feed_view)));

        // Assert that: 'MRW looking at years old code' is shown
        assertTrue("'MRW looking at years old code' is not shown!",
                solo.searchText("MRW looking at years old code"));

        //solo.sleep(shortDelay);

        // Press menu back key
        solo.goBack();

        solo.clickOnView(solo.getView(android.R.id.text1, 2));

        // Assert that: feed_view shown
        assertTrue("feed_view not shown", solo.waitForView(solo
                .getView(com.example.notificationsassignment.R.id.feed_view)));

        // Assert that: 'Looking forward to seeing you at the #AndroidDevSummit!' is shown
        assertTrue("'Looking forward to seeing you at the #AndroidDevSummit!' is not shown!",
                solo.searchText("Looking forward to seeing you at the #AndroidDevSummit!"));
    }

}

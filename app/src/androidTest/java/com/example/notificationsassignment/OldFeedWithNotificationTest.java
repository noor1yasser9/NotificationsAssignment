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
public class OldFeedWithNotificationTest {

    private Activity activity;

    @Rule
    public ActivityScenarioRule<TestActivity> rule  = new  ActivityScenarioRule<>(TestActivity.class);

    @Test
    public void useAppContext() {

        rule.getScenario().onActivity(new ActivityScenario.ActivityAction<TestActivity>() {
            @Override
            public void perform(TestActivity activity) {

                OldFeedWithNotificationTest.this.activity = activity;

            }
        });

        Solo solo = new Solo(InstrumentationRegistry.getInstrumentation(), activity);

        //int shortDelay = 2000;
        int longDelay = 10000;

        // Wait for activity:
        // 'com.example.notificationsassignment.TestActivity'
        solo.waitForActivity(
                TestActivity.class, 2000);

        //solo.sleep(shortDelay);

        // Click on Make Tweets Old
        solo.clickOnView(solo
                .getView(com.example.notificationsassignment.R.id.age_tweets_button));

        //solo.sleep(shortDelay);

        // Click on Start Main Activty
        solo.clickOnView(solo
                .getView(com.example.notificationsassignment.R.id.start_main_button));

        // Wait for activity: 'com.example.notificationsassignment.MainActivity'
        assertTrue(
                "com.example.notificationsassignment.MainActivity is not found!",
                solo.waitForActivity(com.example.notificationsassignment.MainActivity.class));

        //solo.sleep(shortDelay);

        solo.clickOnView(solo.getView(android.R.id.text1));

        // Assert that: 'Please wait while we download the Tweets!' is shown
        assertTrue("'Please wait while we download the Tweets!' is not shown!",
                solo.waitForView(solo
                        .getView(com.example.notificationsassignment.R.id.feed_view)));

        //solo.sleep(shortDelay);

        // Press menu back key
        solo.goBack();

        //solo.sleep(shortDelay);

        // Press menu back key
        solo.goBack();

        // Wait for activity:
        // 'com.example.notificationsassignment.TestActivity'
        assertTrue("com.example.notificationsassignment.TestActivity is not found!",
                solo.waitForActivity(TestActivity.class));

        // Sleep while twitter feed loads
         solo.sleep(longDelay);
        
    }
}

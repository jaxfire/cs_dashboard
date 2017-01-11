package com.jaxfire.donutview;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.test.InstrumentationTestCase;

import org.json.JSONException;
import org.junit.Before;

import java.net.MalformedURLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RetrieveScoreTaskTest extends InstrumentationTestCase {

    private Context insCont;
    private DonutView donutView;
    private String scoreURL = "https://5lfoiyb0b3.execute-api.us-west-2.amazonaws.com/prod/mockcredit/values";
    private CountDownLatch signal;
    private RetrieveScoreTask retrieveScoreTask;

    @Before
    public void setUp() throws Exception{
        insCont = InstrumentationRegistry.getTargetContext();
        donutView = new DonutView(insCont);

        signal = new CountDownLatch(1);

        retrieveScoreTask = new RetrieveScoreTask(insCont, donutView, false) {

            @Override
            protected void onPostExecute(String result) {
                signal.countDown();
            }
        };
    }

    public void testParseString(){
        RetrieveScoreTask retrieveScoreTask = new RetrieveScoreTask(insCont, donutView, true);
        String jsonString = "{\"creditReportInfo\":{\"score\":514,\"maxScoreValue\":700}}";
        retrieveScoreTask.parseString(jsonString);
        assertEquals(false, retrieveScoreTask.exception instanceof JSONException);
        assertEquals(retrieveScoreTask.score, 514);
        assertEquals(retrieveScoreTask.maxScoreValue, 700);
    }

    public void testParseStringMalformedJson(){
        RetrieveScoreTask retrieveScoreTask = new RetrieveScoreTask(insCont, donutView, true);
        String jsonString = "{jkadsfhkdjsa}";
        retrieveScoreTask.parseString(jsonString);
        assertEquals(true, retrieveScoreTask.exception instanceof JSONException);
    }

    public void testJsonRequest () throws Throwable {

        // Must be on the UI thread!
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                retrieveScoreTask.execute(scoreURL);
            }
        });

        /* The testing thread will wait here until the UI thread releases it
         * above with the countDown() or 30 seconds passes and it times out.
         */
        signal.await(30, TimeUnit.SECONDS);

        assertEquals(retrieveScoreTask.errorCode, 0);
        assertEquals(retrieveScoreTask.exception, null);

    }

    public void testJsonRequestMalformedURL () throws Throwable {

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                retrieveScoreTask.execute("incorrectURL");
            }
        });

        signal.await(30, TimeUnit.SECONDS);

        assertEquals(retrieveScoreTask.errorCode, RetrieveScoreTask.ERROR_CODE_MALFORMED_URL_EXCEPTION);
        assertEquals(true, retrieveScoreTask.exception instanceof MalformedURLException);

    }

}
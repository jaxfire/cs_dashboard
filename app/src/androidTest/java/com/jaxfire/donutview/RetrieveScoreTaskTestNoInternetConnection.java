package com.jaxfire.donutview;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.test.InstrumentationRegistry;
import android.test.InstrumentationTestCase;

import org.junit.Before;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RetrieveScoreTaskTestNoInternetConnection  extends InstrumentationTestCase{

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

    public void testJsonRequestNoInternetConnection () throws Throwable {

        if (isNetworkAvailable()){
            fail("The internet connection must be disabled. Turn on flight mode");
        }

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                retrieveScoreTask.execute(scoreURL);
            }
        });

        signal.await(30, TimeUnit.SECONDS);

        assertEquals(retrieveScoreTask.errorCode, RetrieveScoreTask.ERROR_CODE_IO_EXCEPTION);
        assertEquals(true, retrieveScoreTask.exception instanceof IOException);

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) insCont.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
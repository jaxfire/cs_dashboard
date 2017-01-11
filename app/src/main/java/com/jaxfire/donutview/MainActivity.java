package com.jaxfire.donutview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

//Used only for example 2
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements RetrieveScoreTaskListener {

    DonutView donutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //EXAMPLE 1: USING A DONUTVIEW THAT IS PREDFINED IN THE LAYOUT XML
        donutView = (DonutView) findViewById(R.id.donut_view_1);
        //SetRadius is optional as it has a default size
        donutView.setRadius(100.0f);

        /*
        //EXAMPLE 2: CREATES A DONUTVIEW AND ADDS IT TO A LAYOUT PROGRAMMATICALLY
        //Note: You will need to remove the DonutView xml from activity_main.xml else two donuts will appear.
        donutView = new DonutView(this);

        //An alternative constructor which allows us to specify the score and radius
        //donutView = new DonutView(this, 200, 100.0f);

        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.activity_main);
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.donut_layout, mainLayout);

        DonutView donutView = (DonutView) findViewById(R.id.donut_view_2) ;
        donutView.setRadius(100.0f);
        */

        //Not necessary but we can hide the view until we have made a successful json call
        donutView.setVisibility(View.INVISIBLE);

        //json call to retrieve score values
        String scoreURL = "https://5lfoiyb0b3.execute-api.us-west-2.amazonaws.com/prod/mockcredit/values";
        new RetrieveScoreTask(this, donutView, true).execute(scoreURL);

    }

    //Call back from RetrieveScoreTask
    @Override
    public void updateDonut(DonutView dv, int score, int maxScoreValue, int errorCode, Exception exception) {
        if (exception != null){
            //If it's required then the exception can be cast to its specific type as per the supplied error code
            //Handle the error according to the applications overarching protocol / Send an error report
            Log.d("jim", "updateDonut: Error Code " + errorCode);
        } else {
            dv.updateValues(score, maxScoreValue);
            //The view can now ve made visible to the user
            dv.setVisibility(View.VISIBLE);
        }
    }
}
package com.jaxfire.donutview;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class RetrieveScoreTask extends AsyncTask<String, String, String> {

    static final int ERROR_CODE_MALFORMED_URL_EXCEPTION = -100;
    static final int ERROR_CODE_IO_EXCEPTION = -200;
    private static final int ERROR_CODE_JSON_EXCEPTION = -300;
    int  errorCode;
    Exception exception;
    private DonutView donutView;
    int score;
    int maxScoreValue;
    private Context context;
    private boolean showProgressDialog;

    RetrieveScoreTask(Context context, DonutView donutView, boolean showProgressDialog){
        this.context = context;
        this.donutView = donutView;
        this.showProgressDialog = showProgressDialog;
    }

    private ProgressDialog pd;

    protected void onPreExecute() {
        super.onPreExecute();

        pd = new ProgressDialog(context);

        if(showProgressDialog) {
            pd.setMessage(context.getResources().getString(R.string.retrieving_score));
            pd.setCancelable(false);
            pd.show();
        }
    }

    protected String doInBackground(String... params) {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            return buffer.toString();

        } catch (MalformedURLException e) {
            exception = e;
            errorCode = ERROR_CODE_MALFORMED_URL_EXCEPTION;
            Log.d("jim", "malformed");
        } catch (IOException e) {
            exception = e;
            errorCode = ERROR_CODE_IO_EXCEPTION;
            Log.d("jim", "io");
        }finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                exception = e;
                errorCode = ERROR_CODE_IO_EXCEPTION;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (pd.isShowing()){
            pd.dismiss();
        }

        RetrieveScoreTaskListener main = (RetrieveScoreTaskListener) context;

        if(errorCode == 0) {
            parseString(result);
        }

        main.updateDonut(donutView, score, maxScoreValue, errorCode, exception);

    }

    void parseString(String jsonString){

        try {
            JSONObject mainObject = new JSONObject(jsonString);
            JSONObject creditReportInfo = mainObject.getJSONObject(context.getResources().getString(R.string.credit_report_info));
            score = creditReportInfo.getInt(context.getResources().getString(R.string.score));
            maxScoreValue = creditReportInfo.getInt(context.getResources().getString(R.string.max_score_value));
        } catch (JSONException e) {
            exception = e;
            errorCode = ERROR_CODE_JSON_EXCEPTION;
        }

    }

}
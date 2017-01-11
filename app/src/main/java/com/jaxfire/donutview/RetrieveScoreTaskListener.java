package com.jaxfire.donutview;

interface RetrieveScoreTaskListener {
    void updateDonut(DonutView donutView, int score, int maxScoreValue, int errorCode, Exception exception);
}

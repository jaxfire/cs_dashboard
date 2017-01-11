package com.jaxfire.donutview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Printer;
import android.view.View;

public class DonutView extends View {

    private Context context;
    private AttributeSet attrs;
    private float radius, diameter, defaultRadius;
    private int score, maxScore, defaultScore;
    private Paint paint;
    private Path myPath;
    private RectF border, background, scoreArc, innerCircleMask;
    private SweepGradient gradient;

    {
        defaultRadius = 50.0f;
        defaultScore = 0;
    }

    public DonutView(Context context) {
        super(context);
        radius = defaultRadius;
        score = defaultScore;
    }

    public DonutView(Context context, int score, float radius) {
        this(context);
        this.score = score;
        this.radius = radius;
    }

    public DonutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;

        //Default of 700
        maxScore = 700;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,R.styleable.DonutChart,0,0);

        try {
            //If the optional values in the xml layout file are used they will be picked up here
            //else the second (default) arguments will be used
            radius = a.getDimension(R.styleable.DonutChart_radius, 50.0f);
            score = a.getInteger(R.styleable.DonutChart_score, 0);
        } finally {
            a.recycle();
        }

        init();
    }

    //Called after json parsing
    public void updateValues(int s, int max){
        score = s;
        maxScore = max;
        invalidate();
        requestLayout();
    }

    public void setRadius(float r){
        //Scale from dps value to pixels using the display scale factor
        final float scale = getContext().getResources().getDisplayMetrics().density;
        radius = (int) (r * scale + 0.5f);
        init();
        invalidate();
        requestLayout();
    }

    public void init(){

        paint = new Paint();
        paint.setDither(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(radius / 14.0f);

        //Score arc
        int[] arcColors = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE};
        gradient = new SweepGradient(radius, radius, arcColors , null);

        myPath = new Path();

        border = new RectF();
        background = new RectF();
        scoreArc = new RectF();
        innerCircleMask = new RectF();

        diameter = radius * 2;

        //DETERMINE GEOMETRY SIZES
        border.set(0, 0, diameter, diameter);

        float adjust = 0.002f * diameter;
        background.set(adjust, adjust, diameter - adjust, diameter - adjust);

        adjust = 0.05f * radius;
        scoreArc.set(adjust, adjust, diameter - adjust, diameter - adjust);

        adjust = 0.08f * radius;
        innerCircleMask.set(adjust, adjust, diameter - adjust, diameter - adjust);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //RENDER GEOMETRY
        //Border
        paint.setColor(Color.BLACK);
        canvas.drawArc(border, 270, 360, true, paint);

        //Background
        paint.setColor(Color.WHITE);
        canvas.drawArc(background, 270, 360, true, paint);

        Matrix matrix = new Matrix();
        matrix.setRotate(270, radius, radius);
        gradient.setLocalMatrix(matrix);
        matrix.reset();

        paint.setShader(gradient);
        canvas.drawArc(scoreArc, 270, scoreToDegrees(score), true, paint);
        paint.setShader(null);

        //Inner mask
        paint.setColor(Color.WHITE);
        canvas.drawArc(innerCircleMask, 270, 360, true, paint);

        //RENDER TEXT
        paint.setColor(Color.BLACK);
        paint.setTextSize(radius / 6);

        String firstLine = getContext().getString(R.string.your_credit_score_is);
        canvas.drawText(firstLine, (float)(radius - (radius * 0.70)), (float)(diameter * 0.35), paint);

        String secondLine = getContext().getString(R.string.out_of) + maxScore;
        canvas.drawText(secondLine, (float)(radius - radius * 0.35), (float)(diameter * 0.72), paint);

        //Add more colours to array to provide a more representative text colour.
        //Alternatively create an array that holds the full spectrum of colours in the gradient and select the relevant index.
        int[] textColors = {
                Color.RED, Color.rgb(255,128,0),
                Color.YELLOW, Color.rgb(128,255,0),
                Color.GREEN, Color.rgb(46,139,87),
                Color.BLUE
        };

        int score_text_color = textColors[(int)(score - 0.01) / 100];

        paint.setColor(score_text_color);
        paint.setTextSize(radius / 2);
        String scoreText = Integer.toString(score);
        float centeredXValue = (float)(radius - (scoreText.length() * (radius / 7.5)));
        canvas.drawText(scoreText, centeredXValue, (float)(diameter * 0.6), paint);

    }

    private long scoreToDegrees(int s){
        return (s - 1) * (360 * 1) / (maxScore - 1) + 1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth = (int) diameter;
        int desiredHeight = (int) diameter;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        }else if (widthMode == MeasureSpec.AT_MOST) {
            //wrap content
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        //THIS MUST BE CALLED
        setMeasuredDimension(width, height);
    }

}
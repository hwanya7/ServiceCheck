package com.seers.servicecheck.graph_new;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class StepCountBarGraph extends View {

    private Context context;
    private StepCountType stepCountType = StepCountType.DAY;
    private ArrayList<StepCountData> stepCountDataArrayList = new ArrayList<>();
    private int maxStepCount = 0;

    //걸음수 표현 타입
    public enum StepCountType{
        DAY,            //일단위
        WEEK,           //주단위
        WEEK_NUMBER     //주차 단위
    }

    private float gWidth = 0;
    private float gHeight = 0;

    private float itemWidth = 0;             //타입 마다 달라짐
    private float itemHeight = 0;             //타입 마다 달라짐
    private final int TOP_PADDING = 20;
    private final int BOTTOM_PADDING = 20;
    private final int LEFT_PADDING = 30;
    private final int RIGHT_PADDING = 20;
    private final int BAR_WIDTH = 3;
    private final int TEXT_SIZE = 10;


    private Paint valueTextPaint;       //값(평균값)
    private Paint dateTextPaint;        //일자
    private Paint timeTextPaint;        //시간
    private Paint graphLinePaint;       //line
    private Paint barPaint;             //bar



    public StepCountBarGraph(Context context) {
        super(context);
        this.context = context;
    }

    public StepCountBarGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void settingData(StepCountType stepCountType, ArrayList<StepCountData> stepCountDataArrayList, int maxStepCount){
        this.stepCountType = stepCountType;
        this.stepCountDataArrayList = stepCountDataArrayList;

        if(maxStepCount > 1000){
            if(maxStepCount > 1000 && maxStepCount <= 2000){
                maxStepCount = 2000;
            }else if(maxStepCount > 2000 && maxStepCount <= 5000){
                maxStepCount = 5000;
            }else if(maxStepCount > 5000 && maxStepCount <= 10000){
                maxStepCount = 10000;
            }else if(maxStepCount > 10000 && maxStepCount <= 15000){
                maxStepCount = 15000;
            }else if(maxStepCount > 15000 && maxStepCount <= 20000){
                maxStepCount = 20000;
            }else if(maxStepCount > 20000 && maxStepCount <= 30000){
                maxStepCount = 30000;
            }
        }else{
            maxStepCount = 1000;
        }
        this.maxStepCount = maxStepCount;
        init();
    }

    private void init(){
        valueTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valueTextPaint.setColor(Color.parseColor("#797979"));
        valueTextPaint.setTextSize(convertDpToPixel(TEXT_SIZE));

        dateTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dateTextPaint.setColor(Color.parseColor("#797979"));
        dateTextPaint.setTextSize(convertDpToPixel(TEXT_SIZE));

        timeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timeTextPaint.setColor(Color.parseColor("#797979"));
        timeTextPaint.setTextSize(convertDpToPixel(TEXT_SIZE));

        graphLinePaint = new Paint(0);
        graphLinePaint.setStrokeWidth(1);
        graphLinePaint.setColor(Color.parseColor("#EDEDED"));


        barPaint = new Paint(0);
        barPaint.setStrokeWidth(convertDpToPixel(20));
        barPaint.setColor(Color.parseColor("#e57373"));

        requestLayout();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        int left  = getLeft();
        int top   = getTop();
        int width = getWidth();
        int height= getHeight();
        int mwidth= getMeasuredWidth();
        int mheight=getMeasuredHeight();


        float bottomLineY = height - (convertDpToPixel(BOTTOM_PADDING) * 2);

        if(graphLinePaint == null) return;

        //y측 기준 0, middle, max만 표시할 예정
        for(int i=0; i<3; i++){
            int baseValue = 0;
            float baseHeight = (gHeight - convertDpToPixel(BOTTOM_PADDING)) ;
            if(i==1) {
                baseValue = maxStepCount / 2;
                baseHeight = (gHeight - convertDpToPixel(BOTTOM_PADDING)) / 2;
            }else if(i==2) {
                baseValue = maxStepCount;
                baseHeight = convertDpToPixel(TOP_PADDING);
            }
            float textWidth = dateTextPaint.measureText(baseValue+"");
            canvas.drawText(baseValue+"", 0, baseHeight, valueTextPaint);
            canvas.drawLine(
                    convertDpToPixel(LEFT_PADDING),
                    baseHeight,
                    gWidth-convertDpToPixel(RIGHT_PADDING),
                    baseHeight,
                    graphLinePaint);
        }


        float graphHeight = height - convertDpToPixel(BOTTOM_PADDING) - convertDpToPixel(TOP_PADDING);
        float graphItemHeight = (graphHeight / maxStepCount);

        for(int i = 0; i< stepCountDataArrayList.size(); i++){

            float x = (i*itemWidth) + convertDpToPixel(LEFT_PADDING);
            float y = (gHeight - (stepCountDataArrayList.get(i).getStepCount() * itemHeight)) - convertDpToPixel(BOTTOM_PADDING);

            //일자
            String dateStr = "";
            if((i+1) % 6 == 0){
                dateStr = (i+1)+"";
            }

            float dateTextWidth = dateTextPaint.measureText(dateStr);
            canvas.drawText(dateStr, x, gHeight, dateTextPaint);

            float dotYValue = stepCountDataArrayList.get(i).getStepCount();
            if(dotYValue >= maxStepCount){
                dotYValue = maxStepCount;
            }
            float dotY = ((maxStepCount - (dotYValue)) * graphItemHeight) + convertDpToPixel(TOP_PADDING);

            Path path = getPath(convertDpToPixel(BAR_WIDTH), true, true, false, false,
                    x, dotY, x, graphHeight + convertDpToPixel(TOP_PADDING));
            canvas.drawPath(path, barPaint);
        }




    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        gWidth = width;
        gHeight = height;

        setMeasuredDimension(width, height);

        itemWidth = (gWidth - convertDpToPixel(LEFT_PADDING) - convertDpToPixel(RIGHT_PADDING)) / (float) stepCountDataArrayList.size();
        itemHeight = (gHeight - convertDpToPixel(TOP_PADDING) - convertDpToPixel(BOTTOM_PADDING)) / (float) maxStepCount;
        Log.e("ssshin", "asdfasdf:"+itemWidth + ">>"+gWidth+">>"+stepCountDataArrayList.size());

        invalidate();

    }

    
    //둥근부분이 부분일 경우 사용
    private Path getPath(float radius, boolean topLeft, boolean topRight,
                         boolean bottomRight, boolean bottomLeft, float startX, float startY, float endX, float endY) {

        final Path path = new Path();
        final float[] radii = new float[8];

        if (topLeft) {
            radii[0] = radius;
            radii[1] = radius;
        }

        if (topRight) {
            radii[2] = radius;
            radii[3] = radius;
        }

        if (bottomRight) {
            radii[4] = radius;
            radii[5] = radius;
        }

        if (bottomLeft) {
            radii[6] = radius;
            radii[7] = radius;
        }

        path.addRoundRect(new RectF(startX, startY, endX+convertDpToPixel(BAR_WIDTH), endY),
                radii, Path.Direction.CW);

        return path;
    }

    private int getShowStepValue(int maxStep){
        int ret = 0;
        if(maxStep > 1000){
            if(maxStep > 1000 && maxStep <= 2000){
                ret = 500;
            }else if(maxStep > 2000 && maxStep <= 5000){
                ret = 2500;
            }else if(maxStep > 5000 && maxStep <= 10000){
                ret = 10000;
            }else if(maxStep > 10000 && maxStep <= 15000){
                ret = 15000;
            }else if(maxStep > 15000 && maxStep <= 20000){
                ret = 20000;
            }else if(maxStep > 20000 && maxStep <= 30000){
                ret = 30000;
            }
        }else{
            ret = 500;
        }

        return ret;
    }
    
    //dp를 px로 변환 (dp를 입력받아 px을 리턴)
    public float convertDpToPixel(float dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }


}

package com.seers.servicecheck.graph;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BloodPressHorizontalGraph extends View {

    private Context context;

    private final MeasurementType measurementType = MeasurementType.BLOOD_PRESS;

    private final int ITEM_WIDTH = 60;
    private final int TOP_PADDING = 20;
    private final int BOTTOM_PADDING = 20;
    private final int BAR_WIDTH = 7;
    private final int TEXT_SIZE = 10;
    
    private Paint valueTextPaint;       //값(평균값)
    private Paint dateTextPaint;        //일자
    private Paint timeTextPaint;        //시간
    private Paint graphLinePaint;       //line
    private Paint systolicGraphBarPaint;    //수축기
    private Paint diastolicGraphBarPaint;   //이완기

    private ArrayList<BloodPressGraphModel> bloodPressGraphModels = new ArrayList<>();

    public BloodPressHorizontalGraph(Context context) {
        super(context);
        this.context = context;
    }

    public BloodPressHorizontalGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void settingData(ArrayList<BloodPressGraphModel> bloodPressGraphModels){
        this.bloodPressGraphModels = bloodPressGraphModels;
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


        systolicGraphBarPaint = new Paint(0);
        systolicGraphBarPaint.setStrokeWidth(convertDpToPixel(20));
        systolicGraphBarPaint.setColor(Color.parseColor("#e57373"));

        diastolicGraphBarPaint = new Paint(0);
        diastolicGraphBarPaint.setStrokeWidth(convertDpToPixel(20));
        diastolicGraphBarPaint.setColor(Color.parseColor("#90caf9"));

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

        //하단 라인
        canvas.drawLine(0, bottomLineY, width, bottomLineY, graphLinePaint);


        for(int i = 0; i< bloodPressGraphModels.size(); i++){
            float x = ((i*convertDpToPixel(ITEM_WIDTH)) + ((i+1)*convertDpToPixel(ITEM_WIDTH))) / 2;
            float y = height - convertDpToPixel(BOTTOM_PADDING);

            //일자
            String dateStr = bloodPressGraphModels.get(i).getDateStr();
            String timeStr = "";
            SimpleDateFormat dateParser  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try{
                Date date = dateParser.parse(dateStr);

                SimpleDateFormat sdfDate = new SimpleDateFormat("MM월dd일");
                SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

                dateStr = sdfDate.format(date);
                timeStr = sdfTime.format(date);
            }catch (ParseException e){
                e.printStackTrace();
            }
            float dateTextWidth = dateTextPaint.measureText(dateStr);
            canvas.drawText(dateStr, x-(dateTextWidth/2), y, dateTextPaint);

            float timeTextWidth = timeTextPaint.measureText(timeStr);
            canvas.drawText(timeStr, x-(timeTextWidth/2), y+40, timeTextPaint);


            //체온 dot(범위: 30~44)
            float graphHeight = height - (convertDpToPixel(BOTTOM_PADDING) * 2) - convertDpToPixel(TOP_PADDING);
            float graphItemHeight = (graphHeight / (measurementType.getMaxValue() - measurementType.getMinValue()));
            float dotAmY = ((measurementType.getMaxValue() - (bloodPressGraphModels.get(i).getSystolicValue())) * graphItemHeight) + convertDpToPixel(TOP_PADDING);
            float dotPmY = ((measurementType.getMaxValue() - (bloodPressGraphModels.get(i).getDiastolicValue())) * graphItemHeight) + convertDpToPixel(TOP_PADDING);
            if(bloodPressGraphModels.get(i).getSystolicValue() >= measurementType.getMaxValue()){
                dotAmY = measurementType.getMaxValue();
            }else if(bloodPressGraphModels.get(i).getSystolicValue() <= measurementType.getMinValue()){
                dotAmY = measurementType.getMinValue()+1;
            }
            if(bloodPressGraphModels.get(i).getDiastolicValue() >= measurementType.getMaxValue()){
                dotPmY = measurementType.getMaxValue();
            }else if(bloodPressGraphModels.get(i).getDiastolicValue() <= measurementType.getMinValue()){
                dotPmY = measurementType.getMinValue()+1;
            }

            //수축기
//            if(bloodPressGraphModels.get(i).getSystolicValue() >= 97) {
//                systolicGraphBarPaint.setColor(Color.parseColor("#DBDBDB"));
//            }else if(bloodPressGraphModels.get(i).getSystolicValue() < 97 && bloodPressGraphModels.get(i).getSystolicValue() >= 90){
//                systolicGraphBarPaint.setColor(Color.parseColor("#888888"));
//            }else{
//                systolicGraphBarPaint.setColor(Color.parseColor("#D56868"));
//            }
            Path path = getPath(convertDpToPixel(BAR_WIDTH), true, true, false, false,
                    x - 20, dotAmY, x - 20, graphHeight + convertDpToPixel(TOP_PADDING));
            canvas.drawPath(path, systolicGraphBarPaint);

            //이완기
//            if(bloodPressGraphModels.get(i).getDiastolicValue() >= 97) {
//                diastolicGraphBarPaint.setColor(Color.parseColor("#DBDBDB"));
//            }else if(bloodPressGraphModels.get(i).getDiastolicValue() < 97 && bloodPressGraphModels.get(i).getDiastolicValue() >= 90){
//                diastolicGraphBarPaint.setColor(Color.parseColor("#888888"));
//            }else{
//                diastolicGraphBarPaint.setColor(Color.parseColor("#D56868"));
//            }
            Path path2 = getPath(convertDpToPixel(BAR_WIDTH), true, true, false, false,
                    x + 20, dotPmY, x + 20, graphHeight + convertDpToPixel(TOP_PADDING));
            //canvas.clipPath(path);
            canvas.drawPath(path2, diastolicGraphBarPaint);

            //value
//            int average = Math.round((bloodPressGraphModels.get(i).getSystolicValue() + bloodPressGraphModels.get(i).getDiastolicValue()) / 2);
//            if(average >= 97) {
//                valueTextPaint.setColor(Color.parseColor("#DBDBDB"));
//            }else if(average < 97 && average >= 90){
//                valueTextPaint.setColor(Color.parseColor("#888888"));
//            }else{
//                valueTextPaint.setColor(Color.parseColor("#D56868"));
//            }
//
//            float temperatureTextWidth = dateTextPaint.measureText(average+"");
//            canvas.drawText(average+"",
//                    x-(temperatureTextWidth/2), 50, valueTextPaint);
            String value = Math.round(bloodPressGraphModels.get(i).getSystolicValue())+"/"+Math.round(bloodPressGraphModels.get(i).getDiastolicValue());
            float textWidth = dateTextPaint.measureText(value);
            canvas.drawText(value+"",
                    x-(textWidth/2), 50, valueTextPaint);


        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth() + ((int)convertDpToPixel(ITEM_WIDTH) * (bloodPressGraphModels.size()));
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        if(widthMeasureSpec > minw){
            w = resolveSizeAndState(widthMeasureSpec, widthMeasureSpec, 1);
        }

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = MeasureSpec.getSize(w) - (int)100 + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(MeasureSpec.getSize(w) - (int)100, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);

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

    //dp를 px로 변환 (dp를 입력받아 px을 리턴)
    public float convertDpToPixel(float dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}

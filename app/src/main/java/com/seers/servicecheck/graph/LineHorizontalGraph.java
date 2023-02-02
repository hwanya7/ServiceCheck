package com.seers.servicecheck.graph;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LineHorizontalGraph extends View {

    private Context context;

    private final int ITEM_WIDTH = 60;
    private final int TOP_PADDING = 20;
    private final int BOTTOM_PADDING = 20;
    private final int TEXT_SIZE = 10;
    private final int RADIUS = 3;

    //측정 타입 구분
    //float minValue, float maxValue, float lowDangerValue, float highDangerValue, float textValueLocation
    private MeasurementType measurementType = null;

    private int minValue = 0;
    private int maxValue = 0;

    private Paint valueTextPaint;       //값(평균값)
    private Paint dateTextPaint;        //일자
    private Paint timeTextPaint;        //시간
    private Paint graphLinePaint;
    private Paint graphDotPaint;
    private Paint graphDotConnectPaint;

    private ArrayList<MeasurementGraphModel> measurementGraphModels = new ArrayList<>();

    public LineHorizontalGraph(Context context) {
        super(context);
        this.context = context;
    }

    public LineHorizontalGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void settingData(MeasurementType measurementType, ArrayList<MeasurementGraphModel> measurementGraphModels){
        this.measurementType = measurementType;
        this.measurementGraphModels = measurementGraphModels;
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

        graphDotPaint = new Paint(0);
        graphDotPaint.setColor(Color.parseColor("#000000"));

        graphDotConnectPaint = new Paint(0);
        graphDotConnectPaint.setStrokeWidth(convertDpToPixel(RADIUS));
        graphDotConnectPaint.setColor(Color.parseColor("#DBDBDB"));

        requestLayout();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(measurementType != null){
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

            ArrayList<TemperatureDotLocationModel> dotLocationList = new ArrayList<>(); //dot을 마지막에 표시하기 위함
            TemperatureDotLocationModel beforeTemperatureDotLocationModel = null;

            float textLocationY = 0f;

            for(int i = 0; i< measurementGraphModels.size(); i++){
                float x = ((i*convertDpToPixel(ITEM_WIDTH)) + ((i+1)*convertDpToPixel(ITEM_WIDTH))) / 2;
                float y = height - convertDpToPixel(BOTTOM_PADDING);

                //일자
                /*float dateTextWidth = dateTextPaint.measureText(measurementGraphModels.get(i).getDateStr());
                canvas.drawText(measurementGraphModels.get(i).getDateStr(), x-(dateTextWidth/2), y, dateTextPaint);*/

                //일자
                String dateStr = measurementGraphModels.get(i).getDateStr();
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

                //세로 line
                canvas.drawLine(x , TOP_PADDING, x, bottomLineY, graphLinePaint);

                //min max를 이용해서 처리(예: 체온 30~44)
                float graphHeight = height - (convertDpToPixel(BOTTOM_PADDING) * 2) - convertDpToPixel(TOP_PADDING);
                float graphItemHeight = (graphHeight / (measurementType.getMaxValue() - measurementType.getMinValue()));
                float dotYValue = measurementGraphModels.get(i).getValue();
                if(measurementGraphModels.get(i).getValue() >= measurementType.getMaxValue()){
                    dotYValue = measurementType.getMaxValue();
                }else if(measurementGraphModels.get(i).getValue() <= measurementType.getMinValue()){
                    dotYValue = measurementType.getMinValue()+1;
                }

                float dotY = (measurementType.getMaxValue() - (dotYValue)) * graphItemHeight + convertDpToPixel(TOP_PADDING);

                dotLocationList.add(new TemperatureDotLocationModel(x, dotY, measurementGraphModels.get(i).getValue()));

                if(i > 0){
                    if(beforeTemperatureDotLocationModel != null){
                        canvas.drawLine(beforeTemperatureDotLocationModel.getX(), beforeTemperatureDotLocationModel.getY(), x, dotY, graphDotConnectPaint);
                    }
                }

                //데이터 저장
                beforeTemperatureDotLocationModel = new TemperatureDotLocationModel(x, dotY, 0f);

            }

            //dot 표시를 나중에 함
            for(int i = 0; i< measurementGraphModels.size(); i++){
                float radius = 15;
                //measurementType.getHighDangerValue() 값이 -1일 경우는 사용하지 않는 다는 뜻임
                if(measurementGraphModels.get(i).getValue() > measurementType.getHighDangerValue()
                        && measurementType.getHighDangerValue() > 0) { //사용여부 판단 0보다 커야 사용한다
                    graphDotPaint.setColor(Color.parseColor("#FF0000"));
                    valueTextPaint.setColor(Color.parseColor("#FF0000"));
                }else if(measurementGraphModels.get(i).getValue() < measurementType.getLowDangerValue()
                        && measurementType.getLowDangerValue() > 0){ //사용여부 판단 0보다 커야 사용한다
                    graphDotPaint.setColor(Color.parseColor("#FF0000"));
                    valueTextPaint.setColor(Color.parseColor("#FF0000"));
                }else{
                    graphDotPaint.setColor(Color.parseColor("#000000"));
                    valueTextPaint.setColor(Color.parseColor("#000000"));
                }

                //dot 표시
                canvas.drawCircle(dotLocationList.get(i).getX(), dotLocationList.get(i).getY(), convertDpToPixel(RADIUS), graphDotPaint);

                //value값 표시(체온만 소수점)
                /*float temperatureTextWidth = dateTextPaint.measureText(measurementGraphModels.get(i).getDateStr());
                if(measurementType == MeasurementType.TEMPERATURE){
                    canvas.drawText(measurementGraphModels.get(i).getValue()+"",
                            dotLocationList.get(i).getX()-(temperatureTextWidth/2), textLocationY, tempTextPaint);
                }else{
                    canvas.drawText(Math.round(measurementGraphModels.get(i).getValue())+"",
                            dotLocationList.get(i).getX()-(temperatureTextWidth/2), textLocationY, tempTextPaint);
                }*/

                String value = Math.round(measurementGraphModels.get(i).getValue())+"";
                if(measurementType == MeasurementType.TEMPERATURE){
                    value = measurementGraphModels.get(i).getValue()+"";
                }else{
                    value = Math.round(measurementGraphModels.get(i).getValue())+"";
                }
                float textWidth = dateTextPaint.measureText(value);
                canvas.drawText(value+"",
                        dotLocationList.get(i).getX()-(textWidth/2), convertDpToPixel(TOP_PADDING/2), valueTextPaint);

            }

        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth() + ((int)convertDpToPixel(ITEM_WIDTH) * (measurementGraphModels.size()));
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);


        if(widthMeasureSpec > minw){
            w = resolveSizeAndState(widthMeasureSpec, widthMeasureSpec, 1);
        }



        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = MeasureSpec.getSize(w) - (int)getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0);

        setMeasuredDimension(w, h);

        invalidate();

    }

    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    //dp를 px로 변환 (dp를 입력받아 px을 리턴)
    public float convertDpToPixel(float dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}

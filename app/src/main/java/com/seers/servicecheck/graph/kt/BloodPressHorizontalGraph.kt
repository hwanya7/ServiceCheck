package com.seers.homemonitoring.view.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import com.seers.homemonitoring.data_class.BloodPressGraphModel
import com.seers.homemonitoring.data_class.MeasurementType
import com.seers.homemonitoring.data_class.DotLocationModel
import java.text.ParseException
import java.text.SimpleDateFormat
import kotlin.math.roundToInt


class BloodPressHorizontalGraph : View {
    private val ITEM_WIDTH = 50
    private val TOP_PADDING = 30
    private val LEFT_PADDING = 60
    private val BOTTOM_PADDING = 40
    private val TEXT_SIZE = 10
    private val TEXT_DATE_SIZE = 8
    private val RADIUS = 4

    private val isVisibleVerticalLine = false  //세로 라인 표시 여부
    private val isVisibleTemperatureValue = false
    private val isChangeDotColor = false        //dot 컬러 변경 여부

    //측정 타입 구분
    //float minValue, float maxValue, float lowDangerValue, float highDangerValue, float textValueLocation
    private var measurementType: MeasurementType? = null
    private val minValue = 0
    private val maxValue = 0
    private var valueLineTextPaint: Paint? = null
    private var valueTextPaint: Paint? = null
    private var dateTextPaint: Paint? = null
    private var timeTextPaint: Paint? = null
    private var graphLinePaint: Paint? = null
    private var graphDotPaint: Paint? = null
    private var graphDotSysConnectPaint: Paint? = null
    private var graphDotDiaConnectPaint: Paint? = null
    private var graphHorizontalLinePaint: Paint? = null
    private var graphHorizontalMaxAlertLinePaint: Paint? = null
    private var graphHorizontalMinAlertLinePaint: Paint? = null
    private var graphMaxMinBoxPaint: Paint? = null
    private var measurementGraphModels: ArrayList<BloodPressGraphModel> = ArrayList()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun settingData(
        measurementType: MeasurementType?,
        measurementGraphModels: ArrayList<BloodPressGraphModel>
    ) {
        this.measurementType = measurementType
        this.measurementGraphModels = measurementGraphModels
        init()
    }

    private fun init() {
        valueLineTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        valueLineTextPaint!!.color = Color.parseColor("#000000")
        valueLineTextPaint!!.textSize = convertDpToPixel(TEXT_SIZE.toFloat())
        valueTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        valueTextPaint!!.color = Color.parseColor("#797979")
        valueTextPaint!!.textSize = convertDpToPixel(TEXT_SIZE.toFloat())
        dateTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dateTextPaint!!.color = Color.parseColor("#000000")
        dateTextPaint!!.textSize = convertDpToPixel(TEXT_DATE_SIZE.toFloat())
        timeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        timeTextPaint!!.color = Color.parseColor("#000000")
        timeTextPaint!!.textSize = convertDpToPixel(TEXT_SIZE.toFloat())
        graphLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        graphLinePaint!!.strokeWidth = 1f
        graphLinePaint!!.color = Color.parseColor("#EDEDED")
        graphDotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        graphDotPaint!!.color = Color.parseColor("#000000")
        graphDotSysConnectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        graphDotSysConnectPaint!!.strokeWidth = convertDpToPixel(RADIUS.toFloat())
        graphDotSysConnectPaint!!.color = Color.parseColor("#E63118")
        graphDotDiaConnectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        graphDotDiaConnectPaint!!.strokeWidth = convertDpToPixel(RADIUS.toFloat())
        graphDotDiaConnectPaint!!.color = Color.parseColor("#3E66F7")

        graphHorizontalLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        graphHorizontalLinePaint!!.strokeWidth = 1f
        graphHorizontalLinePaint!!.color = Color.parseColor("#EAEAEA")
        graphHorizontalMaxAlertLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        graphHorizontalMaxAlertLinePaint!!.strokeWidth = 1f
        graphHorizontalMaxAlertLinePaint!!.color = Color.parseColor("#FF0000")
        graphHorizontalMinAlertLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        graphHorizontalMinAlertLinePaint!!.strokeWidth = 1f
        graphHorizontalMinAlertLinePaint!!.color = Color.parseColor("#0776FF")
        graphMaxMinBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        graphMaxMinBoxPaint!!.color = Color.parseColor("#F5F7FD")

        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (measurementType != null) {
            val left = left
            val top = top
            val width = width
            val height = height
            val mwidth = measuredWidth
            val mheight = measuredHeight
            val bottomLineY = height - convertDpToPixel(BOTTOM_PADDING.toFloat())
            if (graphLinePaint == null) return

            var tempSize = (measurementType!!.maxValue.toInt() - measurementType!!.minValue.toInt())
            val graphHeight = height - convertDpToPixel(BOTTOM_PADDING.toFloat()) - convertDpToPixel(TOP_PADDING.toFloat())
            val graphItemHeight: Float = graphHeight / tempSize



            val dotSysLocationList: ArrayList<DotLocationModel> = ArrayList() //dot을 마지막에 표시하기 위함
            var beforeDotSysLocationModel: DotLocationModel? = null
            val dotDiaLocationList: ArrayList<DotLocationModel> = ArrayList() //dot을 마지막에 표시하기 위함
            var beforeDotDiaLocationModel: DotLocationModel? = null
            val textLocationY = 0f

            //min, max 색 박스
            canvas.drawRect(
                convertDpToPixel(LEFT_PADDING.toFloat()),
                (convertDpToPixel(TOP_PADDING.toFloat())+(graphItemHeight*(measurementType!!.maxValue - measurementType!!.highDangerValue) )),
                width.toFloat(),
                (convertDpToPixel(TOP_PADDING.toFloat())+(graphItemHeight*(measurementType!!.maxValue - measurementType!!.lowDangerValue) )),
                graphMaxMinBoxPaint!!
            )

            //체온 라인(가로), 텍스트
            //min max를 이용해서 처리(예: 체온 33~40)
            val leftTextPadding = 5
            for (i in 0 .. (tempSize / 10).toInt()) {
                var verticalStr = (measurementType!!.maxValue - (i*10)).toInt().toString()
                val verticalTextWidth = timeTextPaint!!.measureText(verticalStr)
                val bounds = Rect()
                timeTextPaint!!.getTextBounds(verticalStr, 0, verticalStr.length, bounds)
                val timeTextHeight: Int = bounds.height()
                canvas.drawText(verticalStr, (convertDpToPixel(LEFT_PADDING.toFloat()) - verticalTextWidth  - convertDpToPixel(leftTextPadding.toFloat())), (convertDpToPixel(TOP_PADDING.toFloat())+((graphItemHeight*i*10) + (timeTextHeight/2))), timeTextPaint!!)
                canvas.drawLine(convertDpToPixel(LEFT_PADDING.toFloat()), (convertDpToPixel(TOP_PADDING.toFloat())+(graphItemHeight*i*10)), width.toFloat(), (convertDpToPixel(TOP_PADDING.toFloat())+(graphItemHeight*i*10)), graphHorizontalLinePaint!!)
            }

            //단위
            val unit = "mmHg"
            val unitTextWidth = dateTextPaint!!.measureText(unit)
            canvas.drawText(unit,  (convertDpToPixel(LEFT_PADDING.toFloat()) - unitTextWidth  - convertDpToPixel(leftTextPadding.toFloat())), 15f, dateTextPaint!!)
            
            //max경고 라인
            canvas.drawLine(
                convertDpToPixel(LEFT_PADDING.toFloat()),
                (convertDpToPixel(TOP_PADDING.toFloat())+(graphItemHeight*(measurementType!!.maxValue - measurementType!!.highDangerValue))),
                width.toFloat(),
                (convertDpToPixel(TOP_PADDING.toFloat())+(graphItemHeight*(measurementType!!.maxValue - measurementType!!.highDangerValue))),
                graphHorizontalMaxAlertLinePaint!!
            )
            //min경고 라인
            canvas.drawLine(
                convertDpToPixel(LEFT_PADDING.toFloat()),
                (convertDpToPixel(TOP_PADDING.toFloat())+(graphItemHeight*(measurementType!!.maxValue - measurementType!!.lowDangerValue))),
                width.toFloat(),
                (convertDpToPixel(TOP_PADDING.toFloat())+(graphItemHeight*(measurementType!!.maxValue - measurementType!!.lowDangerValue))),
                graphHorizontalMinAlertLinePaint!!
            )

            
            var tempDateStr = ""
            for (i in measurementGraphModels.indices) {
                val x = (i * convertDpToPixel(ITEM_WIDTH.toFloat()) + (i + 1) * (convertDpToPixel(ITEM_WIDTH.toFloat())) / 2) + convertDpToPixel(LEFT_PADDING.toFloat())
                val y = height - convertDpToPixel(BOTTOM_PADDING.toFloat())
                
                //일자
                var dateStr: String = measurementGraphModels[i].dateStr
                var timeStr = ""
                val dateParser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                try {
                    val date = dateParser.parse(dateStr)
                    val sdfDate = SimpleDateFormat("M/d")
                    val sdfTime = SimpleDateFormat("HH:mm")
                    dateStr = sdfDate.format(date)
                    //timeStr = sdfTime.format(date)
                    timeStr = measurementGraphModels[i].round.toString()+"차"
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                if(tempDateStr != dateStr){
                    val dateTextWidth = dateTextPaint!!.measureText(dateStr)
                    canvas.drawText(dateStr, x - dateTextWidth / 2, height.toFloat() - (convertDpToPixel(BOTTOM_PADDING.toFloat())/2), dateTextPaint!!)
                }

                val timeTextWidth = timeTextPaint!!.measureText(timeStr)
                canvas.drawText(timeStr, x - timeTextWidth / 2, height.toFloat(), timeTextPaint!!)

                if(isVisibleVerticalLine){
                    //세로 line
                    canvas.drawLine(x, convertDpToPixel(TOP_PADDING.toFloat()), x, bottomLineY, graphLinePaint!!)
                }

                //수축기
                var dotSysYValue: Float = measurementGraphModels[i].systolicValue
                if (measurementGraphModels[i].systolicValue >= measurementType!!.maxValue) {
                    dotSysYValue = measurementType!!.maxValue
                } else if (measurementGraphModels[i].systolicValue <= measurementType!!.minValue) {
                    dotSysYValue = measurementType!!.minValue + 1
                }

                val dotSysY: Float = (((measurementType!!.maxValue - dotSysYValue) * graphItemHeight)) + convertDpToPixel(TOP_PADDING.toFloat())
                dotSysLocationList.add(DotLocationModel(x, dotSysY, measurementGraphModels[i].systolicValue))
                if (i > 0) {
                    if (beforeDotSysLocationModel != null) {
                        canvas.drawLine(
                            beforeDotSysLocationModel.x,
                            beforeDotSysLocationModel.y,
                            x,
                            dotSysY,
                            graphDotSysConnectPaint!!
                        )
                    }
                }

                //이완기
                var dotDiaYValue: Float = measurementGraphModels[i].diastolicValue
                if (measurementGraphModels[i].diastolicValue >= measurementType!!.maxValue) {
                    dotDiaYValue = measurementType!!.maxValue
                } else if (measurementGraphModels[i].diastolicValue <= measurementType!!.minValue) {
                    dotDiaYValue = measurementType!!.minValue + 1
                }
                val dotDiaY: Float = (((measurementType!!.maxValue - dotDiaYValue) * graphItemHeight)) + convertDpToPixel(TOP_PADDING.toFloat())
                dotDiaLocationList.add(DotLocationModel(x, dotDiaY, measurementGraphModels[i].diastolicValue))
                if (i > 0) {
                    if (beforeDotDiaLocationModel != null) {
                        canvas.drawLine(
                            beforeDotDiaLocationModel.x,
                            beforeDotDiaLocationModel.y,
                            x,
                            dotDiaY,
                            graphDotDiaConnectPaint!!
                        )
                    }
                }

                //데이터 저장
                beforeDotSysLocationModel = DotLocationModel(x, dotSysY, 0f)
                beforeDotDiaLocationModel = DotLocationModel(x, dotDiaY, 0f)

                tempDateStr = dateStr
            }

            //dot 표시를 나중에 함
            for (i in measurementGraphModels.indices) {
                val radius = 15f
                //measurementType.getHighDangerValue() 값이 -1일 경우는 사용하지 않는 다는 뜻임
                
                //todo 색이 안바뀌게 일단 처리해둠
                if(isChangeDotColor){
                    if (measurementGraphModels[i].systolicValue > measurementType!!.highDangerValue
                        && measurementType!!.highDangerValue > 0) { //사용여부 판단 0보다 커야 사용한다
                        graphDotPaint!!.color = Color.parseColor("#FF0000")
                        valueTextPaint!!.color = Color.parseColor("#FF0000")
                    } else if (measurementGraphModels[i].systolicValue < measurementType!!.lowDangerValue
                        && measurementType!!.lowDangerValue > 0) { //사용여부 판단 0보다 커야 사용한다
                        graphDotPaint!!.color = Color.parseColor("#FF0000")
                        valueTextPaint!!.color = Color.parseColor("#FF0000")
                    } else if (measurementGraphModels[i].diastolicValue > measurementType!!.highDangerValue
                        && measurementType!!.highDangerValue > 0) { //사용여부 판단 0보다 커야 사용한다
                        graphDotPaint!!.color = Color.parseColor("#FF0000")
                        valueTextPaint!!.color = Color.parseColor("#FF0000")
                    } else if (measurementGraphModels[i].diastolicValue < measurementType!!.lowDangerValue
                        && measurementType!!.lowDangerValue > 0) { //사용여부 판단 0보다 커야 사용한다
                        graphDotPaint!!.color = Color.parseColor("#FF0000")
                        valueTextPaint!!.color = Color.parseColor("#FF0000")
                    }else {
                        graphDotPaint!!.color = Color.parseColor("#000000")
                        valueTextPaint!!.color = Color.parseColor("#000000")
                    }
                }


                //수축기 dot 표시
                canvas.drawCircle(
                    dotSysLocationList[i].x,
                    dotSysLocationList[i].y,
                    convertDpToPixel(RADIUS.toFloat()),
                    graphDotPaint!!
                )

                //이완기 dot 표시
                canvas.drawCircle(
                    dotDiaLocationList[i].x,
                    dotDiaLocationList[i].y,
                    convertDpToPixel(RADIUS.toFloat()),
                    graphDotPaint!!
                )

                /*//체온값 표시
                if(isVisibleTemperatureValue){
                    var value: String
                    if (measurementType === MeasurementType.TEMPERATURE) {
                        value = measurementGraphModels[i].value.toString()
                    } else {
                        value = measurementGraphModels[i].value.roundToInt().toString()
                    }
                    val textWidth = dateTextPaint!!.measureText(value)
                    canvas.drawText(
                        value + "",
                        dotLocationList[i].x - textWidth / 2,
                        convertDpToPixel((TOP_PADDING / 2).toFloat()),
                        valueTextPaint!!
                    )
                }*/
                
            }



        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // Try for a width based on our minimum
        val minw = paddingLeft + paddingRight + suggestedMinimumWidth + convertDpToPixel(ITEM_WIDTH.toFloat()).toInt() * measurementGraphModels.size
        var w = resolveSizeAndState(minw, widthMeasureSpec, 1)
        if (widthMeasureSpec > minw) {
            w = resolveSizeAndState(widthMeasureSpec, widthMeasureSpec, 1)
        }


        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        val minh = MeasureSpec.getSize(w) - paddingBottom + paddingTop
        val h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)
        setMeasuredDimension(w, h)
        invalidate()
    }

    //dp를 px로 변환 (dp를 입력받아 px을 리턴)
    fun convertDpToPixel(dp: Float): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    companion object {
        fun getDefaultSize(size: Int, measureSpec: Int): Int {
            var result = size
            val specMode = MeasureSpec.getMode(measureSpec)
            val specSize = MeasureSpec.getSize(measureSpec)
            when (specMode) {
                MeasureSpec.UNSPECIFIED -> result = size
                MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> result = specSize
            }
            return result
        }
    }
}
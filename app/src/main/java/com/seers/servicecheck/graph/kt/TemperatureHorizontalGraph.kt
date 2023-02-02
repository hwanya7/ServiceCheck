package com.seers.homemonitoring.view.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import com.seers.homemonitoring.data_class.MeasurementGraphModel
import com.seers.homemonitoring.data_class.MeasurementType
import com.seers.homemonitoring.data_class.DotLocationModel
import java.text.ParseException
import java.text.SimpleDateFormat
import kotlin.math.roundToInt


class TemperatureHorizontalGraph : View {
    private val ITEM_WIDTH = 50
    private val TOP_PADDING = 30
    private val LEFT_PADDING = 60
    private val BOTTOM_PADDING = 40
    private val TEXT_SIZE = 10
    private val TEXT_DATE_SIZE = 8
    private val RADIUS = 4

    private val isVisibleVerticalLine = false  //세로 라인 표시 여부
    private val isVisibleTemperatureValue = false

    //측정 타입 구분
    //float minValue, float maxValue, float lowDangerValue, float highDangerValue, float textValueLocation
    private var measurementType: MeasurementType? = null
    private val minValue = 0
    private val maxValue = 0
    private var temperatureValueLineTextPaint: Paint? = null
    private var valueTextPaint: Paint? = null
    private var dateTextPaint: Paint? = null
    private var timeTextPaint: Paint? = null
    private var graphLinePaint: Paint? = null
    private var graphDotPaint: Paint? = null
    private var graphDotConnectPaint: Paint? = null
    private var graphHorizontalLinePaint: Paint? = null
    private var graphHorizontalMaxAlertLinePaint: Paint? = null
    private var graphHorizontalMinAlertLinePaint: Paint? = null
    private var graphMaxMinBoxPaint: Paint? = null
    private var measurementGraphModels: ArrayList<MeasurementGraphModel> = ArrayList<MeasurementGraphModel>()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun settingData(
        measurementType: MeasurementType?,
        measurementGraphModels: ArrayList<MeasurementGraphModel>
    ) {
        this.measurementType = measurementType
        this.measurementGraphModels = measurementGraphModels
        init()
    }

    private fun init() {
        temperatureValueLineTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        temperatureValueLineTextPaint!!.color = Color.parseColor("#000000")
        temperatureValueLineTextPaint!!.textSize = convertDpToPixel(TEXT_SIZE.toFloat())
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
        graphDotConnectPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        graphDotConnectPaint!!.strokeWidth = convertDpToPixel(RADIUS.toFloat())
        graphDotConnectPaint!!.color = Color.parseColor("#000000")

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

            val graphHeight = height - convertDpToPixel(BOTTOM_PADDING.toFloat()) - convertDpToPixel(TOP_PADDING.toFloat())
            val graphItemHeight: Float = graphHeight / (measurementType!!.maxValue - measurementType!!.minValue)
            var tempSize = measurementType!!.maxValue.toInt() - measurementType!!.minValue.toInt()

            //하단 라인
            val dotLocationList: ArrayList<DotLocationModel> = ArrayList<DotLocationModel>() //dot을 마지막에 표시하기 위함
            var beforeDotLocationModel: DotLocationModel? = null
            val textLocationY = 0f
            //canvas.drawLine(convertDpToPixel(LEFT_PADDING.toFloat()), bottomLineY, width.toFloat(), bottomLineY, graphLinePaint!!)

            //min, max 색 박스
            canvas.drawRect(
                convertDpToPixel(LEFT_PADDING.toFloat()),
                (convertDpToPixel(TOP_PADDING.toFloat())+(graphItemHeight*(measurementType!!.maxValue - measurementType!!.highDangerValue))),
                width.toFloat(),
                (convertDpToPixel(TOP_PADDING.toFloat())+(graphItemHeight*(measurementType!!.maxValue - measurementType!!.lowDangerValue))),
                graphMaxMinBoxPaint!!
            )

            //체온 라인(가로), 텍스트
            //min max를 이용해서 처리(예: 체온 33~40)
            val leftTextPadding = 5
            for (i in 0 .. tempSize) {
                var timeStr = (measurementType!!.maxValue - i).toString()
                val timeTextWidth = timeTextPaint!!.measureText(timeStr)
                val bounds = Rect()
                timeTextPaint!!.getTextBounds(timeStr, 0, timeStr.length, bounds)
                val timeTextHeight: Int = bounds.height()
                canvas.drawText(timeStr, (convertDpToPixel(LEFT_PADDING.toFloat()) - timeTextWidth - convertDpToPixel(leftTextPadding.toFloat())), (convertDpToPixel(TOP_PADDING.toFloat())+((graphItemHeight*i) + (timeTextHeight/2))), timeTextPaint!!)
                canvas.drawLine(convertDpToPixel(LEFT_PADDING.toFloat()), (convertDpToPixel(TOP_PADDING.toFloat())+(graphItemHeight*i)), width.toFloat(), (convertDpToPixel(TOP_PADDING.toFloat())+(graphItemHeight*i)), graphHorizontalLinePaint!!)
            }

            //단위 °C
            val celsius = "°C"
            val celsiusTextWidth = dateTextPaint!!.measureText(celsius)
            canvas.drawText(celsius,  (convertDpToPixel(LEFT_PADDING.toFloat()) - celsiusTextWidth - convertDpToPixel(leftTextPadding.toFloat())), 15f, dateTextPaint!!)
            
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
                    canvas.drawText(dateStr, x - dateTextWidth / 2, height.toFloat()-(convertDpToPixel(BOTTOM_PADDING.toFloat())/2), dateTextPaint!!)
                }

                val timeTextWidth = timeTextPaint!!.measureText(timeStr)
                canvas.drawText(timeStr, x - timeTextWidth / 2, height.toFloat(), timeTextPaint!!)

                if(isVisibleVerticalLine){
                    //세로 line
                    canvas.drawLine(x, convertDpToPixel(TOP_PADDING.toFloat()), x, bottomLineY, graphLinePaint!!)
                }

                var dotYValue: Float = measurementGraphModels[i].value
                if (measurementGraphModels[i].value >= measurementType!!.maxValue) {
                    dotYValue = measurementType!!.maxValue
                } else if (measurementGraphModels[i].value <= measurementType!!.minValue) {
                    dotYValue = measurementType!!.minValue + 1
                }
                val dotY: Float = (measurementType!!.maxValue - dotYValue) * graphItemHeight + convertDpToPixel(TOP_PADDING.toFloat())
                dotLocationList.add(DotLocationModel(x, dotY, measurementGraphModels[i].value))
                if (i > 0) {
                    if (beforeDotLocationModel != null) {
                        canvas.drawLine(
                            beforeDotLocationModel.x,
                            beforeDotLocationModel.y,
                            x,
                            dotY,
                            graphDotConnectPaint!!
                        )
                    }
                }

                //데이터 저장
                beforeDotLocationModel = DotLocationModel(x, dotY, 0f)

                tempDateStr = dateStr
            }

            //dot 표시를 나중에 함
            for (i in measurementGraphModels.indices) {
                val radius = 15f
                //measurementType.getHighDangerValue() 값이 -1일 경우는 사용하지 않는 다는 뜻임
                if (measurementGraphModels[i].value > measurementType!!.highDangerValue
                    && measurementType!!.highDangerValue > 0) { //사용여부 판단 0보다 커야 사용한다
                    graphDotPaint!!.color = Color.parseColor("#FF0000")
                    valueTextPaint!!.color = Color.parseColor("#FF0000")
                } else if (measurementGraphModels[i].value < measurementType!!.lowDangerValue
                    && measurementType!!.lowDangerValue > 0) { //사용여부 판단 0보다 커야 사용한다
                    graphDotPaint!!.color = Color.parseColor("#FF0000")
                    valueTextPaint!!.color = Color.parseColor("#FF0000")
                } else {
                    graphDotPaint!!.color = Color.parseColor("#000000")
                    valueTextPaint!!.color = Color.parseColor("#000000")
                }

                //dot 표시
                canvas.drawCircle(
                    dotLocationList[i].x,
                    dotLocationList[i].y,
                    convertDpToPixel(RADIUS.toFloat()),
                    graphDotPaint!!
                )

                //체온값 표시
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
                }
                
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
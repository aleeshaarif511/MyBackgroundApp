package com.example.imscgmandroidapplication

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class MainActivity : ComponentActivity() {
    private var timeIndex = 0f
    private lateinit var heartRateChart: LineChart
    private lateinit var glucoseChartOne: LineChart
    private lateinit var glucoseChartTwo: LineChart
    private lateinit var glucoseChartThree: LineChart

    private lateinit var heartRateDataSet: LineDataSet
    private lateinit var glucoseOneDataSet: LineDataSet
    private lateinit var glucoseTwoDataSet: LineDataSet
    private lateinit var glucoseThreeDataSet: LineDataSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.dashboard)

            intializeCharts()

            sensorOneChart()
            sensorTwoChart()
            sensorThreeChart()
            sensorFourChart()
    }

    fun intializeCharts() {
        heartRateChart = findViewById<LineChart>(R.id.heartRateChart)
        heartRateChart.setExtraOffsets(0f,16f,0f,0f)

        glucoseChartOne = findViewById<LineChart>(R.id.glucoseLevelChartOne)
        glucoseChartOne.setExtraOffsets(0f,16f,0f,0f)

        glucoseChartTwo = findViewById<LineChart>(R.id.glucoseLevelChartTwo)
        glucoseChartTwo.setExtraOffsets(0f,16f,0f,0f)

        glucoseChartThree = findViewById<LineChart>(R.id.glucoseLevelChartThree)
        glucoseChartThree.setExtraOffsets(0f,16f,0f,0f)

    }

    fun sensorOneChart() {

        val entries = mutableListOf<Entry>()
        heartRateDataSet = LineDataSet(entries, "Heart Rate")
        heartRateDataSet.color = ContextCompat.getColor(this@MainActivity,R.color.heart_rate_color)
        heartRateDataSet.fillColor = ContextCompat.getColor(this@MainActivity,R.color.heart_rate_color)
        heartRateDataSet.setDrawCircles(false)
        heartRateDataSet.lineWidth = 2f
        heartRateDataSet.setDrawFilled(true)

        val lineData = LineData(heartRateDataSet)
        heartRateChart.data = lineData
        heartRateChart.description.text = "Heartbeat over 3 hours"
        heartRateChart.xAxis.labelRotationAngle = -45f
        heartRateChart.animateX(1500)

        val yAxis = heartRateChart.axisLeft
        yAxis.axisMinimum = 40f
        yAxis.axisMaximum = 180f
        heartRateChart.axisRight.isEnabled = false

        val restingLimit = LimitLine(60f, "Resting HR").apply {
            lineColor = Color.GRAY
            lineWidth = 2f
            textColor = Color.DKGRAY
            textSize = 12f
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
        }

        val maxLimit = LimitLine(160f, "Max HR").apply {
            lineColor = Color.RED
            lineWidth = 2f
            textColor = Color.RED
            textSize = 12f
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        }

        yAxis.removeAllLimitLines()
        yAxis.addLimitLine(restingLimit)
        yAxis.addLimitLine(maxLimit)
        yAxis.setDrawLimitLinesBehindData(true)
        heartRateChart.invalidate()

        startLiveUpdates()
    }

    fun startLiveUpdates() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val randomHeartRate = (60..160).random().toFloat()
                heartRateDataSet.addEntry(Entry(timeIndex, randomHeartRate))
                heartRateChart.data.notifyDataChanged()
                heartRateChart.notifyDataSetChanged()
                heartRateChart.invalidate()

                timeIndex += 5f / 60f // 5 seconds as fraction of an hour

                handler.postDelayed(this, 2000)
            }
        }
        handler.post(runnable)
    }

    fun sensorTwoChart() {

        val entries = mutableListOf<Entry>()
        glucoseOneDataSet = LineDataSet(entries, "Glucose Level")
        glucoseOneDataSet.color = ContextCompat.getColor(this@MainActivity,R.color.glucose_one_color)
        glucoseOneDataSet.fillColor = ContextCompat.getColor(this@MainActivity,R.color.glucose_one_color)
        glucoseOneDataSet.setDrawCircles(false)
        glucoseOneDataSet.lineWidth = 2f
        glucoseOneDataSet.setDrawFilled(true)

        val lineData = LineData(glucoseOneDataSet)
        glucoseChartOne.data = lineData
        glucoseChartOne.description.text = "Glucose Level over 3 hours"
        glucoseChartOne.xAxis.labelRotationAngle = -45f
        glucoseChartOne.animateX(1500)

        // Set Y-axis range and limit lines
        val yAxis = glucoseChartOne.axisLeft
        yAxis.axisMinimum = 40f
        yAxis.axisMaximum = 180f
        glucoseChartOne.axisRight.isEnabled = false

        val restingLimit = LimitLine(60f, "Min Limit").apply {
            lineColor = Color.GRAY
            lineWidth = 2f
            textColor = Color.DKGRAY
            textSize = 12f
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
        }

        val maxLimit = LimitLine(160f, "Max Limit").apply {
            lineColor = Color.RED
            lineWidth = 2f
            textColor = Color.RED
            textSize = 12f
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        }

        yAxis.removeAllLimitLines()
        yAxis.addLimitLine(restingLimit)
        yAxis.addLimitLine(maxLimit)
        yAxis.setDrawLimitLinesBehindData(true)
        glucoseChartOne.invalidate()

        startLiveUpdatesGlucose()
    }

    fun sensorThreeChart() {

        val entries = mutableListOf<Entry>()
        glucoseTwoDataSet = LineDataSet(entries, "Glucose Level")
        glucoseTwoDataSet.color = ContextCompat.getColor(this@MainActivity,R.color.glucose_two_color)
        glucoseTwoDataSet.fillColor = ContextCompat.getColor(this@MainActivity,R.color.glucose_two_color)
        glucoseTwoDataSet.setDrawCircles(false)
        glucoseTwoDataSet.lineWidth = 2f
        glucoseTwoDataSet.setDrawFilled(true)

        val lineData = LineData(glucoseOneDataSet)
        glucoseChartTwo.data = lineData
        glucoseChartTwo.description.text = "Glucose Level over 3 hours"
        glucoseChartTwo.xAxis.labelRotationAngle = -45f
        glucoseChartTwo.animateX(1500)

        // Set Y-axis range and limit lines
        val yAxis = glucoseChartTwo.axisLeft
        yAxis.axisMinimum = 40f
        yAxis.axisMaximum = 180f
        glucoseChartTwo.axisRight.isEnabled = false

        val restingLimit = LimitLine(60f, "Min Limit").apply {
            lineColor = Color.GRAY
            lineWidth = 2f
            textColor = Color.DKGRAY
            textSize = 12f
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
        }

        val maxLimit = LimitLine(160f, "Max Limit").apply {
            lineColor = Color.RED
            lineWidth = 2f
            textColor = Color.RED
            textSize = 12f
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        }

        yAxis.removeAllLimitLines()
        yAxis.addLimitLine(restingLimit)
        yAxis.addLimitLine(maxLimit)
        yAxis.setDrawLimitLinesBehindData(true)
        glucoseChartTwo.invalidate()

        startLiveUpdatesGlucose()
    }

    fun sensorFourChart() {

        val entries = mutableListOf<Entry>()
        glucoseThreeDataSet = LineDataSet(entries, "Glucose Level")
        glucoseThreeDataSet.color = ContextCompat.getColor(this@MainActivity,R.color.glucose_three_color)
        glucoseThreeDataSet.fillColor = ContextCompat.getColor(this@MainActivity,R.color.glucose_three_color)
        glucoseThreeDataSet.setDrawCircles(false)
        glucoseThreeDataSet.lineWidth = 2f
        glucoseThreeDataSet.setDrawFilled(true)

        val lineData = LineData(glucoseOneDataSet)
        glucoseChartThree.data = lineData
        glucoseChartThree.description.text = "Glucose Level over 3 hours"
        glucoseChartThree.xAxis.labelRotationAngle = -45f
        glucoseChartThree.animateX(1500)

        // Set Y-axis range and limit lines
        val yAxis = glucoseChartThree.axisLeft
        yAxis.axisMinimum = 40f
        yAxis.axisMaximum = 180f
        glucoseChartThree.axisRight.isEnabled = false

        val restingLimit = LimitLine(60f, "Min Limit").apply {
            lineColor = Color.GRAY
            lineWidth = 2f
            textColor = Color.DKGRAY
            textSize = 12f
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
        }

        val maxLimit = LimitLine(160f, "Max Limit").apply {
            lineColor = Color.RED
            lineWidth = 2f
            textColor = Color.RED
            textSize = 12f
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        }

        yAxis.removeAllLimitLines()
        yAxis.addLimitLine(restingLimit)
        yAxis.addLimitLine(maxLimit)
        yAxis.setDrawLimitLinesBehindData(true)
        glucoseChartThree.invalidate()

        startLiveUpdatesGlucose()
    }

    fun startLiveUpdatesGlucose() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val randomHeartRate = (80..120).random().toFloat()
                glucoseOneDataSet.addEntry(Entry(timeIndex, randomHeartRate))
                glucoseChartOne.data.notifyDataChanged()
                glucoseChartOne.notifyDataSetChanged()
                glucoseChartOne.invalidate()

                glucoseTwoDataSet.addEntry(Entry(timeIndex, randomHeartRate))
                glucoseChartTwo.data.notifyDataChanged()
                glucoseChartTwo.notifyDataSetChanged()
                glucoseChartTwo.invalidate()

                glucoseThreeDataSet.addEntry(Entry(timeIndex, randomHeartRate))
                glucoseChartThree.data.notifyDataChanged()
                glucoseChartThree.notifyDataSetChanged()
                glucoseChartThree.invalidate()

                timeIndex += 5f / 60f // 5 seconds as fraction of an hour

                handler.postDelayed(this, 2000)
            }
        }
        handler.post(runnable)
    }
}
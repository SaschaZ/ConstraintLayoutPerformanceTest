package com.krpiotrek.constraintlayoutstuff

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.performance_test_activity.*
import kotlinx.coroutines.*
import kotlin.math.pow
import kotlin.system.measureNanoTime

class PerformanceTestActivity : AppCompatActivity() {

    companion object {

        const val DEFAULT_TEST_RUNS_PER_LAYOUT = 100
        const val LAYOUTS_TO_TEST = 3
    }

    private val mainJob = Job()
    private val mainScope = CoroutineScope(Dispatchers.Main + mainJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.performance_test_activity)

        numberOfRunsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedTestRunCount.text = "$progress"
            }
        })
        numberOfRunsSeekBar.progress = DEFAULT_TEST_RUNS_PER_LAYOUT
        startTestButton.setOnClickListener {
            mainScope.launch {
                val results = runTests(numberOfRunsSeekBar.progress)
                resultTextView.text = results?.joinToString("\n") {
                    it?.let { duration ->
                        getString(
                            R.string.average_duration_per_run,
                            when (results.indexOf(duration)) {
                                0 -> "ConstraintLayout"
                                1 -> "RelativeLayout"
                                2 -> "LinearLayout"
                                else -> "unknown"
                            },
                            duration.total,
                            duration.inflate,
                            duration.measure,
                            duration.layout,
                            duration.draw
                        )
                    } ?: getString(R.string.test_failed)
                } ?: getString(R.string.test_failed)
            }
        }
    }

    suspend fun runTests(testCount: Int): List<MeasuredTimes?>? {
        startTestButton.isEnabled = false
        numberOfRunsSeekBar.isEnabled = false

        val result = ArrayList<MeasuredTimes?>()
        try {
            repeat(LAYOUTS_TO_TEST) {
                result.add(
                    when (it) {
                        0 -> runTestForLayout(R.layout.item_constraint, testCount, it)
                        1 -> runTestForLayout(R.layout.item_relative, testCount, it)
                        2 -> runTestForLayout(R.layout.item_linear, testCount, it)
                        else -> throw IllegalStateException("Do not have test for id $it")
                    }
                )
            }
        } catch (t: Throwable) {
            return null
        } finally {
            numberOfRunsSeekBar.isEnabled = true
            startTestButton.isEnabled = true
        }
        return result
    }

    private suspend fun runTestForLayout(
        layoutRes: Int,
        testCount: Int,
        testIdx: Int
    ): MeasuredTimes? {
        val measuredTimes = ArrayList<MeasuredTimes>()
        testActiveProgress.visibility = View.VISIBLE
        testProgressPercentTextView.visibility = View.VISIBLE

        try {
            repeat(testCount) {
                container.removeAllViews()
                measuredTimes.add(insertTestView(layoutRes))
                testProgressPercentTextView.text =
                    getString(
                        R.string.test_progress,
                        ((testIdx * testCount + it) / (LAYOUTS_TO_TEST.toDouble() * testCount) * 100).toInt()
                    )
                System.gc()
                delay(1)
            }
        } catch (t: Throwable) {
            return null
        } finally {
            testProgressPercentTextView.visibility = View.GONE
            testActiveProgress.visibility = View.GONE
        }

        return measuredTimes.average()
    }

    data class MeasuredTimes(
        val inflate: Double,
        val measure: Double,
        val layout: Double,
        val draw: Double
    ) {
        constructor(value: Double) : this(value, value, value, value)
        constructor() : this(0.0)
        //        constructor(list: List<Double>) : this(list[0], list[1], list[2], list[3])
        constructor(list: List<Long>) : this(
            list[0] / 10.0.pow(6.0),
            list[1] / 10.0.pow(6.0),
            list[2] / 10.0.pow(6.0),
            list[3] / 10.0.pow(6.0)
        )

        val total: Double
            get() = inflate + measure + layout + draw

        operator fun plus(value: MeasuredTimes) =
            MeasuredTimes(
                inflate + value.inflate, measure + value.measure,
                layout + value.layout, draw + value.draw
            )

        operator fun div(value: Double) =
            MeasuredTimes(
                inflate / value, measure / value, layout / value, draw / value
            )
    }

    private fun List<MeasuredTimes>.average(): MeasuredTimes {
        var timeSum = MeasuredTimes()
        forEach { timeSum += it }
        return timeSum / size.toDouble()
    }

    private fun insertTestView(newLayoutRes: Int): MeasuredTimes {
        val measuredTimes = ArrayList<Long>()
        lateinit var view: View
        measuredTimes.add(measureNanoTime {
            view = LayoutInflater.from(container.context)
                .inflate(newLayoutRes, container, false)
        })
        measuredTimes.add(measureNanoTime {
            view.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY)
        })
        measuredTimes.add(measureNanoTime {
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        })
        measuredTimes.add(measureNanoTime {
            view.draw(Canvas())
        })
        return MeasuredTimes(measuredTimes)
    }
}
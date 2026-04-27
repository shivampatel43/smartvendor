package com.example.smartvendor.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class SalesGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        color = Color.parseColor("#440000FF")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val dataPoints = mutableListOf<Float>()
    private val path = Path()

    fun setData(data: List<Float>) {
        dataPoints.clear()
        dataPoints.addAll(data)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val maxVal = dataPoints.maxOrNull() ?: 1f
        val stepX = width / (dataPoints.size - 1)

        path.reset()
        dataPoints.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / maxVal * height)
            if (index == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }

        canvas.drawPath(path, paint)

        // Draw area under graph
        val fillPath = Path(path)
        fillPath.lineTo(width, height)
        fillPath.lineTo(0f, height)
        fillPath.close()
        canvas.drawPath(fillPath, fillPaint)
    }
}

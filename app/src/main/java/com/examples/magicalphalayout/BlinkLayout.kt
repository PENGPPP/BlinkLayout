package com.examples.magicalphalayout

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.format.DateUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout

class BlinkLayout : ConstraintLayout {

    private val mAlphaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mAlphaStyle: LinearGradient? = null
    private val mXfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    private val mMatrix = Matrix()
    private var mAnimator: Animator? = null

    private var mColors = intArrayOf(Color.TRANSPARENT,
        Color.argb(125, 0, 0, 0),
        Color.BLACK, Color.BLACK, Color.BLACK,
        Color.argb(125, 0, 0, 0),
        Color.TRANSPARENT)

    private var mIgnoreViewCache = mutableListOf<View>()

    private var mAnimationRunnable = Runnable {
        tryStartLoading()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setWillNotDraw(false)
        mAlphaPaint.apply {
            xfermode = mXfermode
            style = Paint.Style.FILL
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        // 重写 dispatchDraw 以便保留背景色绘制
        if (mAlphaStyle == null || canvas == null) {
            super.dispatchDraw(canvas)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), null)
        } else {
            canvas.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), null, Canvas.ALL_SAVE_FLAG);
        }

        super.dispatchDraw(canvas)

        mAlphaStyle?.setLocalMatrix(mMatrix)
        canvas.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), mAlphaPaint)
        canvas.restore()

        mIgnoreViewCache.forEach {
            super.drawChild(canvas, it, drawingTime)
        }
        mIgnoreViewCache.clear()
    }

    override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
        if (canvas == null || child == null) {
            return super.drawChild(canvas, child, drawingTime)
        }

        return if ((child.layoutParams as? LayoutParams)?.noBlink == true) {
            mIgnoreViewCache.add(child)
            false
        } else {
            super.drawChild(canvas, child, drawingTime)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (measuredWidth > 0) {
            mAlphaStyle = LinearGradient(0f, 0f, measuredWidth.toFloat(), 0f, mColors, null, Shader.TileMode.CLAMP)
            mAlphaPaint.shader = mAlphaStyle
        }
    }

    fun startLoading() {
        tryStartLoading()
    }

    private fun tryStartLoading() {
        if (mAlphaStyle == null) {
            post(mAnimationRunnable)
            return
        }
        mAnimator?.cancel()
        mAnimator = ValueAnimator.ofFloat(-measuredWidth.toFloat() * 3f, measuredWidth.toFloat() * 1.5f).apply {
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                mMatrix.reset()
                mMatrix.postRotate(15f)
                mMatrix.postTranslate((it.animatedValue as? Float) ?: 0f, 0f)
                invalidate()
            }
            duration = 2 * DateUtils.SECOND_IN_MILLIS
            repeatCount = 1000
            start()
        }
    }

    private fun stop() {
        removeCallbacks(mAnimationRunnable)
        mAnimator?.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startLoading()
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    override fun generateDefaultLayoutParams(): ConstraintLayout.LayoutParams {
        return LayoutParams(super.generateDefaultLayoutParams())
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    open class LayoutParams : ConstraintLayout.LayoutParams{

        var noBlink = false

        constructor(source: ConstraintLayout.LayoutParams?) : super(source)
        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs){
            c?.obtainStyledAttributes(attrs, R.styleable.BlinkLayout_Layout).apply {
                noBlink = this?.getBoolean(R.styleable.BlinkLayout_Layout_no_blink, false) ?: false
            }?.recycle()
        }
        constructor(width: Int, height: Int) : super(width, height)
    }
}
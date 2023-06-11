package com.udacity.loading

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import com.udacity.R
import kotlin.math.min
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private lateinit var buttonBounds: Rect
    private lateinit var buttonText: String
    private val boundingCircle = RectF()
    private var progressCircleValue = 0f
    private var buttonValue = 0f

    private var isInitialized = false

    private var valueAnimator = AnimatorSet()
    private lateinit var animationCircle: ValueAnimator
    private lateinit var animationButton: ValueAnimator

    private var loadingButtonColor = 0
    private var loadingCircleColor = 0
    private var initButtonColor    = 0

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when(new) {
            ButtonState.Clicked -> {
                buttonText = "Download"
                if (buttonState == ButtonState.Completed) {
                    buttonState = ButtonState.Clicked
                    invalidate()
                }
            }
            // when state Loading
            ButtonState.Loading -> {
                buttonText = "Loading ..."
                // Bound init once times
                if (!isInitialized) {
                    buttonBounds = Rect()
                    paintText.getTextBounds(buttonText, 0, buttonText.length, buttonBounds)
                    val textWidth = buttonBounds.width()
                    val textHeight = buttonBounds.height()
                    val circleSize = min(textWidth, textHeight) + 16f
                    val circleCenterX = textWidth + circleSize / 2f + 450f
                    val circleCenterY = heightSize / 2f
                    boundingCircle.set(
                        circleCenterX - circleSize / 2f,
                        circleCenterY - circleSize / 2f,
                        circleCenterX + circleSize / 2f,
                        circleCenterY + circleSize / 2f
                    )
                    isInitialized = true
                }

                // create Animation
                setupCircleAnimation()
                setupButtonAnimation()
                valueAnimator.playTogether(animationCircle, animationButton)
                valueAnimator.start()
            }
            ButtonState.Completed -> {
                // set Text button
                buttonText = "Download"
                valueAnimator.cancel()
                buttonValue = 0f
                invalidate()
            }
        }
    }

    private fun setupCircleAnimation() {
        animationCircle = ValueAnimator.ofFloat(0f, 360f).apply {
                duration = 1000
                interpolator = LinearInterpolator()
                repeatCount = 6
                addUpdateListener { animator ->
                progressCircleValue = animator.animatedValue as Float
                invalidate()
            }
        }
    }

    private fun setupButtonAnimation() {
        animationButton = ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            duration = 1000
            interpolator = LinearInterpolator()
            repeatCount = 6
            addUpdateListener { animator ->
                buttonValue = animator.animatedValue as Float
                invalidate()
            }
        }
    }

    init {
        isClickable = true
        buttonText = "Download"
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
           loadingButtonColor = getColor(R.styleable.LoadingButton_loadingButtonColor, 0)
           loadingCircleColor = getColor(R.styleable.LoadingButton_loadingCircleColor, 0)
           initButtonColor    = getColor(R.styleable.LoadingButton_initButtonColor, 0)
       }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawBackgroundButton(canvas)
        drawText(canvas)
        drawCircleInProgress(canvas)
    }

    private fun drawCircleInProgress(canvas: Canvas?) {
        if (buttonState == ButtonState.Loading) {
            paint.color = loadingCircleColor
            canvas?.drawArc(boundingCircle, 0f, progressCircleValue, true, paint)
        }
    }

    private fun drawText(canvas: Canvas?) {
        paintText.color = Color.WHITE
        canvas?.drawText(
            buttonText,
            widthSize / 2f,
            heightSize / 2f + ( paintText.descent() - paintText.ascent())/2 - paintText.descent(),
            paintText
        )
    }

    private fun drawBackgroundButton(canvas: Canvas?) {
        when (buttonState) {
            ButtonState.Loading -> {
                paint.color = loadingButtonColor
                canvas?.drawRect(0f, 0f, buttonValue, heightSize.toFloat(), paint)
            }
            else -> {
                paint.color = initButtonColor
                canvas?.drawRect(buttonValue, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)
            }
        }
    }

    // This is the function Udacity provided in the source code
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    fun changeButtonState(state: ButtonState) {
        buttonState = state
        invalidate()
    }
}

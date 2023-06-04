package com.udacity.loading

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.udacity.R
import kotlin.math.min
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var loadingText: CharSequence = ""
    private var loadingDefaultText: CharSequence = ""
    private var loadingDefaultBackgroundColor = 0
    private var loadingBackgroundColor = 0
    private var progressCircleBackgroundColor = 0
    private var loadingTextColor = 0

    private val valueAnimator = ValueAnimator()
    private val animatorSet =  AnimatorSet().apply {
        duration = 3000
    }

    // Color Button
    private var paintButton = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Button Text Type
    private lateinit var buttonText: String
    private val paintButtonText = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
        typeface = Typeface.DEFAULT
    }

    // Bounding Text Button
    private lateinit var buttonTextBounds: Rect

    // Bounding Circle
    private var progressCircle = RectF()
    private var progressCircleSize = 0f

    // Animation Circle (Create Circle 360)
    private var progressCircleAnimation = 0f
    private val animationCircle = ValueAnimator.ofFloat(0f, 360f).apply {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        // when ValueAnimator change
        addUpdateListener {
            progressCircleAnimation = it.animatedValue as Float
            invalidate()
        }
    }

    // Background change
    private var currentButtonBackgroundAnimationValue = 0f
    private lateinit var buttonBackgroundAnimator: ValueAnimator

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressCircleSize = (min(w, h) / 2f) * 0.3f
        createButtonBackgroundAnimator()
    }
    private fun createButtonBackgroundAnimator() {
        ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                currentButtonBackgroundAnimationValue = it.animatedValue as Float
                invalidate()
            }
        }.also {
            buttonBackgroundAnimator = it
            animatorSet.playProgressCircleAndButtonBackgroundTogether()
        }
    }

    private fun AnimatorSet.playProgressCircleAndButtonBackgroundTogether() =
        apply { playTogether(animationCircle, buttonBackgroundAnimator) }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when(new) {
            // when state Loading
            ButtonState.Loading -> {
                // set Text button
                buttonText = loadingText.toString()

                // Compute Bonding Text and Circle
                if (!::buttonTextBounds.isInitialized) {
                    computeButtonTextBounds()
                    computeProgressCircleRect()
                }

                animatorSet.start()
            }
            else -> {
                // set Text button
                buttonText = loadingDefaultText.toString()
                new.takeIf { it == ButtonState.Completed }?.run {animatorSet.cancel()}
            }
        }
    }


    override fun performClick(): Boolean {
        super.performClick()
        // We only change button state to Clicked if the current state is Completed
        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Clicked
            invalidate()
        }
        return true
    }
    private fun computeProgressCircleRect() {
        val horizontalCenter =
            (buttonTextBounds.right + buttonTextBounds.width() + 66f)
        val verticalCenter = (heightSize / 2f)

        progressCircle.set(
            horizontalCenter - progressCircleSize,
            verticalCenter - progressCircleSize,
            horizontalCenter + progressCircleSize,
            verticalCenter + progressCircleSize
        )
    }

    private fun computeButtonTextBounds() {
        buttonTextBounds = Rect()
        paintButtonText.getTextBounds(buttonText, 0, buttonText.length, buttonTextBounds)
    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            loadingDefaultBackgroundColor =
                getColor(R.styleable.LoadingButton_loadingDefaultBackgroundColor, 0)
            loadingBackgroundColor =
                getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
            loadingDefaultText =
                getText(R.styleable.LoadingButton_loadingDefaultText)
            loadingTextColor =
                getColor(R.styleable.LoadingButton_loadingTextColor, 0)
            loadingText =
                getText(R.styleable.LoadingButton_loadingText)
        }.also {
            buttonText = loadingDefaultText.toString()
            progressCircleBackgroundColor = ContextCompat.getColor(context, R.color.colorAccent)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { drawButton ->
            drawButton.apply {
                drawBackgroundColorButton()
                drawButtonText()
                drawProgressCircleIfLoading()
            }
        }
    }

    private fun Canvas.drawProgressCircleIfLoading() {
        if (buttonState == ButtonState.Loading) {
            drawProgressCircle(this)
        }
    }

    private fun drawProgressCircle(canvas: Canvas?) {
        paintButton.color = progressCircleBackgroundColor
        canvas?.drawArc(
            progressCircle,
            0f,
            progressCircleAnimation,
            true,
            paintButton
        )
    }

    private fun Canvas.drawButtonText() {
        paintButtonText.color = loadingTextColor
        drawText(
            buttonText,
            (widthSize/2f),
            (heightSize/2f) + paintButtonText.computeTextOffset(),
            paintButtonText
        )
    }

    private fun TextPaint.computeTextOffset() = ((descent() - ascent()) / 2) - descent()

    private fun Canvas.drawBackgroundColorButton() {
        when(buttonState) {
            ButtonState.Loading -> {
                drawLoadingBackgroundColor()
                drawDefaultBackgroundColor()
            }
            else -> {
                drawColor(loadingDefaultBackgroundColor)
            }
        }
    }

    private fun Canvas.drawLoadingBackgroundColor() = paintButton.apply {
        color = loadingBackgroundColor
    }.run {
        drawRect(
            0f,
            0f,
            currentButtonBackgroundAnimationValue,
            heightSize.toFloat(),
            paintButton
        )
    }

    private fun Canvas.drawDefaultBackgroundColor() = paintButton.apply {
        color = loadingDefaultBackgroundColor
    }.run {
        drawRect(
            currentButtonBackgroundAnimationValue,
            0f,
            widthSize.toFloat(),
            heightSize.toFloat(),
            paintButton
        )
    }

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
        if (state != buttonState) {
            buttonState = state
            invalidate()
        }
    }

}



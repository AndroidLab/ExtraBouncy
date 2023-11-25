package com.alab.extra_bouncy

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.EdgeEffect
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.alab.extra_bouncy.util.Bouncy
import com.alab.extra_bouncy.util.BouncyViewHolder
import com.alab.extra_bouncy.util.DragDropAdapter
import com.alab.extra_bouncy.util.DragDropCallBack
import com.alab.extra_bouncy.util.OnOverPullListener
import com.alab.extra_bouncy.util.OnOverScrollOffsetListener
import com.alab.extra_bouncy.util.setPaddingBottom
import com.alab.extra_bouncy.util.setPaddingLeft
import com.alab.extra_bouncy.util.setPaddingRight
import com.alab.extra_bouncy.util.setPaddingTop
import com.alab.extrabouncy.R
import kotlin.math.abs


/**
 * Представляет RecyclerView с эффектом оверскролла.
 */
class BouncyRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    private lateinit var callBack: DragDropCallBack

    /**
     * Слушатель событий оверскролла.
     */
    var onOverPullListener: OnOverPullListener? = null

    /**
     * Слушатель событий оверскролла.
     */
    var onOverScrollOffsetListener: OnOverScrollOffsetListener? = null

    /**
     * Величина эффекта оверскролла.
     */
    var overscrollStartAnimationSize: Float
    var overscrollEndAnimationSize: Float

    /**
     * Величина анимации эффекта оверскролла при автопрокрутке.
     */
    var flingAnimationSize: Float


    /**
     * Возвращает или устанавливает режим оверскрола.
     */
    private var bouncyOverscrollMode = Bouncy.OVERSCROLL_ALL


    /**
     * Тип эффекта оферскролла (Padding или Translation).
     */
    private val bouncyOverscrollType: Int

    private var backAnimDuration: Long = 350

    private val _paddingLeft: Int = this.paddingLeft
    private val _paddingTop: Int = this.paddingLeft
    private val _paddingRight: Int = this.paddingLeft
    private val _paddingBottom: Int = this.paddingLeft

    @Suppress("MemberVisibilityCanBePrivate")
    var dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        set(value) {
            field = value
            spring?.spring = SpringForce()
                .setFinalPosition(0f)
                .setDampingRatio(value)
                .setStiffness(stiffness)
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var stiffness = SpringForce.STIFFNESS_LOW
        set(value) {
            field = value
            spring?.spring = SpringForce()
                .setFinalPosition(0f)
                .setDampingRatio(dampingRatio)
                .setStiffness(value)
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var longPressDragEnabled = false
        set(value) {
            field = value
            if (adapter is DragDropAdapter<*>) callBack.setDragEnabled(value)
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var itemSwipeEnabled = false
        set(value) {
            field = value
            if (adapter is DragDropAdapter<*>) callBack.setSwipeEnabled(value)
        }

    private var animator: ValueAnimator? = null
    private var spring: SpringAnimation? = null
    private var touched: Boolean = false
    private var _direction: Int? = null
    private var currentTouchX = -1f
    private var currentTouchY = -1f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        touched = when (e?.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onOverPullListener?.onRelease()
                _direction?.let {
                    onOverScrollOffsetListener?.onOverScrollRelease(it)
                }

                if (_direction != null) {
                    createSpring()?.start()
                    createAnimator().start()
                }
                _direction = null
                currentTouchX = -1f
                currentTouchY = -1f
                false
            }

            MotionEvent.ACTION_MOVE -> {
                if (bouncyOverscrollType == Bouncy.OVERSCROLL_TRANSLATION) {
                    var offset = 0f
                    if (_direction != null) {
                        if (_direction == EdgeEffectFactory.DIRECTION_LEFT && e.x < currentTouchX && this.translationX > 0) {
                            offset = e.x - currentTouchX
                            this.translationX += offset
                            onOverScrollOffsetListener?.onOverScrollOffset(
                                Bouncy.OVERSCROLL_START,
                                translationX.toInt()
                            )
                        }
                        if (_direction == EdgeEffectFactory.DIRECTION_TOP && e.y < currentTouchY && this.translationY > 0) {
                            offset = e.y - currentTouchY
                            this.translationY += offset
                            onOverScrollOffsetListener?.onOverScrollOffset(
                                Bouncy.OVERSCROLL_START,
                                translationY.toInt()
                            )
                        }
                        if (_direction == EdgeEffectFactory.DIRECTION_RIGHT && e.x > currentTouchX && this.translationX < 0) {
                            offset = e.x - currentTouchX
                            this.translationX += offset
                            onOverScrollOffsetListener?.onOverScrollOffset(
                                Bouncy.OVERSCROLL_END,
                                abs(translationX).toInt()
                            )
                        }
                        if (_direction == EdgeEffectFactory.DIRECTION_BOTTOM && e.y > currentTouchY && this.translationY < 0) {
                            offset = e.y - currentTouchY
                            this.translationY += offset
                            onOverScrollOffsetListener?.onOverScrollOffset(
                                Bouncy.OVERSCROLL_END,
                                abs(translationY).toInt()
                            )
                        }
                    }
                    currentTouchX = e.x - offset
                    currentTouchY = e.y - offset
                } else {
                    if (_direction != null) {
                        if (_direction == EdgeEffectFactory.DIRECTION_LEFT && e.x < currentTouchX && this.paddingLeft > _paddingLeft) {
                            this.setPaddingLeft(this.paddingLeft - (currentTouchX - e.x).toInt())
                            onOverScrollOffsetListener?.onOverScrollOffset(
                                Bouncy.OVERSCROLL_START,
                                paddingLeft - _paddingLeft
                            )
                        }
                        if (_direction == EdgeEffectFactory.DIRECTION_TOP && e.y < currentTouchY && this.paddingTop > _paddingTop) {
                            this.setPaddingTop(this.paddingTop - (currentTouchY - e.y).toInt())
                            onOverScrollOffsetListener?.onOverScrollOffset(
                                Bouncy.OVERSCROLL_START,
                                paddingTop - _paddingTop
                            )
                        }
                        if (_direction == EdgeEffectFactory.DIRECTION_RIGHT && e.x > currentTouchX && this.paddingRight > _paddingRight) {
                            this.setPaddingRight(this.paddingRight - (e.x - currentTouchX).toInt())
                            onOverScrollOffsetListener?.onOverScrollOffset(
                                Bouncy.OVERSCROLL_END,
                                paddingRight - _paddingRight
                            )
                        }
                        if (_direction == EdgeEffectFactory.DIRECTION_BOTTOM && e.y > currentTouchY && this.paddingBottom > _paddingBottom) {
                            this.setPaddingBottom(this.paddingBottom - (e.y - currentTouchY).toInt())
                            onOverScrollOffsetListener?.onOverScrollOffset(
                                Bouncy.OVERSCROLL_END,
                                paddingBottom - _paddingBottom
                            )
                        }
                    }
                    currentTouchX = e.x
                    currentTouchY = e.y
                }
                true
            }

            else -> true
        }
        return super.onTouchEvent(e)
    }


    override fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        super.setAdapter(adapter)
        if (adapter is DragDropAdapter<*>) {
            callBack = DragDropCallBack(adapter, longPressDragEnabled, itemSwipeEnabled)
            val touchHelper = ItemTouchHelper(callBack)
            touchHelper.attachToRecyclerView(this)
        }
    }

    inline fun <reified T : ViewHolder> RecyclerView.forEachVisibleHolder(action: (T) -> Unit) {
        for (i in 0 until childCount) action(getChildViewHolder(getChildAt(i)) as T)
    }

    private fun updateOverScrollOffset(direction: Int) {
        when (direction) {
            EdgeEffectFactory.DIRECTION_LEFT -> onOverScrollOffsetListener?.onOverScrollOffset(
                Bouncy.OVERSCROLL_START,
                if (bouncyOverscrollType == Bouncy.OVERSCROLL_TRANSLATION) {
                    translationX.toInt()
                } else {
                    paddingLeft - _paddingLeft
                }
            )

            EdgeEffectFactory.DIRECTION_TOP -> onOverScrollOffsetListener?.onOverScrollOffset(
                Bouncy.OVERSCROLL_START,
                if (bouncyOverscrollType == Bouncy.OVERSCROLL_TRANSLATION) {
                    translationY.toInt()
                } else {
                    paddingTop - _paddingTop
                }
            )

            EdgeEffectFactory.DIRECTION_RIGHT -> onOverScrollOffsetListener?.onOverScrollOffset(
                Bouncy.OVERSCROLL_END,
                if (bouncyOverscrollType == Bouncy.OVERSCROLL_TRANSLATION) {
                    abs(translationX).toInt()
                } else {
                    paddingRight - _paddingRight
                }
            )

            EdgeEffectFactory.DIRECTION_BOTTOM -> onOverScrollOffsetListener?.onOverScrollOffset(
                Bouncy.OVERSCROLL_END,
                if (bouncyOverscrollType == Bouncy.OVERSCROLL_TRANSLATION) {
                    abs(translationY).toInt()
                } else {
                    paddingBottom - _paddingBottom
                }
            )
        }
    }

    private fun createSpring(): SpringAnimation? {
        fun createSpring(property: FloatPropertyCompat<View>) = SpringAnimation(this, property)
            .setSpring(
                SpringForce()
                    .setFinalPosition(0f)
                    .setDampingRatio(dampingRatio)
                    .setStiffness(stiffness)
            ).addUpdateListener { animation, value, velocity ->
                _direction?.let { updateOverScrollOffset(it) }
            }

        return if (stiffness > 0) {
            when (_direction) {
                EdgeEffectFactory.DIRECTION_LEFT, EdgeEffectFactory.DIRECTION_RIGHT ->
                    createSpring(SpringAnimation.TRANSLATION_X).also {
                        spring = it
                    }

                EdgeEffectFactory.DIRECTION_TOP, EdgeEffectFactory.DIRECTION_BOTTOM ->
                    createSpring(SpringAnimation.TRANSLATION_Y).also {
                        spring = it
                    }

                else -> throw NotImplementedError()
            }
        } else {
            null
        }
    }

    private fun createAnimator(): ValueAnimator {
        fun createAnimator(direction: Int, fromValue: Int, toValue: Int) =
            ValueAnimator.ofInt(fromValue, toValue).apply {
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(p0: Animator) {}
                    override fun onAnimationEnd(p0: Animator) {
                        onOverScrollOffsetListener?.onOverScrollEnd()
                    }

                    override fun onAnimationCancel(p0: Animator) {}
                    override fun onAnimationRepeat(p0: Animator) {}
                })
                addUpdateListener { valueAnimator ->
                    when (direction) {
                        EdgeEffectFactory.DIRECTION_LEFT -> {
                            this@BouncyRecyclerView.setPaddingLeft(
                                valueAnimator.animatedValue as Int
                            )
                        }

                        EdgeEffectFactory.DIRECTION_TOP -> {
                            this@BouncyRecyclerView.setPaddingTop(
                                valueAnimator.animatedValue as Int
                            )
                        }

                        EdgeEffectFactory.DIRECTION_RIGHT -> {
                            this@BouncyRecyclerView.setPaddingRight(
                                valueAnimator.animatedValue as Int
                            )
                        }

                        EdgeEffectFactory.DIRECTION_BOTTOM -> {
                            this@BouncyRecyclerView.setPaddingBottom(
                                valueAnimator.animatedValue as Int
                            )
                        }
                    }
                    updateOverScrollOffset(direction)
                }
                duration = backAnimDuration
            }

        return when (_direction) {
            EdgeEffectFactory.DIRECTION_LEFT -> {
                createAnimator(
                    _direction!!,
                    paddingLeft,
                    _paddingLeft
                ).also {
                    animator = it
                }
            }

            EdgeEffectFactory.DIRECTION_TOP -> {
                createAnimator(
                    _direction!!,
                    paddingTop,
                    _paddingTop
                ).also {
                    animator = it
                }
            }

            EdgeEffectFactory.DIRECTION_RIGHT -> {
                createAnimator(
                    _direction!!,
                    paddingRight,
                    _paddingRight
                ).also {
                    animator = it
                }
            }

            EdgeEffectFactory.DIRECTION_BOTTOM -> {
                createAnimator(
                    _direction!!,
                    paddingBottom,
                    _paddingBottom
                ).also {
                    animator = it
                }
            }

            else -> throw NotImplementedError()
        }
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.BouncyRecyclerView, 0, 0)
            .apply {
                longPressDragEnabled =
                    getBoolean(R.styleable.BouncyRecyclerView_allowDragReorder, false)
                itemSwipeEnabled =
                    getBoolean(R.styleable.BouncyRecyclerView_allowItemSwipe, false)

                overscrollStartAnimationSize = getFloat(
                    R.styleable.BouncyRecyclerView_recyclerviewOverscrollStartAnimationSize,
                    0.5f
                )
                overscrollEndAnimationSize = getFloat(
                    R.styleable.BouncyRecyclerView_recyclerviewOverscrollEndAnimationSize,
                    0.5f
                )

                flingAnimationSize =
                    getFloat(R.styleable.BouncyRecyclerView_recyclerview_fling_animation_size, 0.5f)

                when (getInt(R.styleable.BouncyRecyclerView_bouncyRecyclerviewDampingRatio, 0)) {
                    0 -> dampingRatio = Bouncy.DAMPING_RATIO_NO_BOUNCY
                    1 -> dampingRatio = Bouncy.DAMPING_RATIO_LOW_BOUNCY
                    2 -> dampingRatio = Bouncy.DAMPING_RATIO_MEDIUM_BOUNCY
                    3 -> dampingRatio = Bouncy.DAMPING_RATIO_HIGH_BOUNCY
                }
                when (getInt(R.styleable.BouncyRecyclerView_bouncyRecyclerviewStiffness, 1)) {
                    0 -> stiffness = Bouncy.STIFFNESS_VERY_LOW
                    1 -> stiffness = Bouncy.STIFFNESS_LOW
                    2 -> stiffness = Bouncy.STIFFNESS_MEDIUM
                    3 -> stiffness = Bouncy.STIFFNESS_HIGH
                }
                when (getInt(
                    R.styleable.BouncyRecyclerView_bouncyRecyclerviewOverscrollMode,
                    bouncyOverscrollMode
                )) {
                    -1 -> bouncyOverscrollMode = Bouncy.OVERSCROLL_NEVER
                    0 -> bouncyOverscrollMode = Bouncy.OVERSCROLL_ALL
                    1 -> bouncyOverscrollMode = Bouncy.OVERSCROLL_START
                    2 -> bouncyOverscrollMode = Bouncy.OVERSCROLL_END
                }

                bouncyOverscrollType = getInt(
                    R.styleable.BouncyRecyclerView_bouncyRecyclerviewOverscrollType,
                    Bouncy.OVERSCROLL_TRANSLATION
                )
                recycle()
            }


        this.edgeEffectFactory = object : EdgeEffectFactory() {
            override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {

                return object : EdgeEffect(recyclerView.context) {

                    override fun onPull(deltaDistance: Float) {
                        super.onPull(deltaDistance)
                        if (_direction == null) {
                            _direction = direction
                        }
                        onPullAnimation(deltaDistance)
                    }

                    @SuppressLint("SwitchIntDef")
                    override fun onPull(deltaDistance: Float, displacement: Float) {
                        super.onPull(deltaDistance, displacement)
                        if (bouncyOverscrollMode != Bouncy.OVERSCROLL_NEVER) {
                            if (_direction == null) {
                                _direction = direction
                                onOverScrollOffsetListener?.onOverScrollStart(_direction!!)
                            }
                            onPullAnimation(deltaDistance)

                            when (direction) {
                                DIRECTION_LEFT -> onOverPullListener?.onOverPulledLeft(deltaDistance)
                                DIRECTION_TOP -> onOverPullListener?.onOverPulledTop(deltaDistance)
                                DIRECTION_RIGHT -> onOverPullListener?.onOverPulledRight(
                                    deltaDistance
                                )

                                DIRECTION_BOTTOM -> onOverPullListener?.onOverPulledBottom(
                                    deltaDistance
                                )
                            }
                        }
                    }

                    private fun onPullAnimation(deltaDistance: Float) {
                        animator?.cancel()
                        spring?.cancel()
                        when (direction) {
                            DIRECTION_LEFT -> {
                                val delta =
                                    if (bouncyOverscrollMode == Bouncy.OVERSCROLL_ALL || bouncyOverscrollMode == Bouncy.OVERSCROLL_START) {
                                        recyclerView.width * deltaDistance * overscrollStartAnimationSize
                                    } else {
                                        0f
                                    }
                                if (bouncyOverscrollType == Bouncy.OVERSCROLL_TRANSLATION) {
                                    translationX += delta
                                } else {
                                    setPaddingLeft(paddingLeft + delta.toInt())
                                }
                            }

                            DIRECTION_TOP -> {
                                val delta =
                                    if (bouncyOverscrollMode == Bouncy.OVERSCROLL_ALL || bouncyOverscrollMode == Bouncy.OVERSCROLL_START) {
                                        recyclerView.width * deltaDistance * overscrollStartAnimationSize
                                    } else {
                                        0f
                                    }
                                if (bouncyOverscrollType == Bouncy.OVERSCROLL_TRANSLATION) {
                                    translationY += delta
                                } else {
                                    setPaddingTop(paddingTop + delta.toInt())
                                    onOverScrollOffsetListener?.onOverScrollOffset(
                                        Bouncy.OVERSCROLL_START,
                                        paddingTop - _paddingTop
                                    )
                                }
                            }

                            DIRECTION_RIGHT -> {
                                val delta =
                                    if (bouncyOverscrollMode == Bouncy.OVERSCROLL_ALL || bouncyOverscrollMode == Bouncy.OVERSCROLL_END) {
                                        recyclerView.width * deltaDistance * overscrollEndAnimationSize
                                    } else {
                                        0f
                                    }
                                if (bouncyOverscrollType == Bouncy.OVERSCROLL_TRANSLATION) {
                                    translationX -= delta
                                } else {
                                    setPaddingRight(paddingRight + delta.toInt())
                                }
                            }

                            DIRECTION_BOTTOM -> {
                                val delta =
                                    if (bouncyOverscrollMode == Bouncy.OVERSCROLL_ALL || bouncyOverscrollMode == Bouncy.OVERSCROLL_END) {
                                        recyclerView.width * deltaDistance * overscrollEndAnimationSize
                                    } else {
                                        0f
                                    }
                                if (bouncyOverscrollType == Bouncy.OVERSCROLL_TRANSLATION) {
                                    translationY -= delta
                                } else {
                                    setPaddingBottom(paddingBottom + delta.toInt())
                                }
                            }
                        }
                        updateOverScrollOffset(direction)

                        forEachVisibleHolder { holder: ViewHolder? ->
                            if (holder is BouncyViewHolder) holder.onPulled(
                                deltaDistance
                            )
                        }

                    }

                    override fun onRelease() {
                        super.onRelease()
                        if (touched)
                            return

                        forEachVisibleHolder { holder: ViewHolder? -> if (holder is BouncyViewHolder) holder.onRelease() }
                    }

                    override fun onAbsorb(velocity: Int) {
                        super.onAbsorb(velocity)
                        val v = when (direction) {
                            DIRECTION_LEFT -> {
                                if (bouncyOverscrollMode == Bouncy.OVERSCROLL_ALL || bouncyOverscrollMode == Bouncy.OVERSCROLL_START) {
                                    1 * velocity * flingAnimationSize
                                } else {
                                    0f
                                }
                            }

                            DIRECTION_TOP -> {
                                if (bouncyOverscrollMode == Bouncy.OVERSCROLL_ALL || bouncyOverscrollMode == Bouncy.OVERSCROLL_START) {
                                    1 * velocity * flingAnimationSize
                                } else {
                                    0f
                                }
                            }

                            DIRECTION_RIGHT -> {
                                if (bouncyOverscrollMode == Bouncy.OVERSCROLL_ALL || bouncyOverscrollMode == Bouncy.OVERSCROLL_END) {
                                    -1 * velocity * flingAnimationSize
                                } else {
                                    0f
                                }
                            }

                            DIRECTION_BOTTOM -> {
                                if (bouncyOverscrollMode == Bouncy.OVERSCROLL_ALL || bouncyOverscrollMode == Bouncy.OVERSCROLL_END) {
                                    -1 * velocity * flingAnimationSize
                                } else {
                                    1f
                                }
                            }

                            else -> throw NotImplementedError()
                        }

                        spring?.setStartVelocity(v)?.start()

                        forEachVisibleHolder { holder: ViewHolder? ->
                            if (holder is BouncyViewHolder) holder.onAbsorb(
                                velocity
                            )
                        }
                    }

                    override fun draw(canvas: Canvas?): Boolean {
                        setSize(0, 0)
                        return super.draw(canvas)
                    }
                }
            }
        }
    }

    abstract class Adapter<T : ViewHolder> : RecyclerView.Adapter<T>(), DragDropAdapter<T>
}

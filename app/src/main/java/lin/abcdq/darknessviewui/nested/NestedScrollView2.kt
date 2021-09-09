package lin.abcdq.darknessviewui.nested

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

/**
 * 联动布局
 */
class NestedScrollView2 : NestedScrollView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, a: Int) : super(context, attr, a)

    private val mScroller: OverScroller by lazy {
        val filed = NestedScrollView::class.java.getDeclaredField("mScroller")
        filed.isAccessible = true
        filed.get(this) as OverScroller
    }
    private var mVelocityTracker = VelocityTracker.obtain()//计算抛出速度
    private var mCanFlingParent = false //是否联动抛出NestedScrollView
    private var mOldDy = 0f

    private val mViewChecker = NestedViewChecker()
    private var mNestedChildView: View? = null//childView
    private var mPagerView: ViewPager? = null//viewpager
    private var mPager2View: ViewPager2? = null//viewpager2

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mViewChecker.onDestroy()
    }

    private fun findNestedChild() {
        mViewChecker.start(this) {
            mNestedChildView = mViewChecker.nestedView()
            mPagerView = mViewChecker.pagerView()
            mPager2View = mViewChecker.pager2View()
            childTouch()
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        flingChild()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.pointerCount > 1) return false
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            stopScroll()
            findNestedChild()
            if (mVelocityTracker != null) mVelocityTracker.clear()
        } else if (ev.action == MotionEvent.ACTION_MOVE) {
            if (mVelocityTracker != null) mVelocityTracker.addMovement(ev)
        }
        //ViewPager: java.lang.IllegalArgumentException: pointerIndex out of range
        //try catch ViewPager -> onInterceptTouchEvent()
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun childTouch() {
        mNestedChildView?.setOnTouchListener { _, ev ->
            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                mOldDy = ev.rawY
            } else if (ev.action == MotionEvent.ACTION_MOVE) {
                val diff = mOldDy - ev.rawY
                mOldDy = ev.rawY
                mCanFlingParent = false
                if (diff > 0 && canScrollVertically(1)) {
                    mCanFlingParent = true
                    scrollBy(0, (diff).toInt())
                    enableViewPager(false)
                    return@setOnTouchListener true
                }
            } else if (ev.actionMasked == MotionEvent.ACTION_UP) {
                mOldDy = 0f
                if (mCanFlingParent) flingParent()
                enableViewPager(true)
            }
            return@setOnTouchListener false
        }
    }

    private fun enableViewPager(enable: Boolean) {
        mPagerView?.isEnabled = enable
        mPagerView?.requestDisallowInterceptTouchEvent(!enable)
        mPager2View?.isUserInputEnabled = enable
    }

    private fun stopScroll() {
        mScroller.abortAnimation()
        stopNestedScroll(ViewCompat.TYPE_TOUCH)
        if (mNestedChildView == null) return
        if (mNestedChildView !is RecyclerView) return
        (mNestedChildView as RecyclerView).stopScroll()
        (mNestedChildView as RecyclerView).stopNestedScroll()
    }

    private fun flingChild() {
        if (!canScrollVertically(1)) {
            if (mVelocityTracker == null) return
            if (mNestedChildView == null) return
            if (mNestedChildView !is RecyclerView) return
            mVelocityTracker.computeCurrentVelocity(100)
            val fl = -1 * mVelocityTracker.yVelocity.toInt() * 2
            (mNestedChildView as RecyclerView).fling(0, if (fl <= 500) 0 else fl)
        }
    }

    private fun flingParent() {
        if (mVelocityTracker == null) return
        mVelocityTracker.computeCurrentVelocity(1000)
        val fl = -1 * mVelocityTracker.yVelocity.toInt() / 2
        this.fling(if (fl <= 1000) 0 else fl)
    }
}
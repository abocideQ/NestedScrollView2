package lin.abcdq.darknessviewui.nested

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class NestedParentScrollView : NestedScrollView, NestedParent {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, a: Int) : super(context, attr, a)

    fun setListener(listener: CurrentListener) {
        isFillViewport = true
        mListener = listener
        if (mListener?.viewPager() is ViewPager) {
            mPager = mListener?.viewPager() as ViewPager
        } else {
            mPager2 = mListener?.viewPager() as ViewPager2
        }
    }

    private var mListener: CurrentListener? = null

    interface CurrentListener {
        fun viewPager(): View
        fun child(): NestedChild
    }

    private val mScroller: OverScroller by lazy {
        val filed = NestedScrollView::class.java.getDeclaredField("mScroller")
        filed.isAccessible = true
        filed.get(this) as OverScroller
    }

    private val mViewChecker = NestedViewChecker()
    private var mChild: NestedChild? = null//child
    private var mPager: ViewPager? = null//viewpager
    private var mPager2: ViewPager2? = null//viewpager2

    private var mVelocityTracker = VelocityTracker.obtain()
    private var mTouching = false

    private fun stopAllScroll() {
        mScroller.abortAnimation()
        stopNestedScroll(ViewCompat.TYPE_TOUCH)
        mChild?.stop()
    }

    private fun findNestedChildByChecker() {
        mViewChecker.start(this) {
            mChild = mViewChecker.nestedView()
            mPager = mViewChecker.pagerView()
            mPager2 = mViewChecker.pager2View()
        }
    }

    private fun findNestedChildByListener() {
        mChild = mListener?.child()
        mChild?.setParent(this)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (!canScrollVertically(1)) {
            if (!mTouching) {
                mChild?.fling(mScroller.currVelocity)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.pointerCount > 1) return false
        if (ev.action == MotionEvent.ACTION_DOWN) {
            mTouching = true
            stopAllScroll()
            if (mListener == null) findNestedChildByChecker()
            else findNestedChildByListener()
            if (mVelocityTracker != null) mVelocityTracker.clear()
        } else if (ev.action == MotionEvent.ACTION_MOVE) {
            if (mVelocityTracker != null) mVelocityTracker.addMovement(ev)
        } else if (ev.action == MotionEvent.ACTION_UP) {
            mTouching = false
        }
        //ViewPager: java.lang.IllegalArgumentException: pointerIndex out of range
        //try catch ViewPager -> onInterceptTouchEvent()
        return super.dispatchTouchEvent(ev)
    }

    private var mOldDy = 0f
    private var mOldDx = 0f

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        //fix the bug that origin view (exp:viewpager) which can scroll
        //let this view can not intercept event
        if (mChild?.touching() == false) {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                mOldDy = 0f
                mOldDx = 0f
                mOldDy = ev.rawY
                mOldDx = ev.rawX
            } else if (ev.action == MotionEvent.ACTION_MOVE) {
                val diffDy = mOldDy - ev.rawY
                val diffDx = mOldDx - ev.rawX
                val absDy = abs(diffDy)
                val absDx = abs(diffDx)
                mOldDy = ev.rawY
                return absDx <= absDy
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun enablePager(enable: Boolean) {
        mPager?.isEnabled = enable
        mPager?.requestDisallowInterceptTouchEvent(!enable)
        mPager2?.isUserInputEnabled = enable
    }

    override fun fling() {
        mVelocityTracker.computeCurrentVelocity(1000)
        val fl = (-1.0f * mVelocityTracker.yVelocity / 2.0f).toInt()
        this.fling(fl)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mViewChecker.onDestroy()
    }
}
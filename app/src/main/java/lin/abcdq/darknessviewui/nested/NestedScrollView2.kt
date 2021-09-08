package lin.abcdq.darknessviewui.nested

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.widget.OverScroller
import android.widget.ScrollView
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import java.lang.Exception
import java.util.concurrent.Executors

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

    private var mNestedChildView: View? = null//childView
    private var mPager2: ViewPager2? = null//viewpager2
    private var mPager: ViewPager? = null//viewpager

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        flingChild()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            stopScroll()
            findChild()
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
            } else if (ev.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                return@setOnTouchListener true
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
            } else if (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_CANCEL) {
                mOldDy = 0f
                if (mCanFlingParent) flingParent()
                enableViewPager(true)
            }
            return@setOnTouchListener false
        }
    }

    private fun enableViewPager(enable: Boolean) {
        mPager2?.isUserInputEnabled = enable
        mPager?.isEnabled = enable
        mPager?.requestDisallowInterceptTouchEvent(!enable)
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

    /**
     * StickyView : RecyclerView/ScrollView/NestedScrollView
     *
     * Thread -> ViewGroup
     *
     * (status1) -> RecyclerView/?????  4 times
     *
     * (status2) -> ViewPager -> ViewGroup -> RecyclerView/????  2 times
     */
    private val mThread = Executors.newSingleThreadExecutor()

    private fun findChild() {
        mThread.execute {
            try {
                val list1 = arrayListOf<ViewGroup>()
                for (i in 0 until childCount) {
                    val v = getChildAt(i)
                    if (viewCheck(v)) return@execute
                    if (v is ViewGroup) list1.add(v)
                }
                val list2 = arrayListOf<ViewGroup>()
                for (g in list1) {
                    for (i in 0 until g.childCount) {
                        val v = g.getChildAt(i)
                        if (viewCheck(v)) return@execute
                        if (viewCheckInPager(v)) return@execute
                        if (v is ViewGroup) list2.add(v)
                    }
                }
                val list3 = arrayListOf<ViewGroup>()
                for (g in list2) {
                    for (i in 0 until g.childCount) {
                        val v = g.getChildAt(i)
                        if (viewCheck(v)) return@execute
                        if (viewCheckInPager(v)) return@execute
                        if (v is ViewGroup) list3.add(v)
                    }
                }
                for (g in list3) {
                    for (i in 0 until g.childCount) {
                        val v = g.getChildAt(i)
                        if (viewCheck(v)) return@execute
                        if (viewCheckInPager(v)) return@execute
                    }
                }
                mNestedChildView = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun viewCheck(v: View): Boolean {
        if (v is RecyclerView || v is ScrollView || v is NestedScrollView) {
            mNestedChildView?.setOnTouchListener(null)
            mNestedChildView = v
            return true
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun viewCheckInPager(parent: View): Boolean {
        if (parent is ViewPager) {
            mPager = parent
            val list1 = arrayListOf<ViewGroup>()
            for (i in 0 until parent.childCount) {
                val v1 = parent.getChildAt(i)
                if (viewCheck(v1)) {
                    mNestedChildView = null
                    val params = v1.layoutParams as ViewPager.LayoutParams
                    val positionField = params.javaClass.getDeclaredField("position")
                    positionField.isAccessible = true
                    val position = positionField.get(params) as Int
                    if (!params.isDecor && parent.currentItem == position) {
                        mNestedChildView?.setOnTouchListener(null)
                        mNestedChildView = v1
                        return true
                    }
                }
                if (v1 is ViewGroup) list1.add(v1)
            }
            for (g in list1) {
                for (i in 0 until g.childCount) {
                    val v1 = g.getChildAt(i)
                    if (viewCheck(v1)) {
                        mNestedChildView = null
                        val params = v1.layoutParams as ViewPager.LayoutParams
                        val field = params.javaClass.getDeclaredField("position")
                        field.isAccessible = true
                        val position = field.get(params) as Int
                        if (!params.isDecor && parent.currentItem == position) {
                            mNestedChildView?.setOnTouchListener(null)
                            mNestedChildView = v1
                            return true
                        }
                    }
                    if (v1 is ViewGroup) list1.add(v1)
                }
            }
        } else if (parent is ViewPager2) {
            mPager2 = parent
            val field = ViewPager2::class.java.getDeclaredField("mLayoutManager")
            field.isAccessible = true
            val layoutManager = field.get(parent) as LinearLayoutManager
            val currentView: ViewGroup =
                (layoutManager.findViewByPosition(parent.currentItem) ?: return false) as ViewGroup
            if (viewCheck(currentView)) {
                mNestedChildView = currentView
                childTouch()
                return true
            }
            val list1 = arrayListOf<ViewGroup>()
            for (i in 0 until childCount) {
                val v1 = currentView.getChildAt(i)
                if (viewCheck(v1)) {
                    mNestedChildView?.setOnTouchListener(null)
                    mNestedChildView = v1
                    childTouch()
                    return true
                }
                if (v1 is ViewGroup) list1.add(v1)
            }
            val list2 = arrayListOf<ViewGroup>()
            for (g in list1) {
                for (i in 0 until g.childCount) {
                    val v1 = g.getChildAt(i)
                    if (viewCheck(v1)) {
                        mNestedChildView?.setOnTouchListener(null)
                        mNestedChildView = v1
                        childTouch()
                        return true
                    }
                    if (v1 is ViewGroup) list2.add(v1)
                }
            }
            for (g in list2) {
                for (i in 0 until g.childCount) {
                    val v1 = g.getChildAt(i)
                    if (viewCheck(v1)) {
                        mNestedChildView?.setOnTouchListener(null)
                        mNestedChildView = v1
                        childTouch()
                        return true
                    }
                }
            }
        }
        return false
    }
}
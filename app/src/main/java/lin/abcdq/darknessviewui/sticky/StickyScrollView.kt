package lin.abcdq.darknessviewui.sticky

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
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
 * 粘性布局父布局
 */
class StickyScrollView : NestedScrollView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, a: Int) : super(context, attr, a)

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (!canScrollVertically(1)) {
            mVelocityTracker.computeCurrentVelocity(100)
            val fl = -1 * mVelocityTracker.yVelocity.toInt() * 2
            if (mStickyView is RecyclerView) {
                (mStickyView as RecyclerView).fling(0, if (fl <= 500) 0 else fl)
            }
        }
    }

    private var mVelocityTracker = VelocityTracker.obtain()
    private var mTack = false
    private var mOldDy = 0f

    private var mStickyView: View? = null
    private var mPager2: ViewPager2? = null
    private var mPager: ViewPager? = null

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            findSticky()
            if (mVelocityTracker != null) mVelocityTracker.clear()
        } else if (ev.action == MotionEvent.ACTION_MOVE) {
            if (mVelocityTracker != null) mVelocityTracker.addMovement(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun childTouch() {
        mStickyView?.setOnTouchListener { _, ev ->
            if (ev.action == MotionEvent.ACTION_DOWN) {
                stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
                scrollBy(0, 0)
                mOldDy = ev.rawY
            } else if (ev.action == MotionEvent.ACTION_MOVE) {
                if (ev.rawY < mOldDy && canScrollVertically(1)) {
                    scrollBy(0, (mOldDy - ev.rawY).toInt())
                    mOldDy = ev.rawY
                    mTack = true
                    mPager2?.isUserInputEnabled = false
                    mPager?.isEnabled = false
                    return@setOnTouchListener true
                } else mTack = false
                mOldDy = ev.rawY
            } else if (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_CANCEL) {
                if (mVelocityTracker != null && mTack) {
                    mVelocityTracker.computeCurrentVelocity(1000)
                    val fl = -1 * mVelocityTracker.yVelocity.toInt()
                    fling(if (fl <= 1000) 0 else fl)
                }
                mPager2?.isUserInputEnabled = true
                mPager?.isEnabled = true
                mOldDy = 0f
            }
            return@setOnTouchListener false
        }
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

    private fun findSticky() {
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
                mStickyView = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun viewCheck(v: View): Boolean {
        if (v is RecyclerView || v is ScrollView || v is NestedScrollView) {
            mStickyView?.setOnTouchListener(null)
            mStickyView = v
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
                    mStickyView = null
                    val params = v1.layoutParams as ViewPager.LayoutParams
                    val positionField = params.javaClass.getDeclaredField("position")
                    positionField.isAccessible = true
                    val position = positionField.get(params) as Int
                    if (!params.isDecor && parent.currentItem == position) {
                        mStickyView?.setOnTouchListener(null)
                        mStickyView = v1
                        return true
                    }
                }
                if (v1 is ViewGroup) list1.add(v1)
            }
            for (g in list1) {
                for (i in 0 until g.childCount) {
                    val v1 = g.getChildAt(i)
                    if (viewCheck(v1)) {
                        mStickyView = null
                        val params = v1.layoutParams as ViewPager.LayoutParams
                        val field = params.javaClass.getDeclaredField("position")
                        field.isAccessible = true
                        val position = field.get(params) as Int
                        if (!params.isDecor && parent.currentItem == position) {
                            mStickyView?.setOnTouchListener(null)
                            mStickyView = v1
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
                mStickyView = currentView
                childTouch()
                return true
            }
            val list1 = arrayListOf<ViewGroup>()
            for (i in 0 until childCount) {
                val v1 = currentView.getChildAt(i)
                if (viewCheck(v1)) {
                    mStickyView?.setOnTouchListener(null)
                    mStickyView = v1
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
                        mStickyView?.setOnTouchListener(null)
                        mStickyView = v1
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
                        mStickyView?.setOnTouchListener(null)
                        mStickyView = v1
                        childTouch()
                        return true
                    }
                }
            }
        }
        return false
    }
}
package lin.abcdq.darknessviewui.nested

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class NestedChildRecyclerView : RecyclerView, NestedChild {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, a: Int) : super(context, attr, a)

    private var mOldDy = 0f
    private var mOldDx = 0f
    private var mFlingParent = false

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            mTouching = true
            mOldDy = 0f
            mOldDx = 0f
            mOldDy = ev.rawY
            mOldDx = ev.rawX
        } else if (ev.action == MotionEvent.ACTION_MOVE) {
            val diffDy = mOldDy - ev.rawY
            val diffDx = mOldDx - ev.rawX
            val absDy = abs(diffDy)
            val absDx = abs(diffDx)
            if (absDx > absDy) {
                mParent?.enablePager(true)
            } else {
                mParent?.enablePager(false)
            }
            mOldDy = ev.rawY
            mFlingParent = false
            if (mParent?.canScrollVertically(1) == true) {
                mFlingParent = true
                mParent?.scrollBy(0, (diffDy).toInt())
                return false
            }
        } else if (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_CANCEL) {
            mTouching = false
            mParent?.enablePager(true)
            if (mFlingParent) {
                mParent?.fling()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private var mParent: NestedParent? = null

    override fun setParent(parent: NestedParent) {
        mParent = parent
    }

    private var mTouching = false

    override fun touching(): Boolean {
        return mTouching
    }

    override fun fling(velocity: Float) {
        fling(0, velocity.toInt())
    }

    override fun stop() {
        stopScroll()
        stopNestedScroll()
    }
}
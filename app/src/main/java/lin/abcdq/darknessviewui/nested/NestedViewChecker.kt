package lin.abcdq.darknessviewui.nested

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NestedViewChecker {

    fun nestedView(): NestedChild? {
        return mNestedRecyclerView
    }

    fun pagerView(): ViewPager? {
        return mPagerView
    }

    fun pager2View(): ViewPager2? {
        return mPager2View
    }

    fun onDestroy() {
        forceReset()
        mHandler?.looper?.quitSafely()
    }

    private var mNestedRecyclerView: NestedChild? = null
    private var mPagerView: ViewPager? = null
    private var mPager2View: ViewPager2? = null

    private var mHandler: Handler? = null
    private var mQueue: ExecutorService? = null
    private var mInterrupt = false

    private var mRunnable: Runnable? = null

    fun start(parent: ViewGroup, call: Runnable) {
        mRunnable = call
        forceReset()
        goNext(parent, 0)
    }

    init {
        Thread {
            try {
                Looper.prepare()
                mHandler = Handler(Looper.myLooper()!!) {
                    val v = it.obj ?: return@Handler false
                    when (it.what) {
                        0 -> mQueue?.execute { check(v as ViewGroup) }
                        1 -> mQueue?.execute { checkInPager(v as ViewGroup) }
                        2 -> mQueue?.execute { checkInPager2(v as ViewGroup) }
                    }
                    return@Handler false
                }
                mQueue = Executors.newSingleThreadExecutor()
                Looper.loop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }


    private fun check(group: ViewGroup) {
        for (v in group.children) {
            if (mInterrupt) return
            if (v is ViewPager) {
                mPagerView = v
                goNext(v, 1)
            } else if (v is ViewPager2) {
                mPager2View = v
                goNext(v, 2)
            } else if (!viewCheck(v) && v is ViewGroup) {
                goNext(v, 0)
            } else if (viewCheck(v)) {
                viewDone(v)
                return
            }
        }
    }

    private fun checkInPager(group: ViewGroup) {
        if (group is ViewPager) {
            val currentItem = mPagerView?.currentItem
            for (v in group.children) {
                if (mInterrupt) return
                val params = v.layoutParams as ViewPager.LayoutParams
                val positionField = params.javaClass.getDeclaredField("position")
                positionField.isAccessible = true
                val position = positionField.get(v)
                if (!params.isDecor && currentItem == position) {
                    if (viewCheck(v)) {
                        viewDone(v)
                        return
                    } else if (v is ViewGroup) goNext(v, 1)
                }
            }
        } else {
            for (v in group.children) {
                if (mInterrupt) return
                if (viewCheck(v)) {
                    viewDone(v)
                    return
                } else if (v is ViewGroup) goNext(v, 1)
            }
        }
    }

    private fun checkInPager2(group: ViewGroup) {
        if (group is ViewPager2) {
            val currentItem = mPager2View?.currentItem ?: 0
            val field = ViewPager2::class.java.getDeclaredField("mLayoutManager")
            field.isAccessible = true
            val layoutManager = field.get(mPager2View) as LinearLayoutManager
            val currentView = (layoutManager.findViewByPosition(currentItem)) as ViewGroup
            if (viewCheck(currentView)) {
                viewDone(currentView)
                return
            }
            for (v in currentView.children) {
                if (mInterrupt) return
                if (viewCheck(v)) {
                    viewDone(v)
                    return
                } else if (v is ViewGroup) goNext(v, 2)
            }
        } else {
            for (v in group.children) {
                if (mInterrupt) return
                if (viewCheck(v)) {
                    viewDone(v)
                    return
                } else if (v is ViewGroup) goNext(v, 2)
            }
        }
    }

    private fun viewCheck(view: View): Boolean {
        return when (view) {
            is NestedChild -> true
            else -> false
        }
    }

    private fun viewDone(view: View) {
        mNestedRecyclerView = view as NestedChild
        mInterrupt = true
        mHandler?.removeCallbacksAndMessages(null)
        mRunnable?.run()
    }

    private fun goNext(v: ViewGroup, what: Int) {
        val message = Message()
        message.what = what
        message.obj = v
        mHandler?.sendMessage(message)
    }

    private fun forceReset() {
        mInterrupt = false
        mHandler?.removeCallbacksAndMessages(null)
        mNestedRecyclerView = null
        mPagerView = null
        mPager2View = null
    }
}
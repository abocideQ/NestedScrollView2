package lin.abcdq.darknessviewui.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import lin.abcdq.darknessviewui.R

class HimalayaActivity : AppCompatActivity() {

    private val mScrollView: NestedScrollView by lazy { findViewById(R.id.nsv_content1) }
    private val mView1: View by lazy { findViewById(R.id.v_content1) }
    private val mRecyclerView: RecyclerView by lazy { findViewById(R.id.rv_content1) }
    private val mList = arrayListOf<Any>()

    private val mLinearLayout: LinearLayout by lazy { findViewById(R.id.ll_content2) }
    private val mLinearLayout2: LinearLayout by lazy { findViewById(R.id.ll2_content2) }
    private val mView2: View by lazy { findViewById(R.id.v_content2) }
    private val mViewpager: ViewPager2 by lazy { findViewById(R.id.vp_content2) }
    private val mTabLayout: TabLayout by lazy { findViewById(R.id.tl_content2) }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_himalaya)
        initContent1()
        initContent2()
        //content1
        var height = QMUIDisplayHelper.getScreenHeight(this)
        var params = mView1.layoutParams
        params.height = height
        mView1.layoutParams = params
        val statHeight = QMUIDisplayHelper.getStatusBarHeight(this)
        mScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            val location = IntArray(2)
            mView1.getLocationOnScreen(location)
            if (location[1] < height) {
                mLinearLayout.scrollTo(0, height - location[1] + statHeight)
            }
        })
        //content2
        height = QMUIDisplayHelper.getScreenHeight(this) -
                QMUIDisplayHelper.dpToPx(60) -
                QMUIDisplayHelper.dpToPx(30)
        params = mViewpager.layoutParams
        params.height = height
        mViewpager.layoutParams = params
        params = mView2.layoutParams
        params.height = height
        mView2.layoutParams = params
        params = mLinearLayout2.layoutParams
        params.height = height + QMUIDisplayHelper.dpToPx(60) + QMUIDisplayHelper.dpToPx(30)
        mLinearLayout2.layoutParams = params
        var totalDy = 0f
        var lastDy = 0f
        mLinearLayout2.setOnTouchListener { _, ev ->
            if (ev.action == MotionEvent.ACTION_DOWN) {
                lastDy = ev.rawY
            } else if (ev.action == MotionEvent.ACTION_MOVE) {
                val dy = ev.rawY
                val diff = dy - lastDy
                lastDy = ev.rawY
                totalDy -= diff
                mLinearLayout.scrollBy(0, -1 * diff.toInt())
                if (diff > 0) {
                    val locView1 = IntArray(2)
                    mView1.getLocationOnScreen(locView1)
                    val locView2 = IntArray(2)
                    mLinearLayout2.getLocationOnScreen(locView2)
                    if (locView2[1] > locView1[1]) {
                        mScrollView.scrollBy(0, -1 * diff.toInt())
                    }
                }
            } else if (ev.action == MotionEvent.ACTION_UP) {
                if (totalDy > 200) {
                    mLinearLayout.scrollTo(0, height)
                } else {
                    val location = IntArray(2)
                    mView1.getLocationOnScreen(location)
                    if (location[1] < height) {
                        mLinearLayout.scrollTo(0, height - location[1] + statHeight)
                    } else {
                        mLinearLayout.scrollTo(0, 0)
                    }
                }
            }
            return@setOnTouchListener true
        }
    }

    private fun initContent1() {
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                return object : RecyclerView.ViewHolder(
                    LayoutInflater.from(baseContext).inflate(R.layout.item_layout1, parent, false)
                ) {}
            }

            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val textView = holder.itemView.findViewById<TextView>(R.id.tv_content)
                textView.text = "RecyclerView ${mList[holder.adapterPosition]}"
            }

            override fun getItemCount(): Int {
                return mList.size
            }
        }
        for (i in 0..50) mList.add("$i")
        mRecyclerView.adapter?.notifyItemRangeInserted(0, 50)
    }

    private fun initContent2() {
        val pagerHeight = QMUIDisplayHelper.getScreenHeight(this) - QMUIDisplayHelper.dpToPx(60)
        val params = mViewpager.layoutParams
        params.height = pagerHeight
        mViewpager.layoutParams = params
        mViewpager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 3
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> HimalayaFragment()
                    1 -> HimalayaFragment()
                    else -> HimalayaFragment()
                }
            }
        }
        val mediator = TabLayoutMediator(mTabLayout, mViewpager) { tab, position ->
            when (position) {
                0 -> tab.text = "item1"
                1 -> tab.text = "item2"
                2 -> tab.text = "item3"
            }
        }
        mediator.attach()
    }
}
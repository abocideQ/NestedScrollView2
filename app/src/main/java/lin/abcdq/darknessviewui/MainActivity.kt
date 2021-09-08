package lin.abcdq.darknessviewui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.qmuiteam.qmui.util.QMUIDisplayHelper

class MainActivity : AppCompatActivity() {

    private val mFragments = arrayOf(
        R.layout.item_layout1,
        R.layout.item_layout2,
        R.layout.item_layout3
    )

    private val mViewpager: ViewPager2 by lazy { findViewById(R.id.vp_content) }
    private val mTabLayout: TabLayout by lazy { findViewById(R.id.tb_content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val screenHeight = resources.displayMetrics.heightPixels -
                QMUIDisplayHelper.dpToPx(60) +
                QMUIDisplayHelper.getStatusBarHeight(this)
        val params = mViewpager.layoutParams
        params.height = screenHeight
        mViewpager.layoutParams = params
        mViewpager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 3
            }

            override fun createFragment(position: Int): Fragment {
                return MainFragment(mFragments[position])
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
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

    private val mViewpager: ViewPager2 by lazy { findViewById(R.id.vp_content) }
    private val mTabLayout: TabLayout by lazy { findViewById(R.id.tb_content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
                    0 -> MainFragment()
                    1 -> MainFragment2()
                    else -> MainFragment3()
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
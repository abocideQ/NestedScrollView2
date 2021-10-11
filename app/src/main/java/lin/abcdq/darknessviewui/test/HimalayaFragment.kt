package lin.abcdq.darknessviewui.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import lin.abcdq.darknessviewui.R

class HimalayaFragment : Fragment() {

    private val mList = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(context).inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView: RecyclerView = view.findViewById(R.id.rv_content)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                return object : RecyclerView.ViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.item_layout1, parent, false)
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
        recyclerView.adapter?.notifyItemRangeInserted(0, 50)

        val refreshLayout = view.findViewById<SmartRefreshLayout>(R.id.srl_content)
        refreshLayout.setEnableRefresh(false)
        refreshLayout.setOnLoadMoreListener {
            val index = mList.size
            for (i in 0..50) mList.add("更多$i")
            recyclerView.adapter?.notifyItemRangeInserted(index, 50)
            refreshLayout.finishLoadMore()
        }
    }
}
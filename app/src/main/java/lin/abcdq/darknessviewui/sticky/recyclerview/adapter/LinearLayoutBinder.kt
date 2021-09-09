package lin.abcdq.darknessviewui.sticky.recyclerview.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface LinearLayoutBinder {
    fun layout(position: Int): Int
    fun onBindViewHolder(layout: Int, holder: RecyclerView.ViewHolder, view: View)
}
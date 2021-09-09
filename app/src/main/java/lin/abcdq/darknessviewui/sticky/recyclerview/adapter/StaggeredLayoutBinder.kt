package lin.abcdq.darknessviewui.sticky.recyclerview.adapter

import lin.abcdq.darknessviewui.sticky.recyclerview.adapter.LinearLayoutBinder

interface StaggeredLayoutBinder : LinearLayoutBinder {
    fun spanFull(position: Int): Boolean
}
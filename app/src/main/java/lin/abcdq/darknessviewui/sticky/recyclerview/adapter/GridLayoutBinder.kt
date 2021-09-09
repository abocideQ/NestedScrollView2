package lin.abcdq.darknessviewui.sticky.recyclerview.adapter

interface GridLayoutBinder : LinearLayoutBinder {
    fun spanSize(position: Int): Int
}
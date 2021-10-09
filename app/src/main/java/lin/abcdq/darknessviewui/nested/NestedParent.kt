package lin.abcdq.darknessviewui.nested

interface NestedParent {

    fun canScrollVertically(p: Int): Boolean

    fun enablePager(enable: Boolean)

    fun scrollBy(x: Int, y: Int)

    fun fling()

}
package lin.abcdq.darknessviewui.nested

interface NestedChild {

    fun setParent(parent: NestedParent)

    fun touching(): Boolean

    fun fling(velocity: Float)

    fun stop()

}
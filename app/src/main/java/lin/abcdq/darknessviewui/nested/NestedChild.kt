package lin.abcdq.darknessviewui.nested

interface NestedChild {

    fun setParent(parent: NestedParent)

    fun fling(velocity: Float)

    fun stop()
}
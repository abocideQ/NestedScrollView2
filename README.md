#### 奇怪的知识
```
VelocityTracker 速度跟踪

private var mVelocityTracker = VelocityTracker.obtain() 
if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
    if (mVelocityTracker != null) mVelocityTracker.clear()
} else if (ev.action == MotionEvent.ACTION_MOVE) {
    if (mVelocityTracker != null) mVelocityTracker.addMovement(ev)
} else if (ev.action == MotionEvent.ACTION_UP) {
    mVelocityTracker.computeCurrentVelocity(100)
    val fl = -1 * mVelocityTracker.yVelocity.toInt() 
}
        
OverScroller 滑动小帮手

val filed = NestedScrollView::class.java.getDeclaredField("mScroller")
filed.isAccessible = true
val scroller = filed.get(?????) as OverScroller
scroller.fling(xxxxxx)
```
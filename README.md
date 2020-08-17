# BlinkLayout

界面loading 时暗块加反光效果的过渡页

1.原理其实是渐变绘制BlinkLayout 中的子view，并且有动态效果。渐变绘制会导致子view 的某些部分不被绘制，并且露出BlinkLayout（BlinkLayout 有背景色） 或者 更底层的Layout（BlinkLayout 没有背景），这样来模拟反光效果

2.no_blink 属性用来在布局文件中标记那些不参与渐变绘制的子view

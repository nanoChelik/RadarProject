package pubgradar.ui

private var variableYieldTime: Long = 0
private var lastTime:Long = 0

fun sync(fps: Int) {
    if (fps <= 0) return

    val sleepTime = (1000000000 / fps).toLong() // nanoseconds to sleep this frame
    // yieldTime + remainder micro & nano seconds if smaller than sleepTime
    val yieldTime = Math.min(sleepTime, variableYieldTime + sleepTime % (1000 * 1000))
    var overSleep: Long = 0 // time the sync goes over by

    try {
        while (true) {
            val t = System.nanoTime() - lastTime

            if (t < sleepTime - yieldTime) {
                Thread.sleep(1)
            } else if (t < sleepTime) {
                // burn the last few CPU cycles to ensure accuracy
                Thread.yield()
            } else {
                overSleep = t - sleepTime
                break // exit while loop
            }
        }
    } catch (e: InterruptedException) {
        e.printStackTrace()
    } finally {
        lastTime = System.nanoTime() - Math.min(overSleep, sleepTime)

        // auto tune the time sync should yield
        if (overSleep > variableYieldTime) {
            // increase by 200 microseconds (1/5 a ms)
            variableYieldTime = Math.min(variableYieldTime + 200 * 1000, sleepTime)
        } else if (overSleep < variableYieldTime - 200 * 1000) {
            // decrease by 2 microseconds
            variableYieldTime = Math.max(variableYieldTime - 2 * 1000, 0)
        }
    }
}
package com.icapps.niddler.lib.utils

import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Thread pool executor which is unbound in the number of tasks it can execute but does allow starting new background threads as required
 *
 * @author Nicola Verbeeck
 * @version 1
 * @param coreSize The minimum number of threads to keep alive
 * @param maxSize The maximum number of threads to use for job execution
 * @param keepAliveTime The time to keep a non-core thread alive when there is no work
 * @param keepAliveUnit The unit of the keepAliveTime parameter
 * @param queue The queue to use to keep jobs in
 * @param threadFactory The thread factory to use for creating threads
 */
class ScalingThreadPoolExecutor(coreSize: Int, maxSize: Int, keepAliveTime: Long, keepAliveUnit: TimeUnit, queue: ScalingBlockingQueue = ScalingBlockingQueue(),
                                threadFactory: ThreadFactory = Executors.defaultThreadFactory())
    : ThreadPoolExecutor(coreSize, maxSize, keepAliveTime, keepAliveUnit, queue, threadFactory) {

    private val activeCount = AtomicInteger()

    init {
        rejectedExecutionHandler = RejectedExecutionHandler { r, executor -> executor.queue.put(r) }
        queue.executor = this
    }

    override fun getActiveCount(): Int {
        return activeCount.get()
    }

    override fun beforeExecute(t: Thread, r: Runnable) {
        activeCount.incrementAndGet()
    }

    override fun afterExecute(r: Runnable, t: Throwable?) {
        activeCount.decrementAndGet()
    }

}

/**
 * Blocking queue that integrates with an executor to check if new tasks 'can be added'
 */
class ScalingBlockingQueue : LinkedBlockingQueue<Runnable>() {

    lateinit var executor: ThreadPoolExecutor

    override fun offer(e: Runnable): Boolean {
        val allWorkingThreads = executor.activeCount + super.size
        return allWorkingThreads < executor.poolSize && super.offer(e)
    }

}
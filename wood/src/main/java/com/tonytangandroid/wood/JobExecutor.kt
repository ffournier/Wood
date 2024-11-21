package com.tonytangandroid.wood

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class JobExecutor : Executor {
    private var threadPoolExecutor: ThreadPoolExecutor

    init {
        val workQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()
        threadPoolExecutor =
            ThreadPoolExecutor(
                INITIAL_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME.toLong(),
                KEEP_ALIVE_TIME_UNIT,
                workQueue,
                JobThreadFactory()
            )
    }

    override fun execute(runnable: Runnable) {
        threadPoolExecutor.execute(runnable)
    }

    internal class JobThreadFactory : ThreadFactory {
        private var counter = 0

        override fun newThread(runnable: Runnable): Thread {
            return Thread(runnable, THREAD_NAME + counter++)
        }

        companion object {
            const val THREAD_NAME: String = "log_"
        }
    }

    companion object {
        private const val INITIAL_POOL_SIZE: Int = 2
        private const val MAX_POOL_SIZE: Int = 5
        private const val KEEP_ALIVE_TIME: Int = 3
        private val KEEP_ALIVE_TIME_UNIT: TimeUnit = TimeUnit.SECONDS
    }
}

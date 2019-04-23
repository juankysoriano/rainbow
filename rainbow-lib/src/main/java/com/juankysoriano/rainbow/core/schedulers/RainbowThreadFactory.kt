package com.juankysoriano.rainbow.core.schedulers

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

internal class RainbowThreadFactory(
        private val prefix: String,
        private val priority: Int,
        private val threadGroup: ThreadGroup?,
        private val stackSize: Int
) : AtomicLong(), ThreadFactory {

    override fun newThread(r: Runnable): Thread {
        val name = prefix + '-'.toString() + incrementAndGet()
        val thread = Thread(threadGroup, r, name, stackSize.toLong())
        thread.priority = priority
        thread.isDaemon = true
        return thread
    }

    override fun toByte(): Byte = get().toByte()
    override fun toChar(): Char = get().toChar()
    override fun toShort(): Short = get().toShort()

    companion object {
        fun create(name: String, priority: Int): RainbowThreadFactory {
            return RainbowThreadFactory(name, priority, null, 0)
        }

        fun createForRecursion(name: String, priority: Int): RainbowThreadFactory {
            val threadGroup = ThreadGroup("$name-group")
            return RainbowThreadFactory(name, priority, threadGroup, Integer.MAX_VALUE)
        }
    }
}

package org.me.utils

import kotlinx.coroutines.runBlocking
import org.me.data.dao
import java.util.concurrent.atomic.AtomicLong

class AtomicRefs {
    companion object{
        val atomicImage = AtomicLong(runBlocking { dao.getAtomic().imageRef } )
        val atomicSound = AtomicLong(runBlocking{ dao.getAtomic().soundRef })
    }
}

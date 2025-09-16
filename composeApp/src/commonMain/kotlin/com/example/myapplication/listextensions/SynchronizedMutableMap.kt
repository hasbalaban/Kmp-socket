package com.example.myapplication.listextensions

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SynchronizedMutableMap<K, V> {
    private val mutex = Mutex()
    private val map = mutableMapOf<K, V>()

    suspend operator fun get(key: K?): V? = mutex.withLock { map[key] }
    suspend operator fun set(key: K, value: V) = mutex.withLock { map[key] = value }
    suspend fun getOrPut(key: K, defaultValue: () -> V): V = mutex.withLock { map.getOrPut(key, defaultValue) }


    suspend fun <R> withMap(action: (Map<K, V>) -> R): R {
        return mutex.withLock {
            action(map)
        }
    }

    suspend fun remove(key: K): V? {
        return mutex.withLock {
            map.remove(key)
        }
    }

    suspend fun clear() {
        mutex.withLock {
            map.clear()
        }
    }

    suspend fun getAll(): Map<K, V> {
        return mutex.withLock {
            map.toMap()
        }
    }

}
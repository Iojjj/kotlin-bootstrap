package com.github.iojjj.bootstrap.adapters.selection.keys.mappers

interface KeyMapper<T, K> {

    fun toKey(item: T): K

    fun fromKey(key: K): T
}
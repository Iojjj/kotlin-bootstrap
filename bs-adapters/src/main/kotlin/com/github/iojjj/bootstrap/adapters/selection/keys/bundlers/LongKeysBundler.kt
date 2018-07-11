package com.github.iojjj.bootstrap.adapters.selection.keys.bundlers

import android.os.Bundle
import com.github.iojjj.bootstrap.utils.Bundler

private const val KEY_LONG_KEYS = "com.github.iojjj.bootstrap.adapters.selection.keys.LONG_KEYS"

class LongKeysBundler : Bundler<Collection<Long>> {

    override fun read(bundle: Bundle): Collection<Long>? = bundle.getLongArray(
            KEY_LONG_KEYS).toList()

    override fun write(item: Collection<Long>?, bundle: Bundle) = bundle.putLongArray(
            KEY_LONG_KEYS, item?.toLongArray())
}
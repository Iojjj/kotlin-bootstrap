package com.github.iojjj.bootstrap.adapters.beta.selection.keys.bundlers

import android.os.Bundle
import com.github.iojjj.bootstrap.beta.utils.Bundler

private const val KEY_STRING_KEYS = "com.github.iojjj.bootstrap.adapters.beta.selection.keys.STRING_KEYS"

class StringKeysBundler : Bundler<Collection<String>> {

    override fun read(bundle: Bundle): Collection<String>? = bundle.getStringArrayList(KEY_STRING_KEYS)

    override fun write(item: Collection<String>?, bundle: Bundle) =
            bundle.putStringArrayList(KEY_STRING_KEYS, if (item == null) null else ArrayList(item))
}


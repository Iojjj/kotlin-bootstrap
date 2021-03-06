package com.github.iojjj.bootstrap.utils

import android.os.Bundle

interface Bundler<T> {

    fun read(bundle: Bundle): T?

    fun write(item: T?, bundle: Bundle)
}
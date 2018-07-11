package com.github.iojjj.bootstrap.utils

import android.os.Bundle

interface StateAware {

    fun onSaveInstanceState(outState: Bundle)

    fun onRestoreInstanceState(savedInstanceState: Bundle?)
}
package com.tonytangandroid.wood

internal interface Callback<T> {
    fun onEmit(event: T)
}

package com.tonytangandroid.wood

import android.util.Log

enum class LogLevel(val level: Int) {
    ASSERT(Log.ASSERT),
    ERROR(Log.ERROR),
    WARN(Log.WARN),
    INFO(Log.INFO),
    DEBUG(Log.DEBUG),
    VERBOSE(Log.VERBOSE),
    UNKNOWN(-1);
}

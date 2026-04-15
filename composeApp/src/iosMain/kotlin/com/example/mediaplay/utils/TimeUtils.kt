package com.example.mediaplay.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun getCurrentTimeMillis(): Long {
    return (NSDate.date().timeIntervalSince1970 * 1000).toLong()
}

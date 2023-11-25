package com.alab.extra_bouncy.util

interface OnOverScrollOffsetListener
{
    fun onOverScrollStart(overscrollMode: Int)

    fun onOverScrollOffset(overscrollMode: Int, offset: Int)

    fun onOverScrollRelease(overscrollMode: Int)

    fun onOverScrollEnd()
}
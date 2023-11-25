package com.alab.extra_bouncy.util

interface OnOverPullListener
{
    fun onOverPulledLeft(deltaDistance: Float)

    fun onOverPulledTop(deltaDistance: Float)

    fun onOverPulledRight(deltaDistance: Float)

    fun onOverPulledBottom(deltaDistance: Float)

    fun onRelease()
}
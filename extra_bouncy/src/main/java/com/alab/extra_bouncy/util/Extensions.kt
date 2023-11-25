package com.alab.extra_bouncy.util

import android.view.View

/**
 * Sets the padding on the left.
 */
internal fun View.setPaddingLeft(paddingLeft: Int) {
    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
}

/**
 * Sets the padding on the top.
 */
internal fun View.setPaddingTop(paddingTop: Int) {
    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
}

/**
 * Sets the padding on the right.
 */
internal fun View.setPaddingRight(paddingRight: Int) {
    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
}

/**
 * Sets the padding on the bottom.
 */
internal fun View.setPaddingBottom(paddingBottom: Int) {
    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
}
package com.alab.extrabouncy

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.alab.extra_bouncy.util.Bouncy
import com.alab.extra_bouncy.util.OnOverScrollOffsetListener
import com.alab.extrabouncy.databinding.RvHorizontalActivityBinding

class RVHorizontalActivity : Activity() {

    private val data = (0..9).map { it.toString() }.toList()

    private val binding: RvHorizontalActivityBinding by lazy {
        RvHorizontalActivityBinding.inflate(
            LayoutInflater.from(
                this
            )
        )
    }

    private val rvAdapter = RVAdapter(data)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rv1.adapter = rvAdapter
        binding.rv2.adapter = rvAdapter
        binding.rv3.apply {
            adapter = rvAdapter
            onOverScrollOffsetListener = rv3PullEvent
        }
        binding.rv4.apply {
            adapter = rvAdapter
            onOverScrollOffsetListener = rv4PullEvent
        }


    }

    private val rv3PullEvent = object : OnOverScrollOffsetListener {
        private var isPullEvent = false

        override fun onOverScrollStart(overscrollMode: Int) {}

        @SuppressLint("MissingPermission")
        override fun onOverScrollOffset(overscrollMode: Int, offset: Int) {
            if (offset < binding.iv3.width) {
                binding.iv3.translationX = -offset.toFloat()
                val delta = (offset * 100 / binding.iv3.width) / 100f
                binding.iv3.apply {
                    alpha = delta
                    scaleX = delta
                    scaleY = delta
                }
                isPullEvent = false
            } else {
                isPullEvent = true
                binding.iv3.apply {
                    translationX = -binding.iv3.width.toFloat()
                    alpha = 1f
                    scaleX = 1f
                    scaleY = 1f
                }
            }
        }

        override fun onOverScrollRelease(overscrollMode: Int) {
            if (isPullEvent) {
                Toast.makeText(this@RVHorizontalActivity, "Pull event", Toast.LENGTH_LONG).show()
            }
        }

        override fun onOverScrollEnd() {}
    }

    private val rv4PullEvent = object : OnOverScrollOffsetListener {
        private var isStartPullEvent = false
        private var isEndPullEvent = false

        override fun onOverScrollStart(overscrollMode: Int) {}

        @SuppressLint("MissingPermission")
        override fun onOverScrollOffset(overscrollMode: Int, offset: Int) {
            if (overscrollMode == Bouncy.OVERSCROLL_START) {
                if (offset < binding.iv4Start.width) {
                    binding.iv4Start.translationX = offset.toFloat()
                    val delta = (offset * 100 / binding.iv4Start.width) / 100f
                    binding.iv4Start.apply {
                        alpha = delta
                        scaleX = delta
                        scaleY = delta
                    }
                    isStartPullEvent = false
                } else {
                    isStartPullEvent = true
                    binding.iv4Start.apply {
                        translationX = binding.iv4Start.width.toFloat()
                        alpha = 1f
                        scaleX = 1f
                        scaleY = 1f
                    }
                }
            }

            if (overscrollMode == Bouncy.OVERSCROLL_END) {
                if (offset < binding.iv4End.width) {
                    binding.iv4End.translationX = -offset.toFloat()
                    val delta = (offset * 100 / binding.iv4End.width) / 100f
                    binding.iv4End.apply {
                        alpha = delta
                        scaleX = delta
                        scaleY = delta
                    }
                    isEndPullEvent = false
                } else {
                    isEndPullEvent = true
                    binding.iv4End.apply {
                        translationX = -binding.iv4End.width.toFloat()
                        alpha = 1f
                        scaleX = 1f
                        scaleY = 1f
                    }
                }
            }

        }

        override fun onOverScrollRelease(overscrollMode: Int) {
            if (isStartPullEvent) {
                Toast.makeText(this@RVHorizontalActivity, "Start pull event", Toast.LENGTH_LONG).show()
            }
            if (isEndPullEvent) {
                Toast.makeText(this@RVHorizontalActivity, "End pull event", Toast.LENGTH_LONG).show()
            }
        }

        override fun onOverScrollEnd() {}
    }

}
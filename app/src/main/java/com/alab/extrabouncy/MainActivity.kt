package com.alab.extrabouncy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.rvHorizontal).setOnClickListener {
            startActivity(Intent(this, RVHorizontalActivity::class.java))
        }
    }

}

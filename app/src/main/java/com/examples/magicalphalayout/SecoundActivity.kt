package com.examples.magicalphalayout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main_2.*

class SecoundActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_2)
        start.setOnClickListener {
            startActivity(Intent(this@SecoundActivity, MainActivity::class.java))
        }
    }
}
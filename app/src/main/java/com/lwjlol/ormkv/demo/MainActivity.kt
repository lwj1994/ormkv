package com.lwjlol.ormkv.demo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateText()
        val user = User


//      UserSp.name

//      UserSp.reset()
    }

    @SuppressLint("SetTextI18n")
    private fun updateText() {
    }

}

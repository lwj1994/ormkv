package com.lwjlol.chunchunsp.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateText()

        updateButton.setOnClickListener {
            UserInfos_CCSP.name = nameEditText.text.toString()
            UserInfos_CCSP.age = ageEditText.text.toString().toDouble().toInt()
            UserInfos_CCSP.id = idEditText.text.toString().toDouble().toLong()
            UserInfos_CCSP.isMan = manCheckBox.isChecked
            updateText()
        }

        clearButton.setOnClickListener {
            UserInfos_CCSP.clear()
            updateText()
        }
    }

    private fun updateText() {
        debugTextView.text = "name = ${UserInfos_CCSP.name}" +
                "\n age = ${UserInfos_CCSP.age}" +
                "\n id = ${UserInfos_CCSP.id}" +
                "\n isMan = ${UserInfos_CCSP.isMan}"
    }
}

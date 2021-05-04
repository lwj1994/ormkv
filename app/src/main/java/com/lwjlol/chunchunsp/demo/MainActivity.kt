package com.lwjlol.chunchunsp.demo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.test.UserSp
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateText()

        updateButton.setOnClickListener {
            if (nameEditText.text.isNotEmpty()) {
                UserSp.name = nameEditText.text.toString()
            }
            if (ageEditText.text.isNotEmpty()) {
                UserSp.age = ageEditText.text.toString().toDouble().toInt()
            }
            if (idEditText.text.isNotEmpty()) {
                UserSp.id = idEditText.text.toString().toDouble().toLong()
            }
            UserSp.isMan = manCheckBox.isChecked
            UserSp.temperature = 36.3F
            updateText()
        }

        clearButton.setOnClickListener {
            UserSp.clear()
            updateText()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateText() {
        debugTextView.text = "name = ${UserSp.name}" +
                "\n age = ${UserSp.age}" +
                "\n id = ${UserSp.id}" +
                "\n isMan = ${UserSp.isMan}" +
                "\n temperature = ${UserSp.temperature}"
    }

}

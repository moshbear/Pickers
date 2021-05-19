package com.moshy.pickers.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import com.moshy.pickers.R

class MainActivity : AppCompatActivity() {
    companion object {
        private val navigations = listOf<Pair<String, Class<*>?>>(
            Pair("first (removed)", null),
            Pair("second", SecondActivity::class.java),
            Pair("third", ThirdActivity::class.java)
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        findViewById<LinearLayout>(R.id.main_activity_layout)?.let { layout ->
            navigations.forEach { nav ->
                Button(this@MainActivity).apply {
                    text = nav.first
                    setOnClickListener { _ ->
                        nav.second?.apply {
                            Intent(this@MainActivity, this).run newActivity@{
                                startActivity(this)
                            }
                        }
                    }
                    layout.addView(this)
                }
            }
        }
    }
}
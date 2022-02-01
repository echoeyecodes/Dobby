package com.echoeyecodes.dobby.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.echoeyecodes.dobby.utils.ActivityUtil


class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityUtil.startMainActivity(this)
        finish()
    }
}
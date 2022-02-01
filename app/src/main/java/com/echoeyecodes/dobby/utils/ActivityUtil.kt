package com.echoeyecodes.dobby.utils

import android.content.Context
import android.content.Intent
import com.echoeyecodes.dobby.activities.MainActivity

class ActivityUtil {

    companion object {
        fun startMainActivity(context: Context){
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}
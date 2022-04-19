package com.echoeyecodes.dobby.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.TypedValue
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.echoeyecodes.dobby.db.models.FileDBModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import java.util.regex.Pattern
import kotlin.math.max

fun fillEmptySpace(
    recyclerView: RecyclerView?,
    holder: RecyclerView.ViewHolder,
    itemCount: Int,
    position: Int
) {
    val isLastItem = itemCount - 1 == position
    if (recyclerView == null) return

    val lastItemView = holder.itemView

    if (isLastItem) {
        lastItemView.doOnLayout {
            val recyclerViewHeight = recyclerView.height
            val lastItemBottom = lastItemView.bottom
            val heightDifference = recyclerViewHeight - lastItemBottom
            if (heightDifference > 0) {
                lastItemView.layoutParams.height = lastItemView.height + heightDifference
            }
        }
    } else {
        lastItemView.layoutParams.height = 300.convertToDp()
    }
}

fun getNotificationBitmapFromUrl(
    imageUrl: String
) = runBlocking {
    withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val input = url.openStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            null
        }
    }
}

fun Int.convertToDp(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )
        .toInt()
}

fun Float.convertToDp(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )
        .toInt()
}

fun TextInputEditText.getTextInputLayout(): TextInputLayout {
    return this.parent.parent as TextInputLayout
}

fun String.toImageMetadataUrl(): String {
    return this.replace("upload/", "upload/w_500/")
}

fun String.convertHexToColor(): Int {
    return Color.parseColor(this)
}

fun Int.getDayOfWeek(): String {
    return when (this) {
        1 -> "Sun"
        2 -> "Mon"
        3 -> "Tue"
        4 -> "Wed"
        5 -> "Thur"
        6 -> "Fri"
        else -> "Sat"
    }
}

fun Int.getMonthOfYear(): String {
    return when (this) {
        0 -> "Jan"
        1 -> "Feb"
        2 -> "Mar"
        3 -> "Apr"
        4 -> "May"
        5 -> "Jun"
        6 -> "Jul"
        7 -> "Aug"
        8 -> "Sept"
        9 -> "Oct"
        10 -> "Nov"
        else -> "Dec"
    }
}

fun Calendar.getCalendarDate(): String {
    val dayOfWeek = this.get(Calendar.DAY_OF_WEEK).getDayOfWeek()
    val month = this.get(Calendar.MONTH)
    val date = this.get(Calendar.DAY_OF_MONTH)

    val hour = "0".plus(this.get(Calendar.HOUR_OF_DAY))
    val minute = "0".plus(this.get(Calendar.MINUTE))

    return "${dayOfWeek}, ${month.getMonthOfYear()} $date (${
        hour.substring(
            max(
                hour.length - 2,
                0
            )
        )
    }:${
        minute.substring(
            max(minute.length - 2, 0)
        )
    })"
}

fun Long.toTimeFormat(): String {
    val seconds = this / 1000L

    return when {
        seconds < 60L -> {
            "few seconds"
        }
        seconds / 60L < 60L -> {
            delimitTime((seconds / 60).toInt(), "minute")
        }
        seconds / 60L / 60L < 24L -> {
            delimitTime((seconds / 60 / 60).toInt(), "hour")
        }
        seconds / 60L / 60L / 24L < 8L -> {
            delimitTime((seconds / 60 / 60 / 24).toInt(), "day")
        }
        seconds / 60L / 60L / 24L / 7L < 5L -> {
            delimitTime((seconds / 60 / 60 / 24 / 7).toInt(), "week")
        }
        seconds / 60L / 60L / 24L / 7L / 4L < 13L -> {
            delimitTime((seconds / 60 / 60 / 24 / 7 / 4).toInt(), "month")
        }
        else -> {
            delimitTime((seconds / 60L / 60L / 24L / 7L / 4L / 12L).toInt(), "year")
        }
    }
}

private fun delimitTime(value: Int, suffix: String): String {
    return if (value <= 1) {
        "$value $suffix"
    } else {
        "$value ${suffix}s"
    }
}

@SuppressLint("SimpleDateFormat")
fun String.convertToTimeDifference(reverse: Boolean = false): String {

    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }.parse(this)
        val start = Date(dateFormat!!.time).toInstant()
        val end = Instant.now()

        if (reverse) {
            return (Duration.between(end, start)).toMillis().toTimeFormat()
        }
        return (Duration.between(start, end)).toMillis().toTimeFormat()
    } catch (exception: Exception) {
        "few mins"
    }

}

@SuppressLint("SimpleDateFormat")
fun String.convertStringToDate(): Date {

    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }.parse(this)
        Date(dateFormat!!.time)

    } catch (exception: Exception) {
        Date()
    }

}

@SuppressLint("SimpleDateFormat")
fun String.convertStringToCalendar(): Calendar {
    return Calendar.getInstance().apply {
        time = convertStringToDate()
    }
}


fun Long.toFileSize(): String {
    return String.format("%.2f", this.toDouble() / 1000 / 1000).plus("MB")
}

fun String.replaceWordWithDelimiter(position: Int, replacement: String): String {
    val currentWordPair = this.getCursorWordSelection(position)

    return this.replaceRange(
        currentWordPair.first,
        currentWordPair.first + currentWordPair.second.length,
        replacement
    )
}

fun String.getCursorWordSelection(position: Int): Pair<Int, String> {
    val pattern = Pattern.compile("\\S+");
    val matcher = pattern.matcher(this)
    var currentWord = ""
    var start = 0
    var end: Int

    while (matcher.find()) {
        start = matcher.start();
        end = matcher.end();
        if (start <= position && position <= end) {
            currentWord = this.subSequence(start, end).toString();
            break;
        }
    }
    return Pair(kotlin.math.max(0, (start)), currentWord)
}

fun Long.toMetricCount(): String {
    var value = this.toDouble()
    val arr = arrayOf("", "K", "M", "B", "T", "P", "E")
    var index = 0
    while (value / 1000 >= 1) {
        value /= 1000
        index++
    }
    val decimalFormat = DecimalFormat("#.##")
    return java.lang.String.format("%s%s", decimalFormat.format(value), arr[index])
}

fun Long.getFormattedSize(): String {
    return String.format("%.2f", this.toDouble() / 1000 / 1000).plus("MB")
}

fun String.getFormattedDate(): String {
    return try {
        val timestamp = LocalDateTime.parse(this)
        "${timestamp.dayOfMonth}-${timestamp.monthValue}-${timestamp.year}"
    } catch (exception: Exception) {
        "N/A"
    }
}

fun DownloadStatus.encode(): Int {
    return when (this) {
        DownloadStatus.WAITING -> 0
        DownloadStatus.DOWNLOADING -> 1
        DownloadStatus.COMPLETE -> 2
        DownloadStatus.FAILED -> 3
        DownloadStatus.CANCELLED -> 4
    }
}

fun Int.decode(): DownloadStatus {
    return when (this) {
        0 -> DownloadStatus.WAITING
        1 -> DownloadStatus.DOWNLOADING
        2 -> DownloadStatus.COMPLETE
        3 -> DownloadStatus.FAILED
        else -> DownloadStatus.CANCELLED
    }
}

fun FileDBModel.getPath(context: Context): String {
    return if (this.fileExists && status == DownloadStatus.COMPLETE) {
        path!!
    } else {
        thumbnail
    }
}
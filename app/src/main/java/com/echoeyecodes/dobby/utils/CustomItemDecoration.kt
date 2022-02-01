package com.echoeyecodes.dobby.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class CustomItemDecoration : ItemDecoration {
    private val top: Int
    private val left: Int
    private val bottom: Int
    private val right: Int

    constructor(space: Int) {
        top = space.convertToDp()
        bottom = space.convertToDp()
        left = space.convertToDp()
        right = space.convertToDp()
    }

    constructor(vertical: Int, horizontal: Int) {
        top = vertical.convertToDp()
        bottom = vertical.convertToDp()
        left = horizontal.convertToDp()
        right = horizontal.convertToDp()
    }

    constructor(top: Int, right: Int, bottom: Int, left: Int) {
        this.top = top.convertToDp()
        this.bottom = bottom.convertToDp()
        this.left = left.convertToDp()
        this.right = right.convertToDp()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = left
        outRect.right = right
        outRect.top = top
        outRect.bottom = bottom
    }
}
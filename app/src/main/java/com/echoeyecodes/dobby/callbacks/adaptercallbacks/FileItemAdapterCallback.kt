package com.echoeyecodes.dobby.callbacks.adaptercallbacks

import com.echoeyecodes.dobby.db.models.FileDBModel

interface FileItemAdapterCallback {
    fun onMorePressed(id: String)
    fun onItemPressed(model:FileDBModel)
}
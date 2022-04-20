# Dobby
This project is an app for downloading videos and photos from Twitter, Instagram, and DeviantArt. It's one of my personal projects I worked on to develop my skills in android development, and one of the very few that got to see the light of day😅

# Favorite improved change so far
As you may observe from the [commit logs](https://github.com/echoeyecodes/ClipClip/commits/main), I initially had the download progress for every file cached to sqlite db with room. That means really frequent writes to the db was being made depending on the speed of the download. thought this might be a bad idea for performance reasons, so i decided to change this by only monitoring the download progress via callbacks, and updating the respective content on the recyclerview directly without caching it.
```Kotlin
//MainActivity.kt
 private fun updateDownloadProgress(id: String, bytesRead: Long, size: Long) {
        runOnUiThread {
            val currentList = fileAdapter.currentList
            val position = currentList.indexOfFirst { it.id == id }
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            viewHolder?.let {
                if (it is FileItemAdapter.FileItemAdapterViewHolder) {
                    it.updateProgress(bytesRead, size)
                }
            }
        }
    }

    private val downloadManagerCallback = object : DownloadManagerCallbackImpl {
        override fun onDownloadProgress(id: String, bytesRead: Long, total: Long) {
            super.onDownloadProgress(id, bytesRead, total)
            updateDownloadProgress(id, bytesRead, total)
        }
    }
```

On initial load of the download items in the recycler view, the total bytes downloaded is determined by querying the file size from the android system directly and this information is used to determine the download progress of the file if not yet completely downloaded. the download progress callback handles the rest of the progress content shown in the recyclerview
```Kotlin
// MainActivityViewModel.kt
private var files: LiveData<List<FileDBModel>> =
        Transformations.switchMap(fileRepository.getFilesLiveData()) { mapData ->
            liveData<List<FileDBModel>> {
                if (temp.isNotEmpty()) {
                    val previousData = mapData.map { mData ->
                        val prev = temp.find { it.id == mData.id }
                        if (prev != null) {
                            mData.fileSize = prev.fileSize
                            mData.fileExists = prev.fileExists
                        }
                        mData
                    }
                    emit(previousData)
                }
                val newData = mapData.map {
                    val file = it
                    val fileInfo = FileUtils.getFileInfo(application, file.path ?: "")
                    file.fileExists = fileInfo.exists
                    file.fileSize = fileInfo.size
                    file
                }

                /** cache previously formatted data, to prerender before recalculating
                file metadata for new files
                 **/
                temp.clear()
                temp.addAll(newData)
                emit(newData)
            }
        }
```

It is particularly useful to me, and fun to work on this cos I share a lot of content on my WhatsApp status which are mostly gotten from Instagram and Twitter. In the past, I used 3rd party apps/site to download these contents on my device before sharing, until I had an epiphany on developing my own custom app for this😊 Started this WAY back, but recently got updated with major changes for the V2 as a result of what I've known so for about Android development generally.

## How to run the project
No special setup is required to run and test this on your device. Just clone, and build the project as is and you're good to go!

## Screenshots
![Screenshot 1](https://res.cloudinary.com/dfzhxlgll/image/upload/v1650459329/932847e9owdf_f4rkgc.jpg)
![Screenshot 2](https://res.cloudinary.com/dfzhxlgll/image/upload/v1650459329/09876y5321tyui3o_e66hlw.jpg)
![Screenshot 3](https://res.cloudinary.com/dfzhxlgll/image/upload/v1650459329/093248r7sdfgh_aaepoj.png)

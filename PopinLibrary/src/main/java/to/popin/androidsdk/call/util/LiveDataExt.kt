package to.popin.androidsdk.call.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.hide(): LiveData<T> = this

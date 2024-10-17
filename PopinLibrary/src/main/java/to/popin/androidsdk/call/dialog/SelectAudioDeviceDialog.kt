package to.popin.androidsdk.call.dialog

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.widget.ArrayAdapter
import com.twilio.audioswitch.AudioDevice
import to.popin.androidsdk.call.CallViewModel

fun Activity.showSelectAudioDeviceDialog(callViewModel: CallViewModel) {
    val builder = with(AlertDialog.Builder(this)) {
        setTitle("Select Audio Device")

        val audioHandler = callViewModel.audioHandler
        val audioDevices = audioHandler.availableAudioDevices
        val myPreferredDeviceList by lazy(LazyThreadSafetyMode.NONE) {
            listOf(
                AudioDevice.BluetoothHeadset::class.java,
                AudioDevice.WiredHeadset::class.java,
                AudioDevice.Speakerphone::class.java,
                AudioDevice.Earpiece::class.java,
            )
        }
        val arrayAdapter = ArrayAdapter<String>(this@showSelectAudioDeviceDialog, R.layout.select_dialog_item)
        arrayAdapter.addAll(audioDevices.map { it.name })
        setAdapter(arrayAdapter) { dialog, index ->
            audioHandler.selectDevice(audioDevices[index])
            dialog.dismiss()
        }
        audioHandler.preferredDeviceList = myPreferredDeviceList;
    }
  //  builder.show()
}

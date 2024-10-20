package to.popin.androidsdk.call

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.twilio.audioswitch.AudioDevice
import com.xwray.groupie.GroupieAdapter
import to.popin.androidsdk.Popin
import to.popin.androidsdk.R
import to.popin.androidsdk.databinding.ActivityCallBinding
import to.popin.androidsdk.models.FastCallModel
import kotlinx.coroutines.flow.collectLatest


class CallActivity : AppCompatActivity() {

    val viewModel: CallViewModel by viewModelByFactory {
        val model = intent.getParcelableExtra<FastCallModel>("CALL")
        CallViewModel(model, application)
    }

    lateinit var binding: ActivityCallBinding
    private val screenCaptureIntentLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode != Activity.RESULT_OK || data == null) {
                return@registerForActivityResult
            }
            viewModel.startScreenCapture(data)
        }

    fun setPreferredDeviceList() {
        val myPreferredDeviceList by lazy(LazyThreadSafetyMode.NONE) {
            listOf(
                AudioDevice.BluetoothHeadset::class.java,
                AudioDevice.WiredHeadset::class.java,
                AudioDevice.Speakerphone::class.java,
                AudioDevice.Earpiece::class.java,
            )
        }
        val audioHandler = viewModel.audioHandler
        audioHandler.preferredDeviceList = myPreferredDeviceList;
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent!!)
        // Finish the current activity and start a new instance
        finish()
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding = ActivityCallBinding.inflate(layoutInflater)
        viewModel.callModel?.let { logEvent("call_create", "call_id", it.id) }
        setContentView(binding.root)




//        val mode = intent.getIntExtra("MODE", 0);
//        if (mode != 1 ) {
//            binding.sendProductFab.visibility = View.GONE
//            binding.managerActionFab.visibility = View.GONE
//        }
        // Audience row setup
        val audienceAdapter = GroupieAdapter()
        //   audienceAdapter.oncl
        audienceAdapter.setOnItemClickListener { item, _ ->
            val positionClicked = audienceAdapter.getAdapterPosition(item)
            viewModel.setPrimaryWindow(positionClicked)
            viewModel.callModel?.let { logEvent("call_change_primary", "call_id", it.id) }
        }
        binding.audienceRow.apply {
            layoutManager =
                LinearLayoutManager(this@CallActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = audienceAdapter
        }


        lifecycleScope.launchWhenCreated {
            viewModel.participants
                .collect { participants ->
                    Log.e("PARTICPANT", "MAP")
                    val items = participants.map { participant ->
                        ParticipantItem(
                            viewModel.room,
                            participant
                        )
                    }
                    Log.e("PARTICPANT", "CNT>" + items.count())
                    audienceAdapter.update(items)

                }
        }

        // speaker view setup
        val speakerAdapter = GroupieAdapter()
        binding.speakerView.apply {
            layoutManager =
                LinearLayoutManager(this@CallActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = speakerAdapter
        }



        lifecycleScope.launchWhenCreated {
            viewModel.primarySpeaker.collectLatest { speaker ->
                val items = listOfNotNull(speaker)
                    .map { participant ->
                        ParticipantItem(
                            viewModel.room,
                            participant,
                            speakerView = true
                        )
                    }
                speakerAdapter.update(items)
            }
        }

        Log.e("ADA", "1")
        // Controls setup
        viewModel.cameraEnabled.observe(this) { enabled ->
            Log.e("ADA", "2")
            logEvent("call_camera_enabled", "enabled", if (enabled) 1 else 0)
            binding.camera.setOnClickListener { viewModel.setCameraEnabled(!enabled) }
            binding.camera.setImageResource(
                if (enabled) {
                    R.drawable.outline_videocam_24
                } else {
                    R.drawable.outline_videocam_off_24
                },
            )
            binding.flipCamera.isEnabled = enabled
        }
        viewModel.micEnabled.observe(this) { enabled ->
            logEvent("call_mic_enabled", "enabled", if (enabled) 1 else 0)
            binding.mic.setOnClickListener { viewModel.setMicEnabled(!enabled) }
            binding.mic.setImageResource(
                if (enabled) {
                    R.drawable.outline_mic_24
                } else {
                    R.drawable.outline_mic_off_24
                },
            )
        }

        binding.flipCamera.setOnClickListener {
            viewModel.callModel?.let { logEvent("call_flip_camera", "call_id", it.id) }
            viewModel.flipCamera()
        }



        viewModel.screenshareEnabled.observe(this) { enabled ->
            logEvent("call_screenshare", "enabled", if (enabled) 1 else 0)
            binding.screenShare.setOnClickListener {
                if (enabled) {
                    viewModel.stopScreenCapture()
                } else {
                    requestMediaProjection()
                }
            }
            binding.screenShare.setImageResource(
                if (enabled) {
                    R.drawable.baseline_cast_connected_24
                } else {
                    R.drawable.baseline_cast_24
                },
            )
        }

        binding.message.setOnClickListener {
            val editText = EditText(this)
            AlertDialog.Builder(this)
                .setTitle("Send Message")
                .setView(editText)
                .setPositiveButton("Send") { dialog, _ ->
                    viewModel.sendData(editText.text?.toString() ?: "")
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .create()
                .show()
        }

        binding.exit.setOnClickListener { finish() }

        // Controls row 2
        setPreferredDeviceList();

        lifecycleScope.launchWhenCreated {
            viewModel.permissionAllowed.collect { allowed ->
                val resource =
                    if (allowed) R.drawable.account_cancel_outline else R.drawable.account_cancel
                //  binding.permissions.setImageResource(resource)
            }
        }

        val back = this.onBackPressedDispatcher
        back.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                Log.e("AD","3")
                enterPip();
            }
        })

        binding.actionChat.setOnClickListener({
            enterPip();
        })


        viewModel.getAction()?.observe(this) { action ->
            if (action.value == Action.CLOSE_ACTIVITY) {
                finish()
            }
        }
        viewModel.getEventLog()?.observe(this) {eventLog ->
            logEvent(eventLog.eventName,eventLog.key,eventLog.value)
        }

        viewModel.getConnectionStatus()?.observe(this) {connectionStatus ->
            runOnUiThread {
                binding.textConnecting.text = connectionStatus.status
                if (connectionStatus.status.length == 0) {
                    binding.textConnecting.visibility = View.GONE
                } else {
                    binding.textConnecting.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            binding.mic.isEnabled = true
            binding.camera.isEnabled = true
            binding.flipCamera.isEnabled = true

        }

        lifecycleScope.launchWhenStarted {
            viewModel.connectToRoom()
        }

    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        if (isInPictureInPictureMode) {
            binding.linearLayoutSideButtons.visibility = View.GONE
            binding.exit.visibility = View.GONE
            binding.audienceRow.visibility = View.GONE
            binding.textArtifact.visibility = View.GONE
        } else {
            binding.linearLayoutSideButtons.visibility = View.VISIBLE
            binding.exit.visibility = View.VISIBLE
            binding.audienceRow.visibility = View.VISIBLE
            binding.textArtifact.visibility = View.VISIBLE
        }
        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            finishAndRemoveTask()
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }



    override fun onResume() {
        super.onResume()

        lifecycleScope.launchWhenResumed {
            viewModel.error.collect {
                if (it != null) {
                    logEvent("call_resume_error", "$it", 0)
                    Toast.makeText(this@CallActivity, "Error: $it", Toast.LENGTH_LONG).show()
                    viewModel.dismissError()
                }
            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.dataReceived.collect {
                Toast.makeText(this@CallActivity, "Data received: $it", Toast.LENGTH_LONG).show()
            }
        }
        actionBar?.hide()
        supportActionBar?.hide()
    }

    private fun requestMediaProjection() {
        val mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenCaptureIntentLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    override fun onDestroy() {
        binding.audienceRow.adapter = null
        binding.speakerView.adapter = null


        if (viewModel.getShowRemarks()) {
            //mainThreadBus.post(new CaptureRemarkEvent(call_id));

        }

        super.onDestroy()
    }

    fun logEvent(eventName: String, key: String, value: Int) {
       // val app: Popin = application as Popin
       // app.logEvent(eventName, key, value)
    }

    override fun onUserLeaveHint() {
        enterPip()
        super.onUserLeaveHint()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun enterPip() {
        val d = windowManager
            .defaultDisplay
        val p = Point()
        d.getSize(p)
        val width = p.x
        val height = p.y
        val ratio = Rational(width, height)
        val pipBuilder = PictureInPictureParams.Builder()
        pipBuilder
            .setAspectRatio(ratio)
            .build()
        enterPictureInPictureMode(pipBuilder.build())
    }

    companion object {
        const val KEY_ARGS = "args"
    }

}

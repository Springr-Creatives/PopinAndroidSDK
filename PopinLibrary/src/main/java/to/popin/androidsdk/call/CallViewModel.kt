package to.popin.androidsdk.call

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import to.popin.androidsdk.models.FastCallModel
import to.popin.androidsdk.call.service.ForegroundService
import io.livekit.android.LiveKit
import io.livekit.android.RoomOptions
import io.livekit.android.audio.AudioSwitchHandler
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.LocalScreencastVideoTrack
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.LocalVideoTrackOptions
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoCaptureParameter
import io.livekit.android.util.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class CallViewModel(
    val callModel: FastCallModel?,
    application: Application,
) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private var initialSet = false;
    val room = LiveKit.create(
        appContext = application,
        options = RoomOptions(adaptiveStream = true, dynacast = false),
    )

    val audioHandler = room.audioHandler as AudioSwitchHandler

    val participants = room::remoteParticipants.flow
        .map { remoteParticipants ->
            listOf<Participant>(room.localParticipant) +
                    remoteParticipants
                        .keys
                        .sortedBy { it.value }
                        .mapNotNull { remoteParticipants[it] }
        }

    private val mutableError = MutableStateFlow<Throwable?>(null)
    val error = mutableError.hide()

    private val mutablePrimarySpeaker = MutableStateFlow<Participant?>(null)
    val primarySpeaker: StateFlow<Participant?> = mutablePrimarySpeaker

    val activeSpeakers = room::activeSpeakers.flow

    private var localScreencastTrack: LocalScreencastVideoTrack? = null


    // Controls
    private val mutableMicEnabled = MutableLiveData(true)
    val micEnabled = mutableMicEnabled.hide()

    private val mutableCameraEnabled = MutableLiveData(true)
    val cameraEnabled = mutableCameraEnabled.hide()

    private val mutableScreencastEnabled = MutableLiveData(false)
    val screenshareEnabled = mutableScreencastEnabled.hide()

    // Emits a string whenever a data message is received.
    private val mutableDataReceived = MutableSharedFlow<String>()
    val dataReceived = mutableDataReceived

    // Whether other participants are allowed to subscribe to this participant's tracks.
    private val mutablePermissionAllowed = MutableStateFlow(true)
    val permissionAllowed = mutablePermissionAllowed.hide()
    private val mAction: MutableLiveData<Action> = MutableLiveData<Action>()

    fun getAction(): LiveData<Action>? {
        return mAction
    }

    private val mLog: MutableLiveData<EventLog> = MutableLiveData<EventLog>()

    private val mConnectionStatus: MutableLiveData<ConnectionStatus> =
        MutableLiveData<ConnectionStatus>()

    fun getEventLog(): LiveData<EventLog>? {
        return mLog
    }

    fun getConnectionStatus(): LiveData<ConnectionStatus>? {
        return mConnectionStatus
    }

    init {
        viewModelScope.launch {
            // Collect any errors.
            launch {
                error.collect { }
            }


            // Handle room events.
            launch {
                room.events.collect {
                    when (it) {
                        is RoomEvent.FailedToConnect -> {
                            mLog.setValue(
                                EventLog(
                                    "call_failed_connect",
                                    "call_id",
                                    callModel?.id ?: 0
                                )
                            )
                            mutableError.value = it.error
                        }

                        is RoomEvent.Disconnected -> {
                            mLog.setValue(
                                EventLog(
                                    "call_disconnected",
                                    "call_id",
                                    callModel?.id ?: 0
                                )
                            )
                            mAction.setValue(Action(Action.CLOSE_ACTIVITY));
                        }

                        is RoomEvent.DataReceived -> {
                            val identity = it.participant?.identity ?: "server"
                            val message = it.data.toString(Charsets.UTF_8)
                            mutableDataReceived.emit("$identity: $message")
                        }

                        is RoomEvent.ParticipantConnected -> {
                            mConnectionStatus.setValue(ConnectionStatus("Loading video..."))
                        }

                        is RoomEvent.TrackSubscribed -> {

                            val track = it.track
                            val participant = it.participant

                            if (track.kind == Track.Kind.VIDEO && !initialSet) {
                                initialSet = true
                                mConnectionStatus.setValue(ConnectionStatus(""))
                                setPrimaryWindowByIdentity(participant.identity)
                            }
                        }

                        is RoomEvent.ParticipantDisconnected -> {
                            mLog.setValue(
                                EventLog(
                                    "call_participant_disconnected",
                                    "call_id",
                                    callModel?.id ?: 0
                                )
                            )
                        }

                        else -> {
                            mLog.setValue(
                                EventLog(
                                    "call_event",
                                    it::class.simpleName ?: "unknown_event",
                                    callModel?.id ?: 0
                                )
                            )
                            Log.e("LOCA_ERR", "Room event: $it")
                        }
                    }
                }
            }


        }

        // Start a foreground service to keep the call from being interrupted if the
        // app goes into the background.
        val foregroundServiceIntent = Intent(application, ForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            application.startForegroundService(foregroundServiceIntent)
        } else {
            application.startService(foregroundServiceIntent)
        }
    }
    fun setPrimaryWindowByIdentity(identity: Participant.Identity?) {
        viewModelScope.launch {
            participants.collect { participantList ->
                val participantByIdentity = participantList
                    .firstOrNull { it.identity == identity }

                if (participantByIdentity != null) {
                    Log.e("SET","IDENTITY");
                     mutablePrimarySpeaker.value = participantByIdentity
                    return@collect // Stop collecting once we've found and set the primary speaker
                }
            }
        }
    }

    private suspend fun collectTrackStats(event: RoomEvent.TrackSubscribed) {
        val pub = event.publication
        while (true) {
            delay(10000)
            if (pub.subscribed) {
                val statsReport = pub.track?.getRTCStats() ?: continue
                Log.e("ERR", "stats for ${pub.sid}:")

                for (entry in statsReport.statsMap) {
                    Log.e("ERR", "${entry.key} = ${entry.value}")
                }
            }
        }
    }

    suspend fun connectToRoom() {
        try {
            mLog.setValue(EventLog("call_connect_room", "", 0))
            mLog.setValue(EventLog("call_start_connect", callModel?.accessToken ?: "", 0))
            mConnectionStatus.setValue(ConnectionStatus("CONNECTING ..."))
            callModel?.accessToken?.let {
                mLog.setValue(EventLog("call_connect_flow", "connect", 0))
                room.connect(
                    url = callModel.websocket,
                    token = it,
                )
                mLog.setValue(EventLog("call_connect_flow", "connect", 1))
                val localParticipant = room.localParticipant
                localParticipant.setMicrophoneEnabled(true)
                mutableMicEnabled.postValue(localParticipant.isMicrophoneEnabled())
                mLog.setValue(EventLog("call_connect_flow", "connect", 2))
                localParticipant.setCameraEnabled(true)
                mutableCameraEnabled.postValue(localParticipant.isCameraEnabled())
                mLog.setValue(EventLog("call_connect_flow", "connect", 3))
                // Update the speaker
                handlePrimarySpeaker(emptyList(), emptyList(), room)
                mLog.setValue(EventLog("call_connect_flow", "connect", 4))
            }
            mLog.setValue(EventLog("call_connect_flow_end", "end", 0))
            Log.e("ROOM", "END_CONNECT")
        } catch (e: Throwable) {
            Log.e("ROOM", "CONNECT_ERROR>" + e.message)
            mLog.setValue(EventLog("call_connect_error", e.message ?: "connect", 0))
            mutableError.value = e
            mAction.setValue(Action(Action.CLOSE_ACTIVITY));
        }
    }


    private fun handlePrimarySpeaker(
        participantsList: List<Participant>,
        speakers: List<Participant>,
        room: Room?
    ) {
        var speaker = mutablePrimarySpeaker.value

        // If speaker is local participant (due to defaults),
        // attempt to find another remote speaker to replace with.
        if (speaker is LocalParticipant) {
            val remoteSpeaker = participantsList
                .filterIsInstance<RemoteParticipant>() // Try not to display local participant as speaker.
                .firstOrNull()

            if (remoteSpeaker != null) {
                speaker = remoteSpeaker
            }
        }

        // If previous primary speaker leaves
        if (!participantsList.contains(speaker)) {
            // Default to another person in room, or local participant.
            speaker = participantsList.filterIsInstance<RemoteParticipant>()
                .firstOrNull()
                ?: room?.localParticipant
        }

        if (speakers.isNotEmpty() && !speakers.contains(speaker)) {
            val remoteSpeaker = speakers
                .filterIsInstance<RemoteParticipant>() // Try not to display local participant as speaker.
                .firstOrNull()

            if (remoteSpeaker != null) {
                speaker = remoteSpeaker
            }
        }

        mutablePrimarySpeaker.value = speaker
    }

    fun setPrimaryWindow(position: Int) {
        runBlocking {

            val elementAtPosition = participants
                .map { it.getOrNull(position) }
                .filterNotNull()
                .firstOrNull()

            if (elementAtPosition != null) {
                mutablePrimarySpeaker.value = elementAtPosition
            }
        }
    }

    /**
     * Start a screen capture with the result intent from
     * [MediaProjectionManager.createScreenCaptureIntent]
     */
    fun startScreenCapture(mediaProjectionPermissionResultData: Intent) {
        val localParticipant = room.localParticipant
        viewModelScope.launch {
            val resources = getApplication<Application>().resources
            val displayMetrics = resources.displayMetrics

            val screencastTrack =
                localParticipant.createScreencastTrack(
                    mediaProjectionPermissionResultData = mediaProjectionPermissionResultData,
                    options = LocalVideoTrackOptions(
                        captureParams = VideoCaptureParameter(
                            width = displayMetrics.widthPixels,
                            height = displayMetrics.heightPixels,
                            30
                        )
                    )
                )
            localParticipant.publishVideoTrack(
                screencastTrack,

                )

            // Must start the foreground prior to startCapture.
            screencastTrack.startForegroundService(null, null)
            screencastTrack.startCapture()

            this@CallViewModel.localScreencastTrack = screencastTrack
            mutableScreencastEnabled.postValue(screencastTrack.enabled)
        }
    }

    fun stopScreenCapture() {
        viewModelScope.launch {
            localScreencastTrack?.let { localScreencastVideoTrack ->
                localScreencastVideoTrack.stop()
                room.localParticipant.unpublishTrack(localScreencastVideoTrack)
                mutableScreencastEnabled.postValue(localScreencastTrack?.enabled ?: false)
            }
        }
    }

    fun cleanUp() {
        mLog.setValue(EventLog("call_connect_clear", "clear", 0))
        room.localParticipant.cleanup()
        room.disconnect()
        room.release()

        // Clean up foreground service
        val application = getApplication<Application>()
        val foregroundServiceIntent = Intent(application, ForegroundService::class.java)
        application.stopService(foregroundServiceIntent)
    }

    override fun onCleared() {
        super.onCleared()

        // Make sure to release any resources associated with LiveKit
        cleanUp()
    }

    fun setMicEnabled(enabled: Boolean) {
        viewModelScope.launch {
            room.localParticipant.setMicrophoneEnabled(enabled)
            mutableMicEnabled.postValue(enabled)
        }
    }

    fun setCameraEnabled(enabled: Boolean) {
        viewModelScope.launch {
            room.localParticipant.setCameraEnabled(enabled)
            mutableCameraEnabled.postValue(enabled)
        }
    }

    fun flipCamera() {
        val videoTrack = room.localParticipant.getTrackPublication(Track.Source.CAMERA)
            ?.track as? LocalVideoTrack
            ?: return

        val newPosition = when (videoTrack.options.position) {
            CameraPosition.FRONT -> CameraPosition.BACK
            CameraPosition.BACK -> CameraPosition.FRONT
            else -> null
        }

        videoTrack.switchCamera(position = newPosition)
    }

    fun dismissError() {
        mutableError.value = null
    }

    fun sendData(message: String) {
        viewModelScope.launch {
            room.localParticipant.publishData(message.toByteArray(Charsets.UTF_8))
        }
    }

    fun toggleSubscriptionPermissions() {
        mutablePermissionAllowed.value = !mutablePermissionAllowed.value
        room.localParticipant.setTrackSubscriptionPermissions(mutablePermissionAllowed.value)
    }

    // Debug functions
    fun simulateMigration() {
        room.sendSimulateScenario(Room.SimulateScenario.MIGRATION)
    }

    fun simulateNodeFailure() {
        room.sendSimulateScenario(Room.SimulateScenario.NODE_FAILURE)
    }

    fun getShowRemarks(): Boolean {

        return false;

    }

    fun reconnect() {
        mLog.setValue(EventLog("call_reconnect", "clear", 0))
        mutablePrimarySpeaker.value = null
        room.disconnect()
        viewModelScope.launch {
            connectToRoom()
        }
    }
}

private fun <T> LiveData<T>.hide(): LiveData<T> = this
private fun <T> MutableStateFlow<T>.hide(): StateFlow<T> = this
private fun <T> Flow<T>.hide(): Flow<T> = this

class Action(val value: Int) {

    companion object {
        const val CLOSE_ACTIVITY = 0
        const val SHOW_INVALID_PASSWARD_OR_LOGIN = 1
    }
}

class EventLog(val eventName: String, val key: String, val value: Int)

class ConnectionStatus(val status: String)
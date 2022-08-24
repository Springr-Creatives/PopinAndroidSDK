package to.popin.androidsdk.call;

import com.twilio.audioswitch.AudioSwitch;

import kotlin.Unit;

public class CallAudioManager {
    private final AudioSwitch audioSwitch;

    public CallAudioManager(AudioSwitch audioSwitch) {
        this.audioSwitch = audioSwitch;
        audioSwitch.start(
                (audioDevices, audioDevice) -> Unit.INSTANCE);
    }

    public void stop() {
        audioSwitch.stop();
    }

    public void activate() {
        audioSwitch.activate();
    }

    public void deactivate() {
        audioSwitch.deactivate();
    }

}

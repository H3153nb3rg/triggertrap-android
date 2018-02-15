package at.photosniper.inputs;
import android.media.AudioFormat;import android.media.AudioRecord;import android.media.MediaRecorder;import android.os.Handler;
import at.photosniper.PhotoSniperApp;
public class LightMonitor implements SensorEventListener {
    private static final int MAX_VOL_RANGE = Short.MAX_VALUE;    private static final int MIN_VOL_RANGE = 500;    private static final int DEFAULT_VOL_RANGE = 5000;    private static final int SAMPLE_RATE = 16000;    private static final String TAG = LightMonitor.class.getSimpleName();    private static final int VOLUME_UPDATE_INTERVAL = 10;    private static final int TRIGGER_UPDATE_INTERVAL = 10;        private int mVolumeRange = DEFAULT_VOL_RANGE;    private int mThreshold = (DEFAULT_VOL_RANGE / 2);
 private int lastValueMeasured = 0;     private boolean mIsRecording = false;    private VolumeListener mListener = null;    private boolean mIsThresholdEnabled = false;
    private long lastUpdateTime = System.currentTimeMillis();    private long lastTriggerTime = System.currentTimeMillis();     private SensorManager mSensorManager; private Sensor mLight;  private final Handler handler = new Handler();
    private LightMonitor() {        initRecorder();    }
    public LightMonitor(VolumeListener listener) {        initRecorder();        mListener = listener;    }
    private void initRecorder() {
  mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);  mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }/*    private void startBufferedRead() {        new Thread(new Runnable() {
            public void run() {                while (mIsRecording) {                    double sum = 0;                    int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);                    for (int i = 0; i < readSize; i++) {                        sum += mBuffer[i] * mBuffer[i];                    }                    if (readSize > 0) {                        final double amplitude = sum / readSize;                        int rmsAmplitude = (int) Math.sqrt(amplitude);                        //Pass amplitude to listener                        if (mListener != null) {                            rmsAmplitude = (rmsAmplitude <= mVolumeRange) ? rmsAmplitude : mVolumeRange;                            //Get the amplitude as a percentage in the range 0-100                            final int percentAmplitude = (int) ((float) (rmsAmplitude) / mVolumeRange * 100);
                            final long currentTime = System.currentTimeMillis();
                            if (currentTime - lastUpdateTime > VOLUME_UPDATE_INTERVAL) {                                lastUpdateTime = currentTime;                                mListener.onVolumeUpdate(percentAmplitude);                            }                            if (rmsAmplitude > mThreshold && mIsThresholdEnabled) {                                if (currentTime - lastTriggerTime > PhotoSniperApp.getInstance(null).getSensorResetDelay()) {                                    lastTriggerTime = currentTime;
                                    //Trigger the output after the sensor delay                                    handler.postDelayed(new Runnable() {                                        public void run() {                                            mListener.onExceedThreshold(percentAmplitude);                                        }                                    }, PhotoSniperApp.getInstance(null).getSensorDelay());
                                }                            }                        }                    } else {                        //Pass 0 amplitude if we have no sample.                        if (mListener != null) {                            mListener.onVolumeUpdate(0);                        }                    }                }            }        }).start();    }*/    public void start() {        if (!mIsRecording) {            mIsRecording = true;
    mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);                     }    }
    public void stop() {        if (mIsRecording) {            mIsRecording = false;               mSensorManager.unregisterListener(this);        }    }
 @Override  public final void onAccuracyChanged(Sensor sensor, int accuracy) {
  }
  @Override  public final void onSensorChanged(SensorEvent event) {    float lux = event.values[0];    if (abs(lux) > rmsAmplitude ) {  rmsAmplitude = abs(lux); } lastValueMeasured = abs(lux);       final int percentAmplitude = (int) ((float) (lastValueMeasured) / ((mVolumeRange / 100));      // final int percentAmplitude = (int) ((float) (lastValueMeasured) / rmsAmplitude * 100);         final long currentTime = System.currentTimeMillis();
 if (currentTime - lastUpdateTime > VOLUME_UPDATE_INTERVAL) {  lastUpdateTime = currentTime;  mListener.onLightUpdate(percentAmplitude); }
 if (percentAmplitude > mThreshold && mIsThresholdEnabled) {  if (currentTime - lastTriggerTime > PhotoSniperApp.getInstance(null).getSensorResetDelay()) {   lastTriggerTime = currentTime;
   //Trigger the output after the sensor delay   handler.postDelayed(new Runnable() {    public void run() {     mListener.onExceedLightThreshold(percentAmplitude);    }   }, PhotoSniperApp.getInstance(null).getSensorDelay());
  } }         }
     public void enabledThreshold() {        mIsThresholdEnabled = true;    }
    public void disableThreshold() {        mIsThresholdEnabled = false;    }
    public boolean getThresholdEnabled() {        return mIsThresholdEnabled;    }
    public void release() {        mRecorder.release();    }
    public void setLightSensitivity(float percentage) {
 //Calculate current threshold percentage        float thresholdPercentage = ((float) mThreshold / mVolumeRange) * 100;
        percentage = (percentage < 0) ? 0 : percentage;        percentage = (percentage > 100) ? 100 : percentage;        final int volumeRange = MAX_VOL_RANGE - MIN_VOL_RANGE;        final int percentageRange = (int) (volumeRange * percentage / 100);        mVolumeRange = MAX_VOL_RANGE - percentageRange;        //Reset threshold          setThreshold(thresholdPercentage);    }
    public void setThreshold(float percentage) {          percentage = (percentage < 0) ? 0 : percentage;        percentage = (percentage > 100) ? 100 : percentage;        mThreshold = (int) (mVolumeRange * percentage / 100);        //Log.d(TAG, "Threshold pecentage: " +  percentage + " threshold value:" + mThreshold + " Volume range: " + mVolumeRange);    }
    public interface LightListener {        void onLightUpdate(int amplitude);
        void onExceedLightThreshold(int amplitude);    }
}

package com.ms.womensafetyapp.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.annotation.Keep

/** This class is directly implemented from Square Seismic Shake Detector(Java)[Thank You],
 * tried to convert it into Kotlin with few tweaks.*/

//@Keep
class ShakeDetectorSeismic(val listener: Listener) : SensorEventListener {

    companion object {
        const val SENSITIVITY_LIGHT: Int = 11
        const val SENSITIVITY_MEDIUM: Int = 13
        const val SENSITIVITY_HARD: Int = 15

        private const val DEFAULT_ACCELERATION_THRESHOLD: Int = SENSITIVITY_MEDIUM
    }

    interface Listener {
        fun hearShake()
    }

    private var accelerationThreshold: Int = DEFAULT_ACCELERATION_THRESHOLD
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null


    fun start(sensorManager: SensorManager): Boolean {
        return start(sensorManager, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun start(sensorManager: SensorManager, sensorDelay: Int): Boolean {
        if (accelerometer != null) {
            return true
        }
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer != null) {
            this.sensorManager = sensorManager
            sensorManager.registerListener(this, accelerometer, sensorDelay)
        }
        return accelerometer != null
    }


    fun stop() {
        if (accelerometer != null) {
            SampleQueue.clear()
            sensorManager?.unregisterListener(this, accelerometer)
            sensorManager = null
            accelerometer = null
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val accelerating = isAccelerating(it)
            val timeStamp = it.timestamp
            SampleQueue.add(timeStamp,accelerating)
            if(SampleQueue.isShaking()){
                SampleQueue.clear()
                listener.hearShake()
            }
        }
    }

    private fun isAccelerating(event: SensorEvent): Boolean {
        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]

        // Instead of comparing magnitude to ACCELERATION_THRESHOLD,
        // compare their squares. This is equivalent and doesn't need the
        // actual magnitude, which would be computed using (expensive) Math.sqrt().
        val magnitudeSquared = ax * ax + ay * ay + az * az
        return magnitudeSquared > accelerationThreshold * accelerationThreshold
    }

    fun setSensitivity(accelerationThreshold: Int){
        this.accelerationThreshold = accelerationThreshold
    }


    object SampleQueue {

        private const val MAX_WINDOW_SIZE: Long = 500000000 // 0.5s
        private const val MIN_WINDOW_SIZE: Long = MAX_WINDOW_SIZE shr 1 // 0.25s

        private const val MIN_QUEUE_SIZE: Int = 4

        private var oldest: Sample? = null
        private var newest: Sample? = null

        private var sampleCount: Int = 0
        private var acceleratingCount: Int = 0

        fun add(timeStamp: Long, accelerating: Boolean) {
            purge(timeStamp - MAX_WINDOW_SIZE)

            val added = SamplePool.acquire()
            added.timestamp = timeStamp
            added.accelerating = accelerating
            added.next = null

            newest?.next = added

            newest = added

            if (oldest == null) {
                oldest = added
            }

            sampleCount++
            if (accelerating) {
                acceleratingCount++
            }
        }

//        private fun purge(cutoff: Long) {
//            while (sampleCount >= MIN_QUEUE_SIZE && oldest != null && cutoff - oldest!!.timestamp > 0) {
//                val removed: Sample = oldest!!
//                if (removed.accelerating) {
//                    acceleratingCount--
//                }
//                sampleCount--
//                oldest = removed.next
//                if (oldest != null) {
//                    newest = null
//                }
//                SamplePool.release(removed)
//            }
//        }

        private fun purge(cutoff: Long) {
            while (sampleCount >= MIN_QUEUE_SIZE && oldest != null && cutoff - oldest!!.timestamp > 0) {
                //val removed: Sample = oldest!!
                val removed = oldest
                if (removed!!.accelerating) {
                    acceleratingCount--
                }
                sampleCount--

                oldest = removed.next
                if (oldest == null) {
                    newest = null
                }
                SamplePool.release(removed)
            }
        }

        fun clear() {
            while (oldest != null) {
                val removed = oldest
                oldest = removed?.next
                SamplePool.release(removed!!)
            }
            newest = null
            sampleCount = 0
            acceleratingCount = 0
        }

        fun asList(): List<Sample> {
            val list = mutableListOf<Sample>()
            var s = oldest
            while (s != null) {
                list.add(s)
                s = s.next
            }
            return list
        }

        fun isShaking(): Boolean {
            return newest != null && oldest != null && newest!!.timestamp - oldest!!.timestamp >= MIN_WINDOW_SIZE && acceleratingCount >= (sampleCount shr 1) + (sampleCount shr 2)
        }

    }


   class Sample {
        /** Time sample was taken.  */
        var timestamp: Long = 0

        /** If acceleration > [.accelerationThreshold].  */
        var accelerating: Boolean = false

        /** Next sample in the queue or pool.  */
        var next: Sample? = null
    }


    private object SamplePool {

        private var head: Sample? = null

        /** Acquires a sample from the pool.  */
        fun acquire(): Sample {
            var acquired = head
            if (acquired == null) {
                acquired = Sample()
            } else {
                // Remove instance from pool.
                head = acquired.next
            }
            return acquired
        }

        /** Returns a sample to the pool.  */
        fun release(sample: Sample) {
            sample.next = head
            head = sample
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
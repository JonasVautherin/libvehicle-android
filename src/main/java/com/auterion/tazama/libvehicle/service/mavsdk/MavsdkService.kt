/*
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at https://mozilla.org/MPL/2.0/.
*/

package com.auterion.tazama.libvehicle.service.mavsdk

import com.auterion.tazama.libvehicle.ActionException
import com.auterion.tazama.libvehicle.ActionWriter
import com.auterion.tazama.libvehicle.Altitude
import com.auterion.tazama.libvehicle.CameraWriter
import com.auterion.tazama.libvehicle.Degrees
import com.auterion.tazama.libvehicle.Euler
import com.auterion.tazama.libvehicle.HomeDistance
import com.auterion.tazama.libvehicle.HomePosition
import com.auterion.tazama.libvehicle.PositionAbsolute
import com.auterion.tazama.libvehicle.Radian
import com.auterion.tazama.libvehicle.Speed
import com.auterion.tazama.libvehicle.TelemetryWriter
import com.auterion.tazama.libvehicle.VehicleWriter
import com.auterion.tazama.libvehicle.VelocityNed
import com.auterion.tazama.libvehicle.VideoStreamInfo
import com.auterion.tazama.libvehicle.service.VehicleService
import com.auterion.tazama.libvehicle.util.GeoUtils
import io.mavsdk.MavsdkEventQueue
import io.mavsdk.System
import io.mavsdk.mavsdkserver.MavsdkServer
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.pow
import kotlin.math.sqrt

class MavsdkService(private val vehicleWriter: VehicleWriter) : VehicleService {
    private lateinit var drone: System
    private val mavsdkServer = MavsdkServer()
    private val disposables = CopyOnWriteArrayList<Disposable>()

    override fun connect() {
        MavsdkEventQueue.executor().execute {
            drone = System("127.0.0.1", mavsdkServer.run())
            linkAction(drone.action, vehicleWriter.actionWriter)
            linkCamera(drone.camera, vehicleWriter.cameraWriter)
            linkTelemetry(drone.telemetry, vehicleWriter.telemetryWriter)
        }
    }

    private fun linkAction(from: io.mavsdk.action.Action, to: ActionWriter) {
        linkArm(from, to)
        linkDisarm(from, to)
        linkTakeoff(from, to)
        linkLand(from, to)
        linkHold(from, to)
        linkRtl(from, to)
    }

    private fun linkArm(from: io.mavsdk.action.Action, to: ActionWriter) {
        to.arm = {
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { cont ->
                    val disposable = from
                        .arm()
                        .subscribe(
                            { cont.resume(Unit) },
                            { e ->
                                e as io.mavsdk.action.Action.ActionException
                                cont.resumeWithException(e.toActionException())
                            }
                        )
                    cont.invokeOnCancellation { disposable.dispose() }
                }
            }
        }
    }

    private fun linkDisarm(from: io.mavsdk.action.Action, to: ActionWriter) {
        to.disarm = {
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { cont ->
                    val disposable = from
                        .disarm()
                        .subscribe(
                            { cont.resume(Unit) },
                            { e ->
                                e as io.mavsdk.action.Action.ActionException
                                cont.resumeWithException(e.toActionException())
                            }
                        )
                    cont.invokeOnCancellation { disposable.dispose() }
                }
            }
        }
    }

    private fun linkTakeoff(from: io.mavsdk.action.Action, to: ActionWriter) {
        to.takeoff = {
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { cont ->
                    val disposable = from
                        .takeoff()
                        .subscribe(
                            { cont.resume(Unit) },
                            { e ->
                                e as io.mavsdk.action.Action.ActionException
                                cont.resumeWithException(e.toActionException())
                            }
                        )
                    cont.invokeOnCancellation { disposable.dispose() }
                }
            }
        }
    }

    private fun linkLand(from: io.mavsdk.action.Action, to: ActionWriter) {
        to.land = {
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { cont ->
                    val disposable = from
                        .land()
                        .subscribe(
                            { cont.resume(Unit) },
                            { e ->
                                e as io.mavsdk.action.Action.ActionException
                                cont.resumeWithException(e.toActionException())
                            }
                        )
                    cont.invokeOnCancellation { disposable.dispose() }
                }
            }
        }
    }

    private fun linkHold(from: io.mavsdk.action.Action, to: ActionWriter) {
        to.hold = {
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { cont ->
                    val disposable = from
                        .hold()
                        .subscribe(
                            { cont.resume(Unit) },
                            { e ->
                                e as io.mavsdk.action.Action.ActionException
                                cont.resumeWithException(e.toActionException())
                            }
                        )
                    cont.invokeOnCancellation { disposable.dispose() }
                }
            }
        }
    }

    private fun linkRtl(from: io.mavsdk.action.Action, to: ActionWriter) {
        to.rtl = {
            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { cont ->
                    val disposable = from
                        .returnToLaunch()
                        .subscribe(
                            { cont.resume(Unit) },
                            { e ->
                                e as io.mavsdk.action.Action.ActionException
                                cont.resumeWithException(e.toActionException())
                            }
                        )
                    cont.invokeOnCancellation { disposable.dispose() }
                }
            }
        }
    }

    private fun linkCamera(from: io.mavsdk.camera.Camera, to: CameraWriter) {
        linkVideoStreamInfo(from.videoStreamInfo, to.videoStreamInfoWriter)
    }

    private fun linkVideoStreamInfo(
        from: Flowable<io.mavsdk.camera.Camera.VideoStreamInfo>,
        to: MutableStateFlow<VideoStreamInfo?>
    ) {
        val videoStreamInfoDisposable = from.subscribe({ videoStreamInfo ->
            to.value = VideoStreamInfo(videoStreamInfo.settings.uri)
        }, {})

        disposables.add(videoStreamInfoDisposable)
    }

    private fun linkTelemetry(from: io.mavsdk.telemetry.Telemetry, to: TelemetryWriter) {
        linkPosition(from.position, to.positionWriter)
        linkVelocity(from.velocityNed, to.velocityWriter)
        linkAttitude(from.attitudeEuler, to.attitudeWriter)
        linkHomePosition(from.home, to.homePositionWriter)
        linkDistanceToHome(from.position, from.home, to.distanceToHomeWriter)
        linkGroundSpeed(from.velocityNed, to.groundSpeedWriter)
    }

    private fun linkPosition(
        from: Flowable<io.mavsdk.telemetry.Telemetry.Position>,
        to: MutableStateFlow<PositionAbsolute?>
    ) {
        val positionDisposable = from.subscribe({ position ->
            to.value =
                PositionAbsolute(
                    Degrees(position.latitudeDeg),
                    Degrees(position.longitudeDeg),
                    Altitude(position.absoluteAltitudeM.toDouble())
                )
        }, {})

        disposables.add(positionDisposable)
    }

    private fun linkVelocity(
        from: Flowable<io.mavsdk.telemetry.Telemetry.VelocityNed>,
        to: MutableStateFlow<VelocityNed?>
    ) {
        val velocityDisposable = from.subscribe({
            to.value = VelocityNed(
                Speed(it.northMS.toDouble()),
                Speed(it.eastMS.toDouble()),
                Speed(it.downMS.toDouble())
            )
        }, {})

        disposables.add(velocityDisposable)
    }

    private fun linkAttitude(
        from: Flowable<io.mavsdk.telemetry.Telemetry.EulerAngle>,
        to: MutableStateFlow<Euler?>
    ) {
        val headingDisposable = from.subscribe({
            to.value = Euler(
                Radian.fromDegrees(it.rollDeg.toDouble()),
                Radian.fromDegrees(it.pitchDeg.toDouble()),
                Radian.fromDegrees(it.yawDeg.toDouble())
            )
        }, {})

        disposables.add(headingDisposable)
    }

    private fun linkHomePosition(
        from: Flowable<io.mavsdk.telemetry.Telemetry.Position>,
        to: MutableStateFlow<HomePosition?>
    ) {
        val homeDisposable = from.subscribe({
            to.value = HomePosition(
                lat = Degrees(it.latitudeDeg),
                lon = Degrees(it.longitudeDeg),
                alt = Altitude(it.absoluteAltitudeM.toDouble())
            )
        }, {})

        disposables.add(homeDisposable)
    }

    private fun linkDistanceToHome(
        fromPos: Flowable<io.mavsdk.telemetry.Telemetry.Position>,
        fromHome: Flowable<io.mavsdk.telemetry.Telemetry.Position>,
        to: MutableStateFlow<HomeDistance?>
    ) {
        val distanceToHomeDisposable = Flowable.combineLatest(fromPos, fromHome) { position, home ->
            val horizontal =
                GeoUtils.distanceBetween(
                    Degrees(home.latitudeDeg),
                    Degrees(home.longitudeDeg),
                    Degrees(position.latitudeDeg),
                    Degrees(position.longitudeDeg)
                )
            val vertical =
                Altitude((position.absoluteAltitudeM - home.absoluteAltitudeM).toDouble())
            to.value = HomeDistance(horizontal, vertical)
        }.subscribe()

        disposables.add(distanceToHomeDisposable)
    }

    private fun linkGroundSpeed(
        from: Flowable<io.mavsdk.telemetry.Telemetry.VelocityNed>,
        to: MutableStateFlow<Speed?>
    ) {
        val groundSpeedDisposable = from.subscribe({ velocity ->
            to.value = Speed(sqrt(velocity.northMS.pow(2) + velocity.eastMS.pow(2)).toDouble())
        }, {})

        disposables.add(groundSpeedDisposable)
    }

    override suspend fun destroy() {
        MavsdkEventQueue.executor().execute {
            disposables.forEach { it.dispose() }
            vehicleWriter.reset()
            mavsdkServer.stop()
            mavsdkServer.destroy()
        }
    }

    private fun io.mavsdk.action.Action.ActionException.toActionException(): ActionException {
        return ActionException(this.message, this.cause)
    }
}

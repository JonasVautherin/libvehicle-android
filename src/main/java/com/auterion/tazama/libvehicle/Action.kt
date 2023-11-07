/*
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at https://mozilla.org/MPL/2.0/.
*/

package com.auterion.tazama.libvehicle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface Action {
    suspend fun arm()
    suspend fun disarm()
    suspend fun takeoff()
    suspend fun land()
    suspend fun hold()
    suspend fun rtl()
}

interface ActionWriter {
    var arm: (suspend CoroutineScope.() -> Unit)?
    var disarm: (suspend CoroutineScope.() -> Unit)?
    var takeoff: (suspend CoroutineScope.() -> Unit)?
    var land: (suspend CoroutineScope.() -> Unit)?
    var hold: (suspend CoroutineScope.() -> Unit)?
    var rtl: (suspend CoroutineScope.() -> Unit)?

    fun reset()
}

class ActionImpl : Action, ActionWriter {
    override var arm: (suspend CoroutineScope.() -> Unit)? = null
    override var disarm: (suspend CoroutineScope.() -> Unit)? = null
    override var takeoff: (suspend CoroutineScope.() -> Unit)? = null
    override var land: (suspend CoroutineScope.() -> Unit)? = null
    override var hold: (suspend CoroutineScope.() -> Unit)? = null
    override var rtl: (suspend CoroutineScope.() -> Unit)? = null

    override suspend fun arm() {
        arm?.let { withContext(Dispatchers.IO) { it() } }
            ?: { throw RuntimeException("arm is undefined!") }
    }

    override suspend fun disarm() {
        disarm?.let { withContext(Dispatchers.IO) { it() } }
            ?: { throw RuntimeException("disarm is undefined!") }
    }

    override suspend fun takeoff() {
        takeoff?.let { withContext(Dispatchers.IO) { it() } }
            ?: { throw RuntimeException("takeoff is undefined!") }
    }

    override suspend fun land() {
        land?.let { withContext(Dispatchers.IO) { it() } }
            ?: { throw RuntimeException("land is undefined!") }
    }

    override suspend fun hold() {
        hold?.let { withContext(Dispatchers.IO) { it() } }
            ?: { throw RuntimeException("hold is undefined!") }
    }

    override suspend fun rtl() {
        rtl?.let { withContext(Dispatchers.IO) { it() } }
            ?: { throw RuntimeException("rtl is undefined!") }
    }

    override fun reset() {
        arm = null
        disarm = null
        takeoff = null
        land = null
        hold = null
        rtl = null
    }
}

class ActionException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}

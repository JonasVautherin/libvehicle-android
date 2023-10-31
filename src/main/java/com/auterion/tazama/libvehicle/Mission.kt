/*
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at https://mozilla.org/MPL/2.0/.
*/

package com.auterion.tazama.libvehicle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface Mission {
    val position: StateFlow<PositionAbsolute?>
}

interface MissionWriter {
    val positionWriter: MutableStateFlow<PositionAbsolute?>
}

class MissionImpl : Mission, MissionWriter {
    override val positionWriter = MutableStateFlow<PositionAbsolute?>(null)
    override val position = positionWriter.asStateFlow()
}

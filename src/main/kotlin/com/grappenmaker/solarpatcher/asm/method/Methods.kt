/*
 * Solar Patcher, a runtime patcher for Lunar Client
 * Copyright (C) 2022 Solar Tweaks and respective contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.grappenmaker.solarpatcher.asm.method
import org.objectweb.asm.Opcodes.*

// Description of a method (simple)
data class MethodDescription(
    val name: String,
    val descriptor: String,
    val owner: String,
    val access: Int = -1 // -1 can mean that access is not available
)

// Invocation types (see JVMS)
enum class InvocationType(val opcode: Int) {
    SPECIAL(INVOKESPECIAL),
    DYNAMIC(INVOKEDYNAMIC),
    VIRTUAL(INVOKEVIRTUAL),
    INTERFACE(INVOKEINTERFACE),
    STATIC(INVOKESTATIC);

    companion object {
        fun getFromOpcode(opcode: Int) = enumValues<InvocationType>().find { it.opcode == opcode }
    }
}
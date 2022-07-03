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

package com.grappenmaker.solarpatcher.asm
import java.lang.reflect.Field
import java.lang.reflect.Modifier

// Description of a field (simple)
data class FieldDescription(
    val name: String,
    val descriptor: String,
    val owner: String,
    val access: Int = -1 // -1 can mean that access is not available
)

// Accessors for fields
val Field.isPublic get() = Modifier.isPublic(modifiers)
val Field.isPrivate get() = Modifier.isPrivate(modifiers)
val Field.isProtected get() = Modifier.isProtected(modifiers)
val Field.isStatic get() = Modifier.isStatic(modifiers)
val Field.isFinal get() = Modifier.isFinal(modifiers)
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

package com.grappenmaker.solarpatcher.asm.matching

import com.grappenmaker.solarpatcher.asm.method.MethodDescription
import org.objectweb.asm.Opcodes

// Utility to match on methods
// Lambda takes in a method description to match on,
// and returns true if it matches exactly, false otherwise
typealias MethodMatcher = (method: MethodDescription) -> Boolean

object MethodMatching {
    // Always match
    fun matchAny(): MethodMatcher = { true }

    // Match on properties of methods
    fun matchName(name: String): MethodMatcher = { it.name == name }
    fun matchOwner(owner: String): MethodMatcher = { it.owner == owner }
    fun matchAccess(access: Int): MethodMatcher = { it.access == access }
    fun matchDescriptor(descriptor: String): MethodMatcher = { it.descriptor == descriptor }

    fun matchDescription(method: MethodDescription): MethodMatcher = {
        method.name == it.name
                && method.owner == it.owner
                && method.descriptor == it.descriptor
                && (method.access == it.access || method.access == -1 || it.access == -1)
    }

    // Utility to match clinit
    fun matchClinit(): MethodMatcher = matchName("<clinit>")
    fun matchInit(): MethodMatcher = matchName("<init>")

    // Utility to chain matchers
    operator fun MethodMatcher.plus(other: MethodMatcher): MethodMatcher = { this(it) && other(it) }
    operator fun MethodMatcher.not(): MethodMatcher = { !this(it) }
    infix fun MethodMatcher.or(other: MethodMatcher): MethodMatcher = { this(it) || other(it) }

    // Utility to provide a match function, for clarity
    fun MethodMatcher.match(other: MethodDescription) = this(other)
}

// Util to generate a matcher from method description
fun MethodDescription.asMatcher() = com.grappenmaker.solarpatcher.asm.matching.MethodMatching.matchDescription(this)
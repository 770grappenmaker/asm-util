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
import org.objectweb.asm.tree.ClassNode

// Utility for matching on classes, similar to method matching
typealias ClassMatcher = (ClassNode) -> Boolean

object ClassMatching {
    // Always match
    fun matchAny(): ClassMatcher = { true }

    // Match on properties of classes
    fun matchName(name: String): ClassMatcher = { it.name == name }
    fun matchAccess(access: Int): ClassMatcher = { it.access == access }

    // Match on interfaces and extensions
    fun implements(interfaceName: String): ClassMatcher = { it.interfaces.contains(interfaceName) }
    fun extends(superClass: String): ClassMatcher = { it.superName == superClass }

    // Utility to chain matchers
    operator fun ClassMatcher.plus(other: ClassMatcher): ClassMatcher = { this(it) && other(it) }

    operator fun ClassMatcher.not(): ClassMatcher = { !this(it) }
    infix fun ClassMatcher.or(other: ClassMatcher): ClassMatcher = { this(it) || other(it) }

    // Utility to provide a match function, for clarity
    fun ClassMatcher.match(other: ClassNode) = this(other)
}
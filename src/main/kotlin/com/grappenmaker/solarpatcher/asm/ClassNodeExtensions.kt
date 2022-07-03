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

import com.grappenmaker.solarpatcher.asm.matching.MethodMatcher
import com.grappenmaker.solarpatcher.asm.method.MethodDescription
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import org.objectweb.asm.util.TraceClassVisitor
import java.io.PrintWriter

val ClassNode.constants: List<Any>
    get() = methods.flatMap { m -> m.constants }

val MethodNode.constants: List<Any>
    get() = instructions.filterIsInstance<LdcInsnNode>().map { it.cst } +
            instructions.filterIsInstance<InvokeDynamicInsnNode>().flatMap { it.bsmArgs.asIterable() }

val MethodNode.calls: List<MethodInsnNode>
    get() = instructions.filterIsInstance<MethodInsnNode>()

fun MethodNode.asDescription(owner: ClassNode) =
    MethodDescription(name, desc, owner.name, access)

fun MethodNode.asDescription(owner: String) =
    MethodDescription(name, desc, owner, access)

fun FieldNode.asDescription(owner: ClassNode) =
    FieldDescription(name, desc, owner.name, access)

fun MethodInsnNode.asDescription() =
    MethodDescription(name, desc, owner)

val ClassNode.isInterface get() = access and Opcodes.ACC_INTERFACE != 0
val ClassNode.isAbstract get() = access and Opcodes.ACC_ABSTRACT != 0

fun ClassNode.dump() = accept(TraceClassVisitor(PrintWriter(System.out)))
fun ClassNode.asBytes(): ByteArray = ClassWriter(0).also { accept(it) }.toByteArray()

fun ClassNode.hasConstant(cst: Any?) = constants.contains(cst)
fun ClassNode.calls(matcher: MethodMatcher) = methods.any { it.calls.any { c -> matcher(c.asDescription()) } }

fun MethodNode.hasConstant(cst: Any?) = constants.contains(cst)
fun MethodNode.calls(matcher: MethodMatcher) = calls.any { matcher(it.asDescription()) }

fun FieldInsnNode.asDescription() = FieldDescription(name, desc, owner)

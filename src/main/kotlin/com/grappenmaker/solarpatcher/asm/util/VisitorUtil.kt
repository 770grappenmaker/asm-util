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

package com.grappenmaker.solarpatcher.asm.util

import com.grappenmaker.solarpatcher.asm.API
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.BIPUSH
import org.objectweb.asm.Opcodes.SIPUSH

// Visitor wrappers, used for chaining visitors before we actually have the visitors
typealias ClassVisitorWrapper = (parent: ClassVisitor) -> ClassVisitor
typealias ModuleVisitorWrapper = (parent: ModuleVisitor) -> ModuleVisitor
typealias AnnotationVisitorWrapper = (parent: AnnotationVisitor) -> AnnotationVisitor
typealias RecordComponentVisitorWrapper = (parent: RecordComponentVisitor) -> RecordComponentVisitor
typealias FieldVisitorWrapper = (parent: FieldVisitor) -> FieldVisitor
typealias MethodVisitorWrapper = (parent: MethodVisitor) -> MethodVisitor

private fun <T> Collection<(T) -> T>.foldWrappers(parent: T) = fold(parent) { acc, cur -> cur(acc) }

fun Collection<ClassVisitorWrapper>.toVisitor(parent: ClassVisitor) = foldWrappers(parent)
fun Collection<ModuleVisitorWrapper>.toVisitor(parent: ModuleVisitor) = foldWrappers(parent)
fun Collection<AnnotationVisitorWrapper>.toVisitor(parent: AnnotationVisitor) = foldWrappers(parent)
fun Collection<RecordComponentVisitorWrapper>.toVisitor(parent: RecordComponentVisitor) = foldWrappers(parent)
fun Collection<FieldVisitorWrapper>.toVisitor(parent: FieldVisitor) = foldWrappers(parent)
fun Collection<MethodVisitorWrapper>.toVisitor(parent: MethodVisitor) = foldWrappers(parent)

// Utility to remap ldc instructions
fun replaceClassConstants(parent: ClassVisitor, map: Map<Any, Any?>) = object : ClassVisitor(API, parent) {
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ) = replaceMethodConstants(super.visitMethod(access, name, descriptor, signature, exceptions), map)
}

fun replaceMethodConstants(parent: MethodVisitor, map: Map<Any, Any?>) = object : MethodVisitor(API, parent) {
    override fun visitLdcInsn(value: Any?) {
        when (value) {
            in map -> {
                val replace = map.getValue(value!!)
                if (replace != null) parent.loadConstant(replace)
            }
            else -> super.visitLdcInsn(value)
        }
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        if (opcode == BIPUSH || opcode == SIPUSH) {
            val value = map[operand] ?: operand
            if (value == operand) {
                super.visitIntInsn(opcode, operand)
                return
            }

            if (value !is Int) {
                throw IllegalArgumentException("Replacement for an integer should be an integer")
            }

            parent.loadConstant(value)
        } else {
            super.visitIntInsn(opcode, operand)
        }
    }

    override fun visitInvokeDynamicInsn(
        name: String,
        desc: String,
        handle: Handle,
        vararg args: Any
    ) {
        super.visitInvokeDynamicInsn(name, desc, handle, *args.map { map[it] ?: it }.toTypedArray())
    }
}
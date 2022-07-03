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
import com.grappenmaker.solarpatcher.asm.matching.MethodMatcher
import com.grappenmaker.solarpatcher.asm.method.MethodDescription
import com.grappenmaker.solarpatcher.asm.method.*
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

// Creates a method visitor that applies advice to a visitor
// desc: Description of the method, used in the advice adapter
// parent: the parent method visitor to apply advice on
// enterAdvice: the code to run, when the start of the method is being visited
// exitAdvice: the code to run, when the end of the method is being visited
fun createAdvice(
    desc: MethodDescription,
    parent: MethodVisitor,
    enterAdvice: MethodVisitor.() -> Unit = {},
    exitAdvice: MethodVisitor.(Int) -> Unit = {}
) = object : AdviceAdapter(API, parent, desc.access, desc.name, desc.descriptor) {
    override fun onMethodEnter() = enterAdvice()
    override fun onMethodExit(opcode: Int) = exitAdvice(opcode)
}

// Creates a class visitor that applies advice to a given method
// parent: the parent class visitor, if any
// desc: Description of the method that will have advice applied on
// enterAdvice: the code to run, when the start of the method is being visited
// exitAdvice: the code to run, when the end of the method is being visited
class AdviceClassVisitor(
    parent: ClassVisitor,
    private val matcher: MethodMatcher,
    private val enterAdvice: MethodVisitor.() -> Unit = {},
    private val exitAdvice: MethodVisitor.(Int) -> Unit = {}
) : ClassVisitor(API, parent) {
    private lateinit var owner: String

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.owner = name
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        val thisMethod = MethodDescription(name, descriptor, owner, access)

        return if (matcher(thisMethod)) createAdvice(thisMethod, mv, enterAdvice, exitAdvice) else mv
    }
}
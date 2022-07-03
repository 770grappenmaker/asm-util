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

package com.grappenmaker.solarpatcher.asm.transform

import com.grappenmaker.solarpatcher.asm.matching.ClassMatcher
import com.grappenmaker.solarpatcher.asm.util.LoaderClassWriter
import com.grappenmaker.solarpatcher.asm.util.toVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.TraceClassVisitor
import java.io.PrintWriter
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

// A ClassFileTransformer that applies Transform generators
class FileTransformer(
    private val transforms: List<TransformGenerator> = listOf(),
    private val debug: Boolean = false,
    private val removeDebugInfo: Boolean = !debug
) : ClassFileTransformer {
    override fun transform(
        loader: ClassLoader?,
        className: String,
        classBeingRedefined: Class<*>?,
        domain: ProtectionDomain,
        classFile: ByteArray
    ): ByteArray? {
        // Load the classfile as node
        val reader = ClassReader(classFile)
        val node = ClassNode().also { reader.accept(it, 0) }

        val classTransform = try {
            transforms.getFromNode(node) ?: return null
        } catch (e: Exception) {
            println("Error while generating transforms: $e")
            e.printStackTrace()
            return null
        }

        // Not interested to transform if there are no transforms or visitors
        if (classTransform.methodTransforms.isEmpty() && classTransform.visitors.isEmpty()) return null
        val actualLoader = loader ?: return null // Not interested in transforming classes from bootstrap loader

        val writer = LoaderClassWriter(actualLoader, reader, ClassWriter.COMPUTE_FRAMES)
        try {
            val visitor = classTransform.visitors.toVisitor(writer)
            val options = (if (removeDebugInfo) ClassReader.SKIP_DEBUG else 0) or
                    if (classTransform.shouldExpand) ClassReader.EXPAND_FRAMES else 0

            reader.accept(TransformVisitor(classTransform.methodTransforms, visitor), options)
        } catch (e: Exception) {
            println("Error while transforming ${node.name}: $e")
            e.printStackTrace()
            return null
        }

        return writer.toByteArray().also {
            if (debug) {
                val count = classTransform.methodTransforms.size
                println("Result of transforming $className (applying $count transforms)")
                println()

                ClassReader(it).accept(TraceClassVisitor(PrintWriter(System.out)), 0)
            }
        }
    }
}

// Utility to get a class transform based on the name
private fun Collection<TransformGenerator>.getFromNode(node: ClassNode) =
    mapNotNull {
        runCatching { it.generate(node) }.onFailure {
            println("Exception while generating transform:")
            it.printStackTrace()
        }.getOrNull()
    }.reduceTransforms()

fun Collection<ClassTransform>.reduceTransforms() = reduceOrNull { acc, cur ->
    ClassTransform(
        acc.methodTransforms + cur.methodTransforms,
        acc.visitors + cur.visitors,
        acc.shouldExpand || cur.shouldExpand
    )
}

// Transform generator type
interface TransformGenerator {
    // Returns null if the generator is not interested in transforming
    fun generate(node: ClassNode): ClassTransform?
}

// Utility method to create a transform generator based on a matcher
fun matcherGenerator(transform: ClassTransform, matcher: ClassMatcher) = object : TransformGenerator {
    override fun generate(node: ClassNode) = if (matcher(node)) transform else null
}

// Utility method to match on lunar client classes only
fun matchLunar(): ClassMatcher = { it.name.startsWith("lunar/") }
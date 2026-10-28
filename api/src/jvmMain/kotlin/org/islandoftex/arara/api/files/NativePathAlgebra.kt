// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

/** Native implementation of the focused PathAlgebra contract. */
internal class NativePathAlgebra(private val identity: NativePathIdentity) : PathAlgebra {
    /**
     * Checks whether the path starts with [p].
     */
    override fun startsWith(p: MPPPath): Boolean =
        identity.providedPath.startsWith(p.providedPath)

    /**
     * Normalizes the path. For [MPPPath] this means, the path is transformed
     * into an absolute path and then normalized.
     */
    override fun normalize(): MPPPath =
        identity.path(identity.referencePath)

    /**
     * Resolve the child [p] of the current path.
     */
    override fun resolve(p: String): MPPPath =
        MPPPath(identity.providedPath.resolve(p), identity.fileSystem)

    /**
     * Resolve the child [p] of the current path.
     */
    override fun resolve(p: MPPPath): MPPPath =
        MPPPath(identity.providedPath.resolve(p.providedPath), identity.fileSystem)

    /**
     * Resolve the sibling [p] of the current path.
     */
    override fun resolveSibling(p: String): MPPPath =
        identity.path(identity.referencePath.parent ?: identity.referencePath.root).resolve(p)

    /**
     * Resolve the sibling [p] of the current path.
     */
    override fun resolveSibling(p: MPPPath): MPPPath =
        identity.path(identity.referencePath.parent ?: identity.referencePath.root).resolve(p)

    /**
     * Resolve the child [p] against the current path.
     */
    override operator fun div(p: String): MPPPath = resolve(p)

    /**
     * Resolve the child [p] against the current path.
     */
    override operator fun div(p: MPPPath): MPPPath = resolve(p)
}

// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.files

/**
 * Syntax to interpret independently of the host.
 */
public enum class PathSyntax {
    /**
     * Slash-separated paths; backslashes are ordinary characters.
     */
    POSIX,

    /**
     * Drive, UNC and relative paths; both separators are accepted.
     */
    WINDOWS
}

/**
 * Explicit foreign path algebra, with case-sensitive lexical identity and no filesystem access.
 * Parsing preserves dot segments; [normalize] removes them explicitly. No working directory is inferred.
 */
public class ForeignPath private constructor(
    /**
     * The selected syntax.
     */
    public val syntax: PathSyntax,

    /**
     * Root spelling, including any drive or UNC share. Empty for an ordinary relative path.
     */
    public val root: String,
    private val components: List<String>
) {
    /**
     * Path segments, including dot segments until explicitly normalized.
     */
    public val segments: List<String>
        get() = components.toList()

    /**
     * Windows drive-relative and root-relative paths are not absolute.
     */
    public val isAbsolute: Boolean
        get() = if (syntax == PathSyntax.POSIX) {
            root == "/"
        } else {
            root.startsWith("\\\\") || root.endsWith(":\\")
        }

    /**
     * Final segment, or an empty string at a root.
     */
    public val fileName: String
        get() = components.lastOrNull().orEmpty()

    /**
     * Lexical parent; a root is its own parent.
     */
    public val parent: ForeignPath
        get() = ForeignPath(syntax, root, components.dropLast(1))

    /**
     * Remove dot segments without accessing a filesystem or resolving links.
     */
    public fun normalize(): ForeignPath {
        val result = mutableListOf<String>()
        for (segment in components) {
            when {
                segment == "." -> Unit
                segment == ".." && result.isNotEmpty() && result.last() != ".." -> result.removeAt(result.lastIndex)
                segment == ".." && root.isNotEmpty() && !root.endsWith(":") -> Unit
                else -> result.add(segment)
            }
        }
        return ForeignPath(syntax, root, result)
    }

    /**
     * Resolve a child of the same syntax. Drive-relative children require a drive context and are rejected.
     * A Windows root-relative child inherits an absolute base's drive or UNC share.
     */
    public fun resolve(child: ForeignPath): ForeignPath {
        require(syntax == child.syntax) { "Cannot resolve paths of different syntaxes" }
        return when {
            child.isAbsolute -> child
            child.root == "\\" -> {
                require(isAbsolute) { "A root-relative child requires an absolute Windows base" }
                ForeignPath(syntax, root, child.components)
            }

            else -> {
                require(child.root.isEmpty()) { "Drive-relative resolution requires an explicit drive context" }
                ForeignPath(syntax, root, components + child.components)
            }
        }
    }

    /**
     * Parse and resolve a child using this path's explicit syntax.
     */
    public fun resolve(child: String): ForeignPath = resolve(parse(child, syntax))

    /**
     * Segment-wise lexical prefix comparison, including syntax and root.
     */
    public fun startsWith(other: ForeignPath): Boolean = syntax == other.syntax && root == other.root &&
        components.size >= other.components.size && components.take(other.components.size) == other.components

    /**
     * Render using the selected syntax, without consulting the host.
     */
    override fun toString(): String {
        val separator = if (syntax == PathSyntax.POSIX) "/" else "\\"
        return (root + components.joinToString(separator)).ifEmpty { "." }
    }

    /**
     * Case-sensitive lexical equality, including the selected syntax.
     */
    override fun equals(other: Any?): Boolean = other is ForeignPath &&
        syntax == other.syntax && root == other.root && components == other.components

    /**
     * Hash of syntax, root and lexical segments.
     */
    override fun hashCode(): Int = (syntax.hashCode() * HASH_FACTOR + root.hashCode()) *
        HASH_FACTOR + components.hashCode()

    /**
     * Explicit parsing entry point.
     */
    public companion object {
        private const val HASH_FACTOR: Int = 31
        private const val UNC_ROOT_SEGMENTS: Int = 2
        private const val DRIVE_PREFIX_LENGTH: Int = 2
        private const val DRIVE_ROOT_LENGTH: Int = 3

        /**
         * Parse only the requested syntax; reject NUL characters and incomplete UNC roots.
         */
        public fun parse(value: String, syntax: PathSyntax): ForeignPath {
            require('\u0000' !in value) { "Paths cannot contain NUL" }
            val path = if (syntax == PathSyntax.WINDOWS) value.replace('/', '\\') else value
            val separator = if (syntax == PathSyntax.POSIX) '/' else '\\'
            val root = if (syntax == PathSyntax.WINDOWS) {
                windowsRoot(path)
            } else if (path.startsWith('/')) {
                "/"
            } else {
                ""
            }
            val segments = if (root.startsWith("\\\\")) {
                path.split(separator).filter { it.isNotEmpty() }.drop(UNC_ROOT_SEGMENTS)
            } else {
                path.drop(root.length).split(separator).filter { it.isNotEmpty() }
            }
            return ForeignPath(syntax, root, segments)
        }

        private fun windowsRoot(path: String): String = when {
            path.startsWith("\\\\") -> {
                val entries = path.split('\\').filter { it.isNotEmpty() }
                require(entries.size >= UNC_ROOT_SEGMENTS) { "A UNC path requires a server and share" }
                "\\\\${entries[0]}\\${entries[1]}\\"
            }

            path.length >= DRIVE_PREFIX_LENGTH && path[1] == ':' -> {
                require(path[0] in 'A'..'Z' || path[0] in 'a'..'z') { "Invalid Windows drive" }
                if (path.getOrNull(DRIVE_PREFIX_LENGTH) == '\\') {
                    path.take(DRIVE_ROOT_LENGTH)
                } else {
                    path.take(DRIVE_PREFIX_LENGTH)
                }
            }

            path.startsWith('\\') -> "\\"
            else -> ""
        }

    }
}

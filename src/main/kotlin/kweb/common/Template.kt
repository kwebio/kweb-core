package kweb.common

/**
 * A fast, lightweight string templating system with zero dependencies.
 *
 * You supply a template String and an array of tokens to replace when calling
 * [.apply]. The order of tokens supplied in the constructor
 * corresponds to the order of strings supplied in apply().
 *
 * The io.kweb.common.Template will compile your string into an executable stack, which
 * generates output with extreme efficiency; the only string-matching performed
 * is during io.kweb.common.Template compile, and it processes all tokens in a single,
 * efficient iteration of the template String.
 *
 * This ensures an absolute minimum of processing, and allows large templates
 * with a large number of replacements to scale nicely.
 *
 * Usage:
 *
 * assert
 * new Template("$1, $2!", "$1", "$2")
 * .apply("Hello", "World")
 * .equals("Hello, World!");
 *
 * assert
 * new Template("(<>[])$.toArray(new Object[$.size()])", "<>", "$")
 * .apply("String", "myList")
 * .equals("(String[])myList.toArray(new Object[myList.size()])");
 *
 * @author "James X. Nelson (james@wetheinter.net)"
 */
class Template(template: String, vararg replaceables: String) : Stack("") {

    /**
     * Applies the current template to the supplied arguments.
     *
     */
    override fun apply(vararg args: String?): String {
        return super.apply(*args)
    }

    /**
     * Translates a template string into a stack of .toStringable() nodes.
     */
    private fun compile(template: String, replaceables: Array<out String>) {
        var numLive = 0
        // These are the only two arrays created by the io.kweb.common.Template
        val tokenPositions = IntArray(replaceables.size)
        val liveIndices = IntArray(replaceables.size)
        // Get the first index, if any, of each replaceable token.
        var i = replaceables.size
        while (i-- > 0) {
            val next = template.indexOf(replaceables[i])
            if (next > -1) { // Record only live tokens (ignore missing replacements)
                liveIndices[numLive++] = i
                tokenPositions[i] = template.indexOf(replaceables[i])
            }
        }
        // Try to get off easy
        if (numLive == 0) {
            next = Stack(template)
            return
        }
        // Perform a single full sort of live indices
        crossSort(liveIndices, tokenPositions, numLive - 1)
        // Recursively fill our stack
        lexTemplate(this, template, replaceables, liveIndices, tokenPositions, 0, numLive)
    }

    /**
     * Performs the lexing of the template, filling the io.kweb.common.Template io.kweb.common.Stack.
     */
    private fun lexTemplate(
            head: Stack, template: String, replaceables: Array<out String>,
            liveIndices: IntArray, tokenPositions: IntArray, curPos: Int, numLive: Int) { // Chop up the template string into nodes.
        var curPos = curPos
        var numLive = numLive
        val nextIndex = liveIndices[0] // Guaranteed the lowest valid position
        val nextPos = tokenPositions[nextIndex] // token position in template String
        assert(nextPos > -1)
        // Pull off the constant string value since last token (might be 0 length)
        val constant = template.substring(curPos, nextPos)
        val replaceable = replaceables[nextIndex]
        // Update our index in the template string
        curPos = nextPos + replaceable.length
        // Push a new node onto the stack
        val tail = head.push(constant, nextIndex)
        // Update our sort so liveIndices[0] points to next replacement position
        val newPosition = template.indexOf(replaceable, nextPos + 1)
        if (newPosition == -1) { // A token is exhausted
            if (--numLive == 0) { // At the very end, we tack on a tail with any remaining string value
                tail.next = Stack(if (curPos == template.length) "" else template.substring(curPos))
                return  // The end of the recursion
            }
            // Reusing the same array, just shift values left;
// We limit our scope to the numLive counter, so no need to copy arrays.
            System.arraycopy(liveIndices, 1, liveIndices, 0, numLive)
        } else { // This token has another replacement; we may have to re-sort.
            tokenPositions[nextIndex] = newPosition
            if (numLive > 1 && newPosition > tokenPositions[liveIndices[1]]) { // Only re-sort if the new index isn't still lowest
                var test = 1
                while (newPosition > tokenPositions[liveIndices[test]]) { // Safe to shift backwards
                    liveIndices[test - 1] = liveIndices[test]
                    if (++test == numLive) break
                }
                // Wherever the loop ended is where the current fragment must go
                liveIndices[test - 1] = nextIndex
            }
        }
        // If we didn't return, we must recurse
        lexTemplate(tail, template, replaceables, liveIndices, tokenPositions, curPos, numLive)
    }

    companion object {
        /**
         * A simple adaptation of the quicksort algorithm; the only difference is that
         * the values of the array being sorted are pointers to a separate array.
         *
         * This method is only performed once per compile,
         * and then we just keep the pointers sorted as we go.
         *
         * @param pointers
         * - The pointers to sort in ascending order
         * @param values
         * - The values used to determine sort order of pointers
         * @param endIndex
         * - Max index of pointers to sort (inclusive).
         */
        private fun crossSort(pointers: IntArray, values: IntArray, endIndex: Int) {
            var i = 0
            var j = i
            while (i < endIndex) {
                val ai = pointers[i + 1]
                while (values[ai] < values[pointers[j]]) {
                    pointers[j + 1] = pointers[j]
                    if (j-- == 0) break
                }
                pointers[j + 1] = ai
                j = ++i
            }
        }
    }

    init {
        compile(template, replaceables)
    }
}

/**
 * This is the base stack object used in the compiled io.kweb.common.Template.
 *
 * This superclass is used only for the head and tail node, which allows us to
 * limit the number of null checks by ensuring regular nodes never have null pointers.
 *
 * @author "James X. Nelson (james@wetheinter.net)"
 */
open class Stack(prefix: String?) {

    val prefix: String?
    var next: Stack? = null
    open fun apply(vararg values: String?): String {
        return prefix + if (next == null) "" else next!!.apply(*values)
    }

    /**
     * Pushes a string constant and a pointer to a token's replacement position
     * onto stack.
     */
    fun push(prefix: String?, pos: Int): Stack {
        assert(next == null)
        val sn = StackNode(prefix, pos)
        next = sn
        return sn
    }

    init {
        assert(prefix != null)
        this.prefix = prefix
    }
}

/**
 * This subclass of io.kweb.common.Stack is for active nodes of the template which have both
 * a string prefix, and a pointer to a replacement value.
 *
 * Each instance of io.kweb.common.StackNode performs one direct lookup of a value during
 * .toString()
 *
 * @author "James X. Nelson (james@wetheinter.net)"
 */
internal class StackNode(prefix: String?, position: Int) : Stack(prefix) {

    private val position: Int
    override fun apply(vararg values: String?): String {
        return (prefix
                + (if (position < values.size && values[position] != null) values[position] else "")
                + next!!.apply(*values))
    }

    init {
        assert(position >= 0)
        this.position = position
    }
}
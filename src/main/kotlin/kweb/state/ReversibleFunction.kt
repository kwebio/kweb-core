package kweb.state

/**
 * Defines a reversible function, for use by [KVar.map].
 */
abstract class ReversibleFunction<Input, Output>(val label: String) {
    abstract operator fun invoke(from: Input): Output

    abstract fun reverse(original: Input, change: Output): Input
}

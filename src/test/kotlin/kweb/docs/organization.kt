package kweb.docs

import kweb.state.KVar
import dev.forkhandles.result4k.Result
import kweb.ElementCreator
import kweb.state.Renderable

class EditableField(val username : KVar<String>, val validator : (String) -> Result<Nothing, String>)
    : Renderable {
    override fun render(element: ElementCreator<*>) {
        TODO("Not yet implemented")
    }

}


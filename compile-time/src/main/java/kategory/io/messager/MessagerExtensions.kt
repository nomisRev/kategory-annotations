package kategory.io.messager

import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic.Kind.*

fun Messager.log(message: String) {
    this.printMessage(NOTE, message)
}

fun Messager.logW(message: String) {
    this.printMessage(WARNING, message)
}

fun Messager.logMW(message: String) {
    this.printMessage(MANDATORY_WARNING, message)
}

fun Messager.logE(message: String, vararg args: Any) {
    var formattedMsg = message
    if (args.isNotEmpty()) {
        formattedMsg = String.format(message, *args)
    }
    this.printMessage(ERROR, formattedMsg)
}

fun Messager.logE(element: Element, message: String, vararg args: Any) {
    var formattedMsg = message
    if (args.isNotEmpty()) {
        formattedMsg = String.format(message, *args)
    }
    this.printMessage(ERROR, formattedMsg, element)
}

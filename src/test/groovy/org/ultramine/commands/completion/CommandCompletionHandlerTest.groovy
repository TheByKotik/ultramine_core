package org.ultramine.commands.completion

import spock.lang.Specification

class CommandCompletionHandlerTest extends Specification {

    def "Test completion"() {
        setup:
        def argumentHandler = Mock(IArgumentCompletionHandler)
        def commandHandler = new CommandCompletionHandler()

        when: "Add argument handler to command handler"
        commandHandler.addNextArgument(argumentHandler, "a", "b")

        and: "Get completion for first argument"
        commandHandler.getCompletionOptions("as")

        then: "Argument handler is called"
        1 * argumentHandler.handleCompletion("as", "a", "b")
    }
}

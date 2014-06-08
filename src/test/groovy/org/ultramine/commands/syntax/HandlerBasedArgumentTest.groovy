package org.ultramine.commands.syntax

import spock.lang.Specification

class HandlerBasedArgumentTest extends Specification {


    def "Test argument replacement"() {
        setup:
        def argument = new HandlerBasedArgument("name", "p1", "&0", "p3")
        def handler = Mock(IArgumentCompletionHandler)
        argument.setCompletionHandler(handler)

        when: "Get completion for argument"
        argument.getCompletionOptions("first", "second")

        then: "Argument handler is called with right params"
        1 * handler.handleCompletion("second", "p1", "first", "p3")
    }

    def "Test argument replacement: out if bound index"() {
        setup:
        def argument = new HandlerBasedArgument("name", "p1", "&5", "p3")
        def handler = Mock(IArgumentCompletionHandler)
        argument.setCompletionHandler(handler)

        when: "Get completion for argument"
        argument.getCompletionOptions("first", "second")

        then: "Argument handler is called with right params"
        1 * handler.handleCompletion("second", "p1", "", "p3")
    }

    def "Test argument replacement: not number"() {
        setup:
        def argument = new HandlerBasedArgument("name", "p1", "&aza", "p3")
        def handler = Mock(IArgumentCompletionHandler)
        argument.setCompletionHandler(handler)

        when: "Get completion for argument"
        argument.getCompletionOptions("first", "second")

        then: "Argument handler is called with right params"
        1 * handler.handleCompletion("second", "p1", "&aza", "p3")
    }
}

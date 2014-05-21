package org.ultramine.commands.completion

import spock.lang.Specification

class CommandCompletionHandlerTest extends Specification {

    def commandHandler = new CommandCompletionHandler()

    def "Test completion"() {
        setup:
        def argumentHandler = Mock(IArgumentCompletionHandler)

        when: "Add argument handler to command handler"
        commandHandler.addNextArgument(argumentHandler, "p1", "p2")

        and: "Get completion for first argument"
        commandHandler.getCompletionOptions("first")

        then: "Argument handler is called"
        1 * argumentHandler.handleCompletion("first", "p1", "p2")
    }

    def "Test ignored argument"() {
        when: "Add ignored argument"
        commandHandler.ignoreNextArgument()

        and: "Get completion for first argument"
        def result = commandHandler.getCompletionOptions("first")

        then: "Result is null"
        result == null
    }

    def "Test argument replacement"() {
        setup:
        def argumentHandler = Mock(IArgumentCompletionHandler)

        when: "Add argument handler with linked params to command handler"
        commandHandler.ignoreNextArgument()
        commandHandler.addNextArgument(argumentHandler, "p1", "&0", "p3")

        and: "Get completion for second argument"
        commandHandler.getCompletionOptions("first", "second")

        then: "Argument handler is called with right params"
        1 * argumentHandler.handleCompletion("second", "p1", "first", "p3")
    }

    def "Test argument replacement: out if bound index"() {
        setup:
        def argumentHandler = Mock(IArgumentCompletionHandler)

        when: "Add argument handler with linked params to command handler"
        commandHandler.ignoreNextArgument()
        commandHandler.addNextArgument(argumentHandler, "p1", "&5", "p3")

        and: "Get completion for second argument"
        commandHandler.getCompletionOptions("first", "second")

        then: "Argument handler is called with right params"
        1 * argumentHandler.handleCompletion("second", "p1", "&5", "p3")
    }

    def "Test infinite handler"() {
        setup:
        def argumentHandler = Mock(IArgumentCompletionHandler)
        commandHandler.addNextArgument(argumentHandler)

        when: "Get completion for second argument"
        def result = commandHandler.getCompletionOptions("first", "second")

        then: "Argument handler is not called"
        0 * argumentHandler._

        and: "Result is null"
        result == null

        when: "Make command handler infinite"
        commandHandler.makeInfinite()

        and: "Get completion for second and third argument"
        commandHandler.getCompletionOptions("first", "second")
        commandHandler.getCompletionOptions("first", "second", "third")

        then: "Argument handler is called"
        1 * argumentHandler.handleCompletion("second")
        1 * argumentHandler.handleCompletion("third")
    }

    def "Test username argument"() {
        setup: "Command completion with username argument"
        commandHandler.ignoreNextArgument()
        commandHandler.addNextArgument(Mock(IArgumentCompletionHandler) { isUsername() >> true })
        commandHandler.addNextArgument(Mock(IArgumentCompletionHandler))

        expect: "Username argument is detected correctly"
        !commandHandler.isUsernameIndex(0)
        commandHandler.isUsernameIndex(1)
        !commandHandler.isUsernameIndex(2)

        when: "Add another username argument"
        commandHandler.addNextArgument(Mock(IArgumentCompletionHandler) { isUsername() >> true })

        then: "Only first argument is detected"
        !commandHandler.isUsernameIndex(0)
        commandHandler.isUsernameIndex(1)
        !commandHandler.isUsernameIndex(2)
        !commandHandler.isUsernameIndex(3)
    }

    def "Test black handler"() {
        expect: "Always return null"
        commandHandler.getCompletionOptions("first") == null
        commandHandler.getCompletionOptions("first", "second") == null
    }

    def "Test integration"() {
        setup:
        commandHandler.ignoreNextArgument()
        commandHandler.addNextArgument(
                new IArgumentCompletionHandler() {
                    @Override
                    List<String> handleCompletion(String val, String[] args) {
                        return args.findAll { it.startsWith(val) }
                    }

                    @Override
                    boolean isUsername() {
                        return false
                    }
                }, "boom", "bod", "&0"
        )

        when: "Get completion"
        def r1 = commandHandler.getCompletionOptions("ara", "bo")
        def r2 = commandHandler.getCompletionOptions("zava", "za")
        def r3 = commandHandler.getCompletionOptions("b", "va", "vaka")

        then: "Result is correct"
        r1 == ["boom", "bod"]
        r2 == ["zava"]
        r3 == null

    }
}

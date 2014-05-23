package org.ultramine.commands.completion

import spock.lang.Specification

class CommandCompletionHandlerTest extends Specification {

    def commandHandler = new CommandCompletionHandler()

    def "Test completion"() {
        setup:
        def argumentHandler = Mock(IArgumentCompletionHandler)

        when: "Add argument handler to command handler"
        commandHandler.addNextArgument("name", argumentHandler, "p1", "p2")

        and: "Get completion for first argument"
        commandHandler.getCompletionOptions("first")

        then: "Argument handler is called"
        1 * argumentHandler.handleCompletion("first", "p1", "p2")
    }

    def "Test action completion"() {
        setup:
        commandHandler.addNextActionArgument("add", "adopt", "remove")

        expect:
        commandHandler.getCompletionOptions("ad") == ["add", "adopt"]
    }

    def "Test ignored argument"() {
        when: "Add ignored argument"
        commandHandler.ignoreNextArgument("name")

        and: "Get completion for first argument"
        def result = commandHandler.getCompletionOptions("first")

        then: "Result is null"
        result == null
    }

    def "Test argument replacement"() {
        setup:
        def argumentHandler = Mock(IArgumentCompletionHandler)

        when: "Add argument handler with linked params to command handler"
        commandHandler.ignoreNextArgument("name")
        commandHandler.addNextArgument("name", argumentHandler, "p1", "&0", "p3")

        and: "Get completion for second argument"
        commandHandler.getCompletionOptions("first", "second")

        then: "Argument handler is called with right params"
        1 * argumentHandler.handleCompletion("second", "p1", "first", "p3")
    }

    def "Test argument replacement: out if bound index"() {
        setup:
        def argumentHandler = Mock(IArgumentCompletionHandler)

        when: "Add argument handler with linked params to command handler"
        commandHandler.ignoreNextArgument("name")
        commandHandler.addNextArgument("name", argumentHandler, "p1", "&5", "p3")

        and: "Get completion for second argument"
        commandHandler.getCompletionOptions("first", "second")

        then: "Argument handler is called with right params"
        1 * argumentHandler.handleCompletion("second", "p1", "&5", "p3")
    }

    def "Test infinite handler"() {
        setup:
        def argumentHandler = Mock(IArgumentCompletionHandler)
        commandHandler.addNextArgument("name", argumentHandler)

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
        commandHandler.ignoreNextArgument("name")
        commandHandler.addNextArgument("name", Mock(IArgumentCompletionHandler) {
            isUsername() >> true
        })
        commandHandler.addNextArgument("name", Mock(IArgumentCompletionHandler))

        expect: "Username argument is detected correctly"
        !commandHandler.isUsernameIndex(0)
        commandHandler.isUsernameIndex(1)
        !commandHandler.isUsernameIndex(2)

        when: "Add another username argument"
        commandHandler.addNextArgument("name", Mock(IArgumentCompletionHandler) {
            isUsername() >> true
        })

        then: "Only first argument is detected"
        !commandHandler.isUsernameIndex(0)
        commandHandler.isUsernameIndex(1)
        !commandHandler.isUsernameIndex(2)
        !commandHandler.isUsernameIndex(3)
    }

    def "Test blank handler"() {
        expect: "Always return null"
        commandHandler.getCompletionOptions("first") == null
        commandHandler.getCompletionOptions("first", "second") == null
    }

    def "Test matching"() {
        setup:
        commandHandler.ignoreNextArgument("ignored")
        commandHandler.addNextActionArgument("add")
        commandHandler.addNextArgument("name", Mock(IArgumentCompletionHandler))

        expect: "Strict matching is correct"
        !commandHandler.match(true, "arg1")
        !commandHandler.match(true, "arg1", "add")
        commandHandler.match(true, "arg1", "add", "arg2")
        !commandHandler.match(true, "arg1", "add2", "arg2")
        !commandHandler.match(true, "arg1", "add", "arg2", "arg3")

        and: "Not strict matching is correct"
        commandHandler.match(false, "arg1")
        commandHandler.match(false, "arg1", "add")
        commandHandler.match(false, "arg1", "add", "arg2")
        !commandHandler.match(false, "arg1", "add2", "arg2")
        !commandHandler.match(false, "arg1", "add", "arg2", "arg3")

        when: "Make handler infinite"
        commandHandler.makeInfinite()

        then: "Strict matching is correct"
        !commandHandler.match(true, "arg1")
        !commandHandler.match(true, "arg1", "add")
        commandHandler.match(true, "arg1", "add", "arg2")
        !commandHandler.match(true, "arg1", "ad", "arg2")
        commandHandler.match(true, "arg1", "add", "arg2", "arg3")

        and: "Not strict matching is correct"
        commandHandler.match(false, "arg1")
        commandHandler.match(false, "arg1", "add")
        commandHandler.match(false, "arg1", "add", "arg2")
        !commandHandler.match(false, "arg1", "ad", "arg2")
        commandHandler.match(false, "arg1", "add", "arg2", "arg3")
    }

    def "Test names"() {
        setup:
        commandHandler.ignoreNextArgument("ignored")
        commandHandler.addNextActionArgument("add")
        commandHandler.addNextArgument("name", Mock(IArgumentCompletionHandler))

        expect: "Names are correct"
        commandHandler.getNames() == ["ignored", "action", "name"]
    }

    def "Test integration"() {
        setup:
        commandHandler.ignoreNextArgument("name")
        commandHandler.addNextArgument("name",
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

        expect: "Completions are correct"
        commandHandler.getCompletionOptions("ara", "bo") == ["boom", "bod"]
        commandHandler.getCompletionOptions("zava", "za") == ["zava"]
        commandHandler.getCompletionOptions("b", "va", "vaka") == null
    }
}

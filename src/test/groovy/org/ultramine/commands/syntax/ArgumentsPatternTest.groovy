package org.ultramine.commands.syntax

import spock.lang.Specification

import static org.ultramine.commands.syntax.ArgumentsPattern.MatchResult.*

class ArgumentsPatternTest extends Specification {

    def builder = new ArgumentsPattern.Builder()
    def pattern = builder.build();

    def "Test completion"() {
        setup: "Pattern with argument"
        def argument = Mock(IArgument)
        builder.addArgument("name", argument);

        when: "Get completion for first argument"
        pattern.getCompletionOptions("first")

        then: "Argument handler is called"
        1 * argument.getCompletionOptions("first")
    }

    def "Test action completion"() {
        setup: "Pattern with action"
        builder.addAction("add", "adopt", "remove")

        expect:
        pattern.getCompletionOptions("ad") == ["add", "adopt"]
    }

    def "Test action name"() {
        setup: "Pattern without action"
        builder.addArgument("name", Mock(IArgument))

        expect: "Action name is blank"
        pattern.resolveActionName("name") == ""

        when: "With one action"
        builder.addAction("add", "get")

        then: "Action name is correct"
        pattern.resolveActionName("name", "add") == "add"

        when: "With two actions"
        builder.addAction("make")

        then: "Space is separator between actions"
        pattern.resolveActionName("name", "get", "make") == "get make"
    }

    def "Test infinite handler"() {
        setup:
        def argument = Mock(IArgument)
        builder.addArgument("name", argument)

        when: "Get completion for second argument"
        def result = pattern.getCompletionOptions("first", "second")

        then: "Argument handler is not called"
        0 * argument._

        and: "Result is null"
        result == null

        when: "Make command handler infinite"
        builder.makeInfinite()

        and: "Get completion for second and third argument"
        pattern.getCompletionOptions("first", "second")
        pattern.getCompletionOptions("first", "second", "third")

        then: "Argument handler is called"
        1 * argument.getCompletionOptions("first", "second")
        1 * argument.getCompletionOptions("first", "second", "third")
    }

    def "Test username argument"() {
        setup: "Command completion with username argument"
        builder.addArgument("name", Mock(IArgument))
        builder.addArgument("name", Mock(IArgument) {
            isUsername() >> true
        })
        builder.addArgument("name", Mock(IArgument))

        expect: "Username argument is detected correctly"
        !pattern.isUsernameIndex(0)
        pattern.isUsernameIndex(1)
        !pattern.isUsernameIndex(2)

        when: "Add another username argument"
        builder.addArgument("name", Mock(IArgument) {
            isUsername() >> true
        })

        then: "Both arguments are detected"
        !pattern.isUsernameIndex(0)
        pattern.isUsernameIndex(1)
        !pattern.isUsernameIndex(2)
        pattern.isUsernameIndex(3)
    }

    def "Test blank handler"() {
        expect: "Always return null"
        pattern.getCompletionOptions("first") == null
        pattern.getCompletionOptions("first", "second") == null
    }

    def "Test matching"() {
        setup:
        builder.addArgument("name", Mock(IArgument))
        builder.addAction("add")
        builder.addArgument("name", Mock(IArgument))

        expect: "Matching is correct"
        !pattern.match("arg1")
        !pattern.match("arg1", "add")
        pattern.match("arg1", "add", "arg2")
        !pattern.match("arg1", "add2", "arg2")
        !pattern.match("arg1", "add", "arg2", "arg3")

        when: "Make handler infinite"
        builder.makeInfinite()

        then: "Matching is correct"
        !pattern.match("arg1")
        !pattern.match("arg1", "add")
        pattern.match("arg1", "add", "arg2")
        !pattern.match("arg1", "ad", "arg2")
        pattern.match("arg1", "add", "arg2", "arg3")
    }

    def "Test partial matching"()
    {
        setup:
        builder.addArgument("name", Mock(IArgument))
        builder.addAction("add")
        builder.addArgument("name", Mock(IArgument))

        expect: "Partial matching is correct"
        pattern.partialMatch("arg1") == POSSIBLY
        pattern.partialMatch("arg1", "add") == FULLY
        pattern.partialMatch("arg1", "add", "arg2") == FULLY
        pattern.partialMatch("arg1", "add2", "arg2") == NOT
        pattern.partialMatch("arg1", "add", "arg2", "arg3") == NOT

        when: "Make handler infinite"
        builder.makeInfinite()

        then: "Partial matching is correct"
        pattern.partialMatch("arg1") == POSSIBLY
        pattern.partialMatch("arg1", "add") == FULLY
        pattern.partialMatch("arg1", "add", "arg2") == FULLY
        pattern.partialMatch("arg1", "ad", "arg2") == NOT
        pattern.partialMatch("arg1", "add", "arg2", "arg3") == FULLY
    }

    def "Test names"() {
        setup:
        builder.addArgument("ignored", Mock(IArgument))
        builder.addAction("add")
        builder.addArgument("name", Mock(IArgument))

        expect: "Names are correct"
        pattern.getArgumentsNames() == ["ignored", null, "name"]
    }

    def "Test integration"() {
        setup:
        builder.addArgument("name", Mock(IArgument))
        IArgument argument = new HandlerBasedArgument("name", "boom", "bod", "&0");
        argument.setCompletionHandler(new IArgumentCompletionHandler() {
            @Override
            List<String> handleCompletion(String val, String[] params) {
                return params.findAll { it.startsWith(val) }
            }

            @Override
            boolean isUsername() {
                return false
            }
        })
        builder.addArgument("name", argument)

        expect: "Completions are correct"
        pattern.getCompletionOptions("ara", "bo") == ["boom", "bod"]
        pattern.getCompletionOptions("zava", "za") == ["zava"]
        pattern.getCompletionOptions("b", "va", "vaka") == null
    }
}

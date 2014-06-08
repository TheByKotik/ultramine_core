package org.ultramine.commands.syntax

import spock.lang.Specification

class ArgumentsPatternParserTest extends Specification {

    def parser = new ArgumentsPatternParser()

    def "Test single handler"() {
        setup:
        def argumentHandler = Mock(IArgumentCompletionHandler)
        parser.registerHandler("test", argumentHandler)
        def commandHandler = parser.parse("<test par1 par2>")

        when: "Get completion"
        commandHandler.getCompletionOptions("first")

        then: "Argument handler is called"
        1 * argumentHandler.handleCompletion("first", "par1", "par2")
    }

    def "Test not registered handler"() {
        setup:
        def argumentHandler = Mock(IArgumentCompletionHandler)
        parser.registerHandler("test", argumentHandler)
        def commandHandler = parser.parse("<test2 par1 par2>")

        when: "Get completion"
        commandHandler.getCompletionOptions("first")

        then: "Argument handler is not called"
        0 * argumentHandler._
    }

    def "Test action argument"() {
        setup:
        def commandHandler = parser.parse("[ add   remove ]")

        expect: "Action is parsed"
        commandHandler.getCompletionOptions("a") == ["add"]
        commandHandler.resolveActionName("add") == "add"
    }

    def "Test several arguments"() {
        setup:
        def playerHandler = Mock(IArgumentCompletionHandler)
        def itemHandler = Mock(IArgumentCompletionHandler)
        parser.registerHandler("player", playerHandler)
        parser.registerHandler("item", itemHandler)
        def commadHandler = parser.parse(string)

        when: "Get completions"
        commadHandler.getCompletionOptions("first")
        commadHandler.getCompletionOptions("first", "second")
        commadHandler.getCompletionOptions("first", "second", "third")
        commadHandler.getCompletionOptions("first", "second", "third", "fourth")

        then: "Argument handles called correctly"
        1 * playerHandler.handleCompletion("first")
        1 * itemHandler.handleCompletion("third", "first")
        0 * playerHandler._
        0 * itemHandler._

        where:
        string << ["<player> <> <item &0>", "  <   player> <  > < item &0   >"]
    }

    def "Test infinite"() {
        setup:
        def argumentHandler = Mock(IArgumentCompletionHandler)
        parser.registerHandler("test", argumentHandler)
        def commandHandler = parser.parse("<> <test par1 par2>...")

        when: "Get completion"
        commandHandler.getCompletionOptions("first", "second", "third")

        then: "Argument handler is called"
        1 * argumentHandler.handleCompletion("third", "par1", "par2")
    }

    def "Test naming"() {
        setup:
        parser.registerHandler("test", Mock(IArgumentCompletionHandler))
        def commandHandler = parser.parse("<p1> <test> [add remove] <test % p2> <%p3>")

        expect: "Names are correct"
        commandHandler.getArgumentsNames() == ["p1", "test", null, "p2", "p3"]
    }

    def "Test integration"() {
        setup:
        def commnadHander = parser.parse("<player> <list kick kill summon>")
        parser.registerHandlers(TestHandlers)

        expect:
        commnadHander.getCompletionOptions("B") == ["Bob", "Barny"]
        commnadHander.getCompletionOptions("Bob", "ki") == ["kick", "kill"]
        commnadHander.match("Jenifer", "aza")
        commnadHander.isUsernameIndex(0)
    }

    public static class TestHandlers {
        @ArgumentCompleter(value = "player", isUsername = true)
        public static List<String> player(String val, String[] args)
        {
            return ["Bob", "Jenifer", "Barny"].findAll { it.startsWith(val) }
        }

        @ArgumentCompleter("list")
        public static List<String> list(String val, String[] args)
        {
            return args.findAll { it.startsWith(val) }
        }

        @ArgumentValidator("player")
        public static boolean player_v(String val, String[] args)
        {
            return ["Bob", "Jenifer", "Barny"].contains(val)
        }
    }
}

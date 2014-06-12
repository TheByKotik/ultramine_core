package org.ultramine.permission

import spock.lang.Specification

/**
 * Created by Евгений on 08.05.2014.
 */
class GroupPermissionTest extends Specification {

    def "Test recursive calculation"() {
        setup: "Prepare recursive groups"
        def group1 = new GroupPermission("g1", [m1: "a"])
        def group2 = new GroupPermission("g2", [m2: "b"])
        group1.addPermission(group2)
        group2.addPermission(group1)

        when: "Calculate permissions"
        group1.calculate()

        then: "Both groups are not dirty"
        !group1.isDirty()
        !group2.isDirty()

        and: "Both groups have own meta"
        group1.getMeta("m1")
        group2.getMeta("m2")
    }

    def "Test dirty notification"() {
        setup:
        def listener = Mock(IDirtyListener)
        def group = new GroupPermission("group.test")
        group.subscribe(listener)

        when: "Group becomes dirty several times"
        group.makeDirty()
        group.makeDirty()
        group.makeDirty()

        then: "Listener is notified only once"
        1 * listener.makeDirty()
    }

    def "Test subscribing to permission changes"() {
        setup:
        def permission = Mock(IPermission) { getKey() >> "c" }
        def group = new GroupPermission("group.test")

        when: "Add permission to group"
        group.addPermission(permission)

        then: "Group subscribes to permission"
        1 * permission.subscribe(group)

        when: "Remove permission from group"
        group.removePermission(permission)

        then: "Group unsubscribes to permission"
        1 * permission.unsubscribe(group)
    }

    def "Test meta parsing"() {
        setup:
        def group = new GroupPermission("group.test", [
                priority: "200",
                perfix: "Test3"
        ])

        expect:
        group.getKey() == "group.test"
        group.getPriority() == 200
        group.getMeta("perfix") == "Test3"
        group.getMeta("asd") == ""
    }

    def "Test blank group"() {
        setup:
        def group = new GroupPermission("group.test", [:])

        expect:
        group.getKey() == "group.test"
        group.getPriority() == 0
    }
}

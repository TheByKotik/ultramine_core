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
        group1.getMetaResolver().getString("m1")
        group2.getMetaResolver().getString("m2")
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
        def sPerm = Mock(IPermission) { getKey() >> "p" }
        def cPerm = Mock(IPermission) { getKey() >> "c" }
        def group = new GroupPermission("group.test")

        when: "Add permissions to group"
        group.addPermission(sPerm)
        group.addPermission(cPerm)

        then: "Group subscribes to IChangeablePermission"
        1 * cPerm.subscribe(group)
        0 * sPerm.subscribe(_)

        when: "Remove permissions from group"
        group.removePermission(sPerm)
        group.removePermission(cPerm)

        then: "Group unsubscribes to IChangeablePermission"
        1 * cPerm.unsubscribe(group)
        0 * sPerm.unsubscribe(_)
    }

    def "Test meta parsing"() {
        setup:
        def group = new GroupPermission("group.test", [
                name: "Test1",
                description: "Test2",
                priority: 200,
                perfix: "Test3"
        ])

        expect:
        group.getKey() == "group.test"
        group.getName() == "Test1"
        group.getDescription() == "Test2"
        group.getPriority() == 200
        group.getMetaResolver().getString("perfix") == "Test3"
        group.getMetaResolver().getString("asd") == ""
        group.getMetaResolver().getInt("dsa") == 0
    }

    def "Test blank group"() {
        setup:
        def group = new GroupPermission("group.test", [:])

        expect:
        group.getKey() == "group.test"
        group.getName() == "group.test"
        group.getDescription() == ""
        group.getPriority() == 0
    }
}

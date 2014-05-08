package org.ultramine.permission

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import spock.lang.Specification

/**
 * Created by Евгений on 08.05.2014.
 */
class GroupPermissionTest extends Specification {

    MetaResolver createMetaResolver(Map meta)
    {
        def resolver = new MetaResolver()
        resolver.merge(meta, 0)
        return resolver
    }

    def "Test calculation"() {
        setup:
        def resolver = Mock(PermissionResolver)
        def perm1 = Mock(IPermission) {
            getKey() >> "p.1"
            getPermissions() >> resolver
            getMeta() >> createMetaResolver([test1: "1", test2: "1", test3: "1"])
            getPriority() >> 1
        }
        def perm2 = Mock(IPermission) {
            getKey() >> "p.2"
            getPermissions() >> resolver
            getMeta() >> createMetaResolver([test2: "2"])
            getPriority() >> 2
        }

        def group = new GroupPermission("group.test", [test1: "0"])
        group.permissionResolver = resolver
        group.addPermission(perm1)
        group.addPermission(perm2)

        when: "Calculate meta and permissions"
        group.calculate()

        then: "Permissions are calculated"
        !group.isDirty()
        1 * resolver.clear()
        1 * resolver.merge(resolver, 1)
        1 * resolver.merge(resolver, 2)
        0 * resolver._

        and: "Meta is correct"
        group.getMeta().getString("test1") == "0"
        group.getMeta().getString("test2") == "2"
        group.getMeta().getString("test3") == "1"

        when: "Calculate one more time"
        group.calculate()

        then: "Nothing happens"
        !group.isDirty()
        0 * resolver._
    }

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
        group1.getMeta().getString("m1")
        group2.getMeta().getString("m2")
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
        def cPerm = Mock(IChangeablePermission) { getKey() >> "c" }
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

    def "Test dirty methods"() {
        setup:
        def listener = Mock(IDirtyListener)
        def group = new GroupPermission("group")
        group.subscribe(listener)

        when: "Call setMeta method"
        group.calculate()
        group.setMeta("test", 21)

        then: "Group becomes dirty"
        group.isDirty()
        1 * listener.makeDirty()

        when: "Call removeMeta method"
        group.calculate()
        group.removeMeta("test")

        then: "Group becomes dirty"
        group.isDirty()
        1 * listener.makeDirty()

        when: "Call addPermission method"
        group.calculate()
        group.addPermission(new Permission("test"))

        then: "Group becomes dirty"
        group.isDirty()
        1 * listener.makeDirty()

        when: "Call removePermission method"
        group.calculate()
        group.removePermission("test")

        then: "Group becomes dirty"
        group.isDirty()
        1 * listener.makeDirty()
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
        group.getMeta().getString("perfix") == "Test3"
        group.getMeta().getString("asd") == ""
        group.getMeta().getInt("dsa") == 0
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

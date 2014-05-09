package org.ultramine.permission

import spock.lang.Specification

import static org.ultramine.permission.PermissionResolver.CheckResult.UNRESOLVED

class PermissionHolderTest extends Specification {

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

        def holder = new PermissionHolder([test1: "0"])
        holder.permissionResolver = resolver
        holder.addPermission(perm1)
        holder.addPermission(perm2)

        when: "Calculate meta and permissions"
        holder.calculate()

        then: "Permissions are calculated"
        !holder.isDirty()
        1 * resolver.clear()
        1 * resolver.merge(resolver, 1)
        1 * resolver.merge(resolver, 2)
        0 * resolver._

        and: "Meta is correct"
        holder.getMeta().getString("test1") == "0"
        holder.getMeta().getString("test2") == "2"
        holder.getMeta().getString("test3") == "1"

        when: "Calculate one more time"
        holder.calculate()

        then: "Nothing happens"
        !holder.isDirty()
        0 * resolver._
    }

    def "Test clearPermissions"() {
        setup:
        def perm = Mock(IChangeablePermission) {
            getKey() >> "p1"
            getPermissions() >> PermissionResolver.createForKey("p1", 0)
            getMeta() >> createMetaResolver([p1: 1])
        }
        def holder = new PermissionHolder([a: 1, b: 2])
        holder.addPermission(new Permission("p2"))
        holder.addPermission(perm)

        when: "Clear holder's permissions"
        holder.clearPermissions()

        then: "It contains only inner meta"
        !holder.getMeta().getInt("p1")
        holder.getMeta().getInt("a") == 1
        holder.getMeta().getInt("b") == 2

        and: "It contains no permissions"
        holder.getPermissions().check("p1") == UNRESOLVED
        holder.getPermissions().check("p2") == UNRESOLVED

        and: "It unsubscribed from all permissions"
        1 * perm.unsubscribe(holder)
    }

    def "Test clearMeta"() {
        setup:
        def perm = Mock(IChangeablePermission) {
            getKey() >> "p1"
            getPermissions() >> PermissionResolver.createForKey("p1", 0)
            getMeta() >> createMetaResolver([p1: 1])
        }
        def holder = new PermissionHolder([a: 1, b: 2])
        holder.addPermission(new Permission("p2"))
        holder.addPermission(perm)

        when: "Clear holder's meta"
        holder.clearMeta()

        then: "It contains only permission's meta"
        holder.getMeta().getInt("p1") == 1
        !holder.getMeta().getInt("a")
        !holder.getMeta().getInt("b")

        and: "It contains all permissions"
        holder.getPermissions().check("p1") != UNRESOLVED
        holder.getPermissions().check("p2") != UNRESOLVED

        and: "It did not unsubscribe from all permissions"
        0 * perm.unsubscribe(holder)
    }

    def "Test makeDirty"() {
        setup:
        def holder = new PermissionHolder()
        holder.calculate()

        when: "makeDirty is called"
        holder.makeDirty()

        then: "holder is dirty"
        holder.isDirty()
    }

    def "Test dirty methods"() {
        setup:
        def holder = Spy(PermissionHolder)

        when: "Call setMeta method"
        holder.calculate()
        holder.setMeta("test", 21)

        then: "Group becomes dirty"
        1 * holder.makeDirty()

        when: "Call removeMeta method"
        holder.calculate()
        holder.removeMeta("test")

        then: "Group becomes dirty"
        1 * holder.makeDirty()

        when: "Call addPermission method"
        holder.calculate()
        holder.addPermission(new Permission("test"))

        then: "Group becomes dirty"
        1 * holder.makeDirty()

        when: "Call removePermission method"
        holder.calculate()
        holder.removePermission("test")

        then: "Group becomes dirty"
        1 * holder.makeDirty()

        when: "Call clearPermissions method"
        holder.calculate()
        holder.clearPermissions()

        then: "Group becomes dirty"
        1 * holder.makeDirty()

        when: "Call clearMeta method"
        holder.calculate()
        holder.clearMeta()

        then: "Group becomes dirty"
        1 * holder.makeDirty()
    }
}

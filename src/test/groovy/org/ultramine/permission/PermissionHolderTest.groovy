package org.ultramine.permission

import spock.lang.Specification

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

    def "Test dirty methods"() {
        setup:
        def holder = new PermissionHolder()

        when: "Call setMeta method"
        holder.calculate()
        holder.setMeta("test", 21)

        then: "Group becomes dirty"
        holder.isDirty()

        when: "Call removeMeta method"
        holder.calculate()
        holder.removeMeta("test")

        then: "Group becomes dirty"
        holder.isDirty()

        when: "Call addPermission method"
        holder.calculate()
        holder.addPermission(new Permission("test"))

        then: "Group becomes dirty"
        holder.isDirty()

        when: "Call removePermission method"
        holder.calculate()
        holder.removePermission("test")

        then: "Group becomes dirty"
        holder.isDirty()
    }
}

package org.ultramine.permission

import spock.lang.Specification

/**
 * Created by Евгений on 08.05.2014.
 */
class GroupPermissionTest extends Specification {

    def "Test calculation"() {
        setup:
        def resolver = Mock(PermissionResolver)
        def perm1 = Mock(IPermission) {
            getKey() >> "p.1"
            getResolver() >> resolver
            getEffectiveMeta() >> [test1: "1", test2: "1", test3: "1"]
            getPriority() >> 1
        }
        def perm2 = Mock(IPermission) {
            getKey() >> "p.2"
            getResolver() >> resolver
            getEffectiveMeta() >> [test2: "2"]
            getPriority() >> 2
        }

        def group = new GroupPermission("group.test", [test1: "0"])
        group.resolver = resolver
        group.addPermission(perm1)
        group.addPermission(perm2)

        when: "Calculate meta and permissions"
        group.calculate()

        then: "Permissions are calculated"
        1 * resolver.clear()
        1 * resolver.merge(resolver, 1)
        1 * resolver.merge(resolver, 2)
        0 * resolver._

        and: "Meta is correct"
        def meta = group.getEffectiveMeta()
        meta.test1 == "0"
        meta.test2 == "2"
        meta.test3 == "1"

        when: "Calculate one more time"
        group.calculate()

        then: "Nothing happens"
        0 * resolver._

    }
}

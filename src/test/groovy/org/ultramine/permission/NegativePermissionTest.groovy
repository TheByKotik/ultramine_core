package org.ultramine.permission

import org.ultramine.permission.internal.MetaResolver
import org.ultramine.permission.internal.PermissionResolver

import static org.ultramine.permission.internal.CheckResult.*
import spock.lang.Specification

/**
 * Created by Евгений on 08.05.2014.
 */
class NegativePermissionTest extends Specification {

    def "Test wrap IPermission"() {
        setup:
        IPermission permission = Mock(IPermission) {
            getKey() >> "test.key"
            getMeta("aza") >> "aza"
            check("aza") >> TRUE
        }

        when: "Create new NegativePermission"
        def perm = new NegativePermission("^test.key", permission)

        then: "PermissionResolver was inverted"
        perm.getKey() == "^test.key"
        perm.getMeta("aza") == ""
        perm.check("aza") == FALSE
    }

    def "Test subscribe/unsubscribe permission"() {
        setup:
        IPermission permission = Mock(IPermission)

        def listener = Mock(IDirtyListener)
        def perm = new NegativePermission("key", permission)

        when: "Try to subscribe/unsubscribe listener"
        perm.subscribe(listener)
        perm.unsubscribe(listener)

        then: "Permission received interactions"
        1 * permission.subscribe(listener)
        1 * permission.unsubscribe(listener)
        0 * listener._
    }

    def "Test blank meta"() {

        when: "Create new NegativePermission"
        def perm = new NegativePermission("per", Mock(IPermission))

        then: "Description is blank"
        perm.getMeta("za") == ""
    }

    def "Test integration with group permission"() {
        setup:
        def group = new GroupPermission("group")
        group.addPermission(new DummyPermission("p1"))

        when: "Create negative permission"
        def perm = new NegativePermission("key", group)

        then: "Negative permission contains group permissions"
        perm.check("p1") == FALSE

        when: "Group permission updates"
        group.addPermission(new DummyPermission("p2"))

        then: "Negative permission also updates"
        perm.check("p1") == FALSE
        perm.check("p2") == FALSE
    }
}

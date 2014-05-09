package org.ultramine.permission

import static org.ultramine.permission.PermissionResolver.CheckResult.*
import spock.lang.Specification

/**
 * Created by Евгений on 08.05.2014.
 */
class NegativePermissionTest extends Specification {

    def "Test wrap IPermission"() {
        setup:
        IPermission permission = Mock(IPermission) {
            getKey() >> "test.key"
            getName() >> "Test Name"
            getDescription() >> "Test Description"
            getPriority() >> 100
            getPermissions() >> PermissionResolver.createForKey("test.key", 1)
        }

        when: "Create new NegativePermission"
        def perm = new NegativePermission(permission)

        then: "PermissionResolver was inverted"
        perm.getKey() == "^test.key"
        perm.getName() == "NOT: Test Name"
        perm.getDescription() == "NOT: Test Description"
        perm.getPriority() == 100
        perm.getPermissions().check("test.key") == FALSE
    }

    def "Test subscribe/unsubscribe IPermission"() {
        setup:
        IPermission permission = Mock(IPermission)

        def listener = Mock(IDirtyListener)
        def perm = new NegativePermission(permission)

        when: "Try to subscribe/unsubscribe listener"
        perm.subscribe(listener)
        perm.unsubscribe(listener)

        then: "No interaction were done"
        0 * permission._
        0 * listener._
    }

    def "Test wrap IChangeablePermission"() {
        setup: "Create new NegativePermission"
        IPermission permission = Mock(IChangeablePermission) {
            getKey() >> "test.key"
            getName() >> "Test Name"
            getDescription() >> "Test Description"
            getPriority() >> 100
            getPermissions() >> PermissionResolver.createForKey("test.key", 1)
        }

        when: "Create new NegativePermission"
        def perm = new NegativePermission(permission)

        then: "PermissionResolver was inverted"
        perm.getKey() == "^test.key"
        perm.getName() == "NOT: Test Name"
        perm.getDescription() == "NOT: Test Description"
        perm.getPriority() == 100
        perm.getPermissions().check("test.key") == FALSE

        and: "Subscribed to permission"
        1 * permission.subscribe(_)
    }

    def "Test subscribe/unsubscribe IChangeablePermission"() {
        setup:
        IPermission permission = Mock(IChangeablePermission)

        def listener = Mock(IDirtyListener)
        def perm = new NegativePermission(permission)

        when: "Try to subscribe/unsubscribe listener"
        perm.subscribe(listener)
        perm.unsubscribe(listener)

        then: "Permission received interactions"
        1 * permission.subscribe(listener)
        1 * permission.unsubscribe(listener)
        0 * listener._
    }

    def "Test blank description"() {
        setup: "Permission with blank description"
        IPermission permission = Mock(IChangeablePermission) {
            getDescription() >> ""
        }

        when: "Create new NegativePermission"
        def perm = new NegativePermission(permission)

        then: "Description is blank"
        perm.getDescription() == ""
    }

    def "Test blank meta"() {

        when: "Create new NegativePermission"
        def perm = new NegativePermission(Mock(IPermission))

        then: "Description is blank"
        perm.getMeta() == MetaResolver.BLANK_RESOLVER
    }

    def "Test integration with group permission"() {
        setup:
        def group = new GroupPermission("group")
        group.addPermission(new Permission("p1"))

        when: "Create negative permission"
        def perm = new NegativePermission(group)

        then: "Negative permission contains group permissions"
        perm.getPermissions().check("p1") == FALSE

        when: "Group permission updates"
        group.addPermission(new Permission("p2"))

        then: "Negative permission also updates"
        perm.getPermissions().check("p1") == FALSE
        perm.getPermissions().check("p2") == FALSE
    }
}

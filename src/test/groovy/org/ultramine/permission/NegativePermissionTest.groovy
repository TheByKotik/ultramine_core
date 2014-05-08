package org.ultramine.permission

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
            getMeta() >> Mock(MetaResolver) { getString(_) >> "mock" }
        }

        when: "Create new NegativePermission"
        def perm = new NegativePermission(permission)

        then: "PermissionResolver was inverted"
        perm.getKey() == "^test.key"
        perm.getName() == "NOT: Test Name"
        perm.getDescription() == "NOT: Test Description"
        perm.getPriority() == 100
        perm.getMeta().getString("1") == "mock"
        !perm.getPermissions().has("test.key")
    }

    def "Test isDirty IPermission"() {
        setup:
        IPermission permission = Mock(IPermission) {
            getPermissions() >> PermissionResolver.createForKey("test.key", 1)
            isDirty() >> true
        }

        when: "Create new NegativePermission"
        def perm = new NegativePermission(permission)

        then: "It is not dirty"
        !perm.isDirty()
        0 * permission.isDirty()
    }

    def "Test subscribe/unsubscribe IPermission"() {
        setup:
        IPermission permission = Mock(IPermission) {
            getPermissions() >> PermissionResolver.createForKey("test.key", 1)
        }

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
        setup:
        IPermission permission = Mock(IChangeablePermission) {
            getKey() >> "test.key"
            getName() >> "Test Name"
            getDescription() >> "Test Description"
            getPriority() >> 100
            getPermissions() >> PermissionResolver.createForKey("test.key", 1)
            getMeta() >> Mock(MetaResolver) { getString(_) >> "mock" }
        }

        when: "Create new NegativePermission"
        def perm = new NegativePermission(permission)

        then: "PermissionResolver was inverted"
        perm.getKey() == "^test.key"
        perm.getName() == "NOT: Test Name"
        perm.getDescription() == "NOT: Test Description"
        perm.getPriority() == 100
        perm.getMeta().getString("1") == "mock"
        !perm.getPermissions().has("test.key")
    }

    def "Test isDirty IChangeablePermission"() {
        setup:
        IPermission permission = Mock(IChangeablePermission) {
            getPermissions() >> PermissionResolver.createForKey("test.key", 1)
        }
        def perm = new NegativePermission(permission)

        when: "Wrapped permission dirty changes"
        permission.isDirty() >>> [true, false]

        then: "Negative permission dirty is changing too"
        perm.isDirty()
        !perm.isDirty()
    }

    def "Test subscribe/unsubscribe IChangeablePermission"() {
        setup:
        IPermission permission = Mock(IChangeablePermission) {
            getPermissions() >> PermissionResolver.createForKey("test.key", 1)
        }

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
            getPermissions() >> PermissionResolver.createForKey("test.key", 1)
            getDescription() >> ""
        }

        when: "Create new NegativePermission"
        def perm = new NegativePermission(permission)

        then: "Description is blank"
        perm.getDescription() == ""
    }
}

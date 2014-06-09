package org.ultramine.permission

import org.spockframework.mock.MockDetector
import spock.lang.Specification

import static org.ultramine.permission.PermissionRepository.ProxyPermission.ProxyType.*
import static CheckResult.FALSE
import static CheckResult.TRUE
import static CheckResult.UNRESOLVED

class PermissionRepositoryTest extends Specification {

    def "Test get proxy permission"() {
        setup:
        def detector = new MockDetector()
        def perm = Mock(IPermission) {
            getKey() >> "key"
            getName() >> "test"
        }
        def repository = new PermissionRepository()

        when: "Try to get not registered permission"
        def proxy = repository.getPermission("key")

        then: "Returned proxy of dummy permission"
        proxy.getWrappedPermission().class == DummyPermission
        proxy.getType() == DUMMY
        proxy.getName() == "key"

        when: "Register this permission"
        repository.registerPermission(perm)

        then: "Proxy linked to added permission"
        detector.isMock(proxy.getWrappedPermission())
        proxy.getType() != DUMMY
        proxy.getName() == "test"
    }

    def "Test registration of existed permission"() {
        setup:
        def repository = new PermissionRepository()

        when: "Register permission same key twice"
        repository.registerPermission(Mock(IPermission) { getKey() >> "key"; getName() >> "1" })
        repository.registerPermission(Mock(IPermission) { getKey() >> "key"; getName() >> "2" })

        then: "Exception is thrown"
        thrown(IllegalArgumentException)

        and: "First permission is not overwritten"
        repository.getPermission("key").getName() == "1"
    }

    def "Test proxy of IPermission"() {
        setup:
        def listener = Mock(IDirtyListener)
        def perm = Mock(IPermission) { getKey() >> "key" }
        def repository = new PermissionRepository()

        when: "Listener is subscribed to proxy permission"
        def proxy = repository.getPermission("key")
        proxy.subscribe(listener)

        and: "And IPermission is registered"
        repository.registerPermission(perm)

        then: "Listener is notified"
        1 * listener.makeDirty()

        and: "Proxy type is SIMPLE"
        proxy.getType() == SIMPLE

        when: "Add another lister"
        proxy.subscribe(Mock(IDirtyListener))

        then: "noting is happened"
        0 * perm._
    }

    def "Test proxy of IChangeablePermission"() {
        setup:
        def listener = Mock(IDirtyListener)
        def perm = Mock(IPermission) { getKey() >> "key" }
        def repository = new PermissionRepository()

        when: "Listener is subscribed to proxy permission"
        def proxy = repository.getPermission("key")
        proxy.subscribe(listener)

        and: "And IChangeablePermission is registered"
        repository.registerPermission(perm)

        then: "Listener is notified"
        1 * listener.makeDirty()

        and: "Proxy type is CHANGEABLE"
        proxy.getType() == CHANGEABLE

        and: "Listener is passed to permission"
        1 * perm.subscribe(listener)

        when: "Add another lister"
        proxy.subscribe(Mock(IDirtyListener))

        then: "Listener is added to permission"
        1 * perm.subscribe(_)

        when: "Remove listener"
        proxy.unsubscribe(listener)

        then: "Listener is removed from permission"
        1 * perm.unsubscribe(listener)
    }

    def "Test proxy unsubscribe"() {
        setup:
        def listener = Mock(IDirtyListener)
        def perm = Mock(IPermission) { getKey() >> "key" }
        def repository = new PermissionRepository()

        when: "Listener is subscribed and unsubscribe to proxy permission"
        def proxy = repository.getPermission("key")
        proxy.subscribe(listener)
        proxy.unsubscribe(listener)

        and: "And IChangeablePermission is registered"
        repository.registerPermission(perm)

        then: "0 listener passed to proxy"
        0 * perm.subscribe(_)
    }

    def "Test negative key"() {
        setup:
        def repository = new PermissionRepository()

        when: "Try to get permission with negative key"
        def perm = repository.getPermission("^group.admin")

        then: "Proxy of negative permission is return"
        perm.class == PermissionRepository.NegativePermission

        and: "Negative permission linked to proxy"
        perm.getWrappedPermission().getName() == "group.admin"
        perm.getWrappedPermission().getType() == DUMMY
    }

    def "Test registre ^* permission"() {
        setup:
        def repository = new PermissionRepository()

        when: "Try to register ^* permission"
        repository.registerPermission(Mock(IPermission) { getKey() >> "^test" })

        then: "Exception is thrown"
        thrown(IllegalArgumentException)
    }

    def "Test integration"() {
        setup:
        def repository = new PermissionRepository();
        def group1 = new GroupPermission("group1")
        group1.addPermission(repository.getPermission("p1"))
        group1.addPermission(repository.getPermission("group2"))
        repository.registerPermission(group1)

        when: "Create negative permission"
        def perm = repository.getPermission("^group1")

        then: "Negative permission contains group1 permissions"
        perm.getPermissionResolver().check("p1") == FALSE
        perm.getPermissionResolver().check("p2") == UNRESOLVED
        perm.getPermissionResolver().check("group2") == FALSE
        perm.getPermissionResolver().check("p3") == UNRESOLVED

        when: "Group permission updates"
        group1.addPermission(repository.getPermission("p2"))

        then: "Negative permission also updates"
        perm.getPermissionResolver().check("p1") == FALSE
        perm.getPermissionResolver().check("p2") == FALSE
        perm.getPermissionResolver().check("group2") == FALSE
        perm.getPermissionResolver().check("p3") == UNRESOLVED

        when: "Register group2"
        def group2 = new GroupPermission("group2")
        group2.addPermission(repository.getPermission("^p3"))
        repository.registerPermission(group2)

        then: "Negative permission also updates"
        perm.getPermissionResolver().check("p1") == FALSE
        perm.getPermissionResolver().check("p2") == FALSE
        perm.getPermissionResolver().check("group2") == UNRESOLVED
        perm.getPermissionResolver().check("p3") == TRUE

        and: "Group1 updates too"
        group1.getPermissionResolver().check("p1") == TRUE
        group1.getPermissionResolver().check("p2") == TRUE
        group1.getPermissionResolver().check("group2") == UNRESOLVED
        group1.getPermissionResolver().check("p3") == FALSE
    }
}

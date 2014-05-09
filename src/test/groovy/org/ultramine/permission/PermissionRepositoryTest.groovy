package org.ultramine.permission

import org.spockframework.mock.MockDetector
import spock.lang.Specification

import static org.ultramine.permission.PermissionRepository.ProxyPermission.ProxyType.*

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
        proxy.getWrappedPermission().class == Permission
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
        def perm = Mock(IChangeablePermission) { getKey() >> "key" }
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
        def perm = Mock(IChangeablePermission) { getKey() >> "key" }
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

    def "Test proxy isDirty"() {
        setup:
        def repository = new PermissionRepository()
        repository.registerPermission(Mock(IPermission) { getKey() >> "s" })
        repository.registerPermission(Mock(IChangeablePermission) { getKey() >> "c"; isDirty() >>> [true, false] })

        expect: "DUMMY and SIMPLE proxies are not dirty"
        !repository.getPermission("d").isDirty()
        !repository.getPermission("s").isDirty()

        and: "CHANGEABLE proxy dirty check delegated to wrapped permission"
        repository.getPermission("c").isDirty()
        !repository.getPermission("c").isDirty()
    }

    def "Test negative key"() {
        setup:
        def repository = new PermissionRepository()

        when: "Try to get permission with negative key"
        def perm = repository.getPermission("^group.admin")

        then: "Proxy of negative permission is return"
        perm.class == NegativePermission

        and: "Negative permission linked to proxy"
        perm.getWrappedPermission().getName() == "group.admin"
        perm.getWrappedPermission().getType() == DUMMY
    }
}

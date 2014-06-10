package org.ultramine.permission

import org.ultramine.permission.internal.MetaResolver
import org.ultramine.permission.internal.PermissionHolder
import org.ultramine.permission.internal.PermissionResolver
import spock.lang.Specification

import static org.ultramine.permission.internal.CheckResult.FALSE
import static org.ultramine.permission.internal.CheckResult.TRUE
import static org.ultramine.permission.internal.CheckResult.UNRESOLVED

class PermissionHolderTest extends Specification {

    MetaResolver createMetaResolver(Map meta)
    {
        def resolver = new MetaResolver()
        resolver.merge(meta, 0)
        return resolver
    }

    def "Test calculation"() {
        setup:
        def spy = Spy(PermissionResolver)
        def perm1 = Stub(IPermission) {
            getKey() >> "p.1"
            mergeMetaTo(_) >> { MetaResolver resolver ->
                resolver.merge([test1: "1", test2: "1", test3: "1"], 1)
            }
            mergePermissionsTo(_) >> { PermissionResolver resolver ->
                resolver.merge([test1: true, test2: true], 1)
            }
        }
        def perm2 = Stub(IPermission) {
            getKey() >> "p.2"
            mergeMetaTo(_) >> { MetaResolver resolver ->
                resolver.merge([test2: "2"], 2)
            }
            mergePermissionsTo(_) >> { PermissionResolver resolver ->
                resolver.merge([test2: false], 2)
            }
        }

        def holder = new PermissionHolder([test1: "0"])
        holder.permissionResolver = spy
        holder.addPermission(perm1)
        holder.addPermission(perm2)

        when: "Calculate meta and permissions"
        holder.calculate()

        then: "Permissions are calculated"
        !holder.isDirty()
        holder.check("test1") == TRUE
        holder.check("test2") == FALSE
        holder.check("test3") == UNRESOLVED

        and: "Meta is correct"
        holder.getMeta("test1") == "0"
        holder.getMeta("test2") == "2"
        holder.getMeta("test3") == "1"

        when: "Calculate one more time"
        holder.calculate()

        then: "Nothing happens"
        !holder.isDirty()
        0 * spy._
    }

    def "Test clearPermissions"() {
        setup:
        def perm = Mock(IPermission) {
            getKey() >> "p1"
            mergeMetaTo(_) >> { MetaResolver resolver ->
                resolver.merge([test2: "1"], 0)
            }
            mergePermissionsTo(_) >> { PermissionResolver resolver ->
                resolver.merge([test2: true], 0)
            }
        }
        def holder = new PermissionHolder([a: "1", b: "2"])
        holder.addPermission(new DummyPermission("p2"))
        holder.addPermission(perm)
        holder.calculate()

        when: "Clear holder's permissions"
        holder.clearPermissions()

        then: "It contains only inner meta"
        !holder.getMeta("test2")
        holder.getMeta("a") == "1"
        holder.getMeta("b") == "2"

        and: "It contains no permissions"
        holder.check("test2") == UNRESOLVED
        holder.check("p2") == UNRESOLVED

        and: "It unsubscribed from all permissions"
        1 * perm.unsubscribe(holder)
    }

    def "Test clearMeta"() {
        setup:
        def perm = Mock(IPermission) {
            getKey() >> "p1"
            mergeMetaTo(_) >> { MetaResolver resolver ->
                resolver.merge([test2: "1"], 0)
            }
            mergePermissionsTo(_) >> { PermissionResolver resolver ->
                resolver.merge([test2: true], 0)
            }
        }
        def holder = new PermissionHolder([a: "1", b: "2"])
        holder.addPermission(new DummyPermission("p2"))
        holder.addPermission(perm)

        when: "Clear holder's meta"
        holder.clearMeta()

        then: "It contains only permission's meta"
        holder.getMeta("test2") == "1"
        !holder.getMeta("a")
        !holder.getMeta("b")

        and: "It contains all permissions"
        holder.check("test2") != UNRESOLVED
        holder.check("p2") != UNRESOLVED

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
        holder.setMeta("test", "21")

        then: "Group becomes dirty"
        1 * holder.makeDirty()

        when: "Call removeMeta method"
        holder.calculate()
        holder.removeMeta("test")

        then: "Group becomes dirty"
        1 * holder.makeDirty()

        when: "Call addPermission method"
        holder.calculate()
        holder.addPermission(new DummyPermission("test"))

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

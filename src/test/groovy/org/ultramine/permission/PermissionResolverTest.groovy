package org.ultramine.permission

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by Евгений on 08.05.2014.
 */
class PermissionResolverTest extends Specification {

    def setupSpec() {
        PermissionResolver.metaClass.addEntry = { String key, Boolean value, Integer prio ->
            delegate.permissions.put(key, value)
            delegate.priorities.put(key, prio)
        }
    }

    @Unroll
    def "Test createForKey: #key"() {
        when: "Creating resolver for key"
        def resolver = PermissionResolver.createForKey(key, 0)

        then: "Resolver has this key"
        resolver.has(key)

        where:
        key << ["test.key", "super.test.*", "^group.admin"]
    }

    def "Test createInverted"() {
        setup:
        def resolver = new PermissionResolver()
        resolver.addEntry("p.true", true, 0)
        resolver.addEntry("p.false", false, 0)

        when: "Create inverted resolver"
        def inverted = PermissionResolver.createInverted(resolver)

        then: "Permission are inverted"
        inverted.has("p.false")
        !inverted.has("p.true")

        and: "New permissions are not created"
        !inverted.has("group.admin")
    }

    def "Test wildcard"() {
        setup: "Resolver with wildcard permission"
        def resolver = new PermissionResolver()
        resolver.addEntry("test.perm.*", true, 0)

        expect: "Other permissions are not affected"
        !resolver.has("group.admin")
        !resolver.has("group.admin.super")

        and: "Parent nodes are not affected"
        !resolver.has("test")
        !resolver.has("test.perm")

        and: "Child nodes are affected"
        resolver.has("test.perm.1")
        resolver.has("test.perm.2.3")
    }

    def "Test single permission override wildcard"() {
        setup: "Resolver with wildcard and permission"
        def resolver = new PermissionResolver()
        resolver.addEntry("test.perm.*", true, 1)
        resolver.addEntry("test.perm.super", false, 0)

        expect: "Wildcard has lower priority"
        !resolver.has("test.perm.super")
        resolver.has("test.perm.super2")

        when: "Invert resolver"
        resolver = PermissionResolver.createInverted(resolver)

        then: "Same effect"
        resolver.has("test.perm.super")
        !resolver.has("test.perm.super2")
    }

    def "Test clear"() {
        setup:
        def resolver = new PermissionResolver()
        resolver.addEntry("test.perm", true, 0)

        when: "Clear resolver's data"
        resolver.clear()

        then: "It has no permissions"
        !resolver.has("test.perm")
    }

    def "Test merge"() {
        setup: "First resolver"
        def resolver1 = new PermissionResolver()
        resolver1.addEntry("test.perm", true, 1)
        resolver1.addEntry("test.perm.1", true, 1)
        resolver1.addEntry("test.perm.2", false, 1)

        and: "Second resolver"
        def resolver2 = new PermissionResolver()
        resolver2.addEntry("test.perm", false, 0)
        resolver2.addEntry("test.perm.1", false, 2)
        resolver2.addEntry("test.perm.3", true, 2)

        when: "Merging first then second"
        def result = new PermissionResolver()
        result.merge(resolver1, 1)
        result.merge(resolver2, 2)

        then:
        !result.has("test.perm")
        !result.has("test.perm.1")
        !result.has("test.perm.2")
        result.has("test.perm.3")
        !result.has("group.admin")

        when: "Merge second then first"
        result = new PermissionResolver()
        result.merge(resolver2, 2)
        result.merge(resolver1, 1)

        then: "Same effect"
        !result.has("test.perm")
        !result.has("test.perm.1")
        !result.has("test.perm.2")
        result.has("test.perm.3")
        !result.has("group.admin")

        when: "Merge first to second"
        resolver2.merge(resolver1, 1)

        then:
        resolver2.has("test.perm")
        !resolver2.has("test.perm.1")
        !resolver2.has("test.perm.2")
        resolver2.has("test.perm.3")
        !resolver2.has("group.admin")

    }
}

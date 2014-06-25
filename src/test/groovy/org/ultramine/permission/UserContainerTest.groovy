package org.ultramine.permission

import org.ultramine.permission.internal.UserContainer
import spock.lang.Specification

class UserContainerTest extends Specification {

    def stubUser(String name, Map<String, Boolean> permissions) {
        def user = new User(name)
        user.permissionResolver.merge(permissions, 0)
        return user
    }

    def "Test permissions"() {
        setup:
        def parent = new UserContainer()
        def child = new UserContainer(parent)

        when: "Add user to parent container"
        parent.add(stubUser("parent", [parent: true]))

        then: "Both container has user with permission"
        parent.checkUserPermission("parent", "parent")
        child.checkUserPermission("parent", "parent")

        when: "Add user to child container"
        child.add(stubUser("child", [child: true]))

        then: "Only child container has user with permission"
        !parent.checkUserPermission("child", "child")
        child.checkUserPermission("child", "child")

        when: "Override parent user in child container"
        child.add(stubUser("parent", [parent: false, child: true]))

        then: "Parent container permissions have lower priority"
        !child.checkUserPermission("parent", "parent")
        child.checkUserPermission("parent", "child")

        and: "Parent container permissions is not modified"
        parent.checkUserPermission("parent", "parent")
        !parent.checkUserPermission("parent", "child")
    }

    def "Test second user cannot overwrite first"() {
        setup:
        def container = new UserContainer()
        container.add(stubUser("u", [p1: true]))

        when: "Try to add user with same name"
        container.add(stubUser("u", [p1:false, p2: true]))

        then: "User is not overwritten"
        container.checkUserPermission("u", "p1")
        !container.checkUserPermission("u", "p2")
    }
}

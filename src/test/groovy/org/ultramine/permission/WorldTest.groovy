package org.ultramine.permission

import spock.lang.Specification

class WorldTest extends Specification {

    def "Test config parsing"() {
        setup:
        def container = new World(new PermissionRepository())
        container.load(testWorldData)

        expect: "Permissions are loaded correctly"
        container.checkUserPermission("user1", "d")
        container.checkUserPermission("user1", "p.1")
        !container.checkUserPermission("user1", "p.2")
        !container.checkUserPermission("user1", "p.3")
        !container.checkUserPermission("user1", "group.admin")

        !container.checkUserPermission("user2", "d")
        !container.checkUserPermission("user2", "p.1")
        !container.checkUserPermission("user2", "p.2")
        container.checkUserPermission("user2", "p.3")
        !container.checkUserPermission("user2", "group.admin")

        and: "Meta is loaded correctly"
        container.get("user1").getMeta("a") == "a"
        container.get("user1").getMeta("b") == "1"

        !container.get("user2").getMeta("a")
        !container.get("user2").getMeta("b")
    }

    def "Test config reloading"() {
        setup:
        def repository = new PermissionRepository()
        def container = new World(repository)
        container.load(testWorldData)

        when: "Add permission and meta to user"
        container.get("user1").addPermission(repository.getPermission("test"))
        container.get("user2").setMeta("test", "data")

        then: "User have this permission and meta"
        container.checkUserPermission("user1", "test")
        container.get("user2").getMeta("test") == "data"

        when: "Reloading container"
        container.load(testWorldData)

        then: "User have not this permission and meta"
        !container.checkUserPermission("user1", "test")
        !container.get("user2").getMeta("test")

        and: "Container is reloaded correctly"
        container.checkUserPermission("user1", "d")
        container.checkUserPermission("user1", "p.1")
        !container.checkUserPermission("user1", "p.2")
        !container.checkUserPermission("user1", "p.3")
        !container.checkUserPermission("user1", "group.admin")

        !container.checkUserPermission("user2", "d")
        !container.checkUserPermission("user2", "p.1")
        !container.checkUserPermission("user2", "p.2")
        container.checkUserPermission("user2", "p.3")
        !container.checkUserPermission("user2", "group.admin")

        container.get("user1").getMeta("a") == "a"
        container.get("user1").getMeta("b") == "1"
        !container.get("user2").getMeta("a")
        !container.get("user2").getMeta("b")
    }

    def "Test config saving"() {
        setup:
        def repository = new PermissionRepository()
        def container = new World(repository)
        def user = new User("test")
        user.addPermission(repository.getPermission("p1"))
        user.addPermission(repository.getPermission("^p2"))

        when: "Add data to container"
        container.add(user)
        container.getDefaultPermissions().addPermission(repository.getPermission("d1"))

        and: "Save data"
        def data = container.save()

        then: "Output data is correct"
        data.default_permissions.contains('d1')
        data.default_permissions.size() == 1
        data.users.size() == 1
        data.users['test'].permissions.containsAll(['p1', '^p2'])
        data.users['test'].permissions.size() == 2
        data.users['test'].meta.size() == 0

        when: "Try to load this config"
        def anotherContainer = new World(repository)
        anotherContainer.load(data)

        then: "Container loaded correctly"
        anotherContainer.checkUserPermission("test", "d1")
        anotherContainer.checkUserPermission("test", "p1")
        !anotherContainer.checkUserPermission("test", "p2")

        anotherContainer.checkUserPermission("test1", "d1")
        !anotherContainer.checkUserPermission("test1", "p1")
        !anotherContainer.checkUserPermission("test1", "p2")
    }


    def testWorldData = new  World.WorldData(
            default_permissions: ['d'],
            users: [
                    user1: new World.HolderData(
                            permissions: ['p.1', '^p.2'],
                            meta: [a: 'a', b: "1"]
                    ),
                    user2: new World.HolderData(
                            permissions: ['^d', 'p.3'],
                    )
            ]
    )
}

package org.ultramine.permission

import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Specification

class YamlBasedContainerTest extends Specification {

    def "Test config parsing"() {
        setup:
        def container = new YamlBasedContainer(new PermissionRepository(), testYaml)

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
        container.get("user1").getMeta().getString("a") == "a"
        container.get("user1").getMeta().getInt("b") == 1

        !container.get("user2").getMeta().getString("a")
        !container.get("user2").getMeta().getInt("b")
    }

    def "Test config reloading"() {
        setup:
        def repository = new PermissionRepository()
        def container = new YamlBasedContainer(repository, testYaml)

        when: "Add permission and meta to user"
        container.get("user1").addPermission(repository.getPermission("test"))
        container.get("user2").setMeta("test", "data")

        then: "User have this permission and meta"
        container.checkUserPermission("user1", "test")
        container.get("user2").getMeta().getString("test") == "data"

        when: "Reloading container"
        container.reload()

        then: "User have not this permission and meta"
        !container.checkUserPermission("user1", "test")
        !container.get("user2").getMeta().getString("test")

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

        container.get("user1").getMeta().getString("a") == "a"
        container.get("user1").getMeta().getInt("b") == 1
        !container.get("user2").getMeta().getString("a")
        !container.get("user2").getMeta().getInt("b")
    }

    def "Test config saving"() {
        setup:
        def file = File.createTempFile(RandomStringUtils.randomNumeric(10), ".yml")
        def repository = new PermissionRepository()
        def container = new YamlBasedContainer(repository, file)
        def user = new User("test")
        user.addPermission(repository.getPermission("p1"))
        user.addPermission(repository.getPermission("^p2"))

        when: "Add data to container"
        container.add(user)
        container.getDefaultPermissions().addPermission(repository.getPermission("d1"))

        and: "Save data"
        container.save()

        then: "Output data is correct"
        file.text ==
"""default_permissions:
- d1
users:
  test:
    meta: {}
    permissions:
    - ^p2
    - p1
"""

        when: "Try to load this config"
        def anotherContainer = new YamlBasedContainer(repository, file)

        then: "Container loaded correctly"
        anotherContainer.checkUserPermission("test", "d1")
        anotherContainer.checkUserPermission("test", "p1")
        !anotherContainer.checkUserPermission("test", "p2")

        anotherContainer.checkUserPermission("test1", "d1")
        !anotherContainer.checkUserPermission("test1", "p1")
        !anotherContainer.checkUserPermission("test1", "p2")
    }


    def testYaml = File.createTempFile(RandomStringUtils.randomNumeric(10), ".yml")
            .with { write("""
default_permissions:
- d
users:
  user1:
    permissions:
    - p.1
    - ^p.2
    meta:
      a: a
      b: 1
  user2:
    permissions:
    - ^d
    - p.3
    meta: {}
"""); it }
}

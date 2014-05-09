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

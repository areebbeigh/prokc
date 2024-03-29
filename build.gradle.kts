buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath("io.spring.gradle:dependency-management-plugin:1.1.3")
  }
}

plugins {
    id("java")
    id("io.spring.dependency-management") version("1.1.3")
}

allprojects {
  apply(plugin = "java")
  apply(plugin = "io.spring.dependency-management")

  group = "com.areebbeigh"
  version = "1.0-SNAPSHOT"

  repositories {
      mavenCentral()
  }

  dependencies {
      testImplementation(platform("org.junit:junit-bom:5.9.1"))
      testImplementation("org.junit.jupiter:junit-jupiter")
      implementation("org.apache.logging.log4j:log4j-api")
      implementation("org.apache.logging.log4j:log4j-core")
      implementation("org.apache.logging.log4j:log4j-slf4j2-impl")
      implementation("org.slf4j:slf4j-api")
      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
  }

  dependencyManagement {
    dependencies {
      dependency("org.apache.logging.log4j:log4j-api:2.22.1")
      dependency("org.apache.logging.log4j:log4j-core:2.22.1")
      dependency("org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1")
      dependency("org.slf4j:slf4j-api:2.0.11")

      dependency("org.projectlombok:lombok:1.18.30")

      dependency("com.athaydes.rawhttp:rawhttp-core:2.6.0")

      dependency("org.apache.commons:commons-lang3:3.14.0")
      dependency("org.apache.commons:commons-collections4:4.4")
      dependency("org.apache.commons:commons-pool2:2.12.0")
      dependency("org.bouncycastle:bcprov-jdk18on:1.77")
      dependency("org.bouncycastle:bcpkix-jdk18on:1.77")
      dependency("org.bouncycastle:bctls-jdk18on:1.77")
    }
  }

  tasks.test {
    useJUnitPlatform()
  }
}

dependencies {
  implementation("com.athaydes.rawhttp:rawhttp-core")
  implementation("org.apache.commons:commons-pool2")
  implementation("org.bouncycastle:bcprov-jdk18on")
  implementation("org.bouncycastle:bcpkix-jdk18on")
  implementation("org.bouncycastle:bctls-jdk18on")
  implementation(project(":proxy-core"))
}

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
      implementation("org.apache.logging.log4j:log4j-slf4j-impl")
      implementation("org.slf4j:slf4j-api")
      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
  }

  dependencyManagement {
    dependencies {
      dependency("com.athaydes.rawhttp:rawhttp-core:2.6.0")

      dependency("org.apache.logging.log4j:log4j-api:2.22.1")
      dependency("org.apache.logging.log4j:log4j-core:2.22.1")
      dependency("org.apache.logging.log4j:log4j-slf4j-impl:2.22.1")
      dependency("org.slf4j:slf4j-api:2.0.11")

      dependency("org.projectlombok:lombok:1.18.30")
    }
  }

  tasks.test {
    useJUnitPlatform()
  }
}

dependencies {
  implementation("com.athaydes.rawhttp:rawhttp-core")
}

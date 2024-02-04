plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.athaydes.rawhttp:rawhttp-core")
    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.commons:commons-collections4")
}

tasks.test {
    useJUnitPlatform()
}
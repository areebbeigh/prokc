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
    implementation("org.apache.commons:commons-pool2")
    implementation("org.bouncycastle:bcprov-jdk18on")
    implementation("org.bouncycastle:bcpkix-jdk18on")
    implementation("org.bouncycastle:bctls-jdk18on")
}

tasks.test {
    useJUnitPlatform()
}
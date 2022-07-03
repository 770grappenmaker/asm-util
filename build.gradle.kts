plugins {
    kotlin("jvm") version "1.7.0"
}

group = "com.grappenmaker"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.0")

    val asmVersion = "9.3"
    api("org.ow2.asm:asm:$asmVersion")
    api("org.ow2.asm:asm-commons:$asmVersion")
    api("org.ow2.asm:asm-util:$asmVersion")
}
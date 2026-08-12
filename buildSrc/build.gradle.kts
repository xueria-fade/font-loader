plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(libs.okhttp)
}

kotlin {
    jvmToolchain(17)
}
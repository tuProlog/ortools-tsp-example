import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("jvm") version "1.4.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.panavis.open-source:google-ortools-java:7.8.+")
    implementation("com.panavis.open-source:ortools-linux-x86-64:7.8.+")
    implementation("it.unibo.tuprolog:solve-classic-jvm:0.15.2")
    implementation("it.unibo.tuprolog:parser-core-jvm:0.15.2")
    implementation("it.unibo.tuprolog:dsl-solve-jvm:0.15.2")
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
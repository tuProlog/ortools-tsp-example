import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("jvm") version "1.4.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

val tuPrologVersion: String by project

repositories {
    maven("https://dl.bintray.com/pika-lab/tuprolog/")
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.panavis.open-source:google-ortools-java:7.8.+")
    implementation("com.panavis.open-source:ortools-linux-x86-64:7.8.+")
    implementation("it.unibo.tuprolog:repl-jvm:$tuPrologVersion")
    implementation("it.unibo.tuprolog:dsl-solve-jvm:$tuPrologVersion")
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("Repl")
}

tasks.getByName<JavaExec>("run") {
    standardInput = System.`in`
}
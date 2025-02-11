plugins {
  kotlin("jvm") version "1.7.20"
  application
}

group = "dev.baseio.slackserver"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies {
  testImplementation(kotlin("test"))
  // grpc
  implementation("io.grpc:grpc-netty-shaded:1.49.2")
  implementation("dev.baseio.slackdatalib:slack-multiplatform-generate-protos:1.0.0")

  //mongodb
  implementation("org.litote.kmongo:kmongo:4.7.1")
  implementation("org.litote.kmongo:kmongo-async:4.7.1")
  implementation("org.litote.kmongo:kmongo-coroutine:4.7.1")


  //jwt
  implementation("io.jsonwebtoken:jjwt-api:0.11.5")
  runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
  runtimeOnly("io.jsonwebtoken:jjwt-orgjson:0.11.5")

  //passwords
  implementation("at.favre.lib:bcrypt:0.9.0")

}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

application {
  mainClass.set("MainKt")
}
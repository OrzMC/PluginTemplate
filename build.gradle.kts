import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
plugins {
    base
    id("com.gradleup.shadow") version "9.2.2" apply false
}
allprojects {
    group = "cn.orzmc.template"
    version = "0.1.0-SNAPSHOT"
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
    }
}
subprojects {
    apply(plugin = "java")
    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    }
    tasks.withType<JavaCompile>().configureEach {
        options.release.set(25)
        options.encoding = "UTF-8"
    }
    tasks.withType<Test>().configureEach { useJUnitPlatform() }
}

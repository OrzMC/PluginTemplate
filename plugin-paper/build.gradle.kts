import org.gradle.api.tasks.SourceSetContainer

plugins {
    id("java")
}

val paperApiVersion = providers.gradleProperty("paperApiVersion").get()
val coreMainOutput = project(":plugin-core").extensions
    .getByType<SourceSetContainer>()
    .named("main")
    .get()
    .output

dependencies {
    implementation(project(":plugin-core"))
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
    testImplementation("io.papermc.paper:paper-api:$paperApiVersion")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.3:4.118.0")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand("version" to project.version) }
}

tasks.jar {
    archiveBaseName.set("OrzMCPluginTemplate")
    from(coreMainOutput)
}

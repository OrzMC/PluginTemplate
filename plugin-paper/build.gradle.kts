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
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand("version" to project.version) }
}

tasks.jar {
    archiveBaseName.set("OrzMCPluginTemplate")
    from(coreMainOutput)
}

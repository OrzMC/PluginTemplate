import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult

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
    // MockBukkit validates the Paper API's version-specific registries at startup.
    // Keep this exact API build aligned with the MockBukkit artifact's Paper-Version manifest.
    testImplementation("io.papermc.paper:paper-api:${providers.gradleProperty("mockBukkitPaperApiVersion").get()}")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.3:4.124.0")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") { expand("version" to project.version) }
}

tasks.jar {
    archiveBaseName.set("OrzMCPluginTemplate")
    from(coreMainOutput)
}

tasks.withType<Test>().configureEach {
    testLogging {
        events("passed", "skipped", "failed")
    }
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) = Unit
        override fun beforeTest(testDescriptor: TestDescriptor) = Unit
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) = Unit

        override fun afterSuite(descriptor: TestDescriptor, result: TestResult) {
            if (descriptor.parent == null) {
                if (result.testCount == 0L) {
                    throw GradleException("Paper integration test suite discovered no tests.")
                }
                val skippedTests = result.testCount - result.successfulTestCount - result.failedTestCount
                if (skippedTests > 0) {
                    throw GradleException(
                        "Paper integration test suite contains $skippedTests skipped tests; " +
                            "MockBukkit unsupported operations must be handled explicitly."
                    )
                }
            }
        }
    })
}

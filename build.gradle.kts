import groovy.json.JsonSlurper
import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    java
    id("xyz.jpenilla.run-paper") version "2.2.3"
    id("com.gradleup.shadow") version "8.3.5"
    alias(libs.plugins.spotless)
    alias(libs.plugins.pluginyml)
    alias(libs.plugins.publishdata)
    `maven-publish`
}

group = "de.sirywell.noisypatterns"
version = "1.0.2"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(platform("com.intellectualsites.bom:bom-newest:1.42"))
    compileOnly(libs.bundles.fawe)
    compileOnly(libs.paper)
    implementation(libs.jlibnoise)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        target("**/*.java")
    }
}

publishData {
    addBuildData()
    useEldoNexusRepos()
    publishComponent("java")
}

publishing {
    publications.create<MavenPublication>("maven") {
        publishData.configurePublication(this)
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }

            setUrl(publishData.getRepository())
            name = "EldoNexus"
        }
    }
}

tasks {
    shadowJar {
        relocate("net.royawesome.jlibnoise", "de.sirywell.noisypatterns.libs.jlibnoise")
        archiveClassifier.set("")
    }
    build {
        dependsOn(shadowJar)
    }

    // copied from PlotSquared
    register("cacheLatestFaweArtifact") {
        val lastSuccessfulBuildUrl = uri("https://ci.athion.net/job/FastAsyncWorldEdit/lastSuccessfulBuild/api/json").toURL()
        val artifact = ((JsonSlurper().parse(lastSuccessfulBuildUrl) as Map<*, *>)["artifacts"] as List<*>)
                .map { it as Map<*, *> }
                .map { it["fileName"] as String }
                .firstOrNull { it.contains("Bukkit") }
        project.ext["faweArtifact"] = artifact
    }

    runServer {
        dependsOn(getByName("cacheLatestFaweArtifact"))
        jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
        downloadPlugins {
            url("https://ci.athion.net/job/FastAsyncWorldEdit/lastSuccessfulBuild/artifact/artifacts/${project.ext["faweArtifact"]}")
        }
        minecraftVersion("1.20.4")
    }
    test {
        useJUnitPlatform()
    }
}

bukkit {
    name = "NoisyPatterns"
    author = "SirYwell"
    version = publishData.getVersion(true)
    main = "de.sirywell.noisypatterns.NoisyPatternsPlugin"
    depend = listOf("FastAsyncWorldEdit")
    apiVersion = "1.16"

    commands {
        register("noisypatterns") {
            description = "Noisy pattern info command"
        }
    }
}

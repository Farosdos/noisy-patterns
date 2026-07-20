plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
    alias(libs.plugins.spotless)
    alias(libs.plugins.pluginyml)
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

tasks {
    shadowJar {
        relocate("net.royawesome.jlibnoise", "de.sirywell.noisypatterns.libs.jlibnoise")
        archiveClassifier.set("")
    }
    build {
        dependsOn(shadowJar)
    }
    test {
        useJUnitPlatform()
    }
}

bukkit {
    name = "NoisyPatterns"
    author = "SirYwell"
    version = project.version.toString()
    main = "de.sirywell.noisypatterns.NoisyPatternsPlugin"
    depend = listOf("FastAsyncWorldEdit")
    apiVersion = "1.16"

    commands {
        register("noisypatterns") {
            description = "Noisy pattern info command"
        }
    }
}

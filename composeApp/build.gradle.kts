import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
    kotlin("plugin.serialization") version "2.0.0"
}

room { schemaDirectory("$projectDir/schemas") }

dependencies { ksp(libs.androidx.room.compiler) }

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.screenmodel)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.kotlinx.serialization.json)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation(libs.bcrypt)
            implementation(libs.coroutines.core)
            implementation(libs.coroutines.swing)

            implementation("com.itextpdf:kernel:8.0.5")
            implementation("com.itextpdf:io:8.0.5")
            implementation("com.itextpdf:layout:8.0.5")
            implementation("com.itextpdf:html2pdf:5.0.5")

            implementation("com.github.librepdf:openpdf:2.2.4")
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe)
            packageName = "Magrinov"
            version = "1.0.0"
            vendor = "Magrinov"

            windows {
                // Specify the icon file for Windows. This should be an .ico file.
                iconFile.set(project.file("src/commonMain/resources/icon.ico"))
            }
        }
    }
}

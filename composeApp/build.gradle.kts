import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import java.util.Locale

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.metro)
    alias(libs.plugins.poko)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(21)
    androidTarget {
    }

    jvm {
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmCommon") {
                withAndroidTarget()
                withJvm()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.compose.ui.util)

            implementation(libs.circuit.foundation)
            implementation(libs.circuit.overlay)
            implementation(libs.circuitx.overlays)
            implementation(libs.circuitx.gestureNav)
            implementation(libs.circuit.annotations)
        }
        maybeCreate("jvmCommonMain").apply {
            dependencies {
                implementation(compose.materialIconsExtended)
            }
        }


        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.ui.tooling)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }

        configureEach {
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            compilerOptions {
                optIn.addAll(
                    "androidx.compose.material.ExperimentalMaterialApi",
                    "androidx.compose.material3.ExperimentalMaterial3Api",
                )
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }

        targets.configureEach {
            if (platformType == KotlinPlatformType.androidJvm) {
                compilations.configureEach {
                    compileTaskProvider.configure {
                        compilerOptions {
                            freeCompilerArgs.addAll(
                                "-P",
                                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=com.slack.circuit.internal.runtime.Parcelize",
                            )
                        }
                    }
                }
            }
        }
    }
}

android {
    namespace = "dev.jvmname.sprakbund"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.jvmname.sprakbund"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

    }
}


tasks.withType<JavaCompile>().configureEach {
    // Only configure kotlin/jvm tasks with this
    if (name.startsWith("compileJvm")) {
        options.release.set(21)
    }
}

compose.desktop {
    application {
        mainClass = "dev.jvmname.sprakbund.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.jvmname.sprakbund"
            packageVersion = "1.0.0"
        }
    }
}

//ksp { arg("circuit.codegen.mode", "metro") }

fun String.capitalizeUS() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
}

val kspTargets = kotlin.targets.names.map { it.capitalizeUS() }

dependencies {
    for (target in kspTargets) {
        val targetConfigSuffix = if (target == "Metadata") "CommonMainMetadata" else target
        add("ksp${targetConfigSuffix}", libs.circuit.codegen)
    }
}
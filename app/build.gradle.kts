plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val versionMajor: Int = 3
val versionMinor: Int = 3
val versionPatch: Int = 0
val versionBuild: Int = 0

android {
    compileSdk = 33
    buildToolsVersion = "33.0.0"
    namespace = "com.github.bstartweaks"

    defaultConfig {
        applicationId = "com.github.bstartweaks"
        minSdk = 24
        targetSdk = 33
        versionCode = versionMajor * 10000 + versionMinor * 1000 + versionPatch * 100 + versionBuild
        versionName = "$versionMajor.$versionMinor.$versionPatch.$versionBuild"
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }
    buildTypes {
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    androidResources {
        additionalParameters("--allow-reserved-package-id", "--package-id", "0x45")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    packagingOptions {
        resources {
            excludes += "**"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation("com.github.kyuubiran:EzXHelper:2.0.0-alpha04")
    implementation("org.luckypray:DexKit:1.1.0")
    compileOnly("de.robv.android.xposed:api:82")
}

val adbExecutable: String = androidComponents.sdkComponents.adb.get().asFile.absolutePath

val restartHost = task("restartHost").doLast {
    exec {
        commandLine(adbExecutable, "shell", "am", "force-stop", "com.bstar.intl")
    }
    exec {
        commandLine(
            adbExecutable,
            "shell",
            "am",
            "start",
            "$(pm resolve-activity --components com.bstar.intl)"
        )
    }
}

tasks.whenTaskAdded {
    when (name) {
        "installDebug" -> {
            finalizedBy(restartHost)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.RequiresOptIn")
}


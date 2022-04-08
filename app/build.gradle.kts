plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val versionMajor: Int = 3
val versionMinor: Int = 0
val versionPatch: Int = 0
val versionBuild: Int = 0

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.github.bstartweaks"
        minSdk = 24
        targetSdk = 32
        versionCode = versionMajor * 10000 + versionMinor * 1000 + versionPatch * 100 + versionBuild
        versionName = "$versionMajor.$versionMinor.$versionPatch.$versionBuild"
    }

    buildTypes {
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
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
    namespace = "com.github.bstartweaks"
}

dependencies {
    implementation("com.github.kyuubiran:EzXHelper:0.7.8")
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

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Assinatura de release: lê keystore.properties (fora do git). Sem o arquivo, o release continua
// assinando com a chave de debug — dá pra testar no celular, mas NÃO serve pra Play Store.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val temKeystore = keystoreProps.getProperty("storeFile") != null

ksp {
    arg("room.schemaLocation","$projectDir/schemas")
}

android {
    namespace = "com.development.motorlog"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }


    defaultConfig {
        applicationId = "com.development.motorlog"
        minSdk = 28
        targetSdk = 36
        // Play Store: versionCode sempre cresce a cada envio; versionName é o que o usuário vê
        versionCode = 4
        versionName = "1.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (temKeystore) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName(if (temKeystore) "release" else "debug")
            // R8: encolhe código e recursos (Room, WorkManager e Compose trazem as próprias regras;
            // as do projeto ficam em src/main/keepRules/)
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    // o MigrationTestHelper lê os schemas exportados (schemas/<db>/<versão>.json) como assets do androidTest
    sourceSets.getByName("androidTest").assets.directories.add("schemas")
}

dependencies {
    constraints {
        // room-testing 2.8 (MigrationTestHelper) lê os schemas com kotlinx-serialization 1.8;
        // o lifecycle 2.9 traz a 1.7.3 e a resolução consistente do AGP puxava o androidTest pra ela.
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
    }
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core) // conjunto pequeno (ArrowBack, Add…); o extended ficou de fora
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Dao Dependencies
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // ViewModel Dependecies
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // lembrete diário (notificação) — ver DECISOES D11
    implementation(libs.androidx.work.runtime.ktx)
}
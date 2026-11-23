plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    // Mide que porcentaje del codigo ejecutan las pruebas
    jacoco
}

jacoco {
    toolVersion = "0.8.12"
}

/**
 * Hace visible cada prueba en la terminal.
 *
 * Por defecto Gradle solo imprime "BUILD SUCCESSFUL" y hay que ir a
 * buscar el informe HTML para saber que se ejecuto. Para la defensa
 * hace falta lo contrario: que se vea el nombre de cada prueba y su
 * resultado mientras corren.
 *
 * No se activa showStandardStreams: MockK escribe trazas internas en
 * la salida de error aunque la prueba pase, y llenan la pantalla de
 * ruido que parece un fallo sin serlo.
 */
tasks.withType<Test>().configureEach {
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    // Resumen al final: cuantas pasaron, cuantas fallaron
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(test: TestDescriptor) {}
        override fun afterTest(test: TestDescriptor, result: TestResult) {}
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            // suite.parent == null solo en el nodo raiz: asi el resumen
            // se imprime una vez y no por cada clase de prueba
            if (suite.parent == null) {
                println(
                    "\nRESUMEN: ${result.testCount} pruebas - " +
                        "${result.successfulTestCount} pasaron, " +
                        "${result.failedTestCount} fallaron, " +
                        "${result.skippedTestCount} omitidas"
                )
            }
        }
    })
}

/**
 * Informe de cobertura de las pruebas unitarias.
 *
 * Se corre con:  ./gradlew jacocoTestReport
 * El resultado queda en: app/build/reports/jacoco/jacocoTestReport/html/index.html
 *
 * Mide la LOGICA, no la interfaz. Quedan fuera a proposito:
 *
 *  - ui/views: son Composables. Una prueba unitaria no los puede
 *    ejecutar; eso se hace con pruebas instrumentadas, que corren en un
 *    emulador. Incluirlos aqui solo diluiria el numero sin decir nada
 *    sobre la calidad de las pruebas.
 *
 *  - Lo generado: Room escribe los *_Impl, Compose los
 *    ComposableSingletons y las lambdas. Nadie escribio ese codigo, asi
 *    que medir si esta probado no tiene sentido.
 */
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Cobertura de las pruebas unitarias sobre la logica de la app"

    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    val generadoOInterfaz = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*_Impl*.*",                 // Room
        "**/*_Factory*.*",
        "**/ComposableSingletons*.*",   // Compose
        "**/*ComposableLambda*.*",
        "**/*\$\$inlined*.*",
        "**/ui/views/**",               // Composables
        // Mismo caso: el login y el registro de proveedor declaran el
        // paquete ui.ui.view aunque viven en ui/views. Tambien son
        // Composables, asi que tampoco cuentan.
        "**/ui/ui/view/**",
        "**/ui/theme/**",
        "**/MainActivity*.*",           // punto de entrada, no logica
        "**/navigation/**",             // declaracion de rutas, no logica
    )

    classDirectories.setFrom(
        files(
            fileTree(layout.buildDirectory) {
                include(
                    "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes/**/*.class",
                    "tmp/kotlin-classes/debug/**/*.class"
                )
                exclude(generadoOInterfaz)
            }
        )
    )

    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) { include("**/testDebugUnitTest.exec") }
    )
}

android {
    namespace = "cl.duoc.costostock"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.duoc.costostock"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navegación entre pantallas
    implementation(libs.androidx.navigation.compose)
    // Persistencia local de la sesión y las preferencias
    implementation(libs.androidx.datastore.preferences)
    // Serializa objetos a JSON para poder guardarlos en DataStore
    implementation(libs.gson)
    // Permite usar viewModel() dentro de un Composable (MVVM)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Recurso nativo: ubicacion del dispositivo (GPS, antenas y wifi)
    implementation(libs.play.services.location)

    // Persistencia local en SQLite
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Carga de imagenes desde URL
    implementation(libs.coil.compose)

    // Genera el QR del código de retiro (Java puro, sin cámara ni permisos)
    implementation(libs.zxing.core)

    testImplementation(libs.junit)
    // Dobles de prueba: permiten probar ViewModel y Repository sin Room
    // ni Android, que es lo que exige la pauta de la evaluacion.
    testImplementation(libs.mockk)
    // runTest y TestDispatcher: ejecutan corrutinas de forma determinista
    testImplementation(libs.kotlinx.coroutines.test)
    // Para verificar lo que emite un Flow paso a paso
    testImplementation(libs.turbine)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}
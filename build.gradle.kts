plugins {
    id("com.android.application") version "${libs.versions.agp.get()}" apply false
    id("org.jetbrains.kotlin.android") version "${libs.versions.kotlin.get()}" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "${libs.versions.kotlin.get()}" apply false
    id("com.google.dagger.hilt.android") version "${libs.versions.hiltAndroid.get()}" apply false
    id("com.google.devtools.ksp") version "${libs.versions.ksp.get()}" apply false
}
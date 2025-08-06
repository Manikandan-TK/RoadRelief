package com.roadrelief.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The Application class for the RoadRelief app.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation, which
 * includes a base class for your application that serves as the
 * application-level dependency container.
 */
@HiltAndroidApp
class MainApplication : Application()
package com.example.postsapplication.core.common

import javax.inject.Qualifier

/** App-wide scope for work that must outlive a screen (e.g. server writes). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

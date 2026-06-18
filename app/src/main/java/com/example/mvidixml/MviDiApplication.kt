package com.example.mvidixml

import android.app.Application
import com.example.mvidixml.di.AppContainer

class MviDiApplication : Application() {

    /*
     * Это корень ручного Dependency Injection.
     *
     * В больших проектах здесь часто подключают Hilt/Koin/Dagger.
     * Для учебного примера ручной контейнер полезнее: видно, где создаются
     * зависимости и как Activity/ViewModel получают уже готовые объекты.
     */
    val appContainer: AppContainer by lazy {
        AppContainer()
    }
}

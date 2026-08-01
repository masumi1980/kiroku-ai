package jp.co.kirokuai.app

import android.app.Application
import jp.co.kirokuai.app.data.AppContainer

class KirokuApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }
}

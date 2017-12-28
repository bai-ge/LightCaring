请注意build.gradle文件中的配置
signingConfigs {
        config {
            keyAlias 'lightcaring'
            keyPassword 'lightcaring'
            storeFile file('C:/Users/baige/.android/lightcaring.debug.jks')
            storePassword 'lightcaring'
        }
        config_release {
            keyAlias 'lightcaring'
            keyPassword 'lightcaring'
            storeFile file('C:/Users/baige/.android/lightcaring.release.jks')
            storePassword 'lightcaring'
        }
    }
如果无法编译运行，请把工程路径下的lightcaring.debug.jks和lightcaring.release.jks复制到用户文件夹中，并修改配置文件中的用户名
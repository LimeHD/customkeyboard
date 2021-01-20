# customkeyboard

## Подключение модуля к проекту



### 1. Add it in your root build.gradle at the end of repositories:

``` git
allprojects {
	repositories {
	...
	maven { url 'https://jitpack.io' }
	}
}
```

### 2. Добавить зависимость

``` git
dependencies {
	implementation 'com.github.LimeHD:customkeyboard:Tag'
}
```

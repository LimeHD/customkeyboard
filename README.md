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
	implementation 'com.github.LimeHD:customkeyboard:53c5f3031c'
}
```

## Работа с модулем

### 1. Инициализация клавиатуры

``` git
Keyboard keyboard = new Keyboard.Builder(Activity activity, Keyboard.KeyListener(), FrameLayout frameLayout)
	.enableNumberLine(boolean b) // Включение цифрового ряда
	.setNightMode(boolean b) // Установка тёмной темы
	.build();
```
### 2. Показ клавиатуры

``` git
keyboard.showKeyboard();
```
### 3. Скрытие клавиатуры

``` git
keyboard.hideKeyboard();
```








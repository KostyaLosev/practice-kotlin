# MVI DI XML Demo

Это маленькое Android-приложение на Kotlin для знакомства с двумя идеями:

- MVI - как управлять экраном через события и состояние.
- Dependency Injection - как передавать зависимости, не создавая все прямо внутри Activity.

Интерфейс сделан через обычный XML layout, без Compose.

## Что делает приложение

На экране есть счетчик и три кнопки:

- `-` уменьшает счетчик.
- `Сброс` возвращает счетчик к нулю.
- `+` увеличивает счетчик.

Пример специально простой. Его цель - показать архитектуру, а не сложную бизнес-логику.

## Что смотреть в первую очередь

Начни с этих файлов:

1. `app/src/main/res/layout/activity_main.xml`

   Это обычная XML-разметка главного экрана. Здесь описаны текст счетчика, статус и кнопки.

2. `app/src/main/java/com/example/mvidixml/presentation/MainActivity.kt`

   Activity ничего не вычисляет сама. Она только:
   - слушает нажатия кнопок;
   - отправляет действия во ViewModel;
   - получает новое состояние;
   - перерисовывает XML-экран.

3. `app/src/main/java/com/example/mvidixml/presentation/MainContract.kt`

   Это главный файл для понимания MVI. В нем описаны:
   - `Intent` - что произошло;
   - `State` - как должен выглядеть экран;
   - `Effect` - одноразовые события, например Toast.

4. `app/src/main/java/com/example/mvidixml/presentation/MainViewModel.kt`

   Здесь находится центр MVI-потока. ViewModel принимает `Intent`, вызывает бизнес-логику и публикует новый `State`.

5. `app/src/main/java/com/example/mvidixml/di/AppContainer.kt`

   Это простой ручной Dependency Injection. Здесь создаются зависимости приложения.

## Как работает MVI на примере кнопки +

Путь одного клика выглядит так:

1. Пользователь нажимает кнопку `+` в XML-экране.
2. `MainActivity` отправляет во ViewModel событие:

   ```kotlin
   viewModel.accept(Intent.IncrementClicked)
   ```

3. `MainViewModel` получает этот `Intent`.
4. ViewModel вызывает `CounterInteractor.increment()`.
5. Interactor меняет значение через Repository.
6. ViewModel создает новый `State`.
7. `MainActivity` получает `State` и обновляет TextView.

Главная идея: экран не меняется случайно из разных мест. Все идет по одному маршруту:

```text
User action -> Intent -> ViewModel -> Interactor -> State -> UI
```

## Что такое Intent, State и Effect

### Intent

`Intent` - это действие пользователя или системы.

В этом проекте есть такие действия:

```kotlin
Load
IncrementClicked
DecrementClicked
ResetClicked
```

То есть Activity не говорит: "увеличь текст на экране". Она говорит: "пользователь нажал плюс".

### State

`State` - это полное состояние экрана в один момент времени.

В проекте он выглядит так:

```kotlin
data class State(
    val counter: Int = 0,
    val isLoading: Boolean = false,
    val statusText: String = "Нажмите кнопку, чтобы изменить счетчик"
)
```

Если Activity получила `State`, она может полностью понять, что показать на экране.

### Effect

`Effect` - это одноразовое событие.

Например, Toast после сброса счетчика:

```kotlin
ShowMessage("Счетчик сброшен")
```

Toast не стоит хранить в `State`, потому что это не постоянное состояние экрана, а событие на один раз.

## Что демонстрирует Dependency Injection

Плохой вариант был бы таким: `MainActivity` сама создает Repository, Interactor и ViewModel.

В этом проекте сделано иначе:

```text
MviDiApplication
    -> AppContainer
        -> InMemoryCounterRepository
        -> CounterInteractor
        -> MainViewModelFactory
            -> MainViewModel
```

Activity получает уже готовую фабрику:

```kotlin
(application as MviDiApplication).appContainer.mainViewModelFactory
```

Зачем это нужно:

- Activity становится проще.
- Зависимости видно в одном месте.
- Repository можно заменить, например, на базу данных или сеть.
- ViewModel легче тестировать, потому что ей можно передать фейковый Interactor.

## Слои проекта

```text
data
```

Слой данных. Здесь лежит `CounterRepository` и его простая реализация `InMemoryCounterRepository`.

```text
domain
```

Слой бизнес-логики. Здесь `CounterInteractor`, который знает, как менять счетчик.

```text
di
```

Слой создания зависимостей. Здесь `AppContainer`.

```text
presentation
```

Слой экрана. Здесь Activity, ViewModel, MVI Contract и Reducer.

## Почему здесь нет Hilt

Проект показывает Dependency Injection вручную, чтобы было видно сам принцип.

Hilt, Koin или Dagger делают похожую работу автоматически: создают объекты и передают их туда, где они нужны. Но для первого знакомства ручной DI проще: меньше магии, больше понятного кода.

## Как запустить

Собрать debug APK:

```powershell
.\gradlew.bat assembleDebug
```

APK появится здесь:

```text
app/build/outputs/apk/debug/app-debug.apk
```

В Android Studio можно просто открыть проект и нажать Run.

## Что попробовать изменить самому

- Добавить кнопку `+10`.
- Сделать новый `Intent.AddTenClicked`.
- Добавить метод `addTen()` в `CounterInteractor`.
- Обновить `MainViewModel`, чтобы он обрабатывал новый Intent.
- Добавить новый текст статуса в `MainReducer`.

Это хорошее упражнение: оно заставляет пройти весь MVI-маршрут от XML-кнопки до нового `State`.

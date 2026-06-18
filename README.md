# Customers List MVI

Android XML application that refactors the previous Customers List lab to the MVI architecture used in the lecture example.

The app keeps the customer logic from the earlier works:

- regular, medium and premium customers;
- account and phone fields;
- premium assistant phone;
- cashback settings for customer categories;
- customer details based on customer type.

The implementation is intentionally not a direct copy of either reference project. It uses the same architectural ideas, but the screen, state model and UI flow are adapted for this project.

## Architecture

The screen follows this flow:

```text
User action -> Intent -> ViewModel -> Interactor -> Repository -> Reducer -> State -> UI
```

Main files:

1. `app/src/main/res/layout/activity_main.xml`

   XML layout for the customer list screen. It contains search, the add/edit form, category cashback inputs, details output and the customer list.

2. `app/src/main/java/com/example/mvidixml/presentation/customers/CustomersContract.kt`

   Defines the MVI contract:

   - `Intent` for user actions;
   - `State` for the full screen state;
   - `Effect` for one-time UI events such as Toast messages.

3. `app/src/main/java/com/example/mvidixml/presentation/customers/CustomersViewModel.kt`

   Accepts intents, calls domain logic and publishes new state or effects.

4. `app/src/main/java/com/example/mvidixml/presentation/customers/CustomersReducer.kt`

   Builds new immutable screen states.

5. `app/src/main/java/com/example/mvidixml/domain/CustomerInteractor.kt`

   Contains business logic: loading, filtering, adding, editing, deleting, customer details and category cashback updates.

6. `app/src/main/java/com/example/mvidixml/data`

   Contains the in-memory repository and customer/category models.

7. `app/src/main/java/com/example/mvidixml/di/AppContainer.kt`

   Manual dependency injection container, similar to the lecture MVI example.

## Features To Check

- Customer list is loaded from an in-memory repository.
- Search works by name, account, phone and category.
- A new customer can be added.
- Existing customers can be edited.
- Customers can be deleted.
- Details show type-specific information:
  - regular customer text;
  - medium customer cashback for 30 days;
  - premium customer assistant phone.
- Category cashback values can be changed and saved.
- Toast messages are emitted through MVI `Effect`.

## MVI Examples In This Project

User searches:

```kotlin
viewModel.accept(Intent.SearchChanged(query))
```

User saves a customer:

```kotlin
viewModel.accept(Intent.SubmitClicked)
```

User opens details:

```kotlin
viewModel.accept(Intent.DetailsClicked(customer.id))
```

The Activity does not modify repository data directly. It only sends intents and renders the current state.

## How To Build

From the project root:

```powershell
.\gradlew.bat assembleDebug
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To run visually, open `C:\Users\PC\practice-kotlin` in Android Studio, wait for Gradle Sync, choose an emulator or connected phone, then press Run.

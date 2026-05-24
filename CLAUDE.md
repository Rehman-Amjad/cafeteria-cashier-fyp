# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android app (Java, single `:app` module) that acts as the cashier/customer-side terminal for an RFID-based cafeteria payment system. The companion hardware/admin systems write into the same Firebase Realtime Database that this app reads from — this app is essentially a thin client over RTDB with no backend of its own.

- Package: `com.technogenis.cafeteriacashier`
- Gradle 8.7, AGP 8.5.2, version catalog at `gradle/libs.versions.toml`
- `compileSdk`/`targetSdk` = 34, `minSdk` = 24, Java 8 source/target
- Launcher: `SplashActivity` (5 s delay → `LoginActivity`)

## Build / run

```bash
./gradlew assembleDebug                 # build debug APK -> app/build/outputs/apk/debug/
./gradlew installDebug                  # build + install on connected device/emulator
./gradlew test                          # JVM unit tests (app/src/test)
./gradlew connectedAndroidTest          # instrumented tests on a device (app/src/androidTest)
./gradlew :app:testDebugUnitTest --tests "com.technogenis.cafeteriacashier.ExampleUnitTest.addition_isCorrect"
./gradlew lint                          # Android lint -> app/build/reports/lint-results-debug.html
./gradlew clean
```

`local.properties` must point `sdk.dir` at a local Android SDK; it is git-ignored.

`app/google-services.json` is committed and currently points at the `cafeteriapayment-1503a` Firebase project. Replacing it changes the backing database for the whole app.

## Architecture

### Auth has two parallel paths

`LoginActivity` is the fork point and they do not share a code path:

1. **Customer/cashier login (`btnLogin`)** — `handleLogin()` does `FirebaseDatabase.getReference("customers").child(username)` and matches the entered password against `customerPin`. On success the username (treated as the customer's RFID) is saved to `SharedPreferences` under key `"rfid"` and `MainActivity` opens. There is no Firebase Auth — anyone who knows an RFID + PIN can log in directly against RTDB.
2. **Admin login (`btnAdmin`)** — hardcoded `admin`/`admin` string comparison in `LoginActivity`, opens `AdminDashboardActivity`. This is intentional and load-bearing; do not rewire it through `handleLogin`.

### Realtime Database shape (read-only from this app)

Two top-level nodes are consumed everywhere:

- `customers/{rfid}` → `CustomerModel` (`customername`, `customerPhone`, `customerPin`, `customerrfid`, `customerblance`, `customernic`)
- `buyitems/{pushId}` → `HistoryModel` (`Id` = millis timestamp string, `customerrfid`, `itemname`, `itemprice`, `itemqty`, `customerpayment`, `customerblance`)

**Field-name gotcha:** the POJO field names are intentionally lowercase-concatenated (`customerblance`, `customerrfid`, `itemname`, `customerpayment`) because Firebase deserialization matches field names against RTDB keys. The getters/setters use mixed case (`getCustomerBalance`, etc.) but the *fields* — not the accessors — are what bind to RTDB. If you rename a field you will silently break reads. Also note the misspelling `customerblance` (not `balance`) is the actual key in the DB.

`HistoryModel.itemprice` is stored as a `String` and parsed via `Integer.parseInt` in `ItemsPurchaseHistory`, `ItemPurchaseCash`, and `AdminCheckListActivity`. A non-integer or decimal value will crash those screens.

### Screen flow

```
SplashActivity ──► LoginActivity ──► MainActivity              (customer; rfid in prefs)
                                 └─► AdminDashboardActivity ──► AdminCheckListActivity
                                                                (per-customer history,
                                                                 selected via Intent extra "rfid")
```

`DashboardActivity` exists with a drawer + three fragments (`HomeFragment`, `ItemsPurchaseHistory`, `ItemPurchaseCash`) but is **not currently in the user flow** — `MainActivity` is what `LoginActivity` opens. The fragments duplicate logic that's also in `MainActivity`/`AdminCheckListActivity`; if you touch one, check the others. `ItemPurchaseCash` differs from `ItemsPurchaseHistory` only in that it filters by `customerpayment.equals("Cash")` instead of by RFID.

### Listener lifecycle

Every screen attaches `addValueEventListener` in `onCreate`/`onCreateView` and never detaches it. This leaks the activity/fragment and will fire callbacks against destroyed views on rotation or back-stack pop. When adding new RTDB code, keep the existing pattern unless you're explicitly fixing the leak — converting one screen will not match the rest.

### Shared state

`MyPreferenceManager` is a thread-safe singleton over `SharedPreferences` ("USER_DATA"). The only key currently used is `"rfid"` (the logged-in customer). Read it via `MyPreferenceManager.getInstance(ctx).getString("rfid")`.

## Conventions

- Java, not Kotlin. New files should match.
- Layouts use view IDs in lowerCamelCase (`tvItemName`, `recyclerView`) and are bound via `findViewById`. No view binding, no data binding.
- `RecyclerView` adapters live under `adapter/`, POJOs under `model/`, admin screens under `admin/`, drawer fragments under `fragment/`.
- The `:app` module is the only module; no library modules planned.

# Design: Premium Home Screen + Back Navigation

Date: 2026-05-31
Status: Approved (pending spec review)

## Context

After login, the customer lands on `HomeFragment` inside `DashboardActivity`. Today it
shows a single "latest purchase" card and nothing else, and the history sub-screens have
no way back (device-back exits the app). The user wants a richer, premium-feeling home
screen and proper back navigation.

This app is a **read-only** client over Firebase Realtime Database. No schema or backend
changes are allowed; every feature below is computed from data already present:

- `customers/{rfid}` → `customername`, `customerrfid`, `customerblance` (often stale/"0")
- `buyitems/{id}` → `itemname`, `itemprice` (line total, String), `itemqty`,
  `customerpayment` ("Card"/"Cash"), `customerblance` (balance at that moment), `Id`
  (millisecond timestamp string)

Key data facts used by this design:
- `itemprice` is the **line total** (unit × qty), so total spent = sum of `itemprice`.
- The latest transaction's `customerblance` is the truest current balance available
  (the `customers` node balance is "0" in current data).
- `Id` is a millisecond timestamp → used for sorting "recent" and formatting dates.

## Goals

1. Redesign `HomeFragment` into a premium dashboard with four sections (below).
2. Add working back navigation from the history sub-screens to Home.
3. Keep the existing full-history screen (`ItemsPurchaseHistory`) unchanged in behavior;
   reuse its adapter rather than duplicating row logic.

## Non-Goals

- No write operations (no top-up, no editing). Low-balance banner is informational only.
- No new architecture (no multi-view-type RecyclerView for the whole screen, no view
  binding). Stay with the project's `findViewById` + static-XML style.
- No charting library; Card/Cash split is shown as counts, not a graph.

## Approach

`HomeFragment` stays a single `ScrollView` → vertical `LinearLayout`. Sections are static
Material cards in XML. All numbers are computed inside the existing Firebase listener.
Two reads are used:

- **`buyitems` query** (existing): `orderByChild("customerrfid").equalTo(rfid)` — drives
  balance, stats, and the recent list.
- **`customers/{rfid}` single read** (new): for the customer's display name.

The "recent 3" preview reuses `HistoryAdapter` capped at 3 items (newest first), so row
rendering is not duplicated.

## Home layout (top → bottom)

### 1. Hero card (teal `primaryColor`)
- Time-based greeting: "Good morning/afternoon/evening, <name>".
- Customer name + RFID.
- **Current balance** large and bold, formatted with thousands separators (e.g.
  "PKR 4,675"), taken from the latest transaction's `customerblance`.

### 2. Low-balance banner
- Visible **only** when latest balance < **PKR 100**.
- Colored warning style (uses existing `red`/`Yellow` tokens), text: "Low balance —
  please top up." Informational; no action button.

### 3. Stats row (3 tiles)
Computed all-time from `buyitems`:
- **Total spent** = sum of `itemprice` (safe-parsed via `SafeParse`).
- **Orders** = number of buyitems records for this RFID.
- **Card / Cash** = count of each `customerpayment` value.

### 4. Recent purchases preview
- Last **3** transactions, newest first (sort by `Id` descending).
- Each row: item name, qty, line price, and a readable date/time parsed from `Id`
  (e.g. "30 May, 9:18 PM"). Date formatting added to `HistoryAdapter`.
- Followed by the **"View All Purchases"** button → opens `ItemsPurchaseHistory`.

## Back navigation

Problem: history screens are fragments shown via `DashboardActivity.swapFragment(...)`
with no back stack, and `OnBackPressedCallback` finishes the activity.

Fix:
- Sub-fragments are added to the fragment back stack so device-back returns to Home.
- The toolbar shows a **back arrow** when a sub-fragment is open and the **drawer ☰** when
  on Home, toggled via a `FragmentManager` back-stack-changed listener +
  `ActionBarDrawerToggle.setDrawerIndicatorEnabled(...)`.
- Back arrow / device-back pops to Home; on Home, back closes the drawer if open else
  exits (current behavior preserved).

## Affected files

- `fragment/HomeFragment.java` — new sections, customers read, stats computation,
  recent-3 via adapter, date formatting, low-balance logic.
- `res/layout/fragment_home.xml` — hero card, banner, stats tiles, recent list +
  existing "View All Purchases" button.
- `adapter/HistoryAdapter.java` — add a date/time line from `Id` (shared by recent
  preview and full history).
- `DashboardActivity.java` — back-stack-aware navigation + toolbar indicator toggle;
  `swapFragment` adds to back stack for sub-screens.
- `res/values/strings.xml`, `res/values/colors.xml` — new strings (greetings, stat
  labels, low-balance text) and any banner color token if needed.
- Helper: a small date formatter (e.g. `util/DateFormatHelper`) so `Id` → readable
  string is reused by adapter and home.

## Edge cases

- **No transactions:** hero shows name + "PKR 0"; stats all zero; recent list shows an
  empty state. The low-balance rule is simply `balance < 100`, so with no balance (0) the
  banner does show — acceptable, since 0 genuinely is low.
- **Unparseable `itemprice`/`Id`:** use `SafeParse`/guarded parsing; skip bad rows for
  sums, show "—" for bad dates. Never crash.
- **Missing customer name:** fall back to RFID in the greeting.
- **Listener lifecycle:** keep the detach-in-onDestroyView pattern already used by the
  refactored fragments.

## Verification

1. Build: `JAVA_HOME=<jdk17> ./gradlew assembleDebug` (default JDK 25 is unsupported by
   Gradle 8.7/AGP 8.5.2).
2. Log in as RFID `0011036808` / PIN `2233` (5 transactions, balance 4675).
   - Hero shows greeting + "Urwa" + "PKR 4,675"; no low-balance banner.
   - Stats: total spent = sum of the 5 line prices; Orders = 5; Card/Cash counts correct.
   - Recent shows newest 3 with readable dates; "View All Purchases" opens full list.
3. Log in as RFID `0006347025` (latest balance 30) → low-balance banner appears.
4. Open "View All Purchases" → toolbar back arrow returns to Home; device-back also
   returns to Home (does not exit). On Home, the drawer ☰ is restored.

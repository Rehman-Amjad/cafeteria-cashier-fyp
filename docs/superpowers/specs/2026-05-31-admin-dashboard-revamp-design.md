# Design: Admin Dashboard Revamp

Date: 2026-05-31
Status: Approved (pending spec review)

## Context

The admin side is currently two bare screens: `AdminDashboardActivity` (a flat list of all
customers — name, phone, RFID) and `AdminCheckListActivity` (one customer's purchase list +
total). There is no overview, no search, no aggregate reporting, and no navigation chrome
(no toolbar, back, or logout). Admin is a **read-only** client over Firebase Realtime
Database — every feature below is computed client-side from data that already exists. No
schema or backend changes.

Data available:
- `customers/{rfid}` → `customername`, `customerPhone`, `customernic`, `customerrfid`,
  `customerblance` (often stale/"0" in live data)
- `buyitems/{id}` → `itemname`, `itemprice` (line total, String), `itemqty`,
  `customerpayment` ("Card"/"Cash"), `customerblance` (balance at that moment), `Id`
  (millisecond timestamp string)

Balance definition (consistent with the customer Home built earlier): a customer's balance
is their **latest transaction's `customerblance`** (max `Id`), falling back to the
`customers` node value, or 0 if they have no transactions.

## Goals

1. Turn `AdminDashboardActivity` into a **drawer host** (toolbar + `DrawerLayout` +
   `NavigationView` + `main_frame`) mirroring the customer `DashboardActivity`, with drawer
   items: Overview · Customers · Reports · Logout · Exit. Default = Overview.
2. **Overview** fragment: cafeteria-wide stats.
3. **Customers** fragment: searchable customer list with per-customer balance + low-balance
   highlight.
4. **Reports** fragment: best-selling items (qty sold + revenue).
5. **Customer-detail upgrade**: `AdminCheckListActivity` gets a customer header + proper
   toolbar/back.
6. A pure, unit-tested aggregation helper `util/AdminStats` so fragments stay thin.

## Non-Goals

- No writes (no editing customers, balances, or items). Everything is informational.
- No new navigation paradigm — reuse the drawer + manual toolbar nav-icon pattern from
  `DashboardActivity` (hamburger at root, back arrow on sub-screens).
- No charts/graphs; Reports and splits are numeric lists/tiles.
- Per-customer detail stays a **separate activity** (`AdminCheckListActivity`), opened by
  tapping a customer row — not a fragment.

## Architecture

```
LoginActivity (admin/admin)
   └─► AdminDashboardActivity  (drawer host, toolbar, main_frame)
          ├─ AdminOverviewFragment   (default)
          ├─ AdminCustomersFragment  ── tap row ─► AdminCheckListActivity (header + list)
          ├─ AdminReportsFragment
          ├─ Logout ─► LoginActivity (clear task)
          └─ Exit
```

Toolbar nav icon is managed explicitly (the same fix applied to the customer side):
`ic_menu` at drawer root, `ic_arrow_back` on back-stack sub-fragments, with a manual click
listener (open drawer at root / `onBackPressed` otherwise). The admin toolbar uses
`wrap_content` height + `minHeight="?attr/actionBarSize"` so the edge-to-edge inset does not
crush it.

### `util/AdminStats` (new, pure, testable)

Static methods operating on a `List<HistoryModel>` (all buyitems) and/or
`List<CustomerModel>`. No Android dependencies, unit-tested with JUnit.

- `int totalRevenue(List<HistoryModel> items)` — sum of `itemprice` via `SafeParse`.
- `int transactionCount(List<HistoryModel> items)` — `items.size()`.
- `int[] cardCashSplit(List<HistoryModel> items)` — `{cardCount, cashCount}`
  (non-"Cash" counts as Card, matching the customer-side rule).
- `int salesOnDay(List<HistoryModel> items, long dayStartMillis, long dayEndMillis)` —
  sum of `itemprice` where `Id` ∈ [start, end). Day bounds are passed in (caller computes
  from `Calendar`) so the method stays pure and testable.
- `Map<String,Integer> latestBalanceByRfid(List<HistoryModel> items)` — per RFID, the
  `customerblance` of the max-`Id` transaction (parsed to int).
- `List<ItemSalesModel> itemSales(List<HistoryModel> items)` — grouped by `itemname`,
  each with summed `qty` and summed `revenue` (itemprice); unsorted (caller sorts).

### `model/ItemSalesModel` (new)

Plain POJO: `String name; int qtySold; int revenue;` with getters. Used only for the
Reports list (not a Firebase model).

## Feature detail

### 1. AdminOverviewFragment
Layout mirrors the customer Home style: a teal hero card ("Admin Dashboard" / cafeteria
name) and stat tiles. Reads:
- `customers` (single value listener) → **Total Customers** = child count.
- `buyitems` (value listener) → **Total Revenue** (`AdminStats.totalRevenue`),
  **Total Transactions** (`transactionCount`), **Card/Cash** (`cardCashSplit`),
  **Today's Sales** (`salesOnDay` with today's local day bounds from `Calendar`).
All amounts formatted via `HomeFormat.groupedAmount` + `R.string.label_currency_pkr`.

### 2. AdminCustomersFragment
- A Material search field (`TextInputLayout` + `EditText`) at the top; a `TextWatcher`
  filters the list by `customername` or `customerrfid` (case-insensitive `contains`).
- Reads `customers` (list) and `buyitems` (to build `latestBalanceByRfid`). The fragment
  holds the full customer list + balance map and renders the filtered subset.
- `CustomerAdapter` is extended: constructor also takes a `Map<String,Integer> balanceByRfid`
  and a low-balance threshold (PKR 100). Each row shows balance via a new `tvBalance` in
  `customer_list.xml`; rows below threshold tint the balance text with `lowBalanceText`.
- Tapping a row keeps the existing behavior: `Intent` to `AdminCheckListActivity` with
  `"rfid"`.

### 3. AdminReportsFragment
- Reads `buyitems`, calls `AdminStats.itemSales`, sorts by revenue desc by default.
- A small toggle (e.g. two `MaterialButton`s or a chip pair: "By Revenue" / "By Qty")
  re-sorts the same list and calls `notifyDataSetChanged()`.
- `ItemSalesAdapter` (new) renders `item_sales_row.xml`: item name, qty sold, revenue.
- Empty state reuses `empty_history`-style text when there are no buyitems.

### 4. AdminCheckListActivity upgrade
- Add a header above the existing RecyclerView showing the customer's **name** (read
  `customers/{rfid}` once), **balance**, **total spent**, **orders**, **Card/Cash** —
  computed from the same `buyitems` query already running, via `AdminStats`.
- Wrap the screen in the toolbar pattern (or add a `MaterialToolbar`) with a back arrow
  that finishes the activity. (`AdminCheckListActivity` is a separate activity, so back =
  `finish()` / up navigation, not fragment back stack.)

## Files

Create:
- `util/AdminStats.java`, `model/ItemSalesModel.java`
- `adapter/ItemSalesAdapter.java`
- `admin/fragment/AdminOverviewFragment.java`, `AdminCustomersFragment.java`,
  `AdminReportsFragment.java`
- Layouts: `fragment_admin_overview.xml`, `fragment_admin_customers.xml`,
  `fragment_admin_reports.xml`, `item_sales_row.xml`, admin drawer header
  (`admin_drawer_header.xml`), admin drawer menu (`menu/admin_menu.xml`)
- `res/drawable/ic_arrow_back.xml` already exists (from customer-side fix); reuse.
- Test: `app/src/test/java/.../AdminStatsTest.java`

Modify:
- `admin/AdminDashboardActivity.java` → drawer host (was a flat list activity).
- `res/layout/activity_admin_dashboard.xml` → toolbar + drawer + main_frame
  (mirror `activity_dashboard.xml`, using `wrap_content`+`minHeight` toolbar).
- `adapter/CustomerAdapter.java` + `res/layout/customer_list.xml` → add balance + filter.
- `admin/AdminCheckListActivity.java` + `res/layout/activity_admin_check_list.xml` →
  header + toolbar/back.
- `res/values/strings.xml`, `res/values/colors.xml` → new labels (reuse `lowBalanceText`,
  `lowBalanceBg`, `cd_open_menu`, `cd_go_back`).

## Edge cases

- **No customers / no buyitems:** overview shows zeros; customers list shows empty state;
  reports shows empty state. No crashes.
- **Customer with no transactions:** balance falls back to `customers` node value (or 0);
  appears in list; low-balance highlight applies if < 100.
- **Unparseable `itemprice`/`Id`/`itemqty`:** guarded by `SafeParse`; bad rows contribute 0
  and are skipped where parsing fails. Never crash.
- **Search with no matches:** list empties and shows the empty state.
- **Listener lifecycle:** fragments attach in `onViewCreated` / detach in `onDestroyView`;
  `AdminCheckListActivity` keeps its `onStart`/`onStop` pattern.

## Verification

1. Build with JDK 17: `JAVA_HOME=<jdk17> ./gradlew :app:testDebugUnitTest assembleDebug`
   (default JDK 25 is unsupported). `AdminStatsTest` passes.
2. Log in as **admin / admin**.
3. **Overview**: Total Customers = 2; Total Revenue = sum of all buyitems prices;
   Total Transactions = total buyitems count; Card/Cash split correct; Today's Sales
   reflects only today's `Id`s.
4. **Customers**: search "Urwa" → filters to Urwa; each row shows balance; a customer with
   balance < 100 shows the low-balance highlight; tapping opens their detail.
5. **Reports**: items grouped (e.g. "Coke 1.5 ltr", "Brownie", …) with qty + revenue;
   default sorted by revenue; toggle re-sorts by qty.
6. **Customer detail**: header shows name + balance + total spent + orders + Card/Cash;
   toolbar back arrow returns to the customer list.
7. **Drawer**: Overview/Customers/Reports switch via drawer; back arrow appears on
   sub-screens and returns to Overview; **Logout** returns to Login.

## Notes / out of scope

- RTDB rules should declare `"buyitems": { ".indexOn": "customerrfid" }` (already noted in
  `RTDB_RULES.md` per a comment in `AdminCheckListActivity`). Correctness unaffected.
- Reports reads the entire `buyitems` node client-side; fine at FYP scale. If the dataset
  grew large, server-side aggregation would be needed — out of scope here.

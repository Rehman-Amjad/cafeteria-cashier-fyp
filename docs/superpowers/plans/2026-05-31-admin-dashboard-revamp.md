# Admin Dashboard Revamp Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the bare admin screens into a drawer-based dashboard with an Overview, a searchable customer list (with balances), a best-selling-items report, and an upgraded per-customer detail screen — all read-only over Firebase.

**Architecture:** `AdminDashboardActivity` becomes a drawer host (toolbar + `DrawerLayout` + `NavigationView` + `main_frame`) mirroring the customer `DashboardActivity`, hosting three fragments (Overview, Customers, Reports). All aggregation lives in a pure, unit-tested `AdminStats` helper. Per-customer detail stays a separate activity (`AdminCheckListActivity`), upgraded with a header + toolbar/back.

**Tech Stack:** Java, Android (Material3), Firebase Realtime Database, JUnit. No new libraries.

---

## Build / test environment

Default JDK is 25 (unsupported by Gradle 8.7 / AGP 8.5.2). Prefix every Gradle command:

```bash
export JAVA_HOME=/Users/rehmanamjad/Library/Java/JavaVirtualMachines/jbr-17.0.14/Contents/Home
```

Emulator helpers (a Pixel emulator is available):
```bash
export ANDROID_HOME=/Users/rehmanamjad/Library/Android/sdk
ADB="$ANDROID_HOME/platform-tools/adb"
```

## File structure

Create:
- `util/AdminStats.java` — pure aggregation over `List<HistoryModel>`.
- `model/ItemSalesModel.java` — per-item sales aggregate POJO.
- `adapter/ItemSalesAdapter.java` + `res/layout/item_sales_row.xml` — reports list.
- `admin/fragment/AdminOverviewFragment.java` + `res/layout/fragment_admin_overview.xml`.
- `admin/fragment/AdminCustomersFragment.java` + `res/layout/fragment_admin_customers.xml`.
- `admin/fragment/AdminReportsFragment.java` + `res/layout/fragment_admin_reports.xml`.
- `res/menu/admin_menu.xml`, `res/layout/admin_drawer_header.xml`.
- Test: `app/src/test/java/com/technogenis/cafeteriacashier/AdminStatsTest.java`.

Modify:
- `adapter/CustomerAdapter.java` + `res/layout/customer_list.xml` — add balance + low-balance highlight (backward-compatible constructor).
- `admin/AdminDashboardActivity.java` + `res/layout/activity_admin_dashboard.xml` — drawer host.
- `admin/AdminCheckListActivity.java` + `res/layout/activity_admin_check_list.xml` — header + toolbar/back.
- `res/values/strings.xml`.

Reuse unchanged: `HistoryAdapter`, `SafeParse`, `HomeFormat`, `ic_arrow_back`/`ic_menu`/`home`/`card`/`cash`/`ic_logout`/`ic_switch`/`nav_profile` drawables, `EdgeToEdgeHelper`, `lowBalanceText`/`lowBalanceBg`/`cd_open_menu`/`cd_go_back` resources.

---

## Task 1: AdminStats + ItemSalesModel (TDD)

**Files:**
- Create: `app/src/main/java/com/technogenis/cafeteriacashier/model/ItemSalesModel.java`
- Create: `app/src/main/java/com/technogenis/cafeteriacashier/util/AdminStats.java`
- Test: `app/src/test/java/com/technogenis/cafeteriacashier/AdminStatsTest.java`

- [ ] **Step 1: Create the ItemSalesModel POJO**

`app/src/main/java/com/technogenis/cafeteriacashier/model/ItemSalesModel.java`:

```java
package com.technogenis.cafeteriacashier.model;

/** Aggregated sales for one item name (used by the admin Reports screen). */
public class ItemSalesModel {

    private final String name;
    private int qtySold;
    private int revenue;

    public ItemSalesModel(String name, int qtySold, int revenue) {
        this.name = name;
        this.qtySold = qtySold;
        this.revenue = revenue;
    }

    public void add(int qty, int rev) {
        this.qtySold += qty;
        this.revenue += rev;
    }

    public String getName() { return name; }
    public int getQtySold() { return qtySold; }
    public int getRevenue() { return revenue; }
}
```

- [ ] **Step 2: Write the failing test**

`app/src/test/java/com/technogenis/cafeteriacashier/AdminStatsTest.java`:

```java
package com.technogenis.cafeteriacashier;

import static org.junit.Assert.assertEquals;

import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.model.ItemSalesModel;
import com.technogenis.cafeteriacashier.util.AdminStats;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AdminStatsTest {

    // HistoryModel(id, balance, payment, rfid, itemName, itemPrice, itemQty)
    private List<HistoryModel> sample() {
        return Arrays.asList(
                new HistoryModel("100", "500", "Card", "R1", "Coke", "60", "2"),
                new HistoryModel("200", "440", "Cash", "R1", "Burger", "50", "1"),
                new HistoryModel("150", "300", "Card", "R2", "Coke", "30", "1")
        );
    }

    @Test
    public void totalRevenue_sumsItemPrice() {
        assertEquals(140, AdminStats.totalRevenue(sample())); // 60+50+30
    }

    @Test
    public void transactionCount_isSize() {
        assertEquals(3, AdminStats.transactionCount(sample()));
    }

    @Test
    public void cardCashSplit_countsByPayment() {
        int[] split = AdminStats.cardCashSplit(sample());
        assertEquals(2, split[0]); // card
        assertEquals(1, split[1]); // cash
    }

    @Test
    public void salesOnDay_sumsWithinRange() {
        // include Ids in [100, 160): the "100" (60) and "150" (30) rows
        assertEquals(90, AdminStats.salesOnDay(sample(), 100L, 160L));
    }

    @Test
    public void latestBalanceByRfid_picksMaxId() {
        Map<String, Integer> bal = AdminStats.latestBalanceByRfid(sample());
        assertEquals(Integer.valueOf(440), bal.get("R1")); // Id 200 wins over 100
        assertEquals(Integer.valueOf(300), bal.get("R2"));
    }

    @Test
    public void itemSales_groupsByNameSummingQtyAndRevenue() {
        List<ItemSalesModel> sales = AdminStats.itemSales(sample());
        assertEquals(2, sales.size());
        ItemSalesModel coke = sales.get(0).getName().equals("Coke") ? sales.get(0) : sales.get(1);
        assertEquals(3, coke.getQtySold());   // 2 + 1
        assertEquals(90, coke.getRevenue());  // 60 + 30
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.technogenis.cafeteriacashier.AdminStatsTest"`
Expected: FAIL — `AdminStats` does not exist (compilation error).

- [ ] **Step 4: Write the implementation**

`app/src/main/java/com/technogenis/cafeteriacashier/util/AdminStats.java`:

```java
package com.technogenis.cafeteriacashier.util;

import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.model.ItemSalesModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pure aggregation helpers for the admin dashboard. No Android dependencies. */
public final class AdminStats {

    private static final String PAYMENT_CASH = "Cash";

    private AdminStats() {}

    public static int totalRevenue(List<HistoryModel> items) {
        int sum = 0;
        for (HistoryModel h : items) sum += SafeParse.parseIntOr(h.getItemPrice(), 0);
        return sum;
    }

    public static int transactionCount(List<HistoryModel> items) {
        return items.size();
    }

    /** Returns {cardCount, cashCount}; anything not "Cash" counts as Card. */
    public static int[] cardCashSplit(List<HistoryModel> items) {
        int card = 0, cash = 0;
        for (HistoryModel h : items) {
            if (PAYMENT_CASH.equalsIgnoreCase(h.getCustomerPayment())) cash++;
            else card++;
        }
        return new int[]{card, cash};
    }

    /** Sum of itemprice for rows whose Id timestamp is in [startMillis, endMillis). */
    public static int salesOnDay(List<HistoryModel> items, long startMillis, long endMillis) {
        int sum = 0;
        for (HistoryModel h : items) {
            long id = SafeParse.parseLongOr(h.getId(), 0L);
            if (id >= startMillis && id < endMillis) {
                sum += SafeParse.parseIntOr(h.getItemPrice(), 0);
            }
        }
        return sum;
    }

    /** Per RFID, the customerblance of the transaction with the largest Id. */
    public static Map<String, Integer> latestBalanceByRfid(List<HistoryModel> items) {
        Map<String, Long> latestId = new LinkedHashMap<>();
        Map<String, Integer> balance = new LinkedHashMap<>();
        for (HistoryModel h : items) {
            String rfid = h.getCustomerRfid();
            long id = SafeParse.parseLongOr(h.getId(), 0L);
            Long prev = latestId.get(rfid);
            if (prev == null || id > prev) {
                latestId.put(rfid, id);
                balance.put(rfid, SafeParse.parseIntOr(h.getCustomerBalance(), 0));
            }
        }
        return balance;
    }

    /** Group by item name, summing quantity and revenue. Unsorted; caller sorts. */
    public static List<ItemSalesModel> itemSales(List<HistoryModel> items) {
        Map<String, ItemSalesModel> map = new LinkedHashMap<>();
        for (HistoryModel h : items) {
            String name = h.getItemName();
            ItemSalesModel agg = map.get(name);
            if (agg == null) {
                agg = new ItemSalesModel(name, 0, 0);
                map.put(name, agg);
            }
            agg.add(SafeParse.parseIntOr(h.getItemQty(), 0),
                    SafeParse.parseIntOr(h.getItemPrice(), 0));
        }
        return new ArrayList<>(map.values());
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.technogenis.cafeteriacashier.AdminStatsTest"`
Expected: PASS (BUILD SUCCESSFUL).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/technogenis/cafeteriacashier/model/ItemSalesModel.java \
        app/src/main/java/com/technogenis/cafeteriacashier/util/AdminStats.java \
        app/src/test/java/com/technogenis/cafeteriacashier/AdminStatsTest.java
git commit -m "feat: add AdminStats aggregation helper + ItemSalesModel (unit tested)"
```

---

## Task 2: Strings

**Files:**
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Add admin strings**

In `app/src/main/res/values/strings.xml`, before the closing `</resources>`, add:

```xml
    <!-- Admin dashboard -->
    <string name="admin_title">Admin Dashboard</string>
    <string name="nav_overview">Overview</string>
    <string name="nav_customers">Customers</string>
    <string name="nav_reports">Reports</string>
    <string name="stat_total_customers">Customers</string>
    <string name="stat_revenue">Revenue</string>
    <string name="stat_transactions">Transactions</string>
    <string name="stat_today_sales">Today\'s Sales</string>
    <string name="hint_search_customers">Search by name or RFID</string>
    <string name="sort_by_revenue">By Revenue</string>
    <string name="sort_by_qty">By Qty</string>
    <string name="label_sold">Sold</string>
    <string name="label_revenue">Revenue</string>
    <string name="empty_items_sold">No sales yet</string>
    <string name="title_customer_detail">Customer</string>
```

- [ ] **Step 2: Verify resources compile**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/values/strings.xml
git commit -m "feat: add admin dashboard strings"
```

---

## Task 3: ItemSalesAdapter + row layout

**Files:**
- Create: `app/src/main/res/layout/item_sales_row.xml`
- Create: `app/src/main/java/com/technogenis/cafeteriacashier/adapter/ItemSalesAdapter.java`

- [ ] **Step 1: Create the row layout**

`app/src/main/res/layout/item_sales_row.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="12dp"
    android:layout_marginVertical="6dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="2dp"
    app:cardBackgroundColor="?attr/colorSurface">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:padding="16dp">

        <TextView
            android:id="@+id/tvName"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:textColor="?attr/colorOnSurface"
            android:textStyle="bold"
            android:textSize="15sp" />

        <TextView
            android:id="@+id/tvQtySold"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginEnd="20dp"
            android:textColor="@color/onSurfaceSecondary"
            android:textSize="13sp" />

        <TextView
            android:id="@+id/tvRevenue"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="?attr/colorPrimary"
            android:textStyle="bold"
            android:textSize="15sp" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

- [ ] **Step 2: Create the adapter**

`app/src/main/java/com/technogenis/cafeteriacashier/adapter/ItemSalesAdapter.java`:

```java
package com.technogenis.cafeteriacashier.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.model.ItemSalesModel;
import com.technogenis.cafeteriacashier.util.HomeFormat;

import java.util.List;

public class ItemSalesAdapter extends RecyclerView.Adapter<ItemSalesAdapter.VH> {

    private final Context context;
    private final List<ItemSalesModel> data;

    public ItemSalesAdapter(Context context, List<ItemSalesModel> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_sales_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ItemSalesModel m = data.get(position);
        holder.tvName.setText(m.getName());
        holder.tvQtySold.setText(context.getString(R.string.label_sold) + ": " + m.getQtySold());
        holder.tvRevenue.setText(context.getString(R.string.label_currency_pkr) + " "
                + HomeFormat.groupedAmount(m.getRevenue()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvQtySold, tvRevenue;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvQtySold = itemView.findViewById(R.id.tvQtySold);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
        }
    }
}
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/item_sales_row.xml \
        app/src/main/java/com/technogenis/cafeteriacashier/adapter/ItemSalesAdapter.java
git commit -m "feat: add ItemSalesAdapter for admin reports"
```

---

## Task 4: Extend CustomerAdapter with balance + low-balance highlight

**Files:**
- Modify: `app/src/main/res/layout/customer_list.xml`
- Modify: `app/src/main/java/com/technogenis/cafeteriacashier/adapter/CustomerAdapter.java`

- [ ] **Step 1: Add a balance TextView to the row**

In `app/src/main/res/layout/customer_list.xml`, add a balance `TextView` as the last child of
the inner horizontal `LinearLayout` (after the vertical name/phone/rfid block, before the
closing `</LinearLayout>` at line 64):

```xml
        <TextView
            android:id="@+id/tv_balance"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:textSize="14sp"
            android:textStyle="bold"
            android:textColor="?attr/colorPrimary"
            android:visibility="gone" />
```

- [ ] **Step 2: Update CustomerAdapter**

Replace the entire contents of
`app/src/main/java/com/technogenis/cafeteriacashier/adapter/CustomerAdapter.java` with:

```java
package com.technogenis.cafeteriacashier.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.admin.AdminCheckListActivity;
import com.technogenis.cafeteriacashier.model.CustomerModel;
import com.technogenis.cafeteriacashier.util.HomeFormat;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.MyViewHolder> {

    private final Context context;
    private final List<CustomerModel> mDatalist;
    private final Map<String, Integer> balanceByRfid;
    private final int lowBalanceThreshold;

    /** Backward-compatible: no balances shown. */
    public CustomerAdapter(Context context, List<CustomerModel> mDatalist) {
        this(context, mDatalist, Collections.emptyMap(), Integer.MIN_VALUE);
    }

    public CustomerAdapter(Context context, List<CustomerModel> mDatalist,
                           Map<String, Integer> balanceByRfid, int lowBalanceThreshold) {
        this.context = context;
        this.mDatalist = mDatalist;
        this.balanceByRfid = balanceByRfid;
        this.lowBalanceThreshold = lowBalanceThreshold;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_list, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CustomerModel model = mDatalist.get(position);
        holder.tv_name.setText(model.getCustomerName());
        holder.tv_phone.setText(model.getCustomerPhone());
        holder.tv_rfid.setText(model.getCustomerRfid());

        String rfid = model.getCustomerRfid();
        Integer balance = rfid == null ? null : balanceByRfid.get(rfid);
        if (balance != null) {
            holder.tv_balance.setVisibility(View.VISIBLE);
            holder.tv_balance.setText(context.getString(R.string.label_currency_pkr) + " "
                    + HomeFormat.groupedAmount(balance));
            int colorRes = balance < lowBalanceThreshold ? R.color.lowBalanceText : R.color.primaryColor;
            holder.tv_balance.setTextColor(ContextCompat.getColor(context, colorRes));
        } else {
            holder.tv_balance.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (rfid == null || rfid.isEmpty()) return;
            Intent intent = new Intent(context, AdminCheckListActivity.class);
            intent.putExtra("rfid", rfid);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mDatalist.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_phone, tv_rfid, tv_balance;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_phone = itemView.findViewById(R.id.tv_phone);
            tv_rfid = itemView.findViewById(R.id.tv_rfid);
            tv_balance = itemView.findViewById(R.id.tv_balance);
        }
    }
}
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL. (The existing `AdminDashboardActivity` still uses the 2-arg
constructor, which now delegates — balances stay hidden there until Task 8.)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/customer_list.xml \
        app/src/main/java/com/technogenis/cafeteriacashier/adapter/CustomerAdapter.java
git commit -m "feat: customer row shows balance with low-balance highlight"
```

---

## Task 5: AdminOverviewFragment

**Files:**
- Create: `app/src/main/res/layout/fragment_admin_overview.xml`
- Create: `app/src/main/java/com/technogenis/cafeteriacashier/admin/fragment/AdminOverviewFragment.java`

- [ ] **Step 1: Create the layout**

`app/src/main/res/layout/fragment_admin_overview.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?android:attr/colorBackground"
    android:fillViewport="true"
    android:clipToPadding="false">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <com.google.android.material.card.MaterialCardView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:cardCornerRadius="20dp"
            app:cardElevation="6dp"
            app:cardBackgroundColor="?attr/colorPrimary">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="22dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="@string/admin_title"
                    android:textColor="@color/white"
                    android:textSize="20sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="16dp"
                    android:text="@string/stat_revenue"
                    android:alpha="0.85"
                    android:textColor="@color/white"
                    android:textSize="13sp" />

                <TextView
                    android:id="@+id/tvAdminRevenue"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="@string/placeholder_amount_zero"
                    android:textColor="@color/white"
                    android:textSize="30sp"
                    android:textStyle="bold" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:orientation="horizontal"
            android:baselineAligned="false"
            android:weightSum="2">

            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginEnd="6dp"
                app:cardCornerRadius="14dp"
                app:cardElevation="2dp"
                app:cardBackgroundColor="?attr/colorSurface">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="16dp">

                    <TextView
                        android:id="@+id/tvAdminTotalCustomers"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0"
                        android:textColor="?attr/colorPrimary"
                        android:textSize="18sp"
                        android:textStyle="bold" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:text="@string/stat_total_customers"
                        android:textColor="@color/onSurfaceSecondary"
                        android:textSize="12sp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginStart="6dp"
                app:cardCornerRadius="14dp"
                app:cardElevation="2dp"
                app:cardBackgroundColor="?attr/colorSurface">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="16dp">

                    <TextView
                        android:id="@+id/tvAdminTransactions"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0"
                        android:textColor="?attr/colorPrimary"
                        android:textSize="18sp"
                        android:textStyle="bold" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:text="@string/stat_transactions"
                        android:textColor="@color/onSurfaceSecondary"
                        android:textSize="12sp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:orientation="horizontal"
            android:baselineAligned="false"
            android:weightSum="2">

            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginEnd="6dp"
                app:cardCornerRadius="14dp"
                app:cardElevation="2dp"
                app:cardBackgroundColor="?attr/colorSurface">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="16dp">

                    <TextView
                        android:id="@+id/tvAdminSplit"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0 / 0"
                        android:textColor="?attr/colorPrimary"
                        android:textSize="18sp"
                        android:textStyle="bold" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:text="@string/stat_card_cash"
                        android:textColor="@color/onSurfaceSecondary"
                        android:textSize="12sp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginStart="6dp"
                app:cardCornerRadius="14dp"
                app:cardElevation="2dp"
                app:cardBackgroundColor="?attr/colorSurface">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="16dp">

                    <TextView
                        android:id="@+id/tvAdminTodaySales"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/placeholder_amount_zero"
                        android:textColor="?attr/colorPrimary"
                        android:textSize="18sp"
                        android:textStyle="bold" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:text="@string/stat_today_sales"
                        android:textColor="@color/onSurfaceSecondary"
                        android:textSize="12sp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>

    </LinearLayout>
</ScrollView>
```

- [ ] **Step 2: Create the fragment**

`app/src/main/java/com/technogenis/cafeteriacashier/admin/fragment/AdminOverviewFragment.java`:

```java
package com.technogenis.cafeteriacashier.admin.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.util.AdminStats;
import com.technogenis.cafeteriacashier.util.HomeFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdminOverviewFragment extends Fragment {

    private TextView tvRevenue, tvCustomers, tvTransactions, tvSplit, tvTodaySales;

    private DatabaseReference customersRef, buyitemsRef;
    private ValueEventListener customersListener, buyitemsListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_overview, container, false);
        tvRevenue = root.findViewById(R.id.tvAdminRevenue);
        tvCustomers = root.findViewById(R.id.tvAdminTotalCustomers);
        tvTransactions = root.findViewById(R.id.tvAdminTransactions);
        tvSplit = root.findViewById(R.id.tvAdminSplit);
        tvTodaySales = root.findViewById(R.id.tvAdminTodaySales);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        customersRef = FirebaseDatabase.getInstance().getReference("customers");
        customersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvCustomers.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminOverview", "customers cancelled", error.toException());
            }
        };
        customersRef.addValueEventListener(customersListener);

        buyitemsRef = FirebaseDatabase.getInstance().getReference("buyitems");
        buyitemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<HistoryModel> items = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    HistoryModel h = s.getValue(HistoryModel.class);
                    if (h != null) items.add(h);
                }
                String pkr = getString(R.string.label_currency_pkr) + " ";
                tvRevenue.setText(pkr + HomeFormat.groupedAmount(AdminStats.totalRevenue(items)));
                tvTransactions.setText(String.valueOf(AdminStats.transactionCount(items)));
                int[] split = AdminStats.cardCashSplit(items);
                tvSplit.setText(split[0] + " / " + split[1]);

                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                long dayStart = c.getTimeInMillis();
                long dayEnd = dayStart + 24L * 60L * 60L * 1000L;
                tvTodaySales.setText(pkr
                        + HomeFormat.groupedAmount(AdminStats.salesOnDay(items, dayStart, dayEnd)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminOverview", "buyitems cancelled", error.toException());
            }
        };
        buyitemsRef.addValueEventListener(buyitemsListener);
    }

    @Override
    public void onDestroyView() {
        if (customersRef != null && customersListener != null) {
            customersRef.removeEventListener(customersListener);
        }
        if (buyitemsRef != null && buyitemsListener != null) {
            buyitemsRef.removeEventListener(buyitemsListener);
        }
        customersRef = null; customersListener = null;
        buyitemsRef = null; buyitemsListener = null;
        super.onDestroyView();
    }
}
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/fragment_admin_overview.xml \
        app/src/main/java/com/technogenis/cafeteriacashier/admin/fragment/AdminOverviewFragment.java
git commit -m "feat: admin overview fragment (cafeteria-wide stats)"
```

---

## Task 6: AdminCustomersFragment (search + balances)

**Files:**
- Create: `app/src/main/res/layout/fragment_admin_customers.xml`
- Create: `app/src/main/java/com/technogenis/cafeteriacashier/admin/fragment/AdminCustomersFragment.java`

- [ ] **Step 1: Create the layout**

`app/src/main/res/layout/fragment_admin_customers.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?android:attr/colorBackground">

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/searchLayout"
        style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="12dp"
        app:startIconDrawable="@android:drawable/ic_menu_search">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etSearch"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="@string/hint_search_customers"
            android:inputType="text"
            android:imeOptions="actionSearch"
            android:maxLines="1" />
    </com.google.android.material.textfield.TextInputLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_below="@id/searchLayout"
        android:clipToPadding="false"
        android:paddingBottom="16dp" />

    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true" />

    <TextView
        android:id="@+id/tvEmpty"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:text="@string/empty_customers"
        android:textColor="@color/emptyStateText"
        android:textSize="15sp"
        android:visibility="gone" />

</RelativeLayout>
```

- [ ] **Step 2: Create the fragment**

`app/src/main/java/com/technogenis/cafeteriacashier/admin/fragment/AdminCustomersFragment.java`:

```java
package com.technogenis.cafeteriacashier.admin.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.adapter.CustomerAdapter;
import com.technogenis.cafeteriacashier.model.CustomerModel;
import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.util.AdminStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminCustomersFragment extends Fragment {

    private static final int LOW_BALANCE_THRESHOLD = 100;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private EditText etSearch;

    private CustomerAdapter adapter;
    private final List<CustomerModel> allCustomers = new ArrayList<>();
    private final List<CustomerModel> shown = new ArrayList<>();
    private Map<String, Integer> balanceByRfid = new java.util.HashMap<>();

    private DatabaseReference customersRef, buyitemsRef;
    private ValueEventListener customersListener, buyitemsListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_customers, container, false);
        recyclerView = root.findViewById(R.id.recyclerView);
        progressBar = root.findViewById(R.id.progressBar);
        tvEmpty = root.findViewById(R.id.tvEmpty);
        etSearch = root.findViewById(R.id.etSearch);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CustomerAdapter(requireContext(), shown, balanceByRfid, LOW_BALANCE_THRESHOLD);
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                applyFilter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        buyitemsRef = FirebaseDatabase.getInstance().getReference("buyitems");
        buyitemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<HistoryModel> items = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    HistoryModel h = s.getValue(HistoryModel.class);
                    if (h != null) items.add(h);
                }
                balanceByRfid.clear();
                balanceByRfid.putAll(AdminStats.latestBalanceByRfid(items));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminCustomers", "buyitems cancelled", error.toException());
            }
        };
        buyitemsRef.addValueEventListener(buyitemsListener);

        customersRef = FirebaseDatabase.getInstance().getReference("customers");
        customersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allCustomers.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    CustomerModel c = s.getValue(CustomerModel.class);
                    if (c != null) allCustomers.add(c);
                }
                applyFilter(etSearch.getText().toString());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminCustomers", "customers cancelled", error.toException());
                progressBar.setVisibility(View.GONE);
            }
        };
        customersRef.addValueEventListener(customersListener);
    }

    private void applyFilter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        shown.clear();
        for (CustomerModel c : allCustomers) {
            String name = c.getCustomerName() == null ? "" : c.getCustomerName().toLowerCase(Locale.getDefault());
            String rfid = c.getCustomerRfid() == null ? "" : c.getCustomerRfid().toLowerCase(Locale.getDefault());
            if (q.isEmpty() || name.contains(q) || rfid.contains(q)) {
                shown.add(c);
            }
        }
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        if (customersRef != null && customersListener != null) {
            customersRef.removeEventListener(customersListener);
        }
        if (buyitemsRef != null && buyitemsListener != null) {
            buyitemsRef.removeEventListener(buyitemsListener);
        }
        customersRef = null; customersListener = null;
        buyitemsRef = null; buyitemsListener = null;
        super.onDestroyView();
    }
}
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/fragment_admin_customers.xml \
        app/src/main/java/com/technogenis/cafeteriacashier/admin/fragment/AdminCustomersFragment.java
git commit -m "feat: admin customers fragment with search + balances"
```

---

## Task 7: AdminReportsFragment (best-selling items)

**Files:**
- Create: `app/src/main/res/layout/fragment_admin_reports.xml`
- Create: `app/src/main/java/com/technogenis/cafeteriacashier/admin/fragment/AdminReportsFragment.java`

- [ ] **Step 1: Create the layout**

`app/src/main/res/layout/fragment_admin_reports.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="?android:attr/colorBackground">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp"
        android:gravity="center">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnSortRevenue"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginEnd="6dp"
            android:text="@string/sort_by_revenue" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnSortQty"
            style="@style/Widget.Material3.Button.TonalButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="6dp"
            android:text="@string/sort_by_qty" />
    </LinearLayout>

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:clipToPadding="false"
            android:paddingBottom="16dp" />

        <TextView
            android:id="@+id/tvEmpty"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="@string/empty_items_sold"
            android:textColor="@color/emptyStateText"
            android:textSize="15sp"
            android:visibility="gone" />
    </FrameLayout>

</LinearLayout>
```

- [ ] **Step 2: Create the fragment**

`app/src/main/java/com/technogenis/cafeteriacashier/admin/fragment/AdminReportsFragment.java`:

```java
package com.technogenis.cafeteriacashier.admin.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.adapter.ItemSalesAdapter;
import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.model.ItemSalesModel;
import com.technogenis.cafeteriacashier.util.AdminStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminReportsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private MaterialButton btnSortRevenue, btnSortQty;

    private ItemSalesAdapter adapter;
    private final List<ItemSalesModel> data = new ArrayList<>();
    private boolean sortByRevenue = true;

    private DatabaseReference buyitemsRef;
    private ValueEventListener buyitemsListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_reports, container, false);
        recyclerView = root.findViewById(R.id.recyclerView);
        tvEmpty = root.findViewById(R.id.tvEmpty);
        btnSortRevenue = root.findViewById(R.id.btnSortRevenue);
        btnSortQty = root.findViewById(R.id.btnSortQty);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ItemSalesAdapter(requireContext(), data);
        recyclerView.setAdapter(adapter);

        btnSortRevenue.setOnClickListener(v -> { sortByRevenue = true; sortAndRefresh(); });
        btnSortQty.setOnClickListener(v -> { sortByRevenue = false; sortAndRefresh(); });

        buyitemsRef = FirebaseDatabase.getInstance().getReference("buyitems");
        buyitemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<HistoryModel> items = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    HistoryModel h = s.getValue(HistoryModel.class);
                    if (h != null) items.add(h);
                }
                data.clear();
                data.addAll(AdminStats.itemSales(items));
                sortAndRefresh();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminReports", "buyitems cancelled", error.toException());
            }
        };
        buyitemsRef.addValueEventListener(buyitemsListener);
    }

    private void sortAndRefresh() {
        Collections.sort(data, new Comparator<ItemSalesModel>() {
            @Override
            public int compare(ItemSalesModel a, ItemSalesModel b) {
                return sortByRevenue
                        ? Integer.compare(b.getRevenue(), a.getRevenue())
                        : Integer.compare(b.getQtySold(), a.getQtySold());
            }
        });
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        if (buyitemsRef != null && buyitemsListener != null) {
            buyitemsRef.removeEventListener(buyitemsListener);
        }
        buyitemsRef = null; buyitemsListener = null;
        super.onDestroyView();
    }
}
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/fragment_admin_reports.xml \
        app/src/main/java/com/technogenis/cafeteriacashier/admin/fragment/AdminReportsFragment.java
git commit -m "feat: admin reports fragment (best-selling items)"
```

---

## Task 8: AdminDashboardActivity → drawer host

**Files:**
- Create: `app/src/main/res/menu/admin_menu.xml`
- Create: `app/src/main/res/layout/admin_drawer_header.xml`
- Modify (full rewrite): `app/src/main/res/layout/activity_admin_dashboard.xml`
- Modify (full rewrite): `app/src/main/java/com/technogenis/cafeteriacashier/admin/AdminDashboardActivity.java`

- [ ] **Step 1: Create the admin drawer menu**

`app/src/main/res/menu/admin_menu.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">

    <item android:id="@+id/menuAdminOverview"
        android:icon="@drawable/home"
        android:title="@string/nav_overview" />

    <item android:id="@+id/menuAdminCustomers"
        android:icon="@drawable/card"
        android:title="@string/nav_customers" />

    <item android:id="@+id/menuAdminReports"
        android:icon="@drawable/cash"
        android:title="@string/nav_reports" />

    <item android:id="@+id/menuAdminLogout"
        android:icon="@drawable/ic_logout"
        android:title="@string/action_logout" />

    <item android:id="@+id/menuAdminExit"
        android:icon="@drawable/ic_switch"
        android:title="@string/nav_exit" />

</menu>
```

- [ ] **Step 2: Create the admin drawer header**

`app/src/main/res/layout/admin_drawer_header.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="?attr/colorPrimary"
    android:orientation="vertical"
    android:padding="20dp">

    <ImageView
        android:layout_width="80dp"
        android:layout_height="80dp"
        app:srcCompat="@drawable/nav_profile"
        android:contentDescription="@string/cd_profile_avatar" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="@color/white"
        android:paddingTop="12dp"
        android:text="@string/admin_title" />

</LinearLayout>
```

- [ ] **Step 3: Rewrite the activity layout as a drawer host**

Replace the entire contents of `app/src/main/res/layout/activity_admin_dashboard.xml` with:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawerlayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?android:attr/colorBackground"
    tools:context=".admin.AdminDashboardActivity">

    <LinearLayout
        android:id="@+id/contentRoot"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <com.google.android.material.appbar.MaterialToolbar
            android:id="@+id/Toolbar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:minHeight="?attr/actionBarSize"
            android:background="?attr/colorPrimary"
            app:navigationIcon="@drawable/ic_menu"
            app:navigationIconTint="@color/white"
            app:title="@string/admin_title"
            app:titleTextColor="@color/white" />

        <FrameLayout
            android:id="@+id/main_frame"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1" />

    </LinearLayout>

    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navMenu"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        android:background="?attr/colorSurface"
        app:itemIconTint="?attr/colorPrimary"
        app:itemTextColor="?attr/colorOnSurface"
        app:headerLayout="@layout/admin_drawer_header"
        app:menu="@menu/admin_menu" />

</androidx.drawerlayout.widget.DrawerLayout>
```

- [ ] **Step 4: Rewrite the activity**

Replace the entire contents of
`app/src/main/java/com/technogenis/cafeteriacashier/admin/AdminDashboardActivity.java` with:

```java
package com.technogenis.cafeteriacashier.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.technogenis.cafeteriacashier.LoginActivity;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.admin.fragment.AdminCustomersFragment;
import com.technogenis.cafeteriacashier.admin.fragment.AdminOverviewFragment;
import com.technogenis.cafeteriacashier.admin.fragment.AdminReportsFragment;
import com.technogenis.cafeteriacashier.util.EdgeToEdgeHelper;

public class AdminDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navMenu;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        toolbar = findViewById(R.id.Toolbar);
        drawerLayout = findViewById(R.id.drawerlayout);
        navMenu = findViewById(R.id.navMenu);

        setSupportActionBar(toolbar);

        EdgeToEdgeHelper.applySystemBarsPadding(toolbar, true, false, false, true);
        EdgeToEdgeHelper.applySystemBarsPadding(findViewById(R.id.main_frame), false, true, true, true);
        EdgeToEdgeHelper.applySystemBarsPadding(navMenu, true, true, true, true);

        if (savedInstanceState == null) {
            swapFragment(new AdminOverviewFragment());
        }

        toolbar.setNavigationOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getOnBackPressedDispatcher().onBackPressed();
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this::syncToolbarIndicator);
        syncToolbarIndicator();

        navMenu.setNavigationItemSelectedListener(this::onDrawerItemSelected);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });
    }

    private void syncToolbarIndicator() {
        boolean atRoot = getSupportFragmentManager().getBackStackEntryCount() == 0;
        toolbar.setNavigationIcon(atRoot ? R.drawable.ic_menu : R.drawable.ic_arrow_back);
        toolbar.setNavigationContentDescription(
                atRoot ? R.string.cd_open_menu : R.string.cd_go_back);
    }

    private boolean onDrawerItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuAdminOverview) {
            getSupportFragmentManager()
                    .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (id == R.id.menuAdminCustomers) {
            swapSubFragment(new AdminCustomersFragment());
        } else if (id == R.id.menuAdminReports) {
            swapSubFragment(new AdminReportsFragment());
        } else if (id == R.id.menuAdminLogout) {
            performLogout();
        } else if (id == R.id.menuAdminExit) {
            finishAffinity();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void swapFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame, fragment)
                .commit();
    }

    private void swapSubFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void performLogout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
```

- [ ] **Step 5: Verify it compiles**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/menu/admin_menu.xml \
        app/src/main/res/layout/admin_drawer_header.xml \
        app/src/main/res/layout/activity_admin_dashboard.xml \
        app/src/main/java/com/technogenis/cafeteriacashier/admin/AdminDashboardActivity.java
git commit -m "feat: admin dashboard is now a drawer host (overview/customers/reports)"
```

---

## Task 9: AdminCheckListActivity — customer header + toolbar/back

**Files:**
- Modify (full rewrite): `app/src/main/res/layout/activity_admin_check_list.xml`
- Modify: `app/src/main/java/com/technogenis/cafeteriacashier/admin/AdminCheckListActivity.java`

- [ ] **Step 1: Rewrite the layout with a toolbar + customer header**

Replace the entire contents of `app/src/main/res/layout/activity_admin_check_list.xml` with:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="?android:attr/colorBackground"
    tools:context=".admin.AdminCheckListActivity">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/Toolbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:minHeight="?attr/actionBarSize"
        android:background="?attr/colorPrimary"
        app:navigationIcon="@drawable/ic_arrow_back"
        app:navigationIconTint="@color/white"
        app:title="@string/title_customer_detail"
        app:titleTextColor="@color/white" />

    <com.google.android.material.card.MaterialCardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="12dp"
        app:cardCornerRadius="16dp"
        app:cardElevation="4dp"
        app:cardBackgroundColor="?attr/colorPrimary">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="18dp">

            <TextView
                android:id="@+id/tvCustName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="@string/label_name"
                android:textColor="@color/white"
                android:textSize="20sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/tvCustBalance"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:text="@string/placeholder_amount_zero"
                android:textColor="@color/white"
                android:textSize="14sp" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="10dp"
                android:orientation="horizontal"
                android:weightSum="3">

                <TextView
                    android:id="@+id/tvCustSpent"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:textColor="@color/white"
                    android:textSize="12sp" />

                <TextView
                    android:id="@+id/tvCustOrders"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:textColor="@color/white"
                    android:textSize="12sp" />

                <TextView
                    android:id="@+id/tvCustSplit"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:textColor="@color/white"
                    android:textSize="12sp" />
            </LinearLayout>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <LinearLayout
        android:id="@+id/ll_head"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:weightSum="5"
        android:orientation="horizontal">

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="@string/label_qty"
            android:textColor="@color/white"
            android:textStyle="bold"
            android:textSize="13sp"
            android:textAllCaps="true"
            android:padding="12dp"
            android:layout_weight="1"
            android:gravity="center"
            android:background="@drawable/text_background_color" />

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="@string/label_name"
            android:textColor="@color/white"
            android:textStyle="bold"
            android:textSize="13sp"
            android:textAllCaps="true"
            android:padding="12dp"
            android:layout_weight="2"
            android:gravity="center"
            android:background="@drawable/text_background_color" />

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="@string/label_price"
            android:textColor="@color/white"
            android:textStyle="bold"
            android:textSize="13sp"
            android:textAllCaps="true"
            android:padding="12dp"
            android:layout_weight="1"
            android:gravity="center"
            android:background="@drawable/text_background_color" />

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="@string/label_date"
            android:textColor="@color/white"
            android:textStyle="bold"
            android:textSize="13sp"
            android:textAllCaps="true"
            android:padding="12dp"
            android:layout_weight="1"
            android:gravity="center"
            android:background="@drawable/text_background_color" />
    </LinearLayout>

    <RelativeLayout
        android:id="@+id/ll_list"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:padding="8dp">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:clipToPadding="false" />

        <ProgressBar
            android:id="@+id/progressBar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:visibility="visible" />

        <TextView
            android:id="@+id/tvEmpty"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:text="@string/empty_history"
            android:textColor="@color/emptyStateText"
            android:textSize="15sp"
            android:visibility="gone" />
    </RelativeLayout>

    <LinearLayout
        android:id="@+id/ll_total"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:weightSum="2">

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="@string/label_total"
            android:textColor="@color/white"
            android:textStyle="bold"
            android:textSize="14sp"
            android:textAllCaps="true"
            android:padding="16dp"
            android:layout_weight="1"
            android:gravity="center"
            android:background="@drawable/text_background_color" />

        <TextView
            android:id="@+id/tvTotal"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="@string/placeholder_amount_zero"
            android:textColor="@color/white"
            android:textStyle="bold"
            android:textSize="14sp"
            android:textAllCaps="true"
            android:padding="16dp"
            android:layout_weight="1"
            android:gravity="center"
            android:background="@drawable/text_background_color" />

    </LinearLayout>

</LinearLayout>
```

- [ ] **Step 2: Update the activity to set the toolbar + header**

In `app/src/main/java/com/technogenis/cafeteriacashier/admin/AdminCheckListActivity.java`,
add imports near the existing imports (after line 27):

```java
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.database.DatabaseReference;
import com.technogenis.cafeteriacashier.util.AdminStats;
import com.technogenis.cafeteriacashier.util.HomeFormat;
```

Add header fields next to the existing fields (after `private View root;` around line 39):

```java
    private TextView tvCustName, tvCustBalance, tvCustSpent, tvCustOrders, tvCustSplit;
    private DatabaseReference customerRef;
    private ValueEventListener customerListener;
```

In `initUI()`, after `root = findViewById(R.id.main);` (line 71), add toolbar + header binding:

```java
        Toolbar toolbar = findViewById(R.id.Toolbar);
        EdgeToEdgeHelper.applySystemBarsPadding(toolbar, true, false, false, true);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvCustName = findViewById(R.id.tvCustName);
        tvCustBalance = findViewById(R.id.tvCustBalance);
        tvCustSpent = findViewById(R.id.tvCustSpent);
        tvCustOrders = findViewById(R.id.tvCustOrders);
        tvCustSplit = findViewById(R.id.tvCustSplit);
```

Note: the existing `EdgeToEdgeHelper.applySystemBarsPadding(root, true, true);` line in `initUI()`
should be **removed** — the toolbar now handles the top inset and the bottom total bar handles
the bottom inset; padding the whole root would double-inset. Replace that single line with the
toolbar padding shown above (i.e. delete `EdgeToEdgeHelper.applySystemBarsPadding(root, true, true);`).

In `onStart()`, inside `onDataChange`, after the existing loop sets `tvTotal`, also compute and
set the header stats. Replace the body of `onDataChange` with:

```java
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int total = 0;
                mDataList.clear();
                List<HistoryModel> all = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HistoryModel h = snapshot.getValue(HistoryModel.class);
                    if (h == null) continue;
                    total += SafeParse.parseIntOr(h.getItemPrice(), 0);
                    mDataList.add(h);
                    all.add(h);
                }
                String pkr = getString(R.string.label_currency_pkr) + " ";
                tvTotal.setText(pkr + total);

                int[] split = AdminStats.cardCashSplit(all);
                java.util.Map<String, Integer> bal = AdminStats.latestBalanceByRfid(all);
                Integer balance = bal.get(rfid);
                tvCustBalance.setText(getString(R.string.label_balance) + ": " + pkr
                        + HomeFormat.groupedAmount(balance != null ? balance : 0));
                tvCustSpent.setText(getString(R.string.stat_total_spent) + ": " + pkr
                        + HomeFormat.groupedAmount(total));
                tvCustOrders.setText(getString(R.string.stat_orders) + ": " + all.size());
                tvCustSplit.setText(getString(R.string.stat_card_cash) + ": "
                        + split[0] + "/" + split[1]);

                mHistoryAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(mDataList.isEmpty() ? View.VISIBLE : View.GONE);
            }
```

Add the customer-name read at the end of `onStart()` (after `query.addValueEventListener(listener);`):

```java
        customerRef = FirebaseDatabase.getInstance().getReference("customers").child(rfid);
        customerListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("customername").getValue(String.class);
                tvCustName.setText(name != null && !name.isEmpty() ? name : rfid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvCustName.setText(rfid);
            }
        };
        customerRef.addValueEventListener(customerListener);
```

In `onStop()`, detach the customer listener too — replace the method body with:

```java
        if (query != null && listener != null) {
            query.removeEventListener(listener);
        }
        if (customerRef != null && customerListener != null) {
            customerRef.removeEventListener(customerListener);
        }
        listener = null;
        customerListener = null;
        super.onStop();
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/activity_admin_check_list.xml \
        app/src/main/java/com/technogenis/cafeteriacashier/admin/AdminCheckListActivity.java
git commit -m "feat: customer detail gets header + toolbar back button"
```

---

## Task 10: Full build, tests, and manual verification

- [ ] **Step 1: Run unit tests + full build**

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```
Expected: both BUILD SUCCESSFUL; `AdminStatsTest` and `HomeFormatTest` pass.

- [ ] **Step 2: Install and verify on the emulator**

```bash
./gradlew installDebug
"$ADB" shell am force-stop com.technogenis.cafeteriacashier
"$ADB" shell monkey -p com.technogenis.cafeteriacashier -c android.intent.category.LAUNCHER 1
```

Then, logging in as **admin / admin**, check:
- **Overview** is the landing screen: Total Customers = 2; Revenue = sum of all buyitems
  prices; Transactions = total buyitems count; Card/Cash split correct; Today's Sales = only
  today's `Id`s (likely PKR 0 for old test data).
- **Drawer → Customers**: search bar filters by name/RFID; each row shows balance; a customer
  below PKR 100 shows the low-balance (red) balance; tapping a row opens their detail.
- **Customer detail**: header shows name + balance + total spent + orders + Card/Cash; the
  toolbar back arrow (←) returns to the customer list.
- **Drawer → Reports**: items grouped with Sold + Revenue; default sorted by revenue; tapping
  "By Qty" re-sorts.
- **Drawer nav**: back arrow appears on Customers/Reports and returns to Overview; the drawer
  hamburger shows on Overview.
- **Logout**: returns to the Login screen.

- [ ] **Step 3: Final commit (only if cleanup was needed)**

```bash
git add -A
git commit -m "chore: admin dashboard revamp verified"
```

---

## Notes / out of scope

- RTDB rules should declare `"buyitems": { ".indexOn": "customerrfid" }` (already noted for
  the per-customer query). The new fragments read whole nodes (`customers`, `buyitems`)
  without `orderByChild`, so no extra index is required.
- All admin reads pull whole nodes client-side — fine at FYP scale; server-side aggregation
  would be needed only for large datasets (out of scope).
```


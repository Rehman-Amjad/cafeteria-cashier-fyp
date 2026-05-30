# Premium Home Screen + Back Navigation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the customer Home screen into a premium dashboard (greeting + balance hero, low-balance banner, all-time spending stats, recent-3 preview) and add working back navigation from the history sub-screens to Home.

**Architecture:** `HomeFragment` stays a single `ScrollView` of static Material cards; all numbers are computed inside the existing `buyitems` Firebase listener, plus one extra read of `customers/{rfid}` for the display name. The recent-3 preview reuses the existing `HistoryAdapter` (which already renders dates). `DashboardActivity` gains a fragment back stack so device-back and a toolbar up-arrow return to Home.

**Tech Stack:** Java, Android (Material3), Firebase Realtime Database, JUnit (JVM unit tests). No new libraries.

---

## Build / test environment

The machine's default JDK is **25**, which Gradle 8.7 / AGP 8.5.2 reject. All Gradle commands in this plan MUST set `JAVA_HOME` to the JDK 17 already installed:

```bash
export JAVA_HOME=/Users/rehmanamjad/Library/Java/JavaVirtualMachines/jbr-17.0.14/Contents/Home
```

Run that once per shell, or prefix each `./gradlew` command with it.

## File structure

- Create `app/src/main/java/com/technogenis/cafeteriacashier/util/HomeFormat.java` — pure, testable formatting (currency grouping + greeting-by-hour). One responsibility: presentation helpers.
- Create `app/src/test/java/com/technogenis/cafeteriacashier/HomeFormatTest.java` — JVM unit tests for the helper.
- Modify `app/src/main/res/values/strings.xml` — greeting + stat-label + low-balance strings.
- Modify `app/src/main/res/values/colors.xml` — one banner background token.
- Rewrite `app/src/main/res/layout/fragment_home.xml` — hero, banner, stats row, recent list, existing View-All button.
- Rewrite `app/src/main/java/.../fragment/HomeFragment.java` — name read, greeting, balance, stats, recent-3, low-balance.
- Modify `app/src/main/java/.../DashboardActivity.java` — back-stack navigation + toolbar indicator toggle.

`HistoryAdapter` and `list_item.xml` are **unchanged** — the adapter already formats the `Id` timestamp into a date column.

---

## Task 1: HomeFormat helper (TDD)

**Files:**
- Create: `app/src/main/java/com/technogenis/cafeteriacashier/util/HomeFormat.java`
- Test: `app/src/test/java/com/technogenis/cafeteriacashier/HomeFormatTest.java`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/technogenis/cafeteriacashier/HomeFormatTest.java`:

```java
package com.technogenis.cafeteriacashier;

import static org.junit.Assert.assertEquals;

import com.technogenis.cafeteriacashier.util.HomeFormat;

import org.junit.Test;

public class HomeFormatTest {

    @Test
    public void groupedAmount_addsThousandsSeparator() {
        assertEquals("0", HomeFormat.groupedAmount(0));
        assertEquals("60", HomeFormat.groupedAmount(60));
        assertEquals("1,250", HomeFormat.groupedAmount(1250));
        assertEquals("4,675", HomeFormat.groupedAmount(4675));
    }

    @Test
    public void greetingIndex_byHourOfDay() {
        assertEquals(0, HomeFormat.greetingIndex(0));   // morning
        assertEquals(0, HomeFormat.greetingIndex(8));   // morning
        assertEquals(1, HomeFormat.greetingIndex(12));  // afternoon
        assertEquals(1, HomeFormat.greetingIndex(16));  // afternoon
        assertEquals(2, HomeFormat.greetingIndex(17));  // evening
        assertEquals(2, HomeFormat.greetingIndex(23));  // evening
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.technogenis.cafeteriacashier.HomeFormatTest"`
Expected: FAIL — compilation error, `HomeFormat` does not exist.

- [ ] **Step 3: Write the minimal implementation**

Create `app/src/main/java/com/technogenis/cafeteriacashier/util/HomeFormat.java`:

```java
package com.technogenis.cafeteriacashier.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Pure presentation helpers for the Home dashboard. No Android dependencies. */
public final class HomeFormat {

    private HomeFormat() {}

    /** Formats an amount with thousands separators, e.g. 4675 -> "4,675". */
    public static String groupedAmount(int amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }

    /**
     * Maps a 24-hour hour-of-day to a greeting bucket:
     * 0 = morning (&lt;12), 1 = afternoon (&lt;17), 2 = evening.
     */
    public static int greetingIndex(int hourOfDay) {
        if (hourOfDay < 12) return 0;
        if (hourOfDay < 17) return 1;
        return 2;
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.technogenis.cafeteriacashier.HomeFormatTest"`
Expected: PASS (BUILD SUCCESSFUL).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/technogenis/cafeteriacashier/util/HomeFormat.java \
        app/src/test/java/com/technogenis/cafeteriacashier/HomeFormatTest.java
git commit -m "feat: add HomeFormat helper (currency grouping + greeting bucket)"
```

---

## Task 2: Strings and color token

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values/colors.xml`

- [ ] **Step 1: Add strings**

In `app/src/main/res/values/strings.xml`, inside the `<!-- Home / lists -->` group (after the `placeholder_amount_zero` line), add:

```xml
    <!-- Home dashboard -->
    <string name="greeting_morning">Good morning</string>
    <string name="greeting_afternoon">Good afternoon</string>
    <string name="greeting_evening">Good evening</string>
    <string name="label_current_balance">Current Balance</string>
    <string name="label_recent_purchases">Recent Purchases</string>
    <string name="stat_total_spent">Total Spent</string>
    <string name="stat_orders">Orders</string>
    <string name="stat_card_cash">Card / Cash</string>
    <string name="low_balance_warning">⚠ Low balance — please top up soon.</string>
```

- [ ] **Step 2: Add the banner color token**

In `app/src/main/res/values/colors.xml`, inside `<resources>` (next to the other semantic tokens), add:

```xml
    <color name="lowBalanceBg">#FDECEA</color>
    <color name="lowBalanceText">#B3261E</color>
```

- [ ] **Step 3: Verify resources compile**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL (resources merge with no errors).

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/values/strings.xml app/src/main/res/values/colors.xml
git commit -m "feat: add home dashboard strings and low-balance colors"
```

---

## Task 3: Redesign fragment_home.xml

**Files:**
- Modify (full rewrite): `app/src/main/res/layout/fragment_home.xml`

- [ ] **Step 1: Replace the layout file**

Replace the entire contents of `app/src/main/res/layout/fragment_home.xml` with:

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?android:attr/colorBackground"
    android:fillViewport="true"
    android:clipToPadding="false"
    tools:context=".fragment.HomeFragment">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <!-- Hero card: greeting + name/rfid + balance -->
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

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal">

                    <TextView
                        android:id="@+id/tvGreeting"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_weight="1"
                        android:text="@string/greeting_evening"
                        android:textColor="@color/white"
                        android:textSize="14sp" />

                    <TextView
                        android:id="@+id/tvRfid"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:textColor="@color/white"
                        android:textSize="12sp"
                        android:alpha="0.85" />
                </LinearLayout>

                <TextView
                    android:id="@+id/tvName"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="2dp"
                    android:text="@string/label_name"
                    android:textColor="@color/white"
                    android:textSize="22sp"
                    android:textStyle="bold" />

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="18dp"
                    android:text="@string/label_current_balance"
                    android:textColor="@color/white"
                    android:alpha="0.85"
                    android:textSize="13sp" />

                <TextView
                    android:id="@+id/tvBalance"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="@string/placeholder_amount_zero"
                    android:textColor="@color/white"
                    android:textSize="30sp"
                    android:textStyle="bold" />
            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Low-balance banner (hidden unless balance < threshold) -->
        <com.google.android.material.card.MaterialCardView
            android:id="@+id/cardLowBalance"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:visibility="gone"
            app:cardCornerRadius="12dp"
            app:cardElevation="0dp"
            app:cardBackgroundColor="@color/lowBalanceBg">

            <TextView
                android:id="@+id/tvLowBalance"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:padding="14dp"
                android:text="@string/low_balance_warning"
                android:textColor="@color/lowBalanceText"
                android:textSize="13sp"
                android:textStyle="bold" />
        </com.google.android.material.card.MaterialCardView>

        <!-- Stats row: total spent / orders / card-cash -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:orientation="horizontal"
            android:baselineAligned="false"
            android:weightSum="3">

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
                    android:padding="14dp">

                    <TextView
                        android:id="@+id/tvSpentValue"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/placeholder_amount_zero"
                        android:textColor="?attr/colorPrimary"
                        android:textSize="16sp"
                        android:textStyle="bold" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:text="@string/stat_total_spent"
                        android:textColor="@color/onSurfaceSecondary"
                        android:textSize="11sp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <com.google.android.material.card.MaterialCardView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginHorizontal="3dp"
                app:cardCornerRadius="14dp"
                app:cardElevation="2dp"
                app:cardBackgroundColor="?attr/colorSurface">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:gravity="center"
                    android:padding="14dp">

                    <TextView
                        android:id="@+id/tvOrdersValue"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0"
                        android:textColor="?attr/colorPrimary"
                        android:textSize="16sp"
                        android:textStyle="bold" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:text="@string/stat_orders"
                        android:textColor="@color/onSurfaceSecondary"
                        android:textSize="11sp" />
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
                    android:padding="14dp">

                    <TextView
                        android:id="@+id/tvSplitValue"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="0 / 0"
                        android:textColor="?attr/colorPrimary"
                        android:textSize="16sp"
                        android:textStyle="bold" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:text="@string/stat_card_cash"
                        android:textColor="@color/onSurfaceSecondary"
                        android:textSize="11sp" />
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
        </LinearLayout>

        <!-- Recent purchases -->
        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="20dp"
            android:layout_marginBottom="6dp"
            android:text="@string/label_recent_purchases"
            android:textColor="?attr/colorOnSurface"
            android:textSize="16sp"
            android:textStyle="bold" />

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rvRecent"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:nestedScrollingEnabled="false"
            android:overScrollMode="never" />

        <TextView
            android:id="@+id/tvEmptyRecent"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="16dp"
            android:gravity="center"
            android:text="@string/empty_history"
            android:textColor="@color/emptyStateText"
            android:visibility="gone" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnViewAllPurchases"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="@string/action_view_all_purchases" />

    </LinearLayout>
</ScrollView>
```

- [ ] **Step 2: Verify the layout compiles**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL. (The build will still pass even though `HomeFragment` doesn't yet reference the new IDs — it's rewritten in Task 4.)

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/layout/fragment_home.xml
git commit -m "feat: premium home layout (hero, banner, stats, recent list)"
```

---

## Task 4: Rewrite HomeFragment logic

**Files:**
- Modify (full rewrite): `app/src/main/java/com/technogenis/cafeteriacashier/fragment/HomeFragment.java`

- [ ] **Step 1: Replace the fragment**

Replace the entire contents of `app/src/main/java/com/technogenis/cafeteriacashier/fragment/HomeFragment.java` with:

```java
package com.technogenis.cafeteriacashier.fragment;

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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.technogenis.cafeteriacashier.DashboardActivity;
import com.technogenis.cafeteriacashier.MyPreferenceManager;
import com.technogenis.cafeteriacashier.R;
import com.technogenis.cafeteriacashier.adapter.HistoryAdapter;
import com.technogenis.cafeteriacashier.model.HistoryModel;
import com.technogenis.cafeteriacashier.util.HomeFormat;
import com.technogenis.cafeteriacashier.util.SafeParse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int LOW_BALANCE_THRESHOLD = 100;
    private static final int RECENT_LIMIT = 3;
    private static final String PAYMENT_CASH = "Cash";

    private TextView tvGreeting, tvName, tvRfid, tvBalance;
    private TextView tvSpentValue, tvOrdersValue, tvSplitValue;
    private TextView tvEmptyRecent;
    private MaterialCardView cardLowBalance;
    private MaterialButton btnViewAll;
    private RecyclerView rvRecent;
    private View root;

    private final List<HistoryModel> recentList = new ArrayList<>();
    private HistoryAdapter recentAdapter;

    private String rfid;
    private Query buyitemsQuery;
    private ValueEventListener buyitemsListener;
    private DatabaseReference customerRef;
    private ValueEventListener customerListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        tvGreeting = root.findViewById(R.id.tvGreeting);
        tvName = root.findViewById(R.id.tvName);
        tvRfid = root.findViewById(R.id.tvRfid);
        tvBalance = root.findViewById(R.id.tvBalance);
        tvSpentValue = root.findViewById(R.id.tvSpentValue);
        tvOrdersValue = root.findViewById(R.id.tvOrdersValue);
        tvSplitValue = root.findViewById(R.id.tvSplitValue);
        tvEmptyRecent = root.findViewById(R.id.tvEmptyRecent);
        cardLowBalance = root.findViewById(R.id.cardLowBalance);
        btnViewAll = root.findViewById(R.id.btnViewAllPurchases);
        rvRecent = root.findViewById(R.id.rvRecent);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecent.setNestedScrollingEnabled(false);
        recentAdapter = new HistoryAdapter(requireContext(), recentList);
        rvRecent.setAdapter(recentAdapter);

        btnViewAll.setOnClickListener(v -> {
            if (requireActivity() instanceof DashboardActivity) {
                ((DashboardActivity) requireActivity()).showPurchaseHistory();
            }
        });

        applyGreeting();

        rfid = MyPreferenceManager.getInstance(requireContext()).getString("rfid");
        if (rfid == null || rfid.isEmpty()) {
            // No session — outer activity will route to login on next resume.
            return;
        }
        tvRfid.setText(getString(R.string.label_rfid) + ": " + rfid);

        attachCustomerListener();
        attachBuyitemsListener();
    }

    private void applyGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int[] greetings = {
                R.string.greeting_morning,
                R.string.greeting_afternoon,
                R.string.greeting_evening
        };
        tvGreeting.setText(getString(greetings[HomeFormat.greetingIndex(hour)]));
    }

    private void attachCustomerListener() {
        customerRef = FirebaseDatabase.getInstance().getReference("customers").child(rfid);
        customerListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("customername").getValue(String.class);
                tvName.setText(name != null && !name.isEmpty() ? name : rfid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Customer read cancelled", error.toException());
                tvName.setText(rfid);
            }
        };
        customerRef.addValueEventListener(customerListener);
    }

    private void attachBuyitemsListener() {
        buyitemsQuery = FirebaseDatabase.getInstance()
                .getReference("buyitems")
                .orderByChild("customerrfid")
                .equalTo(rfid);

        buyitemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalSpent = 0;
                int orders = 0;
                int cardCount = 0;
                int cashCount = 0;
                long latestId = -1L;
                String latestBalance = null;

                List<HistoryModel> all = new ArrayList<>();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    HistoryModel h = snap.getValue(HistoryModel.class);
                    if (h == null) continue;
                    orders++;
                    totalSpent += SafeParse.parseIntOr(h.getItemPrice(), 0);
                    if (PAYMENT_CASH.equalsIgnoreCase(h.getCustomerPayment())) {
                        cashCount++;
                    } else {
                        cardCount++;
                    }
                    long id = SafeParse.parseLongOr(h.getId(), 0L);
                    if (id > latestId) {
                        latestId = id;
                        latestBalance = h.getCustomerBalance();
                    }
                    all.add(h);
                }

                // Balance hero
                int balance = SafeParse.parseIntOr(latestBalance, 0);
                tvBalance.setText(getString(R.string.label_currency_pkr) + " "
                        + HomeFormat.groupedAmount(balance));

                // Low-balance banner
                cardLowBalance.setVisibility(
                        balance < LOW_BALANCE_THRESHOLD ? View.VISIBLE : View.GONE);

                // Stats
                tvSpentValue.setText(getString(R.string.label_currency_pkr) + " "
                        + HomeFormat.groupedAmount(totalSpent));
                tvOrdersValue.setText(String.valueOf(orders));
                tvSplitValue.setText(cardCount + " / " + cashCount);

                // Recent 3 (newest first)
                Collections.sort(all, new Comparator<HistoryModel>() {
                    @Override
                    public int compare(HistoryModel a, HistoryModel b) {
                        long ia = SafeParse.parseLongOr(a.getId(), 0L);
                        long ib = SafeParse.parseLongOr(b.getId(), 0L);
                        return Long.compare(ib, ia);
                    }
                });
                recentList.clear();
                for (int i = 0; i < all.size() && i < RECENT_LIMIT; i++) {
                    recentList.add(all.get(i));
                }
                recentAdapter.notifyDataSetChanged();
                tvEmptyRecent.setVisibility(recentList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Buyitems query cancelled", error.toException());
                if (root != null) {
                    Snackbar.make(root, error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        };
        buyitemsQuery.addValueEventListener(buyitemsListener);
    }

    @Override
    public void onDestroyView() {
        if (buyitemsQuery != null && buyitemsListener != null) {
            buyitemsQuery.removeEventListener(buyitemsListener);
        }
        if (customerRef != null && customerListener != null) {
            customerRef.removeEventListener(customerListener);
        }
        buyitemsQuery = null;
        buyitemsListener = null;
        customerRef = null;
        customerListener = null;
        super.onDestroyView();
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/technogenis/cafeteriacashier/fragment/HomeFragment.java
git commit -m "feat: home dashboard logic (balance, stats, recent-3, low-balance)"
```

---

## Task 5: Back navigation in DashboardActivity

**Files:**
- Modify: `app/src/main/java/com/technogenis/cafeteriacashier/DashboardActivity.java`

Goal: drawer sub-screens (Item Purchase, Cash, and the Home "View All" button) push onto the fragment back stack so a toolbar up-arrow and device-back return to Home; Home keeps the drawer ☰.

- [ ] **Step 1: Keep a reference to the drawer toggle**

In `DashboardActivity`, add a field next to the existing fields (after `private Toolbar toolbar;`):

```java
    private ActionBarDrawerToggle toggle;
```

Then change the local toggle creation in `onCreate` from:

```java
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
```

to (assign to the field, no `ActionBarDrawerToggle` type prefix):

```java
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
```

- [ ] **Step 2: Sync the toolbar icon with the back stack**

Still in `onCreate`, immediately after `toggle.syncState();`, add:

```java
        getSupportFragmentManager().addOnBackStackChangedListener(this::syncToolbarIndicator);
        toggle.setToolbarNavigationClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
```

Then add this method to the class (e.g. just below `applyDrawerHeader()`):

```java
    private void syncToolbarIndicator() {
        boolean atRoot = getSupportFragmentManager().getBackStackEntryCount() == 0;
        toggle.setDrawerIndicatorEnabled(atRoot);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!atRoot);
        }
    }
```

- [ ] **Step 3: Route sub-screens through the back stack**

Replace the existing `onDrawerItemSelected` body so Home pops to root and sub-screens are added to the back stack:

```java
    private boolean onDrawerItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuHome) {
            getSupportFragmentManager()
                    .popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (id == R.id.menuItemPurchase) {
            swapSubFragment(new ItemsPurchaseHistory());
        } else if (id == R.id.menuItemPurchaseCash) {
            swapSubFragment(new ItemPurchaseCash());
        } else if (id == R.id.menu_logout) {
            confirmLogout();
        } else if (id == R.id.menuExit) {
            finishAffinity();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
```

- [ ] **Step 4: Update showPurchaseHistory and add swapSubFragment**

Replace the existing `showPurchaseHistory()` method and the `swapFragment(...)` method block with:

```java
    /** Opens the full purchase-history list (back-stack aware) and syncs the drawer item. */
    public void showPurchaseHistory() {
        swapSubFragment(new ItemsPurchaseHistory());
        navMenu.setCheckedItem(R.id.menuItemPurchase);
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
```

- [ ] **Step 5: Make device-back pop the stack before exiting**

Replace the existing `handleOnBackPressed()` body inside the `OnBackPressedCallback` with:

```java
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
```

- [ ] **Step 6: Add the import for ActionBarDrawerToggle**

Confirm `import androidx.appcompat.app.ActionBarDrawerToggle;` is present at the top of the file (it already is, since the toggle was used locally). No change if present.

- [ ] **Step 7: Verify it compiles**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/technogenis/cafeteriacashier/DashboardActivity.java
git commit -m "feat: back navigation from history sub-screens to Home"
```

---

## Task 6: Full build + manual verification

- [ ] **Step 1: Run unit tests + full build**

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```
Expected: both BUILD SUCCESSFUL; `HomeFormatTest` passes.

- [ ] **Step 2: Install and verify on a device/emulator**

```bash
./gradlew installDebug
```

Then check manually:
- Log in as RFID `0011036808` / PIN `2233` (5 transactions, latest balance 4675).
  - Hero shows a time-based greeting, name "Urwa", RFID, and "PKR 4,675".
  - No low-balance banner.
  - Stats: Total Spent = sum of the 5 line prices; Orders = 5; Card/Cash counts correct.
  - Recent shows the newest 3 with dates; "View All Purchases" opens the full list.
- Log in as RFID `0006347025` (latest balance 30) → low-balance banner is visible.
- From "View All Purchases": toolbar shows a back arrow; tapping it returns to Home;
  device-back also returns to Home (does NOT exit the app). On Home the drawer ☰ is back.

- [ ] **Step 3: Final commit (if any cleanup was needed)**

```bash
git add -A
git commit -m "chore: premium home + back nav verified"
```

---

## Notes / out of scope

- Firebase console rules should declare `"buyitems": { ".indexOn": "customerrfid" }` to
  avoid a client-side-filtering perf warning. Correctness is unaffected; not part of this
  plan.
- No instrumented UI tests are added — this project has no fragment test harness; the
  build + manual checklist is the verification gate, matching the existing test setup
  (only JVM unit tests exist).

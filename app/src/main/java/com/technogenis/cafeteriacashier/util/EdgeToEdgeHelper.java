package com.technogenis.cafeteriacashier.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class EdgeToEdgeHelper {

    private EdgeToEdgeHelper() {}

    public static void applySystemBarsPadding(final View root,
                                              final boolean padTop,
                                              final boolean padBottom) {
        applySystemBarsPadding(root, padTop, true, padBottom, true);
    }

    public static void applySystemBarsPadding(final View root,
                                              final boolean padTop,
                                              final boolean padHorizontal,
                                              final boolean padBottom,
                                              final boolean addToExisting) {
        if (root == null) return;

        final int basePaddingLeft   = root.getPaddingLeft();
        final int basePaddingTop    = root.getPaddingTop();
        final int basePaddingRight  = root.getPaddingRight();
        final int basePaddingBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());

            int left   = addToExisting ? basePaddingLeft   : 0;
            int top    = addToExisting ? basePaddingTop    : 0;
            int right  = addToExisting ? basePaddingRight  : 0;
            int bottom = addToExisting ? basePaddingBottom : 0;

            if (padHorizontal) {
                left += bars.left;
                right += bars.right;
            }
            if (padTop)    top += bars.top;
            if (padBottom) bottom += bars.bottom;

            v.setPadding(left, top, right, bottom);
            return insets;
        });

        if (ViewCompat.isAttachedToWindow(root)) {
            ViewCompat.requestApplyInsets(root);
        }
    }
}

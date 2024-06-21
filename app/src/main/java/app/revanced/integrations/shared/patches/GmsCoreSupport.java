package app.revanced.integrations.shared.patches;

import static app.revanced.integrations.shared.utils.StringRef.str;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;

import java.net.MalformedURLException;
import java.net.URL;

import app.revanced.integrations.shared.utils.Logger;
import app.revanced.integrations.shared.utils.Utils;

/**
 * @noinspection unused
 */
public class GmsCoreSupport {
    private static final String GMS_CORE_PACKAGE_NAME
            = getGmsCoreVendorGroupId() + ".android.gms";
    private static final Uri GMS_CORE_PROVIDER
            = Uri.parse("content://" + getGmsCoreVendorGroupId() + ".android.gsf.gservices/prefix");
    private static final String DONT_KILL_MY_APP_LINK
            = "https://dontkillmyapp.com";

    private static void open(Activity mActivity, String queryOrLink) {
        Intent intent;
        try {
            // Check if queryOrLink is a valid URL.
            new URL(queryOrLink);

            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(queryOrLink));
        } catch (MalformedURLException e) {
            intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, queryOrLink);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intent);

        // Gracefully exit, otherwise the broken app will continue to run.
        System.exit(0);
    }

    private static void showBatteryOptimizationDialog(Activity context,
                                                      String dialogMessageRef,
                                                      String positiveButtonStringRef,
                                                      DialogInterface.OnClickListener onPositiveClickListener) {
        // Use a delay to allow the activity to finish initializing.
        // Otherwise, if device is in dark mode the dialog is shown with wrong color scheme.
        Utils.runOnMainThreadDelayed(() -> new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(str("gms_core_dialog_title"))
                .setMessage(str(dialogMessageRef))
                .setPositiveButton(str(positiveButtonStringRef), onPositiveClickListener)
                // Allow using back button to skip the action, just in case the check can never be satisfied.
                .setCancelable(true)
                .show(), 100);
    }

    /**
     * Injection point.
     */
    public static void checkGmsCore(Activity mActivity) {
        try {
            // Verify GmsCore is installed.
            try {
                PackageManager manager = mActivity.getPackageManager();
                manager.getPackageInfo(GMS_CORE_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            } catch (PackageManager.NameNotFoundException exception) {
                Logger.printInfo(() -> "GmsCore was not found");
                // Cannot show a dialog and must show a toast,
                // because on some installations the app crashes before a dialog can be displayed.
                Utils.showToastLong(str("gms_core_toast_not_installed_message"));
                open(mActivity, getGmsCoreDownload());
                return;
            }

            if (contentProviderClientUnAvailable(mActivity)) {
                Logger.printInfo(() -> "GmsCore is not running in the background");

                showBatteryOptimizationDialog(mActivity,
                        "gms_core_dialog_not_whitelisted_not_allowed_in_background_message",
                        "gms_core_dialog_open_website_text",
                        (dialog, id) -> open(mActivity, DONT_KILL_MY_APP_LINK));
                return;
            }

            // Check if GmsCore is whitelisted from battery optimizations.
            if (batteryOptimizationsEnabled(mActivity)) {
                Logger.printInfo(() -> "GmsCore is not whitelisted from battery optimizations");
                showBatteryOptimizationDialog(mActivity,
                        "gms_core_dialog_not_whitelisted_using_battery_optimizations_message",
                        "gms_core_dialog_continue_text",
                        (dialog, id) -> openGmsCoreDisableBatteryOptimizationsIntent(mActivity));
            }
        } catch (Exception ex) {
            Logger.printException(() -> "checkGmsCore failure", ex);
        }
    }

    /**
     * @return If GmsCore is not running in the background.
     */
    public static boolean contentProviderClientUnAvailable(Context context) {
        // Check if GmsCore is running in the background.
        // Do this check before the battery optimization check.
        try (ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(GMS_CORE_PROVIDER)) {
            return client == null;
        }
    }

    @SuppressLint("BatteryLife") // Permission is part of GmsCore
    private static void openGmsCoreDisableBatteryOptimizationsIntent(Activity mActivity) {
        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.fromParts("package", GMS_CORE_PACKAGE_NAME, null));
        mActivity.startActivityForResult(intent, 0);
    }

    /**
     * @return If GmsCore is not whitelisted from battery optimizations.
     */
    public static boolean batteryOptimizationsEnabled(Context context) {
        if (context.getSystemService(Context.POWER_SERVICE) instanceof PowerManager powerManager) {
            return !powerManager.isIgnoringBatteryOptimizations(GMS_CORE_PACKAGE_NAME);
        }
        return false;
    }

    private static String getGmsCoreDownload() {
        final String vendorGroupId = getGmsCoreVendorGroupId();
        //noinspection SwitchStatementWithTooFewBranches
        return switch (vendorGroupId) {
            case "app.revanced" -> "https://github.com/revanced/gmscore/releases/latest";
            default -> vendorGroupId + ".android.gms";
        };
    }

    // Modified by a patch. Do not touch.
    private static String getGmsCoreVendorGroupId() {
        return "app.revanced";
    }
}
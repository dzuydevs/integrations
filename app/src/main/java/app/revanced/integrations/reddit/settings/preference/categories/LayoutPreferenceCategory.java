package app.revanced.integrations.reddit.settings.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;

import app.revanced.integrations.reddit.settings.Settings;
import app.revanced.integrations.reddit.settings.SettingsStatus;
import app.revanced.integrations.reddit.settings.preference.TogglePreference;

@SuppressWarnings("deprecation")
public class LayoutPreferenceCategory extends ConditionalPreferenceCategory {
    public LayoutPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context, screen);
        setTitle("Layout");
    }

    @Override
    public boolean getSettingsStatus() {
        return SettingsStatus.layoutCategoryEnabled();
    }

    @Override
    public void addPreferences(Context context) {
        if (SettingsStatus.screenshotPopupEnabled) {
            addPreference(new TogglePreference(
                    context,
                    "Disable screenshot popup",
                    "Disables the popup that appears when taking a screenshot.",
                    Settings.DISABLE_SCREENSHOT_POPUP
            ));
        }
        if (SettingsStatus.navigationButtonsEnabled) {
            addPreference(new TogglePreference(
                    context,
                    "Hide Chat button",
                    "Hides the Chat button in the navigation bar.",
                    Settings.HIDE_CHAT_BUTTON
            ));
            addPreference(new TogglePreference(
                    context,
                    "Hide Create button",
                    "Hides the Create button in the navigation bar.",
                    Settings.HIDE_CREATE_BUTTON
            ));
            addPreference(new TogglePreference(
                    context,
                    "Hide Discover or Communities button",
                    "Hides the Discover or Communities button in the navigation bar.",
                    Settings.HIDE_DISCOVER_BUTTON
            ));
        }
        if (SettingsStatus.recentlyVisitedShelfEnabled) {
            addPreference(new TogglePreference(
                    context,
                    "Hide Recently Visited shelf",
                    "Hides the Recently Visited shelf in the sidebar.",
                    Settings.HIDE_RECENTLY_VISITED_SHELF
            ));
        }
        if (SettingsStatus.toolBarButtonEnabled) {
            addPreference(new TogglePreference(
                    context,
                    "Hide toolbar button",
                    "Hide toolbar button",
                    Settings.HIDE_TOOLBAR_BUTTON
            ));
        }
        if (SettingsStatus.subRedditDialogEnabled) {
            addPreference(new TogglePreference(
                    context,
                    "Remove NSFW warning dialog",
                    "Removes the NSFW warning dialog that appears when visiting a subreddit by accepting it automatically.",
                    Settings.REMOVE_NSFW_DIALOG
            ));
            addPreference(new TogglePreference(
                    context,
                    "Remove notification suggestion dialog",
                    "Removes the notifications suggestion dialog that appears when visiting a subreddit by dismissing it automatically.",
                    Settings.REMOVE_NOTIFICATION_DIALOG
            ));
        }
    }
}

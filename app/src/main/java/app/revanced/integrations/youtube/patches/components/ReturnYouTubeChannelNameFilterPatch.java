package app.revanced.integrations.youtube.patches.components;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.patches.components.ByteArrayFilterGroup;
import app.revanced.integrations.shared.patches.components.ByteArrayFilterGroupList;
import app.revanced.integrations.shared.patches.components.Filter;
import app.revanced.integrations.shared.patches.components.StringFilterGroup;
import app.revanced.integrations.shared.utils.Logger;
import app.revanced.integrations.youtube.patches.utils.ReturnYouTubeChannelNamePatch;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class ReturnYouTubeChannelNameFilterPatch extends Filter {
    private final ByteArrayFilterGroupList shortsChannelBarAvatarFilterGroup = new ByteArrayFilterGroupList();

    public ReturnYouTubeChannelNameFilterPatch() {
        addPathCallbacks(
                new StringFilterGroup(Settings.RETURN_SHORTS_CHANNEL_NAME, "|reel_channel_bar_inner.eml|")
        );
        shortsChannelBarAvatarFilterGroup.addAll(
                new ByteArrayFilterGroup(Settings.RETURN_SHORTS_CHANNEL_NAME, "/@")
        );
    }

    @Override
    public boolean isFiltered(String path, @Nullable String identifier, String allValue, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (shortsChannelBarAvatarFilterGroup.check(protobufBufferArray).isFiltered()) {
            setLastShortsChannelId(protobufBufferArray);
        }

        return false;
    }

    private void setLastShortsChannelId(byte[] protobufBufferArray) {
        try {
            final String delimitingCharacter = "❙"; // Non ascii character, to allow easier log filtering.
            final String channelIdIdentifierCharacter = "UC";
            final String channelIdIdentifierWithDelimitingCharacter = "❙UC";
            final String handleIdentifierCharacter = "@";
            final String handleIdentifierWithDelimitingCharacter = "❙/@";

            final String bufferString = findAsciiStrings(protobufBufferArray);
            final String splitedBufferString =  channelIdIdentifierCharacter + bufferString.split(channelIdIdentifierWithDelimitingCharacter)[1];
            final String channelId =  splitedBufferString.split(delimitingCharacter)[0].replaceAll("\"", "");
            final String handle = handleIdentifierCharacter + splitedBufferString.split(handleIdentifierWithDelimitingCharacter)[1].split(delimitingCharacter)[0];

            ReturnYouTubeChannelNamePatch.setLastShortsChannelId(handle.trim(), channelId.trim());
        } catch (Exception ex) {
            Logger.printException(() -> "setLastShortsChannelId failed", ex);
        }
    }

    private String findAsciiStrings(byte[] buffer) {
        StringBuilder builder = new StringBuilder(Math.max(100, buffer.length / 2));
        builder.append("");

        // Valid ASCII values (ignore control characters).
        final int minimumAscii = 32;  // 32 = space character
        final int maximumAscii = 126; // 127 = delete character
        final int minimumAsciiStringLength = 4; // Minimum length of an ASCII string to include.
        String delimitingCharacter = "❙"; // Non ascii character, to allow easier log filtering.

        final int length = buffer.length;
        int start = 0;
        int end = 0;
        while (end < length) {
            int value = buffer[end];
            if (value < minimumAscii || value > maximumAscii || end == length - 1) {
                if (end - start >= minimumAsciiStringLength) {
                    for (int i = start; i < end; i++) {
                        builder.append((char) buffer[i]);
                    }
                    builder.append(delimitingCharacter);
                }
                start = end + 1;
            }
            end++;
        }
        return builder.toString();
    }
}

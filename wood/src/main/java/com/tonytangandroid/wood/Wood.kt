package com.tonytangandroid.wood

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build

/**
 * Class description
 *
 * @author tonytangandroid
 * @version 1.0
 * @since 03/06/18
 */
object Wood {
    /**
     * Get an Intent to launch the Wood UI directly.
     *
     * @param context A Context.
     * @return An Intent for the main Wood Activity that can be started with [     ][Context.startActivity].
     */
    fun getLaunchIntent(context: Context?): Intent {
        return Intent(context, LeafListActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    /**
     * Register an app shortcut to launch the Wood UI directly from the launcher on Android 7.0 and
     * above.
     *
     * @param context A valid [Context]
     * @return The id of the added shortcut (`null` if this feature is not supported on the
     * device). It can be used if you want to remove this shortcut later on.
     */
    @TargetApi(Build.VERSION_CODES.N_MR1)
    fun addAppShortcut(context: Context): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val id = context.packageName + ".wood_ui"
            val shortcutManager =
                context.getSystemService<ShortcutManager>(ShortcutManager::class.java)
            val shortcut =
                ShortcutInfo.Builder(context, id)
                    .setShortLabel("Wood")
                    .setLongLabel("Open Wood")
                    .setIcon(Icon.createWithResource(context, R.drawable.wood_icon))
                    .setIntent(getLaunchIntent(context).setAction(Intent.ACTION_VIEW))
                    .build()
            shortcutManager.addDynamicShortcuts(listOf<ShortcutInfo?>(shortcut))
            return id
        } else {
            return null
        }
    }
}

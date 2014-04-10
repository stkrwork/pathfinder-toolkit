package com.lateensoft.pathfinder.toolkit.util;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import org.jetbrains.annotations.NotNull;

/**
 * @author trevsiemens
 */
public interface ActivityLauncher {

    public void startActivity(Intent intent);

    public static class ActivityLauncherActivity implements ActivityLauncher {
        private Activity m_activity;

        public ActivityLauncherActivity(@NotNull Activity activity) {
            m_activity = activity;
        }

        @Override
        public void startActivity(Intent intent) {
            m_activity.startActivity(intent);
        }
    }

    public static class ActivityLauncherFragment implements ActivityLauncher {
        private Fragment m_fragment;

        public ActivityLauncherFragment(@NotNull Fragment fragment) {
            m_fragment = fragment;
        }

        @Override
        public void startActivity(Intent intent) {
            m_fragment.startActivity(intent);
        }
    }
}
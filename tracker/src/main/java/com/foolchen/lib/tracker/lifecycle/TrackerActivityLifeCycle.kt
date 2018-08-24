package com.foolchen.lib.tracker.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.foolchen.lib.tracker.Tracker
import com.foolchen.lib.tracker.utils.getTrackName
import com.foolchen.lib.tracker.utils.getTrackProperties
import com.foolchen.lib.tracker.utils.getTrackTitle
import java.lang.ref.WeakReference

/**
 * 该类用于监听项目中所有Activity的生命周期<p/>
 * 需要在[Application]中初始化，以便于能够及时监听所有的[Activity]
 * @author chenchong
 * 2017/11/4
 * 上午11:26
 */
object TrackerActivityLifeCycle {
//    val fragmentLifeCycle = TrackerFragmentLifeCycle()
    val refs = ArrayList<WeakReference<Activity>>()

    fun onActivityCreated(activity: Activity?) {
//        if (activity != null) {
//            wrap(activity)
//        }
//        if (activity is FragmentActivity) {
//            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifeCycle, true)
//        }
    }

    fun onActivityStarted(activity: Activity?) {
        if (/*Tracker.isBackground &&*/ refs.isEmpty()) {
            // 此处仅从后台切换到前台时触发，首次触发为初始化时，防止首次触发滞后
            Tracker.onForeground()
        }
        activity?.let {
            refs.add(WeakReference(activity))
        }
    }

    fun onActivityResumed(activity: Activity?) {
        if (activity != null) {
            if (activity is ITrackerIgnore) {
                if (!activity.isIgnored()) {
                    // 内部没有Fragment，直接进行统计
                    track(activity)
                }
            } else {
                // Activity内部没有Fragment，则直接进行统计
                track(activity)
            }
        }
    }

    fun onActivityPaused(activity: Activity?) {
    }

    fun onActivityStopped(activity: Activity?) {
        activity?.let {
            for (ref in refs) {
                if (ref.get() == activity) {
                    refs.remove(ref)
                    break
                }
            }
        }
        if (refs.isEmpty()) {
            Tracker.onBackground()
        }
    }

    fun onActivityDestroyed(activity: Activity?) {
//        if (activity is FragmentActivity) {
//            activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifeCycle)
//        }
    }

    fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    private fun track(activity: Activity) {
        Tracker.referer = Tracker.screenName
        Tracker.refererClass = Tracker.screenClass
        Tracker.screenName = activity.getTrackName()
        Tracker.screenClass = activity.javaClass.canonicalName
        Tracker.screenTitle = activity.getTrackTitle()
        Tracker.parent = ""
        Tracker.parentClass = ""
        Tracker.trackScreen(activity.getTrackProperties())
    }
}



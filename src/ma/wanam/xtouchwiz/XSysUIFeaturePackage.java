/*
 * Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ma.wanam.xtouchwiz;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ma.wanam.xtouchwiz.utils.Packages;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;

import com.samsung.android.multiwindow.MultiWindowStyle;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSysUIFeaturePackage {

	private static Object objKeyguardAbsKeyInputView;
	private static XSharedPreferences prefs;

	public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

		XSysUIFeaturePackage.prefs = prefs;

		if (!prefs.getString("selectedMwApps", "").trim().equalsIgnoreCase("")) {
			setMWApps(prefs, classLoader);
		}

		if (prefs.getBoolean("expandNotifications", false)) {
			try {
				expandAllNotifications(classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);

			}
		}

		if (prefs.getBoolean("quickPinUnlockEnabled", false)) {
			try {
				enableQuickUnlock(prefs, classLoader);
			} catch (Throwable e) {
				XposedBridge.log(e);

			}
		}

		try {
			setExpandedVolumePanel(classLoader);
		} catch (Throwable e) {
			XposedBridge.log(e);

		}

		try {
			Class<?> classFeature = XposedHelpers.findClass(Packages.SYSTEM_UI + ".statusbar.Feature", classLoader);

			try {
				XposedHelpers.setStaticBooleanField(classFeature, "mShowDataUsageInQuickPanel",
						prefs.getBoolean("showDataUsuage", false));
			} catch (Throwable e) {
				XposedBridge.log(e);

			}

			try {
				XposedHelpers.setStaticBooleanField(classFeature, "mShowDataUsageLimitForVZW",
						prefs.getBoolean("showDataUsuage", false));
			} catch (Throwable e) {

			}

			try {
				XposedHelpers.setStaticBooleanField(classFeature, "mShowWirelessChargerInfoPopUp",
						prefs.getBoolean("showWirelessChargerInfoPopUp", false));
			} catch (Throwable e) {

			}

			try {
				XposedHelpers.setStaticBooleanField(classFeature, "mShowMobileDataOffPopup",
						prefs.getBoolean("showDataPopUp", false));
			} catch (Throwable e) {
				XposedBridge.log(e);

			}

			try {
				XposedHelpers.setStaticBooleanField(classFeature, "mShowMobileDataPopupForLgt",
						prefs.getBoolean("showDataPopUp", false));
			} catch (Throwable e) {

			}

			try {
				XposedHelpers.setStaticBooleanField(classFeature, "mShowMobileDataPopupForVZW",
						prefs.getBoolean("showDataPopUp", false));
			} catch (Throwable e) {

			}

			try {
				XposedHelpers.setStaticBooleanField(classFeature, "mShowNoSimNotification",
						prefs.getBoolean("showNOSim", false));
			} catch (Throwable e) {
				XposedBridge.log(e);

			}

			try {
				XposedHelpers.setStaticBooleanField(classFeature, "mNoSIMNotificationForTMO",
						prefs.getBoolean("showNOSim", false));
			} catch (Throwable e) {

			}

			try {
				XposedHelpers.setStaticBooleanField(classFeature, "mNoSIMNotificationForVZW",
						prefs.getBoolean("showNOSim", false));
			} catch (Throwable e) {

			}

			try {
				XposedHelpers.setStaticBooleanField(classFeature, "mUseLTEDataIcon",
						!prefs.getBoolean("show4GForLTE", false));
			} catch (Throwable e) {

			}

			try {
				XposedHelpers.setStaticBooleanField(classFeature, "mShowAirplaneModeONPopup",
						!prefs.getBoolean("disableAirplaneModeDialog", false));
			} catch (Throwable e) {

			}
		} catch (Throwable e1) {
			XposedBridge.log(e1.getMessage());
		}

	}

	private static void setMWApps(final XSharedPreferences prefs, final ClassLoader classLoader) {
		try {
			XposedHelpers.findAndHookMethod("com.android.systemui.multiwindow.MultiWindowAppListInfo", classLoader,
					"loadMultiWindowAppList", new XC_MethodHook() {
						@Override
						protected void afterHookedMethod(MethodHookParam param) throws Throwable {

							Context mContext = (Context) XposedHelpers.getStaticObjectField(
									param.thisObject.getClass(), "mContext");
							PackageManager pm = mContext.getPackageManager();
							Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
							mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

							List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);
							Collections.sort(appList, new ResolveInfo.DisplayNameComparator(pm));
							List<String> appsMW = Arrays
									.asList(prefs.getString("selectedMwApps", "").trim().split(";"));

							XposedBridge.log("selectedMwApps" + prefs.getString("selectedMwApps", "").trim());

							for (ResolveInfo temp : appList) {

								if (appsMW.contains(temp.activityInfo.packageName)) {
									Class<?> mLaunchItem = XposedHelpers.findClass(
											"com.android.systemui.multiwindow.MultiWindowAppListInfo$LaunchItem",
											classLoader);
									Constructor<?> consLanch = XposedHelpers.findConstructorExact(mLaunchItem,
											param.thisObject.getClass(), ResolveInfo.class);
									Object tempAppLanch = consLanch.newInstance(param.thisObject, temp);
									XposedHelpers.callMethod(param.thisObject, "addwithcheckduplicate", tempAppLanch);
									XposedHelpers.callMethod(param.thisObject, "updateAppListOrder");
								}
							}

						}
					});

			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".recents.model.RecentsTaskLoader", classLoader,
					"isSupportMultiWindow", ActivityInfo.class, MultiWindowStyle.class,
					XC_MethodReplacement.returnConstant(true));
		} catch (Throwable e) {
			XposedBridge.log(e);

		}

	}

	private static void enableQuickUnlock(final XSharedPreferences prefs, ClassLoader classLoader) {

		Class<?> KeyguardAbsKeyInputView = XposedHelpers.findClass("com.android.keyguard.KeyguardAbsKeyInputView",
				classLoader);
		final Method verifyPasswordAndUnlock = XposedHelpers.findMethodExact(KeyguardAbsKeyInputView,
				"verifyPasswordAndUnlock");

		Class<?> PasswordTextView = XposedHelpers.findClass("com.android.keyguard.PasswordTextView", classLoader);

		XposedHelpers.findAndHookMethod(KeyguardAbsKeyInputView, "onFinishInflate", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {

				objKeyguardAbsKeyInputView = param.thisObject;
			}
		});

		XposedHelpers.findAndHookMethod(PasswordTextView, "append", char.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(final MethodHookParam param) throws Throwable {

				String mText = (String) XposedHelpers.getObjectField(param.thisObject, "mText");

				if (mText.length() == prefs.getInt("PINSize", 4)) {
					XposedBridge.invokeOriginalMethod(verifyPasswordAndUnlock, objKeyguardAbsKeyInputView, null);
				}
			}
		});
	}

	private static void expandAllNotifications(ClassLoader classLoader) {
		try {

			XposedHelpers.findAndHookMethod(Packages.SYSTEM_UI + ".statusbar.ExpandableNotificationRow", classLoader,
					"isUserExpanded", new XC_MethodReplacement() {

						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							return true;
						}
					});

		} catch (Throwable e) {
			XposedBridge.log(e);

		}
	}

	private static void setExpandedVolumePanel(ClassLoader classLoader) {

		Class<?> mVolumePanel = null;
		try {
			// Try stock Samsung implementation
			mVolumePanel = XposedHelpers.findClass(Packages.SYSTEM_UI + ".volume.SecVolumeDialog", classLoader);
			try {
				XposedHelpers.findAndHookMethod(mVolumePanel, "updateTintColor", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {

						XSysUIFeaturePackage.prefs.reload();
						if (XSysUIFeaturePackage.prefs.getBoolean("semiTransparentVolumePanel", false)) {
							XposedHelpers.setObjectField(
									param.thisObject,
									"mVolumePanelBgColor",
									XposedHelpers.callMethod(param.thisObject, "colorToColorStateList",
											Color.parseColor("#7fffffff")));
						}

					}

				});

			} catch (Throwable e) {

			}
		} catch (Throwable e) {

			// Try Good lock implementation
			try {
				mVolumePanel = XposedHelpers.findClass(Packages.SYSTEM_UI + ".volume.VolumeDialog", classLoader);
				final Class<?> mUtils = XposedHelpers.findClass(Packages.SYSTEM_UI + ".opensesame.utils.Utils",
						classLoader);
				final Class<?> mVolumeRow = XposedHelpers.findClass(Packages.SYSTEM_UI
						+ ".volume.VolumeDialog$VolumeRow", classLoader);

				XposedHelpers.findAndHookMethod(mVolumePanel, "updateVolumeRowSliderTintH", mVolumeRow, boolean.class,
						new XC_MethodHook() {
							@Override
							protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

								XSysUIFeaturePackage.prefs.reload();
								if (XSysUIFeaturePackage.prefs.getBoolean("semiTransparentVolumePanel", false)) {

									ColorStateList csl = (ColorStateList) XposedHelpers.callStaticMethod(mUtils,
											"colorToColorStateList", Color.parseColor("#7fffffff"));

									Object mDialogView = XposedHelpers.getObjectField(param.thisObject, "mDialogView");
									XposedHelpers.callMethod(mDialogView, "setBackgroundTintList", csl);
								}

							}

						});

			} catch (Throwable t) {
				XposedBridge.log(t.getMessage());
			}
		}

		try {
			XposedHelpers.findAndHookMethod(mVolumePanel, "showH", int.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					XSysUIFeaturePackage.prefs.reload();
					XposedHelpers.callMethod(param.thisObject, "setExpandedH",
							XSysUIFeaturePackage.prefs.getBoolean("autoExpandVolumePanel", false));
				}

			});

		} catch (Throwable e) {
			XposedBridge.log(e);

		}

	}

}

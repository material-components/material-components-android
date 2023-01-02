/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.application;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.multidex.MultiDexApplication;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.Log;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import io.material.catalog.preferences.BaseCatalogPreferences;
import java.lang.reflect.InvocationTargetException;
import javax.inject.Inject;

/** Catalog application class that provides support for using dispatching Dagger injectors. */
public class CatalogApplication extends MultiDexApplication implements HasAndroidInjector {

  /** Logging tag */
  public static final String TAG = "CatalogApplication";
  /** Key that contains the class name to replace the default application component. */
  public static final String COMPONENT_OVERRIDE_KEY =
      "io.material.catalog.application.componentOverride";

  @Inject DispatchingAndroidInjector<Object> androidInjector;
  @Inject BaseCatalogPreferences catalogPreferences;

  @Override
  public void onCreate() {
    super.onCreate();
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

    if (!overrideApplicationComponent(this)) {
      DaggerCatalogApplicationComponent.builder().application(this).build().inject(this);
    }
    catalogPreferences.applyPreferences(this);
  }

  /**
   * Replaces the application component by the one specified in AndroidManifest.xml metadata with
   * key {@link #COMPONENT_OVERRIDE_KEY}. Returns {@code true} if the component was properly
   * initialized and replaced, otherwise returns {@code false}.
   *
   * <p>This assumes that the replacement component can be initialized exactly the same way as the
   * default component.
   *
   * <p>Suppressing unchecked warnings because there is no way we have a statically typed class
   * argument for instances of Class in this method.
   */
  private boolean overrideApplicationComponent(CatalogApplication catalogApplication) {
    try {
      ApplicationInfo applicationInfo =
          getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
      String className = applicationInfo.metaData.getString(COMPONENT_OVERRIDE_KEY);
      if (className == null) {
        // Fail early
        Log.i(TAG, "Component override metadata not found, using default component.");
        return false;
      }
      Log.i(TAG, className);
      Object builderObject = Class.forName(className).getMethod("builder").invoke(null);
      Class<?> builderClass = builderObject.getClass();
      builderClass
          .getMethod("application", Application.class)
          .invoke(builderObject, catalogApplication);
      Object component = builderClass.getMethod("build").invoke(builderObject);
      component
          .getClass()
          .getMethod("inject", getCatalogApplicationClass())
          .invoke(component, catalogApplication);
      return true;
    } catch (PackageManager.NameNotFoundException
        | ClassNotFoundException
        | NoSuchMethodException
        | InvocationTargetException
        | IllegalAccessException e) {
      Log.e(TAG, "Component override failed with exception:", e);
    }
    return false;
  }

  protected Class<? extends CatalogApplication> getCatalogApplicationClass() {
    return CatalogApplication.class;
  }

  @Override
  public AndroidInjector<Object> androidInjector() {
    return androidInjector;
  }
}

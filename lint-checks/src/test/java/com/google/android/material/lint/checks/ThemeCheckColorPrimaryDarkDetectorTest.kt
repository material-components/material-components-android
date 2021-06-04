package com.google.android.material.lint.checks

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ThemeCheckColorPrimaryDarkDetectorTest : LintDetectorTest() {

  override fun getIssues(): MutableList<Issue> = mutableListOf(ThemeCheckColorPrimaryDarkDetector.ISSUE)

  override fun getDetector(): Detector = ThemeCheckColorPrimaryDarkDetector()

  @Test
  fun expectPass() {
    lint()
      .allowMissingSdk(true)
      .files(
        xml(
          "res/values/styles.xml", """
<resources>
   <style name="AppTheme" parent="Theme.MaterialComponents.DayNight">
     <item name="colorPrimary">@color/blu500</item>
   </style>
</resources>
                """
        )
      )
      .run()
      .expectClean()
  }

  @Test
  fun expectFail() {
    lint()
      .allowMissingSdk(true)
      .files(
        xml(
          "res/values/styles.xml", """
<resources>
   <style name="AppTheme" parent="Theme.MaterialComponents.DayNight">
     <item name="colorPrimaryDark">@color/blu500</item>
   </style>
</resources>
                """
        )
      )
      .run()
      .expect(
        """
res/values/styles.xml:4: Warning: colorPrimaryDark is superseded by newer theme attribute colorPrimaryVariant [ThemeCheckColorPrimaryDark]
     <item name="colorPrimaryDark">@color/blu500</item>
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings
            """
      )
  }
}

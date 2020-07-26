package com.google.android.material.lint.checks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

/**
 * The class contains the list of issues that will be checked when running lint.
 */

class IssueRegistry : IssueRegistry() {

  override val api: Int = CURRENT_API

  override val issues: List<Issue> = listOf(
    ThemeCheckColorAccentDetector.ISSUE,
    ThemeCheckColorPrimaryDarkDetector.ISSUE
  )
}

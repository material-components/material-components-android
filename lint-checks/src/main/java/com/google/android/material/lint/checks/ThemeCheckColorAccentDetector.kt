package com.google.android.material.lint.checks

import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.*
import org.w3c.dom.Attr
import org.w3c.dom.Element
import org.w3c.dom.Node

class ThemeCheckColorAccentDetector : ResourceXmlDetector() {

  companion object {
    val ISSUE = Issue.create(
      id = "ThemeCheckColorAccent",
      briefDescription = "Detects usages colorAccent in style resources",
      explanation = "The colorAccent is superseded by newer theme attribute colorSecondary in style resources",
      category = Category.CORRECTNESS,
      severity = Severity.WARNING,
      implementation = Implementation(
        ThemeCheckColorAccentDetector::class.java,
        Scope.RESOURCE_FILE_SCOPE
      )
    )
    private const val NODE_STYLE = "style"
    private const val NODE_ITEM = "item"
    private const val ATTR_NAME = "name"
    private const val COLOR_ACCENT = "colorAccent"
  }

  override fun appliesTo(folderType: ResourceFolderType): Boolean {
    //we only need to analyze 'style' in the 'values' resource folder.
    return folderType == ResourceFolderType.VALUES
  }

  override fun getApplicableElements(): Collection<String>? {
    //we want to analyze every `<item>` element that is declared in XML.
    return setOf(NODE_ITEM)
  }

  override fun visitElement(context: XmlContext, element: Element) {

    if (element.hasAttribute(ATTR_NAME) && element.parentNode.nodeName == NODE_STYLE) {
      visitAttribute(context,element.getAttributeNode(ATTR_NAME))
    }
  }

  override fun visitAttribute(context: XmlContext, attribute: Attr) {

    val attributeName = attribute.nodeValue

    if (attributeName == COLOR_ACCENT) {

      context.report(
        issue = ISSUE,
        scope = attribute,
        location = context.getLocation(attribute.ownerElement),
        message = "colorAccent is superseded by newer theme attribute colorSecondary",
        quickfixData = LintFix.create()
          .replace()
          .text("colorAccent")
          .with("colorSecondary")
          .build()
      )
    }
  }
}

package android.support.design.widget.expandable;

import android.support.annotation.IdRes;

/**
 * A widget that has expanded/collapsed state.
 *
 * <p>When the expanded state changes, an event is dispatched so that other widgets may react via a
 * {@link android.support.design.widget.CoordinatorLayout.Behavior}.
 */
public interface ExpandableWidget {

  /**
   * Sets the expanded state on this widget.
   */
  void setExpanded(boolean expanded);

  /** Returns whether this widget is expanded. */
  boolean isExpanded();

  /**
   * Sets the expanded component id hint, which may be used by a Behavior to determine whether it
   * should handle this widget's state change.
   */
  void setExpandedComponentIdHint(@IdRes int expandedComponentIdHint);

  /**
   * Returns the expanded component id hint.
   */
  @IdRes
  int getExpandedComponentIdHint();
}

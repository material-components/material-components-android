package android.support.design.widget.expandable;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewParent;

/**
 * ExpandableWidgetHelper is a helper class for writing custom {@link ExpandableWidget
 * ExpandableWidgets}. Please see each method's documentation when implementing your custom class.
 */
public final class ExpandableWidgetHelper {

  private final View widget;

  private boolean expanded;
  @IdRes private int expandedComponentIdHint;

  /** Call this from the constructor. */
  public <T extends View & ExpandableWidget> ExpandableWidgetHelper(T widget) {
    this.widget = widget;
  }

  /** Call this from {@link ExpandableWidget#setExpanded(boolean)}. */
  public void setExpanded(boolean expanded) {
    if (this.expanded != expanded) {
      this.expanded = expanded;
      dispatchExpandedStateChanged();
    }
  }

  /** Call this from {@link ExpandableWidget#isExpanded()}. */
  public boolean isExpanded() {
    return expanded;
  }

  /** Call this from {@link ExpandableWidget#setExpandedComponentIdHint(int)}. */
  public void setExpandedComponentIdHint(@IdRes int expandedComponentIdHint) {
    this.expandedComponentIdHint = expandedComponentIdHint;
  }

  /** Call this from {@link ExpandableWidget#getExpandedComponentIdHint()}. */
  @IdRes
  public int getExpandedComponentIdHint() {
    return expandedComponentIdHint;
  }

  /**
   * Call this from {@link View#onSaveInstanceState()} after your widget's custom state is saved.
   */
  public Bundle onSaveInstanceState() {
    Bundle state = new Bundle();
    state.putBoolean("expanded", expanded);
    state.putInt("expandedComponentIdHint", expandedComponentIdHint);

    return state;
  }

  /**
   * Call this from {@link View#onRestoreInstanceState(Parcelable)} after your widget's custom state
   * is restored.
   */
  public void onRestoreInstanceState(Bundle state) {
    expanded = state.getBoolean("expanded", false);
    expandedComponentIdHint = state.getInt("expandedComponentIdHint", 0);

    if (expanded) {
      dispatchExpandedStateChanged();
    }
  }

  private void dispatchExpandedStateChanged() {
    ViewParent parent = widget.getParent();
    if (parent instanceof CoordinatorLayout) {
      ((CoordinatorLayout) parent).dispatchDependentViewsChanged(widget);
    }
  }
}

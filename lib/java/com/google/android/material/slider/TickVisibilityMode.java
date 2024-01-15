package com.google.android.material.slider;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mode to specify the visibility of tick marks.
 */
@IntDef({
    TickVisibilityMode.TICK_VISIBILITY_VISIBLE_ALL,
    TickVisibilityMode.TICK_VISIBILITY_AUTO_LIMIT,
    TickVisibilityMode.TICK_VISIBILITY_AUTO_HIDE,
    TickVisibilityMode.TICK_VISIBILITY_HIDDEN
})
@Retention(RetentionPolicy.SOURCE)
public @interface TickVisibilityMode {

  /**
   * All tick marks will be drawn, even if they are spaced too densely.
   */
  int TICK_VISIBILITY_VISIBLE_ALL = 0;

  /**
   * All tick marks will be drawn if they are not spaced too densely. Otherwise, the maximum
   * allowed number of tick marks will be drawn.
   * Note that in this case, the drawn ticks may not match the actual snap values.
   */
  int TICK_VISIBILITY_AUTO_LIMIT = 1;

  /**
   * All tick marks will be drawn if they are not spaced too densely. Otherwise, the tick marks
   * will not be drawn.
   */
  int TICK_VISIBILITY_AUTO_HIDE = 2;

  /**
   * Tick marks will not be drawn.
   */
  int TICK_VISIBILITY_HIDDEN = 3;
}

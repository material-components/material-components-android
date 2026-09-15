package io.material.catalog.textfield;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class AutoCompleteTextView2 extends MaterialAutoCompleteTextView {
  public AutoCompleteTextView2(@NonNull Context context) {
    super(context);
  }

  public AutoCompleteTextView2(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public AutoCompleteTextView2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    int threshold = getThreshold();
    setThreshold(Integer.MAX_VALUE);
    super.onRestoreInstanceState(state);
    setThreshold(threshold);
  }
}

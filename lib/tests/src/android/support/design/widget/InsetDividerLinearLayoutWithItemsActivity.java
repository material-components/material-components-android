package android.support.design.widget;

import android.support.design.test.R;

public class InsetDividerLinearLayoutWithItemsActivity extends BaseTestActivity {

  public InsetDividerLinearLayout mLinearLayout;

  @Override
  protected int getContentViewLayoutResId() {
    return R.layout.design_inset_divider_linear_layout_items;
  }

  @Override
  protected void onContentViewSet() {
    mLinearLayout = (InsetDividerLinearLayout) findViewById(R.id.inset_divider_linear_layout);
  }
}

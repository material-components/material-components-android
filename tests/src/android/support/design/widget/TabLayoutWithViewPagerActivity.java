package android.support.design.widget;

import android.support.design.test.R;
import android.support.v7.widget.Toolbar;

public class TabLayoutWithViewPagerActivity extends BaseTestActivity {
    @Override
    protected int getContentViewLayoutResId() {
        return R.layout.design_tabs_viewpager;
    }

    @Override
    protected void onContentViewSet() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

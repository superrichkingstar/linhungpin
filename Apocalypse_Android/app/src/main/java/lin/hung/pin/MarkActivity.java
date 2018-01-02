package lin.hung.pin;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import lin.hung.pin.adapter.MyPagerAdapter;
import lin.hung.pin.base.BaseActivity;
import lin.hung.pin.db.BookCatalogue;
import lin.hung.pin.util.FileUtils;
import lin.hung.pin.util.PageFactory;

import java.util.ArrayList;

import butterknife.Bind;

/**
 * Created by Administrator on 2017/11/6.
 */
public class MarkActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.appbar)
    AppBarLayout appbar;
    @Bind(R.id.tabs)
    PagerSlidingTabStrip tabs;
    @Bind(R.id.pager)
    ViewPager pager;

//    @Bind(R.id.lv_catalogue)
//    ListView lv_catalogue;

    private PageFactory pageFactory;
    private Config config;
    private Typeface typeface;
    private ArrayList<BookCatalogue> catalogueList = new ArrayList<>();
    private DisplayMetrics dm;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_mark;
    }

    @Override
    protected void initData() {
        pageFactory = PageFactory.getInstance();
        config = Config.getInstance();
        dm = getResources().getDisplayMetrics();
        typeface = config.getTypeface();

        setSupportActionBar(toolbar);
        //設置導航圖標
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(FileUtils.getFileName(pageFactory.getBookPath()));
        }

        setTabsValue();
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(),pageFactory.getBookPath()));
        tabs.setViewPager(pager);
    }

    private void setTabsValue() {
        // 設置Tab是自動填充滿屏幕的
        tabs.setShouldExpand(true);//所有初始化要在setViewPager方法之前
        // 設置Tab的分割線是透明的
        tabs.setDividerColor(Color.TRANSPARENT);
        // 設置Tab底部線的高度
        tabs.setUnderlineHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, dm));
        // 設置Tab Indicator的高度
        tabs.setIndicatorHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2, dm));
        // 設置Tab標題文字的大小
        tabs.setTextSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 16, dm));
        //設置Tab標題文字的字體
        tabs.setTypeface(typeface,0);
        // 設置Tab Indicator的顏色
        tabs.setIndicatorColor(getResources().getColor(R.color.colorAccent));
        // 取消點擊Tab時的背景色
        tabs.setTabBackground(0);

        // pagerSlidingTabStrip.setDividerPadding(18);
    }

    @Override
    protected void initListener() {

    }

}

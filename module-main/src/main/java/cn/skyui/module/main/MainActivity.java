package cn.skyui.module.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.List;

import cn.skyui.library.base.activity.BaseActivity;
import cn.skyui.library.base.fragment.BaseLazyLoadFragment;
import cn.skyui.library.utils.StringUtils;
import cn.skyui.library.utils.ToastUtils;
import cn.skyui.module.main.fragment.CustomViewPager;
import cn.skyui.module.main.model.MainIntentProtocol;
import cn.skyui.moudle.market.fragment.MainMarketFragment;
import cn.skyui.moudle.market.fragment.TempFragment;

/**
 * @author tianshaojie
 * @date 2018/1/15
 */
@Route(path = "/main/main")
public class MainActivity extends BaseActivity {

    private static final String SELECTED_INDEX = "selectedIndex";
    private int selectedIndex = MainIntentProtocol.DEFAULT_SELECTED_INDEX;
    // 外部跳转到首页的协议类
    private MainIntentProtocol protocol;

    private RadioGroup radioGroup;
    private CustomViewPager fragmentViewPager;

    private String[] fragmentTitles;
    private List<BaseLazyLoadFragment> fragments;

    @Override
    protected void onCreateSafely(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        initFragments();
        initView();
        initByIntent(getIntent(), savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initByIntent(intent, null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_INDEX, selectedIndex);
        super.onSaveInstanceState(outState);
    }

    private void initByIntent(Intent intent, Bundle savedInstanceState) {
        if(intent == null) {
            return;
        }
        protocol = getIntent().getParcelableExtra(MainIntentProtocol.MAIN_PROTOCOL);
        if(protocol == null) {
            protocol =  MainIntentProtocol.DEFAULT;
        }
        selectedIndex = protocol.primaryTabIndex;
        if (savedInstanceState != null) {
            selectedIndex = savedInstanceState.getInt(SELECTED_INDEX, MainIntentProtocol.DEFAULT_SELECTED_INDEX);
        }
        showSelectedFragment(selectedIndex);
        updateFragmentArguments(protocol.bundle);
        if(StringUtils.isNotEmpty(protocol.openPageRouter)) {
            ARouter.getInstance().build(protocol.openPageRouter)
                    .withBundle(MainIntentProtocol.MAIN_PROTOCOL_BUNDLE, protocol.bundle)
                    .navigation();
        }
    }

    private void initFragments() {
        fragmentTitles = getResources().getStringArray(R.array.main_fragment_titles);
        fragments = new ArrayList<>(fragmentTitles.length);
        for (int i = 0; i < fragmentTitles.length; i++) {
            fragments.add(null);
        }
    }

    private void initView() {
        fragmentViewPager = findViewById(R.id.fragment_container);
        fragmentViewPager.setOffscreenPageLimit(fragments.size());
        fragmentViewPager.setCanScroll(false);
        fragmentViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return MainMarketFragment.newInstance(fragmentTitles[position]);
                    case 1:
                        return TempFragment.newInstance(fragmentTitles[position]);
                    case 2:
                        return TempFragment.newInstance(fragmentTitles[position]);
                    case 3:
                        return TempFragment.newInstance(fragmentTitles[position]);
                    default:
                        return null;
                }
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                BaseLazyLoadFragment object = (BaseLazyLoadFragment) super.instantiateItem(container, position);
                fragments.set(position, object);
                return object;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                super.destroyItem(container, position, object);
                fragments.set(position, null);
            }

            @Override
            public int getCount() {
                return fragmentTitles.length;
            }
        });

        fragmentViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                radioGroup.check(radioGroup.getChildAt(position).getId());
            }
        });

        radioGroup = findViewById(R.id.group);
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            final int index = i;
            radioGroup.getChildAt(i).setOnClickListener(v -> showSelectedFragment(index));
        }
    }

    private void showSelectedFragment(int index) {
        if(index < 0 || index >= fragments.size()) {
            index = 0;
        }
        selectedIndex = index;
        fragmentViewPager.setCurrentItem(selectedIndex);
    }

    private void updateFragmentArguments(Bundle bundle) {
        if(bundle != null) {
            fragments.get(selectedIndex).setArguments(bundle);
        }
    }

    private long firstTime = 0;
    @Override
    public void onBackPressed() {
        long secondTime = System.currentTimeMillis();
        if (secondTime - firstTime > 2000) {
            ToastUtils.showShort("再按一次退出程序");
            firstTime = secondTime;
        } else {
            finish();
        }
    }

}
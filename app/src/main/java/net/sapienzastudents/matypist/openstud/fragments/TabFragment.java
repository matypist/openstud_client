package net.sapienzastudents.matypist.openstud.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import net.sapienzastudents.matypist.openstud.R;
import net.sapienzastudents.matypist.openstud.activities.PaymentsActivity;
import net.sapienzastudents.matypist.openstud.adapters.TaxAdapter;

public class TabFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int int_items = 2;
    private int selectedTab = -1;


    public static TabFragment newInstance(int page) {
        TabFragment frag = new TabFragment();
        Bundle args = new Bundle();
        args.putInt("tabSelected", page);
        frag.setArguments(args);
        return frag;
    }


    public void onCreate(Bundle bdl) {
        super.onCreate(bdl);
        selectedTab = getArguments().getInt("tabSelected");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //this inflates out tab layout file.
        View x = inflater.inflate(R.layout.tab_fragment_payment_layout, null);
        // set up stuff.
        tabLayout = x.findViewById(R.id.tabs);
        viewPager = x.findViewById(R.id.viewpager);
        // create a new adapter for our pageViewer. This adapters returns child net.sapienzastudents.matypist.openstud.fragments as per the positon of the page Viewer.
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));
        if (selectedTab != -1) {
            viewPager.setCurrentItem(selectedTab);
            notifyItemChanged(selectedTab);
        }
        tabLayout.post(() -> {
            //provide the viewPager to TabLayout.
            tabLayout.setupWithViewPager(viewPager);
        });
        //to preload the adjacent tabs. This makes transition smooth.
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                notifyItemChanged(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return x;
    }

    private void notifyItemChanged(int item) {
        PaymentsActivity act = (PaymentsActivity) getActivity();
        if (act != null) {
            act.updateSelectTab(item);
        }
    }

    class MyAdapter extends FragmentPagerAdapter {

        MyAdapter(FragmentManager fm) {
            super(fm);
        }

        //return the fragment with respect to page position.
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return PaymentsFragment.newInstance(TaxAdapter.Mode.UNPAID.getValue());
                case 1:
                    return PaymentsFragment.newInstance(TaxAdapter.Mode.PAID.getValue());
            }
            return null;
        }

        @Override
        public int getCount() {
            return int_items;
        }

        //This method returns the title of the tab according to the position.
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    if (!isAdded()) return "Unpaid";
                    return getResources().getString(R.string.unpaid);
                case 1:
                    if (!isAdded()) return "Paid";
                    return getResources().getString(R.string.paid);
            }
            return null;
        }
    }
}
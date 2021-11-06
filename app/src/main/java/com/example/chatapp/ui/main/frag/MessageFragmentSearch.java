package com.example.chatapp.ui.main.frag;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chatapp.R;
import com.example.chatapp.cons.ZoomOutPageTransformer;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class MessageFragmentSearch extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private final Context context;
    private FragmentSearchFriend fragmentSearchFriend;
    private FragmentSearchGroup fragmentSearchGroup;
    private FragmentSearchMessage fragmentSearchMessage;
    private final int NUM_PAGES = 3;
    private TabLayout tableLayout;
    private int[] counts = new int[NUM_PAGES];
    private String[] title = new String[NUM_PAGES];

    public MessageFragmentSearch(Context context) {
        this.context = context;
    }

    public static MessageFragmentSearch newInstance(String param1, String param2, Context context) {
        MessageFragmentSearch fragment = new MessageFragmentSearch(context);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_search, container, false);

        if (fragmentSearchFriend == null)
            fragmentSearchFriend = new FragmentSearchFriend(context, this);
        if (fragmentSearchGroup == null)
            fragmentSearchGroup = new FragmentSearchGroup(context, this);

        tableLayout = view.findViewById(R.id.fragment_message_search_tab_layout);
        ViewPager2 viewPager = view.findViewById(R.id.fragment_message_search_view_paper);

        viewPager.setAdapter(new ScreenSlidePagerAdapter(context));
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        viewPager.setOffscreenPageLimit(NUM_PAGES);

        tableLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.purple_200));

        title[0] = context.getString(R.string.friend);
        title[1] = context.getString(R.string.title_group);
        title[2] = context.getString(R.string.title_message);

        new TabLayoutMediator(tableLayout, viewPager, (tab, position) -> {
            if (counts[position] == 0)
                tab.setText(title[position]);
            else
                tab.setText(String.format("%s(%d)", title[position], counts[position]));
        }).attach();

        return view;
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(@NonNull Context fragment) {
            super((FragmentActivity) fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return fragmentSearchFriend;
            } else if (position == 1) {
                return fragmentSearchGroup;
            } else {
                if (fragmentSearchMessage == null)
                    fragmentSearchMessage = new FragmentSearchMessage(context);
                return fragmentSearchMessage;
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void search(String s) {
        if (fragmentSearchFriend != null)
            fragmentSearchFriend.search(s);
        if (fragmentSearchGroup != null)
            fragmentSearchGroup.search(s);
    }

    public void updateCountSearchResult(int position, int count) {
        if (position < NUM_PAGES && tableLayout.getTabAt(position) != null) {
            counts[position] = count;
            if (count != 0)
                tableLayout.getTabAt(position).setText(String.format("%s(%d)", title[position], count));
            else
                tableLayout.getTabAt(position).setText(title[position]);
        }
    }

}
package com.example.chatapp.ui.main.frag;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.chatapp.R;
import com.example.chatapp.dto.MessageDto;
import com.example.chatapp.ui.AddFriendActivity;
import com.example.chatapp.ui.CreateGroupActivity;

public class MessageFragment extends Fragment {
    private final Context context;
    private MessageFragmentContent messageFragmentContent;
    private MessageFragmentSearch messageFragmentSearch;
    private boolean isSearchShow = false;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public MessageFragment(Context context) {
        this.context = context;
    }

    public static MessageFragment newInstance(String param1, String param2, Context context) {
        MessageFragment fragment = new MessageFragment(context);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        enable menu trên action bar
         */
        setHasOptionsMenu(false);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setNewMessage(MessageDto newMessage) {
        messageFragmentContent.setNewMessage(newMessage);
    }

    @Override
    public void onResume() {
        super.onResume();
        /*
        chỉ khi nào fragment được hiển thị thì mới hiện menu
         */
        setHasOptionsMenu(isVisible());
//        showFragmentContent();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        if (messageFragmentContent == null)
            messageFragmentContent = new MessageFragmentContent(context);
        if (messageFragmentSearch == null)
            messageFragmentSearch = new MessageFragmentSearch(context);
        showFragmentContent();
        return view;
    }

    private void showFragmentContent() {
        isSearchShow = false;
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_message_body, messageFragmentContent).commit();
    }

    private void showFragmentSearch() {
        isSearchShow = true;
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_message_body, messageFragmentSearch).commit();
    }

    /**
     * menu search user
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_message_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.search_user_fragment_message);

        View actionView = menuItem.getActionView();

        SearchView searchView = (SearchView) actionView;
        searchView.setQueryHint(getString(R.string.search_more));
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.requestFocus();

        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        searchPlate.setBackgroundResource(R.drawable.search_view_background_light);
        ViewGroup.LayoutParams params = searchPlate.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        searchPlate.setLayoutParams(params);

        int searchIcon = searchView.getContext().getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView magImage = searchView.findViewById(searchIcon);
        magImage.setVisibility(View.GONE);
        magImage.setImageDrawable(null);

        /*
        tìm icon close và edit text của search view
         */
        int closeIconId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        int editTextId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        ImageView closeIcon = searchView.findViewById(closeIconId);
        closeIcon.setImageResource(R.drawable.ic_baseline_close_circle_24_white);
        EditText editText = searchView.findViewById(editTextId);
        editText.setHintTextColor(Color.WHITE);
        editText.setTextColor(Color.WHITE);
        editText.setPadding(50, 0, 50, 0);

        menu.findItem(R.id.submenu_fragment_message).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final FragmentActivity activity = getActivity();
                if (activity != null) {
                    View view = activity.findViewById(item.getItemId());

                    final PopupMenu popupMenu = new PopupMenu(activity.getApplicationContext(), view);
                    popupMenu.getMenuInflater().inflate(R.menu.popup_menu_message_fragment, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.action_create_group) {
                                Intent intent = new Intent(activity, CreateGroupActivity.class);
                                activity.startActivity(intent);
                            } else if (item.getItemId() == R.id.action_add_friend) {
                                Intent intent2 = new Intent(activity, AddFriendActivity.class);
                                activity.startActivity(intent2);
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }

                return true;
            }
        });

        /*
        sự kiện click icon close trên search view
         */
        closeIcon.setOnClickListener(v -> {
            editText.setText("");
            showFragmentContent();
        });

        /*
        sự kiện gõ trên search view
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextSubmit(String query) {
                onQueryTextChange(query);
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!isSearchShow)
                    showFragmentSearch();
                messageFragmentSearch.search(newText);

                return false;
            }
        });

        /*
        sự kiện click vào để mở rộng search view, và sự kiện click mũi tên để thu gọn search view
         */
        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                /*
                sự kiện thu gọn search view
                 */
                showFragmentContent();
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                showFragmentSearch();
                /*
                sự kiện mở rộng search view
                 */
                return true;
            }
        };

        MenuItemCompat.setOnActionExpandListener(menuItem, expandListener);
        super.onCreateOptionsMenu(menu, inflater);
    }

}
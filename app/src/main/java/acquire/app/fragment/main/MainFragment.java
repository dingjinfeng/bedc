package acquire.app.fragment.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import acquire.app.R;
import acquire.app.databinding.AppFragmentMainBinding;
import acquire.app.databinding.AppPageDotBinding;
import acquire.app.fragment.main.menu.MainMenu;
import acquire.app.fragment.main.menu.MainMenuAdapter;
import acquire.app.fragment.main.menu.MenuItem;
import acquire.base.ActivityStackManager;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DisplayUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.TransActivity;
import acquire.core.constant.TransTag;

/**
 * A main {@link Fragment}
 *
 * @author Janson
 * @date 2019/1/28 10:22
 */
public class MainFragment extends BaseFragment {
    /**
     * menu page size
     */
    private static int PAGE_SIZE = 12;
    private AppFragmentMainBinding binding;

    @NonNull
    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = AppFragmentMainBinding.inflate(inflater, container, false);
        DisplayMetrics metrics = DisplayUtils.getDisplayMetrics(mActivity);
        double heightDps = metrics.heightPixels / metrics.density;
        if (heightDps >= 800) {
            PAGE_SIZE = 15;
        }else if (heightDps<=480){
            PAGE_SIZE = 9;
        }
        initView();
        return binding.getRoot();
    }


    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }

    @Override
    public int[] getPopAnimation() {
        return new int[]{R.anim.slide_left_in, 0};
    }

    private void initView() {
        List<MenuItem> items = MainMenu.getInstance().getMenu();
        //top item
        MenuItem topItem = items.get(0);
        binding.ivTopIcon.setImageResource(topItem.getIcon());
        binding.tvTopName.setText(topItem.getName());
        if (topItem.getColorId() != 0) {
            binding.cvTopContainer.setCardBackgroundColor(ContextCompat.getColor(mActivity, topItem.getColorId()));
        }
        binding.cvTopContainer.setOnClickListener(v -> {
            if (ActivityStackManager.getTopActivity() instanceof TransActivity) {
                return;
            }
            //start transaction
            Intent intent = new Intent(mActivity, TransActivity.class);
            intent.putExtra(TransTag.TRANS_TYPE, topItem.getTransType());
            ActivityCompat.startActivity(mActivity, intent, null);
        });
        //menu page
        List<MenuFragment> childFragments = createMenuFragments(items.subList(1, items.size()));
        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return childFragments.get(position);
            }

            @Override
            public int getItemCount() {
                return childFragments.size();
            }
        });
        //bottom dot
        if (childFragments.size() < 2) {
            binding.rvDot.setVisibility(View.GONE);
        } else {
            binding.rvDot.setVisibility(View.VISIBLE);
            DotAdapter dotAdapter = new DotAdapter(childFragments.size());
            binding.rvDot.setAdapter(dotAdapter);
            binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    dotAdapter.setSelected(position);
                }
            });
        }
    }

    @NonNull
    private List<MenuFragment> createMenuFragments(@NonNull List<MenuItem> items) {
        List<MenuFragment> fragments = new ArrayList<>();
        for (int i = 0; i < items.size(); i += PAGE_SIZE) {
            List<MenuItem> fragItems = new ArrayList<>();
            int remainSize = items.size() - i;
            if (remainSize >= PAGE_SIZE) {
                fragItems.addAll(items.subList(i, i + PAGE_SIZE));
            } else {
                //fill placeholder item
                fragItems.addAll(items.subList(i, items.size()));
                int fillSize = PAGE_SIZE - remainSize;
                for (int j = 0; j < fillSize; j++) {
                    fragItems.add(MainMenuAdapter.FILL_PLACE_ITEM);
                }
            }
            fragments.add(MenuFragment.newInstance(fragItems));
        }
        return fragments;
    }

    @Override
    public boolean onBack() {
        //Confirm to exit this application
        new MessageDialog.Builder(mActivity)
                .setMessage(R.string.app_exit_prompt)
                .setBackEnable(true)
                .setConfirmButton(dialog -> mActivity.finish())
                .setCancelButton(dialog -> {
                })
                .show();
        return true;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (MainMenu.getInstance().isChanged()) {
            initView();
        }
    }


    /**
     * dot adapter
     *
     * @author Janson
     * @date 2021/11/26 16:10
     */
    private class DotAdapter extends BaseBindingRecyclerAdapter<AppPageDotBinding> {
        private final int count;
        private int selected;

        public DotAdapter(int count) {
            this.count = count;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setSelected(int selected) {
            this.selected = selected;
            notifyDataSetChanged();
        }

        @Override
        protected void bindItemData(AppPageDotBinding itemBinding, int position) {
            itemBinding.ivDot.setSelected(position == selected);
            itemBinding.getRoot().setOnClickListener(v -> binding.viewPager.setCurrentItem(position, true));
        }

        @Override
        protected Class<AppPageDotBinding> getViewBindingClass() {
            return AppPageDotBinding.class;
        }

        @Override
        public int getItemCount() {
            return count;
        }
    }
}

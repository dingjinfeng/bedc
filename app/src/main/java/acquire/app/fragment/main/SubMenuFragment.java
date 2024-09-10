package acquire.app.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import acquire.app.databinding.AppFragmentSubMenuBinding;
import acquire.app.fragment.main.menu.MainMenuAdapter;
import acquire.app.fragment.main.menu.MenuItem;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;

/**
 * A sub menu fragment.
 *
 * @author Janson
 * @date 2021/8/6 14:53
 */
public class SubMenuFragment extends BaseFragment {
    private List<MenuItem> menuItems;
    private String title;

    @NonNull
    public static SubMenuFragment newInstance(String title, List<MenuItem> menuItems) {
        SubMenuFragment fragment = new SubMenuFragment();
        fragment.menuItems = menuItems;
        fragment.title = title;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppFragmentSubMenuBinding binding = AppFragmentSubMenuBinding.inflate(inflater, container, false);
        if (menuItems != null){
            binding.rvMenu.setAdapter(new MainMenuAdapter(mActivity, menuItems));
        }
        binding.toolbar.setTitle(title);
        return binding.getRoot();
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}

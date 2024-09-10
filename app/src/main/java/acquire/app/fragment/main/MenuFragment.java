package acquire.app.fragment.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import acquire.app.databinding.AppFragmentMenuBinding;
import acquire.app.fragment.main.menu.MainMenuAdapter;
import acquire.app.fragment.main.menu.MenuItem;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;

/**
 * A menu fragment.
 *
 * @author Janson
 * @date 2021/8/6 14:53
 */
public class MenuFragment extends BaseFragment {
    private List<MenuItem> menuItems;

    public static MenuFragment newInstance(List<MenuItem> menuItems) {
        MenuFragment fragment = new MenuFragment();
        fragment.menuItems = menuItems;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppFragmentMenuBinding binding = AppFragmentMenuBinding.inflate(inflater, container, false);
        binding.rvMenu.setAdapter(new MainMenuAdapter(mActivity,menuItems));
        return binding.getRoot();
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}

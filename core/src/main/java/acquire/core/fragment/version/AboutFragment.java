package acquire.core.fragment.version;

import android.icu.util.TimeZone;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.AppUtils;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.core.BuildConfig;
import acquire.core.R;
import acquire.core.databinding.CoreAboutItemBinding;
import acquire.core.databinding.CoreFragmentAboutBinding;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.BExtDevice;
import acquire.sdk.emv.BEmvProcessor;
import acquire.sdk.emv.BExtEmvProcessor;

/**
 * A {@link Fragment} that displays app information.
 *
 * @author Janson
 * @date 2019/1/28 10:22
 */
public class AboutFragment extends BaseFragment {
    private SimpleCallback callback;

    @NonNull
    public static AboutFragment newInstance(SimpleCallback callback) {
        AboutFragment fragment = new AboutFragment();
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoreFragmentAboutBinding binding = CoreFragmentAboutBinding.inflate(inflater, container, false);
        List<DeviceItem> items = new ArrayList<>();
        items.add(new DeviceItem(R.string.core_about_app_name, AppUtils.getAppName(mActivity)));
        items.add(new DeviceItem(R.string.core_about_app_version, AppUtils.getAppVersionName(mActivity)));
        items.add(new DeviceItem(R.string.core_about_nsdk_version, BDevice.getSdkVersion()));
        items.add(new DeviceItem(R.string.core_about_firmware_version, BDevice.getFirmwareVersion()));
        if (!BDevice.isCpos()) {
            //built-in
            items.add(new DeviceItem(R.string.core_about_emv_version, new BEmvProcessor().getVersion()));
        }
        //app creation time
        String creationTime = FormatUtils.formatTimeStamp(BuildConfig.RELEASE_TIMESTAMP, "yyyy/MM/dd HH:mm:ss");
        items.add(new DeviceItem(R.string.core_about_app_creation_time, creationTime + "\n" + TimeZone.getDefault().getDisplayName()));
        DeviceAdapter deviceAdapter = new DeviceAdapter(items);
        if (ExtServiceHelper.getInstance().isInit()) {
            //external
            ThreadPool.execute(() -> {
                int start = items.size();
                items.add(new DeviceItem(R.string.core_about_external_emv_version, new BExtEmvProcessor().getVersion()));
                items.add(new DeviceItem(R.string.core_about_external_device_version, BExtDevice.getVersion()));
                items.add(new DeviceItem(R.string.core_about_external_device_baudrate, BExtDevice.getBaudRateMode()));
                mActivity.runOnUiThread(()->deviceAdapter.notifyItemRangeChanged(start, items.size() - start));
            });
        }
        binding.rvVersion.setAdapter(deviceAdapter);
        binding.btnConfirm.setOnClickListener(view -> callback.result());
        return binding.getRoot();
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return callback;
    }


    /**
     * A {@link RecyclerView.Adapter} used to display infomation from {@link DeviceItem}
     *
     * @author Janson
     * @date 2020/12/4 13:50
     */
    private static class DeviceAdapter extends BaseBindingRecyclerAdapter<CoreAboutItemBinding> {
        private final List<DeviceItem> mDeviceItems;

        public DeviceAdapter(List<DeviceItem> mDeviceItems) {
            this.mDeviceItems = mDeviceItems;
        }

        @Override
        protected void bindItemData(CoreAboutItemBinding itemBinding, int position) {
            DeviceItem item = mDeviceItems.get(position);
            itemBinding.tvInfoTitle.setText(item.getTitle());
            itemBinding.tvInfoContent.setText(item.getContent());
        }

        @Override
        protected Class<CoreAboutItemBinding> getViewBindingClass() {
            return CoreAboutItemBinding.class;
        }

        @Override
        public int getItemCount() {
            return mDeviceItems.size();
        }

    }

    private static class DeviceItem {
        private final @StringRes int title;
        private final String content;

        public DeviceItem(@StringRes int title, String content) {
            this.title = title;
            this.content = content;
        }

        public @StringRes int getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

    }
}

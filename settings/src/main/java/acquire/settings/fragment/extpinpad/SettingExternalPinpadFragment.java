package acquire.settings.fragment.extpinpad;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.thread.CommonPoolExecutor;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.progress.ProgressDialog;
import acquire.core.constant.ParamsConst;
import acquire.core.tools.SelfCheckHelper;
import acquire.sdk.ConnectMode;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.dock.BaseDock;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.MenuDialogItem;
import acquire.settings.widgets.item.MenuDialogItem.MenuBean;
import acquire.settings.widgets.item.SwitchItem;
import acquire.settings.widgets.item.TextItem;

/**
 * Set external PIN pad
 *
 * @author Janson
 * @date 2021/11/25 10:44
 */
public class SettingExternalPinpadFragment extends BaseSettingFragment {
    private final Executor EXECUTOR = CommonPoolExecutor.newSinglePool("SettingExternalPinpadFragment");
    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_item_external_pinpad);
    }

    private boolean isDock;

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //External PIN pad
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_external_pinpad_item_enable)
                .setParamKey(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD)
                .setCheckChangeListener((buttonView, isChecked) -> {
                    EXECUTOR.execute(()-> ExtServiceHelper.getInstance().destroy());
                    refreshItems();
                })
                .create());
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD)) {
            //External connect
            List<MenuBean> menu = new ArrayList<>();
            menu.add(new MenuBean(getString(R.string.settings_connect_mode_serial_port)
                    , ConnectMode.SERIAL_PORT));
            menu.add(new MenuBean(getString(R.string.settings_connect_mode_usb)
                    , ConnectMode.USB));
            menu.add(new MenuBean(getString(R.string.settings_connect_mode_dock_serial_port)
                    , ConnectMode.DOCK_SERIAL_PORT));
            menu.add(new MenuBean(getString(R.string.settings_connect_mode_dock_usb1)
                    , ConnectMode.DOCK_USB1));
            menu.add(new MenuBean(getString(R.string.settings_connect_mode_dock_usb2)
                    , ConnectMode.DOCK_USB2));
            items.add(new MenuDialogItem.Builder(mActivity)
                    .setTitle(R.string.settings_external_pinpad_item_connect_mode)
                    .setParamKey(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD_CONNECT_MODE)
                    .setParamBean(menu)
                    .setOnChangeListener(index -> {
                        int connectMode = (int) menu.get(index).getValue();
                        if (isDock != isDockMode(connectMode)) {
                            refreshItems();
                        }
                        EXECUTOR.execute(()->ExtServiceHelper.getInstance().notifyConnect(connectMode));
                    })
                    .create());
            int mode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD_CONNECT_MODE);
            isDock = isDockMode(mode);
            if (isDock) {
                //Test dock
                items.add(new TextItem.Builder(mActivity)
                        .setTitle(R.string.settings_dock_test)
                        .setOnClickListener(v ->
                                ThreadPool.execute(() -> {
                                    if (new BaseDock().init()) {
                                        ToastUtils.showToast(R.string.settings_dock_serivce_test_success);
                                    }else{
                                        ToastUtils.showToast(R.string.settings_dock_serivce_test_failed);
                                    }
                                })
                        )
                        .create());
                //Dock set
                items.add(new TextItem.Builder(mActivity)
                        .setTitle(R.string.settings_dock_set)
                        .setOnClickListener(v -> {
                            if (!new BaseDock().startSettins()) {
                                ToastUtils.showToast(R.string.settings_dock_serivce_not_exist);
                            }
                        })
                        .create());
            }
            //Connect external PIN pad
            items.add(new TextItem.Builder(mActivity)
                    .setTitle(R.string.settings_external_pinpad_item_connect)
                    .setOnClickListener(v ->
                            new ProgressDialog.Builder(mActivity)
                                    .setContent(R.string.settings_external_pinpad_item_connect)
                                    .setShowListener(dialog -> {
                                        //Ping External PIN Pad
                                        EXECUTOR.execute(() -> {
                                            int connectMode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD_CONNECT_MODE);
                                            ExtServiceHelper.getInstance().destroy();
                                            boolean isConnect = ExtServiceHelper.getInstance().init(mActivity, connectMode);
                                            if (isConnect) {
                                                LoggerUtils.d("Re-init device");
                                                SelfCheckHelper.initDevice(mActivity);
                                                ToastUtils.showToast(R.string.settings_external_pinpad_test_success);
                                            } else {
                                                ToastUtils.showToast(R.string.settings_external_pinpad_test_failed);
                                            }

                                            ThreadPool.postDelayOnMain(dialog::dismiss, 500);
                                        });

                                    })
                                    .show()
                    )
                    .create());
        }
        return items;
    }

    private boolean isDockMode(int connectMode) {
        return ConnectMode.DOCK_USB1 == connectMode || ConnectMode.DOCK_USB2 == connectMode || ConnectMode.DOCK_SERIAL_PORT == connectMode;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}

package acquire.settings.fragment.key;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ToastUtils;
import acquire.base.widget.dialog.menu.MenuDialog;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.base.widget.dialog.progress.ProgressDialog;
import acquire.core.constant.Characters;
import acquire.core.constant.ParamsConst;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.FlyKeyHelper;
import acquire.sdk.pin.constant.KeyAlgorithmType;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.EditTextItem;
import acquire.settings.widgets.item.MenuDialogItem;
import acquire.settings.widgets.item.MenuDialogItem.MenuBean;
import acquire.settings.widgets.item.TextItem;

/**
 * A {@link Fragment} that configures terminal key.
 *
 * @author Janson
 * @date 2019/2/14 9:42
 */
public class SettingKeyManageFragment extends BaseSettingFragment {

    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_item_key);
    }

    @Override
    protected List<IItemView> getItems() {

        List<IItemView> items = new ArrayList<>();
        //algorithm type
        List<MenuBean> menu = new ArrayList<>();
        menu.add(new MenuBean(getString(R.string.settings_key_type_dukpt), KeyAlgorithmType.DUKPT));
        menu.add(new MenuBean(getString(R.string.settings_key_type_mksk), KeyAlgorithmType.MKSK));
        items.add(new MenuDialogItem.Builder(mActivity)
                .setTitle(getString(R.string.settings_key_item_algorithm))
                .setParamKey(ParamsConst.PARAMS_KEY_PINPAD_ALGORITHM_TYPE)
                .setParamBean(menu)
                .create());
        //Key index
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(getString(R.string.settings_key_item_index))
                .setParamKey(ParamsConst.PARAMS_KEY_PINPAD_MASTER_KEY_INDEX)
                .setDigits(Characters.NUMBER)
                .setInputMinLen(1)
                .setInputMaxLen(1)
                .create());
        //Pinpad timeout
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_key_item_pinpad_timeout)
                .setParamKey(ParamsConst.PARAMS_KEY_PINPAD_TIMEOUT)
                .setDigits(Characters.NUMBER)
                .setInputMinLen(1)
                .setInputMaxLen(2)
                .create());
        //FLY key
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_key_item_fly_key)
                .setMessage(R.string.settings_key_fly_key_summary)
                .setOnClickListener(v -> {
                    new MenuDialog.Builder(mActivity)
                            .setBackEnable(true)
                            .setTitle(R.string.settings_key_fly_key_dialog_title)
                            .setItems(getString(R.string.settings_key_fly_key_dialog_built_in), getString(R.string.settings_key_fly_key_dialog_external))
                            .setConfirmButton(index -> {
                                boolean externalPinpad = index == 1;
                                if (externalPinpad && !ExtServiceHelper.getInstance().isInit()) {
                                    ToastUtils.showToast(R.string.settings_key_fly_key_connect_external_pin_pad);
                                    return;
                                }
                                flykey(externalPinpad);
                            })
                            .setCancelButton(dialog -> {
                            })
                            .show();
                })
                .create());
        return items;
    }

    private void flykey(boolean externalPinpad) {
        new ProgressDialog.Builder(mActivity)
                .setContent(R.string.settings_key_fly_key_sending)
                .setShowListener(dialog ->
                        FlyKeyHelper.downloadMsterKey(mActivity, externalPinpad, new FlyKeyHelper.FlyKeyListener() {
                            @Override
                            public void onSuccess(@NonNull int[] indexes) {
                                mActivity.runOnUiThread(() -> {
                                    dialog.dismiss();
                                    if (indexes.length == 0) {
                                        ToastUtils.showToast(R.string.settings_key_fly_key_no_key);
                                    } else {
                                        ToastUtils.showToast(R.string.settings_key_fly_key_success);
                                    }
                                });

                            }

                            @Override
                            public void onFailed(FlyKeyHelper.FlyKeyErrorBean errorBean) {
                                mActivity.runOnUiThread(() -> {
                                    dialog.dismiss();
                                    new MessageDialog.Builder(mActivity)
                                            .setMessage(errorBean.getMessage())
                                            .setConfirmButton(dialog1 -> {
                                            })
                                            .show();
                                });
                            }


                            @Override
                            public void onException(Exception e) {
                                e.printStackTrace();
                                mActivity.runOnUiThread(() -> {
                                    dialog.dismiss();
                                    ToastUtils.showToast(R.string.settings_key_fly_key_exception);
                                });

                            }
                        })
                )
                .show();
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }


}

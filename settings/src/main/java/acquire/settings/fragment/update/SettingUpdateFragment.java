package acquire.settings.fragment.update;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import acquire.base.BaseApplication;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.thread.CommonPoolExecutor;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.base.widget.dialog.progress.ProgressDialog;
import acquire.core.constant.FileConst;
import acquire.core.constant.FileDir;
import acquire.core.constant.ParamsConst;
import acquire.core.tools.AppParamsImporter;
import acquire.core.tools.RemoteParamsUpdater;
import acquire.core.tools.SelfCheckHelper;
import acquire.database.service.RecordService;
import acquire.database.service.impl.RecordServiceImpl;
import acquire.sdk.FlyParameterHelper;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.SwitchItem;
import acquire.settings.widgets.item.TextItem;

/**
 * A {@link Fragment} to update terminal
 *
 * @author Janson
 * @date 2022/1/27 16:06
 */
public class SettingUpdateFragment extends BaseSettingFragment {
    private final static Executor FLY_SINGLE_EXECUTOR = CommonPoolExecutor.newSinglePool("FlyParameter");

    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_item_update);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //TOMS FLY params
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_paramfile_fly_parameter)
                .setMessage(R.string.settings_paramfile_fly_parameter_message)
                .setParamKey(ParamsConst.PARAMS_KEY_TOMS_FLY_PARAMETERS)
                .setCheckChangeListener((switchButton, value) -> {
                    refreshItems();
                    if (value) {
                        //bind fly parameter services
                        FLY_SINGLE_EXECUTOR.execute(() -> {
                            boolean result = FlyParameterHelper.getInstance().bind(mActivity);
                            if (result) {
                                FlyParameterHelper.getInstance().setParameterWatcher(mActivity, () ->
                                    new RemoteParamsUpdater().updateParams(BaseApplication.getAppContext())
                                );
                            } else {
                                mActivity.runOnUiThread(() -> {
                                    ToastUtils.showToast(acquire.core.R.string.core_device_fly_parameter_init_failed);
                                    switchButton.setChecked(false);
                                });
                            }
                        });
                    } else {
                        FLY_SINGLE_EXECUTOR.execute(() -> FlyParameterHelper.getInstance().unbind());
                    }
                })
                .create());
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_TOMS_FLY_PARAMETERS)) {
            //update parameters from TOMS FLY params
            items.add(new TextItem.Builder(mActivity)
                    .setTitle(R.string.settings_paramfile_update_by_fly_parameter)
                    .setOnClickListener(v -> new RemoteParamsUpdater().updateParams(mActivity))
                    .create());
        }

        //update parameters from PC
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_paramfile_update_by_pc)
                .setInterceptors(getSafePasswordInterceptor())
                .setOnClickListener(v ->
                        new MessageDialog.Builder(mActivity)
                                .setBackEnable(true)
                                .setMessage(R.string.settings_paramfile_import_params_prompt)
                                .setConfirmButton(dialog -> {
                                    File params = new File(FileDir.SHARE_PATH + FileConst.PARAMS);
                                    if (params.exists()) {
                                        try (FileInputStream input = new FileInputStream(params)) {
                                            if (input.available() > 0) {
                                                AppParamsImporter.importAppParams(input);
                                                ToastUtils.showToast(R.string.settings_paramfile_update_success);
                                            } else {
                                                ToastUtils.showToast(R.string.settings_paramfile_no_data);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            ToastUtils.showToast(R.string.settings_paramfile_update_fail);
                                        }
                                        params.delete();
                                    } else {
                                        ToastUtils.showToast(R.string.settings_paramfile_no_exist);
                                    }
                                })
                                .setCancelButton(dialog -> {
                                })
                                .show()
                )
                .create());
        //update merchants from PC
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_paramfile_update_pc_merchant)
                .setInterceptors(getSafePasswordInterceptor())
                .setOnClickListener(v ->
                        new MessageDialog.Builder(mActivity)
                                .setBackEnable(true)
                                .setMessage(R.string.settings_paramfile_import_merchants_prompt)
                                .setConfirmButton(dialog -> {
                                    RecordService recordService = new RecordServiceImpl();
                                    int count = recordService.getCount();
                                    if (count > 0) {
                                        ToastUtils.showToast(R.string.settings_paramfile_settle_first);
                                        return;
                                    }
                                    File params = new File(FileDir.SHARE_PATH + FileConst.MERCHANTS);
                                    if (params.exists()) {
                                        try (FileInputStream input = new FileInputStream(params)) {
                                            if (input.available() > 0) {
                                                AppParamsImporter.importMerchants(input);
                                                ToastUtils.showToast(R.string.settings_paramfile_update_success);
                                            } else {
                                                ToastUtils.showToast(R.string.settings_paramfile_no_data);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            ToastUtils.showToast(R.string.settings_paramfile_update_fail);
                                        }
                                        params.delete();
                                    } else {
                                        ToastUtils.showToast(R.string.settings_paramfile_no_exist);
                                    }
                                })
                                .setCancelButton(dialog -> {
                                })
                                .show()
                )
                .create());
        //Re-load EMV configure
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_paramfile_reload_emv_configuration)
                .setOnClickListener(v -> {
                    new MessageDialog.Builder(mActivity)
                            .setMessage(R.string.settings_paramfile_emv_aid_capk)
                            .setBackEnable(true)
                            .setConfirmButton(dialog ->
                                    new ProgressDialog.Builder(mActivity)
                                            .setContent(R.string.settings_paramfile_emv_aid_capk)
                                            .setShowListener(dlg -> {
                                                ThreadPool.execute(() -> {
                                                    if (SelfCheckHelper.loadEmvConfig(mActivity, ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_EXTERNAL_PINPAD), true)) {
                                                        ToastUtils.showToast(R.string.settings_emv_update_success);
                                                    } else {
                                                        ToastUtils.showToast(R.string.settings_emv_update_failed);
                                                    }
                                                    dlg.dismiss();
                                                });
                                            })
                                            .show()

                            )
                            .setCancelButton(dialog -> {
                            })
                            .show();


                })
                .create());
        return items;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}

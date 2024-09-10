package acquire.settings.fragment.clear;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ToastUtils;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.tools.SignatureDirManager;
import acquire.database.service.RecordService;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.database.service.impl.RecordServiceImpl;
import acquire.database.service.impl.ReversalDataServiceImpl;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.TextItem;

/**
 * A {@link Fragment} that dispalys the items of clearing data.
 *
 * @author Janson
 * @date 2019/2/13 9:43
 */
public class SettingClearFragment extends BaseSettingFragment {

    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_item_other);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //Clear Reversal
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_clear_down_item_clear_reversal_record)
                .setInterceptors(getSafePasswordInterceptor())
                .setOnClickListener(v->new MessageDialog.Builder(mActivity)
                        .setMessage(R.string.settings_clear_down_item_clear_reversal_record)
                        .setConfirmButton(dialog->{
                            new ReversalDataServiceImpl().delete();
                            ToastUtils.showToast(R.string.settings_clear_success);
                        })
                        .setCancelButton(dialog->{})
                        .show()
                )
                .create());
        //Clear Records
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_clear_down_item_clear_trans_record)
                .setInterceptors(getSafePasswordInterceptor())
                .setOnClickListener(v->new MessageDialog.Builder(mActivity)
                        .setMessage(R.string.settings_clear_down_item_clear_trans_record)
                        .setConfirmButton(dialog->{
                            RecordService recordService = new RecordServiceImpl();
                            recordService.deleteAll();
                            //delete all signature bmp
                            SignatureDirManager.clearAllSignature();

                            new MerchantServiceImpl().clearHalt();
                            ToastUtils.showToast(R.string.settings_clear_success);
                        })
                        .setCancelButton(dialog->{})
                        .show()
                )
                .create());
        //Clear Settle flag
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_clear_down_item_clear_settlement_marks)
                .setInterceptors(getSafePasswordInterceptor())
                .setOnClickListener(v->new MessageDialog.Builder(mActivity)
                        .setMessage(R.string.settings_clear_down_item_clear_settlement_marks)
                        .setConfirmButton(dialog->{
                            new MerchantServiceImpl().clearHalt();
                            ToastUtils.showToast(R.string.settings_clear_success);
                        })
                        .setCancelButton(dialog->{})
                        .show()
                )
                .create());
        return items;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }



}

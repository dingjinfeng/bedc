package acquire.settings.fragment.system;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.core.constant.ParamsConst;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.SwitchItem;

/**
 * A {@link Fragment} that configures trans support.
 *
 * @author Janson
 * @date 2019/2/13 16:12
 */
public class SystemSupportFragment extends BaseSettingFragment {

    @Override
    protected String getTitle() {
        return getString(R.string.settings_system_item_support_transaction);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //Sale
        items.add(new SwitchItem.Builder(mActivity)
                .setParamKey(ParamsConst.PARAMS_KEY_TRANS_SALE)
                .setTitle(R.string.settings_system_support_item_sale)
                .create());
        //VoidSale
        items.add(new SwitchItem.Builder(mActivity)
                .setParamKey(ParamsConst.PARAMS_KEY_TRANS_VOID)
                .setTitle(R.string.settings_system_support_item_void_sale)
                .create());
        //Refund
        items.add(new SwitchItem.Builder(mActivity)
                .setParamKey(ParamsConst.PARAMS_KEY_TRANS_REFUND)
                .setTitle(R.string.settings_system_support_item_refund)
                .create());
        //Balance
        items.add(new SwitchItem.Builder(mActivity)
                .setParamKey(ParamsConst.PARAMS_KEY_TRANS_BALANCE)
                .setTitle(R.string.settings_system_support_item_balance)
                .create());
        //Pre-auth
        items.add(new SwitchItem.Builder(mActivity)
                .setParamKey(ParamsConst.PARAMS_KEY_TRANS_PREAUTH)
                .setTitle(R.string.settings_system_support_item_preauth)
                .create());
        //Scan pay
        items.add(new SwitchItem.Builder(mActivity)
                .setParamKey(ParamsConst.PARAMS_KEY_TRANS_MOBILE_PAY)
                .setTitle(R.string.settings_system_support_item_scan_pay)
                .create());
        //Installment
        items.add(new SwitchItem.Builder(mActivity)
                .setParamKey(ParamsConst.PARAMS_KEY_TRANS_INSTALLMENT)
                .setTitle(R.string.settings_system_support_item_installment_pay)
                .create());
        return items;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}

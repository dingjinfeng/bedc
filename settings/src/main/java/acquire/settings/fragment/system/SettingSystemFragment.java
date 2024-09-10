package acquire.settings.fragment.system;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DisplayUtils;
import acquire.core.constant.Characters;
import acquire.core.constant.ParamsConst;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.AmountItem;
import acquire.settings.widgets.item.EditTextItem;
import acquire.settings.widgets.item.SwitchItem;
import acquire.settings.widgets.item.TextItem;

/**
 * A {@link Fragment} that configures system parameters.
 *
 * @author Janson
 * @date 2019/2/13 9:48
 */
public class SettingSystemFragment extends BaseSettingFragment {


    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_item_system);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //Support trans
        items.add(new TextItem.Builder(mActivity)
                .setTitle(getString(R.string.settings_system_item_support_transaction))
                .setOnClickListener(v -> mSupportDelegate.switchContent(new SystemSupportFragment()))
                .create());
        //Current Date/Time
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_time)
                .setOnClickListener(v -> mSupportDelegate.switchContent(new SystemTimeFragment()))
                .create());
        //Trace No
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_trace_no)
                .setParamKey(ParamsConst.PARAMS_KEY_BASE_TRACE_NO)
                .setDigits(Characters.NUMBER)
                .setInputMinLen(6)
                .setInputMaxLen(6)
                .create());

        //Refund max amount(long)
        items.add(new AmountItem.Builder(mActivity)
                .setParamKey(ParamsConst.PARAMS_KEY_BASE_MAX_REFUND_AMOUNT)
                .setTitle(R.string.settings_system_item_refund_max_money)
                .setMaxValue(9999999999.99d)
                .setMinValue(1.00d)
                .create());
        //Max record count
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_max_count)
                .setParamKey(ParamsConst.PARAMS_KEY_BASE_MAX_TRANS_COUNT)
                .setDigits(Characters.NUMBER)
                .setInputMinLen(1)
                .setInputMaxLen(4)
                .create());
        //Currency code
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_currency_code)
                .setParamKey(ParamsConst.PARAMS_KEY_BASE_CURRENCY_CODE)
                .setDigits(Characters.NUMBER)
                .setInputMinLen(3)
                .setInputMaxLen(3)
                .create());
        //Void card
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_void_card)
                .setParamKey(ParamsConst.PARAMS_KEY_OTHER_VOID_CARD)
                .create());
        //Void PIN
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_void_pin)
                .setParamKey(ParamsConst.PARAMS_KEY_OTHER_VOID_PIN)
                .create());

        //Second Screen overlay
        if (DisplayUtils.getDisplay2(mActivity) != null){
            items.add(new SwitchItem.Builder(mActivity)
                    .setTitle(R.string.settings_system_item_second_screen_top)
                    .setParamKey(ParamsConst.PARAMS_KEY_OTHER_SECOND_SCREEN_TOP)
                    .create());
        }
        //Third result
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_third_bill_show)
                .setParamKey(ParamsConst.PARAMS_KEY_OTHER_THRID_BILL_SHOW)
                .create());
        //Enable tip
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_input_tip)
                .setParamKey(ParamsConst.PARAMS_KEY_OTHER_TIP_INPUT)
                .create());
        return items;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}

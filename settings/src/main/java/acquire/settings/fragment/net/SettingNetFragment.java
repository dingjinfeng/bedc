package acquire.settings.fragment.net;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.core.constant.Characters;
import acquire.core.constant.ParamsConst;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.EditTextItem;
import acquire.settings.widgets.item.SwitchItem;

/**
 * A {@link Fragment} that configures network communication information.
 *
 * @author Janson
 * @date 2019/4/24 9:46
 */
public class SettingNetFragment extends BaseSettingFragment {
    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_item_communication);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items=new ArrayList<>();
        //Communication timeout
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_net_item_timeout)
                .setDigits(Characters.NUMBER)
                .setInputMinLen(1)
                .setInputMaxLen(3)
                .setParamKey(ParamsConst.PARAMS_KEY_COMM_TIMEOUT)
                .create());
        //SSL
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_net_item_ssl)
                .setParamKey(ParamsConst.PARAMS_KEY_COMM_USE_SSL)
                .create());
        //Server address
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_net_item_url)
                .setInputMinLen(1)
                .setParamKey(ParamsConst.PARAMS_KEY_COMM_SERVER_ADDRESS)
                .create());
        //Server port
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_net_item_port)
                .setParamKey(ParamsConst.PARAMS_KEY_COMM_PORT)
                .setDigits(Characters.NUMBER)
                .setInputMinLen(1)
                .setInputMaxLen(6)
                .create());
        //TPDU
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_net_item_tpdu)
                .setParamKey(ParamsConst.PARAMS_KEY_COMM_TPDU)
                .setDigits(Characters.NUMBER)
                .setInputMaxLen(10)
                .setInputMinLen(10)
                .create());
        //NII
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_net_item_nii)
                .setParamKey(ParamsConst.PARAMS_KEY_COMM_NII)
                .setDigits(Characters.NUMBER)
                .setInputMaxLen(3)
                .setInputMinLen(3)
                .create());
        return items;
    }


    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}

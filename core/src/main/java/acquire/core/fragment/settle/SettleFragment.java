package acquire.core.fragment.settle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.core.R;
import acquire.core.constant.SettleAttr;
import acquire.core.constant.TransType;
import acquire.core.databinding.CoreFragmentSettleBinding;
import acquire.core.databinding.CoreSettleItemBinding;
import acquire.core.databinding.CoreSettleMerchantItemBinding;
import acquire.core.tools.StatisticsUtils;
import acquire.core.tools.TransUtils;
import acquire.database.model.Merchant;
import acquire.database.service.MerchantService;
import acquire.database.service.RecordService;
import acquire.database.service.impl.MerchantServiceImpl;
import acquire.database.service.impl.RecordServiceImpl;

/**
 * A settle {@link androidx.fragment.app.Fragment}
 *
 * @author Janson
 * @date 2021/7/20 10:07
 */
public class SettleFragment extends BaseFragment {
    private final RecordService recordService = new RecordServiceImpl();
    private final MerchantService merchantService = new MerchantServiceImpl();
    private CoreFragmentSettleBinding binding;
    private FragmentCallback<List<Merchant>> callback;

    public static SettleFragment newInstance(FragmentCallback<List<Merchant>> callback) {
        SettleFragment fragment = new SettleFragment();
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentSettleBinding.inflate(inflater,container,false);
        List<Merchant> merchants = merchantService.findAll();
        SettleMerchantAdapter adapter = new SettleMerchantAdapter(merchants);
        binding.rvMerchants.setAdapter(adapter);

        binding.btnSettle.setOnClickListener(v->{
            if (recordService.getCount() == 0 || adapter.getSelects().size() == 0) {
                ToastUtils.showToast(R.string.core_settle_no_record_to_settled);
                return;
            }
            callback.onSuccess(adapter.getSelects());
        });
        binding.cbAll.setOnClickListener(v-> adapter.setAll(binding.cbAll.isChecked()));
        return binding.getRoot();
    }

    @Override
    public FragmentCallback<List<Merchant>> getCallback() {
        return callback;
    }

    class SettleMerchantAdapter extends BaseBindingRecyclerAdapter<CoreSettleMerchantItemBinding> {
        private final List<Merchant> merchants;
        private final List<Merchant> selects = new ArrayList<>();

        public SettleMerchantAdapter(List<Merchant> merchants) {
            this.merchants = merchants;
        }

        public void setAll(boolean checked){
            selects.clear();
            if (checked){
                selects.addAll(merchants);
            }
            notifyDataSetChanged();
        }

        public List<Merchant> getSelects() {
            return selects;
        }

        @Override
        protected void bindItemData(CoreSettleMerchantItemBinding itemBinding, int position) {
            Merchant merchant = merchants.get(position);
            itemBinding.cbCardorg.setText(merchant.getCardOrg());
            //Add transaction to be settled
            List<String> transTypes = new ArrayList<>();
            transTypes.add(TransType.TRANS_SALE);
            transTypes.add(TransType.TRANS_VOID_SALE);
            transTypes.add(TransType.TRANS_REFUND);
            transTypes.add(TransType.TRANS_AUTH_COMPLETE);
            transTypes.add(TransType.TRANS_VOID_AUTH_COMPLETE);
            transTypes.add(TransType.TRANS_INSTALLMENT);
            transTypes.add(TransType.TRANS_VOID_INSTALLMENT);
            transTypes.add(TransType.TRANS_SCAN_PAY);
            transTypes.add(TransType.TRANS_QR_CODE);
            transTypes.add(TransType.TRANS_QR_REFUND);
            //Count the total amount of these transactions
            long totalAmount = 0;
            long totalNumber = 0;
            Map<String,long[]> totalMap =
                    StatisticsUtils.getTotalAmtNum(merchant.getMid(),merchant.getTid());
            List<TransItem> items = new ArrayList<>();
            for (String transType : transTypes) {
                long[] value = totalMap.get(transType);
                if (value == null){
                    value = new long[]{0,0};
                }
                long amount = value[0];
                long number = value[1];
                String name = TransUtils.getName(transType);
                int settleAttr = TransUtils.getSettleAttr(transType);
                switch (settleAttr) {
                    case SettleAttr.PLUS:
                        totalAmount += amount;
                        break;
                    case SettleAttr.REDUCE:
                        totalAmount -= amount;
                        amount = -amount;
                        break;
                    default:
                        continue;
                }
                items.add(new TransItem(name,number,amount));
                totalNumber +=number;
            }
            items.add(new TransItem(mActivity.getString(R.string.core_settle_total),totalNumber,totalAmount));
            //Transaction items
            itemBinding.rvRecords.setAdapter(new SettleAdapter(items));

            itemBinding.cbCardorg.setChecked(selects.contains(merchant));
            itemBinding.cbCardorg.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked){
                    if (!selects.contains(merchant)){
                        selects.add(merchant);
                        if (selects.size() == merchants.size()){
                            binding.cbAll.setChecked(true);
                        }
                    }
                }else{
                    selects.remove(merchant);
                    binding.cbAll.setChecked(false);
                }
            });
        }

        @Override
        protected Class<CoreSettleMerchantItemBinding> getViewBindingClass() {
            return CoreSettleMerchantItemBinding.class;
        }

        @Override
        public int getItemCount() {
            return merchants.size();
        }
    }


    /**
     * Settle adapter
     *
     * @author Janson
     * @date 2021/3/15 13:48
     */
    static class SettleAdapter extends BaseBindingRecyclerAdapter<CoreSettleItemBinding> {
        private final List<TransItem> items;

        SettleAdapter(List<TransItem> items) {
            this.items = items;
        }

        @Override
        protected void bindItemData(CoreSettleItemBinding itemBinding, int position) {
            if (position % 2 == 0) {
                itemBinding.getRoot().setBackgroundResource( R.color.core_settle_item_gray);
            }
            TransItem item = items.get(position);
            itemBinding.tvName.setText(item.name);
            itemBinding.tvNumber.setText(String.format(Locale.getDefault(), "%d", item.number));
            itemBinding.tvAmount.setText(FormatUtils.formatAmount(item.amount));
        }

        @Override
        protected Class<CoreSettleItemBinding> getViewBindingClass() {
            return CoreSettleItemBinding.class;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    /**
     * Transaction settle info
     *
     * @author Janson
     * @date 2020/6/23 19:16
     */
    static class TransItem {
        private final String name;
        private final long number;
        private final long amount;

        TransItem(String name, long number, long amount) {
            this.name = name;
            this.number = number;
            this.amount = amount;
        }
    }
}

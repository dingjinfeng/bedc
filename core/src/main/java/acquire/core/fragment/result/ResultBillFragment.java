package acquire.core.fragment.result;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentResultBillBinding;
import acquire.core.databinding.CoreResultBillItemBinding;
import acquire.core.tools.CurrencyCodeProvider;
import acquire.database.model.Record;

/**
 * A {@link Fragment} that displays the result bill information
 *
 * @author Janson
 * @date 2019/1/23 10:22
 */
public class ResultBillFragment extends BaseFragment {
    /**
     * The record to be printed
     */
    private Record mRecord;
    /**
     * Result callback
     */
    private SimpleCallback mCallback;


    private CoreFragmentResultBillBinding binding;

    @NonNull
    public static ResultBillFragment newInstance(Record record, SimpleCallback callback) {
        ResultBillFragment fragment = new ResultBillFragment();
        fragment.mCallback = callback;
        fragment.mRecord = record;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentResultBillBinding.inflate(inflater, container, false);
        binding.rvDetail.setAdapter(new BillAdapter(getItems(mRecord)));
        //count down timer
        CountDownTimer timer = new CountDownTimer(5 * 1000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int countDown = (int) Math.ceil(millisUntilFinished / 1000f);
                String content = String.format(Locale.getDefault(),"%s(%d)",
                        mActivity.getString(R.string.base_done), countDown );
                binding.btnDone.setText(content);
            }

            @Override
            public void onFinish() {
                if (mCallback != null) {
                    mCallback.onSuccess(null);
                    mCallback = null;
                }
            }
        }.start();

        //done button
        binding.btnDone.setOnClickListener(v -> {
            timer.cancel();
            if (mCallback != null) {
                mCallback.onSuccess(null);
                mCallback = null;
            }
        });

        return binding.getRoot();
    }

    private List<BillItem> getItems(Record record) {
        List<BillItem> items = new ArrayList<>();
        //merchant id
        items.add(new BillItem(R.string.core_result_bill_item_mid, record.getMid()));
        //merchant id
        items.add(new BillItem(R.string.core_result_bill_item_tid, record.getTid()));
        //amount
        String identify = CurrencyCodeProvider.getCurrencySymbol(record.getCurrencyCode());
        items.add(new BillItem(R.string.core_result_bill_item_amount, identify + FormatUtils.formatAmount(record.getAmount())));

        if (record.getBizOrderNo()!= null) {
            //qr code
            items.add(new BillItem(R.string.core_result_bill_item_qr_orde, record.getBizOrderNo()));
        }
        if (record.getCardNo() != null) {
            //Card No.
            items.add(new BillItem(R.string.core_result_bill_item_card_num, FormatUtils.maskCardNo(record.getCardNo())));
        }
        //trace num
        items.add(new BillItem(R.string.core_result_bill_item_trace_no, record.getTraceNo()));
        //auth code
        if (record.getAuthCode() != null){
            items.add(new BillItem(R.string.core_result_bill_item_auth_code, record.getAuthCode()));
        }
        //reference num
        if (record.getReferNo() != null){
            items.add(new BillItem(R.string.core_result_bill_item_reference_no, record.getReferNo()));
        }
        //data/time
        items.add(new BillItem(R.string.core_result_bill_item_time, FormatUtils.formatTimeStamp(record.getDate() + record.getTime())));
        return items;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return mCallback;
    }


    static class BillAdapter extends BaseBindingRecyclerAdapter<CoreResultBillItemBinding> {
        private final List<BillItem> items;

        public BillAdapter(List<BillItem> items) {
            this.items = items;
        }


        @Override
        protected void bindItemData(CoreResultBillItemBinding itemBinding, int position) {
            BillItem item = items.get(position);
            //title
            itemBinding.tvTitle.setText(item.getTitle());
            //content
            itemBinding.tvContent.setText(item.getContent());
        }

        @Override
        protected Class<CoreResultBillItemBinding> getViewBindingClass() {
            return CoreResultBillItemBinding.class;
        }


        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    /**
     * Item of {@link ResultBillFragment}
     *
     * @author Janson
     * @date 2019/1/23 14:42
     */
    public static class BillItem {
        private final @StringRes int title;
        private final String content;

        public BillItem(@StringRes int title, String content) {
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

package acquire.core.fragment.record;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.core.R;
import acquire.core.constant.TransStatus;
import acquire.core.databinding.CoreFragmentRecordDetailBinding;
import acquire.core.databinding.CoreRecordDetailItemBinding;
import acquire.core.tools.CurrencyCodeProvider;
import acquire.core.tools.TransUtils;
import acquire.database.model.Record;

/**
 * A {@link Fragment} that displays the record detail.
 *
 * @author Janson
 * @date 2019/7/25 11:23
 */
public class RecordDetailFragment extends BaseFragment {
    private FragmentCallback<Void> mCallback;
    private Record mRecord;
    private String buttonText;

    @NonNull
    public static RecordDetailFragment newInstance(String buttonText, Record record, FragmentCallback<Void> callback) {
        RecordDetailFragment fragment = new RecordDetailFragment();
        fragment.mCallback = callback;
        fragment.mRecord = record;
        fragment.buttonText = buttonText;
        return fragment;
    }

    @NonNull
    public static RecordDetailFragment newInstance(Record record, FragmentCallback<Void> callback) {
        RecordDetailFragment fragment = new RecordDetailFragment();
        fragment.mCallback = callback;
        fragment.mRecord = record;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoreFragmentRecordDetailBinding binding = CoreFragmentRecordDetailBinding.inflate(inflater, container, false);
        String identify = CurrencyCodeProvider.getCurrencySymbol(mRecord.getCurrencyCode());
        String formatAmt = identify + FormatUtils.formatAmount(mRecord.getAmount());
        binding.tvAmount.setText(formatAmt);
        binding.tvTransName.setText(TransUtils.getName(mRecord.getTransType()));
        binding.tvTransStatus.setText(TransStatus.getDescription(mRecord.getStatus()));
        List<DetailItem> items = new ArrayList<>();
        items.add(new DetailItem(R.string.core_record_detail_name_pan, FormatUtils.maskCardNo(mRecord.getCardNo())));
        items.add(new DetailItem(R.string.core_record_detail_name_organization, mRecord.getCardOrg()));
        items.add(new DetailItem(R.string.core_record_detail_name_trace, mRecord.getTraceNo()));
        items.add(new DetailItem(R.string.core_record_detail_name_batch, mRecord.getBatchNo()));
        items.add(new DetailItem(R.string.core_record_detail_name_refnum, mRecord.getReferNo()));
        items.add(new DetailItem(R.string.core_record_detail_name_authcode, mRecord.getAuthCode()));
        items.add(new DetailItem(R.string.core_record_detail_name_time, FormatUtils.formatTimeStamp(mRecord.getDate() + mRecord.getTime())));
        binding.rvDetail.setAdapter(new DetailAdapter(items));
        if (buttonText != null) {
            binding.btnConfirm.setText(buttonText);
        }
        binding.btnConfirm.setOnClickListener(v -> mCallback.onSuccess(null));
        return binding.getRoot();
    }


    @Override
    public FragmentCallback<Void> getCallback() {
        return mCallback;
    }

    private static class DetailAdapter extends BaseBindingRecyclerAdapter<CoreRecordDetailItemBinding> {
        private final List<DetailItem> items;

        DetailAdapter(List<DetailItem> items) {
            this.items = items;
        }

        @Override
        protected void bindItemData(@NonNull CoreRecordDetailItemBinding itemBinding, int position) {
            DetailItem item = items.get(position);
            itemBinding.tvName.setText(item.name);
            itemBinding.tvValue.setText(item.value);
        }

        @Override
        protected Class<CoreRecordDetailItemBinding> getViewBindingClass() {
            return CoreRecordDetailItemBinding.class;
        }


        @Override
        public int getItemCount() {
            return items.size();
        }
    }


    private static class DetailItem {
        private @StringRes
        final int name;
        private final String value;

        DetailItem(@StringRes int name, String value) {
            this.name = name;
            this.value = value;
        }
    }

}

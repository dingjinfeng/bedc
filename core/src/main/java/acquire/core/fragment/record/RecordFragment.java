package acquire.core.fragment.record;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.base.widget.dialog.date.DateRangeDialog;
import acquire.core.constant.TransStatus;
import acquire.core.databinding.CoreFragmentEmptyRecordBinding;
import acquire.core.databinding.CoreFragmentRecordBinding;
import acquire.core.databinding.CoreRecordItemBinding;
import acquire.core.tools.CurrencyCodeProvider;
import acquire.core.tools.TransUtils;
import acquire.database.model.Record;
import acquire.database.service.RecordService;
import acquire.database.service.impl.RecordServiceImpl;


/**
 * A {@link Fragment} that displays the records
 *
 * @author Janson
 * @date 2019/7/25 11:20
 */
public class RecordFragment extends BaseFragment {
    private CoreFragmentRecordBinding binding;
    private FragmentCallback<Record> mCallback;
    private String[] mTransTypes;
    private DateRangeDialog dateRangeDialog;
    private final RecordService recordService = new RecordServiceImpl();
    private final static int PAGE_SIZE = 16;
    private int pageIndex = 0;
    private String mStartDate;
    private String mEndDate;
    private int[] mStatus;

    @NonNull
    public static RecordFragment newInstance(FragmentCallback<Record> callback) {
        RecordFragment fragment = new RecordFragment();
        fragment.mCallback = callback;
        return fragment;
    }

    /**
     * create a {@link RecordFragment}
     *
     * @param transTypes Used to filter records.Only records matching transTypes can be displayed
     * @param status     Used to filter records.Only records matching status can be displayed
     * @param callback   selection result callback
     * @return {@link RecordFragment}
     */
    @NonNull
    public static RecordFragment newInstance(String[] transTypes, int[] status,FragmentCallback<Record> callback) {
        RecordFragment fragment = new RecordFragment();
        fragment.mCallback = callback;
        fragment.mTransTypes = transTypes;
        fragment.mStatus = status;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        List<Record> records = recordService.findByPageDesc(mTransTypes, mStatus, null, null, 0, PAGE_SIZE);
        if (records.size() == 0) {
            //no record
            CoreFragmentEmptyRecordBinding emptyRecordBinding = CoreFragmentEmptyRecordBinding.inflate(inflater, container, false);
            return emptyRecordBinding.getRoot();
        }
        binding = CoreFragmentRecordBinding.inflate(inflater, container, false);
        DisplayUtils.fitsWindowStatus(binding.llToolbar);
        binding.ivBack.setOnClickListener(v -> mActivity.onBackPressed());
        //adpater
        RecordAdapter adapter = new RecordAdapter(records);
        binding.rvRecords.setAdapter(adapter);

        //search a record
        binding.tvSearch.setOnClickListener(v ->
                mSupportDelegate.switchContent(SearchFragment.newInstance(new FragmentCallback<Record>() {
                    @Override
                    public void onSuccess(Record record) {
                        if (mCallback != null) {
                            mCallback.onSuccess(record);
                        }
                    }

                    @Override
                    public void onFail(int errorType, String errorMsg) {
                        mSupportDelegate.popBackFragment(1);
                    }
                }, mStatus, mTransTypes))
        );
        //pre-load when RecyclerView scrolling.
        int pageMax = recordService.getCount() / PAGE_SIZE + (recordService.getCount() % PAGE_SIZE > 0 ? 1 : 0);
        binding.rvRecords.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //scroll to next
                LinearLayoutManager llManager = (LinearLayoutManager) binding.rvRecords.getLayoutManager();
                if (llManager != null) {
                    int lastVisibleItem = llManager.findLastVisibleItemPosition();
                    int itemCount = llManager.getItemCount();
                    if (lastVisibleItem >= itemCount - 2 && dy > 0) {
                        //load next page
                        if (pageIndex < pageMax) {
                            new Handler(Looper.getMainLooper()).post(() -> loadNextPage(adapter));
                        }
                    }
                }
            }
        });
        //select the date ranage
        binding.llDate.setOnClickListener(v -> {
            if (dateRangeDialog == null) {
                dateRangeDialog = new DateRangeDialog.Builder(mActivity)
                        .setBackEnable(true)
                        .setConfirmListener((startDate, endDate) -> selectDateRecords(adapter, startDate, endDate))
                        .endToday()
                        .create();
            }
            dateRangeDialog.show();
        });
        return binding.getRoot();
    }

    /**
     * load next page records
     *
     * @param adapter records view adapter
     */
    private void loadNextPage(RecordAdapter adapter) {
        //load next page
        pageIndex++;
        List<Record> nextRecords = recordService.findByPageDesc(mTransTypes, mStatus, mStartDate, mEndDate, pageIndex, PAGE_SIZE);
        if (nextRecords != null) {
            List<Record> records = adapter.getRecords();
            if (records == null) {
                records = nextRecords;
            } else {
                records.addAll(nextRecords);
            }
            adapter.update(records);
            LoggerUtils.d("Loading next page -> " + pageIndex);
        }
    }

    /**
     * select the date ranage and update records
     *
     * @param adapter   records view adapter
     * @param startDate start date [yyyy,MM,dd]
     * @param endDate   end date [yyyy,MM,dd]
     */
    private void selectDateRecords(RecordAdapter adapter, int[] startDate, int[] endDate) {
        if (startDate == null && endDate == null) {
            return;
        }
        String start = null;
        String startFormat = "";
        if (startDate != null) {
            start = String.format(Locale.getDefault(), "%04d%02d%02d", startDate[0], startDate[1], startDate[2]);
            startFormat = FormatUtils.formatTimeStamp(start, "yyyyMMdd", "yyyy/MM/dd");
        }
        String end = null;
        String endFormat = "";
        if (endDate != null) {
            end = String.format(Locale.getDefault(), "%04d%02d%02d", endDate[0], endDate[1], endDate[2]);
            endFormat = FormatUtils.formatTimeStamp(end, "yyyyMMdd", "yyyy/MM/dd");
        }

        this.mStartDate = start;
        this.mEndDate = end;
        pageIndex = 0;
        adapter.update(recordService.findByPageDesc(mTransTypes, mStatus, mStartDate, mEndDate, pageIndex, PAGE_SIZE));
        binding.tvDate.setText(startFormat + " - " + endFormat);
    }

    @Override
    public FragmentCallback<Record> getCallback() {
        return mCallback;
    }

    /**
     * record list adapter
     *
     * @author Janson
     * @date 2021/11/25 11:36
     */
    private class RecordAdapter extends BaseBindingRecyclerAdapter<CoreRecordItemBinding> {
        private List<Record> records;

        public RecordAdapter(List<Record> records) {
            this.records = records;
        }

        public void update(List<Record> records) {
            this.records = records;
            notifyDataSetChanged();
        }

        public List<Record> getRecords() {
            return records;
        }

        @Override
        protected void bindItemData(CoreRecordItemBinding itemBinding, int position) {
            final Record record = records.get(position);
            if (record != null) {
                //Trans name
                String transName = TransUtils.getName(record.getTransType());
                itemBinding.tvTransType.setText(transName);

                //status
                if (record.getStatus() != TransStatus.SUCCESS) {
                    itemBinding.tvStatus.setVisibility(View.VISIBLE);
                    itemBinding.tvStatus.setText("[" + TransStatus.getDescription(record.getStatus()) + "]");
                } else {
                    itemBinding.tvStatus.setVisibility(View.GONE);
                }
                //Amount
                String amt = FormatUtils.formatAmount(record.getAmount(), 2, "");
                String identify = CurrencyCodeProvider.getCurrencySymbol(record.getCurrencyCode());
                amt = identify + amt;
                itemBinding.tvAmount.setText(amt);
                //Time
                itemBinding.tvTime.setText(FormatUtils.formatTimeStamp(record.getDate() + record.getTime()));
                //Trace
                itemBinding.tvTraceNo.setText(record.getTraceNo());
            }
            //click
            itemBinding.getRoot().setOnClickListener(v -> mCallback.onSuccess(record));
        }

        @Override
        protected Class<CoreRecordItemBinding> getViewBindingClass() {
            return CoreRecordItemBinding.class;
        }

        @Override
        public int getItemCount() {
            if (records == null) {
                return 0;
            }
            return records.size();
        }
    }
}

package acquire.database.service.impl;


import java.util.List;

import acquire.base.utils.LoggerUtils;
import acquire.database.AcquireDatabase;
import acquire.database.dao.MerchantDao;
import acquire.database.model.Merchant;
import acquire.database.service.MerchantService;

/**
 * The implemented class of{@link MerchantService}.
 * <p>It is used to access the merchant table according to business needs.
 *
 * @author Janson
 * @date 2021/1/5 17:17
 */
public class MerchantServiceImpl implements MerchantService {
    private final MerchantDao mMerchantDao;

    public MerchantServiceImpl() {
        mMerchantDao = AcquireDatabase.getInstance().merchantDao();
    }

    @Override
    public boolean add(Merchant merchant) {
        if (merchant == null) {
            LoggerUtils.e("Add Merchant Error：Merchant is null!");
            return false;
        }
        if (find(merchant.getMid(),merchant.getTid()) != null) {
            LoggerUtils.e(" Redundant merchant[mid = "+merchant.getMid()+",tid = "+merchant.getTid()+"].");
            return false;
        }
        return mMerchantDao.insert(merchant) > 0;
    }


    @Override
    public List<Merchant> findAll() {
        return mMerchantDao.findAll();
    }
    @Override
    public Merchant find(String mid, String tid) {
        if (mid == null) {
            LoggerUtils.e("Mid is null!");
            return null;
        }
        if (tid == null) {
            LoggerUtils.e("Tid is null!");
            return null;
        }
        List<Merchant> merchants = mMerchantDao.find(mid,tid);
        return merchants.size() > 0 ? merchants.get(0) : null;
    }

    @Override
    public Merchant find(String cardOrg) {
        if (cardOrg == null) {
            LoggerUtils.e("Card Organization is null!");
            return null;
        }
        List<Merchant> merchants = mMerchantDao.find(cardOrg);
        return merchants.size() > 0 ? merchants.get(0) : null;
    }

    @Override
    public boolean deleteAll() {
        return mMerchantDao.deleteAll() >= 0;
    }

    @Override
    public boolean delete(String mid, String tid) {
        Merchant merchant = find(mid,tid);
        if (merchant == null) {
            LoggerUtils.e(" No such merchant[mid = "+mid+", tid = "+tid+"].");
            return true;
        }
        return mMerchantDao.delete(merchant.getId())>=0;
    }

    @Override
    public boolean update(Merchant merchant) {
        if (merchant == null) {
            LoggerUtils.e("Merchant is null");
            return false;
        }
        if (merchant.getId() <= 0) {
            Merchant tmp = find(merchant.getMid(),merchant.getTid());
            if (tmp == null) {
                LoggerUtils.e("Merchant is not exist[mid = "+merchant.getMid()+",tid = "+merchant.getTid()+"]." );
                return false;
            }
            merchant.setId(tmp.getId());
        }
        return mMerchantDao.update(merchant) >= 0;
    }

    @Override
    public void clearHalt() {
        List<Merchant> merchants = mMerchantDao.findAll();
        for (Merchant merchant : merchants) {
            merchant.setSettleStep(0);
            mMerchantDao.update(merchant);
        }
    }

    @Override
    public void clearHalt(List<Merchant> merchants) {
        for (Merchant merchant : merchants) {
            merchant.setSettleStep(0);
            mMerchantDao.update(merchant);
        }
    }
}

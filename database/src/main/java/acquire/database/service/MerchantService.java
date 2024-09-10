package acquire.database.service;

import java.util.List;

import acquire.database.dao.MerchantDao;
import acquire.database.model.Merchant;

/**
 * The service interface that uses {@link MerchantDao} to access to the merchant table.
 *
 * @author Janson
 * @date 2021/3/12 11:10
 */
public interface MerchantService {

    /**
     * Add a merchant
     */
    boolean add(Merchant merchant);
    /**
     * find all merchants
     */
    List<Merchant> findAll();
    /**
     * find a merchant by merchant id and terminal id
     */
    Merchant find(String mid, String tid);
    /**
     * find a merchant by card organization
     */
    Merchant find(String cardOrg);
    /**
     * Delete all merchants
     */
    boolean deleteAll();

    /**
     * Delete a merchant by merchant id  and terminal id
     */
    boolean delete(String mid, String tid);

    /**
     * update a merchant
     */
    boolean update(Merchant merchant);

    /**
     * clear all merchants halt flag
     */
    void clearHalt();

    /**
     * clear merchants halt flag
     * @param merchants  merchants to clear halt flag
     */
    void clearHalt(List<Merchant> merchants);
}

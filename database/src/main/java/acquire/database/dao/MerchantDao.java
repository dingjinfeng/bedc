package acquire.database.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import acquire.database.model.Merchant;

/**
 * Access to merchant data
 *
 * @author Janson
 * @date 2021/3/12 11:10
 */
@Dao
public interface MerchantDao {
    /**
     * Inserts a merchant to the merchant table
     *
     * @param merchant merchant infomation
     * @return the ID of the new record. If -1,failed.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Merchant merchant);

    /**
     * Deletes a merchant by ID
     *
     * @param id record ID
     * @return the count of deleted records
     */
    @Query("DELETE FROM T_MERCHANT WHERE ID = (:id)")
    int delete(long id);

    /**
     * Deletes all merchants
     *
     * @return the count of deleted records
     */
    @Query("DELETE FROM T_MERCHANT ")
    int deleteAll();

    /**
     * update a merchant
     *
     * @param merchant new merchant infomation
     * @return the count of updated records
     */
    @Update()
    int update(Merchant merchant);


    /**
     * find all merchants
     *
     * @return all merchants
     */
    @Query("SELECT * FROM T_MERCHANT")
    List<Merchant> findAll();

    /**
     * find the merchant by MID and TID
     *
     * @param mid merchant's ID
     * @param tid terminal's ID
     * @return the list of found merchants
     */
    @Query("SELECT * FROM T_MERCHANT WHERE MID = :mid and TID =:tid")
    List<Merchant> find(String mid, String tid);
    /**
     * find the merchant by card organization
     *
     * @param cardOrg card organization
     * @return the list of found merchants
     */
    @Query("SELECT * FROM T_MERCHANT WHERE CARD_ORGANIZATION = :cardOrg")
    List<Merchant> find(String cardOrg);
}

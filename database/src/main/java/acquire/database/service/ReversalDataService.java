package acquire.database.service;

import acquire.database.dao.ReversalDataDao;
import acquire.database.model.ReversalData;

/**
 * The service interface that uses {@link ReversalDataDao} to access to the reversal table.
 *
 * @author Janson
 * @date 2021/1/5 17:20
 */
public interface ReversalDataService {

    /**
     * Add a reversal data
     */
    boolean add(ReversalData reversalData) ;

    /**
     * Delete a reversal data
     */
    boolean delete();

    /**
     * Get the reversal data
     */
    ReversalData getReverseRecord();


    /**
     * update field55
     */
    boolean updateField55(String field55);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workshop2.persistencelayer;

import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workshop2.domain.Account;
import workshop2.domain.AccountType;
import workshop2.interfacelayer.DatabaseConnection;

/**
 *
 * @author hwkei
 */
public class PersistenceService {
    private static final Logger log = LoggerFactory.getLogger(PersistenceService.class);
    private final EntityManager entityManager;
    private final GenericDaoImpl accountDao;
    private final GenericDaoImpl accountTypeDao;
    
    public PersistenceService() {
        entityManager = DatabaseConnection.getInstance().getEntityManager();
        accountDao = new GenericDaoImpl(Account.class, entityManager);
        accountTypeDao = new GenericDaoImpl(AccountType.class, entityManager);
    }
    
    public void createAccount(Account account, Long accountTypeId) {
        try {
            entityManager.getTransaction().begin();
            // Retrieve the accountType            
            AccountType accountType = (AccountType)accountTypeDao.findById(accountTypeId);                
            // Add the AccountType to the given Account (temporary workarround)
            account.setAccountType(accountType);       
            accountDao.persist(account);            
            entityManager.getTransaction().commit();            
        } catch (Exception ex) {
            entityManager.getTransaction().rollback();
            System.out.println("Transactie is niet uitgevoerd!");            
            // Exception doorgooien of FailedTransaction oid opgooien?
        } finally {
            // Always clear the persistence context to prevent increasing memory ????
            entityManager.clear();
        }
    }
    
    public Optional<Account> findAccountByUserName(String userName) {
        Account resultAccount;
        try {
            Query queryAccountByUserName = entityManager.createNamedQuery("findAccountByUserName");
            queryAccountByUserName.setParameter("username", userName);
            resultAccount = (Account)queryAccountByUserName.getSingleResult();
        } catch(NoResultException ex) {
            log.debug("Username {} is not found in the database", userName);
            return Optional.empty();
        }
        return Optional.ofNullable(resultAccount);
    }

}
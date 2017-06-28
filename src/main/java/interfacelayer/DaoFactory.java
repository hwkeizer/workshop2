/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfacelayer;

/**
 *
 * @author hwkei
 */
public abstract class DaoFactory {
    
    // List of DAO types supported by this factory
    public static final int MYSQL = 1;
    
    // DAO's that can be produced. The concrete factories will implement
    // these methods.
    public abstract ProductDao getProductDAO();
    
    // Deliver the correct factory based on the given factoryType
    public static DaoFactory getDAOFactory(int factoryType) {
        switch (factoryType) {
            case MYSQL :
                return new DaoFactoryMysql();
            default :
                return null;
        }
    }

}
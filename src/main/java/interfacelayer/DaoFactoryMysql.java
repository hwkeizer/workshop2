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
public class DaoFactoryMysql extends DaoFactory {
    
    @Override
    public ProductDao getProductDAO() {
        return new ProductDaoMysql();
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package workshop2.interfacelayer.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import javax.persistence.EntityManager;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workshop2.domain.Account;
import static workshop2.domain.AccountType.*;
import workshop2.domain.Address;
import static workshop2.domain.Address.AddressType.*;
import workshop2.domain.Customer;
import workshop2.domain.Order;
import workshop2.domain.OrderItem;
import static workshop2.domain.OrderStatus.AFGEHANDELD;
import static workshop2.domain.OrderStatus.IN_BEHANDELING;
import static workshop2.domain.OrderStatus.NIEUW;
import workshop2.domain.Product;
import workshop2.interfacelayer.DatabaseConnection;
import workshop2.interfacelayer.view.OrderItemView;
import workshop2.interfacelayer.view.OrderView;
import workshop2.persistencelayer.CustomerService;
import workshop2.persistencelayer.CustomerServiceFactory;
import workshop2.persistencelayer.OrderService;
import workshop2.persistencelayer.OrderServiceFactory;

/**
 *
 * @author thoma
 */
public class OrderControllerTest {
    private static final Logger log = LoggerFactory.getLogger(OrderControllerTest.class);
    OrderView mockOrderView;
    OrderItemView mockOrderItemView;
    CustomerController mockCustomerController;
    OrderController orderController;
    OrderService orderService = OrderServiceFactory.getOrderService();
    List<Product> productList = new ArrayList<>();
    Scanner input = new Scanner(System.in);
    
    public OrderControllerTest(){
        
    }
    
    @Before
    public void setupMocks(){
        mockOrderView = mock(OrderView.class);
        mockOrderItemView = mock(OrderItemView.class);
        mockCustomerController = mock(CustomerController.class);
        orderController = new OrderController(mockOrderView, mockOrderItemView);
        
        // Drop account table and insert new data
        dropAndInsert();
    }
    
    @Test
    public void testCreateOrderEmployee(){
        System.out.println("testCreateOrderEmployee");
        
        //select customer
        CustomerService customerService = CustomerServiceFactory.getCustomerService();
        Optional<Customer> optionalCustomer = customerService.<Customer>fetchById(Customer.class, 8L);
        when(mockCustomerController.selectCustomerByUser()).thenReturn(optionalCustomer);

        // create orderItemList
        List<OrderItem> orderItemList = new ArrayList<OrderItem>();
        orderItemList.add(new OrderItem(null, productList.get(2), 10, new BigDecimal("20.25")));
        orderItemList.add(new OrderItem(null, productList.get(4), 10, new BigDecimal("9.50")));
        when(mockOrderItemView.createOrderItemListForNewOrder(productList)).thenReturn(orderItemList);
        
        // get confirmation
        when(mockOrderView.requestConfirmationToCreate()).thenReturn(1);
        

        // assert this order did not exist before
        assertFalse("Order should not exist before inserting in database", orderService.<Order>fetchById(Order.class, 49L).isPresent());
        log.debug("Asserted that order did not exist before");

        // Create the order by employee
        orderController.createOrderEmployee(mockCustomerController);
        
        // check if order does exist
        Optional<Order> optionalOrder = orderService.<Order>fetchById(Order.class, 49L);
        assertTrue("Order should exist after inserting in database", orderService.<Order>fetchById(Order.class, 49L).isPresent());
        assertEquals("Order total price should equal 29.75", optionalOrder.get().getTotalPrice(), new BigDecimal("29.75"));
        
        // check if OrderItems exist
        List<OrderItem> orderItemListAfterInsert = orderService.findAllOrderItemsAsListByOrder(optionalOrder.get());
        assertEquals("retrieved orderItemList should contain 2 orderItems", orderItemListAfterInsert.size(), 2);
        assertEquals("OrderItem1 should have subtotal 20.25", orderItemListAfterInsert.get(0).getSubTotal(), new BigDecimal("20.25"));
        assertEquals("OrderItem2 should have subtotal 9.50", orderItemListAfterInsert.get(0).getSubTotal(), new BigDecimal("9.50"));
  
    }
    

    private void dropAndInsert() {
        EntityManager em = DatabaseConnection.getInstance().getEntityManager();
        em.getTransaction().begin();
//        em.createNativeQuery("DELETE FROM account").executeUpdate();
        
        // Account
        em.createNativeQuery("DELETE FROM account").executeUpdate();
        String pass1 = PasswordHash.generateHash("welkom");
        String pass2 = PasswordHash.generateHash("welkom");
        String pass3 = PasswordHash.generateHash("welkom");
        Account account1 = new Account("piet", pass1, ADMIN);
        Account account2 = new Account("klaas", pass2, MEDEWERKER);
        Account account3 = new Account("jan", pass3, KLANT);
        Account account4 = new Account("fred", pass1, KLANT);
        Account account5 = new Account("joost", pass2, KLANT);
        Account account6 = new Account("jaap", pass3, KLANT);
        em.persist(account1);
        em.persist(account2);
        em.persist(account3);
        em.persist(account4);
        em.persist(account5);
        em.persist(account6);

        // Customer
        em.createNativeQuery("DELETE FROM customer").executeUpdate();
        Customer customer1 = new Customer("Piet", "Pietersen", null, account1);
        Customer customer2 = new Customer("Klaas", "Klaassen", "van", account2);
        Customer customer3 = new Customer("Jan", "Jansen", null, account3);
        Customer customer4 = new Customer("Fred", "Horst", "ter", account4);
        Customer customer5 = new Customer("Joost", "Draaier", "den", account5);
        em.persist(customer1);
        em.persist(customer2);
        em.persist(customer3);
        em.persist(customer4);
        em.persist(customer5);
        
        // Address
        em.createNativeQuery("DELETE FROM address").executeUpdate();
        Address address1 = new Address("Postweg", 201, "h", "3781JK", "Aalst", customer1, POSTADRES);
        Address address2 = new Address("Snelweg", 56, null, "3922JL", "Ee", customer2, POSTADRES);
        Address address3 = new Address("Torenstraat", 82, null, "7620CX", "Best", customer2, FACTUURADRES);
        Address address4 = new Address("Valkstraat", 9, "e", "2424DF", "Goorle", customer2, BEZORGADRES);
        Address address5 = new Address("Dorpsstraat", 5, null, "9090NM", "Best", customer3, POSTADRES);
        Address address6 = new Address("Plein", 45, null, "2522BH", "Oss", customer4, POSTADRES);
        Address address7 = new Address("Maduralaan", 23, null, "8967HJ", "Apeldoorn", customer5, POSTADRES);
        em.persist(address1);
        em.persist(address2);
        em.persist(address3);
        em.persist(address4);
        em.persist(address5);
        em.persist(address6);
        em.persist(address7);
        
        // Order
        em.createNativeQuery("DELETE FROM `order`").executeUpdate();
        Order order1 = new Order(new BigDecimal("230.78"), customer1, LocalDateTime.now(), AFGEHANDELD);
        Order order2 = new Order(new BigDecimal("62.97"), customer1, LocalDateTime.now(), AFGEHANDELD);
        Order order3 = new Order(new BigDecimal("144.12"), customer1, LocalDateTime.now(), IN_BEHANDELING);
        Order order4 = new Order(new BigDecimal("78.23"), customer2, LocalDateTime.now(), AFGEHANDELD);
        Order order5 = new Order(new BigDecimal("6.45"), customer3, LocalDateTime.now(), NIEUW);
        Order order6 = new Order(new BigDecimal("324.65"), customer3, LocalDateTime.now(), AFGEHANDELD);
        Order order7 = new Order(new BigDecimal("46.08"), customer3, LocalDateTime.now(), IN_BEHANDELING);
        Order order8 = new Order(new BigDecimal("99.56"), customer4, LocalDateTime.now(), NIEUW);
        Order order9 = new Order(new BigDecimal("23.23"), customer5, LocalDateTime.now(), AFGEHANDELD);
        em.persist(order1);
        em.persist(order2);
        em.persist(order3);
        em.persist(order4);
        em.persist(order5);
        em.persist(order6);
        em.persist(order7);
        em.persist(order8);
        em.persist(order9);

        // Product
        em.createNativeQuery("DELETE FROM product").executeUpdate();
        Product product1 = new Product("Goudse belegen kaas", new BigDecimal("12.99"), 134);
        Product product2 = new Product("Goudse extra belegen kaas", new BigDecimal("14.70"), 239);
        Product product3 = new Product("Leidse oude kaas", new BigDecimal("14.65"), 89);
        Product product4 = new Product("Schimmelkaas", new BigDecimal("11.74"), 256);
        Product product5 = new Product("Leidse jonge kaas", new BigDecimal("11.24"), 122);
        Product product6 = new Product("Boeren jonge kaas", new BigDecimal("12.57"), 85);
        productList.add(product1);
        productList.add(product2);
        productList.add(product3);
        productList.add(product4);
        productList.add(product5);
        productList.add(product6);
        em.persist(product1);
        em.persist(product2);
        em.persist(product3);
        em.persist(product4);
        em.persist(product5);
        em.persist(product6);
        
        
        // OrderItem
        em.createNativeQuery("DELETE FROM order_item").executeUpdate();
        OrderItem orderItem1 = new OrderItem(order1, product6, 23, new BigDecimal("254.12"));
        OrderItem orderItem2 = new OrderItem(order1, product1, 26, new BigDecimal("345.20"));
        OrderItem orderItem3 = new OrderItem(order1, product2, 2, new BigDecimal("24.14"));
        OrderItem orderItem4 = new OrderItem(order2, product1, 25, new BigDecimal("289.89"));
        OrderItem orderItem5 = new OrderItem(order3, product4, 2, new BigDecimal("34.89"));
        OrderItem orderItem6 = new OrderItem(order4, product2, 13, new BigDecimal("156.76"));
        OrderItem orderItem7 = new OrderItem(order4, product5, 2, new BigDecimal("23.78"));
        OrderItem orderItem8 = new OrderItem(order5, product2, 2, new BigDecimal("21.34"));
        OrderItem orderItem9 = new OrderItem(order6, product1, 3, new BigDecimal("35.31"));
        OrderItem orderItem10 = new OrderItem(order6, product3, 1, new BigDecimal("11.23"));
        OrderItem orderItem11 = new OrderItem(order7, product6, 1, new BigDecimal("14.23"));
        OrderItem orderItem12 = new OrderItem(order7, product2, 3, new BigDecimal("31.87"));
        OrderItem orderItem13 = new OrderItem(order8, product4, 23, new BigDecimal("167.32"));
        OrderItem orderItem14 = new OrderItem(order9, product1, 1, new BigDecimal("11.34"));
        OrderItem orderItem15 = new OrderItem(order9, product2, 2, new BigDecimal("22.41"));
        em.persist(orderItem1);
        em.persist(orderItem2);
        em.persist(orderItem3);
        em.persist(orderItem4);
        em.persist(orderItem5);
        em.persist(orderItem6);
        em.persist(orderItem7);
        em.persist(orderItem8);
        em.persist(orderItem9);
        em.persist(orderItem10);
        em.persist(orderItem11);
        em.persist(orderItem12);
        em.persist(orderItem13);
        em.persist(orderItem14);
        em.persist(orderItem15);
            
        em.getTransaction().commit();
        
        em.close();
    }
}

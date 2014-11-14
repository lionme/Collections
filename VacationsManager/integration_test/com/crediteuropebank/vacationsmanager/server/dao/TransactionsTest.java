package com.crediteuropebank.vacationsmanager.server.dao;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.springconfig.TestApplicationConfig;

/**
 * Just for test isolation levels. This class is ugly and isn'e used.
 * 
 * @author DIMAS
 *
 */
@Ignore
@RunWith(value = SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"file:war/WEB-INF/applicationContext.xml"})
@ContextConfiguration(classes={TestApplicationConfig.class})
@TransactionConfiguration(transactionManager="txManager", defaultRollback=false)
@Transactional(propagation=Propagation.REQUIRED, rollbackFor={Exception.class}, readOnly=false, isolation=Isolation.SERIALIZABLE)
public class TransactionsTest {
	
	private class FirstThread implements Runnable {

		@Override
		public void run() {
			//if (savedRole != null) {
				System.out.println("First concurrent thread start working.");
				
				
				Role fetchedRole = roleDAO.getById(savedRoleId);
				
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				
				fetchedRole.setDesription("Updated by first thread");
				try {
					roleDAO.update(fetchedRole);
				} catch (StaleObjectStateException e) {
					throw new RuntimeException(e);
				}
				
				System.out.println("First concurrent thread stop working.");
			/*} else {
				throw new IllegalArgumentException("Saved role should not be null!");
			}*/
		}
		
	}
	
	private class SecondThread implements Runnable {

		@Override
		public void run() {
			//if (savedRole != null) {
				System.out.println("Second concurrent thread start working.");
				
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				
				Role fetchedRole = roleDAO.getById(savedRoleId);
				
				fetchedRole.setDesription("Updated by second thread");
				try {
					roleDAO.update(fetchedRole);
				} catch (StaleObjectStateException e) {
					throw new RuntimeException(e);
				}
				
				System.out.println("Second concurrent thread stop working.");
			/*} else {
				throw new IllegalArgumentException("Saved role should not be null!");
			}*/
		}
		
	}
	
	private long savedRoleId = 14L;
	
	@Autowired
	private RoleDAO roleDAO;
	
	/*@Autowired
	private ApplicationContext applicationContext;*/
	
	/*@Autowired(required=true)
	private void setStaticDAO(RoleDAO roleDAO) {
		TransactionsTest.staticDA0 = roleDAO;
	}*/
	
/*	@Before
	public void setUp() {
		Role role = new Role();
		
		role.setName("TEST_CONCURRENT");
		role.setDesription("For testing concurrent uodate");
		// No parent role
		//role.setParentRole(null)
		
		savedRole = roleDAO.save(role);
	}*/
	
	//@Test
	public void testConcurrentUpdatingThread1() throws InterruptedException, StaleObjectStateException {
		System.out.println("First concurrent thread start working.");
		
		
		Role fetchedRole = roleDAO.getById(savedRoleId);
		
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		fetchedRole.setDesription("Updated by first thread");
		roleDAO.update(fetchedRole);
		
		System.out.println("First concurrent thread stop working.");
	}
	
	//@Test
	public void testConcurrentUpdatingThread2() throws InterruptedException {
		System.out.println("Second concurrent thread start working.");
		
		/*Role fetchedRole = roleDAO.getById(savedRoleId);
		
		fetchedRole.setDesription("Updated by first thread");
		roleDAO.update(fetchedRole);*/
		
		System.out.println("Second concurrent thread stop working.");
	}
	
	/*@Test
	@Transactional(propagation=Propagation.NEVER, isolation=Isolation.SERIALIZABLE)
	public void testConcurrentUpdating() throws InterruptedException {
		//testConcurrentUpdatingThread1();
		//testConcurrentUpdatingThread2();
		
		Thread thread1 = new Thread(new FirstThread());
		thread1.start();
		
		Thread thread2 = new Thread(new SecondThread());
		thread2.start();
		
		TimeUnit.SECONDS.sleep(10);
	}*/
	
	@Test
	public void testThread1() throws InterruptedException {
		Thread thread1 = new Thread(new FirstThread());
		thread1.start();
		TimeUnit.SECONDS.sleep(10);
		
	}
	
	@Test
	public void testThread2() throws InterruptedException {
		Thread thread2 = new Thread(new SecondThread());
		thread2.start();
		//TimeUnit.SECONDS.sleep(10);
	}
	
	
	@After
	public void tearDown() {
		//roleDAO.delete(savedRole.getId());
	}
}

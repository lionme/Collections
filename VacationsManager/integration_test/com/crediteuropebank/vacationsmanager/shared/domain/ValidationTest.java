package com.crediteuropebank.vacationsmanager.shared.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.DateUtil;
import com.crediteuropebank.vacationsmanager.shared.ApprovalStepState;
import com.crediteuropebank.vacationsmanager.shared.BigDecimalUtil;
import com.crediteuropebank.vacationsmanager.shared.Privilege;
import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.springconfig.TestApplicationConfig;

@RunWith(value = SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"file:war/WEB-INF/applicationContext.xml"})
@ContextConfiguration(classes={TestApplicationConfig.class})
@TransactionConfiguration(transactionManager="txManager", defaultRollback=false)
@Transactional(propagation=Propagation.REQUIRED, rollbackFor={Exception.class})
public class ValidationTest {
	
	private static final BigDecimal DAY_VACATIONS_AMOUNT = BigDecimalUtil.newBigDecimal(5);
	
	@Autowired
	Validator validator;

	@Test
	public void checkUserValidaion() {
		/*ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		Validator validator = (Validator) context.getBean("validator");*/
		
		//long testRoleId = 34534L;
		Role testRole = new Role();
		//testRole.setId(testRoleId);
		testRole.setName("TEST_ROLE");
		testRole.setDesription("test role");
		testRole.setPrivilege(Privilege.DEFAULT);
		
		User user = new User();
		user.setUsername("TestSuperviser");
		user.setPassword("121312");
		user.setFullName("Just simple superviser");
		user.seteMail("tesssdt@gmail.com");
		user.setRole(testRole);
		
		RemainingVacationDays vacationDays = new RemainingVacationDays();
		vacationDays.setTwoWeeksVacations(1);
		vacationDays.setOneWeekVacations(1);
		vacationDays.setDayVacations(DAY_VACATIONS_AMOUNT);
		
		Set<ConstraintViolation<User>> constraintViolations = validator.validate(user, Default.class);
		
		//int numErrors = constraintViolations.size();
		for (ConstraintViolation<User> constraintViolation: constraintViolations) {
			System.out.println(constraintViolation.getMessage());
		}
		
		System.out.println("Number of errors: " +  constraintViolations.size());
		
		Assert.assertTrue(constraintViolations.size()==0);
	}
	
	@Test
	public void testVacationValidation() {
		/*ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		Validator validator = (Validator) context.getBean("validator");*/
		
		//long testRoleId = 34534L;
		Role testRole = new Role();
		//testRole.setId(testRoleId);
		testRole.setName("TEST_ROLE");
		testRole.setDesription("test role");
		testRole.setPrivilege(Privilege.DEFAULT);
		
		User user = new User();
		user.setUsername("TestSuperviser");
		user.setPassword("123445678");
		user.setFullName("Just simple superviser");
		user.seteMail("tesssdt@gmail.com");
		user.setRole(testRole);
		
		RemainingVacationDays vacationDays = new RemainingVacationDays();
		vacationDays.setTwoWeeksVacations(1);
		vacationDays.setOneWeekVacations(1);
		vacationDays.setDayVacations(DAY_VACATIONS_AMOUNT);
		
		Vacation vacation = new Vacation();
		vacation.setStartDate(DateUtil.getCurrentDateWithoutTime());
		vacation.setEndDate(DateUtil.getCurrentDateWithoutTime());
		vacation.setState(VacationState.JUST_OPENED);
		vacation.setUser(user);
		//vacation.setDeputy(user);
		
		List<User> deputies = new ArrayList<User>();
		deputies.add(user);
		
		vacation.setDeputies(deputies);
		
		Set<ConstraintViolation<Vacation>> constraintViolations = validator.validate(vacation, Default.class);
		
		//int numErrors = constraintViolations.size();
		for (ConstraintViolation<Vacation> constraintViolation: constraintViolations) {
			System.out.println(constraintViolation.getMessage());
		}
		
		System.out.println("Number of errors: " +  constraintViolations.size());
		
		Assert.assertTrue(constraintViolations.size()==0);
	}
	
	@Test
	public void testApprovalValidation() {
		/*ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		Validator validator = (Validator) context.getBean("validator");*/
		
		//long testRoleId = 34534L;
		Role testRole = new Role();
		//testRole.setId(testRoleId);
		testRole.setName("TEST_ROLE");
		testRole.setDesription("test role");
		testRole.setPrivilege(Privilege.DEFAULT);
		
		User user = new User();
		user.setUsername("TestSuperviser");
		user.setPassword("1");
		user.setFullName("Just simple superviser");
		user.seteMail("tess@@sdt@gmail.com");
		user.setRole(testRole);
		
		RemainingVacationDays vacationDays = new RemainingVacationDays();
		vacationDays.setTwoWeeksVacations(1);
		vacationDays.setOneWeekVacations(1);
		vacationDays.setDayVacations(DAY_VACATIONS_AMOUNT);
		
		Vacation vacation = new Vacation();
		vacation.setStartDate(DateUtil.getCurrentDateWithoutTime());
		vacation.setEndDate(DateUtil.getCurrentDateWithoutTime());
		vacation.setState(VacationState.JUST_OPENED);
		vacation.setUser(user);
		//vacation.setDeputy(user);
		
		List<User> deputies = new ArrayList<User>();
		deputies.add(user);
		vacation.setDeputies(deputies);
		
		ApprovalStep approval = new ApprovalStep();
		approval.setState(ApprovalStepState.ACTIVE);
		approval.setApproverRole(testRole);
		approval.setVacation(vacation);
		approval.setRowNumber(3);
		
		Set<ConstraintViolation<ApprovalStep>> constraintViolations = validator.validate(approval, Default.class);
		
		//int numErrors = constraintViolations.size();
		for (ConstraintViolation<ApprovalStep> constraintViolation: constraintViolations) {
			System.out.println(constraintViolation.getMessage());
		}
		
		System.out.println("Number of errors: " +  constraintViolations.size());
		
		Assert.assertTrue(constraintViolations.size()==0);
	}
}

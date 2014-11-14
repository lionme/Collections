package com.crediteuropebank.vacationsmanager.server.validation;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.crediteuropebank.vacationsmanager.shared.BigDecimalUtil;
import com.crediteuropebank.vacationsmanager.shared.Privilege;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomValidationException;
import com.crediteuropebank.vacationsmanager.springconfig.TestApplicationConfig;

/**
 * Simple functionality to test validation for {@link User} entity.
 * 
 * @author dimas
 *
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestApplicationConfig.class})
public class UserEntityValidationTest {
	
	@Autowired
	ServerValidationUtil validationUtil;
	
	private Role role;
	private User user;
	private RemainingVacationDays vacationDays;
	
	@Before
	public void setUp() {
		role = new Role();
		role.setName("TEST_ROLE");
		role.setDesription("test role");
		role.setPrivilege(Privilege.ADMIN);
		
		user = new User();
		user.setUsername("TestSuperviser");
		user.setPassword("12345678");
		user.setFullName("Just simple superviser");
		user.seteMail("tsdt@gmail.com");
		user.setRole(role);
		
		vacationDays = new RemainingVacationDays();
		vacationDays.setTwoWeeksVacations(1);
		vacationDays.setOneWeekVacations(1);
		vacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(5));
		
		user.setVacationDays(vacationDays);
	}

	@Test
	public void testCorrectValidation() throws CustomValidationException {
		validationUtil.validate(user);
	}
	
	@Test
	public void testValidationWithWrongEnteredData() {
		user.setUsername("eee");
		// Password validation is doing separately in the code because of password encoding.
		//user.setPassword("123");
		user.setFullName("123");
		user.seteMail("rrr@kkk");
		
		try {
			validationUtil.validate(user);
		} catch (CustomValidationException e) {
			List<String> errors = e.getErrorMessages();
			Assert.assertEquals(3, errors.size());
		}
	}
	
	@Test(expected=CustomValidationException.class)
	public void testValidationWhenRoleIsNull() throws CustomValidationException {
		user.setRole(null);
		
		validationUtil.validate(user);
	}
	
	@Test
	public void testCascadeValidationOfRoleObject() {
		role.setName("ss");
		role.setDesription("asd");
		role.setPrivilege(null);
		
		try {
			validationUtil.validate(user);
		} catch (CustomValidationException e) {
			List<String> errors = e.getErrorMessages();
			Assert.assertEquals(3, errors.size());
		}
	}
	
	@Test
	public void testCascadeValidationOfRemainingVacationDaysObject() {
		vacationDays.setTwoWeeksVacations(9);
		vacationDays.setOneWeekVacations(9);
		vacationDays.setDayVacations(BigDecimalUtil.newBigDecimal(100));
		
		try {
			validationUtil.validate(user);
		} catch (CustomValidationException e) {
			List<String> errors = e.getErrorMessages();
			Assert.assertEquals(3, errors.size());
		}
	}
}

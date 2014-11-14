package com.crediteuropebank.vacationsmanager.server.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.crediteuropebank.vacationsmanager.server.DateUtil;
import com.crediteuropebank.vacationsmanager.server.dao.RoleDAO;
import com.crediteuropebank.vacationsmanager.server.dao.UserDAO;
import com.crediteuropebank.vacationsmanager.shared.BigDecimalUtil;
import com.crediteuropebank.vacationsmanager.shared.Privilege;
import com.crediteuropebank.vacationsmanager.shared.VacationDaysUtil;
import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.UsedVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;
import com.crediteuropebank.vacationsmanager.springconfig.TestApplicationConfig;

/**
 * 
 * @author DIMAS
 *
 */
@RunWith(value = SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestApplicationConfig.class})
@TransactionConfiguration(transactionManager="txManager", defaultRollback=false)
@Transactional(propagation=Propagation.REQUIRED, rollbackFor={Exception.class})
public class ComplexDataCreationForTest {
	
	private static final BigDecimal DAY_VACATIONS_AMOUNT = BigDecimalUtil.newBigDecimal(5);
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private RoleDAO roleDAO;
	
	@Autowired
	private VacationManager vacationManager;

	@Ignore
	@Test
	public void createComplexDataForTests() throws CustomMessageException {
	
		// create roles for tests
		
		Role mainHeaderRole = new Role();
		mainHeaderRole.setName("MAIN_HEADER");
		mainHeaderRole.setDesription("Main header role");
		mainHeaderRole.setPrivilege(Privilege.ADMIN);
		
		roleDAO.save(mainHeaderRole);
		
		Role supervisorRole = new Role();
		supervisorRole.setName("SUPERVISOR");
		supervisorRole.setDesription("Developer-supervisor role");
		supervisorRole.setParentRole(mainHeaderRole);
		supervisorRole.setPrivilege(Privilege.DEFAULT);
				
		roleDAO.save(supervisorRole);
		
		Role developerRole = new Role();
		developerRole.setName("DEVELOPER");
		developerRole.setDesription("Developer role");
		developerRole.setParentRole(supervisorRole);
		developerRole.setPrivilege(Privilege.DEFAULT);
				
		roleDAO.save(developerRole);
		
		RemainingVacationDays vacationDays = new RemainingVacationDays();
		vacationDays.setTwoWeeksVacations(1);
		vacationDays.setOneWeekVacations(1);
		vacationDays.setDayVacations(DAY_VACATIONS_AMOUNT);
		
		User mainHeader = new User();
		mainHeader.setUsername("mainHeader");
		mainHeader.setPassword("12345678");
		mainHeader.setFullName("Main header for tests");
		mainHeader.seteMail("Dmitriy.IERMOLOVICH@crediteurope.com.ua");
		mainHeader.setRole(mainHeaderRole);
		mainHeader.setVacationDays(vacationDays);
		
		userDAO.save(mainHeader);	
		
		vacationDays = new RemainingVacationDays(vacationDays);
		
		User supervisor = new User();
		supervisor.setUsername("supervisor");
		supervisor.setPassword("12345678");
		supervisor.setFullName("Supervisor for tests");
		supervisor.seteMail("Dmitriy.IERMOLOVICH@crediteurope.com.ua");
		supervisor.setRole(supervisorRole);
		supervisor.setVacationDays(vacationDays);
		//supervisor.setHeader(mainHeader);
		
		userDAO.save(supervisor);	
		
		vacationDays = new RemainingVacationDays(vacationDays);
		
		User developer = new User();
		developer.setUsername("developer");
		developer.setPassword("12345678");
		developer.setFullName("Developer for tests");
		developer.seteMail("Dmitriy.IERMOLOVICH@crediteurope.com.ua");
		developer.setRole(developerRole);
		developer.setVacationDays(vacationDays);
		//developer.setHeader(supervisor);

		userDAO.save(developer);	
		
		vacationDays = new RemainingVacationDays(vacationDays);
		vacationDays.setTwoWeeksVacations(1);
		vacationDays.setOneWeekVacations(1);
		vacationDays.setDayVacations(DAY_VACATIONS_AMOUNT);
		
		// create a deputy
		User testDeputy = new User();
		testDeputy.setUsername("testDeputy");
		testDeputy.setPassword("12345678");
		testDeputy.setFullName("Deputy (developer) for tests");
		testDeputy.seteMail("Dmitriy.IERMOLOVICH@crediteurope.com.ua");
		testDeputy.setRole(developerRole);
		testDeputy.setVacationDays(vacationDays);
		//developer.setHeader(supervisor);

		userDAO.save(testDeputy);	

		// Test saving of the vacation.
		Vacation vacation = new Vacation();
		vacation.setStartDate(DateUtil.getCurrentDateWithoutTime());
		vacation.setEndDate(DateUtil.addDays(DateUtil.getCurrentDateWithoutTime(), 8));
		vacation.setState(VacationState.JUST_OPENED);
		vacation.setUser(developer);
		
		List<User> deputies = new ArrayList<User>();
		deputies.add(testDeputy);
		deputies.add(supervisor);
		vacation.setDeputies(deputies);
		
		UsedVacationDays usedVacationDays = VacationDaysUtil.calculateVacationDays(9, 0, 2, developer.getVacationDays());

		vacation.setUsedVacationDays(usedVacationDays);
		
		//approvalManager.createApprovalFlow(vacation);
		vacationManager.saveVacation(vacation);
	}
}

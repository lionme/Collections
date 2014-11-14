package com.crediteuropebank.vacationsmanager.server.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.crediteuropebank.vacationsmanager.server.DateUtil;
import com.crediteuropebank.vacationsmanager.server.PropertiesBean;
import com.crediteuropebank.vacationsmanager.server.dao.RemainingVacationDaysDAO;
import com.crediteuropebank.vacationsmanager.server.dao.StaleObjectStateException;
import com.crediteuropebank.vacationsmanager.server.dao.UserDAO;
import com.crediteuropebank.vacationsmanager.server.dao.VacationDAO;
import com.crediteuropebank.vacationsmanager.server.validation.ServerValidationUtil;
import com.crediteuropebank.vacationsmanager.shared.BigDecimalUtil;
import com.crediteuropebank.vacationsmanager.shared.VacationState;
import com.crediteuropebank.vacationsmanager.shared.domain.RemainingVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.crediteuropebank.vacationsmanager.shared.domain.UsedVacationDays;
import com.crediteuropebank.vacationsmanager.shared.domain.User;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.crediteuropebank.vacationsmanager.shared.exception.CustomMessageException;

/**
 * This class contains unit tests for main functionality of {@link VacationManager}.
 * 
 * @author dimas
 *
 */
/* 
 * I don't know how to implement test correctly. If I will find - I will finish this class.
 * For now tests doesn't work.
 */
@Ignore 
@RunWith(MockitoJUnitRunner.class)
public class VacationManagerUnitTests {
	
	private static final long TEST_VACATION_ID = 1L;
	private static final long TEST_USER_ID = 2L;
	//private static final long TEST_REMAININ_VACATION_DAYS_ID = 3L;
	private static final Date TEST_VACATION_START_DATE = new Date();
	private static final Date TEST_VACATION_END_DATE = DateUtil.addDays(new Date(), 7);
	private static final VacationState TEST_VACATION_STATE = VacationState.JUST_OPENED;
	
	/*
	 * Define mock objects used in application:
	 * 
	 */
	private static final Vacation MOCK_VACATION = createMockVacation();
	private static final User MOCK_USER = createMockUser();
	private static final RemainingVacationDays MOCK_REMAINING_VACATION_DAYS = createMockRemainingVacationDays();
	private static final UsedVacationDays MOCK_USED_VACATION_DAYS = createMockUsedVacationDays();
	
	/**
	 * This list of vacations used in tests.
	 */
	private static final List<Vacation> testVacations = new ArrayList<Vacation>();
	
	@Mock
	private VacationDAO vacationDAO;
	
	/*@Mock
	private HolidayDaysDAO holidayDaysDAO;*/
	
	@Mock
	private UserDAO userDAO;
	
	@Mock
	private RemainingVacationDaysDAO userVacationDaysDAO;
	
	/*@Mock
	private UsedVacationDaysDAO usedVacationDaysDAO;*/
	
	@Mock
	private ApprovalManager approvalManager;
	
	/*@Mock
	private UserManager userManager;*/
	
	@Mock
	private ServerValidationUtil validationUtil;
	
	@Mock
	private PropertiesBean propertiesBean;
	
	@InjectMocks
	private VacationManager vacationManager;
	
	@BeforeClass
	public static void beforeTestClass() {
		/*
		 * Create vacations for tests.
		 * Init vacations just partially, this is enough for unit tests.
		 * 
		 */
		
		Vacation vacation = new Vacation();
		vacation.setStartDate(DateUtil.addDays(new Date(), -7));
		vacation.setEndDate(new Date());
		vacation.setState(VacationState.JUST_OPENED);
		
		testVacations.add(vacation);
		
		vacation = new Vacation();
		vacation.setStartDate(DateUtil.addDays(new Date(), 5));
		vacation.setEndDate(DateUtil.addDays(new Date(), 10));
		vacation.setState(VacationState.REJECTED);
		
		testVacations.add(vacation);
	}
	
	@Test
	public void testGetAllVacations() {
		Mockito.when(vacationDAO.getAll()).thenReturn(testVacations);
		
		List<Vacation> fetchedVacations = vacationManager.getAllVacations();
		
		Assert.assertThat(fetchedVacations, CoreMatchers.equalTo(testVacations));
	}
	
	@Test
	public void testSaveVacation() throws CustomMessageException, StaleObjectStateException {
		Vacation testVacation = MOCK_VACATION;
		
		/*
		 *  Define all "when conditions"
		 */
		
		Mockito.when(propertiesBean.getMaxDaysAhead()).thenReturn(90);
		
		/*
		 * When conditions for date range validation.
		 */
		Mockito.when(vacationDAO.calculateVacationsForPeriod(Mockito.anyLong(), 
				Mockito.any(Date.class), 
				Mockito.any(Date.class))).thenReturn(0);
		Mockito.when(vacationDAO.getVacationsIdsWhereUserIsDeputy(Mockito.anyLong(), 
				Mockito.any(Date.class), 
				Mockito.any(Date.class))).thenReturn(new ArrayList<Long>());
		Mockito.when(userDAO.getTotalAmountOfUsersWithRole(Mockito.any(Role.class))).thenReturn(2);
		Mockito.when(vacationDAO.calculateUsersWithVacationForRole(Mockito.any(Role.class), 
				Mockito.any(Date.class), 
				Mockito.any(Date.class))).thenReturn(0);
		
		RemainingVacationDays recalculatedVacationDays = new RemainingVacationDays(MOCK_REMAINING_VACATION_DAYS);
		recalculatedVacationDays.deduct(MOCK_USED_VACATION_DAYS);
		
		/*Mockito.when(userVacationDaysDAO.getById(testVacation.getUser().getVacationDays().getId()))
					.thenReturn(testVacation.getUser().getVacationDays());
					.thenReturn(recalculatedVacationDays);*/
		Mockito.when(userVacationDaysDAO.getById(TEST_USER_ID))
					.thenReturn(MOCK_REMAINING_VACATION_DAYS)
					.thenReturn(recalculatedVacationDays);
		
		Mockito.when(vacationDAO.save(testVacation)).thenReturn(testVacation);
		
		vacationManager.saveVacation(testVacation);
		
		Mockito.verify(validationUtil, Mockito.times(1)).validate(testVacation);
		
		Mockito.verify(testVacation.getUser().getVacationDays(), Mockito.times(1))
			.deductVacationDays(testVacation.getUsedVacationDays().getTwoWeeksVacations(), 
					testVacation.getUsedVacationDays().getOneWeekVacations(), 
					testVacation.getUsedVacationDays().getDayVacations());
		
		// Check that user's remaining vacation days was updated with correct amount of days.
		//RemainingVacationDays leftVacationDays = Mockito.mock(RemainingVacationDays.class);
		Mockito.verify(userVacationDaysDAO, Mockito.times(1)).update(testVacation.getUser().getVacationDays());
		
		Mockito.verify(approvalManager, Mockito.times(1)).createApprovalFlow(testVacation);
		
		Mockito.verify(testVacation.getUser(), Mockito.times(1)).setVacationDays(recalculatedVacationDays);
	}
	
	private static Vacation createMockVacation() {
		Vacation mockVacation = Mockito.mock(Vacation.class);
		
		Mockito.when(mockVacation.getId()).thenReturn(TEST_VACATION_ID);
		Mockito.when(mockVacation.getStartDate()).thenReturn(TEST_VACATION_START_DATE);
		Mockito.when(mockVacation.getEndDate()).thenReturn(TEST_VACATION_END_DATE);
		Mockito.when(mockVacation.getState()).thenReturn(TEST_VACATION_STATE);
		
		//Mockito.when(mockVacation.getUser()).thenReturn(MOCK_USER);
		//Mockito.when(mockVacation.getUsedVacationDays()).thenReturn(MOCK_USED_VACATION_DAYS);
		mockVacation.setUser(MOCK_USER);
		mockVacation.setUsedVacationDays(MOCK_USED_VACATION_DAYS);
		
		return mockVacation;
	}
	
	private static User createMockUser() {
		User mockUser = Mockito.mock(User.class);	
		
		Mockito.when(mockUser.getId()).thenReturn(TEST_USER_ID);
		//Mockito.when(mockUser.getVacationDays()).thenReturn(MOCK_REMAINING_VACATION_DAYS);
		
		mockUser.setVacationDays(MOCK_REMAINING_VACATION_DAYS);
		
		return mockUser;
	}
	
	private static final RemainingVacationDays createMockRemainingVacationDays() {
		RemainingVacationDays mockRemainingVacationDays = Mockito.mock(RemainingVacationDays.class);
		
		Mockito.when(mockRemainingVacationDays.getTwoWeeksVacations()).thenReturn(1);
		Mockito.when(mockRemainingVacationDays.getOneWeekVacations()).thenReturn(1);
		Mockito.when(mockRemainingVacationDays.getDayVacations()).thenReturn(BigDecimalUtil.newBigDecimal(5));
		Mockito.when(mockRemainingVacationDays.getId()).thenReturn(TEST_USER_ID);
		
		return mockRemainingVacationDays;
	}
	
	private static final UsedVacationDays createMockUsedVacationDays() {
		UsedVacationDays mockUsedVacationDays = Mockito.mock(UsedVacationDays.class);
		
		Mockito.when(mockUsedVacationDays.getTwoWeeksVacations()).thenReturn(0);
		Mockito.when(mockUsedVacationDays.getOneWeekVacations()).thenReturn(1);
		Mockito.when(mockUsedVacationDays.getDayVacations()).thenReturn(BigDecimalUtil.newBigDecimal(0));		

		return mockUsedVacationDays;
	}

}

package com.crediteuropebank.vacationsmanager.shared.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.crediteuropebank.vacationsmanager.server.dblogging.Column;

/**
 * 
 * This abstract class has two implementations - {@link RemainingVacationDays} and {@link UsedVacationDays}.
 * But almost all functionality contains in this abstract class. I create two concrete classes in this case for
 * making code cleaner and for easier logging to DB table implementation.
 * 
 * @author dimas
 *
 */
public abstract class VacationDays extends BaseDomain {
	
	/**
	 * Default serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The number of two weeks vacations.
	 */
	@Column(columnName = "TWO_WEEKS_VACATIONS")
	@Max(value=7, message="Two weeks vacation's amount cannot be greater then 7")
	@Min(value=0, message="Two weeks vacation's amount cannot be lower then 0")
	private int twoWeeksVacations;
	
	/**
	 * The number of one week vacations.
	 */
	@Column(columnName = "ONE_WEEK_VACATIONS")
	@Max(value=7, message="One week vacation's amount cannot be greater then 7")
	@Min(value=0, message="One week vacation's amount cannot be lower then 0")
	private int oneWeekVacations;
	
	/**
	 * The number of day vacations.
	 */
	@Column(columnName = "DAY_VACATIONS")
	@Max(value=99, message="Day vacation's amount cannot be greater then 99")
	@Min(value=0, message="Day vacations amount cannot be lower then 0")
	private BigDecimal dayVacations;
	
	public VacationDays() {
		super();
	}
	
	/**
	 * Constructor that initialize all fields. Id and version fields can be set only using this cinstructor.
	 */
	/*public VacationDays(long id, int version, int twoWeeksVacations,
			int oneWeekVacations, BigDecimal dayVacations) {
		super(id, version);
		
		this.twoWeeksVacations = twoWeeksVacations;
		this.oneWeekVacations = oneWeekVacations;
		this.dayVacations = dayVacations;
	}*/
	
	/**
	 * Constructor that initialize basic fields of this class and sets default values for id and version.
	 */
	public VacationDays(int twoWeeksVacations,
			int oneWeekVacations, BigDecimal dayVacations) {
		super();
		
		this.twoWeeksVacations = twoWeeksVacations;
		this.oneWeekVacations = oneWeekVacations;
		this.dayVacations = dayVacations;
	}
	
	/**
	 * <p>Copy constructor. Create an exact copy of the object. This constructor is used instead of clone() method.</p>
	 * 
	 * <p>But you should note that this method doesn't copy id and version. New object will 
	 * have 0 id and 0 version. Id and version are only set by DAO method!</p>
	 * 
	 * @param vacationDays - vacation days object to be copied.
	 */
	public VacationDays(VacationDays vacationDays) {
		//super(vacationDays.getId(), vacationDays.getVersion());
		this.twoWeeksVacations = vacationDays.getTwoWeeksVacations();
		this.oneWeekVacations = vacationDays.getOneWeekVacations();
		this.dayVacations = vacationDays.getDayVacations();
	}

	public int getTwoWeeksVacations() {
		return twoWeeksVacations;
	}

	public void setTwoWeeksVacations(int twoWeeksVacations) {
		this.twoWeeksVacations = twoWeeksVacations;
	}

	public int getOneWeekVacations() {
		return oneWeekVacations;
	}

	public void setOneWeekVacations(int oneWeekVacations) {
		this.oneWeekVacations = oneWeekVacations;
	}

	public BigDecimal getDayVacations() {
		return dayVacations;
	}

	public void setDayVacations(BigDecimal dayVacations) {
		this.dayVacations = dayVacations;
	}

	@Override
	public String toString() {
		return "VacationDays [id=" + id + ", version=" + version
				+ ", twoWeeksVacations=" + twoWeeksVacations
				+ ", oneWeekVacations=" + oneWeekVacations + ", dayVacations="
				+ dayVacations.doubleValue() + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dayVacations == null) ? 0 : dayVacations.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + oneWeekVacations;
		result = prime * result + twoWeeksVacations;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VacationDays other = (VacationDays) obj;
		if (dayVacations == null) {
			if (other.dayVacations != null)
				return false;
		} else if (!dayVacations.equals(other.dayVacations))
			return false;
		if (id != other.id)
			return false;
		if (oneWeekVacations != other.oneWeekVacations)
			return false;
		if (twoWeeksVacations != other.twoWeeksVacations)
			return false;
		return true;
	}

	public void addVacationDays(int twoWeeksVacationAmount, int oneWeekVacationAmount, BigDecimal dayVacationAmount) {
		this.twoWeeksVacations = this.twoWeeksVacations + twoWeeksVacationAmount;
		this.oneWeekVacations = this.oneWeekVacations + oneWeekVacationAmount;
		this.dayVacations = this.dayVacations.add(dayVacationAmount);
	}

	/**
	 * This method is used to subtract specified amount of two weeks vacations, one week vacations and
	 * day vacations from existed amount.
	 * 
	 * @param twoWeeksVacationAmount - amount of two weeks vacations that should be subtracted.
	 * @param oneWeekVacationAmount - amount of one week vacations that should be subtracted.
	 * @param dayVacationAmount - amount of day vacations that should be subtracted.
	 */
	public void deductVacationDays(int twoWeeksVacationAmount, int oneWeekVacationAmount, BigDecimal dayVacationAmount) {
		this.twoWeeksVacations = this.twoWeeksVacations - twoWeeksVacationAmount;
		this.oneWeekVacations = this.oneWeekVacations - oneWeekVacationAmount;
		this.dayVacations = this.dayVacations.subtract(dayVacationAmount);
	}
	
	/**
	 * This method adds specified amount of vacation days.
	 * 
	 * @param vacationDays - vacation days to be added.
	 */
	public void add(VacationDays vacationDays) {
		this.twoWeeksVacations = this.twoWeeksVacations + vacationDays.getTwoWeeksVacations();
		this.oneWeekVacations = this.oneWeekVacations + vacationDays.getOneWeekVacations();
		this.dayVacations = this.dayVacations.add(vacationDays.getDayVacations());
	}
	
	/**
	 * This method subtracts specified amount of vacation days.
	 * 
	 * @param vacationDays - vacation days to be subtracted.
	 */
	public void deduct(VacationDays vacationDays) {
		this.twoWeeksVacations = this.twoWeeksVacations - vacationDays.getTwoWeeksVacations();
		this.oneWeekVacations = this.oneWeekVacations - vacationDays.getOneWeekVacations();
		this.dayVacations = this.dayVacations.subtract(vacationDays.getDayVacations());
	}
}

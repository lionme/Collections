package com.crediteuropebank.vacationsmanager.client.form.tab;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.crediteuropebank.vacationsmanager.client.VacationsManager;
import com.crediteuropebank.vacationsmanager.client.callback.CustomAsyncCallback;
import com.crediteuropebank.vacationsmanager.shared.CollectionsUtil;
import com.crediteuropebank.vacationsmanager.shared.domain.Vacation;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import ca.nanometrics.gflot.client.Axis;
import ca.nanometrics.gflot.client.DataPoint;
import ca.nanometrics.gflot.client.SeriesHandler;
import ca.nanometrics.gflot.client.SimplePlot;
import ca.nanometrics.gflot.client.Tick;
import ca.nanometrics.gflot.client.event.PlotHoverListener;
import ca.nanometrics.gflot.client.event.PlotItem;
import ca.nanometrics.gflot.client.event.PlotPosition;
import ca.nanometrics.gflot.client.jsni.Plot;
import ca.nanometrics.gflot.client.options.AbstractAxisOptions.AxisPosition;
import ca.nanometrics.gflot.client.options.AbstractAxisOptions.TickGenerator;
import ca.nanometrics.gflot.client.options.AxisOptions;
import ca.nanometrics.gflot.client.options.GlobalSeriesOptions;
import ca.nanometrics.gflot.client.options.GridOptions;
import ca.nanometrics.gflot.client.options.LegendOptions;
import ca.nanometrics.gflot.client.options.LineSeriesOptions;
import ca.nanometrics.gflot.client.options.PlotOptions;
import ca.nanometrics.gflot.client.options.PointsSeriesOptions;
import ca.nanometrics.gflot.client.options.TickFormatter;
import ca.nanometrics.gflot.client.options.LegendOptions.LegendPosition;

/**
 * 
 * Region in which vacation chart is placed. Contains all necessary logic for drawing chart.
 * 
 * @author dimas
 *
 */
public class VacationsChartTab extends Composite implements CustomTab {
	
	/**
	 * The custom implementation of the TickGenerator.
	 * It generates ticks only for points that represents start and end day of vacations, and also current date.
	 * @author dimas
	 *
	 */
	private class CustomTickGenerator implements TickGenerator{

		@Override
		public Tick[] generate(Axis axis) {
			Iterator<Date> iterator = datesSet.iterator();
			
			Tick[] ticksArray = new Tick[datesSet.size()];
			int i = 0;
			
			while (iterator.hasNext()) {
				Date date = iterator.next();
				ticksArray[i] = new Tick(date.getTime(), "<div  class='rotate'>" + dateFormatter.format(date) + "</div>");
				i++;
			}
			
			return ticksArray;
		}
		
	}
	
	/**
	 * Width of the plot.
	 */
	private static final String PLOT_WIDTH = "900px";
	
	/**
	 * Height of the plot.
	 */
	private static final String PLOT_HEIGHT = "400px";
	
	/**
	 * This constant specifies the number of milliseconds in a day.
	 */
	private static final double NUMBER_OF_MILLISECONDS_IN_DAY = 86400000D;
	
	/**
	 * Main panel of this region. All other widget is placed in this panel.
	 */
	//private HorizontalPanel mainPanel = new HorizontalPanel();
	
	/**
	 * PlotModel instance.
	 */
	private final CustomPlotModel model = new CustomPlotModel();
	
	/**
	 * PlotOptions instance.
	 */
    private final PlotOptions plotOptions = new PlotOptions();
    
	/**
	 *  I am using this HashSet for generating ticks using custom tick generator. 
	 */
	private final Set<Date> datesSet = new HashSet<Date>();
    
    /**
     * Date formatter instance.
     */
    private final DateTimeFormat dateFormatter = DateTimeFormat.getFormat("dd.MM.yyyy");

    /**
     * This array represents color that will be used for line chart drawing. If number of lines is more
     * then number of colors then colors will repeat. 
     */
    private final String[] colors = new String[] {
    											"#FF0000", // red
    											"#80FF00", // light green    											
    											"#FF8000", // orange
    											"#0000FF", // blue
    											"#FFFF00", // yellow
    											"#01DF01", // green
    											"#FF00FF", // magenta
    											"#006400", // dark green
    											"#00FFFF", // cyan
    											"#4682B4", // steel blue
    											"#696969", // dim grey
    											"#FFBF00", // orange~yellow
    											"#7B68EE", // medium state blue
    											"#EEE8AA", // pale
    											"#FF4000", // red~orange
    											"#A020F0"  // purple
    };
    
    /**
     * This map contains relation between usernames of vacations owners and id's with 
     * which their vacations are displayed on plot.
     * 
     * key - String - username of vacation's owner
     * value - Integer - id with which vacation is displayed
     */
    private Map<String, Integer> usernames_PlotId_RelationMap = null;
    
    /**
     * Variable that holds an instance of SimplePlot.
     */
    private SimplePlot plot;
    
    // This elements is used for searching by dates range
    //private CheckBox dateRangeCheckbox;
    //private DateBox searchStartDate;
   // private DateBox searchEndDate;
    
    /**
     * Constructs VacationsChartTab with all inner elements.
     */
	public VacationsChartTab() {
		VerticalPanel mainPanel = new VerticalPanel();
		
		//mainPanel.add(createSearchCriteriaPanel());
		
		mainPanel.add(createPlotPanel());
		
		initWidget(mainPanel);
	}
	
	/**
	 * Creates Horizontal Panel where plot and legend were placed
	 * @return
	 */
	private HorizontalPanel createPlotPanel() {
		HorizontalPanel plotPanel = new HorizontalPanel();
		plotPanel.setSpacing(10);
        
        plotOptions.setGlobalSeriesOptions( new GlobalSeriesOptions()
        .setLineSeriesOptions( new LineSeriesOptions().setLineWidth( 2 ).setShow( true ) )
        .setPointsOptions( new PointsSeriesOptions().setRadius( 3 ).setShow( true ) ).setShadowSize( 0d ) );
        
        // >>>>>>> You need make the grid hoverable <<<<<<<<<
        plotOptions.setGridOptions( new GridOptions().setHoverable( true ) );
        // >>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        
        AxisOptions xAxisOptions  = new AxisOptions();
        
        xAxisOptions.setMinimum((double)getCurrentDateWithoutTimePart().getTime()) // sets current date as minimum
        .setMinTickSize(NUMBER_OF_MILLISECONDS_IN_DAY)
        .setTicks(this.new CustomTickGenerator());
              
        // Add X axis tick formatter options
        plotOptions.addXAxisOptions(xAxisOptions);
        
        plotOptions.addYAxisOptions( new AxisOptions().setMinTickSize(1d).setMinimum(0)
        		.setPosition(AxisPosition.LEFT).setLabelWidth(60d)
        		.setTickFormatter( new TickFormatter()
        {
            public String formatTickValue( double tickValue, Axis axis )
            {
                int curveId = (int) tickValue;
                
                String username = CollectionsUtil.<String, Integer>getKeyByValue(usernames_PlotId_RelationMap, curveId);
                
            	return username!=null?username:"";
            }
        } ) );
        
        
        FlowPanel legendPanel = new FlowPanel();

        plotOptions.setLegendOptions( new LegendOptions().setBackgroundOpacity( 0 ).setPosition( LegendPosition.NORTH_WEST ).setContainer(legendPanel.getElement()) );

        /*
         *  create the plot
         */
        
        plot = new SimplePlot( model, plotOptions );
        
        plot.setWidth(PLOT_WIDTH);
        plot.setHeight(PLOT_HEIGHT);
        
        final PopupPanel popup = new PopupPanel();
        final Label label = new Label();
        popup.add( label );
        
        // add hover listener
        plot.addHoverListener( new PlotHoverListener()
        {
            public void onPlotHover( Plot plot, PlotPosition position, PlotItem item )
            {
                if ( item != null )
                {
                    String text = dateFormatter.format(new Date((long) item.getDataPoint().getX()));

                    label.setText( text );
                    popup.setPopupPosition( item.getPageX() + 10, item.getPageY() - 25 );
                    popup.show();
                }
                else
                {
                    popup.hide();
                }
            }
        }, false );
        
        
        plotPanel.add(plot);
        plotPanel.add(legendPanel);
		
		return plotPanel;
	}
	
	/**
	 * This method returns current date without time part.
	 * 
	 * @return current date without time part
	 */
	private Date getCurrentDateWithoutTimePart() {
		return dateFormatter.parse(dateFormatter.format(new Date()));
	}
	
	/**
	 * Render vacations chart.
	 * 
	 * @param vacations - the list of vacations to display.
	 * @throws IllegalStateException if one of the SeriesHandler's is null
	 */
	public void renderPlot(List<Vacation> vacations) {
		
		model.removeAllSeries();
		
		int uniqueNumber = 1;
		
		Date currentDate = getCurrentDateWithoutTimePart();
		
		// Map that holds relation between username and id's on plot.
		usernames_PlotId_RelationMap = new HashMap<String, Integer>();
		
		for (Vacation vacation: vacations){
			String username = vacation.getUser().getUsername();
			
			String seriesName = vacation.getUser().getFullName() + 
					" [" + dateFormatter.format(vacation.getStartDate())
					+ " - " + dateFormatter.format(vacation.getEndDate()) + "]"; 
			
			Date startDate = vacation.getStartDate();
			if (startDate.before(currentDate)) {
				startDate = currentDate;
			}
			
			/*
			 * This part is necessary to draw vacations for the same user with same color. 
			 */
			if(usernames_PlotId_RelationMap.containsKey(username)) {
				int id = usernames_PlotId_RelationMap.get(username);
				
				SeriesHandler newSeriesHandler = model.addSeries(seriesName, colors[id-1]);
				
				newSeriesHandler.add(new DataPoint(startDate.getTime(), id));
				newSeriesHandler.add(new DataPoint(vacation.getEndDate().getTime(), id));
			} else {
				SeriesHandler newSeriesHandler = model.addSeries(seriesName, colors[uniqueNumber-1]);
				
				newSeriesHandler.add(new DataPoint(startDate.getTime(), uniqueNumber));
				newSeriesHandler.add(new DataPoint(vacation.getEndDate().getTime(), uniqueNumber));
				
				usernames_PlotId_RelationMap.put(username, uniqueNumber);
				uniqueNumber++;
			}
			
			datesSet.add(startDate);
			datesSet.add(vacation.getEndDate());
		}
		
		// If current date have not been added to the set yet, we add it
		datesSet.add(currentDate);
		
		// Redraw the plot
		if (plot != null) {
			plot.redraw();
		}
	}
	
	/**
	 * This method creates panel for search criteria choosing.
	 * 
	 * @return {@link HorizontalPanel} with all necessary elements.
	 */
/*	private HorizontalPanel createSearchCriteriaPanel() {
		// The panel with search criteria.
		HorizontalPanel searchCriteriaPanel = new HorizontalPanel();
		searchCriteriaPanel.setSpacing(10);
		
		Label spaceLabel = new Label("");	
		spaceLabel.setWidth("40px");
		searchCriteriaPanel.add(spaceLabel);
		
		dateRangeCheckbox = new CheckBox("Date range search");
		//dateRangeCheckbox.getElement().getStyle().setMarginTop(5, Unit.PX);
		searchCriteriaPanel.add(dateRangeCheckbox);
		
		searchStartDate = new DateBox();
		searchStartDate.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(Constants.DATE_FORMAT_STRING)));
		searchStartDate.setWidth("70px");
		searchStartDate.setHeight("12px");		
		searchStartDate.setVisible(false);
		searchCriteriaPanel.add(searchStartDate);
		
		final Label divideLabel = new Label(" - ");	
		divideLabel.setVisible(false);
		searchCriteriaPanel.add(divideLabel);
		
		searchEndDate = new DateBox();
		searchEndDate.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat(Constants.DATE_FORMAT_STRING)));
		searchEndDate.setWidth("70px");
		searchEndDate.setHeight("12px");
		searchEndDate.setVisible(false);
		searchCriteriaPanel.add(searchEndDate);
		
		dateRangeCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (event.getValue()) { // if checkbox is selected
					searchStartDate.setVisible(true);
					divideLabel.setVisible(true);
					searchEndDate.setVisible(true);
				} else {
					searchStartDate.setVisible(false);
					divideLabel.setVisible(false);
					searchEndDate.setVisible(false);
				}
			}
		});
		
		return searchCriteriaPanel;
	}*/
	
	/**
	 * This method updates content of the chart.
	 */
	private void update() {
		VacationsManager.getVacationsService().getActualVacations(new CustomAsyncCallback<List<Vacation>>() {

			@Override
			public void onSuccessExecution(List<Vacation> result) {
				renderPlot(result);
			}
		});
		
		
	}

	@Override
	public void updateContent() {
		update();
	}
}

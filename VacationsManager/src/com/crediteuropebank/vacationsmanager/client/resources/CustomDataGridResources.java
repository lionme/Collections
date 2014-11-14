package com.crediteuropebank.vacationsmanager.client.resources;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.DataGrid.Resources;

/**
 * 
 * This interface is necessary for applying custom styles to standart GWT {@link DataGrid}.
 * 
 * @author DIMAS
 *
 */
public interface CustomDataGridResources extends Resources {
	@Source({DataGrid.Style.DEFAULT_CSS, "resources/CustomDataGridStyles.css"})
	CustomStyle dataGridStyle();

	interface CustomStyle extends DataGrid.Style {
	}
}

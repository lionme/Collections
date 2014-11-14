package com.crediteuropebank.vacationsmanager.client.resources;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;

/**
 * 
 * This interface is necessary for applying custom styles to standart GWT {@link CellTable}.
 * 
 * @author DIMAS
 *
 */
public interface CustomCellTableResources extends Resources {
	@Source({CellTable.Style.DEFAULT_CSS, "resources/CustomCellTableStyles.css"})
    CustomStyle cellTableStyle();

    interface CustomStyle extends CellTable.Style {

    }
}

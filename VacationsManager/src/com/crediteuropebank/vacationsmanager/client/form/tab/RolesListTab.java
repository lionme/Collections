package com.crediteuropebank.vacationsmanager.client.form.tab;

import java.util.List;

import com.crediteuropebank.vacationsmanager.client.VacationsManager;
import com.crediteuropebank.vacationsmanager.client.callback.CustomAsyncCallback;
import com.crediteuropebank.vacationsmanager.client.observer.TypedObservable;
import com.crediteuropebank.vacationsmanager.client.observer.TypedObserver;
import com.crediteuropebank.vacationsmanager.client.widget.Dialog;
import com.crediteuropebank.vacationsmanager.client.widget.ListBoxField;
import com.crediteuropebank.vacationsmanager.client.widget.ObjectListBoxField;
import com.crediteuropebank.vacationsmanager.client.widget.TextField;
import com.crediteuropebank.vacationsmanager.shared.Constants;
import com.crediteuropebank.vacationsmanager.shared.Privilege;
import com.crediteuropebank.vacationsmanager.shared.domain.Role;
import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * 
 * This tab is used to display a list of all existed roles and to add/edit/delete roles.
 * Also this tab is used for setting role's privileges.
 * 
 * @author dimas
 *
 */
public class RolesListTab extends Composite implements CustomTab {
	
	/**
	 * 
	 * Data provider for user list table.
	 * 
	 * @author dimas
	 *
	 */
	private class RolesListAsynchDataProvider extends CustomAbstractDataProvider<Role> implements TypedObserver<Role> {
		
		private final HasData<Role> display;
		
		private List<Role> roles;
		
		public RolesListAsynchDataProvider(HasData<Role> display) {
			this.display = display;
		}
		
		/** 
		 * This function is necessary for correct work of paging.
		 * 
		 */
		@Override
		protected void onRangeChanged(HasData<Role> display) {
			
			final Range range = display.getVisibleRange();
			final int start = range.getStart();
			final int end = start + range.getLength();

			/*AsyncCallback<List<Role>> callback = new CustomAsyncCallback<List<Role>>() {

				@Override
				public void onSuccessExecution(List<Role> result) {
					if (result.size() < end) {
						updateRowData(start, result.subList(start, result.size())); 
					} else {
						updateRowData(start, result.subList(start, end));
					}
					
					updateRowCount(result.size(), true); 
				}
				
			};
			
			VacationsManager.getRoleService().getAllRoles(callback);*/
			
			if (roles != null) {
				if (roles.size() < end) {
					updateRowData(start, roles.subList(start, roles.size())); 
				} else {
					updateRowData(start, roles.subList(start, end));
				}
				
				updateRowCount(roles.size(), true); 
			}
		}

		@Override
		public void update(final List<Role> roles) {
			this.roles = roles;
			
			onRangeChanged(display);
		}
	}
	
	/**
	 * 
	 * This class extends custom list box field with observer functionality (for easy updating 
	 * with new data).
	 * 
	 * @author dimas
	 *
	 */
	private class RoleListBoxField extends ObjectListBoxField<Role> implements TypedObserver<Role> {

		public RoleListBoxField(String labelText) {
			super(labelText);
		}
		
		@Override
		public void update(List<Role> roles) {
			this.removeAllItems();
			
			// Add default value
			this.addItem(0L, "None", null);
			
			for (Role role: roles) {
				parentRoleLBF.addItem(role.getId(), role.getName(), role);
			}
			
			this.setSelectedIndex(0);
		}
	}
	
	/**
	 *  Width of the label of TextField
	 */
	private static final int ROLE_DETAILS_LABEL_WIDTH = 110;
	
	/**
	 *  Width of the whole TextField: width of the label + width of the text box.
	 */
	private static final int ROLE_DETAILS_TEXTFIELD_WIDTH = 300;
	
	/**
	 *  Width of the buttons on the role's details panel (string value)
	 */
	private static final String ROLE_DETAIL_BUTTONS_WIDTH = "60px";
	
	/**
	 * Width of the roles table.
	 */
	private static final String ROLES_TABLE_WIDTH = "800px";
	
	/**
	 * Height of the roles table.
	 */
	private static final String ROLES_TABLE_HEIGHT = "312px";
	
	/**
	 * Panel where will be placed all other elements.
	 */
	private final VerticalPanel mainPanel = new VerticalPanel();
	
	/**
	 * Selection model used in table.
	 */
	SingleSelectionModel<Role> selectionModel = null;
	
	/**
	 * This variable holds an instance of selected in the table vacation.
	 */
	private Role selectedRole;
	
	/* Role details elements. */
	private TextField roleNameTF;
	private TextField roleDescriptionTF;
	private RoleListBoxField parentRoleLBF;
	private ListBoxField privilegesLBF;
	
	/* Buttons for vacation's details panel. */
	private static Button roleDetailsCreateButton;
	private static Button roleDetailsUpdateButton;
	private static Button roleDetailsDeleteButton;
	private static Button roleDetailsClearButton;
	
	/**
	 * Data provider instance.
	 */
	private RolesListAsynchDataProvider roleDataProvider = null;
	
	/**
	 * Create Role Obserable. 
	 */
	private TypedObservable<Role> roleObservable = new TypedObservable<Role>();

	/**
	 * Vacations table.
	 */
	private DataGrid<Role> table;
	
	/**
	 * Private constructor. If you need to create new instance of the class then use static factory method.
	 */
	public RolesListTab() {
		mainPanel.setSpacing(5);
		
		// Panel where table's and detail's panels will be placed. 
		HorizontalPanel rolesPanel = new HorizontalPanel();
		
		rolesPanel.setSpacing(10);
		
		// Create vacations table panel
		VerticalPanel rolesTablePanel = createTablePanel();
		DecoratorPanel rolesTableDecorator = new DecoratorPanel();
		rolesTableDecorator.setWidget(rolesTablePanel);
		// Add vacations table to panel
		rolesPanel.add(rolesTableDecorator);
		
		// Create vacation details panel
		VerticalPanel roleDetailsPanel = createRoleDetailsPanel();
		DecoratorPanel roleDetailsDecorator = new DecoratorPanel();
		roleDetailsDecorator.setWidget(roleDetailsPanel);
		// Add user details panel
		rolesPanel.add(roleDetailsDecorator);
		
		mainPanel.add(rolesPanel);
		
		initWidget(mainPanel);
	}
	
	/**
	 * This method creates table panel with roles table.
	 * @return VerticalPanel with table inside.
	 */
	private VerticalPanel createTablePanel() {
		VerticalPanel tablePanel = new VerticalPanel();
		tablePanel.setSpacing(5);
		
	    /*
	     * Define a key provider for a Vacation. We use the unique ID as the key.
	     */
	    ProvidesKey<Role> keyProvider = new ProvidesKey<Role>() {
	      public Object getKey(Role item) {
	        // Always do a null check.
	        return (item == null) ? null : item.getId();
	      }
	    };
	    
		// Create a DataGrid.
		table = new DataGrid<Role>(Constants.TABLE_PAGE_SIZE, 
				VacationsManager.getCustomDataGridResources(), 
				keyProvider);

		table.setHeight(ROLES_TABLE_HEIGHT);
		table.setWidth(ROLES_TABLE_WIDTH);
	
		// Create id column.
		Column<Role, Number> idColumn = new Column<Role, Number>(new NumberCell()) {
			@Override
			public Number getValue(Role role) {
				return role.getId();
			}
		};
		
		// Create role name column.
		Column<Role, String> nameColumn = new Column<Role, String>(new TextCell()) {
			@Override
			public String getValue(Role role) {
				return role.getName();
			}
		};
		
		// Create role description column.
		Column<Role, String> descriptionColumn = new Column<Role, String>(new TextCell()) {
			@Override
			public String getValue(Role role) {
				return role.getDesription();
			}
		};
		
		// Create parent role column.
		Column<Role, String> parentRoleColumn = new Column<Role, String>(new TextCell()) {
			@Override
			public String getValue(Role role) {
				return (role.getParentRole()!=null)?role.getParentRole().getName():"None";
			}
		};
		
		// Create privilege column.
		Column<Role, String> privilegeColumn = new Column<Role, String>(new TextCell()) {
			@Override
			public String getValue(Role role) {
				return role.getPrivilege().toString();
			}
		};
		
		table.addColumn(idColumn, "ID");
		table.setColumnWidth(idColumn, 50, Unit.PX);
		table.addColumn(nameColumn, "Role name");
		table.setColumnWidth(nameColumn, 150, Unit.PX);
		table.addColumn(descriptionColumn, "Role description");
		table.setColumnWidth(descriptionColumn, 300, Unit.PX);
		table.addColumn(parentRoleColumn, "Parent role");
		table.setColumnWidth(parentRoleColumn, 150, Unit.PX);
		table.addColumn(privilegeColumn, "Privilege");
		table.setColumnWidth(privilegeColumn, 100, Unit.PX);
		
		roleDataProvider = new RolesListAsynchDataProvider(table);

		// Connect the table to the data provider.
		roleDataProvider.addDataDisplay(table);
		
		roleObservable.attach(roleDataProvider);
		
		// Add a selection model to handle user selection.
		selectionModel = new SingleSelectionModel<Role>();
		table.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				Role selected = selectionModel.getSelectedObject();
				if (selected != null) {
					fillRoleDetailsPanel(selected);
				}
			}
		});
		
		// Add table to the table panel
		tablePanel.add(table);
		
		// Create a pager
		SimplePager pager = new SimplePager();
		// Set our DataGrid table as display
		pager.setDisplay(table);
		
		DecoratorPanel pagerDecorator = new DecoratorPanel();
		pagerDecorator.setWidget(pager);
		
		// Set pager's location at center
		tablePanel.add(pagerDecorator);
		tablePanel.setCellHorizontalAlignment(pagerDecorator, HasHorizontalAlignment.ALIGN_CENTER);
		
		return tablePanel;
	}
	
	/**
	 * This method creates panel with role's details
	 * @return
	 */
	private VerticalPanel createRoleDetailsPanel() {
		VerticalPanel detailsPanel = new VerticalPanel();
		detailsPanel.setSpacing(5);
		
		roleNameTF = new TextField("Role name");
		roleNameTF.setWidth(ROLE_DETAILS_TEXTFIELD_WIDTH);
		roleNameTF.setLabelWidth(ROLE_DETAILS_LABEL_WIDTH);
		detailsPanel.add(roleNameTF);
		
		roleDescriptionTF = new TextField("Role description");
		roleDescriptionTF.setWidth(ROLE_DETAILS_TEXTFIELD_WIDTH);
		roleDescriptionTF.setLabelWidth(ROLE_DETAILS_LABEL_WIDTH);
		detailsPanel.add(roleDescriptionTF);
		
		parentRoleLBF = new RoleListBoxField("Parent role");
		parentRoleLBF.setWidth(ROLE_DETAILS_TEXTFIELD_WIDTH);
		parentRoleLBF.setLabelWidth(ROLE_DETAILS_LABEL_WIDTH);
		detailsPanel.add(parentRoleLBF);
		
		/* Add parent RoleListBoxField to observable to be observed. */
		roleObservable.attach(parentRoleLBF);
		
		privilegesLBF = new ListBoxField("Privilege");
		privilegesLBF.setWidth(ROLE_DETAILS_TEXTFIELD_WIDTH);
		privilegesLBF.setLabelWidth(ROLE_DETAILS_LABEL_WIDTH);
		detailsPanel.add(privilegesLBF);
		for (Privilege privilege: Privilege.values()) {
			privilegesLBF.addItem(privilege.toString(), privilege.toString());
		}
		privilegesLBF.setSelectedValue(Privilege.DEFAULT.toString());
		
		
		// Create panel with buttons
		HorizontalPanel buttonsPanel = new HorizontalPanel();
		buttonsPanel.setSpacing(5);

		roleDetailsCreateButton = new Button("Create");
		roleDetailsCreateButton.setWidth(ROLE_DETAIL_BUTTONS_WIDTH);
		roleDetailsCreateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveRole();
			}
		});
		buttonsPanel.add(roleDetailsCreateButton);

		roleDetailsUpdateButton = new Button("Update");
		roleDetailsUpdateButton.setWidth(ROLE_DETAIL_BUTTONS_WIDTH);
		roleDetailsUpdateButton.setEnabled(false); // disable by default
		roleDetailsUpdateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updateRole();
			}
		});
		buttonsPanel.add(roleDetailsUpdateButton);

		roleDetailsDeleteButton = new Button("Delete");
		roleDetailsDeleteButton.setWidth(ROLE_DETAIL_BUTTONS_WIDTH);
		roleDetailsDeleteButton.setEnabled(false); // disable by default
		roleDetailsDeleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteRole();
			}
		});
		buttonsPanel.add(roleDetailsDeleteButton);

		roleDetailsClearButton = new Button("Clear");
		roleDetailsClearButton.setWidth(ROLE_DETAIL_BUTTONS_WIDTH);
		roleDetailsClearButton.setEnabled(false); // disable by default
		roleDetailsClearButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clearRoleDetailsPanel();
			}
		});
		buttonsPanel.add(roleDetailsClearButton);

		DecoratorPanel buttonsDecorator = new DecoratorPanel();
		buttonsDecorator.setWidget(buttonsPanel);
		detailsPanel.add(buttonsDecorator);
		detailsPanel.setCellHorizontalAlignment(buttonsDecorator, 
				HasHorizontalAlignment.ALIGN_CENTER);
		
		return detailsPanel;
	}
	
	/**
	 * Fills details panel using info from input {@link Role} object.
	 * 
	 * @param selectedRole
	 */
	private void fillRoleDetailsPanel(Role selectedRole) {
		roleNameTF.setFieldValue(selectedRole.getName());
		roleDescriptionTF.setFieldValue(selectedRole.getDesription());
		parentRoleLBF.setSelectedObject((selectedRole.getParentRole()!=null)?selectedRole.getParentRole().getId():0L);
	
		this.selectedRole = selectedRole;
		
		changeButtonsStateToEdit();
	}
	
	/**
	 * Change state of the buttons (enable/disable them) for situation when we want to create
	 * new role record.
	 */
	private void changeButtonsStateToCreate() {
		roleDetailsCreateButton.setEnabled(true);
		roleDetailsUpdateButton.setEnabled(false);
		roleDetailsDeleteButton.setEnabled(false);
		roleDetailsClearButton.setEnabled(false);
	}
	
	/**
	 * Change state of the buttons (enable/disable them) for situation when we want to 
	 * update/delete selected role record.
	 */
	private void changeButtonsStateToEdit() {
		roleDetailsCreateButton.setEnabled(false);
		roleDetailsUpdateButton.setEnabled(true);
		roleDetailsDeleteButton.setEnabled(true);
		roleDetailsClearButton.setEnabled(true);
	}
	
	/**
	 * This method clears details panel.
	 */
	private void clearRoleDetailsPanel() {
		roleNameTF.setFieldValue("");
		roleDescriptionTF.setFieldValue("");
		parentRoleLBF.setSelectedIndex(0);
		
		privilegesLBF.setSelectedValue(Privilege.DEFAULT.toString());
		
		changeButtonsStateToCreate();
		
		deselectTableRow();
	}
	
	/**
	 * This method makes selected row in DataGrid unselected.
	 */
	private void deselectTableRow() {
		if (selectionModel != null) {
			Role selectedVacation = selectionModel.getSelectedObject();
			if (selectedVacation== null ) {
				return;
			}
			selectionModel.setSelected(selectedVacation, false);
		}
	}
	
	/**
	 * This method create new role record with entered parameters.
	 */
	private void saveRole() {
		Role role = new Role(roleNameTF.getFieldValue(),
				roleDescriptionTF.getFieldValue(),
				parentRoleLBF.getSelectedObject(),
				Privilege.valueOf(privilegesLBF.getSelectedValue()));
		
		VacationsManager.getRoleService().saveRole(role, new CustomAsyncCallback<Role>() {

			@Override
			public void onSuccessExecution(Role result) {
				refreshRoleData();

				clearRoleDetailsPanel();
			}
		});
	}
	
	/**
	 * This method updates selected role record with entered data.
	 */
	private void updateRole() {
		if (selectedRole != null) {
			selectedRole.setName(roleNameTF.getFieldValue());
			selectedRole.setDesription(roleDescriptionTF.getFieldValue());
			selectedRole.setParentRole(parentRoleLBF.getSelectedObject());
			selectedRole.setPrivilege(Privilege.valueOf(privilegesLBF.getSelectedValue()));
			
			VacationsManager.getRoleService().updateRole(selectedRole, new CustomAsyncCallback<Void>() {

				@Override
				public void onSuccessExecution(Void result) {
					refreshRoleData();

					clearRoleDetailsPanel();
				}
			});
		} else {
			Dialog.showSimpleMessage("You don't select role for updating!", 
					Dialog.TITLE_WARNING);
		}
	}
	
	/**
	 * This method deletes selected record from DB by its id.
	 */
	private void deleteRole() {
		if (selectedRole != null) {
			
			VacationsManager.getRoleService().deleteRole(selectedRole, new CustomAsyncCallback<Void>() {

				@Override
				public void onSuccessExecution(Void result) {
					refreshRoleData();

					clearRoleDetailsPanel();
				}
			});
		} else {
			Dialog.showSimpleMessage("You don't select role for deleting!", 
					Dialog.TITLE_WARNING);
		}	
	}

	@Override
	public void updateContent() {
		refreshRoleData();
	}
	
	/**
	 * This method refresh role's data in the screen.
	 */
	private void refreshRoleData() {
		
		CustomAsyncCallback<List<Role>> callback = new CustomAsyncCallback<List<Role>>() {

			@Override
			public void onSuccessExecution(List<Role> roles) {
				roleObservable.notifyObservers(roles);
			}
		};
		
		VacationsManager.getRoleService().getAllRoles(callback);
	}

}

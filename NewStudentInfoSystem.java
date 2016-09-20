import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

public class NewStudentInfoSystem extends JFrame {
	
	private static final long serialVersionUID = 7411363481203108843L;
	//########BEGIN JFRAME COMPONENTS########
	private final NewStudentInfoSystem self = this;
	private JMenuBar menuBar;
	private static final int numCVUMenus = 5;
	private JMenu[] createViewUpdateMenu = new JMenu[numCVUMenus];  //The JMenu objects added to the JFrame.
	private JMenu fileMenu,
				  enrollMenu,
	              gradesMenu,
	              reportsMenu;
	private JMenuItem[] createItems = new JMenuItem[numCVUMenus],  //JMenuItems added to the JMenu objects
		  	viewUpdateItems = new JMenuItem[numCVUMenus];
	private JMenuItem helpItem,
					  listScheduledClassesItem,
					  enrollItem,
				  	  addGradesItem,
					  viewGradesItem,
					  viewReportsItem;
	private JPanel masterPanel; //masterPanel is the only panel directly added onto the JFrame; to change panels, I only need to change the masterPanel reference.
	//########END JFRAME COMPONENTS########
	
	private final DBInterface db;
	private final String[] CVUcreateAction = {"Register","Create","Establish","Hire","Schedule"};
	private final String[] CVUkeys = {"R", "C", "E", "H", "S"};
	private final int[] CVUkeyEvents = {KeyEvent.VK_R, KeyEvent.VK_C, KeyEvent.VK_E, KeyEvent.VK_H, KeyEvent.VK_S};
	private final Pattern patUnderscore = Pattern.compile("[_]");
	
	public NewStudentInfoSystem() {
		Loader loading = new Loader();
		
		Thread loadThread = new Thread(loading);
		
		loadThread.start();
		
		db = new DBInterface();
		
		db.loadCache();
		
		waitForDB(this);
		
		setTitle("Student Information System");
		
		setMinimumSize(new Dimension(800,700));  //A minimum size for the JFrame, so the title always remains visible even when masterPanel changes.
		
		loading.end();
		try {
			loadThread.join();
		} catch (InterruptedException e1) {
			JOptionPane.showMessageDialog(null, "Error loading application.");
			System.exit(0);
		}
		
		buildMenuBar();
		
		buildHelpPanel();   //Set masterPanel to home panel
		
		setVisible(true);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		//The user is prompted to make sure they actually want to close the application, and before closing all streams are also closed.
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		    	int confirmExit = JOptionPane.showOptionDialog(
		             null, "Are you sure you want to close the application?", 
		             "Exit Confirmation", JOptionPane.YES_NO_OPTION, 
		             JOptionPane.QUESTION_MESSAGE, null, null, null);
		        if (confirmExit == 0) 
		           System.exit(0);
		    }
		});
	}
	
	private class Loader implements Runnable {
		private volatile JDialog dialog;
		public synchronized void end() {
			dialog.dispose();
		}
		public synchronized void run() {
			final JOptionPane optionPane = new JOptionPane("Please wait.", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
			dialog = new JDialog();
			dialog.setTitle("Loading...");
			dialog.setLocationRelativeTo(null);
			dialog.setContentPane(optionPane);
			dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			dialog.pack();
			
			dialog.setVisible(true);
		}
	}
	
	private void waitForDB(Object invoker) {
		try {
			synchronized(invoker) {
				if(!db.done)
					invoker.wait();
				while(!db.done) {}
				invoker.notify();
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//To change the masterPanel, first remove masterPanel from the JFrame, link it to a new reference, and add it back onto the JFrame.
	private void switchMasterPanelTo(JPanel panel) {
		remove(masterPanel);
		masterPanel = panel;
		add(masterPanel, BorderLayout.CENTER);
		revalidate();
		pack();
		setLocationRelativeTo(null);
	}
	
	//Builds the menu bar
	private void buildMenuBar() {
		menuBar = new JMenuBar();
		buildFileMenu();
		buildCreateViewUpdateMenus();
		buildEnrollMenu();
		buildGradesMenu();
		buildReportsMenu();
		menuBar.add(fileMenu);
		for(JMenu menu : createViewUpdateMenu) 
			menuBar.add(menu);
		menuBar.add(enrollMenu);
		menuBar.add(gradesMenu);
		menuBar.add(reportsMenu);
		setJMenuBar(menuBar);
	}
	private void buildFileMenu() {
		fileMenu = new formattedJMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_1);
		helpItem = new formattedJMenuItem("Help");
		helpItem.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				switchMasterPanelTo(new HelpPanel());
			}
		});
		fileMenu.add(helpItem); 
	}
	/**
	 * The following build__Menu methods instantiate the necessary JMenu and their child JMenuItems. The JMenuItems are chained to listeners, and added to the JMenu.
	 * Note a pattern: when a listener is instantiated once, I use an anonymous class.
	 *                 when instantiated multiple times, I create a separate inner class.
	 * I follow this pattern for many of the classes in this project in accordance with DRY and reducing the complexity of the code.
	 */
	private void buildCreateViewUpdateMenus() {
		String[] menuTitles = {"Student", "Course", "Department", "Professor", "Class"};
		int[] keyEvents = {KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6};
		for(int menuIndex = 0; menuIndex < numCVUMenus; menuIndex++) {
			createViewUpdateMenu[menuIndex] = new formattedJMenu(menuTitles[menuIndex]);
			createViewUpdateMenu[menuIndex].setMnemonic(keyEvents[menuIndex]);
			createItems[menuIndex] = new formattedJMenuItem(CVUcreateAction[menuIndex]);
			viewUpdateItems[menuIndex] = new formattedJMenuItem("View/Update");
			createItems[menuIndex].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Object source = e.getSource();
					for(int sourceIndex = 0; sourceIndex < numCVUMenus; sourceIndex++) {
						if(source == createItems[sourceIndex]) 
							switchMasterPanelTo(new CreateRowPanel(menuTitles[sourceIndex].toLowerCase(), CVUcreateAction[sourceIndex], CVUkeys[sourceIndex], CVUkeyEvents[sourceIndex]));
					}
				}
			});
			viewUpdateItems[menuIndex].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Object source = e.getSource();
					for(int sourceIndex = 0; sourceIndex < numCVUMenus; sourceIndex++) {
						if(source == viewUpdateItems[sourceIndex]) 
							switchMasterPanelTo(new ViewUpdateRowPanel(menuTitles[sourceIndex].toLowerCase()));
					}
				}
			});
			createViewUpdateMenu[menuIndex].add(createItems[menuIndex]);
			createViewUpdateMenu[menuIndex].add(viewUpdateItems[menuIndex]);
		}
		listScheduledClassesItem = new formattedJMenuItem("List by Course");
		listScheduledClassesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchMasterPanelTo(new ReportRowPanel("course","class"));
			}
		});
		createViewUpdateMenu[4].add(listScheduledClassesItem);
	}
	
	private void buildEnrollMenu() {
		enrollMenu = new formattedJMenu("Enroll");
		enrollMenu.setMnemonic(KeyEvent.VK_7);
		enrollItem = new formattedJMenuItem("Enroll");
		enrollItem.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				switchMasterPanelTo(new CreateRowPanel("enrollment", "Create", "c", KeyEvent.VK_C));
			}
		});
		enrollMenu.add(enrollItem);
	}
	
	private void buildGradesMenu() {
		gradesMenu = new formattedJMenu("Grades");
		gradesMenu.setMnemonic(KeyEvent.VK_8);
		addGradesItem = new formattedJMenuItem("Add");
		addGradesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchMasterPanelTo(new AddGradeRowPanel());
			}
		});
		viewGradesItem = new formattedJMenuItem("View");
		viewGradesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchMasterPanelTo(new ViewGradeRowPanel());
			}
		});
		gradesMenu.add(addGradesItem);
		gradesMenu.add(viewGradesItem);
	}
	
	private void buildReportsMenu() {
		reportsMenu = new formattedJMenu("Reports");
		reportsMenu.setMnemonic(KeyEvent.VK_9);
		viewReportsItem = new formattedJMenuItem("View");
		viewReportsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchMasterPanelTo(new ReportRowPanel("class", "enrollment"));
			}
		});
		reportsMenu.add(viewReportsItem);
	}
	
	private class formattedJMenu extends JMenu {
		private static final long serialVersionUID = -8388142119166461544L;
		{this.setFont(new Font("Serif",Font.BOLD,25));}
		public formattedJMenu(String title) {
			super(title);
		}
	}
	
	private class formattedJMenuItem extends JMenuItem {
		private static final long serialVersionUID = 3051424999489951006L;
		{this.setFont(new Font("Serif",Font.PLAIN,20));}
		public formattedJMenuItem(String title) {
			super(title);
		}
	}
	
	//This method creates the first instance of the HomePanel and links it to the masterPanel when the application begins. Only to be called once.
	private void buildHelpPanel() {
		masterPanel = new HelpPanel();
		add(masterPanel);
		revalidate();
		pack();
		setLocationRelativeTo(null);
	}
	
	//The home panel 
	private class HelpPanel extends JPanel {
		private static final long serialVersionUID = 8255640781278776089L;
		private GridBagConstraints c;
		private final LabelPanel title;
		private final LabelPanel welcomeLabel;
		private final LabelPanel[] directionLabels;
		private final String[] CVUlabels = {"Students", "Courses", "Departments", "Professors", "Classes"};
		private final String[] CVUitem = {"student", "course", "department", "professor", "class"}; 
		private String[] nonCVUdirectionText = {"Enroll: Enroll a student in a course.",
				"Grades: Add or view a grade.","Reports: View all students enrolled in a class."};
		public HelpPanel() {
			int numCVUs = CVUlabels.length,
			    numNonCVUs = nonCVUdirectionText.length,
			    numDirs = numCVUs + numNonCVUs;
			setLayout(new GridBagLayout());
			c = new GridBagConstraints();
			c.weighty = 1;
			title = new LabelPanel("Help");
			title.label.setFont(new Font(title.getFont().getName(), Font.BOLD, 30));
			welcomeLabel = new LabelPanel("Welcome to the Student Information System.");
			directionLabels = new LabelPanel[numDirs];
			for(int index = 0; index < numCVUs; index++)
				directionLabels[index] = new LabelPanel("(alt + " + (index + 1) + ") for " + CVUlabels[index] + ": " + CVUcreateAction[index] + " or view/update a " + CVUitem[index] + (index != 4 ? "" : ". Also list classes by course") + ".");
			for(int nonCVUindex = numCVUs; nonCVUindex < numDirs; nonCVUindex++) 
				directionLabels[nonCVUindex] = new LabelPanel("(alt + " + (nonCVUindex + 1) + ") for " + nonCVUdirectionText[nonCVUindex - numCVUs]);
			c.gridy = 0;
			add(title, c);
			c.gridy++;
			add(welcomeLabel,c);
			for(LabelPanel label : directionLabels) {
				++c.gridy;
				add(label,c);
			}
			pack();
		}
	}
	
	/**TemplatePanel provides a basic set of features that are inherited in all future panels:
	 *      -GridBagLayout is used to provide the greatest personal degree of control over design.
	 *      -Functions addAsNewRow(...), addAsMatrix(...) and addToCenter(...) allow for multiple components to be added exactly as stated 
	 *       without needing to deal directly with GridBagConstraints every time.
	 *      -There is a home button that allows the user to go back to the home panel/page, and a reset button that resets the panel using resetPanel().
	 *      -Function resetPanel() provides an easy, general way to clear or reset all the fields in a subclass instance at once.
	 *       resetPanel() uses reflection to create a new instance of the subclass object that originally called said method, and
	 *       masterPanel is then switched to this new instance, destroying the old reference.
	 *      -Note that resetPanel() requires that the subclass have a no-arg default constructor so that it can be found by reflection.
	 *       The constructors of inner classes implicitly put the instance of their outer class in their first argument, much like other languages like Python.
	 */
	private abstract class TemplatePanel extends JPanel {
		private static final long serialVersionUID = 702205217604505954L;
		protected final GridBagConstraints c;
		private final ButtonPanel resetButtonPanel;
		protected final LabelPanel purpose,
							       userGuide;
		protected final String table;
		protected ColumnMetaData[] colmd;
		protected int numCols;
		protected Map<String,String[]> importedKeyMap;
		public TemplatePanel(String table, String title, String purposeText, String userGuideText) {
			this.table = table;
			db.setUser(table);
			setLayout(new GridBagLayout());
			setBorder(BorderFactory.createTitledBorder(null,title,TitledBorder.CENTER, TitledBorder.TOP,new Font("Monospaced",Font.BOLD,30)));
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.VERTICAL;
			c.ipadx = 250;
			c.ipady = 10;
			resetButtonPanel = new ButtonPanel("Reset Fields (alt+X)");
			resetButtonPanel.button.setMnemonic(KeyEvent.VK_X);
			resetButtonPanel.button.setToolTipText("Reset all fields");
			resetButtonPanel.button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					resetPanel();
				}
			});
			purpose = new LabelPanel(purposeText);
			userGuide = new LabelPanel(userGuideText);
			addToCenter(resetButtonPanel);
			addToCenter(purpose, userGuide);
		};
		@Override
		public Component add(Component comp) {
			add(comp,c);
			return comp;
		}
		public Component[] addAsNewRow(Component... comps) {
			c.insets = new Insets(0,100,0,0);
			++c.gridy;
			for(Component comp: comps) 
				add(comp);
			c.insets = new Insets(0,0,0,0);
			return comps;
		}
		private Component[] addAsNewRowNoOffset(Component... comps) {
			++c.gridy;
			for(Component comp: comps) 
				add(comp);
			return comps;
		}
		public Component[] addToCenter(Component... comps) {
			int tmp = c.gridwidth;
			c.gridwidth = 2;
			for(Component comp : comps)
				addAsNewRowNoOffset(comp);
			c.gridwidth = tmp;
			return comps;
		}
		public void resetPanel() {
			try {
				switchMasterPanelTo((JPanel) this.getClass().getConstructor(NewStudentInfoSystem.class).newInstance(self));
			} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException exception) {
				JOptionPane.showMessageDialog(null, "Error: " + exception);
			}
		}
	}

	private final class CreateRowPanel extends TemplatePanel {
		private static final long serialVersionUID = -8069758043984919605L;
		private final ButtonPanel createButtonPanel;
		private final ComponentPanel[] fieldContainers;
		private final String[] values;
		private final String createAction,
							 key;
		private final int	 keyEvent;
		public CreateRowPanel(final String table, final String createAction, final String key, final int keyEvent) {
			super(table, createAction + " " + capitalize(table), createAction + " a new " + table + ".", "Fill out the fields below and click \"" + createAction + " " + capitalize(table) + "\".");
			this.createAction = createAction;
			this.key = key;
			this.keyEvent = keyEvent;
			colmd = db.getColumnMetaData(table, true);
			numCols = colmd.length;
			importedKeyMap = db.findImportedKeyNames(table);
			values = new String[numCols];
			fieldContainers = new ComponentPanel[numCols];
			
			//Implement and add field components
			decorateFields(fieldContainers, colmd, importedKeyMap, numCols, this, true);
			
			//Implement create button and register a listener to it
			createButtonPanel = new ButtonPanel(createAction + " " + capitalize(table) + " (alt+" + key + ")");
			createButtonPanel.button.setMnemonic(keyEvent);
			createButtonPanel.button.setToolTipText(createAction + " " + "a " + table);
			createButtonPanel.button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for(int col = 0; col < numCols; col++)
						values[col] = fieldContainers[col].getText();
					try {
						JOptionPane.showMessageDialog(null, "New " + table + " " + createAction.toLowerCase() + (createAction.charAt(createAction.length() - 1) != 'e' ? "e" : "") + "d.\n Your " + table + " ID is: " + db.insertRow(table, colmd, importedKeyMap, values) + ".\nRemember this ID!");
						resetPanel();
					} catch (HeadlessException | SQLException e1) {
						if(e1.getMessage().contains("Exception"))
							JOptionPane.showMessageDialog(null, e1.getMessage().split("Exception: ")[1]);
						else
							JOptionPane.showMessageDialog(null, e1.getMessage());
					}
				}
			});
			
			addToCenter(createButtonPanel);
		}
		@Override
		public void resetPanel() {
			try {
				switchMasterPanelTo((JPanel) this.getClass().getConstructor(NewStudentInfoSystem.class, String.class, String.class, String.class, int.class).newInstance(self, table, createAction, key, keyEvent));
			} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException exception) {
				JOptionPane.showMessageDialog(null, "Error: " + exception);
			}
		}
	}
	
	private final void decorateFields(ComponentPanel[] fieldContainers, ColumnMetaData[] colmd, Map<String, String[]> importedKeyMap, int numCols, TemplatePanel parent, boolean create) {
		int newRow = 0;
		String[] keyData;
		String colName;
		if(importedKeyMap != null) {
			for(int col = 0; col < numCols; col++) {
				colName = colmd[col].colName;
				keyData = importedKeyMap.get(colName);
				fieldContainers[col] = colmd[col].colName.compareTo("class_id") != 0 && importedKeyMap.containsKey(colName) ? new ComboBoxPanel(colName, db.listForeignIDsandNames(keyData[0], keyData[1])) :
									   !colmd[col].isBit ? new TextPanel(colName, colmd[col].colType, colmd[col].displaySize) :
									   new CheckBoxPanel(colName);
				if(!create)
					fieldContainers[col].setEnabled(false);
				if(newRow++ % 2 != 0)
					parent.add(fieldContainers[col]);
				else
					parent.addAsNewRow(fieldContainers[col]);
			}
		}
		else {
			for(int col = 0; col < numCols; col++) {
				fieldContainers[col] = new TextPanel(capitalize(colmd[col].colName), colmd[col].colType,  colmd[col].displaySize);
				if(!create)
					fieldContainers[col].setEnabled(false);
				if(newRow++ % 2 != 0)
					parent.add(fieldContainers[col]);
				else
					parent.addAsNewRow(fieldContainers[col]);
			}
		}
	}
	
	private class SearchPanel extends TemplatePanel {
		private static final long serialVersionUID = 3241690371644077513L;
		protected final TextPanel IDPanel;
		protected int ID;
		protected final ButtonPanel searchButtonPanel;
		protected final ColumnMetaData primaryKey;
		public SearchPanel(final String table, final String title, final String purposeText, final String userGuideText, final String buttonAction, final int keyEvent, final String toolText) {
			super(table, title, purposeText,userGuideText);
			
			//Implement and add IDPanel and searchButtonPanel.
			searchButtonPanel = new ButtonPanel(buttonAction); 
			searchButtonPanel.button.setMnemonic(keyEvent);
			searchButtonPanel.button.setToolTipText(toolText);
			primaryKey = db.findPrimaryKey(table);
			IDPanel = new TextPanel(capitalize(table + "_" + primaryKey.colName),primaryKey.colType,primaryKey.displaySize);
		}
	}
	
	private final class ViewUpdateRowPanel extends SearchPanel {
		private static final long serialVersionUID = -8069758043984919605L;
		private final ButtonPanel updateButtonPanel;
		private final ComponentPanel[] fieldContainers;
		private String[][] temp;
		private String[] selectValues;
		private List<String> updatedValues;
		private List<ColumnMetaData> updatedColmd;
		public ViewUpdateRowPanel(final String table) {
			super(table, "View/Update " + capitalize(table), "View or update an existing " + table + ".", "Search for a " + table + " by ID, update any necessary fields, and then click \"Update " + capitalize(table) + "\".", 
					"Search (alt+S)", KeyEvent.VK_S, "Search for a " + table + " record.");
			colmd = db.getColumnMetaData(table, false);
			numCols = colmd.length;
			importedKeyMap = db.findImportedKeyNames(table);
			fieldContainers = new ComponentPanel[numCols];
			
			//Add listener to searchButtonPanel
			searchButtonPanel.button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						ID = Integer.valueOf(IDPanel.textField.getText());
						temp = db.selectAllColumnsFromRows(ID, primaryKey.colName, table, false);
						if(temp == null)
							JOptionPane.showMessageDialog(null, "A " + table + " record with that ID does not exist.");
						else {
							selectValues = temp[0];
							for(int col = 0; col < numCols; col++) {
								fieldContainers[col].setText(selectValues[col]);	
								fieldContainers[col].setEnabled(colmd[col].editable);
							}
						}
					} catch(NumberFormatException ex) {
						JOptionPane.showMessageDialog(null, "ID field must be a number.");
					}
				}
			});
			addToCenter(IDPanel, searchButtonPanel);
			
			//Implement and add field components
			decorateFields(fieldContainers, colmd, importedKeyMap, numCols, this, false);
			
			//Implement and add updateButtonPanel with listener
			updateButtonPanel = new ButtonPanel("Update " + capitalize(table) + " (alt+E)");
			updateButtonPanel.button.setMnemonic(KeyEvent.VK_E);
			updateButtonPanel.setToolTipText("Update the " + table);
			updateButtonPanel.setEnabled(false);
			updateButtonPanel.button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updatedValues = new LinkedList<>();
					updatedColmd = new LinkedList<>();
					for(int col = 0; col < numCols; col++) {
						if(colmd[col].editable && fieldContainers[col].getText().compareTo(selectValues[col]) != 0) {
							updatedValues.add(fieldContainers[col].getText());
							updatedColmd.add(colmd[col]);
						}
					}
					try {
						db.updateRow(table, updatedColmd.toArray(new ColumnMetaData[updatedColmd.size()]), importedKeyMap, updatedValues.toArray(new String[updatedValues.size()]), primaryKey.colName, ID);
						JOptionPane.showMessageDialog(null, capitalize(table) + " ID " + ID + " has been updated.");
						resetPanel();
					} catch (SQLException e1) {
						if(e1.getMessage().contains("Exception"))
							JOptionPane.showMessageDialog(null, e1.getMessage().split("Exception: ")[1]);
						else
							JOptionPane.showMessageDialog(null, e1.getMessage());
					}
				}
			});
			addToCenter(updateButtonPanel);
		}
		@Override
		public void resetPanel() {
			try {
				switchMasterPanelTo((JPanel) this.getClass().getConstructor(NewStudentInfoSystem.class, String.class).newInstance(self, table));
			} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException exception) {
				JOptionPane.showMessageDialog(null, "Error: " + exception);
			}
		}
	}
	
	private abstract class GradeRowPanel extends SearchPanel {
		private static final long serialVersionUID = -8069758043984919605L;
		private String[][] temp;
		private String[] selectValues;
		protected final ComponentPanel[] fieldContainers;
		protected int gradeCol;
		public GradeRowPanel(boolean add) {
			super("enrollment", (add ? "Add " : "View ") + "Grade", (add ? "Add a grade to " : "View the grade of ") + "an existing enrollment record" + (add ? " that has not already been assigned a grade." : "."), 
					"Enter an enrollment record ID and click \"" + (add ? "Search\" to find it, and then click \"Add Grade\" to add a grade." : "View Grade\" to view its grade."),
					add ? "Search (alt+S)" : "View Grade (alt+V)", add ? KeyEvent.VK_S : KeyEvent.VK_V, add ? "Search for an enrollment record." : "View grade of enrollment record");
			colmd = db.getColumnMetaData(table, false);
			numCols = colmd.length;
			importedKeyMap = db.findImportedKeyNames(table);
			fieldContainers = new ComponentPanel[numCols];

			searchButtonPanel.button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						ID = Integer.valueOf(IDPanel.textField.getText());
						temp = db.selectAllColumnsFromRows(ID, primaryKey.colName, "enrollment", false);
						if(temp == null)
							JOptionPane.showMessageDialog(null, "An enrollment record with that ID does not exist.");
						else {
							selectValues = temp[0];
							if(add && selectValues[gradeCol].compareTo("IP") != 0)
								JOptionPane.showMessageDialog(null, "A grade has already been added to this enrollment record.");
							else {
								if(add) {
									for(int col = 0; col < numCols; col++) {
										fieldContainers[col].setText(selectValues[col]);	
										fieldContainers[col].setEnabled(colmd[col].editable);
									}
								}
								else {
									for(int col = 0; col < numCols; col++) 
										fieldContainers[col].setText(selectValues[col]);	
								}
							}
						}
					} catch(NumberFormatException ex) {
						JOptionPane.showMessageDialog(null, "ID field must be a number.");
					}
				}
			});
			
			addToCenter(IDPanel, searchButtonPanel);
			
			//Implement and add field components
			int newRow = 0;
			String[] keyData;
			String colName;
			for(int col = 0; col < numCols; col++) {
				colName = colmd[col].colName;
				keyData = importedKeyMap.get(colName);
				fieldContainers[col] = colmd[col].colName.compareTo("class_id") != 0 && importedKeyMap.containsKey(colName) ? new ComboBoxPanel(colName, db.listForeignIDsandNames(keyData[0], keyData[1])) :
					   				   !colmd[col].isBit ? new TextPanel(colName, colmd[col].colType, colmd[col].displaySize) :
					   				   new CheckBoxPanel(colName);
				if(!colmd[col].editable) {
					if(newRow++ % 2 == 0)
						addAsNewRow(fieldContainers[col]);
					else
						add(fieldContainers[col]);
				}
				else
					gradeCol = col;
					
				fieldContainers[col].setEnabled(false);
			}
			
			addToCenter(fieldContainers[gradeCol]);
		}
		@Override
		public void resetPanel() {
			try {
				switchMasterPanelTo((JPanel) this.getClass().getConstructor(NewStudentInfoSystem.class).newInstance(self));
			} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException exception) {
				JOptionPane.showMessageDialog(null, "Error: " + exception);
			}
		}
	}
	
	private final class AddGradeRowPanel extends GradeRowPanel {
		private static final long serialVersionUID = -5744873256323631286L;
		private ButtonPanel addGradeButtonPanel;
		public AddGradeRowPanel() {
			super(true);
			//Implement and add addGradePanel with listener
			addGradeButtonPanel = new ButtonPanel("Add grade (alt+A)");
			addGradeButtonPanel.button.setMnemonic(KeyEvent.VK_A);
			addGradeButtonPanel.setToolTipText("Add a grade to the enrollment record.");
			addGradeButtonPanel.setEnabled(false);
			addGradeButtonPanel.button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						db.updateRow("enrollment", new ColumnMetaData[]{colmd[gradeCol]}, importedKeyMap, new String[]{fieldContainers[gradeCol].getText()}, primaryKey.colName, ID);
						JOptionPane.showMessageDialog(null, "Grade has been added.");
						resetPanel();
					} catch (SQLException e1) {
						if(e1.getMessage().contains("Exception"))
							JOptionPane.showMessageDialog(null, e1.getMessage().split("Exception: ")[1]);
						else
							JOptionPane.showMessageDialog(null, e1.getMessage());
					}
				}
			});
			addToCenter(addGradeButtonPanel);
		}
	}
	
	private final class ViewGradeRowPanel extends GradeRowPanel {
		private static final long serialVersionUID = 7896151933529359136L;

		public ViewGradeRowPanel() {
			super(false);
		}
	}
	
	private final class ReportRowPanel extends SearchPanel {
		private static final long serialVersionUID = -3131829116280557602L;
		private final JScrollPane tablePane;
		private final JTable reportTable;
		private final TableColumnModel tcm;
		private final String[] colNames;
		private final String searchTable,	
							 dataTable;
		private int colWidth;
		private int xDim = 0;
		private String[][] values = null;
		public ReportRowPanel(final String searchTable, final String dataTable) {
			super(searchTable, "Report of " + capitalize(dataTable) + (dataTable.charAt(dataTable.length() - 1) != 's' ? "s " : "es ") + "by " + capitalize(searchTable), 
					"View all of the " + dataTable + (dataTable.charAt(dataTable.length() - 1) != 's' ? "s " : "es ") + "for a " + searchTable + ".", 
					"Enter a " + searchTable + " ID then click \"List (alt+L)\" to list all the " + dataTable + (dataTable.charAt(dataTable.length() - 1) != 's' ? "s " : "es ") + "for a " + searchTable + ".", "List (alt+L)", KeyEvent.VK_L, "");
			this.searchTable = searchTable;
			this.dataTable = dataTable;
			colmd = db.getColumnMetaData(dataTable, false);
			numCols = colmd.length;
			importedKeyMap = db.findImportedKeyNames(dataTable);
			colNames = new String[numCols];
			for(int col = 0; col < numCols; col++)
				colNames[col] = capitalize(colmd[col].colName);
			searchButtonPanel.button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					try {
						if((values = db.selectAllColumnsFromRows(Integer.parseInt(IDPanel.getText()), searchTable + "_" + primaryKey.colName, dataTable, true)) != null)
							reportTable.setModel(new DefaultTableModel(values, colNames));
						else
							JOptionPane.showMessageDialog(null, "There are no " + dataTable + " records with that " + searchTable + " ID.");
						for(int col = 0; col < numCols; col++) { 
							colWidth = colNames[col].length() * 17;
							if(colWidth > 150)
								colWidth = 150;
							if(colWidth < 30)
								colWidth = 30;
							tcm.getColumn(col).setPreferredWidth(colWidth);
							xDim += colWidth;
						}
					} catch(NumberFormatException ex) {
						JOptionPane.showMessageDialog(null, "ID field must be a number.");
					}
				}
			});
			addToCenter(IDPanel, searchButtonPanel);
			reportTable = new JTable(new String[][]{}, colNames);
			reportTable.setRowHeight(40);
			tcm = reportTable.getColumnModel();
			for(int col = 0; col < numCols; col++) { 
				colWidth = colNames[col].length() * 17;
				if(colWidth > 150)
					colWidth = 150;
				if(colWidth < 30)
					colWidth = 30;
				tcm.getColumn(col).setPreferredWidth(colWidth);
				xDim += colWidth;
			}
			
			reportTable.setPreferredSize(new Dimension(xDim,40));
			reportTable.getTableHeader().setFont(new Font("Sans Serif", Font.PLAIN, 25));
			reportTable.setFont(new Font("Sans Serif", Font.PLAIN, 20));
			tablePane = new JScrollPane(reportTable);
			tablePane.setPreferredSize(new Dimension(xDim + 100,200));
			reportTable.setPreferredScrollableViewportSize(reportTable.getPreferredSize());
			reportTable.setFillsViewportHeight(true);
			c.fill = GridBagConstraints.HORIZONTAL;
			addToCenter(tablePane);
		}
		@Override
		public void resetPanel() {
			try {
				switchMasterPanelTo((JPanel) this.getClass().getConstructor(NewStudentInfoSystem.class,String.class,String.class).newInstance(self,searchTable,dataTable));
			} catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException exception) {
				JOptionPane.showMessageDialog(null, "Error: " + exception);
			}
		}
	}
	
	
	private final String capitalize(String string) {
		Matcher match = patUnderscore.matcher(string);
		StringBuilder sb = new StringBuilder();
		String end;
		sb.append(string.substring(0, 1).toUpperCase());
		int afterLastMatch = 1;
		while(match.find()) {
			sb.append(string.substring(afterLastMatch, match.start()) + " ");
			sb.append(string.substring(match.start() + 1, match.end() + 1).toUpperCase());
			afterLastMatch += (match.start() - afterLastMatch) + 2;
		}
		if((end = string.substring(afterLastMatch)).compareTo("d") == 0 || end.compareTo("pa") == 0)
			sb.append(end.toUpperCase());
		else
			sb.append(end);
		return sb.toString();
	}

	private abstract class ComponentPanel extends JPanel {
		private static final long serialVersionUID = 1095181922808595510L;

		public abstract String getText();
		
		public abstract void setEnabled(boolean b);
		
		protected void setEnabled(boolean b, Component comp) {
			comp.setEnabled(b);
		}
		
		public abstract void setText(String text);
	}
	
	/**
	 * The following four panels provide a simple way to encapsulate a JComponent with a preset border, size, and/or dimension. 
	 * Being a panel also prevents the element from being fit to size and getting distorted/looking really ugly. 
	 */
	private class LabelPanel extends JPanel {
		private static final long serialVersionUID = -4826369702450694863L;
		private final JLabel label;
		public LabelPanel(String text) {
			label = new JLabel(text);
			label.setFont(new Font("Dialog", Font.BOLD, 25));
			add(label);
		}
	}
	
	private class TextPanel extends ComponentPanel {
		private static final long serialVersionUID = 6235603207216892144L;
		public final JTextFieldLimit textField;
		private final int type;
		public TextPanel(String title, int type, int size) {
			setLayout(new GridLayout(1,1,0,0));
			setBorder(BorderFactory.createTitledBorder(null,capitalize(title) + ":",TitledBorder.CENTER, TitledBorder.TOP,new Font("Monospaced",Font.BOLD,24)));
			textField = new JTextFieldLimit(size);
			textField.setPreferredSize(new Dimension(25,20));
			textField.setFont(new Font("Dialog", Font.PLAIN, 20));
			this.type = type;
			if(type == java.sql.Types.DATE)
				setText("YYYY-MM-DD");
			else if(type == java.sql.Types.TIME)
				setText("HH:MM:SS");
			add(textField);
		}
		public String getText() {
			return textField.getText();
		}
		@Override
		public void setEnabled(boolean b) {
			setEnabled(b, textField);
		}
		public void setText(String text) {
			if(type == java.sql.Types.DECIMAL) {
				try {
					textField.setText(String.valueOf(Integer.valueOf(text)));
				} catch(NumberFormatException e) {
					textField.setText(String.valueOf(Double.valueOf(text)));
				}
			}
			else
				textField.setText(text);
		}
	}
	
	private class ComboBoxPanel extends ComponentPanel {
		private static final long serialVersionUID = -1148243685670326825L;
		private final JComboBox<String> comboBox;
		private final JLabel title;
		private final String[] values;
		private final int numElements;
		public ComboBoxPanel(String type, String[] values) {
			this.values = values;
			numElements = values.length;
			setLayout(new GridLayout(2,1,0,0));
			title = new JLabel(capitalize(type) +":");
			comboBox = new JComboBox<String>(values);
			comboBox.setPreferredSize(new Dimension(10,30));
			comboBox.setFont(new Font("Dialog", Font.PLAIN, 20));
			comboBox.setMaximumRowCount(4);
			title.setPreferredSize(new Dimension(50,30));
			title.setFont(new Font("Dialog", Font.BOLD, 20));
			title.setHorizontalAlignment(JLabel.CENTER);
			add(title);
			add(comboBox);
		}
		public String getText() {
			return (String) comboBox.getSelectedItem();
		}
		@Override
		public void setEnabled(boolean b) {
			setEnabled(b, comboBox);
		}
		public void setText(String text) {
			for(int elem = 0; elem < numElements; elem++) {
				if(text.compareTo(values[elem].split(" ")[0]) == 0)
					comboBox.setSelectedIndex(elem);
			}
		}
	}
	
	private class CheckBoxPanel extends ComponentPanel {
		private static final long serialVersionUID = -3828709661065885325L;
		private final JCheckBox checkBox;
		public CheckBoxPanel(String title) {
			checkBox = new JCheckBox(capitalize(title));
			checkBox.setFont(new Font(checkBox.getFont().getName(), checkBox.getFont().getStyle(), 25));
			add(checkBox);
		}
		public String getText() {
			if(checkBox.isSelected())
				return "TRUE";
			else
				return "FALSE";
		}
		public void setText(String text) {
			if(Boolean.valueOf(text))
				checkBox.setSelected(true);
			else
				checkBox.setSelected(false);
		}
		public void setEnabled(boolean b) {
			setEnabled(b, checkBox);
		}
	}
	
	private class ButtonPanel extends JPanel {
		private static final long serialVersionUID = -870340853032754291L;
		private final JButton button;
		public ButtonPanel(String text) {
			button = new JButton(text);
			button.setPreferredSize(new Dimension(300,50));
			button.setFont(new Font("Dialog", Font.PLAIN, 20));
			add(button);
		}
	}
	
	/**
	 * The JTextFieldLimit class was taken from here: https://stackoverflow.com/questions/3519151/how-to-limit-the-number-of-characters-in-jtextfield
	 * This class is a subclass of JTextField that limits the maximum number of characters that can be typed into the text field. This ensures that
	 * input text does not exceed the fixed text length when writing to file.
	 */
	public class JTextFieldLimit extends JTextField {
		private static final long serialVersionUID = -5757799080643819444L;
		private int limit;

	    public JTextFieldLimit(int limit) {
	        super();
	        this.limit = limit;
	    }

	    @Override
	    protected Document createDefaultModel() {
	        return new LimitDocument();
	    }

	    private class LimitDocument extends PlainDocument {
			private static final long serialVersionUID = -4501963183965945655L;

			@Override
	        public void insertString( int offset, String  str, AttributeSet attr ) throws BadLocationException {
	            if (str == null) return;

	            if ((getLength() + str.length()) <= limit) {
	                super.insertString(offset, str, attr);
	            }
	        }       

	    }
	}
	
	private final class DBInterface {
		private final MysqlConnectionPoolDataSource ds;
		private final Properties connProps;
		private final Map<String,Integer> sqlToJavaType;
		private final Pattern validSQL = Pattern.compile("^[A-Za-z0-9_]+$");
		private final Map<String,AVLTree<RecordNode>> tableTree = new HashMap<>();
		private final Map<String,ColumnMetaData[]> tableToCColmd = new HashMap<>();
		private final Map<String,ColumnMetaData[]> tableToVUColmd = new HashMap<>();
		private final Map<String,ColumnMetaData> tableToPKey = new HashMap<>();
		private final Map<String,Map<String,String[]>> tableToImportedKeyMap = new HashMap<>();
		private final String[] tables = {"student", "course", "department", "professor", "class", "enrollment"};
		public boolean done = false;
		public DBInterface() {
			sqlToJavaType = new HashMap<String,Integer>();
		    for (Field field : Types.class.getFields()) {
		        try {
		        	sqlToJavaType.put(field.getName(),(Integer)field.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException("Could not load type map.");
				}
		    }
		    sqlToJavaType.put("ENUM", java.sql.Types.CHAR);
		    sqlToJavaType.put("MEDIUMINT", java.sql.Types.INTEGER);
		    sqlToJavaType.put("YEAR", java.sql.Types.SMALLINT);
			connProps = new Properties();
			try {
				connProps.load(new FileInputStream("connection.properties"));
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Cannot find connection properties file.");
			} catch (IOException e) {
				throw new RuntimeException("Cannot open connection properties file.");
			}
			System.setProperty("javax.net.ssl.keyStore",connProps.getProperty("keyStore"));
			System.setProperty("javax.net.ssl.keyStorePassword",connProps.getProperty("keyStorePass"));
			System.setProperty("javax.net.ssl.trustStore",connProps.getProperty("trustStore"));
			System.setProperty("javax.net.ssl.trustStorePassword",connProps.getProperty("trustStorePass"));
			ds = new MysqlConnectionPoolDataSource();
			ds.setServerName(connProps.getProperty("dburl"));
			try {
				ds.setPortNumber(Integer.valueOf(connProps.getProperty("port")));
			} catch (NumberFormatException e) {
				throw new RuntimeException("Port in connection properties file is not a number.");
			}
			ds.setDatabaseName(connProps.getProperty("dbname"));
			ds.setVerifyServerCertificate(true);
			ds.setUseSSL(true);
			ds.setRequireSSL(true);
		}
		private final Connection getConnection() throws SQLException {
			return ds.getConnection();
		}
		public final void setUser(String user) {
			ds.setUser(connProps.getProperty(user));
			ds.setPassword(connProps.getProperty(user + "_pass"));
		}
		public final void loadCache() {
			AVLTree<RecordNode> tempTree;
			for(String table : tables) {
				setUser(table);
				tempTree = new AVLTree<RecordNode>();
				tempTree.addAll(selectAllRowsFromTable(table));
				tableTree.put(table, tempTree);
				getColumnMetaData(table, true);
				getColumnMetaData(table, false);
				findPrimaryKey(table);
				findImportedKeyNames(table);
			}
			done = true;
		}
		public final int mapToJavaSQLType(String sqlType) {
			return sqlToJavaType.get(sqlType);
		}
		/**
		 * Returns an array of ColumnMetaData objects that provide the application with the name, type, display size, and Swing display
		 * type of a column. 
		 * @param table
		 * @return
		 */
		public final ColumnMetaData[] getColumnMetaData(String table, boolean forNewRecord) {
			ColumnMetaData[] tableColumnsMetaData = null;
			if(forNewRecord)
				tableColumnsMetaData = tableToCColmd.get(table);
			else
				tableColumnsMetaData = tableToVUColmd.get(table);
			if(tableColumnsMetaData != null) 
				return tableColumnsMetaData;
			else {
				ResultSet colRs = null,
						  updateColRs = null;
				String errMsg = null;
				
				if(!validSQL.matcher(table).matches()) 
					throw new IllegalArgumentException("Invalid table format: letters, numbers, and underscore only.");	
				try (Connection conn = getConnection();
					 PreparedStatement stmt = conn.prepareStatement("SELECT column_name, character_maximum_length, numeric_precision, data_type " +
							 										"FROM information_schema.columns " +
							 										"WHERE table_name=? AND privileges LIKE ?;");
				){
					String[] updateCols = null;
					int numUpdateCols = 0 ;
					if(!forNewRecord) {
						stmt.setString(1, table);
						stmt.setString(2, "%update%");
						updateColRs = stmt.executeQuery();
						if(updateColRs.last()) {
							numUpdateCols = updateColRs.getRow();
							int updateCol = 0;
							updateColRs.beforeFirst();
							updateCols = new String[numUpdateCols];
							while(updateColRs.next()) {
								updateCols[updateCol++] = updateColRs.getString(1);
							}
							updateColRs.close();
						}
					}
					stmt.setString(1, table);
					stmt.setString(2, forNewRecord ? "%insert%" : "%select%");
					colRs = stmt.executeQuery();
					if(colRs.last()) {
						int numCols = colRs.getRow(),
							updateCol = 0,
							col = 0,
							type;
						colRs.beforeFirst();
						tableColumnsMetaData = new ColumnMetaData[numCols];
						if(updateCols != null) {
							while(colRs.next()) { 
								if(updateCol < numUpdateCols && updateCols[updateCol].compareTo(colRs.getString(1)) == 0) {
									tableColumnsMetaData[col++] = new ColumnMetaData(colRs.getString(1),colRs.getInt(2),colRs.getInt(3),colRs.getString(4),type = mapToJavaSQLType(colRs.getString(4).toUpperCase()),true, type == java.sql.Types.BIT);
									updateCol++;
								}
								else
									tableColumnsMetaData[col++] = new ColumnMetaData(colRs.getString(1),colRs.getInt(2),colRs.getInt(3),colRs.getString(4),type = mapToJavaSQLType(colRs.getString(4).toUpperCase()),false, type == java.sql.Types.BIT);
							}
						}
						else {
							while(colRs.next())
								tableColumnsMetaData[col++] = new ColumnMetaData(colRs.getString(1),colRs.getInt(2),colRs.getInt(3),colRs.getString(4),type = mapToJavaSQLType(colRs.getString(4).toUpperCase()),true, type == java.sql.Types.BIT);
						}
						if(forNewRecord)
							tableToCColmd.put(table, tableColumnsMetaData);
						else
							tableToVUColmd.put(table, tableColumnsMetaData);
					}
					return tableColumnsMetaData;
				} catch (SQLException e) {
					errMsg = e.getMessage();
					throw new RuntimeException(e);
				} finally {
					try {
						if(colRs != null)
							colRs.close();
					} catch (SQLException e) {
						e.printStackTrace();
						if(errMsg != null)
							throw new RuntimeException(errMsg);
					}
				}
			}
		}
		
		public final RecordNode[] selectAllRowsFromTable(String table) {
			List<RecordNode> nodes = new LinkedList<>();
			String[] values = null;
			ResultSet rowResult = null;
			String errMsg = null;
			if(!validSQL.matcher(table).matches()) //Prevent SQL injection
				throw new IllegalArgumentException("Invalid table format: letters, numbers, and underscore only.");
			try (Connection conn = getConnection();
			     PreparedStatement selectRow = conn.prepareStatement("SELECT * FROM " + table);
			){
				String pKey = findPrimaryKey(table).colName;
				ColumnMetaData[] colmd = getColumnMetaData(table, false);
				rowResult = selectRow.executeQuery();
				if(rowResult.last()) {
					int numRows = rowResult.getRow();
					rowResult.beforeFirst();
					ResultSetMetaData rowmd = rowResult.getMetaData();
					int numCols = rowmd.getColumnCount();
					while(rowResult.next()) {
						int ID = -1,
							lastID = ID;
						values = new String[numCols];
						for(int row = 0; row < numRows; row++) {
							lastID = ID;
							for(int col = 0; col < numCols; col++) {
								values[col] = String.valueOf(rowResult.getObject(col + 1));
								if(colmd[col].colName.compareTo(pKey) == 0)
									ID = rowResult.getInt(col + 1);
							}
							if(ID != -1 && lastID != ID)
								nodes.add(new RecordNode(ID, values));
						}
					}
				}
				return nodes.toArray(new RecordNode[nodes.size()]);
			} catch (SQLException e) {
				errMsg = e.getMessage();
				throw new RuntimeException(e);
			} finally {
				if(rowResult != null) {
					try {
						rowResult.close();
					} catch (SQLException e) {
						e.printStackTrace();
						if(errMsg != null)
							throw new RuntimeException(errMsg);
					}
				}
			}
		}
		
		public final String[][] selectAllColumnsFromRows(int id, String pKey, String table, boolean mustQuery) {
			ResultSet rowResult = null;
			String errMsg = null;
			RecordNode rn = null;
			if(!mustQuery) 
				rn = tableTree.get(table).get(new RecordNode(id, null));
			if(rn != null)
				return new String[][] {rn.values};
			else {
				String[][] values = null;
				if(!validSQL.matcher(table).matches() || !validSQL.matcher(pKey).matches()) //Prevent SQL injection
					throw new IllegalArgumentException("Invalid table or primary key format: letters, numbers, and underscore only.");
				try (Connection conn = getConnection();
				     PreparedStatement selectRow = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + pKey + "=?");
				){
					selectRow.setInt(1, id);
					rowResult = selectRow.executeQuery();
					if(rowResult.last()) {
						int numRows = rowResult.getRow();
						int row = 0;
						rowResult.beforeFirst();
						ResultSetMetaData rowmd = rowResult.getMetaData();
						int numCols = rowmd.getColumnCount();
						values = new String[numRows][numCols];
						while(rowResult.next()) {
							for(int col = 0; col < numCols; col++)
								values[row][col] = String.valueOf(rowResult.getObject(col + 1));
							row++;
						}
					}
					return values;
				} catch (SQLException e) {
					errMsg = e.getMessage();
					throw new RuntimeException(e);
				} finally {
					if(rowResult != null) {
						try {
							rowResult.close();
						} catch (SQLException e) {
							e.printStackTrace();
							if(errMsg != null)
								throw new RuntimeException(errMsg);
						}
					}
				}
			}
		}
		
		public final int insertRow(String table, ColumnMetaData[] colmd, Map<String, String[]> foreignKeyMap, String[] values) throws SQLException {
			int numCols = colmd.length,
			    currentCol = 0;
			if(numCols != values.length)
				throw new IllegalArgumentException("Number of columns does not match number of inserted values.");
			if(!validSQL.matcher(table).matches()) 
				throw new IllegalArgumentException("Invalid table format: letters, numbers, and underscore only.");
			String columns = "(",
				   value = "VALUES(";
			ResultSet rsID = null;
			for(int col = 0; col < numCols; col++) {
				if(!validSQL.matcher(colmd[col].colName).matches()) throw new IllegalArgumentException("Invalid format for " + colmd[col].colName + ": letters, numbers, and underscore only.");
				columns += colmd[col].colName + (col + 1 < numCols ? ", " : ")");
				value += "?" + (col + 1 < numCols ? ", " : ")");
			}
			try (Connection conn = getConnection();
			     PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + table +
			    		 										" " + columns +
			    		 										" " + value);
			     PreparedStatement getID = conn.prepareStatement("SELECT LAST_INSERT_ID()");
			) {
				for(int col = 0; col < numCols; col++) {
					if(values[col].isEmpty()) throw new SQLException(capitalize(colmd[currentCol].colName) + " is empty.");
					if(foreignKeyMap.containsKey(colmd[col].colName)) 
						stmt.setObject(col + 1, values[col].split(" ")[0], colmd[col].colType);
					else
						stmt.setObject(col + 1, values[col], colmd[col].colType);
					currentCol++;
				}
				stmt.executeUpdate();
				rsID = getID.executeQuery();
				rsID.next();
				tableTree.get(table).add(new RecordNode(rsID.getInt(1), selectAllColumnsFromRows(rsID.getInt(1), findPrimaryKey(table).colName, table, true)[0]));
				return rsID.getInt(1);
			} catch (SQLException e) {
				if(e.getMessage().contains("Cannot convert") || e.getMessage().contains("Data truncated"))
					throw new SQLException(capitalize(colmd[currentCol].colName) + " must be of type " + capitalize(colmd[currentCol].colTypeName) + ".");
				else
					throw new SQLException(e);
			} finally {
				if(rsID != null) {
					try {
						rsID.close();
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		private final void updateRow(String table, ColumnMetaData[] colmd, Map<String, String[]> foreignKeyMap, String[] values, String primaryKey, int ID) throws SQLException {
			int numCols = colmd.length,
				    	  currentCol = 0;
			if(numCols != values.length)
				throw new IllegalArgumentException("Number of columns does not match number of updated values.");
			if(!validSQL.matcher(table).matches() || !validSQL.matcher(primaryKey).matches()) 
				throw new IllegalArgumentException("Invalid table or primary key format: letters, numbers, and underscore only.");
			String setValues = "SET ";
			for(int col = 0; col < numCols; col++) {
				if(!validSQL.matcher(colmd[col].colName).matches()) throw new IllegalArgumentException("Invalid format for " + colmd[col].colName + ": letters, numbers, and underscore only.");
				setValues += colmd[col].colName + "=?" + (col + 1 != numCols ? ", " : "");
			}
			try (Connection conn = getConnection();
			     PreparedStatement stmt = conn.prepareStatement("UPDATE " + table +
			    		 										" " + setValues +
			    		 										" WHERE " + primaryKey + "=?")
			) {
				for(int col = 0; col < numCols; col++) {
					if(values[col].isEmpty()) throw new SQLException(capitalize(colmd[currentCol].colName) + " is empty.");
					if(foreignKeyMap.containsKey(colmd[col].colName)) 
						stmt.setObject(col + 1, values[col].split(" ")[0], colmd[col].colType);
					else
						stmt.setObject(col + 1, values[col], colmd[col].colType);
					currentCol++;
				}
				stmt.setInt(numCols + 1, ID);
				stmt.executeUpdate();
				tableTree.get(table).set(new RecordNode(ID, values));
			} catch (SQLException e) {
				if(e.getMessage().contains("Cannot convert") || e.getMessage().contains("Data truncated"))
					throw new SQLException(capitalize(colmd[currentCol].colName) + " must be of type " + capitalize(colmd[currentCol].colTypeName) + ".");
				else
					throw new SQLException(e);
			}		
		}
		
		public final ColumnMetaData findPrimaryKey(String table) {
			ColumnMetaData primaryKeyData = tableToPKey.get(table);
			if(primaryKeyData != null)
				return primaryKeyData;
			else {
				ResultSet primaryKeyRs = null;
				String errMsg = null;
				if(!validSQL.matcher(table).matches()) 
					throw new IllegalArgumentException("Invalid table format: letters, numbers, and underscore only.");
				try (Connection conn = getConnection();
				     PreparedStatement stmt = conn.prepareStatement("SELECT column_name, character_maximum_length, numeric_precision, data_type " +
																	"FROM information_schema.columns " +
																	"WHERE table_name=? AND column_key='PRI';");
				){
					stmt.setString(1, table);
					primaryKeyRs = stmt.executeQuery();
					primaryKeyRs.next();
					primaryKeyData = new ColumnMetaData(primaryKeyRs.getString(1),primaryKeyRs.getInt(2),primaryKeyRs.getInt(3), primaryKeyRs.getString(4), mapToJavaSQLType(primaryKeyRs.getString(4).toUpperCase()), true, false);
					tableToPKey.put(table,primaryKeyData);
					return primaryKeyData;
				} catch (SQLException e) {
					errMsg = e.getMessage();
					throw new RuntimeException(e);
				} finally {
					if(primaryKeyRs != null){
						try {
							primaryKeyRs.close();
						} catch (SQLException e) {
							e.printStackTrace();
							if(errMsg != null)
								throw new RuntimeException(errMsg);
						}
					}
				}
			}
		}
		
		public final Map<String,String[]> findImportedKeyNames(String table) {
			Map<String, String[]> importedKeyMap = tableToImportedKeyMap.get(table);
			if(importedKeyMap != null)
				return importedKeyMap;
			else {
				ResultSet importedKeyRs = null;
				String errMsg = null;
				if(!validSQL.matcher(table).matches()) 
					throw new IllegalArgumentException("Invalid table format: letters, numbers, and underscore only.");
				try (Connection conn = getConnection();
				     PreparedStatement stmt = conn.prepareStatement("SELECT column_name, referenced_table_name, referenced_column_name " +
				    		 										"FROM information_schema.key_column_usage " +
				    		 										"WHERE table_name=? AND referenced_table_name IS NOT NULL AND column_name!='class_id'");
				){
					stmt.setString(1, table);
					importedKeyRs = stmt.executeQuery();
					importedKeyMap = new HashMap<>();
					while(importedKeyRs.next()) {
						importedKeyMap.put(importedKeyRs.getString(1), new String[]{importedKeyRs.getString(2), importedKeyRs.getString(3)});
					}
					tableToImportedKeyMap.put(table, importedKeyMap);
					return importedKeyMap;
				} catch (SQLException e) {
					errMsg = e.getMessage();
					throw new RuntimeException(e);
				} finally {
					if(importedKeyRs != null) {
						try {
							importedKeyRs.close();
						} catch (SQLException e) {
							e.printStackTrace();
							if(errMsg != null)
								throw new RuntimeException(errMsg);
						}
					}
				}		
			}
		}
		
		private String[] listForeignIDsandNames(String foreignTable, String foreignColName) {
			String[] result = null;
			if(!validSQL.matcher(foreignTable).matches() || !validSQL.matcher(foreignColName).matches())
				throw new IllegalArgumentException("Invalid foreign table name or foreign column name format. Letters, numbers, or underscores only.");
			String sql = "SELECT " + foreignTable + "." + foreignColName + "," + foreignTable + ".name " +
			             "FROM " + foreignTable;
			try (Connection conn = ds.getConnection();
				 Statement stmt = conn.createStatement();
			     ResultSet IDandNameRs = stmt.executeQuery(sql);
			) {
				if(IDandNameRs.last()) {
					int numIDsAndNames = IDandNameRs.getRow(),
						row = 0;
					IDandNameRs.beforeFirst();
					result = new String[numIDsAndNames];
					while(IDandNameRs.next()) {
						result[row++] = IDandNameRs.getString(1) + " - " + IDandNameRs.getString(2);
					}
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			return result;
		}
    }
	
	public final class ColumnMetaData {
		public final String colName,
							colTypeName;
		public final int displaySize,
						 colType;
		public final boolean editable,
							 isBit;
		public ColumnMetaData(String colName, int displaySizeString, int displaySizeNumeric, String colTypeName, int colType, boolean editable, boolean isBit) {
			this.colName = colName;
			this.displaySize = displaySizeString != 0 ? displaySizeString :
							   displaySizeNumeric != 0 ? displaySizeNumeric : 20;
			this.colTypeName = colTypeName;
			this.colType = colType;
			this.editable = editable;
			this.isBit = isBit;
		}
	}
	
	public final class RecordNode implements Comparable<RecordNode> {
		public final int ID;
		public String[] values;
		public RecordNode(int ID, String[] values) {
			this.ID = ID;
			this.values = values;
		}
		public int compareTo(RecordNode other) {
			if(ID < other.ID)
				return -1;
			else if(ID == other.ID)
				return 0;
			return 1;
		}
	}
	
	public static void main(String[] args) {
		new NewStudentInfoSystem();
	}
}
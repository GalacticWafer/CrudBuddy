import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

class TableFormatter extends JTable {
	private String[] columnNames;
	private JTable table;
	private Dimension scrollPanelSize;
	private boolean[] isHeaderMinimalArray;
	public static final int NUMBER_OF_ROWS_TO_DISPLAY = 25;
	public static int MINIMUM_COLUMN_WIDTH;
	public static int maximumGuiWidth = 600;
	private Crud crud;
	private String tableName;
	private String databaseName;
	
	/**
	 * TableFormatter was made specifically for this project, and is used by the class PlaylistGui
	 * to present Song information in a table. However, it can be fairly easily modified for use
	 * with other tabular representations of data.
	 * <p>
	 * isHeaderMinimalArray: an array of boolean values representing whether or not each column
	 * should be minimized or not (the rank column should always be minimal).
	 * <p>
	 * MINIMUM_COLUMN_WIDTH: if (isHeaderMinimalArray[i] = true), then that column will only be
	 * wide enough to accommodate the widest element in that column
	 * <p>
	 * headers: Names of the columns -> Artist | Title | Rank
	 * <p>
	 * NUMBER_OF_ROWS_TO_DISPLAY: arbitrary number of rows for a reasonably-tall table. Scroll to
	 * see more rows than this.
	 * <p>
	 * MAXIMUM_GUI_WIDTH: arbitrary number of rows for a reasonably-wide table. If the total width
	 * of all content is too wide to be fully accommodated here, the remaining width after minimal
	 * width columns have been subtracted will be evenly split among remaining columns.
	 */
	public TableFormatter(Object[][] tableData, String[] columnNames, Crud crud) {
		this.crud = crud;
		this.columnNames = columnNames;
		isHeaderMinimalArray = getMinimalHeaderBooleans();
		
		// Make a DefaultTableModel interface instead of a normal table model one, and use it to 
		// make sure cells are not editable (read-only).
		DefaultTableModel tableModel = new DefaultTableModel() {
			@Override public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		tableModel.setDataVector(tableData, columnNames);
		table = new JTable(tableModel) {
			@Override public Component prepareRenderer(TableCellRenderer renderer, int row,
			 int column) {
				Component component = super.prepareRenderer(renderer, row, column);
				int rendererWidth = component.getPreferredSize().width;
				TableColumn tableColumn = getColumnModel().getColumn(column);
				tableColumn.setPreferredWidth(Math.max(
				 rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
				return component;
			}
		};
			scrollPanelSize = null;
			MINIMUM_COLUMN_WIDTH =
			
			getMinWidth() +1;
		}
		
		// get the preferred width of the skinniest column
		private int getMinWidth () {
			int min = 0;
			for(String header: columnNames) {
				Math.min(header.length(), min);
			}
			return table.getColumnModel().getColumn(min).getPreferredWidth();
		}
		
		public JTable getTable () {
			return table;
		}
		
		// get an array of columns widths
		private int[] calculateColumnWidths () {
			int[] array = new int[columnNames.length];
			int nonMinimalHeadersCount = 0;
			int minimalHeadersCount = 0;
			// find out how many columns don't want to be minimal.
			for(int i = 0; i < table.getColumnCount(); i++) {
				if(!isHeaderMinimalArray[i]) {
					nonMinimalHeadersCount++;
				}
				else {
					minimalHeadersCount++;
				}
			}
			
			// Calculate how much width goes to each column
			int totalMinimizedWidth = MINIMUM_COLUMN_WIDTH * minimalHeadersCount;
			int nonMinimizedWidth = maximumGuiWidth - totalMinimizedWidth;
			int nonMinimizedColumnWidth = nonMinimizedWidth / nonMinimalHeadersCount;
			isHeaderMinimalArray = getMinimalHeaderBooleans();
			for(int i = 0; i < table.getColumnCount(); i++) {
				if(isHeaderMinimalArray[i]) {
					array[i] = MINIMUM_COLUMN_WIDTH;
				}
				else {
					array[i] = nonMinimizedColumnWidth;
				}
				table.getColumnModel().getColumn(i).setMinWidth(array[i]);
				table.getColumnModel().getColumn(i).setPreferredWidth(array[i]);
			}
			return array;
		}
		
		private void calculateColumnWidths ( int extraWidth){
			int[] array = calculateColumnWidths();
			for(int i = 0; i < array.length; i++) {
				array[i] += extraWidth;
				table.getColumnModel().getColumn(i).setMinWidth(array[i]);
				table.getColumnModel().getColumn(i).setPreferredWidth(array[i]);
			}
		}
		
		// calculate how tall the gui will be
		private int calculateContentHeight () {
			int rowHeight = table.getRowHeight() * (NUMBER_OF_ROWS_TO_DISPLAY);
			int interCellHeight =
			 table.getIntercellSpacing().height * (NUMBER_OF_ROWS_TO_DISPLAY + 1);
			return rowHeight + interCellHeight + tableHeader.getPreferredSize().height;
		}
		
		// used in PlaylistGUI for sizing the frame
		public Dimension getScrollPanelSize () {
			return scrollPanelSize;
		}
		
		// used to update the table any time sorting or filtering has changed.
		public void setData () {
			calculateColumnWidths();
			scrollPanelSize = new Dimension(maximumGuiWidth, calculateContentHeight() + 1);
		}
		public void setData ( int extraWidth){
			setData();
			calculateColumnWidths(extraWidth);
			maximumGuiWidth += extraWidth;
			scrollPanelSize = new Dimension(maximumGuiWidth, calculateContentHeight() + 1);
		}
		
		/**
		 * Called by TableFormatter class to display the info in a table with proper column names.
		 *
		 * @return A boolean arayy where the column name with the shortest sting length should
		 * be minimal, therefore its value should be * true(minimal), and others should be false
		 * (not
		 * minimal).
		 */
		public boolean[] getMinimalHeaderBooleans () {
			boolean[] temp = new boolean[columnNames.length];
			int current = Integer.MAX_VALUE;
			int smallestIndex = 0;
			for(int i = 0; i < columnNames.length; i++) {
				if(current > columnNames[i].length()) {
					temp[i] = columnNames[i].length() < current;
					current = columnNames[i].length();
				}
			}
			for(int i = 0; i < columnNames.length; i++) {
				temp[i] = current == smallestIndex;
			}
			return temp;
		}
	}

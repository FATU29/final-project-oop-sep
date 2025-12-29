package com.sep.framework.crud;

import com.sep.framework.database.ColumnInfo;
import com.sep.framework.database.DatabaseContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Template Method Pattern: Base CRUD Form
 * Form cơ sở cung cấp các tính năng CRUD tự động
 * Các form khác chỉ cần kế thừa và cung cấp table name
 */
public abstract class BaseCrudForm extends JFrame {
    
    protected DatabaseContext dbContext;
    protected String tableName;
    protected String primaryKeyColumn;
    
    // UI Components
    protected JTable dataTable;
    protected DefaultTableModel tableModel;
    protected JScrollPane scrollPane;
    protected JButton btnAdd;
    protected JButton btnDelete;
    protected JButton btnRefresh;
    protected JPopupMenu contextMenu;
    
    // Data
    protected List<ColumnInfo> columns;
    protected List<Map<String, Object>> data;
    protected Map<String, JComponent> inputFields;
    
    public BaseCrudForm(DatabaseContext dbContext, String tableName) {
        this.dbContext = dbContext;
        this.tableName = tableName;
        this.inputFields = new LinkedHashMap<>();
        
        initialize();
        loadTableStructure();
        setupUI();
        loadData();
    }
    
    /**
     * Template Method: Khởi tạo form
     */
    private void initialize() {
        setTitle("Quản lý " + tableName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
    }
    
    /**
     * Load cấu trúc bảng từ database
     */
    private void loadTableStructure() {
        try {
            columns = dbContext.getColumns(tableName);
            primaryKeyColumn = dbContext.getPrimaryKey(tableName);
            
            if (primaryKeyColumn == null && !columns.isEmpty()) {
                // Nếu không có primary key, dùng cột đầu tiên
                primaryKeyColumn = columns.get(0).getName();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi tải cấu trúc bảng: " + e.getMessage(), 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Template Method: Setup UI components
     */
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Toolbar
        JToolBar toolBar = createToolBar();
        add(toolBar, BorderLayout.NORTH);
        
        // Table
        setupTable();
        add(scrollPane, BorderLayout.CENTER);
        
        // Context menu
        setupContextMenu();
    }
    
    /**
     * Template Method: Tạo toolbar
     */
    protected JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        btnAdd = new JButton("Thêm");
        btnAdd.addActionListener(e -> showAddForm());
        toolBar.add(btnAdd);
        
        btnDelete = new JButton("Xóa");
        btnDelete.addActionListener(e -> deleteSelected());
        toolBar.add(btnDelete);
        
        toolBar.addSeparator();
        
        btnRefresh = new JButton("Làm mới");
        btnRefresh.addActionListener(e -> loadData());
        toolBar.add(btnRefresh);
        
        return toolBar;
    }
    
    /**
     * Template Method: Setup table
     */
    protected void setupTable() {
        // Tạo table model với các cột
        String[] columnNames = columns.stream()
            .map(ColumnInfo::getName)
            .toArray(String[]::new);
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho edit trực tiếp trên table
            }
        };
        
        dataTable = new JTable(tableModel);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataTable.setRowHeight(25);
        
        // Auto resize columns
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Double click để edit
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showEditForm();
                }
            }
        });
        
        scrollPane = new JScrollPane(dataTable);
    }
    
    /**
     * Template Method: Setup context menu
     */
    protected void setupContextMenu() {
        contextMenu = new JPopupMenu();
        
        JMenuItem addItem = new JMenuItem("Thêm mới");
        addItem.addActionListener(e -> showAddForm());
        contextMenu.add(addItem);
        
        JMenuItem editItem = new JMenuItem("Cập nhật");
        editItem.addActionListener(e -> showEditForm());
        contextMenu.add(editItem);
        
        JMenuItem deleteItem = new JMenuItem("Xóa");
        deleteItem.addActionListener(e -> deleteSelected());
        contextMenu.add(deleteItem);
        
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });
    }
    
    private void showContextMenu(MouseEvent e) {
        int row = dataTable.rowAtPoint(e.getPoint());
        if (row >= 0) {
            dataTable.setRowSelectionInterval(row, row);
            contextMenu.show(dataTable, e.getX(), e.getY());
        }
    }
    
    /**
     * Template Method: Load data từ database
     */
    protected void loadData() {
        try {
            data = dbContext.getAll(tableName);
            tableModel.setRowCount(0);
            
            for (Map<String, Object> row : data) {
                Object[] rowData = new Object[columns.size()];
                for (int i = 0; i < columns.size(); i++) {
                    rowData[i] = row.get(columns.get(i).getName());
                }
                tableModel.addRow(rowData);
            }
            
            // Adjust column widths
            adjustColumnWidths();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi khi tải dữ liệu: " + e.getMessage(), 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Hook method: Có thể override để điều chỉnh độ rộng cột
     */
    protected void adjustColumnWidths() {
        for (int i = 0; i < columns.size(); i++) {
            int width = Math.max(100, columns.get(i).getSize() * 8);
            dataTable.getColumnModel().getColumn(i).setPreferredWidth(Math.min(width, 300));
        }
    }
    
    /**
     * Template Method: Hiển thị form thêm mới
     */
    protected void showAddForm() {
        Map<String, Object> emptyData = new HashMap<>();
        showDataForm(emptyData, true);
    }
    
    /**
     * Template Method: Hiển thị form cập nhật
     */
    protected void showEditForm() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn một dòng để cập nhật", 
                "Thông báo", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        Map<String, Object> rowData = data.get(selectedRow);
        showDataForm(rowData, false);
    }
    
    /**
     * Template Method: Hiển thị form nhập liệu
     */
    protected void showDataForm(Map<String, Object> data, boolean isNew) {
        JDialog dialog = new JDialog(this, isNew ? "Thêm mới" : "Cập nhật", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        // Form fields
        JPanel formPanel = createFormPanel(data);
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        
        btnSave.addActionListener(e -> {
            if (saveData(dialog, data, isNew)) {
                dialog.dispose();
                loadData();
            }
        });
        
        btnCancel.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * Template Method: Tạo form panel với các input fields
     */
    protected JPanel createFormPanel(Map<String, Object> data) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        inputFields.clear();
        
        for (int i = 0; i < columns.size(); i++) {
            ColumnInfo column = columns.get(i);
            String columnName = column.getName();
            
            // Skip primary key khi thêm mới
            if (column.isPrimaryKey() && !data.containsKey(columnName)) {
                continue;
            }
            
            // Label
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            JLabel label = new JLabel(columnName + ":");
            panel.add(label, gbc);
            
            // Input field
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            JComponent inputField = createInputField(column, data.get(columnName));
            inputFields.put(columnName, inputField);
            panel.add(inputField, gbc);
        }
        
        return panel;
    }
    
    /**
     * Hook method: Tạo input field dựa trên column type
     * Có thể override để custom input fields
     */
    protected JComponent createInputField(ColumnInfo column, Object value) {
        Class<?> javaType = column.getJavaType();
        String columnName = column.getName();
        
        if (javaType == Boolean.class) {
            JCheckBox checkBox = new JCheckBox();
            if (value != null) {
                checkBox.setSelected((Boolean) value);
            }
            return checkBox;
        } else if (javaType == Integer.class || javaType == Long.class) {
            JTextField textField = new JTextField(20);
            if (value != null) {
                textField.setText(value.toString());
            }
            return textField;
        } else if (javaType == java.util.Date.class) {
            JTextField textField = new JTextField(20);
            if (value != null) {
                textField.setText(value.toString());
            }
            // Có thể dùng JDatePicker ở đây
            return textField;
        } else {
            JTextField textField = new JTextField(20);
            if (value != null) {
                textField.setText(value.toString());
            }
            return textField;
        }
    }
    
    /**
     * Template Method: Lưu dữ liệu
     */
    protected boolean saveData(JDialog dialog, Map<String, Object> oldData, boolean isNew) {
        try {
            Map<String, Object> newData = new HashMap<>();
            
            // Lấy giá trị từ các input fields
            for (Map.Entry<String, JComponent> entry : inputFields.entrySet()) {
                String columnName = entry.getKey();
                JComponent component = entry.getValue();
                Object value = getValueFromComponent(component);
                newData.put(columnName, value);
            }
            
            // Validate
            if (!validateData(newData)) {
                return false;
            }
            
            // Save to database
            if (isNew) {
                dbContext.insert(tableName, newData);
                JOptionPane.showMessageDialog(dialog, 
                    "Thêm mới thành công!", 
                    "Thông báo", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                Object primaryKeyValue = oldData.get(primaryKeyColumn);
                String whereClause = primaryKeyColumn + " = '" + primaryKeyValue + "'";
                dbContext.update(tableName, newData, whereClause);
                JOptionPane.showMessageDialog(dialog, 
                    "Cập nhật thành công!", 
                    "Thông báo", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, 
                "Lỗi khi lưu dữ liệu: " + e.getMessage(), 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Hook method: Lấy giá trị từ component
     */
    protected Object getValueFromComponent(JComponent component) {
        if (component instanceof JTextField) {
            String text = ((JTextField) component).getText();
            return text.isEmpty() ? null : text;
        } else if (component instanceof JCheckBox) {
            return ((JCheckBox) component).isSelected();
        } else if (component instanceof JComboBox) {
            return ((JComboBox<?>) component).getSelectedItem();
        }
        return null;
    }
    
    /**
     * Hook method: Validate dữ liệu
     * Có thể override để thêm validation logic
     */
    protected boolean validateData(Map<String, Object> data) {
        // Kiểm tra required fields
        for (ColumnInfo column : columns) {
            if (!column.isNullable() && !column.isPrimaryKey()) {
                Object value = data.get(column.getName());
                if (value == null || value.toString().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Trường " + column.getName() + " là bắt buộc", 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Template Method: Xóa dòng được chọn
     */
    protected void deleteSelected() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn một dòng để xóa", 
                "Thông báo", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn xóa dòng này?", 
            "Xác nhận", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Map<String, Object> rowData = data.get(selectedRow);
                Object primaryKeyValue = rowData.get(primaryKeyColumn);
                String whereClause = primaryKeyColumn + " = '" + primaryKeyValue + "'";
                
                dbContext.delete(tableName, whereClause);
                JOptionPane.showMessageDialog(this, 
                    "Xóa thành công!", 
                    "Thông báo", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Lỗi khi xóa: " + e.getMessage(), 
                    "Lỗi", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}


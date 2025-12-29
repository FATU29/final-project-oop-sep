package com.sep.framework.core;

import com.sep.framework.database.DatabaseContext;
import com.sep.framework.database.DatabaseStrategy;
import com.sep.framework.database.MySQLStrategy;
import com.sep.framework.ioc.ServiceContainer;
import com.sep.framework.membership.AuthenticationManager;
import com.sep.framework.membership.DatabaseMembershipProvider;
import com.sep.framework.membership.MembershipProvider;
import com.sep.framework.patterns.FormFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main Application class
 * Demo việc sử dụng Framework
 */
public class FrameworkApplication extends JFrame {
    
    private DatabaseContext dbContext;
    private ServiceContainer container;
    private MembershipProvider membershipProvider;
    private FormFactory formFactory;
    private AuthenticationManager authManager;
    
    public FrameworkApplication() {
        initializeFramework();
        setupUI();
    }
    
    /**
     * Khởi tạo framework components
     */
    private void initializeFramework() {
        // Khởi tạo IoC Container (Singleton)
        container = ServiceContainer.getInstance();
        
        // Setup database context với MySQL strategy
        DatabaseStrategy strategy = new MySQLStrategy();
        dbContext = new DatabaseContext(strategy);
        
        // Cấu hình connection string với username và password
        // Lấy từ environment variables hoặc sử dụng giá trị mặc định
        String dbUser = System.getenv("MYSQL_USER");
        String dbPassword = System.getenv("MYSQL_PASSWORD");
        String dbName = System.getenv("MYSQL_DATABASE");
        String dbPort = System.getenv("MYSQL_PORT");
        
        // Sử dụng giá trị mặc định nếu không có env vars
        if (dbUser == null || dbUser.isEmpty()) dbUser = "sep_user";
        if (dbPassword == null || dbPassword.isEmpty()) dbPassword = "sep_password";
        if (dbName == null || dbName.isEmpty()) dbName = "sep_demo";
        if (dbPort == null || dbPort.isEmpty()) dbPort = "3306";
        
        // Connection string (không bao gồm credentials trong URL)
        // allowPublicKeyRetrieval=true: Cần thiết cho MySQL 8.0+
        String connectionString = String.format(
            "jdbc:mysql://localhost:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
            dbPort, dbName
        );
        dbContext.setConnectionString(connectionString);
        dbContext.setCredentials(dbUser, dbPassword);
        
        // Đăng ký services vào IoC container
        container.registerSingleton(DatabaseContext.class, dbContext);
        container.registerSingleton(DatabaseStrategy.class, strategy);
        
        // Khởi tạo Membership Provider
        membershipProvider = new DatabaseMembershipProvider(dbContext);
        container.registerSingleton(MembershipProvider.class, membershipProvider);
        
        // Khởi tạo Authentication Manager
        authManager = AuthenticationManager.getInstance();
        authManager.setMembershipProvider(membershipProvider);
        container.registerSingleton(AuthenticationManager.class, authManager);
        
        // Khởi tạo Form Factory
        formFactory = FormFactory.getInstance(dbContext);
        container.registerSingleton(FormFactory.class, formFactory);
    }
    
    /**
     * Setup UI
     */
    private void setupUI() {
        setTitle("SEP Framework - Simple Enterprise Framework");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Menu bar
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createWelcomePanel(), BorderLayout.CENTER);
        add(mainPanel);
    }
    
    /**
     * Tạo menu bar
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Membership menu
        JMenu membershipMenu = new JMenu("Membership");
        JMenuItem loginItem = new JMenuItem("Login");
        loginItem.addActionListener(e -> showLoginDialog());
        JMenuItem registerItem = new JMenuItem("Register");
        registerItem.addActionListener(e -> showRegisterDialog());
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> {
            authManager.logout();
            updateMenuState();
            JOptionPane.showMessageDialog(this, "Đã đăng xuất!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
        JMenuItem manageUsersItem = new JMenuItem("Manage Users");
        manageUsersItem.addActionListener(e -> showUserManagementForm());
        membershipMenu.add(loginItem);
        membershipMenu.add(registerItem);
        membershipMenu.add(logoutItem);
        membershipMenu.addSeparator();
        membershipMenu.add(manageUsersItem);
        
        // CRUD menu
        JMenu crudMenu = new JMenu("CRUD");
        JMenuItem openTableItem = new JMenuItem("Open Table...");
        openTableItem.addActionListener(e -> showTableSelector());
        crudMenu.add(openTableItem);
        
        // Code Generation menu
        JMenu codeGenMenu = new JMenu("Code Generation");
        JMenuItem generateCodeItem = new JMenuItem("Generate Code from Database");
        generateCodeItem.addActionListener(e -> showCodeGeneratorDialog());
        codeGenMenu.add(generateCodeItem);
        
        menuBar.add(fileMenu);
        menuBar.add(membershipMenu);
        menuBar.add(crudMenu);
        menuBar.add(codeGenMenu);
        
        // Cập nhật trạng thái menu ban đầu
        updateMenuState();
        
        return menuBar;
    }
    
    /**
     * Tạo welcome panel
     */
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("SEP Framework", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JTextArea infoArea = new JTextArea(
            "Simple Enterprise Framework (SEP)\n\n" +
            "Tính năng:\n" +
            "• Membership System (đăng nhập, đăng ký, phân quyền)\n" +
            "• Generic CRUD Forms (tự động phát sinh từ database)\n" +
            "• Custom IoC Container\n" +
            "• Code Generation từ database schema\n" +
            "• Hỗ trợ nhiều database (MySQL, PostgreSQL, SQLite)\n\n" +
            "Design Patterns được sử dụng:\n" +
            "• Factory Pattern\n" +
            "• Strategy Pattern\n" +
            "• Singleton Pattern\n" +
            "• Template Method Pattern\n" +
            "• Observer Pattern\n" +
            "• Builder Pattern"
        );
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Arial", Font.PLAIN, 12));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Cập nhật trạng thái menu dựa trên authentication
     */
    private void updateMenuState() {
        boolean isLoggedIn = authManager.isLoggedIn();
        
        // Enable/disable CRUD menu items
        JMenuBar menuBar = getJMenuBar();
        if (menuBar != null) {
            for (int i = 0; i < menuBar.getMenuCount(); i++) {
                JMenu menu = menuBar.getMenu(i);
                if (menu != null) {
                    if (menu.getText().equals("CRUD") || menu.getText().equals("Code Generation")) {
                        menu.setEnabled(isLoggedIn);
                    }
                }
            }
        }
        
        // Update title với username
        if (isLoggedIn) {
            String username = authManager.getCurrentUsername();
            setTitle("SEP Framework - Simple Enterprise Framework - User: " + username);
        } else {
            setTitle("SEP Framework - Simple Enterprise Framework - Chưa đăng nhập");
        }
    }
    
    /**
     * Hiển thị login dialog
     */
    private void showLoginDialog() {
        JDialog dialog = new JDialog(this, "Login", true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        
        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> {
            try {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                
                if (authManager.login(username, password)) {
                    JOptionPane.showMessageDialog(dialog, "Login thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    updateMenuState();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Username hoặc password không đúng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        panel.add(new JLabel());
        panel.add(loginBtn);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * Hiển thị register dialog
     */
    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Register", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField emailField = new JTextField();
        
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        
        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(e -> {
            try {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String email = emailField.getText();
                
                if (membershipProvider.createUser(username, password, email)) {
                    JOptionPane.showMessageDialog(dialog, "Đăng ký thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Username đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        panel.add(new JLabel());
        panel.add(registerBtn);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * Hiển thị user management form
     */
    private void showUserManagementForm() {
        try {
            com.sep.framework.crud.BaseCrudForm form = formFactory.createForm("sep_users");
            form.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Hiển thị table selector
     */
    private void showTableSelector() {
        // Kiểm tra authentication
        if (!authManager.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng đăng nhập để sử dụng tính năng này!", 
                "Yêu cầu đăng nhập", 
                JOptionPane.WARNING_MESSAGE);
            showLoginDialog();
            return;
        }
        
        try {
            java.util.List<String> tables = dbContext.getTables();
            if (tables.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có bảng nào trong database!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            String selectedTable = (String) JOptionPane.showInputDialog(
                this,
                "Chọn bảng:",
                "Chọn bảng",
                JOptionPane.QUESTION_MESSAGE,
                null,
                tables.toArray(),
                tables.get(0)
            );
            
            if (selectedTable != null) {
                com.sep.framework.crud.BaseCrudForm form = formFactory.createForm(selectedTable);
                form.setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Hiển thị code generator dialog
     */
    private void showCodeGeneratorDialog() {
        // Kiểm tra authentication
        if (!authManager.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng đăng nhập để sử dụng tính năng này!", 
                "Yêu cầu đăng nhập", 
                JOptionPane.WARNING_MESSAGE);
            showLoginDialog();
            return;
        }
        
        JDialog dialog = new JDialog(this, "Code Generator", true);
        dialog.setSize(650, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Output path với file chooser
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Output Path:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField pathField = new JTextField(System.getProperty("user.dir") + System.getProperty("file.separator") + "generated");
        panel.add(pathField, gbc);
        
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JButton browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Chọn thư mục output");
            
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        panel.add(browseBtn, gbc);
        
        // Package name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Package Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField packageField = new JTextField("com.sep.generated");
        panel.add(packageField, gbc);
        
        // Use package checkbox
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JCheckBox usePackageCheckBox = new JCheckBox("Generate without package (flat structure - export anywhere)", false);
        usePackageCheckBox.setToolTipText("Nếu chọn, code sẽ được generate không có package, có thể copy và dùng ở bất kỳ đâu");
        panel.add(usePackageCheckBox, gbc);
        
        // Table selection
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Chọn bảng:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JComboBox<String> tableComboBox = new JComboBox<>();
        tableComboBox.addItem("-- Tất cả các bảng --");
        try {
            java.util.List<String> tables = dbContext.getTables();
            for (String table : tables) {
                tableComboBox.addItem(table);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "Lỗi khi tải danh sách bảng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
        panel.add(tableComboBox, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton generateBtn = new JButton("Generate");
        generateBtn.addActionListener(e -> {
            try {
                String outputPath = pathField.getText().trim();
                String packageName = packageField.getText().trim();
                
                if (outputPath.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng chọn output path!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean usePackage = !usePackageCheckBox.isSelected();
                
                // Nếu không dùng package, không cần kiểm tra package name
                if (usePackage && packageName.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng nhập package name!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                com.sep.framework.codegen.CodeGenerator generator = 
                    new com.sep.framework.codegen.CodeGeneratorBuilder(dbContext)
                        .outputPath(outputPath)
                        .packageName(packageName)
                        .usePackage(usePackage)
                        .build();
                
                String selectedTable = (String) tableComboBox.getSelectedItem();
                if (selectedTable != null && !selectedTable.equals("-- Tất cả các bảng --")) {
                    // Generate cho bảng được chọn
                    generator.generateForTable(selectedTable);
                    JOptionPane.showMessageDialog(dialog, 
                        "Generate code cho bảng '" + selectedTable + "' thành công!\n" +
                        "Output: " + outputPath, 
                        "Thông báo", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Generate cho tất cả các bảng
                    generator.generateAll();
                    JOptionPane.showMessageDialog(dialog, 
                        "Generate code cho tất cả các bảng thành công!\n" +
                        "Output: " + outputPath, 
                        "Thông báo", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
                
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(generateBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new FrameworkApplication().setVisible(true);
        });
    }
}


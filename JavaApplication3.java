package javaapplication3;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.mysql.cj.jdbc.Driver;
import java.util.ArrayList;
import java.util.List;

// Interface for books
interface IBook {
    String getTitle();
    String getAuthor();
    String getCategory();
    void setTitle(String title);
    void setAuthor(String author);
}

// Singleton Pattern
class DatabaseConnection {
    private static volatile DatabaseConnection instance;
    private Connection connection;
    
    private DatabaseConnection() {
        try {
            DriverManager.registerDriver(new Driver());
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_management?createDatabaseIfNotExist=true",
                "root",
                ""
            );
            System.out.println("Database Connected");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database: " + e.getMessage(), e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}

// Singleton for Logger with Thread Safety
class Logger {
    private static volatile Logger logger;

    private Logger() {}

    public static Logger getInstance() {
        if (logger == null) {
            synchronized (Logger.class) {
                if (logger == null) {
                    logger = new Logger();
                }
            }
        }
        return logger;
    }

    public synchronized void log(String message) {
        System.out.println("LOG: " + message);
    }
}

// Prototype Pattern
abstract class Book implements IBook, Cloneable {
    protected String title;
    protected String author;
    protected String category;

    @Override
    public String getTitle() { return title; }
    
    @Override
    public String getAuthor() { return author; }
    
    @Override
    public String getCategory() { return category; }
    
    @Override
    public void setTitle(String title) { this.title = title; }
    
    @Override
    public void setAuthor(String author) { this.author = author; }

    @Override
    public Book clone() {
        try {
            return (Book) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}

class SoftwareEngineeringBook extends Book {
    public SoftwareEngineeringBook() {
        this.category = "Software Engineering";
    }
}

class ManagementBook extends Book {
    public ManagementBook() {
        this.category = "Management";
    }
}

class ArtificialIntelligenceBook extends Book {
    public ArtificialIntelligenceBook() {
        this.category = "Artificial Intelligence";
    }
}

// Factory Pattern
interface BookFactory {
    Book createBook();
}

class SoftwareEngineeringBookFactory implements BookFactory {
    @Override
    public Book createBook() {
        return new SoftwareEngineeringBook();
    }
}

class ManagementBookFactory implements BookFactory {
    @Override
    public Book createBook() {
        return new ManagementBook();
    }
}

class ArtificialIntelligenceBookFactory implements BookFactory {
    @Override
    public Book createBook() {
        return new ArtificialIntelligenceBook();
    }
}

// Factory Producer
class BookFactoryProducer {
    public static BookFactory getFactory(String category) {
        switch (category) {
            case "Software Engineering":
                return new SoftwareEngineeringBookFactory();
            case "Management":
                return new ManagementBookFactory();
            case "Artificial Intelligence":
                return new ArtificialIntelligenceBookFactory();
            default:
                throw new IllegalArgumentException("Invalid category: " + category);
        }
    }
}

// Adapter Pattern
class ExternalBook {
    private String externalTitle;
    private String externalAuthor;
    private String externalCategory;

    public ExternalBook(String title, String author, String category) {
        this.externalTitle = title;
        this.externalAuthor = author;
        this.externalCategory = category;
    }

    public String getExternalTitle() { return externalTitle; }
    public String getExternalAuthor() { return externalAuthor; }
    public String getExternalCategory() { return externalCategory; }
}

class ExternalBookAdapter implements IBook {
    private ExternalBook externalBook;

    public ExternalBookAdapter(ExternalBook externalBook) {
        this.externalBook = externalBook;
    }

    @Override
    public String getTitle() { return externalBook.getExternalTitle(); }
    
    @Override
    public String getAuthor() { return externalBook.getExternalAuthor(); }
    
    @Override
    public String getCategory() { return externalBook.getExternalCategory(); }
    
    @Override
    public void setTitle(String title) { throw new UnsupportedOperationException(); }
    
    @Override
    public void setAuthor(String author) { throw new UnsupportedOperationException(); }
}

// Abstract class for LibraryUser
abstract class LibraryUser {
    protected String name;

    public LibraryUser(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String getRole();
}

class Admin extends LibraryUser {
    public Admin(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Admin";
    }
}

class RegularUser extends LibraryUser {
    public RegularUser(String name) {
        super(name);
    }

    @Override
    public String getRole() {
        return "Regular User";
    }
}

// Builder Pattern
interface BookBuilder {
    BookBuilder setTitle(String title);
    BookBuilder setAuthor(String author);
    Book build();
}

class ConcreteBookBuilder implements BookBuilder {
    private Book book;

    public ConcreteBookBuilder(String category) {
        BookFactory factory = BookFactoryProducer.getFactory(category);
        this.book = factory.createBook();
    }

    @Override
    public BookBuilder setTitle(String title) {
        book.setTitle(title);
        return this;
    }

    @Override
    public BookBuilder setAuthor(String author) {
        book.setAuthor(author);
        return this;
    }

    @Override
    public Book build() {
        return book;
    }
}

// Proxy Pattern
interface BookService {
    void addBook(Book book) throws SQLException;
    void removeBook(String title) throws SQLException;
    List<Book> getAllBooks() throws SQLException;
    boolean isBookAvailable(String title) throws SQLException;
    List<Book> getAvailableBooks() throws SQLException;
    List<Book> getBorrowedBooks() throws SQLException;
}

class RealBookService implements BookService {
    private Connection connection;

    public RealBookService() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void addBook(Book book) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO books (title, author, category) VALUES (?, ?, ?)"
        );
        stmt.setString(1, book.getTitle());
        stmt.setString(2, book.getAuthor());
        stmt.setString(3, book.getCategory());
        stmt.executeUpdate();
    }

    @Override
    public void removeBook(String title) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
            "DELETE FROM books WHERE title = ?"
        );
        stmt.setString(1, title);
        stmt.executeUpdate();
    }

    @Override
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM books");
        
        while (rs.next()) {
            Book book = BookFactoryProducer
                .getFactory(rs.getString("category"))
                .createBook();
            book.setTitle(rs.getString("title"));
            book.setAuthor(rs.getString("author"));
            books.add(book);
        }
        return books;
    }

    @Override
    public boolean isBookAvailable(String title) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
            "SELECT COUNT(*) FROM books WHERE title = ? AND is_borrowed = false"
        );
        stmt.setString(1, title);
        ResultSet rs = stmt.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    @Override
    public List<Book> getAvailableBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT * FROM books WHERE is_borrowed = false"
        );
        
        while (rs.next()) {
            Book book = BookFactoryProducer
                .getFactory(rs.getString("category"))
                .createBook();
            book.setTitle(rs.getString("title"));
            book.setAuthor(rs.getString("author"));
            books.add(book);
        }
        return books;
    }

    @Override
    public List<Book> getBorrowedBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT b.* FROM books b " +
            "JOIN borrowed_books bb ON b.title = bb.book_title " +
            "WHERE b.is_borrowed = true"
        );
        
        while (rs.next()) {
            Book book = BookFactoryProducer
                .getFactory(rs.getString("category"))
                .createBook();
            book.setTitle(rs.getString("title"));
            book.setAuthor(rs.getString("author"));
            books.add(book);
        }
        return books;
    }
}

class BookServiceProxy implements BookService {
    private RealBookService realService;
    private Logger logger;

    public BookServiceProxy() {
        this.realService = new RealBookService();
        this.logger = Logger.getInstance();
    }

    @Override
    public void addBook(Book book) throws SQLException {
        logger.log("Adding book: " + book.getTitle());
        realService.addBook(book);
        logger.log("Book added successfully: " + book.getTitle());
    }

    @Override
    public void removeBook(String title) throws SQLException {
        logger.log("Removing book: " + title);
        realService.removeBook(title);
        logger.log("Book removed successfully: " + title);
    }

    @Override
    public List<Book> getAllBooks() throws SQLException {
        logger.log("Fetching all books");
        List<Book> books = realService.getAllBooks();
        logger.log("Retrieved " + books.size() + " books");
        return books;
    }

    @Override
    public boolean isBookAvailable(String title) throws SQLException {
        logger.log("Checking availability for book: " + title);
        boolean available = realService.isBookAvailable(title);
        logger.log("Book " + title + " is " + (available ? "available" : "not available"));
        return available;
    }

    @Override
    public List<Book> getAvailableBooks() throws SQLException {
        logger.log("Fetching available books");
        List<Book> books = realService.getAvailableBooks();
        logger.log("Retrieved " + books.size() + " available books");
        return books;
    }

    @Override
    public List<Book> getBorrowedBooks() throws SQLException {
        logger.log("Fetching borrowed books");
        List<Book> books = realService.getBorrowedBooks();
        logger.log("Retrieved " + books.size() + " borrowed books");
        return books;
    }
}

// Command Pattern for Book Operations
interface BookCommand {
    boolean execute() throws SQLException;
}

class BorrowBookCommand implements BookCommand {
    private final BookService bookService;
    private final String bookTitle;
    private final String userName;

    public BorrowBookCommand(BookService bookService, String bookTitle, String userName) {
        this.bookService = bookService;
        this.bookTitle = bookTitle;
        this.userName = userName;
    }

    @Override
    public boolean execute() throws SQLException {
        // التحقق من وجود الكتاب وعدم استعارته
        if (bookService.isBookAvailable(bookTitle)) {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO borrowed_books (book_title, user_name, borrow_date) VALUES (?, ?, NOW())"
            );
            stmt.setString(1, bookTitle);
            stmt.setString(2, userName);
            stmt.executeUpdate();
            
            // تحديث حالة الكتاب
            PreparedStatement updateStmt = conn.prepareStatement(
                "UPDATE books SET is_borrowed = true WHERE title = ?"
            );
            updateStmt.setString(1, bookTitle);
            updateStmt.executeUpdate();
            
            return true;
        }
        return false;
    }
}

class ReturnBookCommand implements BookCommand {
    private final BookService bookService;
    private final String bookTitle;
    private final String userName;

    public ReturnBookCommand(BookService bookService, String bookTitle, String userName) {
        this.bookService = bookService;
        this.bookTitle = bookTitle;
        this.userName = userName;
    }

    @Override
    public boolean execute() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        // التحقق من أن الكتاب مستعار فعلاً من قبل هذا المستخدم
        PreparedStatement checkStmt = conn.prepareStatement(
            "SELECT COUNT(*) FROM borrowed_books WHERE book_title = ? AND user_name = ?"
        );
        checkStmt.setString(1, bookTitle);
        checkStmt.setString(2, userName);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next() && rs.getInt(1) > 0) {
            // حذف سجل الاستعارة
            PreparedStatement deleteStmt = conn.prepareStatement(
                "DELETE FROM borrowed_books WHERE book_title = ? AND user_name = ?"
            );
            deleteStmt.setString(1, bookTitle);
            deleteStmt.setString(2, userName);
            deleteStmt.executeUpdate();
            
            // تحديث حالة الكتاب إلى متاح
            PreparedStatement updateStmt = conn.prepareStatement(
                "UPDATE books SET is_borrowed = false WHERE title = ?"
            );
            updateStmt.setString(1, bookTitle);
            updateStmt.executeUpdate();
            
            return true;
        }
        return false;
    }
}

// GUI Application
public class JavaApplication3 {
    public static void main(String[] args) {
        DatabaseConnection db = DatabaseConnection.getInstance();
        Logger logger = Logger.getInstance();

        JFrame frame = new JFrame("Library Management System");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        JLabel headerLabel = new JLabel("Library Management System");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        frame.add(headerPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton addBookButton = new JButton("Add Book");
        JButton addUserButton = new JButton("Add User");
        JButton viewDatabaseButton = new JButton("View Database");
        JButton borrowBookButton = new JButton("Borrow Book");
        JButton returnBookButton = new JButton("Return Book");

        buttonPanel.add(addBookButton);
        buttonPanel.add(addUserButton);
        buttonPanel.add(viewDatabaseButton);
        buttonPanel.add(borrowBookButton);
        buttonPanel.add(returnBookButton);
        frame.add(buttonPanel, BorderLayout.CENTER);

        addBookButton.addActionListener(e -> {
            JTextField titleField = new JTextField();
            JTextField authorField = new JTextField();
            String[] categories = {"Software Engineering", "Management", "Artificial Intelligence"};
            JComboBox<String> categoryBox = new JComboBox<>(categories);

            Object[] inputs = {"Title:", titleField, "Author:", authorField, "Category:", categoryBox};
            int option = JOptionPane.showConfirmDialog(frame, inputs, "Add Book", JOptionPane.OK_CANCEL_OPTION);
            
            if (option == JOptionPane.OK_OPTION) {
                try {
                    // استخدام Builder Pattern
                    Book book = new ConcreteBookBuilder((String) categoryBox.getSelectedItem())
                        .setTitle(titleField.getText())
                        .setAuthor(authorField.getText())
                        .build();

                    // استخدام Proxy Pattern
                    BookService bookService = new BookServiceProxy();
                    bookService.addBook(book);

                    // استخدام Prototype Pattern
                    Book backupBook = book.clone();
                    
                    JOptionPane.showMessageDialog(frame, "Book added successfully!");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to add book: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        addUserButton.addActionListener(e -> {
            JTextField nameField = new JTextField();
            String[] roles = {"Admin", "Regular User"};
            JComboBox<String> roleBox = new JComboBox<>(roles);

            Object[] inputs = {"Name:", nameField, "Role:", roleBox};
            int option = JOptionPane.showConfirmDialog(frame, inputs, "Add User", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText();
                    String role = (String) roleBox.getSelectedItem();
                    PreparedStatement stmt = db.getConnection().prepareStatement("INSERT INTO users (name, role) VALUES (?, ?)");
                    stmt.setString(1, name);
                    stmt.setString(2, role);
                    stmt.executeUpdate();
                    logger.log("User added: " + name);
                    JOptionPane.showMessageDialog(frame, "User added successfully!");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to add user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        viewDatabaseButton.addActionListener(e -> {
            try {
                StringBuilder data = new StringBuilder("Books:\n");
                Statement stmt = db.getConnection().createStatement();
                ResultSet books = stmt.executeQuery("SELECT * FROM books");
                while (books.next()) {
                    data.append("Title: ").append(books.getString("title"))
                        .append(", Author: ").append(books.getString("author"))
                        .append(", Category: ").append(books.getString("category"))
                        .append("\n");
                }

                data.append("\nUsers:\n");
                ResultSet users = stmt.executeQuery("SELECT * FROM users");
                while (users.next()) {
                    data.append("Name: ").append(users.getString("name"))
                        .append(", Role: ").append(users.getString("role"))
                        .append("\n");
                }

                JTextArea textArea = new JTextArea(data.toString());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                JOptionPane.showMessageDialog(frame, scrollPane, "Database Contents", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Failed to fetch database contents: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        borrowBookButton.addActionListener(e -> {
            try {
                // عرض الكتب المتاحة في قائمة منسدلة
                BookService bookService = new BookServiceProxy();
                List<Book> availableBooks = bookService.getAvailableBooks();
                
                if (availableBooks.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, 
                        "لا توجد كتب متاحة للاستعارة حالياً",
                        "تنبيه",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String[] bookTitles = availableBooks.stream()
                    .map(Book::getTitle)
                    .toArray(String[]::new);
                    
                JComboBox<String> bookComboBox = new JComboBox<>(bookTitles);
                JTextField userNameField = new JTextField();
                
                Object[] inputs = {
                    "اختر الكتاب:", bookComboBox,
                    "اسم المستعير:", userNameField
                };

                int option = JOptionPane.showConfirmDialog(frame, inputs, 
                    "استعارة كتاب", JOptionPane.OK_CANCEL_OPTION);
                    
                if (option == JOptionPane.OK_OPTION) {
                    String selectedBook = (String) bookComboBox.getSelectedItem();
                    String userName = userNameField.getText();

                    if (userName.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(frame,
                            "الرجاء إدخال اسم المستعير",
                            "خطأ",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // تنفيذ أمر الاستعارة
                    BookCommand borrowCommand = new BorrowBookCommand(bookService, selectedBook, userName);
                    if (borrowCommand.execute()) {
                        Logger.getInstance().log(
                            "تمت استعارة الكتاب: " + selectedBook + " بواسطة: " + userName
                        );
                        JOptionPane.showMessageDialog(frame, "تمت عملية الاستعارة بنجاح!");
                    } else {
                        JOptionPane.showMessageDialog(frame,
                            "عذراً، الكتاب غير متاح للاستعارة",
                            "خطأ",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame,
                    "حدث خطأ أثناء عملية الاستعارة: " + ex.getMessage(),
                    "خطأ",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        returnBookButton.addActionListener(e -> {
            try {
                // عرض الكتب المستعارة في قائمة منسدلة
                BookService bookService = new BookServiceProxy();
                List<Book> borrowedBooks = bookService.getBorrowedBooks();
                
                if (borrowedBooks.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, 
                        "لا توجد كتب مستعارة حالياً",
                        "تنبيه",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String[] bookTitles = borrowedBooks.stream()
                    .map(Book::getTitle)
                    .toArray(String[]::new);
                    
                JComboBox<String> bookComboBox = new JComboBox<>(bookTitles);
                JTextField userNameField = new JTextField();
                
                Object[] inputs = {
                    "اختر الكتاب:", bookComboBox,
                    "اسم المستعير:", userNameField
                };

                int option = JOptionPane.showConfirmDialog(frame, inputs, 
                    "إرجاع كتاب", JOptionPane.OK_CANCEL_OPTION);
                    
                if (option == JOptionPane.OK_OPTION) {
                    String selectedBook = (String) bookComboBox.getSelectedItem();
                    String userName = userNameField.getText();

                    if (userName.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(frame,
                            "الرجاء إدخال اسم المستعير",
                            "خطأ",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // تنفيذ أمر الإرجاع
                    BookCommand returnCommand = new ReturnBookCommand(bookService, selectedBook, userName);
                    if (returnCommand.execute()) {
                        Logger.getInstance().log(
                            "تم إرجاع الكتاب: " + selectedBook + " من قبل: " + userName
                        );
                        JOptionPane.showMessageDialog(frame, "تمت عملية الإرجاع بنجاح!");
                    } else {
                        JOptionPane.showMessageDialog(frame,
                            "عذراً، لم يتم العثور على سجل استعارة لهذا الكتاب والمستخدم",
                            "خطأ",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame,
                    "حدث خطأ أثناء عملية الإرجاع: " + ex.getMessage(),
                    "خطأ",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
}
}
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

// ---------- Custom Exceptions ----------
class InvalidBookIdException extends Exception {
    public InvalidBookIdException(String id) { super("Invalid book ID: " + id); }
}
class InvalidMemberIdException extends Exception {
    public InvalidMemberIdException(String id) { super("Invalid member ID: " + id); }
}
class BookUnavailableException extends Exception {
    public BookUnavailableException(String id) { super("Book " + id + " is currently unavailable."); }
}
class BorrowLimitExceededException extends Exception {
    public BorrowLimitExceededException(String id, int max) {
        super("Member " + id + " exceeded the maximum borrowing limit (" + max + " books).");
    }
}
class InvalidReturnDateException extends Exception {
    public InvalidReturnDateException() { super("Invalid return date. Return date cannot be before issue date."); }
}

// ---------- Model classes ----------
class Book {
    String id, title;
    boolean available = true;
    Book(String id, String title) { this.id = id; this.title = title; }
    public String toString() { return id + " | " + title + " | " + (available ? "Available" : "Issued"); }
}

class Member {
    String id, name;
    List<String> borrowed = new ArrayList<>();
    Member(String id, String name) { this.id = id; this.name = name; }
}

// ---------- Library ----------
class Library {
    static final int MAX_BOOKS = 3;
    Map<String, Book> books = new LinkedHashMap<>();
    Map<String, Member> members = new HashMap<>();
    Map<String, LocalDate> issueDates = new HashMap<>();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    void addBook(Book b) { books.put(b.id, b); }
    void addMember(Member m) { members.put(m.id, m); }

    void search(String keyword) {
        System.out.println("Search \"" + keyword + "\":");
        for (Book b : books.values())
            if (b.title.toLowerCase().contains(keyword.toLowerCase()))
                System.out.println(b);
        System.out.println();
    }

    Book getBook(String id) throws InvalidBookIdException {
        Book b = books.get(id);
        if (b == null) throw new InvalidBookIdException(id);
        return b;
    }

    Member getMember(String id) throws InvalidMemberIdException {
        Member m = members.get(id);
        if (m == null) throw new InvalidMemberIdException(id);
        return m;
    }

    void issueBook(String memberId, String bookId, LocalDate date)
            throws InvalidMemberIdException, InvalidBookIdException,
                   BookUnavailableException, BorrowLimitExceededException {
        Member m = getMember(memberId);
        Book b = getBook(bookId);
        if (!b.available) throw new BookUnavailableException(bookId);
        if (m.borrowed.size() >= MAX_BOOKS) throw new BorrowLimitExceededException(memberId, MAX_BOOKS);
        b.available = false;
        m.borrowed.add(bookId);
        issueDates.put(bookId, date);
        System.out.println("Member " + memberId + " borrowed " + bookId
                + ". Due: " + date.plusDays(14).format(fmt));
    }

    void returnBook(String memberId, String bookId, LocalDate returnDate)
            throws InvalidMemberIdException, InvalidBookIdException, InvalidReturnDateException {
        Member m = getMember(memberId);
        Book b = getBook(bookId);
        LocalDate issued = issueDates.get(bookId);
        if (issued != null && returnDate.isBefore(issued)) throw new InvalidReturnDateException();
        b.available = true;
        m.borrowed.remove(bookId);
        issueDates.remove(bookId);
        System.out.println("Member " + memberId + " returned " + bookId + " successfully.");
    }
}

// ---------- Main ----------
public class LibrarySystemDemo {
    public static void main(String[] args) {
        Library lib = new Library();
        lib.addBook(new Book("B101", "Java Programming"));
        lib.addBook(new Book("B102", "Data Structures"));
        lib.addBook(new Book("B103", "Operating Systems"));
        lib.addBook(new Book("B104", "Computer Networks"));
        lib.addMember(new Member("M001", "Asha"));

        LocalDate today = LocalDate.of(2026, 10, 2);

        System.out.println("===== Library Book Borrowing System =====\n");
        lib.search("Java");

        try { lib.issueBook("M001", "B101", today); }
        catch (Exception e) { System.out.println("Error: " + e.getMessage()); }

        try { lib.issueBook("M001", "B101", today); }          // unavailable
        catch (Exception e) { System.out.println("Error: " + e.getMessage()); }

        try { lib.issueBook("M001", "B999", today); }          // invalid book
        catch (Exception e) { System.out.println("Error: " + e.getMessage()); }

        try { lib.issueBook("M999", "B102", today); }          // invalid member
        catch (Exception e) { System.out.println("Error: " + e.getMessage()); }

        try {                                                  // limit exceeded
            lib.issueBook("M001", "B102", today);
            lib.issueBook("M001", "B103", today);
            lib.issueBook("M001", "B104", today);
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }

        try { lib.returnBook("M001", "B101", today.minusDays(5)); } // bad date
        catch (Exception e) { System.out.println("Error: " + e.getMessage()); }

        try { lib.returnBook("M001", "B101", today.plusDays(7)); }
        catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        finally { System.out.println("\nThank you for using the Library System."); }
    }
}
package lin.hung.pin.web;

import java.io.Serializable;

/**
 * Created by linhonbin on 2017/11/6.
 */

public class BookShelf implements Serializable{
    private int bookId;
    private String bookName;
    private String mainFileName;

    public BookShelf() {

    }

    public BookShelf(String bookName, String mainFileName) {
        this.bookName = bookName;
        this.mainFileName = mainFileName;
    }

    public BookShelf(int bookId, String bookName, String mainFileName) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.mainFileName = mainFileName;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getMainFileName() {
        return mainFileName;
    }

    public void setMainFileName(String mainFileName) {
        this.mainFileName = mainFileName;
    }
}

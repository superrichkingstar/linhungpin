package lin.hung.pin.db;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/11/6.
 */
public class BookList extends DataSupport implements Serializable{
    private int id;
    private String bookname;
    private String bookpath;
    private long begin;
    private String charset;
    private String imagepath;
    private byte[] imageArray;

    public BookList() {

    }

    public BookList(String bookname, String bookpath, long begin, String charset, String imagepath) {
        this.bookname = bookname;
        this.bookpath = bookpath;
        this.begin = begin;
        this.charset = charset;
        this.imagepath = imagepath;
    }

    public BookList(String bookname, String bookpath, long begin, String charset, String imagepath, byte[] imageArray) {
        this.bookname = bookname;
        this.bookpath = bookpath;
        this.begin = begin;
        this.charset = charset;
        this.imagepath = imagepath;
        this.imageArray = imageArray;
    }

    public BookList(int id, String bookname, String bookpath, long begin, String charset, String imagepath) {
        this.id = id;
        this.bookname = bookname;
        this.bookpath = bookpath;
        this.begin = begin;
        this.charset = charset;
        this.imagepath = imagepath;
    }

    public String getBookname() {
        return this.bookname;
    }

    public void setBookname(String bookname) {
        this.bookname = bookname;
    }

    public String getBookpath() {
        return this.bookpath;
    }

    public void setBookpath(String bookpath) {
        this.bookpath = bookpath;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public byte[] getImageArray() {
        return imageArray;
    }

    public void setImageArray(byte[] imageArray) {
        this.imageArray = imageArray;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

}

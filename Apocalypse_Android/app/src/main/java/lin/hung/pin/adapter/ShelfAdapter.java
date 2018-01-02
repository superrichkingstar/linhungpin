package lin.hung.pin.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import lin.hung.pin.Config;
import lin.hung.pin.db.BookList;
import lin.hung.pin.view.DragGridListener;
import lin.hung.pin.view.DragGridView;

import org.litepal.crud.DataSupport;
import org.litepal.exceptions.DataSupportException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import lin.hung.pin.R;


/**
 * Created by Administrator on 2017/11/6.
 */
public class ShelfAdapter extends BaseAdapter implements DragGridListener {
    private Context mContex;
    private List<BookList> bilist;
    private static LayoutInflater inflater = null;
    private int mHidePosition = -1;
    private Typeface typeface;
    protected List<AsyncTask<Void, Void, Boolean>> myAsyncTasks = new ArrayList<>();
    private int[] firstLocation;
    private Config config;
    public ShelfAdapter(Context context, List<BookList> bilist){
        this.mContex = context;
        this.bilist = bilist;
        config = Config.getInstance();
        typeface = config.getTypeface();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        //背景書架的draw需要用到item的高度
        if (bilist.size() < 10) {
            return 10;
        } else {
            return bilist.size();
        }
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return bilist.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        final ViewHolder viewHolder;
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.shelfitem, null);
            //contentView = inflater.inflate(R.layout.shelfitem, arg2, false);
            viewHolder = new ViewHolder(contentView);
            viewHolder.name.setTypeface(typeface);
//            viewHolderNameSetData(position, viewHolder);
            contentView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) contentView.getTag();
//            viewHolderNameSetData(position, viewHolder);
        }

        if(bilist.size() > position){
            //DragGridView  解決復用問題
            if(position == mHidePosition){
                contentView.setVisibility(View.INVISIBLE);
            }else {
                contentView.setVisibility(View.VISIBLE);
            }
            if (DragGridView.getShowDeleteButton()) {
                viewHolder.deleteItem_IB.setVisibility(View.VISIBLE);
            }else {
                viewHolder.deleteItem_IB.setVisibility(View.INVISIBLE);
            }
            viewHolder.name.setVisibility(View.VISIBLE);
            String fileName = bilist.get(position).getBookname();
            viewHolder.name.setText(fileName);
            viewHolderNameSetData(position, viewHolder);
        }else {
            contentView.setVisibility(View.INVISIBLE);
        }
        return contentView;
    }

    static class ViewHolder {
        @Bind(R.id.ib_close)
        ImageButton deleteItem_IB;
        @Bind(R.id.tv_name)
        TextView name;
        @Bind(R.id.tv_image)
        TextView image;

        public ViewHolder(View view) { ButterKnife.bind(this, view); }
    }

    /**
     * Drag移動時item交換數據,並在數據庫中更新交換後的位置數據
     * @param oldPosition
     * @param newPosition
     */
    @Override
    public void reorderItems(int oldPosition, int newPosition) {

        BookList temp = bilist.get(oldPosition);
        List<BookList> bookLists1 = new ArrayList<>();
        bookLists1 = DataSupport.findAll(BookList.class);

        int tempId = bookLists1.get(newPosition).getId();
        // Log.d("oldposotion is",oldPosition+"");
        // Log.d("newposotion is", newPosition + "");
        if(oldPosition < newPosition){
            for(int i=oldPosition; i<newPosition; i++){
                //獲得交換前的ID,必須是數據庫的真正的ID，如果使用bilist獲取id是錯誤的，因為bilist交換後id是跟著交換的
                List<BookList> bookLists = new ArrayList<>();
                bookLists = DataSupport.findAll(BookList.class);
                int dataBasesId = bookLists.get(i).getId();
                Collections.swap(bilist, i, i + 1);

                updateBookPosition(i,dataBasesId, bilist);

            }
        }else if(oldPosition > newPosition){
            for(int i=oldPosition; i>newPosition; i--) {
                List<BookList> bookLists = new ArrayList<>();
                bookLists = DataSupport.findAll(BookList.class);
                int dataBasesId = bookLists.get(i).getId();

                Collections.swap(bilist, i, i - 1);

                updateBookPosition(i,dataBasesId,bilist);

            }
        }

        bilist.set(newPosition, temp);
        updateBookPosition(newPosition, tempId, bilist);

    }

    /**
     * 兩個item數據交換結束後，把不需要再交換的item更新到數據庫中
     * @param position
     * @param bookLists
     */
    public void updateBookPosition (int position,int databaseId,List<BookList> bookLists) {
        BookList bookList = new BookList();
        String bookpath = bookLists.get(position).getBookpath();
        String bookname = bookLists.get(position).getBookname();
        bookList.setBookpath(bookpath);
        bookList.setBookname(bookname);
        bookList.setBegin(bookLists.get(position).getBegin());
        bookList.setCharset(bookLists.get(position).getCharset());
        bookList.setImagepath(bookLists.get(position).getImagepath());
        bookList.setImageArray(bookLists.get(position).getImageArray());
        //開線程保存改動的數據到數據庫
        //使用litepal數據庫框架update時每次只能update一個id中的一條信息，如果相同則不更新。
        upDateBookToSqlite3(databaseId , bookList);
    }

    /**
     * 隱藏item
     * @param hidePosition
     */
    @Override
    public void setHideItem(int hidePosition) {
        this.mHidePosition = hidePosition;
        notifyDataSetChanged();
    }

    /**
     * 刪除書本
     * @param deletePosition
     */
    @Override
    public void removeItem(int deletePosition) {

        String bookpath = bilist.get(deletePosition).getBookpath();
        DataSupport.deleteAll(BookList.class, "bookpath = ?", bookpath);
        bilist.remove(deletePosition);
        // Log.d("刪除的書本是", bookpath);

        notifyDataSetChanged();

    }

    public void setBookList(List<BookList> bookLists){
        this.bilist = bookLists;
        notifyDataSetChanged();
    }
    /**
     * Book打開後位置移動到第一位
     * @param openPosition
     */
    @Override
    public void setItemToFirst(int openPosition) {

        List<BookList> bookLists1 = new ArrayList<>();
        bookLists1 = DataSupport.findAll(BookList.class);
        int tempId = bookLists1.get(0).getId();
        BookList temp = bookLists1.get(openPosition);
        // Log.d("setitem adapter ",""+openPosition);
        if(openPosition!=0) {
            for (int i = openPosition; i > 0 ; i--) {
                List<BookList> bookListsList = new ArrayList<>();
                bookListsList = DataSupport.findAll(BookList.class);
                int dataBasesId = bookListsList.get(i).getId();

                Collections.swap(bookLists1, i, i - 1);
                updateBookPosition(i, dataBasesId, bookLists1);
            }

            bookLists1.set(0, temp);
            updateBookPosition(0, tempId, bookLists1);
            for (int j = 0 ;j<bookLists1.size();j++) {
                String bookpath = bookLists1.get(j).getBookpath();
                //  Log.d("移動到第一位",bookpath);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void nitifyDataRefresh() {
        notifyDataSetChanged();
    }

    public void putAsyncTask(AsyncTask<Void, Void, Boolean> asyncTask) {
        myAsyncTasks.add(asyncTask.execute());
    }

    /**
     * 數據庫書本信息更新
     * @param databaseId  要更新的數據庫的書本ID
     * @param bookList
     */
    public void upDateBookToSqlite3(final int databaseId,final BookList bookList) {

        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {

            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    bookList.update(databaseId);
                } catch (DataSupportException e) {
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {

                } else {
                    Log.d("保存到數據庫結果-->", "失敗");
                }
            }
        });
    }



    public void viewHolderNameSetData(int position, ViewHolder viewHolder) {
        byte[] b = bilist.get(position).getImageArray();
        if(b!=null) {
            Drawable image = new BitmapDrawable(BitmapFactory.decodeByteArray(b, 0, b.length));
            viewHolder.image.setBackground(image);
        } else {
            viewHolder.image.setBackground(mContex.getDrawable(R.drawable.cover_default_new));
        }
    }
}

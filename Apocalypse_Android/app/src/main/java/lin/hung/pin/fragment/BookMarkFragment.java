package lin.hung.pin.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import lin.hung.pin.adapter.MarkAdapter;
import lin.hung.pin.base.BaseFragment;
import lin.hung.pin.db.BookMarks;
import lin.hung.pin.util.PageFactory;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import lin.hung.pin.R;


/**
 * Created by Administrator on 2017/11/6 0031.
 */
public class BookMarkFragment extends BaseFragment {
    public static final String ARGUMENT = "argument";

    @Bind(R.id.lv_bookmark)
    ListView lv_bookmark;

    private String bookpath;
    private String mArgument;
    private List<BookMarks> bookMarksList;
    private MarkAdapter markAdapter;
    private PageFactory pageFactory;
    private boolean flag;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_bookmark;
    }

    @Override
    protected void initData(View view) {
        pageFactory = PageFactory.getInstance();
        Bundle bundle = getArguments();
        if (bundle != null) {
            bookpath = bundle.getString(ARGUMENT);
        }
        bookMarksList = new ArrayList<>();
        bookMarksList = DataSupport.where("bookpath = ?", bookpath).find(BookMarks.class);
        markAdapter = new MarkAdapter(getActivity(), bookMarksList);
        lv_bookmark.setAdapter(markAdapter);
    }

    @Override
    protected void initListener() {
        lv_bookmark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(flag==false) {
                    pageFactory.changeChapter(bookMarksList.get(position).getBegin());
                    getActivity().finish();
                }
            }
        });
        lv_bookmark.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                flag=true;
                new AlertDialog.Builder(getActivity())
                        .setTitle("提示")
                        .setMessage("是否刪除書籤？")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                flag=false;
                            }
                        })
                        .setPositiveButton("刪除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataSupport.delete(BookMarks.class,bookMarksList.get(position).getId());
                                bookMarksList.clear();
                                bookMarksList.addAll(DataSupport.where("bookpath = ?", bookpath).find(BookMarks.class));
                                markAdapter.notifyDataSetChanged();
                                flag=false;
                            }
                        }).setCancelable(false).show();//點擊外面和按返回鍵不會自動關閉對話框的設置
                return false;
            }
        });
    }

    /**
     * 用於從Activity傳遞數據到Fragment
     * @param bookpath
     * @return
     */
    public static BookMarkFragment newInstance(String bookpath)
    {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, bookpath);
        BookMarkFragment bookMarkFragment = new BookMarkFragment();
        bookMarkFragment.setArguments(bundle);
        return bookMarkFragment;
    }

}

package lin.hung.pin.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lin.hung.pin.db.BookList;
import lin.hung.pin.util.Fileutil;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import lin.hung.pin.R;


/**
 * Created by Administrator on 2017/11/6.
 */
public class FileAdapter extends BaseAdapter {
    private List<File> files;
    private HashMap<File, Boolean> checkMap = new HashMap<>();

    private Context context;
    private CheckedChangeListener mCheckedChangeListener;

    public FileAdapter(Context context) {
        this.context = context;
    }

    public FileAdapter(Context context, List<File> files) {
        this.context = context;
        this.files = files;
        initCheckMap();
    }

    private void initCheckMap() {
        if (files != null) {
            for (File file : files) {
                checkMap.put(file, false);
            }
        }
    }

    @Override
    public int getCount() {
        if (files == null) {
            return 0;
        }
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //全選
    public void checkAll() {
        Iterator iter = checkMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            checkMap.put((File) entry.getKey(), true);
        }
        notifyDataSetChanged();
    }

    //取消
    public void cancel() {
        Iterator iter = checkMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            checkMap.put((File) entry.getKey(), false);
        }
        notifyDataSetChanged();
    }

    //選擇的數目
    public int getCheckNum() {
        int num = 0;
        Iterator iter = checkMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if ((Boolean) entry.getValue()) {
                num++;
            }
        }
        return num;
    }

    public List<File> getCheckFiles() {
        List<File> files = new ArrayList<>();
        Iterator iter = checkMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if ((Boolean) entry.getValue()) {
                files.add((File) entry.getKey());
            }
        }
        return files;
    }

    public HashMap<File, Boolean> getCheckMap() {
        return checkMap;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
        initCheckMap();
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final File file = files.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.adapter_file_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        //CheckBox狀態變化監聽
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkMap.put(file, isChecked);
                if (mCheckedChangeListener != null) {
                    mCheckedChangeListener.onCheckedChanged(position, buttonView, isChecked);
                }
            }
        });


        initFileData(file, viewHolder);
        initCheckBox(file, viewHolder);


        return convertView;
    }

    private void initCheckBox(File file, ViewHolder viewHolder) {
        if (checkMap.get(file) != null) {
            viewHolder.checkBox.setChecked(checkMap.get(file));
        }
    }

    private void initFileData(File file, ViewHolder viewHolder) {
        //設置文件名
        viewHolder.textView.setText(file.getName());
        viewHolder.checkBox.setVisibility(View.GONE);
        viewHolder.textImported.setVisibility(View.GONE);
        List<BookList> books = DataSupport.where("bookpath LIKE '%/" + new File(file.getParent()).getName() + "/" + file.getName() + "'").find(BookList.class);
//        List<BookList> books = DataSupport.where("bookpath = ?", bookPath).find(BookList.class);
        //文件夾和文件邏輯判斷
        if (file.isDirectory()) {
            viewHolder.fileIcon.setImageResource(R.mipmap.folder);
            viewHolder.checkBox.setVisibility(View.INVISIBLE);
            viewHolder.textSize.setText("項");
        } else {
            if (books.size() > 0) {
                viewHolder.fileIcon.setImageResource(R.mipmap.file_type_txt);
                viewHolder.textImported.setVisibility(View.VISIBLE);
                viewHolder.textImported.setTextColor(Color.RED);
                viewHolder.textImported.setText("已導入");
                viewHolder.textSize.setText(Fileutil.formatFileSize(file.length()));
            } else {
                viewHolder.fileIcon.setImageResource(R.mipmap.file_type_txt);
                viewHolder.checkBox.setVisibility(View.VISIBLE);
                viewHolder.textSize.setText(Fileutil.formatFileSize(file.length()));
            }
        }
    }

    static class ViewHolder {
        @Bind(R.id.tv_file_text)
        TextView textView;
        @Bind(R.id.tv_file_text_size)
        TextView textSize;
        @Bind(R.id.iv_file_icon)
        ImageView fileIcon;
        @Bind(R.id.cb_file_image)
        CheckBox checkBox;
        @Bind(R.id.tv_Imported)
        TextView textImported;
        @Bind(R.id.ll_file_lin)
        LinearLayout linearLayout;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void setCheckedChangeListener(CheckedChangeListener checkedChangeListener) {
        mCheckedChangeListener = checkedChangeListener;
    }

    public interface CheckedChangeListener {
        void onCheckedChanged(int position, CompoundButton buttonView, boolean isChecked);
    }
}

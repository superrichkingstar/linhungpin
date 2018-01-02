package lin.hung.pin.view;

/**
 * Created by Lxq on 2017/11/6.
 */
public interface DragGridListener {
    /**
     * 重新排列數據
     * @param oldPosition
     * @param newPosition
     */
    public void reorderItems(int oldPosition, int newPosition);


    /**
     * 設置某個item隱藏
     * @param hidePosition
     */
    public void setHideItem(int hidePosition);


    /**
     * 刪除某個item
     * @param deletePosition
     */
    public void removeItem(int deletePosition);

    /**
     * 設置點擊打開後的item移動到第一位置
     * @param openPosition
     */
    void setItemToFirst(int openPosition);

    void nitifyDataRefresh();
}

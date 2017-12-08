package cn.com.pyc.drm.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.maxwin.view.ScaleImageView;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.Constant;
import cn.com.pyc.drm.model.ProductInfo;
import cn.com.pyc.drm.ui.BaseActivity;
import cn.com.pyc.drm.utils.CommonUtil;
import cn.com.pyc.drm.utils.ConvertToUtils;
import cn.com.pyc.drm.utils.help.ImageLoadHelp;

/**
 * mainpage-list
 */
public class MainListAdapter extends BaseAdapter {

    private BaseActivity context;
    private List<ProductInfo> infos = new ArrayList<ProductInfo>();
    private int imgWidth;
    //private Drawable coverDrawable;

    public MainListAdapter(BaseActivity context, List<ProductInfo> infos) {
        super();
        this.infos = infos;
        this.context = context;

        imgWidth = (Constant.screenWidth - CommonUtil.dip2px(context, 10)) / 2;
        //Resources resources = this.context.getResources();
        //coverDrawable = resources.getDrawable(R.drawable.ic_cover_page);
    }

    public List<ProductInfo> getInfos() {
        return infos;
    }

    public void addLastInfos(List<ProductInfo> infos) {
        this.infos.addAll(this.infos.size(), infos);
    }

    @Override
    public int getCount() {
        return infos != null ? infos.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_main_list, null);
            holder = new Holder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        ProductInfo o = infos.get(position);
        // 设置图片,系数 picture_ratio = width/height
        double ratio = ConvertToUtils.toDouble(o.getPicture_ratio(), 1d);
        holder.image.setImageWidth(imgWidth);
        holder.image.setImageHeight((int) (imgWidth / ratio));

        holder.name.setText(o.getProductName());
        holder.author.setText(o.getAuthors());
        //ImageLoader.getInstance().displayImage(o.getPicture_url(), holder.image);
        ImageLoadHelp.loadImage(holder.image, o.getPicture_url());
        return convertView;
    }

    private static class Holder {
        private ScaleImageView image;
        private TextView name;
        private TextView author;

        private Holder(View convertView) {
            if (null == convertView)
                throw new IllegalArgumentException(
                        "convertView require not null.");

            image = (ScaleImageView) convertView.findViewById(R.id.main_item_image);
            name = (TextView) convertView.findViewById(R.id.main_item_name);
            author = (TextView) convertView.findViewById(R.id.main_item_author);
        }
    }
}

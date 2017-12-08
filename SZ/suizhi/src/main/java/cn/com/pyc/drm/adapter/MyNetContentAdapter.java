package cn.com.pyc.drm.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.maxwin.view.ScaleImageView;

import java.util.ArrayList;
import java.util.List;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.model.ProductInfo;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.help.ImageLoadHelp;
import cn.com.pyc.drm.widget.impl.OnRecyclerViewItemClickListener;

//网络Adapter
public class MyNetContentAdapter extends Adapter<MyNetContentAdapter.MyHolder> {
    private Context mContext;
    private List<ProductInfo> oNetList = new ArrayList<>();

    //private int imgWidth;
    //private int imgHeight;


    public MyNetContentAdapter(Context context, List<ProductInfo> oNetList) {
        this.mContext = context;
        this.oNetList = oNetList;

        //imgWidth = (Constant.screenWidth - CommonUtil.dip2px(context, 10)) / 2;
        //imgHeight = (int) (imgWidth * 0.8);
    }

    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener l) {
        this.onRecyclerViewItemClickListener = l;
    }


    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ScaleImageView itemTopBack;
        private TextView itemTopName;
        private TextView itemBottomName;
//        private TextView itemBottomTime;
//        private TextView itemTopTime;
//        private ImageView itemTopIcion;
//        private TextView itemTopCategory;//等级

        MyHolder(View convertView) {
            super(convertView);
            itemTopBack = (ScaleImageView) convertView.findViewById(R.id.item_top_back);
            itemTopName = (TextView) convertView.findViewById(R.id.item_top_name);
//            itemTopName.setBackgroundColor(Color.argb(60,60,60,60));
//            itemTopTime = (TextView) convertView.findViewById(R.id.item_top_time);
//            itemTopIcion = (ImageView) convertView.findViewById(R.id.item_imv_top_ic);
//            itemTopCategory = (TextView) convertView.findViewById(R.id.item_txt_top_category);
//            itemBottomTime = (TextView) convertView.findViewById(R.id.item_bottom_time);
            itemBottomName = (TextView) convertView.findViewById(R.id.item_bottom_name);

            //将创建的View注册点击事件
            convertView.setOnClickListener(this);
            itemBottomName.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onRecyclerViewItemClickListener != null && v.getTag() != null) {
                //注意这里使用getTag方法获取position
                onRecyclerViewItemClickListener.onItemClick(v, (int) v.getTag());
            }
            switch (v.getId()) {
                case R.id.item_bottom_name:
                    OpenPageUtil.openBrowserOfApp(mContext,
                            APIUtil.getStoreShopUrl(oNetList.get(getLayoutPosition() - 1)
                                    .getProduct_creater_id()));
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return oNetList.size();
    }

    public ProductInfo getProduct(int position) {
        return oNetList.get(position);
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup arg0, int arg1) {

        View view = LayoutInflater.from(arg0.getContext()).inflate(
                R.layout.item_mycontent_local, arg0, false);

        return new MyHolder(view);
    }

    public List<ProductInfo> getInfos() {
        return oNetList;
    }

    public void addLastInfos(List<ProductInfo> infos) {
        this.oNetList.addAll(this.oNetList.size(), infos);
    }


    @Override
    public void onBindViewHolder(MyHolder myHolder, int position) {

        myHolder.itemView.setTag(position);
        myHolder.itemTopName.setText(oNetList.get(position).getProductName());
        myHolder.itemBottomName.setText(oNetList.get(position).getStoreName());

//        myHolder.itemTopBack.setImageWidth(imgWidth);
//        myHolder.itemTopBack.setImageHeight(imgHeight);
        ImageLoadHelp.loadImage(myHolder.itemTopBack, oNetList.get(position).getPicture_url());

//        //有效期显示
//        if (oNetList.get(position).getPublishDate() != null) {
//            myHolder.itemBottomTime.setText("有效期：" + oNetList.get(position).getPublishDate());
//        }

//        myHolder.itemTopCategory.setText(oNetList.get(position).getGrade());

//        if(oNetList.get(position).getCategory().equals(DrmPat.VIDEO)){
//            myHolder.itemTopIcion.setImageResource(R.drawable.icon_white_video);
//        }else if(oNetList.get(position).getCategory().equals(DrmPat.MUSIC)){
//            myHolder.itemTopIcion.setImageResource(R.drawable.icon_white_audio);
//        }else if(oNetList.get(position).getCategory().equals(DrmPat.BOOK)){
//            myHolder.itemTopIcion.setImageResource(R.drawable.icon_white_book);
//        }
    }

}

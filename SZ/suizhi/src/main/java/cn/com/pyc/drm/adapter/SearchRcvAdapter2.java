package cn.com.pyc.drm.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.maxwin.view.ScaleImageView;

import java.text.DecimalFormat;
import java.util.List;

import cn.com.pyc.drm.R;
import cn.com.pyc.drm.common.DrmPat;
import cn.com.pyc.drm.model.SearchResultModel.SearchResult;
import cn.com.pyc.drm.utils.APIUtil;
import cn.com.pyc.drm.utils.ConvertToUtils;
import cn.com.pyc.drm.utils.DRMLog;
import cn.com.pyc.drm.utils.FormatUtil;
import cn.com.pyc.drm.utils.OpenPageUtil;
import cn.com.pyc.drm.utils.ViewUtil;
import cn.com.pyc.drm.utils.help.ImageLoadHelp;
import cn.com.pyc.drm.widget.impl.OnRecyclerViewItemClickListener;

public class SearchRcvAdapter2 extends Adapter<SearchRcvAdapter2.MyHolder> {
    private Context mContext;
    private List<SearchResult> searchs;
    //private Drawable videoD, musicD, bookD;
    //private int imgWidth;
    //private int imgHeight;

    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener l) {
        this.onRecyclerViewItemClickListener = l;
    }

    public SearchRcvAdapter2(Context context, List<SearchResult> searchs) {
        this.mContext = context;
        this.searchs = searchs;

//        imgWidth = (Constant.screenWidth - CommonUtil.dip2px(context, 10)) / 2;
//        imgHeight = (int) (imgWidth * 0.8);
//        videoD = mContext.getResources().getDrawable(R.drawable.icon_white_video);
//        musicD = mContext.getResources().getDrawable(R.drawable.icon_white_audio);
//        bookD = mContext.getResources().getDrawable(R.drawable.icon_white_book);
    }

    public SearchResult getResult(int position) {
        return searchs != null ? searchs.get(position) : null;
    }

    public List<SearchResult> getContends() {
        return searchs;
    }

    public void addData(List<SearchResult> searchs) {
        this.searchs.addAll(searchs);
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ScaleImageView image;
        private TextView name;
        //private ImageView category;
        //private TextView shelvesTime;
        //private TextView grade;
        //private TextView totalPrice;
        private TextView discountPrice;
        private TextView sellerName;
        //private TextView validityTime;
        private TextView people;
        private View priceLayout;

        private MyHolder(View convertView) {
            super(convertView);
            image = (ScaleImageView) convertView.findViewById(R.id.search_item_image);
            name = (TextView) convertView.findViewById(R.id.search_item_name);
            //category = ((ImageView) convertView.findViewById(R.id.search_item_category));
            //shelvesTime = (TextView) convertView.findViewById(R.id.search_item_shelves_time);
            //grade = (TextView) convertView.findViewById(R.id.search_item_grade);
            //totalPrice = (TextView) convertView.findViewById(R.id.search_item_total_price);
            discountPrice = (TextView) convertView.findViewById(R.id.search_item_discount_price);
            sellerName = (TextView) convertView.findViewById(R.id.search_item_seller_name);
            //validityTime = (TextView) convertView.findViewById(R.id.search_item_validity);
            people = (TextView) convertView.findViewById(R.id.search_item_people);
            priceLayout = convertView.findViewById(R.id.search_item_rel_price);

            //添加点击事件
            convertView.setOnClickListener(this);
            sellerName.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onRecyclerViewItemClickListener != null && v.getTag() != null) {
                //注意这里使用getTag方法获取position
                onRecyclerViewItemClickListener.onItemClick(v, (int) v.getTag());
            }
            switch (v.getId()) {
                case R.id.search_item_seller_name:
                    SearchResult sr = getResult(getLayoutPosition() - 1);
                    if (sr == null) return;
                    OpenPageUtil.openBrowserOfApp(mContext, APIUtil.getStoreShopUrl(sr
                            .getSellerId()));
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return searchs != null ? searchs.size() : 0;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_search_list2, arg0,
                false);
        return new MyHolder(itemView);
    }

//    @Override
//    public int getItemViewType(int position) {
//        return TYPE_ITEM;
//    }

    @Override
    public void onBindViewHolder(MyHolder arg0, int arg1) {
        arg0.itemView.setTag(arg1);  //将position保存在itemView的Tag中，以便点击时进行获取
        SearchResult result = getResult(arg1);
        if (result == null) return;
        initItem(result, arg0);
    }

    private void initItem(SearchResult result, SearchRcvAdapter2.MyHolder holder) {
        // 设置图片,系数 picture_ratio = width/height
        //double ratio = ConvertToUtils.toDouble(result.getPicture_ratio(), 1.0);
        //ratio = (ratio == 0) ? 1.0 : ratio;
        //holder.image.setImageWidth(imgWidth);
        //arg0.image.setImageHeight((int) (imgWidth / ratio));
        //holder.image.setImageHeight(imgHeight);
        //holder.shelvesTime.setText(result.getOnShelves_time());
        //holder.grade.setText(result.getGrade());
        holder.name.setText(result.getProductName());
        holder.sellerName.setText(result.getSellerName());
        holder.people.setText(FormatUtil.formatPVNo(result.getProductPv()));
        if (result.getSource() == DrmPat.BUYED) {
            //已购买的，不显示价格一栏
            ViewUtil.hideWidget(holder.priceLayout);
        } else {
            ViewUtil.showWidget(holder.priceLayout);
            double totalPrice = ConvertToUtils.toDouble(result.getTotalPrice());
            double discountPrice = ConvertToUtils.toDouble(result.getDiscountPrice());
            DRMLog.e(result.getProductName() + ":原价" + totalPrice + ",折扣价：" + discountPrice);
            if (totalPrice <= 0.0d) {
                //免费
                //ViewUtil.hideWidget(holder.totalPrice);
                holder.discountPrice.setText(mContext.getString(R.string.search_product_free));
            } else {
                if (discountPrice <= 0.0d) {
                    //仅显示原价
                    String totalPrice_ = result.getTotalPrice();
                    //ViewUtil.hideWidget(holder.totalPrice);
                    if (totalPrice_ != null && totalPrice_.length() > 4) { //超过4位，取两位小数
                        DecimalFormat df = new java.text.DecimalFormat("#0.00");
                        totalPrice_ = df.format(totalPrice);
                    }
                    holder.discountPrice.setText(mContext.getString(R.string.search_product_price,
                            totalPrice_));
                } else {
                    //显示折扣和原价
                    //ViewUtil.showWidget(holder.totalPrice);
                    holder.discountPrice.setText(mContext.getString(R.string.search_product_price,
                            result.getDiscountPrice()));
                    //holder.totalPrice.setText(mContext.getString(R.string.search_product_price,
                    //        result.getTotalPrice()));
                    //holder.totalPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //中间横线
                    //holder.totalPrice.getPaint().setAntiAlias(true);// 抗锯齿
                }
            }
        }
        //有效期类型：为空表示没有设置有效期，daterange表示设置有效期期限，days表示设置天数
//        if ("daterange".equals(result.getValidity())) {
//            holder.validityTime.setText(mContext.getString(R.string.search_validity_1, result
//                    .getUse_start_time(), result.getUse_end_time()));
//        } else if ("days".equals(result.getValidity())) {
//            holder.validityTime.setText(mContext.getString(R.string.search_validity_2, result
//                    .getUseDays()));
//        } else {
//            holder.validityTime.setText(mContext.getString(R.string.search_validity_3));
//        }
//        if (DrmPat.VIDEO.equals(result.getCategory())) {
//            holder.category.setImageDrawable(videoD);
//        } else if (DrmPat.MUSIC.equals(result.getCategory())) {
//            holder.category.setImageDrawable(musicD);
//        } else if (DrmPat.BOOK.equals(result.getCategory())) {
//            holder.category.setImageDrawable(bookD);
//        } else {
//            holder.category.setImageDrawable(null);
//        }
        ImageLoadHelp.loadImage(holder.image, result.getPicture_url());
    }

}

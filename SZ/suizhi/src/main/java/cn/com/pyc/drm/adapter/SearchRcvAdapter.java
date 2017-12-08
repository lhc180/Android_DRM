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
import java.util.ArrayList;
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

public class SearchRcvAdapter extends Adapter<SearchRcvAdapter.MyHolder> implements View
        .OnClickListener {
    private static final int TYPE_TEXT = 1;
    private static final int TYPE_ITEM = 2;
    private Context mContext;
    private List<SearchResult> buys;
    private List<SearchResult> searchs;
    private List<SearchResult> contends = new ArrayList<>();

//    private Drawable videoD, musicD, bookD;
//    private int imgWidth;
//    private int imgHeight;
    private boolean buyEmpty = false;
    private int buySize;

    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener l) {
        this.onRecyclerViewItemClickListener = l;
    }

    public SearchRcvAdapter(Context context, List<SearchResult> buys,
                            List<SearchResult> searchs) {
        this.mContext = context;
        this.buys = buys;
        this.searchs = searchs;

        contends.clear();
        buyEmpty = (this.buys == null || this.buys.isEmpty());

        if (!buyEmpty) contends.addAll(this.buys);
        if (!buyEmpty) buySize = this.buys.size();

        contends.addAll(this.searchs);
//        imgWidth = (Constant.screenWidth - CommonUtil.dip2px(context, 10)) / 2;
//        imgHeight = (int) (imgWidth * 0.8);
//        videoD = mContext.getResources().getDrawable(R.drawable.icon_white_video);
//        musicD = mContext.getResources().getDrawable(R.drawable.icon_white_audio);
//        bookD = mContext.getResources().getDrawable(R.drawable.icon_white_book);
    }

    public SearchResult getResult(int position) {
        if (contends.size() == 0) return null;
        if (buyEmpty) {
            return contends.get(position - 1);
        } else {
            if (position <= buySize) {
                return contends.get(position - 1); // 减去一个头部
            } else {
                return contends.get(position - 2);// 减去两个头部
            }
        }
    }

    public List<SearchResult> getContends() {
        return contends;
    }

    public void addData(List<SearchResult> searchs) {
        this.contends.addAll(searchs);
    }

    class MyHolder extends RecyclerView.ViewHolder {
        private ScaleImageView image;
        private TextView name;
        //private ImageView category;
        //private TextView shelvesTime;
        //private TextView grade;
        //private TextView totalPrice;
        private TextView discountPrice;
        private TextView sellerName;
        //private TextView validityTime;
        private View priceLayout;
        private TextView people;
        private TextView headType;

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
            headType = (TextView) convertView.findViewById(R.id.item_search_type);

            //添加点击事件
            convertView.setOnClickListener(SearchRcvAdapter.this);
            if (sellerName != null) {
                sellerName.setOnClickListener(SearchRcvAdapter.this);
            }
        }
    }

    @Override
    public int getItemCount() {
        return buyEmpty ? contends.size() + 1 : contends.size() + 2;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup arg0, int arg1) {
        MyHolder holder = null;
        View itemView = null;
        switch (arg1) {
            case TYPE_ITEM:
                itemView = LayoutInflater.from(mContext).inflate(
                        R.layout.item_search_list2, arg0, false);
                holder = new MyHolder(itemView);
                break;
            case TYPE_TEXT:
                itemView = LayoutInflater.from(mContext).inflate(
                        R.layout.item_search_text, arg0, false);
                holder = new MyHolder(itemView);
                break;
            default:
                break;
        }
        //将创建的View注册点击事件
//        if (itemView != null) {
//            itemView.setOnClickListener(SearchRcvAdapter.this);
//        }
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_TEXT;
        if (!buyEmpty && position == (buySize + 1)) return TYPE_TEXT;
        return TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(MyHolder arg0, int arg1) {
        switch (getItemViewType(arg1)) {
            case TYPE_TEXT: {
                //StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager
                //        .LayoutParams) arg0.headType.getLayoutParams();
                //lp.setFullSpan(true); // 设置占满一行
                if (buyEmpty) {
                    if (arg1 == 0) arg0.headType.setText("猜您喜欢");
                } else {
                    arg0.headType.setText(arg1 == 0 ? "我购买的" : "猜您喜欢");
                }
                //arg0.headType.setLayoutParams(lp);
            }
            break;
            case TYPE_ITEM: {
                arg0.itemView.setTag(arg1);  //将position保存在itemView的Tag中，以便点击时进行获取
                arg0.sellerName.setTag(arg1);
                SearchResult result = getResult(arg1);
                if (result == null) return;
                initItem(result, arg0);
            }
            break;
            default:
                break;
        }
    }

    private void initItem(SearchResult result, MyHolder holder) {
        // 设置图片,系数 picture_ratio = width/height
        //double ratio = ConvertToUtils.toDouble(result.getPicture_ratio(), 1.0);
        //ratio = (ratio == 0) ? 1.0 : ratio;
        //holder.image.setImageWidth(imgWidth);
        //arg0.image.setImageHeight((int) (imgWidth / ratio));
        //holder.image.setImageHeight(imgHeight);
        //holder.shelvesTime.setText(result.getOnShelves_time());
        //holder.grade.setText(result.getGrade());
        holder.name.setText(result.getProductName());
        holder.people.setText(FormatUtil.formatPVNo(result.getProductPv()));
        holder.sellerName.setText(result.getSellerName());
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

    @Override
    public void onClick(View v) {
        //点击店铺名称
        if (v.getId() == R.id.search_item_seller_name) {
            int pos = (int) v.getTag();
            SearchResult sr = getResult(pos);
            if (sr == null) return;
            OpenPageUtil.openBrowserOfApp(mContext, APIUtil.getStoreShopUrl(sr.getSellerId()));
            return;
        }
        if (onRecyclerViewItemClickListener != null && v.getTag() != null) {
            //注意这里使用getTag方法获取position
            onRecyclerViewItemClickListener.onItemClick(v, (int) v.getTag());
        }
    }
}

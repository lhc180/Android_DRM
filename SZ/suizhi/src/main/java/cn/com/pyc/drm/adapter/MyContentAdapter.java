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
import cn.com.pyc.drm.model.db.bean.Album;
import cn.com.pyc.drm.utils.help.ImageLoadHelp;
import cn.com.pyc.drm.widget.impl.OnRecyclerViewItemClickListener;


public class MyContentAdapter extends Adapter<MyContentAdapter.MyHolder> {
    private Context context;
    private List<Album> albumsLocals = new ArrayList<>();


    public MyContentAdapter(Context context, List<Album> albumsLocals) {
        this.context = context;
        this.albumsLocals = albumsLocals;
    }

    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener l) {
        this.onRecyclerViewItemClickListener = l;
    }

    public List<Album> getAlbums() {
        return albumsLocals;
    }

    public Album getAlbum(int position) {

        return albumsLocals.get(position);
    }


    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ScaleImageView itemTopBack;
        private TextView itemTopName;
        //        private TextView itemTopTime;
//        private ImageView itemTopIcion;
//        private TextView itemTopCategory;
        private TextView itemBottomName;
//        private TextView itemBottomTime;

        MyHolder(View convertView) {
            super(convertView);
            itemTopBack = (ScaleImageView) convertView.findViewById(R.id.item_top_back);
            itemTopName = (TextView) convertView.findViewById(R.id.item_top_name);
//            itemTopTime = (TextView) convertView.findViewById(R.id.item_top_time);
//            itemTopIcion = (ImageView) convertView.findViewById(R.id.item_imv_top_ic);
//            itemTopCategory = (TextView) convertView.findViewById(R.id.item_txt_top_category);
            itemBottomName = (TextView) convertView.findViewById(R.id.item_bottom_name);
//            itemBottomTime = (TextView) convertView.findViewById(R.id.item_bottom_time);

            convertView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onRecyclerViewItemClickListener != null && v.getTag() != null) {
                //注意这里使用getTag方法获取position
                onRecyclerViewItemClickListener.onItemClick(v, (int) v.getTag());
            }
        }
    }

    @Override
    public int getItemCount() {
        return albumsLocals.size();
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup arg0, int arg1) {

        View view = LayoutInflater.from(arg0.getContext()).inflate(
                R.layout.item_mycontent_local, arg0, false);

        return new MyHolder(view);
    }


    @Override
    public void onBindViewHolder(MyHolder myHolder, int position) {

        myHolder.itemView.setTag(position);
        myHolder.itemTopName.setText(albumsLocals.get(position).getName());
//        myHolder.itemTopTime.setText(albumsLocals.get(position).getModify_time());
        myHolder.itemBottomName.setText(albumsLocals.get(position).getAuthor());
//        myHolder.itemBottomTime.setText(albumsLocals.get(position).getModify_time());
        ImageLoadHelp.loadImage(myHolder.itemTopBack, albumsLocals.get(position).getPicture());

//        if(albumsLocals.get(position).getCategory().equals(DrmPat.VIDEO)){
//            myHolder.itemTopCategory.setText("视频文件");
//            myHolder.itemTopIcion.setImageResource(R.drawable.icon_white_video);
//        }else if(albumsLocals.get(position).getCategory().equals(DrmPat.MUSIC)){
//            myHolder.itemTopCategory.setText("音频文件");
//            myHolder.itemTopIcion.setImageResource(R.drawable.icon_white_audio);
//        }else if(albumsLocals.get(position).getCategory().equals(DrmPat.BOOK)){
//            myHolder.itemTopCategory.setText("文档文件");
//            myHolder.itemTopIcion.setImageResource(R.drawable.icon_white_book);
//        }
    }


}

package com.adapter.parent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudstream.cslink.parent.ApplicationData;
import com.cloudstream.cslink.R;
import com.common.view.CircularImageView;

import java.util.ArrayList;

public class ChildrenListAdapter extends BaseAdapter {
    private int flag;
    ArrayList<Childbeans> Date;

    ViewHolder holder;

    Context myc;

    public ChildrenListAdapter(Context c, ArrayList<Childbeans> messageList,int flag) {
        myc = c;
        this.flag =flag;
        this.Date = messageList;
    }

    public void updateListAdapter(ArrayList<Childbeans> messageList) {
        this.Date.clear();
        this.Date.addAll(messageList);
        this.notifyDataSetChanged();
    }
    public int getCount() {
        return Date.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView tachername, subject, txtBadge;
        CircularImageView profile_pic;
        public CircularImageView circle;
        public ImageView img_right_arrow;
        public LinearLayout lin_selectchild_badge;
    }

    @Override
    public View getView(final int pos,  View convertview, ViewGroup arg2) {

        LayoutInflater li = (LayoutInflater) myc.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertview == null) {
            convertview = li.inflate(R.layout.children_list_item, null);

            holder = new ViewHolder();
            holder.tachername = (TextView) convertview.findViewById(R.id.textView_teachername);
            holder.subject = (TextView) convertview.findViewById(R.id.textView_subject);
            holder.txtBadge = (TextView) convertview.findViewById(R.id.txtBadge);
            holder.circle=(CircularImageView) convertview.findViewById(R.id.circle);
            holder.profile_pic = (CircularImageView) convertview.findViewById(R.id.imageView1);
            holder.profile_pic.setBorderWidth(5);
            holder.img_right_arrow=(ImageView)convertview.findViewById(R.id.img_right_arrow);
            holder.lin_selectchild_badge=(LinearLayout)convertview.findViewById(R.id.lin_selectchild_badge);
            holder.circle.setVisibility(View.GONE);

//			holder.profile_pic.setBorderColor( getResources().getColor(R.color.white));


            convertview.setTag(holder);
        } else {
            holder = (ViewHolder) convertview.getTag();
        }

        holder.img_right_arrow.setVisibility(View.GONE);
        holder.tachername.setText(Date.get(pos).child_name);
        holder.subject.setText(Date.get(pos).school_name);
        ApplicationData.setProfileImg(myc, ApplicationData.web_server_url+ApplicationData.child_image_path+ Date.get(pos).child_image, holder.profile_pic);

       /* holder.lin_selectchild_badge.setVisibility(View.VISIBLE);
        if (Date.get(pos).badge == 0) {
            holder.lin_selectchild_badge.setVisibility(View.GONE);
        } else if(Date.get(pos).badge<100)
        {
            holder.txtBadge.setText(Date.get(pos).badge + "");
        }
        else
            holder.txtBadge.setText("N");*/

        return convertview;
    }
}
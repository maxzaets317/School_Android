package com.cloudstream.cslink.teacher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.adapter.teacher.CharacterListAdapter;
import com.adapter.teacher.Childbeans;
import com.adapter.teacher.ReportYearAdapter;
import com.cloudstream.cslink.R;
import com.common.utils.ConstantApi;
import com.common.utils.GlobalConstrants;
import com.langsetting.apps.Change_lang;
import com.request.AsyncTaskCompleteListener;
import com.request.ETechAsyncTask;
import com.xmpp.teacher.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by etech on 5/7/16.
 */
public class CharacterCardActivity extends Activity implements AsyncTaskCompleteListener<String> {

    String url, _parent_id, child_id;
    private ExpandableListView expand_report;
    private ListView lin_sem;
    ArrayList<String> array_yr = new ArrayList<String>();
    private int current_yr;
    private int startingyr = 1997;
    private Spinner spn_yr;
    private String selected_year = "", school_id, class_id;
    private ArrayList<Childbeans> semesterarray, subjectarray, characterarray, studentmarkarray;
    ArrayList<String> semester_name = new ArrayList<String>();
    private SemesterlistAdapter listadapter;
    private String semester_ids = "0", _teacher_id, _teacher_image;
    private CharacterListAdapter markadapter;
    private Change_lang change_lang;
    private Childbeans data;
    private TextView txt_title;
    private ImageView imgback;
    private String[] arraysemid;
    private ImageView img_dropdown;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.adres_report_card);

        SharedPreferences sharedpref = getSharedPreferences(Constant.USER_FILENAME, 0);
        change_lang = new Change_lang(getApplicationContext());
        _teacher_id = sharedpref.getString("teacher_id", "");
        _teacher_image = sharedpref.getString("image", "");
        school_id = sharedpref.getString("school_id", "");

        if (getIntent().hasExtra("child_detail")) {
            data = (Childbeans) getIntent().getSerializableExtra("child_detail");
            class_id = (data.class_id != null && data.class_id.length() > 0) ? data.class_id : "";
            child_id = (data.sender_id != null && data.sender_id.length() > 0) ? data.sender_id : "";
        }

        lin_sem = (ListView) findViewById(R.id.lin_sem);
        //   expand_report = (ExpandableListView) findViewById(R.id.expand_report);
        spn_yr = (Spinner) findViewById(R.id.spn_yr);
        txt_title = (TextView) findViewById(R.id.textView1);
        imgback = (ImageView) findViewById(R.id.imgback);
        img_dropdown = (ImageView) findViewById(R.id.img_dropdown);
        //setTitle
        txt_title.setTextColor(getResources().getColor(R.color.color_blue_p));
        //setTitle
        txt_title.setText(getString(R.string.char_heading));
        //get current year

        Date date = new Date();
        int year = date.getYear();
        Date dateInit = new Date(year, 7, 15);
        if (dateInit.before(date))
            current_yr = Calendar.getInstance().get(Calendar.YEAR) + 1;
        else
            current_yr = Calendar.getInstance().get(Calendar.YEAR);
        //create year array list
        //  array_yr.add(getString(R.string.select_yr));

        if (current_yr == 1997)
            array_yr.add("1997-1997");
        else {
            for (int i = 0; i < (current_yr - 1997); i++) {
                array_yr.add(String.valueOf(startingyr) + "-" + String.valueOf(startingyr + 1));
                startingyr++;
            }
            Collections.reverse(array_yr);
        }

        //back from activity
        imgback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ReportYearAdapter adapter = new ReportYearAdapter(CharacterCardActivity.this, array_yr);
        spn_yr.setAdapter(adapter);
        spn_yr.setSelection(0);
        selected_year = array_yr.get(0);


        spn_yr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_year = array_yr.get(position);

                if (!selected_year.equalsIgnoreCase(getString(R.string.select_yr)) && !semester_ids.equalsIgnoreCase("0")) {
                    callapi();
                    img_dropdown.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //get semester list
        callsemesterapi();

    }


    private void callsemesterapi() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("class_id", class_id);
        map.put("school_id", school_id);
        map.put("user_id", child_id);
        if (!GlobalConstrants.isWifiConnected(CharacterCardActivity.this)) {
            return;
        } else {
            ETechAsyncTask task = new ETechAsyncTask(CharacterCardActivity.this, this, ConstantApi.GET_SUBJECT_SEMESTER, map);
            task.execute(ApplicationData.main_url + ConstantApi.GET_SUBJECT_SEMESTER + ".php?");
        }
    }

    @Override
    public void onTaskComplete(int statusCode, String result, String webserviceCb, Object tag) {
        try {
            if (statusCode == ETechAsyncTask.COMPLETED) {
                JSONObject jObject = new JSONObject(result);

                try {
                    if (webserviceCb.equalsIgnoreCase(ConstantApi.GET_SUBJECT_SEMESTER)) {
                        String flag = jObject.getString("flag");

                        if (flag.equalsIgnoreCase("1")) {
                            if (semesterarray != null) semesterarray.clear();
                            if (subjectarray != null) subjectarray.clear();
                            if (characterarray != null) characterarray.clear();
                            if (studentmarkarray != null) studentmarkarray.clear();

                            semesterarray = new ArrayList<Childbeans>();
                            subjectarray = new ArrayList<Childbeans>();
                            characterarray = new ArrayList<Childbeans>();


                            //JsonParsing of Semesterarray
                            if (jObject.has("semester_list")) {

                                JSONArray jseme = jObject.getJSONArray("semester_list");
                                for (int sem = 0; sem < jseme.length(); sem++) {
                                    JSONObject jobsem = jseme.getJSONObject(sem);
                                    Childbeans bean = new Childbeans();
                                    if (jobsem.has("semester_id"))
                                        bean.semester_id = jobsem.getString("semester_id");
                                    if (jobsem.has("semester_name"))
                                        bean.semester_name = jobsem.getString("semester_name");

                                    semesterarray.add(bean);
                                }
                            }

                            //JsonParsing of subject_list
                            if (jObject.has("subject_list")) {

                                JSONArray jsub = jObject.getJSONArray("subject_list");
                                for (int sub = 0; sub < jsub.length(); sub++) {
                                    JSONObject jobsub = jsub.getJSONObject(sub);
                                    Childbeans bean = new Childbeans();
                                    if (jobsub.has("subject_id"))
                                        bean.subject_id = jobsub.getString("subject_id");
                                    if (jobsub.has("subject_name"))
                                        bean.subject_name = jobsub.getString("subject_name");
                                    if (jobsub.has("class_id"))
                                        bean.class_id = jobsub.getString("class_id");

                                    subjectarray.add(bean);
                                }
                            }

                            //JsonParsing of character_list
                            if (jObject.has("character_list")) {

                                JSONArray jchar = jObject.getJSONArray("character_list");
                                for (int cha = 0; cha < jchar.length(); cha++) {
                                    JSONObject jobchar = jchar.getJSONObject(cha);
                                    Childbeans bean = new Childbeans();
                                    if (jobchar.has("character_id"))
                                        bean.character_id = jobchar.getString("character_id");
                                    if (jobchar.has("character_name"))
                                        bean.character_name = jobchar.getString("character_name");
                                    if (jobchar.has("school_id"))
                                        bean.school_id = jobchar.getString("school_id");

                                    characterarray.add(bean);
                                }
                            }
                            setadapter();
                        } else {
                            if (jObject.has("msg")) {
                                String msg = jObject.getString("msg");
                                ApplicationData.showToast(CharacterCardActivity.this, msg, false);
                            } else
                                ApplicationData.showToast(CharacterCardActivity.this, R.string.no_record, false);
                        }

                    } else if (webserviceCb.equalsIgnoreCase(ConstantApi.GET_CHARACTER_PARENT)) {
                        String flag = jObject.getString("flag");

                        if (flag.equalsIgnoreCase("1")) {
                            semester_name = new ArrayList<String>();
                            studentmarkarray = new ArrayList<Childbeans>();

                            if (characterarray != null) characterarray.clear();
                            characterarray = new ArrayList<Childbeans>();

                            if (jObject.has("school_character")) {
                                JSONArray jmark = jObject.getJSONArray("school_character");


                                for (int i = 0; i < jmark.length(); i++) {
                                    JSONObject jobchar = jmark.getJSONObject(i);
                                    Childbeans bean = new Childbeans();
                                    if (jobchar.has("character_id"))
                                        bean.character_id = jobchar.getString("character_id");
                                    if (jobchar.has("character_name"))
                                        bean.character_name = jobchar.getString("character_name");
                                    if (jobchar.has("school_id"))
                                        bean.school_id = jobchar.getString("school_id");

                                    characterarray.add(bean);

                                }
                            }
                            arraysemid = semester_ids.split(",");
                            if (jObject.has("character_details")) {
                                JSONArray jmark = jObject.getJSONArray("character_details");
                                if (arraysemid != null && arraysemid.length > 0) {
                                    if (jmark.length() == arraysemid.length) {
                                        if (jmark.length() > 0) {
                                            for (int i = 0; i < jmark.length(); i++) {

                                                JSONObject jobmark = jmark.getJSONObject(i);

                                                Childbeans bean = new Childbeans();
                                                if (jobmark.has("user_id"))
                                                    bean.user_id = jobmark.getString("user_id");
                                                if (jobmark.has("year"))
                                                    bean.year = jobmark.getString("year");
                                                if (jobmark.has("semester_id"))
                                                    bean.semester_id = jobmark.getString("semester_id");
                                                if (jobmark.has("semester_name"))
                                                    bean.semester_name = jobmark.getString("semester_name");
                                                if (jobmark.has("class_id"))
                                                    bean.class_id = jobmark.getString("class_id");
                                                if (jobmark.has("character_id"))
                                                    bean.character_id = jobmark.getString("character_id");
                                                if (jobmark.has("comment"))
                                                    bean.comment = jobmark.getString("comment");
                                                if (jobmark.has("created_at"))
                                                    bean.created_at = jobmark.getString("created_at");
                                                if (jobmark.has("character_name"))
                                                    bean.character_name = jobmark.getString("character_name");

                                                semester_name.add(bean.semester_name);
                                                studentmarkarray.add(bean);

                                            }
                                        }

                                    } else {
                                        if (jmark.length() > 0) {
                                            for (int semid = 0; semid < arraysemid.length; semid++) {

                                                for (int i = 0; i < jmark.length(); i++) {
                                                    boolean is_resultavailable = false;
                                                    if (jmark.getJSONObject(i).getString("semester_id").equalsIgnoreCase(arraysemid[semid])) {
                                                        JSONObject jobmark = jmark.getJSONObject(i);
                                                        Childbeans bean = new Childbeans();
                                                        if (jobmark.has("user_id"))
                                                            bean.user_id = jobmark.getString("user_id");
                                                        if (jobmark.has("year"))
                                                            bean.year = jobmark.getString("year");
                                                        if (jobmark.has("semester_id"))
                                                            bean.semester_id = jobmark.getString("semester_id");
                                                        if (jobmark.has("semester_name"))
                                                            bean.semester_name = jobmark.getString("semester_name");
                                                        if (jobmark.has("class_id"))
                                                            bean.class_id = jobmark.getString("class_id");
                                                        if (jobmark.has("character_id"))
                                                            bean.character_id = jobmark.getString("character_id");
                                                        if (jobmark.has("comment"))
                                                            bean.comment = jobmark.getString("comment");
                                                        if (jobmark.has("created_at"))
                                                            bean.created_at = jobmark.getString("created_at");
                                                        if (jobmark.has("character_name"))
                                                            bean.character_name = jobmark.getString("character_name");

                                                        semester_name.add(bean.semester_name);
                                                        studentmarkarray.add(bean);
                                                        is_resultavailable = true;
                                                    }
                                                    if (!is_resultavailable) {
                                                        for (int val = 0; val < semesterarray.size(); val++) {
                                                            if (arraysemid[semid].equalsIgnoreCase(semesterarray.get(val).semester_id)) {
                                                                semester_name.add(semesterarray.get(val).semester_name);

                                                                Childbeans bean = new Childbeans();
                                                                bean.semester_name = semesterarray.get(val).semester_name;
                                                                studentmarkarray.add(bean);
                                                            }
                                                        }
                                                    }
                                                }

                                            }
                                        } else {
                                            for (int semid = 0; semid < arraysemid.length; semid++) {
                                                for (int val = 0; val < semesterarray.size(); val++) {
                                                    if (arraysemid[semid].equalsIgnoreCase(semesterarray.get(val).semester_id)) {
                                                        semester_name.add(semesterarray.get(val).semester_name);

                                                        Childbeans bean = new Childbeans();
                                                        bean.semester_name = semesterarray.get(val).semester_name;
                                                        studentmarkarray.add(bean);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (studentmarkarray != null && studentmarkarray.size() > 0) {
                                    expand_report.setVisibility(View.VISIBLE);
                                    markadapter = new CharacterListAdapter(CharacterCardActivity.this, studentmarkarray, semester_name, expand_report, characterarray);
                                    expand_report.setAdapter(markadapter);
                                    ApplicationData.setexpandListViewHeightBasedOnChildren(expand_report);
                                }

                            } else {
                                expand_report.setVisibility(View.GONE);
                                if (jObject.has("msg")) {
                                    String msg = jObject.getString("msg");
                                    ApplicationData.showToast(CharacterCardActivity.this, msg, false);
                                } else
                                    ApplicationData.showToast(CharacterCardActivity.this, R.string.no_record, false);
                            }

                        } else {
                            ApplicationData.showToast(CharacterCardActivity.this, R.string.server_error, false);
                        }
                    }
                    //setAdapter();
                } catch (Exception e) {
                    Log.e("CharacterCardActivity", "onTaskComplete() " + e, e);
                }

            } else if (statusCode == ETechAsyncTask.ERROR_NETWORK) {
                try {
                    ApplicationData.showToast(CharacterCardActivity.this, R.string.msg_operation_error, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setadapter() {

        listadapter = new SemesterlistAdapter(CharacterCardActivity.this, semesterarray, expand_report);
        lin_sem.setAdapter(listadapter);
        ApplicationData.setListViewHeightBasedOnChildren(lin_sem);
    }

    private void callapi() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("user_id", child_id);
        map.put("year", selected_year);
        map.put("semester_ids", semester_ids);
        map.put("class_id", class_id);
        map.put("school_id", school_id);
        if (!GlobalConstrants.isWifiConnected(CharacterCardActivity.this)) {
            return;
        } else {
            ETechAsyncTask task = new ETechAsyncTask(CharacterCardActivity.this, this, ConstantApi.GET_CHARACTER_PARENT, map);
            task.execute(ApplicationData.main_url + ConstantApi.GET_CHARACTER_PARENT + ".php?");
        }
    }

    public class SemesterlistAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<Boolean> flag = new ArrayList<Boolean>();
        private ArrayList<Childbeans> list = new ArrayList<Childbeans>();
        private ExpandableListView expand_list;

        public SemesterlistAdapter(Context context, ArrayList<Childbeans> list, ExpandableListView expand_list) {
            this.context = context;
            this.list = list;
            this.expand_list = expand_list;

            for (int i = 0; i < list.size(); i++) {
                flag.add(false);
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_semester, parent, false);
                holder = new ViewHolder();
                holder.rel_sem = (RelativeLayout) convertView.findViewById(R.id.rel_sem);
                holder.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
                holder.chk_item = (CheckBox) convertView.findViewById(R.id.chk_item);
                holder.view_line = (View) convertView.findViewById(R.id.view_line);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.txt_name.setText(list.get(position).semester_name);

            if (position == list.size() - 1)
                holder.view_line.setVisibility(View.GONE);
            else
                holder.view_line.setVisibility(View.VISIBLE);

            holder.rel_sem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) v.findViewById(R.id.chk_item);

                    if (checkBox.isChecked()) {
                        flag.set(position, false);
                        checkBox.setChecked(false);

                        if (semester_name != null && semester_name.size() > 0) {
                            for (int loop = 0; loop < semester_name.size(); loop++) {
                                if (semester_name.get(loop).equalsIgnoreCase(list.get(position).semester_name)) {
                                    for (int mk = 0; mk < studentmarkarray.size(); mk++) {
                                        if (studentmarkarray.get(mk).semester_name.equalsIgnoreCase(semester_name.get(loop))) {
                                            studentmarkarray.remove(mk);
                                        }
                                    }
                                    semester_name.remove(loop);
                                    break;
                                }
                            }

                            //add semester id which is selected
                            semester_ids = "0";
                            for (int i = 0; i < flag.size(); i++) {
                                if (flag.get(i)) {
                                    if (semester_ids.equalsIgnoreCase("0"))
                                        semester_ids = list.get(i).semester_id;
                                    else
                                        semester_ids = semester_ids + "," + list.get(i).semester_id;
                                }
                            }

                            markadapter.updatenotify(context, studentmarkarray, semester_name, expand_report);
                            markadapter.notifyDataSetChanged();
                            ApplicationData.setexpandListViewHeightBasedOnChildren(expand_report);

                        }
                    } else {

                        boolean isselected = false;
                        if (!selected_year.equalsIgnoreCase(getString(R.string.select_yr))) {
                            flag.set(position, true);
                            checkBox.setChecked(true);
                            semester_ids = "0";
                            for (int i = 0; i < flag.size(); i++) {
                                if (flag.get(i)) {
                                    if (semester_ids.equalsIgnoreCase("0"))
                                        semester_ids = list.get(i).semester_id;
                                    else
                                        semester_ids = semester_ids + "," + list.get(i).semester_id;

                                    isselected = true;
                                }
                            }

                            if (isselected) {
                                callapi();
                            }
                        } else {

                            ApplicationData.showToast(CharacterCardActivity.this, R.string.error_yr, false);
                        }
                    }
                }
            });

            holder.chk_item.setChecked(flag.get(position));

            return convertView;
        }

        class ViewHolder {

            public RelativeLayout rel_sem;
            public TextView txt_name;
            public CheckBox chk_item;
            public View view_line;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        url = null;
        _parent_id = null;
        child_id = null;
        expand_report = null;
        lin_sem = null;
        array_yr = null;
        spn_yr = null;
        selected_year = null;
        school_id = null;
        class_id = null;
        semesterarray = null;
        subjectarray = null;
        characterarray = null;
        studentmarkarray = null;
        semester_name = null;
        listadapter = null;
        semester_ids = null;
        _teacher_id = null;
        _teacher_image = null;
        markadapter = null;
        change_lang = null;
        data = null;
        txt_title = null;
        imgback = null;
        arraysemid = null;
        img_dropdown = null;

        System.gc();
    }
}

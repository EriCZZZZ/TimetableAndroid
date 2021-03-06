package cn.ericweb.timetable;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

//import cn.ericweb.timetable.domain.ClassTable;
//import cn.ericweb.timetable.domain.ClassTableAppAdditionalInfo;
//import cn.ericweb.timetable.domain.CourseAppAdditionalInfo;
//import cn.ericweb.timetable.domain.CourseInClassTable;
import cn.ericweb.timetable.domain.Activity;
import cn.ericweb.timetable.domain.Classtable;
import cn.ericweb.timetable.domain.Subject;
import cn.ericweb.timetable.ericandroid.EricRoundedCornerTextview;
import cn.ericweb.timetable.utils.AppConstant;


/**
 * A simple {@link Fragment} subclass.
 */
public class ClasstableFragment extends Fragment {

    public static final String WEEK_TO_SHOW = "cn.ericweb.ClasstableFragment.week2show";
    public static final String IF_SHOW_WEEKENDS = "cn.ericweb.ClasstableFragment.ifShowWeekends";
    public static final String CLASSTABLE_JSON = "cn.ericweb.ClasstableFragment.classtableJson";
    public static final String CLASSTABLE_ADDITIONAL_JSON = "cn.ericweb.ClasstableFragment.classtableAdditionalJson";
    public static final String CONTAINER_WIDTH = "cn.ericweb.ClasstableFragment.containerWidth";
    public static final String CONTAINER_HEIGHT = "cn.ericweb.ClasstableFragment.containerHeight";
    public static final String FIRST_WEEK_DATE_STRING = "cn.ericweb.ClasstableFragment.firstWeekDateString";

    private Bundle bundle;
    private ViewGroup container;

    public ClasstableFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            savedInstanceState = getArguments();

            this.bundle = savedInstanceState;
            this.container = container;

            final int week2show = savedInstanceState.getInt(WEEK_TO_SHOW);
            // get课程表
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-ddHH:mm:ss").create();
            Classtable classTable = gson.fromJson(savedInstanceState.getString(CLASSTABLE_JSON), Classtable.class);

            // 获得课程表容器并清空
            CoordinatorLayout cl = (CoordinatorLayout) inflater.inflate(R.layout.fragment_classtable, null);

            // init fab
            FloatingActionButton fab = (FloatingActionButton) cl.findViewById(R.id.fragment_classtable_fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), AddActActivity.class);
                    startActivity(intent);
                }
            });
            FloatingActionButton fab_next = (FloatingActionButton) cl.findViewById(R.id.fragment_classtable_fab_next_week);
            fab_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity pActivity = (MainActivity) getActivity();
                    pActivity.doAction(MainActivity.ACTION_NEXT_WEEK);
                }
            });
            FloatingActionButton fab_pre = (FloatingActionButton) cl.findViewById(R.id.fragment_classtable_fab_pre_week);
            fab_pre.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity pActivity = (MainActivity) getActivity();
                    if(week2show - 1 > 0) {
                        pActivity.doAction(MainActivity.ACTION_PRE_WEEK);
                    } else {
                        final FloatingActionButton _v = (FloatingActionButton) v;
                        ObjectAnimator alert = ObjectAnimator.ofArgb(_v, "null", getResources().getColor(R.color.colorBorderGrey), Color.RED, getResources().getColor(R.color.colorBorderGrey));
                        alert.setDuration(300);
                        alert.start();
                        alert.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                Integer _c = (Integer) animation.getAnimatedValue();
                                _v.setBackgroundTintList(ColorStateList.valueOf(_c));
                            }
                        });
                    }
                }
            });
            LinearLayout classTableContainer = (LinearLayout) cl.findViewById(R.id.fragment_classtable_container);

            // 获得显示几天一周
            int dayToShow = savedInstanceState.getBoolean(IF_SHOW_WEEKENDS) ? 7 : 5;
            // 获得尺寸数据
            // 宽度
            int containerWidth = savedInstanceState.getInt(CONTAINER_WIDTH);
            int perClassWidth = containerWidth / (1 + dayToShow);
            // 高度
            int containerHeight = savedInstanceState.getInt(CONTAINER_HEIGHT);
            int perClassHeight = containerHeight / (classTable.getNumberOfClassPerDay() + 1);

            // 添加周几
            LinearLayout weekdayBar = new LinearLayout(getContext());
            weekdayBar.setOrientation(LinearLayout.HORIZONTAL);
            // 添加周几前的周数
            FrameLayout blank = new FrameLayout(getContext());
            blank.setBackground(getContext().getDrawable(R.drawable.classtable_class_background));
            blank.setLayoutParams(new LinearLayout.LayoutParams(perClassWidth, perClassHeight, 0));
            TextView indexOfWeek = new TextView(getContext());
            indexOfWeek.setGravity(Gravity.CENTER);

            // 计算时间
            Date now = new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyyMMdd");
            Date startDate;
            try {
                startDate = yyyymmdd.parse(savedInstanceState.getString(FIRST_WEEK_DATE_STRING));
            } catch (ParseException e) {
                e.printStackTrace();
                startDate = now;
            }

            Calendar tempCalendar = Calendar.getInstance();
            tempCalendar.setTime(startDate);
            // 这里-1是因为保存的日期是第一周的
            tempCalendar.add(Calendar.DATE, (week2show - 1) * 7);
            int month = tempCalendar.get(Calendar.MONTH) + 1;
            indexOfWeek.setText("W" + week2show + "\nM" + month);

            blank.addView(indexOfWeek);
            weekdayBar.addView(blank);

            for (int i = 0; i < dayToShow; i++) {
                FrameLayout weekDayFrameLayout = new FrameLayout(getContext());
                weekDayFrameLayout.setBackground(getContext().getDrawable(R.drawable.classtable_class_background));
                weekDayFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(perClassWidth, perClassHeight, 0));
                TextView weekDayTextView = new TextView(getContext());
                weekDayTextView.setGravity(Gravity.CENTER);
                int temp = i + 1;
                Calendar tempDateCalendar = Calendar.getInstance();
                tempDateCalendar.setTime(tempCalendar.getTime());
                tempDateCalendar.add(Calendar.DATE, i);

                weekDayTextView.setText(temp + "" + "\nD" + tempDateCalendar.get(Calendar.DAY_OF_MONTH));
                Calendar nowCalendar = Calendar.getInstance();
                if (nowCalendar.get(Calendar.DAY_OF_YEAR) == tempDateCalendar.get(Calendar.DAY_OF_YEAR)) {
                    weekDayTextView.setTextColor(getResources().getColor(R.color.colorAccent));
                }
                weekDayFrameLayout.addView(weekDayTextView);
                weekdayBar.addView(weekDayFrameLayout);
            }
            classTableContainer.addView(weekdayBar);

            // 添加课程表
            LinearLayout classTableRow = new LinearLayout(getContext());
            classTableRow.setOrientation(LinearLayout.HORIZONTAL);

            // 添加课程index
            LinearLayout classIndexContainer = new LinearLayout(getContext());
            classIndexContainer.setOrientation(LinearLayout.VERTICAL);

            // 时间的计时器
            int hour = classTable.getClassStartTime().getHour();
            int minute = classTable.getClassStartTime().getMin();
            ArrayList<Integer> classIntervalArrayList = classTable.getIntervals();
            for (int classIndex = 1; classIndex <= classTable.getNumberOfClassPerDay(); classIndex++) {
                FrameLayout frameLayout = new FrameLayout(getContext());
                frameLayout.setBackground(getContext().getDrawable(R.drawable.classtable_class_background));
                frameLayout.setLayoutParams(new LinearLayout.LayoutParams(perClassWidth, perClassHeight, 0));
                TextView classIndexText = new TextView(getContext());
                classIndexText.setGravity(Gravity.CENTER);
                classIndexText.setText(hour + ":" + minute + (minute == 0 ? "0" : "") + "\n" + classIndex);

                // 更新时间
                // TODO: 16-9-14 修改ClassTable 添加一个没节课的时间，修改掉下面的45（UESTC）
                try {
                    minute += classIntervalArrayList.get(classIndex - 1) + 45;
                    int minuteRemainder = minute % 60;
                    hour += minute / 60;
                    minute = minuteRemainder;
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                frameLayout.addView(classIndexText);
                classIndexContainer.addView(frameLayout);
            }
            classTableRow.addView(classIndexContainer);

            RelativeLayout classContainerRL = new RelativeLayout(getContext());
            classContainerRL.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            classTableRow.addView(classContainerRL);
            for(Activity claxx : classTable.getActivities()) {
                // 判断本周是否存在这节课
                if(claxx.getExistedWeek().charAt(week2show) == '1') {
                    int _weekday = claxx.getWhichWeekday();
                    int _indexStart = claxx.getStartClassIndex();
                    int _indexEnd = claxx.getEndClassIndex();
                    int _howLong = _indexEnd - _indexStart + 1;

                    if(claxx.isClass()) {
                        RelativeLayout classContainer = new RelativeLayout(getContext());
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(perClassWidth, perClassHeight * _howLong);
                        lp.setMargins(perClassWidth * (_weekday), perClassHeight * (_indexStart), 0, 0);
                        classContainer.setLayoutParams(lp);
                        EricRoundedCornerTextview classTextview = new EricRoundedCornerTextview(getContext());
                        classTextview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        classTextview.setBorderWidth(1);
                        classTextview.setTextSize(getResources().getInteger(R.integer.classtable_font_size));
                        classTextview.setGravity(Gravity.CENTER);
                        classTextview.setText(claxx.getShowingString());
                        classTextview.setWeekday(_weekday);
                        classTextview.setStartIndex(_indexStart);

                        GradientDrawable classBackgroundDrawable = (GradientDrawable) getContext().getDrawable(R.drawable.classtable_class_background_radius_round_coner);
                        if (classBackgroundDrawable != null) {
                            classBackgroundDrawable.setColor(getResources().getColor(R.color.colorClassBackground));
                            if(claxx.getColorBg() != null) {
                                cn.ericweb.timetable.domain.Color _c = claxx.getColorBg();
                                classBackgroundDrawable.setColor(Color.argb(_c.getA(), _c.getR(), _c.getG(), _c.getB()));
                            }
                        }
                        classTextview.setBackground(classBackgroundDrawable);

                        // 添加磁铁的点击功能
                        classTextview.setOnClickListener(classInfoListener);

                        classContainer.addView(classTextview);

                        classContainerRL.addView(classContainer);
                    } else {
                        // 自定义活动的展示
                    }

                }
            }
            classTableContainer.addView(classTableRow);

            return cl;
        } catch (Exception e) {
            return null;
        }
    }

    View.OnClickListener classInfoListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final EricRoundedCornerTextview textView = (EricRoundedCornerTextview) view;

            AlertDialog.Builder classInfoDialogBuilder = new AlertDialog.Builder(textView.getContext());

            // get课程表
            final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-ddHH:mm:ss").create();
            SharedPreferences tempSharedPref = view.getContext().getSharedPreferences(AppConstant.SHARED_PREF_CLASSTABLE, getContext().MODE_PRIVATE);
            String classtableJson = tempSharedPref.getString(AppConstant.CLASSTABLE_KEY_MAIN, "");
            Classtable classtable;
            Activity _activity = null;
            try {
                classtable = gson.fromJson(classtableJson, Classtable.class);
            } catch (Exception e) {
                return;
            }

            for(Activity _s : classtable.getActivities()) {
                if(_s.getShowingString().equals(textView.getText()) && _s.getWhichWeekday() == textView.getWeekday() && _s.getStartClassIndex() == textView.getStartIndex()) {
                    _activity = _s;
                    break;
                }
            }
            final Activity activity = _activity;
            if(activity == null) {
                return;
            }

            // listener
            DialogInterface.OnClickListener reviseAdditionalInfoListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(textView.getContext(), ReviseClassAdditionalInfo.class);
                    intent.putExtra(ReviseClassAdditionalInfo.TARGET_SUBJECT, gson.toJson(activity.getSubject()));
                    intent.putExtra(ReviseClassAdditionalInfo.TARGET_ACTIVITY, gson.toJson(activity));
                    startActivity(intent);
                }
            };

            // 加载布局文件
            View content = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_class_info, null);

            // title
            TextView classTitle = (TextView) content.findViewById(R.id.className);
            classTitle.setText(activity.getTitle());

            // subject
            TextView shortCourseName = (TextView) content.findViewById(R.id.subject);
            Subject _s = activity.getSubject();
            if(_s != null) {
                shortCourseName.setText(_s.getTitle());
            } else {
                shortCourseName.setText(getString(R.string.activity_editor_no_subject_content));
            }

            // address
            TextView address = (TextView) content.findViewById(R.id.classAddress);
            address.setText(activity.getLocation());

            // teacher name
            TextView teacherName = (TextView) content.findViewById(R.id.classTeacher);
            teacherName.setText(activity.getSubject().getTeacher().getName());


            classInfoDialogBuilder.setView(content);
            // 开启修改按钮
            classInfoDialogBuilder.setPositiveButton(getString(R.string.dialog_class_info_revise_button), reviseAdditionalInfoListener);
            AlertDialog classInfoDialog = classInfoDialogBuilder.create();
            classInfoDialog.setCanceledOnTouchOutside(true);

            classInfoDialog.show();
        }
    };
}

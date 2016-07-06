package com.odoo.calendar.widget;

import android.animation.LayoutTransition;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.calendar.R;
import com.odoo.calendar.listeners.CalendarDateChangeListener;
import com.odoo.calendar.listeners.CalendarWeekDayFilterListener;
import com.odoo.calendar.utils.DateInfo;
import com.odoo.calendar.utils.SysCalUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OdooCalendar extends ViewPager {

    public static final String TAG = OdooCalendar.class.getSimpleName();
    private Context mContext;
    private SysCalUtils calendar;
    private View currentWeekView;
    private int activeYear = -1;
    private int focusDay = -1;
    private View recentClicked = null;
    private CalendarWeekDayFilterListener mCalendarWeekDayFilterListener;
    private CalendarDateChangeListener mCalendarDateChangeListener;

    public OdooCalendar(Context context) {
        super(context);
        init(context);
    }

    public OdooCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        if (!isInEditMode()) {
            mContext = context;
            calendar = new SysCalUtils();
            activeYear = calendar.getYear();
            post(new Runnable() {
                @Override
                public void run() {
                    bindView();
                }
            });
        }
    }

    private void bindView() {
        OdooCalendarAdapter adapter = new OdooCalendarAdapter();
        setOffscreenPageLimit(calendar.getWeeksOfTheYear());
        setAdapter(adapter);
        addOnPageChangeListener(pageChangeListener);
        focusOnWeek(calendar.getWeekOfYear(activeYear));
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        // Setting current item to current week
        setCurrentItem(calendar.getWeekOfYear(activeYear));
    }

    private class OdooCalendarAdapter extends PagerAdapter {

        private List<View> views = new ArrayList<>();

        public OdooCalendarAdapter() {
            bindViews();
        }

        public void bindViews() {
            views.clear();
            int totalWeeks = 53;
            views.add(0, getFakeView(0));
            for (int i = 1; i <= totalWeeks; i++) {
                LinearLayout weekView = (LinearLayout) LayoutInflater.from(mContext)
                        .inflate(R.layout.calendar_week_view, null, false);
                bindWeekView(weekView, i);
                weekView.setLayoutTransition(new LayoutTransition());
                views.add(i, weekView);
            }
            views.add(totalWeeks + 1, getFakeView(totalWeeks + 1));
            notifyDataSetChanged();
        }

        private View getFakeView(int week) {
            LinearLayout fakeWeekView = (LinearLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.calendar_week_view, null, false);
            bindWeekView(fakeWeekView, week);
            fakeWeekView.setLayoutTransition(new LayoutTransition());
            return fakeWeekView;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = views.get(position);
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            int index = views.indexOf(object);
            if (index == -1)
                return POSITION_NONE;
            else
                return index;
        }

        public View getView(int position) {
            return views.get(position);
        }

    }

    private void bindWeekView(View view, int weekOfYear) {
        TextView monthName = (TextView) view.findViewById(R.id.monthName);
        monthName.setText(calendar.getMonthDisplayName(activeYear, weekOfYear));

        TextView monTitle, tueTitle, wedTitle, thuTitle, friTitle, satTitle, sunTitle;
        monTitle = (TextView) view.findViewById(R.id.monTitle);
        tueTitle = (TextView) view.findViewById(R.id.tueTitle);
        wedTitle = (TextView) view.findViewById(R.id.wedTitle);
        thuTitle = (TextView) view.findViewById(R.id.thuTitle);
        friTitle = (TextView) view.findViewById(R.id.friTitle);
        satTitle = (TextView) view.findViewById(R.id.satTitle);
        sunTitle = (TextView) view.findViewById(R.id.sunTitle);

        monTitle.setText(calendar.getDayDisplayName(1, weekOfYear));
        tueTitle.setText(calendar.getDayDisplayName(2, weekOfYear));
        wedTitle.setText(calendar.getDayDisplayName(3, weekOfYear));
        thuTitle.setText(calendar.getDayDisplayName(4, weekOfYear));
        friTitle.setText(calendar.getDayDisplayName(5, weekOfYear));
        satTitle.setText(calendar.getDayDisplayName(6, weekOfYear));
        sunTitle.setText(calendar.getDayDisplayName(7, weekOfYear));

        TextView monValue, tueValue, wedValue, thuValue, friValue, satValue, sunValue;
        monValue = (TextView) view.findViewById(R.id.monValue);
        tueValue = (TextView) view.findViewById(R.id.tueValue);
        wedValue = (TextView) view.findViewById(R.id.wedValue);
        thuValue = (TextView) view.findViewById(R.id.thuValue);
        friValue = (TextView) view.findViewById(R.id.friValue);
        satValue = (TextView) view.findViewById(R.id.satValue);
        sunValue = (TextView) view.findViewById(R.id.sunValue);

        monValue.setText(calendar.getDayDisplayValue(activeYear, 1, weekOfYear));
        tueValue.setText(calendar.getDayDisplayValue(activeYear, 2, weekOfYear));
        wedValue.setText(calendar.getDayDisplayValue(activeYear, 3, weekOfYear));
        thuValue.setText(calendar.getDayDisplayValue(activeYear, 4, weekOfYear));
        friValue.setText(calendar.getDayDisplayValue(activeYear, 5, weekOfYear));
        satValue.setText(calendar.getDayDisplayValue(activeYear, 6, weekOfYear));
        sunValue.setText(calendar.getDayDisplayValue(activeYear, 7, weekOfYear));
    }

    public void focusOnWeek(int weekOfYear) {
        OdooCalendarAdapter adapter = (OdooCalendarAdapter) getAdapter();
        currentWeekView = adapter.getView(weekOfYear);

        View[] days = new View[7];
        for (int i = 1; i <= 7; i++) {
            days[i - 1] = getDayView(i);
            days[i - 1].setTag(calendar.getDateInfo(activeYear, i, weekOfYear));
            days[i - 1].setOnClickListener(dayClick);
        }
        focusOnDay(calendar.getDayOfWeek());
    }

    private View getDayView(int day) {
        switch (day) {
            case 1:
                return currentWeekView.findViewById(R.id.dayMonday);
            case 2:
                return currentWeekView.findViewById(R.id.dayTuesday);
            case 3:
                return currentWeekView.findViewById(R.id.dayWednesday);
            case 4:
                return currentWeekView.findViewById(R.id.dayThursday);
            case 5:
                return currentWeekView.findViewById(R.id.dayFriday);
            case 6:
                return currentWeekView.findViewById(R.id.daySaturday);
            case 7:
                return currentWeekView.findViewById(R.id.daySunday);
        }
        return null;
    }

    private void focusOnDay(int day) {
        View view = (View) getDayView(day).getParent();
        View[] dayViews = {
                view.findViewById(R.id.monValue),
                view.findViewById(R.id.tueValue),
                view.findViewById(R.id.wedValue),
                view.findViewById(R.id.thuValue),
                view.findViewById(R.id.friValue),
                view.findViewById(R.id.satValue),
                view.findViewById(R.id.sunValue)
        };

        for (int i = 0; i < dayViews.length; i++) {
            View dayView = dayViews[i];
            DateInfo dateInfo = (DateInfo) ((View) dayView.getParent()).getTag();
            if ((focusDay != -1 && focusDay == i) || (focusDay == -1 && dateInfo.isToday())) {
                // to focus
                dayView.setBackgroundResource(R.drawable.week_day_bg);
                if (recentClicked == null) {
                    dayClick.onClick((View) dayView.getParent());
                }
            } else {
                if (mCalendarWeekDayFilterListener != null) {
                    if (mCalendarWeekDayFilterListener.hasDataForDate(dateInfo)) {
                        dayView.setBackgroundResource(R.drawable.week_day_bg_data);
                    } else {
                        dayView.setBackgroundResource(R.drawable.week_day_bg_none);
                    }
                } else {
                    dayView.setBackgroundResource(R.drawable.week_day_bg_none);
                }
            }

        }
    }

    OnPageChangeListener pageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            OdooCalendarAdapter adapter = (OdooCalendarAdapter) getAdapter();
            int weekDay = calendar.getWeekDaysDiff(activeYear);
            int nextYearPosition = (weekDay % 7 == 0) ? adapter.getCount() - 1 : adapter.getCount() - 2;
            if (position == 0) {
                activeYear = activeYear - 1;
                adapter.bindViews();
                weekDay = calendar.getWeekDaysDiff(activeYear);
                nextYearPosition = (weekDay % 7 == 0) ? adapter.getCount() - 1 : adapter.getCount() - 2;
                setCurrentItem(nextYearPosition - 1, false);
                return;
            } else if (position == nextYearPosition) {
                activeYear = activeYear + 1;
                adapter.bindViews();
                setCurrentItem(1, false);
                return;
            }
            recentClicked = null;
            focusOnWeek(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    OnClickListener dayClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (recentClicked == view) {
                return;
            }
            recentClicked = view;
            DateInfo dateInfo = (DateInfo) view.getTag();
            focusDay = dateInfo.dayOfWeek - 1;
            focusOnDay(dateInfo.dayOfWeek);
            if (mCalendarDateChangeListener != null) {
                mCalendarDateChangeListener.onCalendarDateChange(dateInfo);
            }
        }
    };

    // Returns first day of the week (SUNDAY or MONDAY)
    public int getFirstDayOfWeek() {
        return Calendar.getInstance(Locale.getDefault()).getFirstDayOfWeek();
    }

    public void gotoToday() {
        activeYear = calendar.getYear();
        setCurrentItem(calendar.getWeekOfYear(activeYear) - 1, true);
        focusDay = -1;
        recentClicked = null;
        focusOnWeek(calendar.getWeekOfYear(activeYear));
    }

    public void setCalendarDateChangeListener(CalendarDateChangeListener callback) {
        mCalendarDateChangeListener = callback;
    }

    public void setCalendarWeekDayFilterListener(CalendarWeekDayFilterListener callback) {
        mCalendarWeekDayFilterListener = callback;
    }

}

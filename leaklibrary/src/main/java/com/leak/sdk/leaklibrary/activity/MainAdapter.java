package com.leak.sdk.leaklibrary.activity;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.ColorRes;

import com.leak.sdk.leaklibrary.R;
import com.leak.sdk.leaklibrary.heap.Exclusion;
import com.leak.sdk.leaklibrary.heap.LeakTrace;
import com.leak.sdk.leaklibrary.heap.LeakTraceElement;

import static com.leak.sdk.leaklibrary.heap.LeakTraceElement.Type.STATIC_FIELD;


public class MainAdapter extends BaseAdapter {
    private LeakTrace leakTrace;
    private LayoutInflater inflater;
    private String classNameColorHexString;
    private String leakColorHexString;
    private String referenceColorHexString;
    private Context context;
    private String extraColorHexString;
    private String referenceName ;


    // https://stackoverflow.com/a/6540378/703646
    private static String hexStringColor(Resources resources, @ColorRes int colorResId) {
        return String.format("#%06X", (0xFFFFFF & resources.getColor(colorResId)));
    }

    MainAdapter(LeakTrace leaks, Context context, String referenceName) {
        this.leakTrace = leaks;
        this.context = context;
        this.referenceName = referenceName;
        this.inflater = LayoutInflater.from(context);
        classNameColorHexString = hexStringColor(context.getResources(), R.color.leak_canary_class_name);
        leakColorHexString = hexStringColor(context.getResources(), R.color.leak_canary_leak);
        referenceColorHexString = hexStringColor(context.getResources(), R.color.leak_canary_reference);
        extraColorHexString = hexStringColor(context.getResources(), R.color.leak_canary_extra);
    }

    void setData(LeakTrace leaks, String referenceName) {
        this.leakTrace = leaks;
        this.referenceName = referenceName;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return leakTrace.elements.size();
    }

    @Override
    public Object getItem(int position) {
        return leakTrace.elements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.layout_leak_item, null);
            viewHolder.name = view.findViewById(R.id.name);
            viewHolder.open = view.findViewById(R.id.open);
            viewHolder.detail = view.findViewById(R.id.detail);

            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        LeakTraceElement leakTraceElement = (LeakTraceElement) getItem(position);
        viewHolder.name.setText(htmlTitle(leakTraceElement, false, context.getResources()));
        viewHolder.detail.setText(htmlDetails(true, leakTraceElement));
        view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (viewHolder.detail.getVisibility() != View.VISIBLE) {
                                            viewHolder.detail.setVisibility(View.VISIBLE);
                                            viewHolder.open.setText("-");
                                        } else {
                                            viewHolder.detail.setVisibility(View.GONE);
                                            viewHolder.open.setText("+");
                                        }
                                    }
                                }
        );
        viewHolder.open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.detail.getVisibility() != View.VISIBLE) {
                    viewHolder.detail.setVisibility(View.VISIBLE);
                    viewHolder.open.setText("-");
                } else {
                    viewHolder.detail.setVisibility(View.GONE);
                    viewHolder.open.setText("+");
                }
            }
        });

        return view;
    }

    private Spanned htmlDetails(boolean isLeakingInstance, LeakTraceElement element) {
        String htmlString = "";
        if (element.extra != null) {
            htmlString += " <font color='" + extraColorHexString + "'>" + element.extra + "</font>";
        }

        Exclusion exclusion = element.exclusion;
        if (exclusion != null) {
            htmlString += "<br/><br/>Excluded by rule";
            if (exclusion.name != null) {
                htmlString += " <font color='#ffffff'>" + exclusion.name + "</font>";
            }
            htmlString += " matching <font color='#f3cf83'>" + exclusion.matching + "</font>";
            if (exclusion.reason != null) {
                htmlString += " because <font color='#f3cf83'>" + exclusion.reason + "</font>";
            }
        }
        htmlString += "<br>"
                + "<font color='" + extraColorHexString + "'>"
                + element.toDetailedString().replace("\n", "<br>")
                + "</font>";

        if (isLeakingInstance && !referenceName.equals("")) {
            htmlString += " <font color='" + extraColorHexString + "'>" + referenceName + "</font>";
        }
        return Html.fromHtml(htmlString);
    }

    private Spanned htmlTitle(LeakTraceElement element, boolean maybeLeakCause, Resources resources) {
        String htmlString = "";
        String simpleName = element.getSimpleClassName();
        simpleName = simpleName.replace("[]", "[ ]");

        String styledClassName =
                "<font color='" + classNameColorHexString + "'>" + simpleName + "</font>";

        if (element.reference != null) {
            String referenceName = element.reference.getDisplayName().replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;");

            if (maybeLeakCause) {
                referenceName =
                        "<u><font color='" + leakColorHexString + "'>" + referenceName + "</font></u>";
            } else {
                referenceName =
                        "<font color='" + referenceColorHexString + "'>" + referenceName + "</font>";
            }

            if (element.reference.type == STATIC_FIELD) {
                referenceName = "<i>" + referenceName + "</i>";
            }

            String classAndReference = styledClassName + "." + referenceName;

            if (maybeLeakCause) {
                classAndReference = "<b>" + classAndReference + "</b>";
            }

            htmlString += classAndReference;
        } else {
            htmlString += styledClassName;
        }

        Exclusion exclusion = element.exclusion;
        if (exclusion != null) {
            htmlString += " (excluded)";
        }
        SpannableStringBuilder builder = (SpannableStringBuilder) Html.fromHtml(htmlString);
        if (maybeLeakCause) {
            SquigglySpan.replaceUnderlineSpans(builder, resources);
        }

        return builder;
    }

    public class ViewHolder {
        TextView name;
        TextView detail;
        TextView open;
    }

}

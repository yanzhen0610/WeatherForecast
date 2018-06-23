package tw.edu.tku.csie.weatherforecast.ui;

import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tw.edu.tku.csie.weatherforecast.R;
import tw.edu.tku.csie.weatherforecast.databinding.ActivityDetailBinding;
import tw.edu.tku.csie.weatherforecast.transition.TransitionUtils;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class DetailSharedElementEnterCallback extends SharedElementCallback {

    private final Intent intent;
    private final Context context;

    private TransitionUtils.TextViewTransitionProperties highTempTargetProperties;
    private TransitionUtils.TextViewTransitionProperties lowTempTargetProperties;
    private TransitionUtils.TextViewTransitionProperties dateTargetProperties;
    private TransitionUtils.TextViewTransitionProperties descriptionTargetProperties;

    private ActivityDetailBinding detailBinding;

    public DetailSharedElementEnterCallback(Intent intent, Context context) {
        this.intent = intent;
        this.context = context;
    }

    @Override
    public void onSharedElementStart(List<String> sharedElementNames,
                                     List<View> sharedElements,
                                     List<View> sharedElementSnapshots) {

        final TextView highTemp = getHighTemp();
        final TextView lowTemp = getLowTemp();
        final TextView date = getDate();
        final TextView description = getDescription();

        highTempTargetProperties = new TransitionUtils.TextViewTransitionProperties(highTemp);
        lowTempTargetProperties = new TransitionUtils.TextViewTransitionProperties(lowTemp);
        dateTargetProperties = new TransitionUtils.TextViewTransitionProperties(date);
        descriptionTargetProperties = new TransitionUtils.TextViewTransitionProperties(description);

        TransitionUtils.getTextViewInfo(intent, context.getString(R.string.transition_name_date))
                .configTextViewWithData(date);
        TransitionUtils.getTextViewInfo(intent, context.getString(R.string.transition_name_description))
                .configTextViewWithData(description);
        TransitionUtils.getTextViewInfo(intent, context.getString(R.string.transition_name_high_temp))
                .configTextViewWithData(highTemp);
        TransitionUtils.getTextViewInfo(intent, context.getString(R.string.transition_name_low_temp))
                .configTextViewWithData(lowTemp);
    }

    @Override
    public void onSharedElementEnd(List<String> sharedElementNames,
                                   List<View> sharedElements,
                                   List<View> sharedElementSnapshots) {
        dateTargetProperties.configTextViewWithData(getDate());
        descriptionTargetProperties.configTextViewWithData(getDescription());
        highTempTargetProperties.configTextViewWithData(getHighTemp());
        lowTempTargetProperties.configTextViewWithData(getLowTemp());

        if (detailBinding != null) {
            forceSharedElementLayout(detailBinding.primaryInfo.primaryInfoLayout);
        }
    }

    @Override
    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        removeObsoleteElements(names, sharedElements, mapObsoleteElements(names));
        mapSharedElement(names, sharedElements, getHighTemp());
        mapSharedElement(names, sharedElements, getLowTemp());
        mapSharedElement(names, sharedElements, getDate());
        mapSharedElement(names, sharedElements, getDescription());
        mapSharedElement(names, sharedElements, getIcon());
    }

    private TextView getHighTemp() {
        if (detailBinding != null) {
            return detailBinding.primaryInfo.highTemperature;
        }
        throw new NullPointerException();
    }

    private TextView getLowTemp() {
        if (detailBinding != null) {
            return detailBinding.primaryInfo.lowTemperature;
        }
        throw new NullPointerException();
    }

    private TextView getDate() {
        if (detailBinding != null) {
            return detailBinding.primaryInfo.date;
        }
        throw new NullPointerException();
    }

    private TextView getDescription() {
        if (detailBinding != null) {
            return detailBinding.primaryInfo.weatherDescription;
        }
        throw new NullPointerException();
    }

    private ImageView getIcon() {
        if (detailBinding != null) {
            return detailBinding.primaryInfo.weatherIcon;
        }
        throw new NullPointerException();
    }

    public void setDetailBinding(ActivityDetailBinding binding) {
        this.detailBinding = binding;
    }

    /**
     * Maps all views that don't start with "android" namespace.
     *
     * @param names All shared element names.
     * @return The obsolete shared element names.
     */
    @NonNull
    private List<String> mapObsoleteElements(List<String> names) {
        List<String> elementsToRemove = new ArrayList<>(names.size());
        for (String name : names) {
            if (name.startsWith("android")) continue;
            elementsToRemove.add(name);
        }
        return elementsToRemove;
    }

    /**
     * Removes obsolete elements from names and shared elements.
     *
     * @param names Shared element names.
     * @param sharedElements Shared elements.
     * @param elementsToRemove The elements that should be removed.
     */
    private void removeObsoleteElements(List<String> names,
                                        Map<String, View> sharedElements,
                                        List<String> elementsToRemove) {
        if (elementsToRemove.size() > 0) {
            names.removeAll(elementsToRemove);
            for (String elementToRemove : elementsToRemove) {
                sharedElements.remove(elementToRemove);
            }
        }
    }

    /**
     * Puts a shared element to transitions and names.
     *
     * @param names The names for this transition.
     * @param sharedElements The elements for this transition.
     * @param view The view to add.
     */
    private void mapSharedElement(List<String> names, Map<String, View> sharedElements, View view) {
        String transitionName = view.getTransitionName();
        names.add(transitionName);
        sharedElements.put(transitionName, view);
    }

    private void forceSharedElementLayout(View view) {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(view.getWidth(),
                View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(view.getHeight(),
                View.MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }

}
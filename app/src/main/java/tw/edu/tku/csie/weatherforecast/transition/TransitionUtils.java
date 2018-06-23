package tw.edu.tku.csie.weatherforecast.transition;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.util.TypedValue;
import android.widget.TextView;

public class TransitionUtils {

    public static void addTextViewInfo(Intent intent, final String key, TextView textView) {
        final String fontSizeKey = key + "_font_size";
        final String fontColorKey = key + "_font_color";
        final String paddingKey = key + "_padding";
        intent.putExtra(fontSizeKey, textView.getTextSize());
        intent.putExtra(fontColorKey, textView.getTextColors());
        intent.putExtra(paddingKey,
                new Rect(textView.getPaddingLeft(),
                        textView.getPaddingTop(),
                        textView.getPaddingRight(),
                        textView.getPaddingBottom()));
    }

    public static TextViewTransitionProperties getTextViewInfo(Intent intent, final String key) {
        final String fontSizeKey = key + "_font_size";
        final String fontColorKey = key + "_font_color";
        final String paddingKey = key + "_padding";
        return new TextViewTransitionProperties(
                intent.getFloatExtra(fontSizeKey, 0),
                intent.getParcelableExtra(fontColorKey),
                intent.getParcelableExtra(paddingKey));
    }

    public static class TextViewTransitionProperties {
        public float fontSize;
        public ColorStateList fontColor;
        public Rect padding;

        public TextViewTransitionProperties(TextView textView) {
            this(textView.getTextSize(), textView.getTextColors(), new Rect(
                    textView.getPaddingLeft(),
                    textView.getPaddingTop(),
                    textView.getPaddingRight(),
                    textView.getPaddingBottom()
            ));
        }

        public TextViewTransitionProperties(float fontSize, ColorStateList fontColor, Rect padding) {
            this.fontSize = fontSize;
            this.fontColor = fontColor;
            this.padding = new Rect(padding);
        }

        public void configTextViewWithData(TextView textView) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            if (fontColor != null) {
                textView.setTextColor(fontColor);
            }
            if (padding != null) {
                textView.setPadding(padding.left, padding.top, padding.right, padding.bottom);
            }
        }
    }

}

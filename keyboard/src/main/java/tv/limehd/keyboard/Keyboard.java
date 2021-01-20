package tv.limehd.keyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Keyboard extends LinearLayout {

    private final String TAG = "Keyboard";

    // Передаваемые параметры
    private WindowManager windowManager;
    private KeyListener callback;
    private ViewGroup viewGroup;

    private boolean isKeyboardActive = false;

    private static final int SYMBOL_BUTTON = 0;
    private static final int LANGUAGE_BUTTON = 1;
    private static final int SPACE_BUTTON = 3;
    private static final int CLEAR_BUTTON = 4;
    private static final int HIDE_BUTTON = 5;
    private static final int SEARCH_BUTTON = 6;

    private final String[][] keyboard = new String[][] {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "(", ")"},
            {"Й", "Ц", "У", "К", "Е", "Н", "Г", "Ш", "Щ", "З", "Х", "Ъ"},
            {"Ф", "Ы", "В", "А", "П", "Р", "О", "Л", "Д", "Ж", "Э"},
            {"Я", "Ч", "С", "М", "И", "Т", "Ь", "Б", "Ю"},
            {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]"},
            {"A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'"},
            {"Z", "X", "C", "V", "B", "N", "M", "<", ">"}
    };

    private Button firstKey;

    // Параметры для установки размеров клавиатура
    private final int param = 12; // Ряд кнопок = 1/12 от высота экрана => клавиатура из 4х рядов занимает 1/3 экрана по высоте
    private final float sizeRation = 1.56F; // Высотка кнопки в портренной ориентации = ширине * sizeRation
    private int margin = 4; // Стандартный размер отступа между кнопками

    private final int spaceSize = 9; // Пробел по ширине как %spacesize% кнопок
    private View keyboardView;
    private int dpWidth, dpHeight;

    // Параметры клавиатуры
    private boolean numberLineEnabled;
    private boolean nightThemeEnabled;
    private boolean isRussian = true;
    private Context context;


    private Keyboard(Context context, WindowManager windowManager, KeyListener callback, ViewGroup viewGroup) {
        super(context);
        this.callback = callback;
        this.viewGroup = viewGroup;
        this.windowManager = windowManager;
        this.context = context;
        this.viewGroup = viewGroup;
    }

    public void showKeyboard() {
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float width = Float.parseFloat(String.valueOf(size.x));
        float height = Float.parseFloat(String.valueOf(size.y));
        margin = Math.round(width / 135) / 2;
        dpWidth = (int) Math.floor(width / param) - margin * 2;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        dpHeight = width > height ? (Math.round(height / param) - margin * 2) : Math.round(dpWidth * sizeRation) - margin * 2;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        int resId = numberLineEnabled ? R.layout.keyboard_view_nums : R.layout.keyboard_view;
        keyboardView = inflater.inflate(resId, null, false);

        viewGroup.addView(keyboardView);
        keyboardView.setOnTouchListener((v, event) -> true);
        keyboardView.setLayoutParams(params);

        KeyboardView keyboardView1 = new KeyboardView(context);
        viewGroup.addView(keyboardView1);
        keyboardView1.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getKeyboardHeight()));

        ArrayList<String[]> array = new ArrayList<>();
        array.add(keyboard[0]);
        if (isRussian) {
            for (int i = 1; i < 4; i++) {
                array.add(keyboard[i]);
            }
        } else {
            for (int i = 4; i < 7; i++) {
                array.add(keyboard[i]);
            }
        }
        addKeys(array);
        isKeyboardActive = true;
    }

    public void setNightThemeEnabled(boolean nightThemeEnabled) {
        this.nightThemeEnabled = nightThemeEnabled;
    }

    public boolean isKeyboardView(View view) {
        try {
            if (view instanceof KeyboardView || view.getId() == R.id.keyboard_view) {
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    private void addKeys(ArrayList<String[]> keyLines) {
        // Полотно для клавиатуры
        LayoutInflater inflater = LayoutInflater.from(getContext());
        LinearLayout host = keyboardView.findViewById(R.id.keyboard_view);
        if (nightThemeEnabled) {
            host.setBackgroundColor(getResources().getColor(R.color.black));
        }

        LinearLayout linearLayout;
        int startIndex = numberLineEnabled ? 0 : 1;
        int thirdLineLength = isRussian ? keyboard[3].length : keyboard[6].length;
        for (int i = startIndex; i < keyLines.size(); i++) {
            String[] array = keyLines.get(i);
            int pos = numberLineEnabled ? i : i - 1;
            linearLayout = keyboardView.findViewById(getLinearLayoutId(pos));

            if (array.length == thirdLineLength) { //
                View v = inflater.inflate(R.layout.keyboard_item, linearLayout, true);
                Button languageButton = v.findViewById(R.id.key_button);
                setupButton(languageButton, LANGUAGE_BUTTON,null);
            }

            for (int j = 0; j < array.length; j++) {
                View v = inflater.inflate(R.layout.keyboard_item, linearLayout, true);
                Button b = v.findViewById(R.id.key_button);
                Button symbolButton = setupButton(b, SYMBOL_BUTTON, array[j]);
                if (i == startIndex && j == 0) {
                    firstKey = symbolButton;
                }
            }

            if (array.length == thirdLineLength) { // Добавление кнопки <-
                View v = inflater.inflate(R.layout.keyboard_clear, linearLayout, true);
                ImageButton clearButton = v.findViewById(R.id.key_button);
                setupButton(clearButton, CLEAR_BUTTON);
            }
        }
        // Заполнение нижнего ряда клавиатуры
        linearLayout = keyboardView.findViewById(getLinearLayoutId(numberLineEnabled ? keyLines.size() : keyLines.size() - 1));

        // Кнопка для скрытия клавиатуры
        View v = inflater.inflate(R.layout.keyboard_hide, linearLayout, true);
        ImageButton hideButton = v.findViewById(R.id.key_button);
        setupButton(hideButton, HIDE_BUTTON);

        // Добавление пробела
        v = inflater.inflate(R.layout.keyboard_space, linearLayout, true);
        Button spaceButton = v.findViewById(R.id.key_button);
        setupButton(spaceButton, SPACE_BUTTON, null);

        // Лупа
        v = inflater.inflate(R.layout.keyboard_search, linearLayout, true);
        ImageButton searchButton = v.findViewById(R.id.key_button);
        setupButton(searchButton, SEARCH_BUTTON);
    }

    private Button setupButton(Button button, int type, String symbol) {
        switch (type) {
            case SYMBOL_BUTTON:
                button.setOnClickListener(v1 -> {
                    callback.onKeyClicked(symbol);
                });
                button.setText(symbol);
                setButtonSize(button, SYMBOL_BUTTON);
            break;
            case LANGUAGE_BUTTON:
                setButtonSize(button, LANGUAGE_BUTTON);
                if (isRussian) {
                    button.setText(R.string.ru_title);
                } else {
                    button.setText(R.string.eng_title);
                }
                button.setOnClickListener(click -> { // Смена языка
                    boolean focused = button.isFocused();
                    isRussian = !isRussian;
                    hideKeyboard();
                    showKeyboard();
                    if (focused) button.requestFocusFromTouch();
                }); // Смена языка
            break;
            case SPACE_BUTTON:
                button.setOnClickListener(click -> {
                    callback.onKeyClicked(" ");
                });
                setButtonSize(button, SPACE_BUTTON);
            break;
        }
        updateButtonTheme(button);
        button.setId(R.id.keyboard_view);
        return button;
    }

    private void setupButton(ImageButton button, int type) {
        switch (type) {
            case CLEAR_BUTTON:
                button.setOnClickListener(click -> {
                    callback.onDeleteButtonClicked();
                }); // Стереть 1 символ
                button.setOnLongClickListener(click -> {
                    callback.onLongDeleteButtonClicked();
                    return false;
                });
            break;
            case HIDE_BUTTON:
                updateButtonTheme(button);
                button.setOnClickListener(click -> {
                    callback.onKeyboardHideClicked();
                });
            break;
            case SEARCH_BUTTON:
                button.setOnClickListener(click -> {
                    callback.onKeyboardOkClicked();
                });
            break;
        }
        setButtonSize(button);
        button.setId(R.id.keyboard_view);
        updateButtonTheme(button);
    }

    private void updateButtonTheme(Button button) {
        if (nightThemeEnabled) {
            button.setBackground(getResources().getDrawable(R.drawable.night_action_button_style));
            button.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void updateButtonTheme(ImageButton imageButton) {
        if (nightThemeEnabled) {
            imageButton.setBackground(getResources().getDrawable(R.drawable.night_action_button_style));
            imageButton.setColorFilter(getResources().getColor(R.color.white));
        }
    }

    public void setFocusInTouchMode() { // Фокусировка на первую кнопку в клавиатуре
        firstKey.requestFocusFromTouch();
    }

    public int getKeyboardHeight() {
        int d = dpHeight + margin * 2;
        int lines = 4;
        if (numberLineEnabled) lines++;
        return d * lines;
    }

    private int getLinearLayoutId(int index) {
        switch (index) {
            case 0: return R.id.firstLine;
            case 1: return R.id.secondLine;
            case 2: return R.id.thirdLine;
            case 3: return R.id.fourthLine;
            case 4: return R.id.fifthLine;
            default: throw new IndexOutOfBoundsException();
        }
    }

    private void setButtonSize(Button button, int type) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.height = dpHeight;
        int width = dpWidth;
        switch (type) {
            case LANGUAGE_BUTTON:
                width = (int) Math.round(dpWidth * 1.5);
            break;
            case SPACE_BUTTON:
                width *= spaceSize;
            break;
        }
        params.width = width;
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.bottomMargin = margin;
        params.topMargin = margin;
        button.setLayoutParams(params);
    }

    private void setButtonSize(ImageButton button) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.height = dpHeight ;
        params.width = (int) Math.round(dpWidth * 1.5);
        params.leftMargin = margin;
        params.rightMargin = margin;
        params.bottomMargin = margin;
        params.topMargin = margin;
        button.setLayoutParams(params);
    }

    public void hideKeyboard() {
        if (keyboardView != null) {
            viewGroup.removeView(keyboardView);
        } else {
            Log.e(TAG, "Keyboard is null");
        }
        isKeyboardActive = false;
    }

    public interface KeyListener {
        void onKeyClicked(String key);
        void onDeleteButtonClicked();
        void onLongDeleteButtonClicked();
        void onKeyboardHideClicked();
        void onKeyboardOkClicked();
    }

    private void setNightMode(boolean status) {
        nightThemeEnabled = status;
    }

    private void setNumberLine(boolean status) {
        numberLineEnabled = status;
    }

    public static class Builder {

        private Context context;
        private WindowManager windowManager;
        private KeyListener callback;
        private ViewGroup viewGroup;

        private boolean nightMode = false;
        private boolean numberLine = false;

        public Builder(@NonNull Activity activity, @NonNull KeyListener callback, @NonNull FrameLayout frameLayout) {
            this.context = activity.getApplicationContext();
            this.windowManager = activity.getWindowManager();
            this.callback = callback;
            this.viewGroup = frameLayout;
        }

        public Builder setNightMode(boolean status) {
            nightMode = status;
            return this;
        }

        public Builder enableNumberLine(boolean status) {
            numberLine = status;
            return this;
        }

        public Keyboard build() {
            Keyboard keyboard = new Keyboard(context, windowManager, callback, viewGroup);
            keyboard.setNightMode(nightMode);
            keyboard.setNumberLine(numberLine);
            return keyboard;
        }
    }
}
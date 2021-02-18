package tv.limehd.keyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
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
import java.util.Arrays;
import java.util.List;

public class Keyboard extends LinearLayout {

    private final String TAG = "Keyboard";

    // Передаваемые параметры
    private final WindowManager windowManager;
    private final KeyListener callback;
    private ViewGroup viewGroup;

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

    //TODO: добавить пересчёт отступа между кнопками при смене языка
    /*private final String[][] keyboard = new String[][] {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"},
            {"Й", "Ц", "У", "К", "Е", "Н", "Г", "Ш", "Щ", "З", "Х", "Ъ"},
            {"Ф", "Ы", "В", "А", "П", "Р", "О", "Л", "Д", "Ж", "Э"},
            {"Я", "Ч", "С", "М", "И", "Т", "Ь", "Б", "Ю"},
            {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"},
            {"A", "S", "D", "F", "G", "H", "J", "K", "L"},
            {"Z", "X", "C", "V", "B", "N", "M"}
    };*/

    private Button[][] buttons;

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
    private final Context context;
    private List<List<View>> buttonsRows = new ArrayList<>();

    private boolean keyboardActive = false;
    private boolean isLanguageButtonFocused = false;


    private Keyboard(Context context, WindowManager windowManager, KeyListener callback, ViewGroup viewGroup) {
        super(context);
        this.callback = callback;
        this.viewGroup = viewGroup;
        this.windowManager = windowManager;
        this.context = context;
    }




    public void showKeyboard() {
        keyboardActive = true;
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
            array.addAll(Arrays.asList(keyboard).subList(1, 4));
        } else {
            array.addAll(Arrays.asList(keyboard).subList(4, 7));
        }
        addKeys(array);
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

            List<View> buttonsRow = new ArrayList<>();

            if (array.length == thirdLineLength) { //
                View v = inflater.inflate(R.layout.keyboard_item, linearLayout, true);
                Button languageButton = v.findViewById(R.id.key_button);
                buttonsRow.add(languageButton);
                setupButton(languageButton, LANGUAGE_BUTTON,null);
            }

            for (int j = 0; j < array.length; j++) {
                View v = inflater.inflate(R.layout.keyboard_item, linearLayout, true);
                Button b = v.findViewById(R.id.key_button);
                Button symbolButton = setupButton(b, SYMBOL_BUTTON, array[j]);
                buttonsRow.add(symbolButton);
                if (i == startIndex && j == 0) {
                    firstKey = symbolButton;
                    firstKey.setFocusable(true);
                    firstKey.setFocusableInTouchMode(true);
                }
            }
            buttonsRows.add(buttonsRow);

            if (array.length == thirdLineLength) { // Добавление кнопки <-
                View v = inflater.inflate(R.layout.keyboard_clear, linearLayout, true);
                ImageButton clearButton = v.findViewById(R.id.key_button);
                buttonsRow.add(clearButton);
                setupButton(clearButton, CLEAR_BUTTON);
            }
        }

        List<View> buttonRow = new ArrayList<>();

        // Заполнение нижнего ряда клавиатуры
        linearLayout = keyboardView.findViewById(getLinearLayoutId(numberLineEnabled ? keyLines.size() : keyLines.size() - 1));

        // Кнопка для скрытия клавиатуры
        View v = inflater.inflate(R.layout.keyboard_hide, linearLayout, true);
        ImageButton hideButton = v.findViewById(R.id.key_button);
        buttonRow.add(hideButton);
        setupButton(hideButton, HIDE_BUTTON);

        // Добавление пробела
        v = inflater.inflate(R.layout.keyboard_space, linearLayout, true);
        Button spaceButton = v.findViewById(R.id.key_button);
        buttonRow.add(spaceButton);
        setupButton(spaceButton, SPACE_BUTTON, null);

        // Лупа
        v = inflater.inflate(R.layout.keyboard_search, linearLayout, true);
        ImageButton searchButton = v.findViewById(R.id.key_button);
        buttonRow.add(searchButton);
        setupButton(searchButton, SEARCH_BUTTON);

        buttonsRows.add(buttonRow);

        Log.d(TAG, "Получен список из " + buttonsRows.size() + " рядов");
        for (int i = 0; i < buttonsRows.size(); i++) {
            Log.d(TAG, "Длина [" + i + "] ряда: " + buttonsRows.get(i).size());
        }
    }

    /*
    Навигация по рядам клавиш

    Алгоритм:

    KEYCODE_DPAD_RIGHT:

    Если выбрана последняя кнопка ряда, то осуществляется переход на первый
    символ нижестоящего ряда. Если ниже нет рядов, то фокус остаётся на текущей кнопке.

    KEYCODE_DPAD_LEFT:

    Если выбрана первая кнопка ряда, то осуществляется переход на последний символ
    вышестоящего ряда. Если выше нет рядов, то фокус остаётся на текущей кнопке

    KEYCODE_DPAD_DOWN

    Берётся индекс текущей кнопки в ряду и осуществляется переход на символ с таким же индексом
    в нижестоящем ряду. Если текущий индекс превышает массив кнопок нижестоящего ряда, то осуществляется
    переход на последний символ данного ряда. Если ниже нет рядов, то фокус остаётся на текущей кнопке

    KEYCODE_DPAD_UP:

    Берётся индекс текущей кнопки в ряду и осуществляется переход на символ с таким же индексом
    в вышестоящем ряду. Если текущий индекс превышает массив кнопок вышестоящего ряда, то осуществляется
    переход на последний символ данного ряда. Если выше нет рядов, то КЛАВИАТУРА ЗАКРЫВАЕТСЯ

    */

    private int currentRow = -1;
    private int currentPosition = -1;

    // Возвращение boolean (false, если нужно скрыть клавиатуру)
    public void focusNavigation(KeyEvent event) {
        Log.d(TAG, "event key code: " + event.getKeyCode());
        if (isKeyboardActive()) {
            Log.d(TAG, "#1");
            if (currentRow < 0) {
                currentRow = 0;
                currentPosition = 0;
                buttonsRows.get(currentRow).get(currentPosition).requestFocusFromTouch();
                buttonsRows.get(currentRow).get(currentPosition).requestFocus();
                Log.d(TAG, "#2: " + buttonsRows.get(currentRow).get(currentPosition));
                return;
            }
            int rowLength = buttonsRows.get(currentRow).size();
            int newPosition, newRow;
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    newPosition = currentPosition + 1;
                    if (newPosition < rowLength) {
                        currentPosition = newPosition;
                        buttonsRows.get(currentRow).get(currentPosition).requestFocusFromTouch();
                    } else {
                        newRow = currentRow + 1;
                        if (newRow < buttonsRows.size()) {
                            currentRow = newRow;
                            currentPosition = 0;
                            buttonsRows.get(currentRow).get(currentPosition).requestFocusFromTouch();
                        }
                    }
                break;

                case KeyEvent.KEYCODE_DPAD_LEFT:
                    newPosition = currentPosition - 1;
                    if (newPosition >= 0) {
                        currentPosition = newPosition;
                        buttonsRows.get(currentRow).get(currentPosition).requestFocusFromTouch();
                    } else {
                        newRow = currentRow - 1;
                        if (newRow < buttonsRows.size() && newRow >= 0) {
                            currentRow = newRow;
                            currentPosition = buttonsRows.get(currentRow).size() - 1;
                            buttonsRows.get(currentRow).get(currentPosition).requestFocusFromTouch();
                        }
                    }
                break;

                case KeyEvent.KEYCODE_DPAD_DOWN:
                    Log.d(TAG, "KEYCODE_DPAD_DOWN!");
                    newRow = currentRow + 1;
                    if (newRow < buttonsRows.size()) {
                        currentRow = newRow;



                        if (buttonsRows.get(currentRow).size() == 3 && currentPosition != 0 && currentPosition != buttonsRows.get(newRow - 1).size() - 1) {
                            currentPosition = 1;
                        } else if (buttonsRows.get(newRow).size() <= currentPosition) {
                            currentPosition = buttonsRows.get(currentRow).size() - 1;
                        }
                        buttonsRows.get(currentRow).get(currentPosition).requestFocusFromTouch();
                    }
                break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    newRow = currentRow - 1;
                    if (newRow >= 0) {
                        currentRow = newRow;
                        if (buttonsRows.get(currentRow + 1).size() == 3 && currentPosition == 1) {
                            currentPosition = buttonsRows.get(currentRow).size() / 2;
                        } else if (buttonsRows.get(newRow).size() <= currentPosition) {
                            currentPosition = buttonsRows.get(currentRow).size() - 1;
                        }
                        buttonsRows.get(currentRow).get(currentPosition).requestFocusFromTouch();

                    } else {
                        hideKeyboard();
                    }
                break;
            }
        }
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
                    isLanguageButtonFocused = button.isFocused();
                    isRussian = !isRussian;
                    hideKeyboard();
                    showKeyboard();
                }); // Смена языка
                if (isLanguageButtonFocused) {
                    button.requestFocusFromTouch();
                    isLanguageButtonFocused = false;
                }
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
        buttonsRows = new ArrayList<>();
        currentPosition = -1; currentRow = -1;
        keyboardActive = false;
        if (keyboardView != null) {
            viewGroup.removeView(keyboardView);
        } else {
            Log.e(TAG, "Keyboard is null");
        }
    }

    public boolean isKeyboardActive() { return keyboardActive; }

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
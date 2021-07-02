package tv.limehd.customkeyboard;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;

import tv.limehd.keyboard.KeyListener;
import tv.limehd.keyboard.Keyboard;

public class MainActivity extends AppCompatActivity implements KeyListener {

    static { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); }

    private EditText editText;

    private Keyboard keyboard;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initKeyboard();
        init();
    }

    private void init() {
        editText = findViewById(R.id.edit_text);
        editText.setFocusableInTouchMode(false);
        editText.setOnClickListener(click -> {
            if (!keyboard.isKeyboardActive()) {
                showKeyboard();
            }
        });
    }


    private void initKeyboard() {
        FrameLayout keyboardView = findViewById(R.id.keyboard_view);
        keyboard = new Keyboard.Builder(this, this, keyboardView)
            .enableNumberLine(true)
            .setNightMode(false)
            .build();
    }

    private void showKeyboard() {
        keyboard.showKeyboard();
    }

    private void hideKeyboard() {
        keyboard.hideKeyboard();
        Log.d("MainActivity", "hide keyboard!");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
            View v = getCurrentFocus();
            if (v.getId() != R.id.keyboard_view) {
                v.clearFocus();
            }
            keyboard.focusNavigation(event);
            Log.d("MainActivity", "current focus: " + getCurrentFocus());

        return true;
    }

    @Override
    public void onKeyClicked(String key) {
        editText.getText().append(key);
    }

    @Override
    public void onDeleteButtonClicked() {
        int length = editText.getText().length();
        if (length > 0) {
            editText.getText().delete(length - 1, length);
        }
    }

    @Override
    public void onLongDeleteButtonClicked() {
        editText.getText().clear();
    }

    @Override
    public void onKeyboardHideClicked() {
        hideKeyboard();
    }

    @Override
    public void onKeyboardOkClicked() {
        Toast.makeText(this, "OnKeyboardOkClicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onKeyboardHided() {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            Log.e("MainActivity.java", "Starting search..");
            View v = findViewAtPosition(getWindow().getDecorView().getRootView(), (int) ev.getRawX(), (int) ev.getRawY());
            Log.e("MainActivity.java", String.valueOf(v));
            Log.e("MainActivity.java", "end.");

            if (!(v instanceof EditText) && !(v instanceof AppCompatImageButton) && !(v instanceof AppCompatImageView) && !keyboard.isKeyboardView(v) && keyboard.isKeyboardActive()) {
                hideKeyboard();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private View findViewAtPosition(View parent, int x, int y) {
        if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup)parent;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                View viewAtPosition = findViewAtPosition(child, x, y);
                if (viewAtPosition != null) {
                    return viewAtPosition;
                }
                Log.e("MainActivity", String.valueOf(viewAtPosition));
            }
            return null;
        } else {
            Rect rect = new Rect();
            parent.getGlobalVisibleRect(rect);
            if (rect.contains(x, y)) {
                return parent;
            } else {
                return null;
            }
        }
    }
}

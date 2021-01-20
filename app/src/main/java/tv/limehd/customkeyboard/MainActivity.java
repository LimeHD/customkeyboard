package tv.limehd.customkeyboard;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;

import tv.limehd.keyboard.Keyboard;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements Keyboard.KeyListener {

    private EditText editText;
    private Button hideKeyboardButton;

    private Keyboard keyboard;
    private boolean isKeyboardActive = false;

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
            if (!isKeyboardActive) {
                showKeyboard();
            }
        });

        hideKeyboardButton = findViewById(R.id.hide_keyboard_button);
        hideKeyboardButton.setOnClickListener(click -> {
            hideKeyboard();
        });
        updateHideButton();
    }

    private void updateHideButton() {
        int visibility = isKeyboardActive ? VISIBLE : INVISIBLE;
        hideKeyboardButton.setVisibility(visibility);
    }

    private void initKeyboard() {
        FrameLayout keyboardView = findViewById(R.id.keyboard_view);
        keyboard = new Keyboard.Builder(this, this, keyboardView)
            .enableNumberLine(false)
            .setNightMode(true)
            .build();
    }

    private void showKeyboard() {
        keyboard.showKeyboard();
        isKeyboardActive = true;
        updateHideButton();
    }

    private void hideKeyboard() {
        keyboard.hideKeyboard();
        isKeyboardActive = false;
        updateHideButton();
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
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) hideKeyboard();
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            Log.e("MainActivity.java", "Starting search..");
            View v = findViewAtPosition(getWindow().getDecorView().getRootView(), (int) ev.getRawX(), (int) ev.getRawY());
            Log.e("MainActivity.java", String.valueOf(v));
            Log.e("MainActivity.java", "end.");

            if (!(v instanceof EditText) && !(v instanceof AppCompatImageButton) && !(v instanceof AppCompatImageView) && !keyboard.isKeyboardView(v) && isKeyboardActive) {
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

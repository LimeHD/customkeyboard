package tv.limehd.keyboard;

import android.view.View;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class KeyboardViewTest {

    private final Keyboard keyboard = Mockito.mock(Keyboard.class);
    private final View mockView = Mockito.mock(View.class);

    { Mockito.when(keyboard.isKeyboardView(mockView)).thenCallRealMethod(); }

    @Test
    public void isKeyboardView_correct() {
        Mockito.when(mockView.getId()).thenReturn(R.id.keyboard_view);
        Assert.assertTrue(keyboard.isKeyboardView(mockView));
    }

    @Test
    public void isKeyboardView_incorrect() {
        Mockito.when(mockView.getId()).thenReturn(Mockito.anyInt());
        Assert.assertFalse(keyboard.isKeyboardView(mockView));
    }
}

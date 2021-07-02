package tv.limehd.keyboard;

public interface KeyListener {
    /**
     * вызывается при нажатии по символьной кнопке
     * @param key нажатый символ
     */
    void onKeyClicked(String key);

    /**
     * короткий клик по кнопке удалить
     */
    void onDeleteButtonClicked();

    /**
     * длительное нажатие по кнопке удалить
     */
    void onLongDeleteButtonClicked();

    /**
     * нажатие по кнопке скрытия клавиатуры
     */
    void onKeyboardHideClicked();

    /**
     * нажатие по кнопке поиска (лупа)
     */
    void onKeyboardOkClicked();

    /**
     * вызывается после скрытия клавиатуры
     */
    void onKeyboardHided();
}

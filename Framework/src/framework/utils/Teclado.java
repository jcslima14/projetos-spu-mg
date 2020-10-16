package framework.utils;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class Teclado {

    private Robot robot;

    public Teclado() throws AWTException {
        this.robot = new Robot();
    }

    public Teclado(Robot robot) {
        this.robot = robot;
    }

    public void digitar(CharSequence characters) {
        int length = characters.length();
        for (int i = 0; i < length; i++) {
            char character = characters.charAt(i);
            digitar(character);
        }
    }

    public void digitar(int keyCode) {
    	robot.keyPress(keyCode);
    	robot.keyRelease(keyCode);
    }
    
    public void digitar(char character) {
        switch (character) {
        case 'a': digitarTecla(KeyEvent.VK_A); break;
        case 'b': digitarTecla(KeyEvent.VK_B); break;
        case 'c': digitarTecla(KeyEvent.VK_C); break;
        case 'd': digitarTecla(KeyEvent.VK_D); break;
        case 'e': digitarTecla(KeyEvent.VK_E); break;
        case 'f': digitarTecla(KeyEvent.VK_F); break;
        case 'g': digitarTecla(KeyEvent.VK_G); break;
        case 'h': digitarTecla(KeyEvent.VK_H); break;
        case 'i': digitarTecla(KeyEvent.VK_I); break;
        case 'j': digitarTecla(KeyEvent.VK_J); break;
        case 'k': digitarTecla(KeyEvent.VK_K); break;
        case 'l': digitarTecla(KeyEvent.VK_L); break;
        case 'm': digitarTecla(KeyEvent.VK_M); break;
        case 'n': digitarTecla(KeyEvent.VK_N); break;
        case 'o': digitarTecla(KeyEvent.VK_O); break;
        case 'p': digitarTecla(KeyEvent.VK_P); break;
        case 'q': digitarTecla(KeyEvent.VK_Q); break;
        case 'r': digitarTecla(KeyEvent.VK_R); break;
        case 's': digitarTecla(KeyEvent.VK_S); break;
        case 't': digitarTecla(KeyEvent.VK_T); break;
        case 'u': digitarTecla(KeyEvent.VK_U); break;
        case 'v': digitarTecla(KeyEvent.VK_V); break;
        case 'w': digitarTecla(KeyEvent.VK_W); break;
        case 'x': digitarTecla(KeyEvent.VK_X); break;
        case 'y': digitarTecla(KeyEvent.VK_Y); break;
        case 'z': digitarTecla(KeyEvent.VK_Z); break;
        case 'A': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_A); break;
        case 'B': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_B); break;
        case 'C': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_C); break;
        case 'D': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_D); break;
        case 'E': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_E); break;
        case 'F': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_F); break;
        case 'G': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_G); break;
        case 'H': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_H); break;
        case 'I': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_I); break;
        case 'J': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_J); break;
        case 'K': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_K); break;
        case 'L': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_L); break;
        case 'M': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_M); break;
        case 'N': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_N); break;
        case 'O': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_O); break;
        case 'P': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_P); break;
        case 'Q': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_Q); break;
        case 'R': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_R); break;
        case 'S': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_S); break;
        case 'T': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_T); break;
        case 'U': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_U); break;
        case 'V': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_V); break;
        case 'W': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_W); break;
        case 'X': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_X); break;
        case 'Y': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_Y); break;
        case 'Z': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_Z); break;
        case '`': digitarTecla(KeyEvent.VK_BACK_QUOTE); break;
        case '0': digitarTecla(KeyEvent.VK_0); break;
        case '1': digitarTecla(KeyEvent.VK_1); break;
        case '2': digitarTecla(KeyEvent.VK_2); break;
        case '3': digitarTecla(KeyEvent.VK_3); break;
        case '4': digitarTecla(KeyEvent.VK_4); break;
        case '5': digitarTecla(KeyEvent.VK_5); break;
        case '6': digitarTecla(KeyEvent.VK_6); break;
        case '7': digitarTecla(KeyEvent.VK_7); break;
        case '8': digitarTecla(KeyEvent.VK_8); break;
        case '9': digitarTecla(KeyEvent.VK_9); break;
        case '-': digitarTecla(KeyEvent.VK_MINUS); break;
        case '=': digitarTecla(KeyEvent.VK_EQUALS); break;
        case '~': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE); break;
        case '!': digitarTecla(KeyEvent.VK_EXCLAMATION_MARK); break;
        case '@': digitarTecla(KeyEvent.VK_AT); break;
        case '#': digitarTecla(KeyEvent.VK_NUMBER_SIGN); break;
        case '$': digitarTecla(KeyEvent.VK_DOLLAR); break;
        case '%': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_5); break;
        case '^': digitarTecla(KeyEvent.VK_CIRCUMFLEX); break;
        case '&': digitarTecla(KeyEvent.VK_AMPERSAND); break;
        case '*': digitarTecla(KeyEvent.VK_ASTERISK); break;
        case '(': digitarTecla(KeyEvent.VK_LEFT_PARENTHESIS); break;
        case ')': digitarTecla(KeyEvent.VK_RIGHT_PARENTHESIS); break;
        case '_': digitarTecla(KeyEvent.VK_UNDERSCORE); break;
        case '+': digitarTecla(KeyEvent.VK_PLUS); break;
        case '\t': digitarTecla(KeyEvent.VK_TAB); break;
        case '\n': digitarTecla(KeyEvent.VK_ENTER); break;
        case '[': digitarTecla(KeyEvent.VK_OPEN_BRACKET); break;
        case ']': digitarTecla(KeyEvent.VK_CLOSE_BRACKET); break;
        case '\\': digitarTecla(KeyEvent.VK_BACK_SLASH); break;
        case '{': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET); break;
        case '}': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET); break;
        case '|': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH); break;
        case ';': digitarTecla(KeyEvent.VK_SEMICOLON); break;
        // case ':': digitarTecla(KeyEvent.VK_COLON); break;
        case ':': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON); break;
        case '\'': digitarTecla(KeyEvent.VK_QUOTE); break;
        case '"': digitarTecla(KeyEvent.VK_QUOTEDBL); break;
        case ',': digitarTecla(KeyEvent.VK_COMMA); break;
        case '<': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA); break;
        case '.': digitarTecla(KeyEvent.VK_PERIOD); break;
        case '>': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD); break;
        case '/': digitarTecla(KeyEvent.VK_SLASH); break;
        case '?': digitarTecla(KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH); break;
        case ' ': digitarTecla(KeyEvent.VK_SPACE); break;
        default:
            throw new IllegalArgumentException("Cannot type character " + character);
        }
    }

    private void digitarTecla(int... keyCodes) {
        digitarTecla(keyCodes, 0, keyCodes.length);
    }

    private void digitarTecla(int[] keyCodes, int offset, int length) {
        if (length == 0) {
            return;
        }

        robot.keyPress(keyCodes[offset]);
        digitarTecla(keyCodes, offset + 1, length - 1);
        robot.keyRelease(keyCodes[offset]);
    }

}
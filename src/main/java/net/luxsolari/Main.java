package net.luxsolari;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.Random;

/**
 * This first tutorial, demonstrating setting up a simple {@link Terminal} and performing some basic operations on it.
 * @author Martin
 */
public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {

        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
        /*
        The DefaultTerminalFactory can be further tweaked, but we'll leave it with default settings in this tutorial.
         */

        Terminal terminal = defaultTerminalFactory.createTerminal();
        Screen screen = null;
        try {
            screen = new TerminalScreen(terminal);
            screen.startScreen();
            screen.setCursorPosition(null);

            Random random = new Random();
            TerminalSize terminalSize = screen.getTerminalSize();
            for(int column = 0; column < terminalSize.getColumns(); column++) {
                for(int row = 0; row < terminalSize.getRows(); row++) {
                    screen.setCharacter(column, row, new TextCharacter(
                            ' ',
                            TextColor.ANSI.DEFAULT,
                            // This will pick a random background color
                            TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().length)]));
                }
            }

            screen.refresh();

            long startTime = System.currentTimeMillis();
            while(System.currentTimeMillis() - startTime < 2000) {
                // The call to pollInput() is not blocking, unlike readInput()
                if(screen.pollInput() != null) {
                    break;
                }
                try {
                    Thread.sleep(1);
                }
                catch(InterruptedException ignore) {
                    break;
                }
            }

            while(true) {
                KeyStroke keyStroke = screen.pollInput();
                if (keyStroke != null && (keyStroke.getKeyType() == KeyType.Escape || keyStroke.getKeyType() == KeyType.EOF)) {
                    break;
                }

                TerminalSize newTerminalSize = screen.doResizeIfNecessary();
                if (newTerminalSize != null) {
                    terminalSize = newTerminalSize;
                }

                // Increase this to increase speed
                final int charactersToModifyPerLoop = 1;
                for (int i = 0; i < charactersToModifyPerLoop; i++) {
                    TerminalPosition cellToModify = new TerminalPosition(
                            random.nextInt(terminalSize.getColumns()),
                            random.nextInt(terminalSize.getRows()));
                    TextColor.ANSI color = TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().length)];

                    TextCharacter characterInBackBuffer = screen.getBackCharacter(cellToModify);
                    characterInBackBuffer = characterInBackBuffer.withBackgroundColor(color);
                    characterInBackBuffer = characterInBackBuffer.withCharacter(' ');   // Because of the label box further down, if it shrinks
                    screen.setCharacter(cellToModify, characterInBackBuffer);

                    String sizeLabel = "Terminal Size: " + terminalSize;
                    TerminalPosition labelBoxTopLeft = new TerminalPosition(1, 1);
                    TerminalSize labelBoxSize = new TerminalSize(sizeLabel.length() + 2, 3);
                    TerminalPosition labelBoxTopRightCorner = labelBoxTopLeft.withRelativeColumn(labelBoxSize.getColumns() - 1);
                    TextGraphics textGraphics = screen.newTextGraphics();
                    //This isn't really needed as we are overwriting everything below anyway, but just for demonstrative purpose
                    textGraphics.fillRectangle(labelBoxTopLeft, labelBoxSize, ' ');

                    textGraphics.drawLine(
                            labelBoxTopLeft.withRelativeColumn(1),
                            labelBoxTopLeft.withRelativeColumn(labelBoxSize.getColumns() - 2),
                            Symbols.DOUBLE_LINE_HORIZONTAL);
                    textGraphics.drawLine(
                            labelBoxTopLeft.withRelativeRow(2).withRelativeColumn(1),
                            labelBoxTopLeft.withRelativeRow(2).withRelativeColumn(labelBoxSize.getColumns() - 2),
                            Symbols.DOUBLE_LINE_HORIZONTAL);

                    textGraphics.setCharacter(labelBoxTopLeft, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);
                    textGraphics.setCharacter(labelBoxTopLeft.withRelativeRow(1), Symbols.DOUBLE_LINE_VERTICAL);
                    textGraphics.setCharacter(labelBoxTopLeft.withRelativeRow(2), Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER);
                    textGraphics.setCharacter(labelBoxTopRightCorner, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
                    textGraphics.setCharacter(labelBoxTopRightCorner.withRelativeRow(1), Symbols.DOUBLE_LINE_VERTICAL);
                    textGraphics.setCharacter(labelBoxTopRightCorner.withRelativeRow(2), Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER);

                    textGraphics.putString(labelBoxTopLeft.withRelative(1, 1), sizeLabel);

                    screen.refresh();
                    Thread.yield();
                }
            }

        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            if(terminal != null) {
                try {
                    /*
                    Closing the terminal doesn't always do something, but if you run the Swing or AWT bundled terminal
                    emulators for example, it will close the window and allow this application to terminate. Calling it
                    on a UnixTerminal will not have any affect.
                     */
                    terminal.close();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

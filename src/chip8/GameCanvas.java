package chip8;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author Luis Gustavo S. Barreto
 */
public class GameCanvas extends JPanel {
    private byte[] display;

    public void setDisplay(byte[] display) {
        this.display = display;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

                if (this.display == null)
            return;
                
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int y = 0; y < 32; ++y) {
            for (int x = 0; x < 64; ++x) {
                if (this.display[(y * 64) + x] != 0) {
                //    g.setColor(Color.black);
                    //} else {
                    g.setColor(Color.white);
                    //}

                    g.fillRect(x * 10, y * 10, 10, 10);
                }
            }
        }
    }
}

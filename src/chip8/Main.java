package chip8;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author Luis Gustavo S. Barreto
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final Chip8 chip8 = new Chip8();

        JFileChooser fc = new JFileChooser("Selecione o arquivo de ROM");
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            chip8.load(fc.getSelectedFile().getAbsolutePath());
        } else {
            System.exit(0);
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.addKeyListener(new KeyListener() {
                    
                    @Override
                    public void keyTyped(KeyEvent e) {
                    }
                    
                    @Override
                    public void keyPressed(KeyEvent e) {
                        chip8.setKey(translateKey(e.getKeyCode()), true);
                    }
                    
                    @Override
                    public void keyReleased(KeyEvent e) {
                        chip8.setKey(translateKey(e.getKeyCode()), false);
                    }
                });
                
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
                Box box = new Box(BoxLayout.Y_AXIS);
                box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
                box.add(Box.createVerticalGlue());
                box.add(chip8.getCanvas());
                box.add(Box.createVerticalGlue());
                
                frame.add(box);
                frame.pack();
                frame.setTitle("Chip 8 Emulator");
                frame.setSize(640, 320);
                frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
                frame.setFocusable(true);
            }
        });
        Runnable virtualMachine = new Runnable() {
            @Override
            public void run() {
                int step = 0;
                while (true) {
                    for (int i = 0; i < 10; i++) {
                        chip8.emulate();
                    }
                    
                    if (chip8.graphicsUpdated) {
                        chip8.getCanvas().setDisplay(chip8.display);
                        chip8.getCanvas().repaint();
                        chip8.graphicsUpdated = false;
                    }
                    
                    if (step++ % 2 == 0) {
                        if (chip8.delayTimer > 0) {
                            chip8.delayTimer--;
                        }
                    }
                    
                    try {
                        Thread.currentThread().sleep(8);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        
        new Thread(virtualMachine).start();
    }
    
    static int translateKey(int key) {
        switch (key) {
            case 49:
                return 1;
            case 50:
                return 2;
            case 51:
                return 3;
            case 52:
                return 4;
            case 81:
                return 5;
            case 87:
                return 6;
            case 69:
                return 7;
            case 82:
                return 8;
            case 65:
                return 9;
            case 83:
                return 10;
            case 68:
                return 11;
            case 70:
                return 12;
            case 90:
                return 13;
            case 88:
                return 14;
            case 67:
                return 15;
            case 86:
                return 16;
        }
        
        return -1;
    }
}

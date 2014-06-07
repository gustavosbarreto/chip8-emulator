package chip8;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luis Gustavo S. Barreto
 */
public class Chip8 {
    short memory[] = new short[4096];
    int opcode = 0;
    byte v[] = new byte[16];
    short index = 0;
    short pc = 0x200;
    short stack[] = new short[16];
    short sp = 0;
    byte display[] = new byte[64 * 32];
    int delayTimer = 0;

    private GameCanvas canvas = new GameCanvas();

    private int[] keys = new int[16];
    
    boolean graphicsUpdated = false;

    static char fontset[] = new char[]{
        0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
        0x20, 0x60, 0x20, 0x20, 0x70, // 1
        0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
        0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
        0x90, 0x90, 0xF0, 0x10, 0x10, // 4
        0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
        0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
        0xF0, 0x10, 0x20, 0x40, 0x40, // 7
        0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
        0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
        0xF0, 0x90, 0xF0, 0x90, 0x90, // A
        0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
        0xF0, 0x80, 0x80, 0x80, 0xF0, // C
        0xE0, 0x90, 0x90, 0x90, 0xE0, // D
        0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
        0xF0, 0x80, 0xF0, 0x80, 0x80 // F
    };

    public Chip8() {
        for (int i = 0; i < 80; i++) {
            memory[i] = (byte) fontset[i];
        }

        this.canvas.setPreferredSize(new Dimension(640, 320));
        this.canvas.setMaximumSize(new Dimension(640, 320));
        //this.canvas.setSize(640, 320);
        //this.canvas.setBackground(Color.white);

        //this.canvas.getGraphics().fillRect(1, 1, 10, 10);
    }

    public void load(String fileName) {
        try {
            byte bytes[] = Files.readAllBytes(Paths.get(fileName));

            for (int i = 0; i < bytes.length; i++) {
                this.memory[i + 0x200] = (short) (bytes[i] & 0xFF);
            }
        } catch (IOException ex) {
            Logger.getLogger(Chip8.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setKey(int key, boolean down) {
        if (key == -1) {
            return;
        }
        this.keys[key] = down ? 1 : 0;
    }

    public void emulate() {
        this.opcode = this.memory[pc] << 8 | this.memory[pc + 1];
        short x = (short) ((this.opcode & 0x0F00) >> 8); // Bit menos significante
        short y = (short) ((this.opcode & 0x00F0) >> 4); // Bit mais significante
        byte kk = (byte) (this.opcode & 0xFF); // Valor de 8-bit (os 8 bits menos significantes)
        short nnn = (short) (this.opcode & 0xFFF); // Valor de 12-bit (os 12 bits menos significantes)

        this.pc += 2;
        
        switch (this.opcode & 0xF000) {
            case 0x0000: // SYS addr
                switch (this.opcode) {
                    case 0x00E0: // CLS
                        for (int i = 0; i < this.display.length; i++)
                            this.display[i] = 0;
                        break;
                    case 0xEE: // RET
                        this.pc = this.stack[--this.sp];
                        break;
                }
                break;

            case 0x1000: // JP addr
                this.pc = nnn;
                break;

            case 0x2000: // CALL addr
                this.stack[this.sp] = this.pc;
                this.sp++;
                this.pc = nnn;
                break;

            case 0x3000: // SE Vx, byte
                if (v[x] == kk) {
                    this.pc += 2;
                }
                break;

            case 0x4000: // SNE Vx, byte
                if (v[x] != kk) {
                    this.pc += 2;
                }
                break;

            case 0x5000: // SE Vx, Vy
                if (v[x] == v[y]) {
                    this.pc += 2;
                }
                break;

            case 0x6000: // LD Vx, byte
                this.v[x] = kk;
                break;

            case 0x7000: // ADD Vx, byte
                this.v[x] += kk;

                if (this.v[x] > 255) {
                    this.v[x] -= 256;
                }
                break;

            case 0x8000:
                switch (this.opcode & 0x000F) {
                    case 0x0000: // LD Vx, Vy
                        this.v[x] = this.v[y];
                        break;
                    case 0x0001: // OR Vx, Vy
                        this.v[x] |= this.v[y];
                        break;
                    case 0x0002: // AND Vx, Vy
                        this.v[x] &= this.v[y];
                        break;
                    case 0x0003: // Xor Vx, Vy
                        this.v[x] ^= this.v[y];
                        break;
                    case 0x0004: // Add Vx, Vy
                        byte value = (byte) (this.v[x] + this.v[y]);
                        this.v[0xF] = (byte) ((value > 255) ? 1 : 0);
                        if (value > 255) {
                            value -= 256;
                        }
                        this.v[x] = value;
                    case 0x0005: // SUB Vx, Vy
                        this.v[0xF] = (byte) (this.v[x] > this.v[y] ? 1 : 0);
                        this.v[x] -= this.v[y];
                        if (this.v[x] < 0) {
                            this.v[x] += 256;
                        }
                        break;
                    case 0x0006: // SHR Vx {, Vy}
                        this.v[0xF] = (byte) (this.v[x] & 0x1);
                        this.v[x] >>= 1;
                        break;
                    case 0x0007: // SUBN Vx, Vy
                        this.v[0xF] = (byte) (this.v[x] > this.v[y] ? 1 : 0);
                        this.v[x] = (byte) (this.v[y] - this.v[x]);
                        if (this.v[x] < 0) {
                            this.v[x] += 256;
                        }
                        break;
                    case 0x000E: // SHL Vx {, Vy}
                        this.v[0xF] = (byte) (this.v[x] > this.v[y] ? 1 : 0);
                        this.v[x] <<= 1;
                        if (this.v[x] > 255) {
                            this.v[x] -= 256;
                        }
                        break;
                }

            case 0x9000: // SNE Vx, Vy
                if (this.v[x] != this.v[y]) {
                    this.pc += 2;
                }
                break;

            case 0xA000: // LD I, addr
                this.index = nnn;
                break;

            case 0xB000: // JP V0, addr
                this.pc = (short) (nnn + this.v[0]);
                break;

            case 0xC000: // RND Vx, byte
                this.v[x] = (byte) ((new Random().nextInt(256)) & kk);
                break;

            case 0xD000: // DRW Vx, By, nibble
                this.v[0xF] = 0;

                int height = opcode & 0x000F;

                for (int pY = 0; pY < height; pY++) {
                    int pixel = this.memory[this.index + pY];
                    for (int pX = 0; pX < 8; pX++) {
                        if ((pixel & 0x80) > 0) {
                            if (setPixel(this.v[x] + pX, this.v[y] + pY)) {
                                this.v[0xF] = 1;
                            }
                        }

                        pixel <<= 1;
                    }
                }

                // Update the GameCanvas
                this.graphicsUpdated = true;

                break;

            case 0xE000: // TODO
                switch (this.opcode & 0x00FF) {
                    case 0x009E: // SKP Vx
                        if (this.keys[this.v[x]] == 1) {
                            this.pc += 2;
                        }
                        break;

                    case 0x00A1: // SKNP Vx
                        if (this.keys[this.v[x]] == 0) {
                            this.pc += 2;
                        }
                        break;
                }

                break;

            case 0xF000:
                switch (this.opcode & 0x00FF) {
                    case 0x0007: // LD Vx, DT
                        this.v[x] = (byte) this.delayTimer;
                        break;
                    case 0x000A: // LD Vx, K
                        // keypress
                        break;
                    case 0x0015: // LD DT, Vx
                        this.delayTimer = this.v[x];
                        break;
                    case 0x0018: // LD ST, Vx
                        break;
                    case 0x001E: // ADD I, Vx
                        this.index += this.v[x];
                        break;
                    case 0x0029: // LD F, Vx
                        this.index = (short) (this.v[x] * 5);
                        break;

                    case 0x0033:
                        byte number = this.v[x];

                        for (int i = 3; i > 0; i--) {
                            this.memory[this.index + i - 1] = (short) (number % 10);
                            number /= 10;
                        }
                        
                        break;

                    case 0x0055:
                        for (int i = 0; i <= x; i++) {
                            this.memory[this.index + i] = this.v[i];
                        }
                        
                        break;
                    case 0x0065:
                        for (int i = 0; i <= x; i++) {
                            this.v[i] = (byte)this.memory[this.index + i];
                        }
                        
                        break;
                }
        }
    }

    private boolean setPixel(int x, int y) {
        if (x > 64) {
            x -= 64;
        } else if (x < 0) {
            x += 64;
        }

        if (y > 32) {
            y -= 32;
        } else if (y < 0) {
            y += 32;
        }

        int location = x + (y * 64);
        this.display[location] ^= 1;

        return this.display[location] != 1;
    }

    public GameCanvas getCanvas() {
        return this.canvas;
    }
}

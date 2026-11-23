import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.Properties;
import java.util.Random;

// Classe principal que abre a janela do Talking Fu
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("TALKINFU — Furão no Banco");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setResizable(false);
            TelaJogo tela = new TelaJogo();
            janela.add(tela);
            janela.pack();
            janela.setLocationRelativeTo(null);
            janela.setVisible(true);
            tela.requestFocusInWindow();
        });
    }
}

// Regras de vida, humor, persistência e ações do furão
class Furao {
    public static final int ANOS_PARA_VENCER = 50;
    private static final String ARQUIVO_SAVE = "furao-banco-save.properties";

    private String nome;
    private int fome = 20;
    private int felicidade = 80;
    private int cansaco = 0;
    private int idade = 0;
    private boolean fim = false;
    private boolean venceu = false;
    private String mensagemFim = "";

    public Furao(String nomeEscolhido) {
        if (nomeEscolhido == null || nomeEscolhido.trim().isEmpty()) {
            this.nome = "Furão";
        } else {
            String limpo = nomeEscolhido.trim();
            this.nome = limpo.length() > 14 ? limpo.substring(0, 14) : limpo;
        }
    }

    private int limitar(int valor) {
        return Math.max(0, Math.min(100, valor));
    }

    public void passarTempo() {
        if (fim) return;
        fome = limitar(fome + 3);
        felicidade = limitar(felicidade - 3);
        cansaco = limitar(cansaco + 10);
        idade++;
        checarFim();
        salvar();
    }

    public void alimentar() {
        if (fim) return;
        fome = limitar(fome - 25);
        felicidade = limitar(felicidade + 3);
        cansaco = limitar(cansaco + 2);
        checarFim();
        salvar();
    }

    public void brincar() {
        if (fim) return;
        fome = limitar(fome + 5);
        felicidade = limitar(felicidade + 15);
        cansaco = limitar(cansaco + 8);
        checarFim();
        salvar();
    }

    public void descansar() {
        if (fim) return;
        fome = limitar(fome + 4);
        cansaco = limitar(cansaco - 35);
        checarFim();
        salvar();
    }

    public String getHumor() {
        if (fome >= 70) return "faminto";
        if (cansaco >= 70) return "cansado";
        if (felicidade <= 30) return "triste";
        return "feliz";
    }

    private void checarFim() {
        if (fome >= 100) {
            fim = true;
            mensagemFim = nome + " passou fome demais. Viveu " + idade + (idade == 1 ? " ano." : " anos.");
        } else if (cansaco >= 100) {
            fim = true;
            mensagemFim = nome + " chegou ao limite do cansaço. Viveu " + idade + (idade == 1 ? " ano." : " anos.");
        } else if (felicidade <= 0) {
            fim = true;
            mensagemFim = nome + " perdeu toda a alegria. Viveu " + idade + (idade == 1 ? " ano." : " anos.");
        } else if (idade >= ANOS_PARA_VENCER) {
            fim = true;
            venceu = true;
            mensagemFim = nome + " chegou aos " + ANOS_PARA_VENCER + " anos, cuidado até o fim.";
        }
        if (fim) apagarSave();
    }

    public void salvar() {
        if (fim) return;
        Properties props = new Properties();
        props.setProperty("nome", nome);
        props.setProperty("fome", String.valueOf(fome));
        props.setProperty("felicidade", String.valueOf(felicidade));
        props.setProperty("cansaco", String.valueOf(cansaco));
        props.setProperty("idade", String.valueOf(idade));
        try (FileOutputStream out = new FileOutputStream(ARQUIVO_SAVE)) {
            props.store(out, "Save Talking Fu");
        } catch (IOException ignored) {}
    }

    public static Furao carregarSave() {
        File arquivo = new File(ARQUIVO_SAVE);
        if (!arquivo.exists()) return null;
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(arquivo)) {
            props.load(in);
            Furao f = new Furao(props.getProperty("nome", "Furão"));
            f.fome = Integer.parseInt(props.getProperty("fome", "20"));
            f.felicidade = Integer.parseInt(props.getProperty("felicidade", "80"));
            f.cansaco = Integer.parseInt(props.getProperty("cansaco", "0"));
            f.idade = Integer.parseInt(props.getProperty("idade", "0"));
            return f;
        } catch (Exception e) {
            return null;
        }
    }

    public static void apagarSave() {
        File arquivo = new File(ARQUIVO_SAVE);
        if (arquivo.exists()) arquivo.delete();
    }

    public String getNome() { return nome; }
    public int getFome() { return fome; }
    public int getFelicidade() { return felicidade; }
    public int getCansaco() { return cansaco; }
    public int getIdade() { return idade; }
    public boolean isFim() { return fim; }
    public boolean isVenceu() { return venceu; }
    public String getMensagemFim() { return mensagemFim; }
}

// Sintetizador simples de efeitos sonoros
class SonsJogo {
    public static void tocar(String tipo) {
        new Thread(() -> {
            try {
                if (tipo.equals("dook")) {
                    tom(520, 680, 75, 0.18);
                    Thread.sleep(35);
                    tom(600, 790, 85, 0.18);
                } else if (tipo.equals("comer")) {
                    for (int i = 0; i < 3; i++) {
                        tom(260, 190, 65, 0.15);
                        Thread.sleep(90);
                    }
                } else if (tipo.equals("dormir")) {
                    tom(420, 280, 260, 0.12);
                } else if (tipo.equals("vitoria")) {
                    tom(523, 523, 110, 0.16);
                    tom(659, 659, 110, 0.16);
                    tom(784, 1046, 240, 0.18);
                } else if (tipo.equals("derrota")) {
                    tom(380, 220, 320, 0.16);
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private static void tom(double freqIni, double freqFim, int ms, double vol) throws LineUnavailableException {
        float sampleRate = 22050f;
        int totalSamples = (int) ((ms / 1000.0) * sampleRate);
        byte[] buf = new byte[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            double prog = i / (double) totalSamples;
            double freq = freqIni + (freqFim - freqIni) * prog;
            double envelope = Math.sin(Math.PI * prog);
            double angulo = 2.0 * Math.PI * i * freq / sampleRate;
            buf[i] = (byte) (Math.sin(angulo) * 127.0 * vol * envelope);
        }

        AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
        try (SourceDataLine sdl = AudioSystem.getSourceDataLine(af)) {
            sdl.open(af);
            sdl.start();
            sdl.write(buf, 0, buf.length);
            sdl.drain();
        }
    }
}

// Cenário procedural, tela inicial integrada e furão animado
class TelaJogo extends JPanel {
    private Furao furao = new Furao("Furão");
    private Furao saveExistente = null;
    private boolean telaInicio = true;
    private StringBuilder nomeDigitado = new StringBuilder();

    private String acaoAtual = "nenhuma";
    private String falaAtual = "";
    private String humorAntes = "feliz";
    private boolean statusAberto = false;
    private boolean somAtivo = true;
    private int botaoHover = -1;

    private double tempoAnim = 0;
    private long inicioAcaoMs = 0;
    private long fimAcaoMs = 0;
    private long fimFalaMs = 0;
    private long ultimoAnoMs = System.currentTimeMillis();
    private long flashIdadeMs = 0;

    private final double[][] vagalumes = new double[9][4];
    private final Rectangle[] btnAcoes = new Rectangle[4];
    private Rectangle btnSom = new Rectangle();
    private Rectangle areaFurao = new Rectangle();
    private Rectangle btnComecar = new Rectangle();
    private Rectangle btnContinuar = new Rectangle();

    private static final Color INK = new Color(10, 15, 11);
    private static final Color BLACK = new Color(2, 4, 3);
    private static final Color TINTA = new Color(7, 11, 8);
    private static final Color OSSO = new Color(232, 230, 212);
    private static final Color MUSGO = new Color(155, 227, 29);
    private static final Color SEDA = new Color(196, 61, 61);

    private static final Color[] MF = {new Color(47, 84, 25), new Color(79, 126, 42), new Color(30, 56, 20)};
    private static final Color[] MN = {new Color(76, 133, 33), new Color(130, 184, 44), new Color(47, 90, 23)};
    private static final Color[] MB = {new Color(106, 169, 30), new Color(162, 212, 60), new Color(62, 115, 22)};
    private static final Color[] MD = {new Color(44, 90, 24), new Color(77, 134, 38), new Color(26, 56, 17)};

    public TelaJogo() {
        setPreferredSize(new Dimension(1280, 720));
        setBackground(TINTA);
        setFocusable(true);

        Random rnd = new Random(11);
        for (int i = 0; i < vagalumes.length; i++) {
            vagalumes[i][0] = 0.25 + rnd.nextDouble() * 0.50;
            vagalumes[i][1] = 0.10 + rnd.nextDouble() * 0.50;
            vagalumes[i][2] = rnd.nextDouble() * 6.283;
            vagalumes[i][3] = 0.02 + rnd.nextDouble() * 0.025;
        }

        // Verifica se existe save sem abrir popup bloqueante
        saveExistente = Furao.carregarSave();

        new Timer(16, e -> atualizar()).start();

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                botaoHover = -1;

                if (telaInicio) {
                    boolean mao = btnComecar.contains(p) || (saveExistente != null && btnContinuar.contains(p));
                    setCursor(Cursor.getPredefinedCursor(mao ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    return;
                }

                for (int i = 0; i < btnAcoes.length; i++) {
                    if (btnAcoes[i] != null && btnAcoes[i].contains(p)) {
                        botaoHover = i;
                        break;
                    }
                }
                boolean mao = botaoHover != -1 || btnSom.contains(p) || areaFurao.contains(p);
                setCursor(Cursor.getPredefinedCursor(mao ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                Point p = e.getPoint();

                if (telaInicio) {
                    if (btnComecar.contains(p)) {
                        comecarNovoJogo();
                    } else if (saveExistente != null && btnContinuar.contains(p)) {
                        furao = saveExistente;
                        entrarNaCena();
                    }
                    return;
                }

                if (btnSom.contains(p)) {
                    somAtivo = !somAtivo;
                    return;
                }

                if (furao.isFim()) {
                    nomeDigitado.setLength(0);
                    saveExistente = null;
                    telaInicio = true;
                    return;
                }

                if (statusAberto) {
                    statusAberto = false;
                    ultimoAnoMs = System.currentTimeMillis();
                    return;
                }

                for (int i = 0; i < btnAcoes.length; i++) {
                    if (btnAcoes[i] != null && btnAcoes[i].contains(p)) {
                        agir(i);
                        return;
                    }
                }

                if (areaFurao.contains(p) && acaoAtual.equals("nenhuma")) {
                    acaoAtual = "pulinho";
                    inicioAcaoMs = System.currentTimeMillis();
                    fimAcaoMs = inicioAcaoMs + 450;
                    falar("Dook!");
                    if (somAtivo) SonsJogo.tocar("dook");
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!telaInicio) return;
                char c = e.getKeyChar();
                if (c == '\b') {
                    if (nomeDigitado.length() > 0) {
                        nomeDigitado.deleteCharAt(nomeDigitado.length() - 1);
                    }
                } else if (c == '\n' || c == '\r') {
                    comecarNovoJogo();
                } else if (!Character.isISOControl(c) && nomeDigitado.length() < 14) {
                    nomeDigitado.append(c);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (telaInicio) return;
                if (furao.isFim()) {
                    nomeDigitado.setLength(0);
                    saveExistente = null;
                    telaInicio = true;
                    return;
                }
                char k = Character.toLowerCase(e.getKeyChar());
                if (k == 's') {
                    statusAberto = !statusAberto;
                    ultimoAnoMs = System.currentTimeMillis();
                    return;
                }
                if (k == 'm') {
                    somAtivo = !somAtivo;
                    return;
                }
                if (statusAberto) return;
                if (k == 'a') agir(0);
                else if (k == 'b') agir(1);
                else if (k == 'd') agir(2);
            }
        });
    }

    private void comecarNovoJogo() {
        furao = new Furao(nomeDigitado.toString());
        furao.salvar();
        entrarNaCena();
    }

    private void entrarNaCena() {
        telaInicio = false;
        acaoAtual = "nenhuma";
        humorAntes = furao.getHumor();
        statusAberto = false;
        ultimoAnoMs = System.currentTimeMillis();
        falar("Dook dook!");
        if (somAtivo) SonsJogo.tocar("dook");
    }

    private void falar(String texto) {
        falaAtual = texto;
        fimFalaMs = System.currentTimeMillis() + 2200;
    }

    private void agir(int idx) {
        if (idx == 3) {
            statusAberto = true;
            return;
        }
        if (!acaoAtual.equals("nenhuma")) return;

        long agora = System.currentTimeMillis();
        inicioAcaoMs = agora;
        if (idx == 0) {
            acaoAtual = "comendo";
            fimAcaoMs = agora + 1800;
            falar("Nham nham!");
            if (somAtivo) SonsJogo.tocar("comer");
            furao.alimentar();
        } else if (idx == 1) {
            acaoAtual = "brincando";
            fimAcaoMs = agora + 1750;
            falar("Dook dook dook!");
            if (somAtivo) SonsJogo.tocar("dook");
            furao.brincar();
        } else if (idx == 2) {
            acaoAtual = "dormindo";
            fimAcaoMs = agora + 2600;
            falar("Zzz…");
            if (somAtivo) SonsJogo.tocar("dormir");
            furao.descansar();
        }
        verificarSomFim();
    }

    private void verificarSomFim() {
        if (furao.isFim() && somAtivo) {
            SonsJogo.tocar(furao.isVenceu() ? "vitoria" : "derrota");
        }
    }

    private void atualizar() {
        long agora = System.currentTimeMillis();
        tempoAnim += 0.035;
        for (double[] v : vagalumes) v[2] += v[3];

        if (telaInicio) {
            repaint();
            return;
        }

        if (!acaoAtual.equals("nenhuma") && agora >= fimAcaoMs) {
            acaoAtual = "nenhuma";
        }
        if (!falaAtual.isEmpty() && agora >= fimFalaMs) {
            falaAtual = "";
        }

        if (!furao.isFim() && !statusAberto) {
            if (agora - ultimoAnoMs >= 8000) {
                ultimoAnoMs += 8000;
                furao.passarTempo();
                flashIdadeMs = agora + 900;
                verificarSomFim();
            }
            String h = furao.getHumor();
            if (acaoAtual.equals("nenhuma") && !h.equals(humorAntes)) {
                if (h.equals("faminto")) falar("Estou com fome…");
                else if (h.equals("cansado")) falar("Preciso dormir…");
                else if (h.equals("triste")) falar("Brinca comigo?");
            }
            humorAntes = h;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Sempre desenha o cenário e o furão ao fundo imediatamente ao abrir
        AffineTransform original = g2.getTransform();
        g2.scale(w / 1919.0, h / 1080.0);
        desenharCenarioCompleto(g2);
        desenharVagalumes(g2);
        desenharFuraoDetalhado(g2);
        if (!telaInicio) {
            desenharPropAcao(g2);
            desenharFala(g2);
        }
        g2.setTransform(original);

        if (telaInicio) {
            desenharTelaInicio(g2, w, h);
            return;
        }

        desenharHUD(g2, w, h);
        desenharBotoes(g2, w, h);
        if (statusAberto) desenharModalStatus(g2, w, h);
        if (furao.isFim()) desenharModalFim(g2, w, h);
    }

    /* ================= TELA INICIAL INTEGRADA (IGUAL AO INDEX.HTML) ================= */
    private void desenharTelaInicio(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(4, 7, 5, 195));
        g2.fillRect(0, 0, w, h);

        int cx = w / 2;
        int cy = h / 2;

        g2.setColor(OSSO);
        g2.setFont(new Font("Serif", Font.BOLD, 34));
        String titulo = "FURÃO NO BANCO";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(titulo, cx - fm.stringWidth(titulo) / 2, cy - 85);

        g2.setFont(new Font("Serif", Font.ITALIC, 16));
        String sub = "Cuide dele até os 50 anos. Dê um nome para começar.";
        fm = g2.getFontMetrics();
        g2.setColor(new Color(232, 230, 212, 215));
        g2.drawString(sub, cx - fm.stringWidth(sub) / 2, cy - 48);

        // Campo de texto estilizado
        int boxW = 290, boxH = 42;
        int boxX = cx - boxW / 2, boxY = cy - 18;
        g2.setColor(new Color(7, 11, 8, 220));
        g2.fillRect(boxX, boxY, boxW, boxH);
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(OSSO);
        g2.drawRect(boxX, boxY, boxW, boxH);

        g2.setFont(new Font("Serif", Font.PLAIN, 17));
        String textoCampo = nomeDigitado.length() == 0 ? "Digite o nome do furão..." : nomeDigitado.toString();
        g2.setColor(nomeDigitado.length() == 0 ? new Color(232, 230, 212, 110) : OSSO);
        fm = g2.getFontMetrics();
        int tx = cx - fm.stringWidth(textoCampo) / 2;
        g2.drawString(textoCampo, tx, boxY + 27);

        // Cursor piscando
        if ((System.currentTimeMillis() / 500) % 2 == 0) {
            int cursorX = nomeDigitado.length() == 0 ? cx : tx + fm.stringWidth(textoCampo) + 2;
            g2.setColor(MUSGO);
            g2.drawLine(cursorX, boxY + 10, cursorX, boxY + 32);
        }

        // Botão COMEÇAR
        btnComecar = new Rectangle(cx - 85, boxY + 58, 170, 38);
        g2.setColor(new Color(18, 28, 13, 230));
        g2.fillRect(btnComecar.x, btnComecar.y, btnComecar.width, btnComecar.height);
        g2.setColor(MUSGO);
        g2.drawRect(btnComecar.x, btnComecar.y, btnComecar.width, btnComecar.height);

        g2.setFont(new Font("Serif", Font.BOLD, 13));
        String lblCom = "COMEÇAR (ENTER)";
        fm = g2.getFontMetrics();
        g2.drawString(lblCom, cx - fm.stringWidth(lblCom) / 2, btnComecar.y + 24);

        // Botão opcional se houver save anterior
        if (saveExistente != null) {
            btnContinuar = new Rectangle(cx - 125, btnComecar.y + 52, 250, 34);
            g2.setColor(new Color(8, 12, 9, 210));
            g2.fillRect(btnContinuar.x, btnContinuar.y, btnContinuar.width, btnContinuar.height);
            g2.setColor(new Color(232, 230, 212, 160));
            g2.drawRect(btnContinuar.x, btnContinuar.y, btnContinuar.width, btnContinuar.height);

            g2.setFont(new Font("Serif", Font.ITALIC, 13));
            String lblCont = "Continuar com " + saveExistente.getNome() + " (" + saveExistente.getIdade() + " anos)";
            fm = g2.getFontMetrics();
            g2.setColor(OSSO);
            g2.drawString(lblCont, cx - fm.stringWidth(lblCont) / 2, btnContinuar.y + 22);
        }
    }

    /* ================= CENÁRIO COMPLETO (1919x1080) ================= */
    private void desenharCenarioCompleto(Graphics2D g2) {
        Random rnd = new Random(11);

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(985, 590), 860,
                new float[]{0f, 0.25f, 0.55f, 0.80f, 1f},
                new Color[]{new Color(136, 164, 106), new Color(107, 138, 71), new Color(52, 80, 44), new Color(21, 36, 26), TINTA}
        ));
        g2.fillRect(0, 0, 1919, 1080);

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(985, 610), 340,
                new float[]{0f, 1f},
                new Color[]{new Color(227, 245, 164, 105), new Color(227, 245, 164, 0)}
        ));
        g2.fillOval(985 - 340, 610 - 280, 680, 560);

        int[][] arcosLuz = {{330, 270, 46}, {440, 360, 40}, {560, 450, 52}, {690, 540, 36}, {820, 640, 44}};
        for (int[] a : arcosLuz) {
            g2.setColor(new Color(223, 242, 168, 18));
            g2.setStroke(new BasicStroke(a[2], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(985 - a[0], 720 - a[1], a[0] * 2, a[1] * 2, 0, 180);
        }

        int[][] colunas = {{690, 60, 480}, {1255, 70, 440}, {905, 50, 300}, {1075, 50, 300}};
        g2.setColor(new Color(13, 26, 16, 72));
        for (int[] c : colunas) {
            g2.fillRoundRect(c[0], 720 - c[2], c[1], c[2], c[1], c[1]);
        }

        int[][] musgoFundo = {{760, 380, 34}, {860, 300, 30}, {1010, 420, 44}, {1330, 260, 34}, {1470, 300, 38}, {1700, 320, 40}, {590, 300, 30}};
        for (int[] m : musgoFundo) desenharMusgoPendurado(g2, m[0], m[1], m[2], MF, rnd);

        Path2D ossoCurvo = new Path2D.Double();
        ossoCurvo.moveTo(520, 40);
        ossoCurvo.curveTo(380, 110, 230, 220, 160, 420);
        ossoCurvo.curveTo(120, 540, 110, 640, 120, 760);
        g2.setStroke(new BasicStroke(76f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(INK);
        g2.draw(ossoCurvo);
        g2.setStroke(new BasicStroke(64f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(159, 171, 154));
        g2.draw(ossoCurvo);

        g2.setStroke(new BasicStroke(7f));
        g2.setColor(new Color(168, 179, 162));
        g2.fillOval(122, 388, 80, 64);
        g2.setColor(INK);
        g2.drawOval(122, 388, 80, 64);

        for (int i = 0; i < 4; i++) {
            int vx = 92 + (i % 2) * 8;
            int vy = 480 + i * 72;
            g2.setColor(i % 2 == 1 ? new Color(165, 179, 174) : new Color(141, 158, 157));
            g2.fillRoundRect(vx, vy, 130, 76, 68, 68);
            g2.setColor(INK);
            g2.drawRoundRect(vx, vy, 130, 76, 68, 68);
            g2.setColor(new Color(211, 220, 211, 180));
            g2.fillRoundRect(vx + 16, vy + 12, 40, 9, 8, 8);
        }

        desenharPecaOsso(g2, new double[][]{{440, 0}, {650, 0}, {620, 70, 560, 150, 500, 205}, {480, 120, 450, 60, 440, 0}}, new Color(180, 191, 172));
        desenharPecaOsso(g2, new double[][]{{1270, 0}, {1480, 0}, {1450, 60, 1400, 120, 1360, 215}, {1330, 140, 1290, 70, 1270, 0}}, new Color(180, 191, 172));
        desenharPecaOsso(g2, new double[][]{{1480, 0}, {1640, 0}, {1610, 60, 1560, 120, 1525, 150}, {1500, 100, 1490, 50, 1480, 0}}, new Color(169, 181, 161));

        desenharPecaOsso(g2, new double[][]{{470, 660}, {430, 570, 430, 470, 525, 378}, {505, 470, 520, 570, 565, 660}}, new Color(180, 191, 172));
        desenharPecaOsso(g2, new double[][]{{395, 700}, {385, 560, 395, 470, 440, 395}, {440, 480, 462, 580, 484, 700}}, new Color(162, 174, 155));
        desenharPecaOsso(g2, new double[][]{{1650, 760}, {1640, 640, 1690, 540, 1762, 470}, {1742, 560, 1762, 660, 1792, 760}}, new Color(125, 138, 128));

        g2.setColor(new Color(154, 167, 147));
        g2.fillOval(585 - 102, 690 - 122, 204, 244);
        g2.setColor(INK);
        g2.drawOval(585 - 102, 690 - 122, 204, 244);

        g2.setColor(new Color(125, 138, 128));
        g2.fillOval(1665 - 92, 715 - 72, 184, 144);
        g2.setColor(INK);
        g2.drawOval(1665 - 92, 715 - 72, 184, 144);

        int[][] musgoFrente = {{985, 300, 32}, {1105, 260, 30}, {1225, 380, 34}, {1445, 280, 32}, {1580, 380, 42}, {1670, 420, 38}};
        for (int[] m : musgoFrente) desenharMusgoPendurado(g2, m[0], m[1], m[2], MN, rnd);

        int[][] musgoClaro = {{330, 280, 34}, {270, 170, 22}, {400, 220, 26}, {190, 120, 18}, {560, 110, 20}};
        for (int[] m : musgoClaro) desenharMusgoPendurado(g2, m[0], m[1], m[2], MB, rnd);

        Color[] cupula = {
                new Color(61, 74, 64), new Color(111, 126, 105),
                new Color(61, 74, 64), new Color(111, 126, 105),
                new Color(61, 74, 64), new Color(111, 126, 105),
                new Color(31, 42, 33)
        };
        for (int i = 0; i < cupula.length; i++) {
            int rx = 178 - i * 22;
            int ry = 128 - i * 15;
            g2.setColor(cupula[i]);
            g2.fillArc(985 - rx, 712 - ry, rx * 2, ry * 2, 0, 180);
            g2.setStroke(new BasicStroke(i == 0 ? 8f : 5f));
            g2.setColor(INK);
            g2.drawArc(985 - rx, 712 - ry, rx * 2, ry * 2, 0, 180);
        }

        Path2D baseBanco = new Path2D.Double();
        baseBanco.moveTo(815, 744);
        baseBanco.curveTo(805, 782, 810, 812, 842, 830);
        baseBanco.lineTo(1128, 830);
        baseBanco.curveTo(1160, 812, 1165, 782, 1155, 744);
        baseBanco.closePath();
        g2.setColor(new Color(34, 46, 37));
        g2.fill(baseBanco);
        g2.setStroke(new BasicStroke(7f));
        g2.setColor(INK);
        g2.draw(baseBanco);

        g2.setColor(new Color(83, 100, 90));
        g2.fillRoundRect(800, 706, 370, 42, 38, 38);
        g2.setColor(INK);
        g2.drawRoundRect(800, 706, 370, 42, 38, 38);
        g2.setColor(new Color(164, 180, 151));
        g2.fillRoundRect(814, 709, 342, 10, 10, 10);

        g2.setColor(new Color(143, 208, 42));
        g2.fillOval(846, 704, 44, 12);
        g2.fillOval(1068, 704, 60, 12);
        g2.fillOval(956, 706, 32, 10);

        desenharMonteMusgo(g2, 745, 725, 70, 60, 9, MB, rnd);
        desenharMonteMusgo(g2, 1340, 735, 110, 55, 10, MD, rnd);
        desenharMonteMusgo(g2, 1620, 790, 95, 55, 9, MN, rnd);
        desenharMonteMusgo(g2, 275, 470, 70, 65, 8, MB, rnd);
        desenharMonteMusgo(g2, 455, 615, 40, 30, 5, MD, rnd);

        Color[] palPedras = {new Color(140, 155, 134), new Color(164, 176, 156), new Color(118, 135, 111), new Color(180, 191, 172), new Color(147, 160, 140)};
        int[][] linhasPedras = {{806, 0, 0}, {844, 16, 1}};
        g2.setStroke(new BasicStroke(5f));
        for (int[] lp : linhasPedras) {
            for (int x = 455 + lp[1]; x < 1800; x += 33) {
                int y = (int) (lp[0] + (-7 + rnd.nextDouble() * 14) - Math.abs(x - 985) * 0.012);
                int rx = (int) (24 + rnd.nextDouble() * 6);
                int ry = (int) (27 + rnd.nextDouble() * 7);
                g2.setColor(lp[2] == 1 ? new Color(102, 119, 97) : palPedras[rnd.nextInt(palPedras.length)]);
                g2.fillOval(x - rx, y - ry, rx * 2, ry * 2);
                g2.setColor(INK);
                g2.drawOval(x - rx, y - ry, rx * 2, ry * 2);
                g2.setColor(new Color(226, 233, 216, 90));
                g2.fillOval(x - 7 - rx / 2, (int) (y - ry * 0.73), rx, (int) (ry * 0.56));
                if (rnd.nextDouble() < 0.35) {
                    g2.setColor(new Color(124, 182, 35));
                    g2.fillOval(x + rnd.nextInt(20) - 10 - 9, (int) (y - ry * 0.6) - 5, 18, 10);
                }
            }
        }

        desenharMonteMusgo(g2, 1520, 900, 260, 70, 16, MN, rnd);
        desenharMonteMusgo(g2, 1400, 870, 120, 50, 8, MB, rnd);
        desenharMonteMusgo(g2, 690, 925, 220, 55, 12, MN, rnd);
        desenharMonteMusgo(g2, 1130, 960, 160, 40, 7, MD, rnd);

        desenharSamambaia(g2, 1830, 170, 1790, 300, 1850, 450, 1800, 720, 24, rnd);
        desenharSamambaia(g2, 1765, 300, 1735, 420, 1775, 520, 1745, 700, 14, rnd);

        g2.setColor(BLACK);
        Path2D silhuetaEsq = new Path2D.Double();
        silhuetaEsq.moveTo(0, 700);
        silhuetaEsq.curveTo(90, 690, 180, 740, 250, 745);
        silhuetaEsq.curveTo(330, 750, 400, 780, 450, 830);
        silhuetaEsq.curveTo(500, 880, 520, 960, 600, 1080);
        silhuetaEsq.lineTo(0, 1080);
        silhuetaEsq.closePath();
        g2.fill(silhuetaEsq);

        Path2D chaoPreto = new Path2D.Double();
        chaoPreto.moveTo(0, 1080);
        chaoPreto.lineTo(0, 950);
        chaoPreto.curveTo(200, 965, 400, 1015, 700, 1022);
        chaoPreto.curveTo(1000, 1030, 1300, 1002, 1500, 972);
        chaoPreto.curveTo(1700, 942, 1850, 905, 1919, 885);
        chaoPreto.lineTo(1919, 1080);
        chaoPreto.closePath();
        g2.fill(chaoPreto);

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(960, 560), 1100,
                new float[]{0.55f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 204)}
        ));
        g2.fillRect(0, 0, 1919, 1080);
    }

    private void desenharMusgoPendurado(Graphics2D g2, int x, int len, int w, Color[] c, Random rnd) {
        java.util.List<int[]> pts = new java.util.ArrayList<>();
        for (int y = -10; y < len; y += Math.max(4, w / 2)) {
            int px = (int) (x + Math.sin(y / 60.0 + x) * w * 0.15);
            int r = (int) (w * (1.0 - 0.5 * y / len) * (0.85 + rnd.nextDouble() * 0.27));
            pts.add(new int[]{px, y, Math.max(3, r)});
        }
        desenharBlob(g2, pts, c);
    }

    private void desenharMonteMusgo(Graphics2D g2, int cx, int cy, int rx, int ry, int n, Color[] c, Random rnd) {
        java.util.List<int[]> pts = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            double a = rnd.nextDouble() * 6.283;
            double d = Math.sqrt(rnd.nextDouble());
            int px = (int) (cx + Math.cos(a) * rx * d);
            int py = (int) (cy + Math.sin(a) * ry * d);
            int r = (int) (rx * (0.2 + rnd.nextDouble() * 0.12));
            pts.add(new int[]{px, py, Math.max(4, r)});
        }
        g2.setColor(c[2]);
        g2.fillOval((int) (cx - rx * 0.85), (int) (cy - ry * 0.7), (int) (rx * 1.7), (int) (ry * 1.4));
        g2.setStroke(new BasicStroke(4f));
        g2.setColor(INK);
        g2.drawOval((int) (cx - rx * 0.85), (int) (cy - ry * 0.7), (int) (rx * 1.7), (int) (ry * 1.4));
        desenharBlob(g2, pts, c);
    }

    private void desenharBlob(Graphics2D g2, java.util.List<int[]> pts, Color[] c) {
        for (int[] p : pts) {
            g2.setColor(INK);
            g2.fillOval(p[0] - p[2] - 4, p[1] - p[2] - 4, (p[2] + 4) * 2, (p[2] + 4) * 2);
        }
        for (int[] p : pts) {
            g2.setColor(c[0]);
            g2.fillOval(p[0] - p[2], p[1] - p[2], p[2] * 2, p[2] * 2);
        }
        for (int[] p : pts) {
            int r2 = (int) (p[2] * 0.6);
            g2.setColor(c[2]);
            g2.fillOval((int) (p[0] + p[2] * 0.25) - r2, (int) (p[1] + p[2] * 0.3) - r2, r2 * 2, r2 * 2);
        }
        for (int[] p : pts) {
            int r3 = (int) (p[2] * 0.3);
            g2.setColor(c[1]);
            g2.fillOval((int) (p[0] - p[2] * 0.3) - r3, (int) (p[1] - p[2] * 0.3) - r3, r3 * 2, r3 * 2);
        }
    }

    private void desenharPecaOsso(Graphics2D g2, double[][] segs, Color fill) {
        Path2D path = new Path2D.Double();
        path.moveTo(segs[0][0], segs[0][1]);
        for (int i = 1; i < segs.length; i++) {
            double[] s = segs[i];
            if (s.length == 2) path.lineTo(s[0], s[1]);
            else path.curveTo(s[0], s[1], s[2], s[3], s[4], s[5]);
        }
        path.closePath();
        g2.setColor(fill);
        g2.fill(path);
        g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(INK);
        g2.draw(path);
    }

    private void desenharSamambaia(Graphics2D g2, double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, int n, Random rnd) {
        Path2D caule = new Path2D.Double();
        caule.moveTo(x0, y0);
        caule.curveTo(x1, y1, x2, y2, x3, y3);
        g2.setStroke(new BasicStroke(5f));
        g2.setColor(new Color(31, 61, 23));
        g2.draw(caule);

        Color[] tonsFolha = {new Color(63, 122, 30), new Color(90, 154, 40), new Color(46, 90, 24)};
        for (int i = 1; i <= n; i++) {
            double t = i / (double) (n + 1), u = 1 - t;
            double x = u * u * u * x0 + 3 * u * u * t * x1 + 3 * u * t * t * x2 + t * t * t * x3;
            double y = u * u * u * y0 + 3 * u * u * t * y1 + 3 * u * t * t * y2 + t * t * t * y3;
            int s = (i % 2 == 1) ? 1 : -1;
            double L = (18 + rnd.nextDouble() * 14) * (1 - t * 0.4);
            Path2D folha = new Path2D.Double();
            folha.moveTo(x, y);
            folha.quadTo(x + s * L * 0.6, y - L * 0.5, x + s * L, y - L * 0.1);
            folha.quadTo(x + s * L * 0.6, y + L * 0.3, x, y);
            g2.setColor(tonsFolha[rnd.nextInt(tonsFolha.length)]);
            g2.fill(folha);
            g2.setStroke(new BasicStroke(1.5f));
            g2.setColor(INK);
            g2.draw(folha);
        }
    }

    private void desenharVagalumes(Graphics2D g2) {
        for (double[] v : vagalumes) {
            int vx = (int) (1919 * v[0] + Math.cos(v[2]) * 35);
            int vy = (int) (1080 * v[1] + Math.sin(v[2] * 1.2) * 30);
            int alpha = (int) (120 + Math.sin(v[2]) * 85);
            g2.setColor(new Color(190, 240, 100, Math.max(25, alpha / 3)));
            g2.fillOval(vx - 10, vy - 10, 28, 28);
            g2.setColor(new Color(215, 245, 138, Math.max(60, alpha)));
            g2.fillOval(vx, vy, 8, 8);
        }
    }

    /* ================= FURÃO DETALHADO ================= */
    private void desenharFuraoDetalhado(Graphics2D g2) {
        long agora = System.currentTimeMillis();
        int pulo = 0;
        double rot = 0;
        double scaleX = 1.0, scaleY = 1.0 + Math.sin(tempoAnim) * 0.015;

        if (acaoAtual.equals("brincando") || acaoAtual.equals("pulinho")) {
            double ciclo = Math.abs(Math.sin((agora - inicioAcaoMs) * 0.006));
            pulo = (int) (-ciclo * 58);
            rot = -0.05 * ciclo;
        } else if (acaoAtual.equals("comendo")) {
            double mastiga = Math.sin((agora - inicioAcaoMs) * 0.018);
            scaleX = 1.0 + mastiga * 0.025;
            scaleY = 1.0 - mastiga * 0.03;
            rot = mastiga * 0.025;
        } else if (acaoAtual.equals("dormindo")) {
            rot = 0.06;
            pulo = 4;
        }

        int furaoX = 983;
        int furaoY = 727 + pulo;

        double sx = getWidth() / 1919.0;
        double sy = getHeight() / 1080.0;
        areaFurao = new Rectangle((int) ((furaoX - 110) * sx), (int) ((furaoY - 280) * sy), (int) (220 * sx), (int) (290 * sy));

        AffineTransform antes = g2.getTransform();
        g2.translate(furaoX, furaoY);
        g2.rotate(rot);
        g2.scale(scaleX * 1.12, scaleY * 1.12);
        g2.translate(-112, -278);

        Color creme = new Color(232, 214, 173);
        Color barriga = new Color(246, 236, 210);
        Color marrom = new Color(90, 60, 39);
        Color contorno = new Color(11, 16, 12);
        Color rosa = new Color(201, 135, 127);

        g2.setColor(new Color(0, 0, 0, 102));
        g2.fillOval(40, 270, 144, 16);

        Path2D cauda = new Path2D.Double();
        cauda.moveTo(140, 262);
        cauda.curveTo(196, 272, 214, 226, 200, 190);
        cauda.curveTo(194, 174, 180, 176, 182, 190);
        g2.setStroke(new BasicStroke(30f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(contorno);
        g2.draw(cauda);
        g2.setStroke(new BasicStroke(20f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(creme);
        g2.draw(cauda);

        Path2D pontaCauda = new Path2D.Double();
        pontaCauda.moveTo(200, 190);
        pontaCauda.curveTo(194, 174, 180, 176, 182, 190);
        g2.setColor(marrom);
        g2.draw(pontaCauda);

        Path2D corpo = new Path2D.Double();
        corpo.moveTo(112, 128);
        corpo.curveTo(158, 128, 170, 190, 160, 238);
        corpo.curveTo(156, 262, 136, 268, 112, 268);
        corpo.curveTo(88, 268, 68, 262, 64, 238);
        corpo.curveTo(54, 190, 66, 128, 112, 128);
        corpo.closePath();
        g2.setColor(creme);
        g2.fill(corpo);
        g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(contorno);
        g2.draw(corpo);

        Path2D pança = new Path2D.Double();
        pança.moveTo(112, 160);
        pança.curveTo(136, 160, 142, 200, 136, 236);
        pança.curveTo(132, 252, 122, 256, 112, 256);
        pança.curveTo(102, 256, 92, 252, 88, 236);
        pança.curveTo(82, 200, 88, 160, 112, 160);
        pança.closePath();
        g2.setColor(barriga);
        g2.fill(pança);

        g2.setColor(marrom);
        g2.fillOval(60, 256, 48, 24);
        g2.fillOval(116, 256, 48, 24);
        g2.setColor(contorno);
        g2.drawOval(60, 256, 48, 24);
        g2.drawOval(116, 256, 48, 24);

        Path2D bracoEsq = new Path2D.Double();
        bracoEsq.moveTo(82, 158);
        bracoEsq.curveTo(62, 178, 66, 204, 84, 214);
        bracoEsq.curveTo(92, 204, 90, 180, 98, 166);
        bracoEsq.closePath();
        g2.setColor(creme);
        g2.fill(bracoEsq);
        g2.setColor(contorno);
        g2.draw(bracoEsq);

        Path2D bracoDir = new Path2D.Double();
        bracoDir.moveTo(142, 158);
        bracoDir.curveTo(162, 178, 158, 204, 140, 214);
        bracoDir.curveTo(132, 204, 134, 180, 126, 166);
        bracoDir.closePath();
        g2.setColor(creme);
        g2.fill(bracoDir);
        g2.setColor(contorno);
        g2.draw(bracoDir);

        g2.setColor(marrom);
        g2.fillOval(77, 205, 22, 18);
        g2.fillOval(125, 205, 22, 18);
        g2.setColor(contorno);
        g2.drawOval(77, 205, 22, 18);
        g2.drawOval(125, 205, 22, 18);

        g2.setColor(creme);
        g2.fillOval(51, 61, 30, 30);
        g2.fillOval(143, 61, 30, 30);
        g2.setColor(contorno);
        g2.drawOval(51, 61, 30, 30);
        g2.drawOval(143, 61, 30, 30);
        g2.setColor(new Color(201, 143, 134));
        g2.fillOval(59, 71, 14, 14);
        g2.fillOval(151, 71, 14, 14);

        g2.setColor(creme);
        g2.fillOval(58, 58, 108, 88);
        g2.setColor(contorno);
        g2.drawOval(58, 58, 108, 88);

        Path2D topete = new Path2D.Double();
        topete.moveTo(92, 66);
        topete.curveTo(100, 58, 124, 58, 132, 66);
        topete.curveTo(128, 76, 96, 76, 92, 66);
        g2.setColor(marrom);
        g2.fill(topete);

        Path2D mascara = new Path2D.Double();
        mascara.moveTo(64, 100);
        mascara.curveTo(70, 84, 92, 82, 112, 94);
        mascara.curveTo(132, 82, 154, 84, 160, 100);
        mascara.curveTo(156, 116, 136, 118, 124, 110);
        mascara.curveTo(118, 106, 106, 106, 100, 110);
        mascara.curveTo(88, 118, 68, 116, 64, 100);
        g2.fill(mascara);

        g2.setColor(barriga);
        g2.fillOval(88, 108, 48, 32);

        Path2D nariz = new Path2D.Double();
        nariz.moveTo(105, 114);
        nariz.quadTo(112, 110, 119, 114);
        nariz.quadTo(116, 121, 112, 122);
        nariz.quadTo(108, 121, 105, 114);
        g2.setColor(rosa);
        g2.fill(nariz);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(contorno);
        g2.draw(nariz);

        String humor = furao.getHumor();
        boolean piscando = (agora % 5500) < 140;
        if (acaoAtual.equals("dormindo") || piscando) {
            g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(81, 100, 99, 100);
            g2.drawLine(125, 100, 143, 100);
            if (acaoAtual.equals("dormindo")) {
                g2.setColor(OSSO);
                g2.setFont(new Font("Serif", Font.ITALIC, 26));
                g2.drawString("z", 160, 50);
                g2.setFont(new Font("Serif", Font.ITALIC, 19));
                g2.drawString("z", 178, 30);
            }
        } else if (humor.equals("cansado")) {
            g2.setColor(contorno);
            g2.fillOval(81, 95, 18, 10);
            g2.fillOval(125, 95, 18, 10);
        } else {
            g2.setColor(contorno);
            g2.fillOval(81, 89, 18, 22);
            g2.fillOval(125, 89, 18, 22);
            g2.setColor(Color.WHITE);
            g2.fillOval(90, 93, 6, 6);
            g2.fillOval(134, 93, 6, 6);
        }

        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (acaoAtual.equals("comendo")) {
            g2.setColor(new Color(58, 20, 20));
            g2.fillOval(104, 124, 16, 14);
            g2.setColor(contorno);
            g2.drawOval(104, 124, 16, 14);
        } else if (humor.equals("feliz") && !acaoAtual.equals("dormindo")) {
            g2.setColor(contorno);
            g2.drawArc(100, 123, 12, 10, 180, 180);
            g2.drawArc(112, 123, 12, 10, 180, 180);
        } else {
            g2.setColor(contorno);
            g2.drawArc(101, 127, 22, 10, 0, 180);
        }

        g2.setTransform(antes);
    }

    /* ================= TIGELA E BOLA COM ANIMAÇÃO DE QUEDA ================= */
    private void desenharPropAcao(Graphics2D g2) {
        if (!acaoAtual.equals("comendo") && !acaoAtual.equals("brincando")) return;

        long decorrido = System.currentTimeMillis() - inicioAcaoMs;
        double quedaY = 0;
        if (decorrido < 350) {
            double t = 1.0 - (decorrido / 350.0);
            quedaY = -140 * t * t;
        } else if (decorrido < 550) {
            double t = (decorrido - 350) / 200.0;
            quedaY = -Math.sin(t * Math.PI) * 22;
        }

        AffineTransform antes = g2.getTransform();
        g2.translate(1125, 665 + quedaY);
        g2.scale(1.3, 1.3);

        g2.setColor(new Color(0, 0, 0, 102));
        g2.fillOval(8, 49, 64, 10);
        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (acaoAtual.equals("comendo")) {
            g2.setColor(new Color(168, 116, 63));
            g2.fillOval(19, 17, 14, 14);
            g2.fillOval(47, 17, 14, 14);
            g2.setColor(new Color(140, 93, 49));
            g2.fillOval(33, 13, 14, 14);
            g2.setColor(new Color(111, 122, 102));
            g2.fillArc(8, 8, 64, 40, 180, 180);
            g2.setColor(new Color(11, 16, 12));
            g2.drawArc(8, 8, 64, 40, 180, 180);
        } else {
            int quique = (int) (-Math.abs(Math.sin(decorrido * 0.01)) * 22);
            g2.setColor(SEDA);
            g2.fillOval(18, 10 + quique, 44, 44);
            g2.setColor(new Color(11, 16, 12));
            g2.drawOval(18, 10 + quique, 44, 44);
        }

        g2.setTransform(antes);
    }

    private void desenharFala(Graphics2D g2) {
        if (falaAtual.isEmpty()) return;
        g2.setFont(new Font("Serif", Font.ITALIC, 30));
        FontMetrics fm = g2.getFontMetrics();
        int tx = 983 - fm.stringWidth(falaAtual) / 2;
        int ty = 378;
        g2.setColor(Color.BLACK);
        g2.drawString(falaAtual, tx + 2, ty + 2);
        g2.setColor(OSSO);
        g2.drawString(falaAtual, tx, ty);
    }

    /* ================= HUD, BOTÕES E MODAIS ================= */
    private void desenharHUD(Graphics2D g2, int w, int h) {
        g2.setColor(SEDA);
        g2.fillRect(0, 0, (int) (w * (furao.getIdade() / (double) Furao.ANOS_PARA_VENCER)), 4);

        g2.setColor(OSSO);
        g2.setFont(new Font("Serif", Font.BOLD, 21));
        g2.drawString(furao.getNome().toUpperCase(), 20, 34);

        boolean flash = System.currentTimeMillis() < flashIdadeMs;
        g2.setColor(flash ? MUSGO : OSSO);
        g2.setFont(new Font("Serif", Font.PLAIN, 13));
        g2.drawString(furao.getIdade() + " / " + Furao.ANOS_PARA_VENCER + " ANOS", 20, 52);

        desenharBarra(g2, "FOME", furao.getFome(), new Color(224, 168, 60), furao.getFome() >= 75, 20, 74);
        desenharBarra(g2, "FELICIDADE", furao.getFelicidade(), MUSGO, furao.getFelicidade() <= 25, 20, 96);
        desenharBarra(g2, "CANSAÇO", furao.getCansaco(), new Color(143, 180, 207), furao.getCansaco() >= 75, 20, 118);

        btnSom = new Rectangle(w - 115, 16, 95, 28);
        g2.setColor(new Color(8, 12, 9, 190));
        g2.fillRoundRect(btnSom.x, btnSom.y, btnSom.width, btnSom.height, 14, 14);
        g2.setColor(new Color(232, 230, 212, 130));
        g2.drawRoundRect(btnSom.x, btnSom.y, btnSom.width, btnSom.height, 14, 14);
        g2.setFont(new Font("Serif", Font.PLAIN, 12));
        g2.setColor(somAtivo ? MUSGO : OSSO);
        g2.drawString(somAtivo ? "SOM: ON (M)" : "SOM: OFF (M)", btnSom.x + 11, btnSom.y + 18);
    }

    private void desenharBarra(Graphics2D g2, String label, int valor, Color cor, boolean perigo, int x, int y) {
        g2.setFont(new Font("Serif", Font.PLAIN, 12));
        g2.setColor(perigo ? new Color(255, 154, 138) : OSSO);
        g2.drawString(label, x, y);

        int bx = x + 90, bw = 140;
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(bx, y - 9, bw, 9);

        Color corBarra = cor;
        if (perigo && (System.currentTimeMillis() / 350) % 2 == 0) {
            corBarra = cor.brighter();
        }
        g2.setColor(corBarra);
        g2.fillRect(bx + 1, y - 8, (int) ((bw - 2) * (valor / 100.0)), 7);
        g2.setColor(new Color(232, 230, 212, 140));
        g2.drawRect(bx, y - 9, bw, 9);
    }

    private void desenharBotoes(Graphics2D g2, int w, int h) {
        String[] nomes = {"Alimentar", "Brincar", "Descansar", "Status"};
        int raio = 30, espaco = 104;
        int inicioX = w / 2 - (espaco * 3) / 2;
        int cy = h - 58;

        for (int i = 0; i < 4; i++) {
            int cx = inicioX + i * espaco;
            btnAcoes[i] = new Rectangle(cx - raio, cy - raio, raio * 2, raio * 2 + 24);

            boolean bloqueado = !acaoAtual.equals("nenhuma") && i != 3;
            boolean hover = (botaoHover == i) && !bloqueado;

            if (hover) {
                g2.setColor(new Color(155, 227, 29, 45));
                g2.fillOval(cx - raio - 6, cy - raio - 6, (raio + 6) * 2, (raio + 6) * 2);
            }

            g2.setPaint(new RadialGradientPaint(
                    new Point2D.Float(cx, cy - 8), raio,
                    new float[]{0f, 1f},
                    new Color[]{bloqueado ? new Color(16, 22, 15, 140) : new Color(22, 33, 15, 240), TINTA}
            ));
            g2.fillOval(cx - raio, cy - raio, raio * 2, raio * 2);

            g2.setStroke(new BasicStroke(2f));
            g2.setColor(bloqueado ? new Color(232, 230, 212, 85) : (hover ? MUSGO : OSSO));
            g2.drawOval(cx - raio, cy - raio, raio * 2, raio * 2);

            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (i == 0) {
                g2.drawArc(cx - 11, cy - 10, 22, 18, 180, 180);
                g2.drawLine(cx - 11, cy - 1, cx + 11, cy - 1);
                g2.fillOval(cx - 6, cy - 6, 3, 3);
                g2.fillOval(cx - 1, cy - 8, 3, 3);
                g2.fillOval(cx + 4, cy - 6, 3, 3);
            } else if (i == 1) {
                g2.drawOval(cx - 10, cy - 10, 20, 20);
                g2.drawArc(cx - 14, cy - 6, 16, 16, 0, 90);
            } else if (i == 2) {
                g2.drawArc(cx - 9, cy - 9, 18, 18, 45, 235);
                g2.drawArc(cx - 3, cy - 11, 14, 14, 75, 190);
            } else {
                g2.drawArc(cx - 12, cy - 7, 24, 14, 0, 180);
                g2.drawArc(cx - 12, cy - 7, 24, 14, 180, 180);
                g2.drawOval(cx - 4, cy - 4, 8, 8);
            }

            g2.setFont(new Font("Serif", Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            String rotulo = nomes[i].toUpperCase();
            g2.drawString(rotulo, cx - fm.stringWidth(rotulo) / 2, cy + raio + 19);
        }
    }

    private void desenharModalStatus(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(3, 6, 4, 200));
        g2.fillRect(0, 0, w, h);

        int mw = 360, mh = 270;
        int mx = (w - mw) / 2, my = (h - mh) / 2;
        g2.setColor(new Color(8, 12, 9, 245));
        g2.fillRect(mx, my, mw, mh);
        g2.setColor(OSSO);
        g2.drawRect(mx, my, mw, mh);

        g2.setFont(new Font("Serif", Font.BOLD, 21));
        g2.drawString(furao.getNome().toUpperCase(), mx + 26, my + 40);
        g2.setFont(new Font("Serif", Font.ITALIC, 14));
        g2.drawString(furao.getIdade() + " de " + Furao.ANOS_PARA_VENCER + " anos", mx + 26, my + 62);

        g2.setFont(new Font("Serif", Font.PLAIN, 15));
        g2.drawString("Fome: " + furao.getFome() + " / 100", mx + 26, my + 104);
        g2.drawString("Felicidade: " + furao.getFelicidade() + " / 100", mx + 26, my + 130);
        g2.drawString("Cansaço: " + furao.getCansaco() + " / 100", mx + 26, my + 156);

        g2.setFont(new Font("Serif", Font.ITALIC, 14));
        String obs = furao.getFome() >= 70 ? "Está com muita fome."
                : furao.getCansaco() >= 70 ? "Está exausto."
                : furao.getFelicidade() <= 30 ? "Anda triste e quer atenção."
                : "Está bem, quieto no banco, entre o musgo e a luz.";
        g2.drawString(obs, mx + 26, my + 196);
        g2.drawString("Clique ou pressione S para fechar", mx + 26, my + 236);
    }

    private void desenharModalFim(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(3, 6, 4, 215));
        g2.fillRect(0, 0, w, h);

        int mw = 440, mh = 210;
        int mx = (w - mw) / 2, my = (h - mh) / 2;
        g2.setColor(new Color(8, 12, 9, 245));
        g2.fillRect(mx, my, mw, mh);
        g2.setColor(furao.isVenceu() ? MUSGO : SEDA);
        g2.drawRect(mx, my, mw, mh);

        g2.setColor(OSSO);
        g2.setFont(new Font("Serif", Font.BOLD, 22));
        g2.drawString(furao.isVenceu() ? "UMA VIDA INTEIRA" : "FIM DA JORNADA", mx + 26, my + 46);

        g2.setFont(new Font("Serif", Font.ITALIC, 15));
        g2.drawString(furao.getMensagemFim(), mx + 26, my + 98);

        g2.setFont(new Font("Serif", Font.PLAIN, 13));
        g2.drawString("Clique em qualquer lugar para jogar de novo", mx + 26, my + 168);
    }
}
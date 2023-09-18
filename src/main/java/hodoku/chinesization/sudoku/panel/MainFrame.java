/*
 * Copyright (C) 2008-12  Bernhard Hobiger
 *
 * This file is part of HoDoKu.
 *
 * HoDoKu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HoDoKu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HoDoKu. If not, see <http://www.gnu.org/licenses/>.
 */
package hodoku.chinesization.sudoku.panel;


import hodoku.chinesization.generator.BackgroundGeneratorThread;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;
import hodoku.chinesization.solver.SudokuSolver;
import hodoku.chinesization.solver.SudokuSolverFactory;
import hodoku.chinesization.sudoku.*;
import hodoku.chinesization.sudoku.dialog.*;
import hodoku.chinesization.sudoku.emuns.ClipboardMode;
import hodoku.chinesization.sudoku.entity.*;
import hodoku.chinesization.sudoku.progress.GenerateSudokuProgressDialog;
import hodoku.chinesization.sudoku.progress.SummaryPanel;
import hodoku.chinesization.upgrade.PropertyUtil;
import hodoku.chinesization.sudoku.util.MyBrowserLauncher;
import hodoku.chinesization.sudoku.Options;
import hodoku.chinesization.sudoku.util.SudokuUtil;
import hodoku.chinesization.sudoku.util.WriteAsPNGDialog;
import learn.framework.ioc.annotaion.Component;
import learn.framework.ioc.annotaion.Value;

/**
 *
 * @author  hobiwan
 */
@Component
public class MainFrame extends JFrame implements FlavorListener {

    private static final long serialVersionUID = 1L;
    // 版本信息
    public static final String VERSION = "HoDoKu - v2.3.0 beta";
    //public static final String BUILD = "Build 16"; // 构建信息
    public static final String BUILD; // 构建信息
    public static final String REV = "$LastChangedRevision: 126 $"; // SVN版本信息
    /** The size of the toggle button icons */
    // 切换按钮图标尺寸
    private static final int TOGGLE_BUTTON_ICON_SIZE = 32;
    // 数独面板
    private SudokuPanel sudokuPanel;
    // 难度级别
    //private DifficultyLevel level = Options.getInstance().getDifficultyLevels()[DifficultyType.EASY.ordinal()];
    // 切换按钮数组
    private JToggleButton[] toggleButtons = new JToggleButton[10];
    /** Icons for the filter toggle buttons in the toolbar (original version) */
    // 工具栏中切换按钮的图标（原始版本）
    private Icon[] toggleButtonIconsOrg = new Icon[10];
    /** Icons for the filter toggle buttons in the toolbar (original version) */
    // 工具栏中切换按钮的空图标（原始版本）
    private Icon[] emptyToggleButtonIconsOrg = new Icon[10];
    /** Images for the filter toggle button icons (ColorKu version) */
    // 工具栏中切换按钮的图标（ColorKu版本）
    private ColorKuImage[] toggleButtonImagesColorKu = new ColorKuImage[10];
    /** Icons for the filter toggle buttons in the toolbar (ColorKu version) */
    // 工具栏中切换按钮的图标（ColorKu版本）
    private Icon[] toggleButtonIconsColorKu = new Icon[10];
    /** Icons for the filter toggle buttons in the toolbar (currently displayed) */
    // 工具栏中切换按钮的图标（当前显示）
    private Icon[] toggleButtonIcons = new Icon[10];
    /** Icons for the filter toggle buttons in the toolbar (no candidates left) */
    // 工具栏中切换按钮的空图标（没有候选项）
    private Icon[] emptyToggleButtonIcons = new Icon[10];
    /** One empty icon for disabled filter buttons - digits */
    // 禁用的过滤按钮的空图标 - 数字
    private Icon emptyToggleButtonIconOrg = new ImageIcon(getClass().getResource("/img/f_0c.png"));
    /** One empty icon for disabled filter buttons */
    // 禁用的过滤按钮的空图标
    private Icon emptyToggleButtonIconOrgColorKu = new ImageIcon(new ColorKuImage(TOGGLE_BUTTON_ICON_SIZE, Color.WHITE));
    //    /** One empty icon for disabled filter buttons */
//    private Icon emptyToggleButtonIcon = emptyToggleButtonIconOrg;
    // 难度级别菜单项
    private JRadioButtonMenuItem[] levelMenuItems = new JRadioButtonMenuItem[5];
    // 游戏模式菜单项
    private JRadioButtonMenuItem[] modeMenuItems;
    // 是否显示偏差
    private boolean oldShowDeviations = true;
    /** only set, when the givens are changed */
    // 只有给定值改变时才设置
    private boolean oldShowDeviationsValid = false;
    /** true if new givens are being entered */
    // 是否处于输入模式。如果正在输入新的给定值，则为True
    private boolean eingabeModus = false;

    // ---------------------------------------------------- 依赖注入
    // 分割面板
    private SplitPanel splitPanel = new SplitPanel();
    // 摘要面板
    private SummaryPanel summaryPanel = new SummaryPanel(this);
    // 解决方案面板
    private SolutionPanel solutionPanel = new SolutionPanel(this);
    // 所有步骤面板
    private AllStepsPanel allStepsPanel = new AllStepsPanel(this, null);
    // 单元格缩放面板
    private CellZoomPanel cellZoomPanel = new CellZoomPanel(this);
    // 选项卡面板
    private JTabbedPane tabPane = new JTabbedPane();
    // 页面格式
    private PageFormat pageFormat = null;
    // 打印作业
    private PrinterJob job = null;
    // 图像大小
    private double bildSize = 400;
    // 图像分辨率
    private int bildAufloesung = 96;
    // 图像单位
    private int bildEinheit = 2;
    // File/IO 数独文件保存过滤器
    private MyFileFilter[] puzzleFileSaveFilters = new MyFileFilter[]{
            new MyFileFilter(1), new MyFileFilter(2), new MyFileFilter(3),
            new MyFileFilter(4), new MyFileFilter(5),  new MyFileFilter(6),
            // FIXME 添加了一个 new MyFileFilter(8)，不确定是否存在 bug
            new MyFileFilter(7), new MyFileFilter(8), new MyFileFilter(9)
    };
    // 数独文件加载过滤器
    private MyFileFilter[] puzzleFileLoadFilters = new MyFileFilter[]{
            new MyFileFilter(1),
            new MyFileFilter(8),
            new MyFileFilter(9)
    };
    // 配置文件过滤器
    private MyFileFilter[] configFileFilters = new MyFileFilter[]{
            new MyFileFilter(0)
    };
    // 插入符号监听器
    private MyCaretListener caretListener = new MyCaretListener();
    // 是否初始化外部分割面板
    private boolean outerSplitPaneInitialized = false;
    // 重置水平分隔栏位置
    private int resetHDivLocLoc = -1;
    // 更改后调整分隔位置
    private boolean resetHDivLoc = false;
    // 仅在一秒钟内调整
    private long resetHDivLocTicks = 0;
    // PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.config_file_ext")
    // 配置文件扩展名
    private String configFileExt = PropertyUtil.getProperty("intl/MainFrame", "MainFrame.config_file_ext");
    // 解决方案文件扩展名
    private String solutionFileExt = PropertyUtil.getProperty("intl/MainFrame", "MainFrame.solution_file_ext");
    // 文本文件扩展名
    private String textFileExt = PropertyUtil.getProperty("intl/MainFrame", "MainFrame.text_file_ext");
    // SS文件扩展名
    private String ssFileExt = PropertyUtil.getProperty("intl/MainFrame", "MainFrame.ss_file_ext");
    // 消息格式
    private MessageFormat formatter = new MessageFormat("");
    // 存储保存点的容器
    private List<GuiState> savePoints = new ArrayList<GuiState>();
    // 游戏模式
    //private GameMode mode = GameMode.PLAYING;
    // 是否在切换全屏模式
    private boolean changingFullScreenMode = false;
    /** Array with images for progressLabel */
    // 进度标签的图像数组
    private ImageIcon[] progressImages = new ImageIcon[Options.DEFAULT_DIFFICULTY_LEVELS.length];
    /** For progress label: current difficulty level; access must be synchronized! */
    // 当前难度级别
    private DifficultyLevel currentLevel = null;
    /** For progress label: current score; access must be synchronized! */
    // 当前分数
    private int currentScore = 0;
    /** Vage hint button in toolbar */
    // 工具栏中的模糊提示按钮
    private JButton vageHintToggleButton = null;
    /** Concrete hint button in toolbar */
    // 工具栏中的具体提示按钮
    private JButton concreteHintToggleButton = null;
    /** Show next step button in toolbar */
    // 工具栏中的显示下一步按钮
    private JButton showNextStepToggleButton = null;
    /** Execute next step button in toolbar */
    // 工具栏中的执行下一步按钮
    private JButton executeStepToggleButton = null;
    /** Abort step button in toolbar */
    // 工具栏中的中止步骤按钮
    private JButton abortStepToggleButton = null;
    /** Seperator for hint buttons in toolbar */
    // 工具栏中提示按钮的分隔符
    private JSeparator hintSeperator = null;
    /** A Timer for accessing the clipboard. If the Clipboard is not available when
     * querying for DataFlavors, it is started an we try again when it expires.
     */
    // 访问剪贴板的计时器。如果在查询DataFlavors时剪贴板不可用，则启动它，当它过期时再试一次。
    private Timer clipTimer = new Timer(100, null);
    /** The file name of the last loded sudoku file */
    // 上次加载的数独文件名
    private String sudokuFileName = null;
     /** The file type of the last loaded sudoku file: 1 .. hsol, 8 .. txt or 9 .. ss */
     // 最后加载的数独文件类型: 1 .. hsol, 8 .. txt or 9 .. ss
    private int sudokuFileType = -1;

    /** Incorporates the last subversion revision of this file into
     *  the version string.<br><br>
     *
     * CAUTION: MainFrame.java must be changed and committed to change
     * the build number.
     */
    static {
        String[] dummy = REV.split(" ");
        BUILD = "Build " + dummy[1];
    }

    public MainFrame() throws HeadlessException {

    }

    /** Creates new form MainFrame
     * @param launchFile
     */
    @SuppressWarnings({"LeakingThisInConstructor", "unchecked"})
    public MainFrame(String launchFile) {
        // if a configuration file is given at the command line, load it before anything
        // else is done (helps restoring the screen layout)
        if (launchFile != null && launchFile.endsWith("." + configFileExt)) {
            Options.readOptions(launchFile);
            BackgroundGeneratorThread.getInstance().resetAll();
        }
        Options.getInstance().checkAllFonts();

        initComponents();
        setTitleWithFile();
        outerSplitPane.getActionMap().getParent().remove("startResize");
        outerSplitPane.getActionMap().getParent().remove("toggleFocus");

        // change hintTextArea font to a proportional font
        String fontName = "Arial";
        if (!Options.getInstance().checkFont(fontName)) {
            fontName = Font.SANS_SERIF;
        }

        Font font = hinweisTextArea.getFont();
//        System.out.println("fontSize: " + font.getSize() + "/" + getFont().getSize() + "/" + bearbeitenMenu.getFont().getSize());
//        font = new Font(fontName, font.getStyle(), font.getSize());
        font = new Font(fontName, font.getStyle(), bearbeitenMenu.getFont().getSize());

        // status line fonts are a bit larger than default in Windows LAF
        // allow adjustments
        // 状态行字体比Windows LAF中的默认字体大一点
        // 允许调整
        font = statusLinePanel.getFont();
        fontName = "MS Song";
        if (!Options.getInstance().checkFont(fontName)) {
            fontName = font.getName();
        }
        int fontSize = 12;
        if (font.getSize() > fontSize) {
            fontSize = font.getSize();
        }
        font = new Font(fontName, getFont().getStyle(), fontSize);

        statusLabelModus.setFont(font);
        progressLabel.setFont(font);

        // get the current difficulty level (is overriden when levels are added
        //to the combo box)
        int actLevel = Options.getInstance().getActLevel();

        Color lafMenuBackColor = UIManager.getColor("textHighlight");
        Color lafMenuColor = UIManager.getColor("textHighlightText");
//        Color lafMenuInactiveColor = UIManager.getColor("textInactiveText");
        if (lafMenuBackColor == null) {
            lafMenuBackColor = Color.BLUE;
        }
        if (lafMenuColor == null) {
            lafMenuColor = Color.BLACK;
        }
//        if (lafMenuInactiveColor == null) {
//            lafMenuInactiveColor = Color.WHITE;
//        }
        statusLinePanel.setBackground(lafMenuBackColor);
        statusLabelLevel.setForeground(lafMenuColor);
        summaryPanel.setTitleLabelColors(lafMenuColor, lafMenuBackColor);
        solutionPanel.setTitleLabelColors(lafMenuColor, lafMenuBackColor);
        cellZoomPanel.setTitleLabelColors(lafMenuColor, lafMenuBackColor);

        statusLabelModus.setText(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.playingMenuItem.text"));

//        UIDefaults def = UIManager.getDefaults();
//        Enumeration defEnum = def.keys();
//        SortedSet<String> defSet = new TreeSet<String>();
//        while (defEnum.hasMoreElements()) {
//            Object tmp = defEnum.nextElement();
//            if (tmp instanceof String) {
//                defSet.add((String)tmp);
//            }
//        }
//        for (String key : defSet) {
//            System.out.println(key + ": " + def.get(key));
//        }

        clipTimer.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clipTimer.stop();
                adjustPasteMenuItem();
            }
        });
        try {
            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            clip.addFlavorListener(this);
            adjustPasteMenuItem();
        } catch (IllegalStateException ex) {
            clipTimer.start();
        }



        sudokuPanel = new SudokuPanel(this);
        sudokuPanel.setCellZoomPanel(cellZoomPanel);
        cellZoomPanel.setSudokuPanel(sudokuPanel);
        outerSplitPane.setLeftComponent(splitPanel);
        splitPanel.setSplitPane(sudokuPanel, null);
        System.setProperty("file.encoding", "UTF-8");



        tabPane.addTab(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.summary"), summaryPanel);
        tabPane.addTab(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.solution_path"), solutionPanel);
        tabPane.addTab(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.all_steps"), allStepsPanel);
        tabPane.addTab(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.cell_zoom"), cellZoomPanel);



        tabPane.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                tabPaneMouseClicked(evt);
            }
        });

        if (Options.getInstance().isSaveWindowLayout()) {
            setWindowLayout(false);
        } else {
            setWindowLayout(true);
        }
        outerSplitPaneInitialized = false;
        if (Options.getInstance().getInitialXPos() != -1 && Options.getInstance().getInitialYPos() != -1) {
            Toolkit t = Toolkit.getDefaultToolkit();
            Dimension screenSize = t.getScreenSize();
            int x = Options.getInstance().getInitialXPos();
            int y = Options.getInstance().getInitialYPos();
            if (x + getWidth() > screenSize.width) {
                x = screenSize.width - getWidth() - 10;
            }
            if (y + getHeight() > screenSize.height) {
                y = screenSize.height - getHeight() - 10;
            }
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            setLocation(x, y);
        }
        showHintPanelMenuItem.setSelected(Options.getInstance().isShowHintPanel());
        showToolBarMenuItem.setSelected(Options.getInstance().isShowToolBar());

        // Level-Menüs und Combo-Box
        levelMenuItems[0] = levelLeichtMenuItem;
        levelMenuItems[1] = levelMittelMenuItem;
        levelMenuItems[2] = levelKniffligMenuItem;
        levelMenuItems[3] = levelSchwerMenuItem;
        levelMenuItems[4] = levelExtremMenuItem;
        FontMetrics metrics = levelComboBox.getFontMetrics(levelComboBox.getFont());
        int miWidth = 0;
        int miHeight = metrics.getHeight();
        Set<Character> mnemonics = new HashSet<Character>();
        for (int i = 1; i < DifficultyType.values().length; i++) {
            levelMenuItems[i - 1].setText(Options.getInstance().getDifficultyLevels()[i].getName());
            char mnemonic = 0;
            boolean mnemonicFound = false;
            for (int j = 0; j < Options.getInstance().getDifficultyLevels()[i].getName().length(); j++) {
                mnemonic = Options.getInstance().getDifficultyLevels()[i].getName().charAt(j);
                if (!mnemonics.contains(mnemonic)) {
                    mnemonicFound = true;
                    break;
                }
            }
            if (mnemonicFound) {
                mnemonics.add(mnemonic);
                levelMenuItems[i - 1].setMnemonic(mnemonic);
            }
            levelComboBox.addItem(Options.getInstance().getDifficultyLevels()[i].getName());
            int aktWidth = metrics.stringWidth(Options.getInstance().getDifficultyLevels()[i].getName());
            //System.out.println(Options.getInstance().getDifficultyLevels()[i].getName() + ": " + aktWidth);
            if (aktWidth > miWidth) {
                miWidth = aktWidth;
            }
        }
        mnemonics = null;

        // mode menu items
        // 模式菜单项
        modeMenuItems = new JRadioButtonMenuItem[]{
            playingMenuItem,
            learningMenuItem,
            practisingMenuItem
        };

        // in Windows miWidth = 35, miHeight = 14; size = 60/20
        if (miWidth > 35) {
            Dimension newLevelSize = new Dimension(60 + (miWidth - 35) + 8, 20 + (miHeight - 14) + 3);
            levelComboBox.setMaximumSize(newLevelSize);
            levelComboBox.setMinimumSize(newLevelSize);
            levelComboBox.setPreferredSize(newLevelSize);
            levelComboBox.setSize(newLevelSize);
            //System.out.println("Size changed to: " + newLevelSize);
            //jToolBar1.doLayout();
            //repaint();
        }

        // set back the saved difficulty level
        // 设置保存的难度级别
        Options.getInstance().setActLevel(actLevel);

        // Menüzustand prüfen, übernimmt Werte von SudokuPanel; muss am Anfang stehen,
        // weil die Werte später in der Methode verwendet werden
        check();

        // Die ToggleButtons in ein Array stecken, ist später einfacher
        toggleButtons[0] = f1ToggleButton;
        toggleButtons[1] = f2ToggleButton;
        toggleButtons[2] = f3ToggleButton;
        toggleButtons[3] = f4ToggleButton;
        toggleButtons[4] = f5ToggleButton;
        toggleButtons[5] = f6ToggleButton;
        toggleButtons[6] = f7ToggleButton;
        toggleButtons[7] = f8ToggleButton;
        toggleButtons[8] = f9ToggleButton;
        toggleButtons[9] = fxyToggleButton;
        for (int i = 0, lim = toggleButtons.length; i < lim; i++) {
            toggleButtonIconsOrg[i] = toggleButtons[i].getIcon();
            toggleButtonIcons[i] = toggleButtons[i].getIcon();
            if (i < 9) {
                emptyToggleButtonIconsOrg[i] = new ImageIcon(getClass().getResource("/img/f_" + (i + 1) + "c_inactive.png"));
            } else {
                emptyToggleButtonIcons[i] = toggleButtons[i].getIcon();
            }
        }
        setToggleButton(null, false);
        prepareToggleButtonIcons(Options.getInstance().isShowColorKu());

        // initialize colorKuMeniItem
        showColorKuMenuItem.setSelected(Options.getInstance().isShowColorKu());
        Options.getInstance().setShowColorKuAct(Options.getInstance().isShowColorKu());

        // Caret-Listener for display of Forcing Chains
        hinweisTextArea.addCaretListener(caretListener);

        // Images for progressLabel
        createProgressLabelImages();
        progressLabel.setIcon(progressImages[0]);

        // set the mode
        setMode(Options.getInstance().getGameMode(), false);

        // show hint buttons in toolbar
        setShowHintButtonsInToolbar();

        // if a puzzle file is given at the command line, load it
        if (launchFile != null && launchFile.endsWith("." + solutionFileExt)) {
            loadFromFile(launchFile, 1);
        }
        if (launchFile != null && launchFile.endsWith("." + textFileExt)) {
            loadFromFile(launchFile, 8);
        }

        fixFocus();

        // Start background creation
        BackgroundGeneratorThread.getInstance().startCreation();

//        Color bg = UIManager.getColor("List.selectionBackground");
//        System.out.println("List.selectionBackground = " + bg);
//        bg = UIManager.getColor("List[Selected].textBackground");
//        System.out.println("List[Selected].textBackground = " + bg);
//        bg = UIManager.getColor("List.selectionForeground");
//        System.out.println("List.selectionForeground = " + bg);
//        bg = UIManager.getColor("List[Selected].textForeground");
//        System.out.println("List[Selected].textForeground = " + bg);
//        bg = UIManager.getColor("List.background");
//        System.out.println("List.background = " + bg);
//        bg = UIManager.getColor("List.foreground");
//        System.out.println("List.foreground = " + bg);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        levelButtonGroup = new ButtonGroup();
        viewButtonGroup = new ButtonGroup();
        colorButtonGroup = new ButtonGroup();
        modeButtonGroup = new ButtonGroup();
        statusLinePanel = new JPanel();
        statusPanelColorResult = new JPanel();
        jPanel1 = new JPanel();
        statusPanelColor1 = new StatusColorPanel(0);
        statusPanelColor2 = new StatusColorPanel(2);
        statusPanelColor3 = new StatusColorPanel(4);
        statusPanelColor4 = new StatusColorPanel(6);
        statusPanelColor5 = new StatusColorPanel(8);
        statusPanelColorClear = new StatusColorPanel(-1);
        statusPanelColorReset = new StatusColorPanel(-2);
        statusLabelCellCandidate = new JLabel();
        jSeparator1 = new JSeparator();
        statusLabelLevel = new JLabel();
        jSeparator8 = new JSeparator();
        progressLabel = new JLabel();
        jSeparator24 = new JSeparator();
        statusLabelModus = new JLabel();
        jToolBar1 = new JToolBar();
        undoToolButton = new JButton();
        redoToolButton = new JButton();
        jSeparator9 = new JSeparator();
        neuesSpielToolButton = new JButton();
        jSeparator12 = new JSeparator();
        levelComboBox = new JComboBox();
        jSeparator13 = new JSeparator();
        jSeparator11 = new JSeparator();
        redGreenToggleButton = new JToggleButton();
        f1ToggleButton = new JToggleButton();
        f2ToggleButton = new JToggleButton();
        f3ToggleButton = new JToggleButton();
        f4ToggleButton = new JToggleButton();
        f5ToggleButton = new JToggleButton();
        f6ToggleButton = new JToggleButton();
        f7ToggleButton = new JToggleButton();
        f8ToggleButton = new JToggleButton();
        f9ToggleButton = new JToggleButton();
        fxyToggleButton = new JToggleButton();
        outerSplitPane = new JSplitPane();
        hintPanel = new JPanel();
        neuerHinweisButton = new JButton();
        hinweisAusfuehrenButton = new JButton();
        solveUpToButton = new JButton();
        hinweisAbbrechenButton = new JButton();
        jScrollPane1 = new JScrollPane();
        hinweisTextArea = new JTextArea();
        jMenuBar1 = new JMenuBar();
        dateiMenu = new JMenu();
        neuMenuItem = new JMenuItem();
        jSeparator39 = new JPopupMenu.Separator();
        loadPuzzleMenuItem = new JMenuItem();
        savePuzzleMenuItem = new JMenuItem();
        savePuzzleAsMenuItem = new JMenuItem();
        loadConfigMenuItem = new JMenuItem();
        saveConfigAsMenuItem = new JMenuItem();
        jSeparator38 = new JPopupMenu.Separator();
        seiteEinrichtenMenuItem = new JMenuItem();
        druckenMenuItem = new JMenuItem();
        extendedPrintMenuItem = new JMenuItem();
        speichernAlsBildMenuItem = new JMenuItem();
        jSeparator37 = new JPopupMenu.Separator();
        spielEingebenMenuItem = new JMenuItem();
        spielEditierenMenuItem = new JMenuItem();
        spielSpielenMenuItem = new JMenuItem();
        jSeparator36 = new JPopupMenu.Separator();
        beendenMenuItem = new JMenuItem();
        bearbeitenMenu = new JMenu();
        undoMenuItem = new JMenuItem();
        redoMenuItem = new JMenuItem();
        jSeparator35 = new JPopupMenu.Separator();
        copyCluesMenuItem = new JMenuItem();
        copyFilledMenuItem = new JMenuItem();
        copyPmGridMenuItem = new JMenuItem();
        copyPmGridWithStepMenuItem = new JMenuItem();
        copyLibraryMenuItem = new JMenuItem();
        copySSMenuItem = new JMenuItem();
        pasteMenuItem = new JMenuItem();
        jSeparator34 = new JPopupMenu.Separator();
        restartSpielMenuItem = new JMenuItem();
        resetSpielMenuItem = new JMenuItem();
        jSeparator33 = new JPopupMenu.Separator();
        configMenuItem = new JMenuItem();
        modeMenu = new JMenu();
        playingMenuItem = new JRadioButtonMenuItem();
        learningMenuItem = new JRadioButtonMenuItem();
        practisingMenuItem = new JRadioButtonMenuItem();
        optionenMenu = new JMenu();
        showCandidatesMenuItem = new JCheckBoxMenuItem();
        showWrongValuesMenuItem = new JCheckBoxMenuItem();
        showDeviationsMenuItem = new JCheckBoxMenuItem();
        showColorKuMenuItem = new JCheckBoxMenuItem();
        jSeparator32 = new JPopupMenu.Separator();
        colorCellsMenuItem = new JRadioButtonMenuItem();
        colorCandidatesMenuItem = new JRadioButtonMenuItem();
        jSeparator31 = new JPopupMenu.Separator();
        levelMenu = new JMenu();
        levelLeichtMenuItem = new JRadioButtonMenuItem();
        levelMittelMenuItem = new JRadioButtonMenuItem();
        levelKniffligMenuItem = new JRadioButtonMenuItem();
        levelSchwerMenuItem = new JRadioButtonMenuItem();
        levelExtremMenuItem = new JRadioButtonMenuItem();
        raetselMenu = new JMenu();
        vageHintMenuItem = new JMenuItem();
        mediumHintMenuItem = new JMenuItem();
        loesungsSchrittMenuItem = new JMenuItem();
        jSeparator30 = new JPopupMenu.Separator();
        backdoorSearchMenuItem = new JMenuItem();
        historyMenuItem = new JMenuItem();
        createSavePointMenuItem = new JMenuItem();
        restoreSavePointMenuItem = new JMenuItem();
        jSeparator29 = new JPopupMenu.Separator();
        setGivensMenuItem = new JMenuItem();
        jSeparator28 = new JPopupMenu.Separator();
        alleHiddenSinglesSetzenMenuItem = new JMenuItem();
        ansichtMenu = new JMenu();
        sudokuOnlyMenuItem = new JRadioButtonMenuItem();
        jSeparator20 = new JPopupMenu.Separator();
        summaryMenuItem = new JRadioButtonMenuItem();
        solutionMenuItem = new JRadioButtonMenuItem();
        allStepsMenuItem = new JRadioButtonMenuItem();
        cellZoomMenuItem = new JRadioButtonMenuItem();
        jSeparator6 = new JPopupMenu.Separator();
        showHintPanelMenuItem = new JCheckBoxMenuItem();
        showToolBarMenuItem = new JCheckBoxMenuItem();
        showHintButtonsCheckBoxMenuItem = new JCheckBoxMenuItem();
        fullScreenMenuItem = new JCheckBoxMenuItem();
        jSeparator23 = new JPopupMenu.Separator();
        resetViewMenuItem = new JMenuItem();
        helpMenu = new JMenu();
        keyMenuItem = new JMenuItem();
        jSeparator26 = new JPopupMenu.Separator();
        userManualMenuItem = new JMenuItem();
        solvingGuideMenuItem = new JMenuItem();
        projectHomePageMenuItem = new JMenuItem();
        jSeparator27 = new JPopupMenu.Separator();
        reportErrorMenuItem = new JMenuItem();
        askQuestionMenuItem = new JMenuItem();
        jSeparator25 = new JPopupMenu.Separator();
        aboutMenuItem = new JMenuItem();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // NOI18N
        ResourceBundle bundle = PropertyUtil.loadResourceFile("intl/MainFrame");

        setTitle(PropertyUtil.getValue("MainFrame.title")); // NOI18N
        setIconImage(getIcon());
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        statusLinePanel.setBackground(new Color(0, 153, 255));
        statusLinePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        statusPanelColorResult.setToolTipText(PropertyUtil.getValue("MainFrame.statusPanelColorResult.toolTipText")); // NOI18N

        GroupLayout statusPanelColorResultLayout = new GroupLayout(statusPanelColorResult);
        statusPanelColorResult.setLayout(statusPanelColorResultLayout);
        statusPanelColorResultLayout.setHorizontalGroup(
            statusPanelColorResultLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        statusPanelColorResultLayout.setVerticalGroup(
            statusPanelColorResultLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        statusLinePanel.add(statusPanelColorResult);

        jPanel1.setOpaque(false);
        //  FIXME  修改hgap参数值 1->10 设置每个颜色色外边距之间相距10像素，避免误触
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));

        statusPanelColor1.setToolTipText(PropertyUtil.getValue("MainFrame.statusPanelColor1.toolTipText")); // NOI18N
        statusPanelColor1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                statusPanelColor1MouseClicked(evt);
            }
        });

        GroupLayout statusPanelColor1Layout = new GroupLayout(statusPanelColor1);
        statusPanelColor1.setLayout(statusPanelColor1Layout);
        statusPanelColor1Layout.setHorizontalGroup(
            statusPanelColor1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        statusPanelColor1Layout.setVerticalGroup(
            statusPanelColor1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        jPanel1.add(statusPanelColor1);

        statusPanelColor2.setToolTipText(PropertyUtil.getValue("MainFrame.statusPanelColor2.toolTipText")); // NOI18N
        statusPanelColor2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                statusPanelColor2MouseClicked(evt);
            }
        });

        GroupLayout statusPanelColor2Layout = new GroupLayout(statusPanelColor2);
        statusPanelColor2.setLayout(statusPanelColor2Layout);
        statusPanelColor2Layout.setHorizontalGroup(
            statusPanelColor2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        statusPanelColor2Layout.setVerticalGroup(
            statusPanelColor2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        jPanel1.add(statusPanelColor2);

        statusPanelColor3.setToolTipText(PropertyUtil.getValue("MainFrame.statusPanelColor3.toolTipText")); // NOI18N
        statusPanelColor3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                statusPanelColor3MouseClicked(evt);
            }
        });

        GroupLayout statusPanelColor3Layout = new GroupLayout(statusPanelColor3);
        statusPanelColor3.setLayout(statusPanelColor3Layout);
        statusPanelColor3Layout.setHorizontalGroup(
            statusPanelColor3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        statusPanelColor3Layout.setVerticalGroup(
            statusPanelColor3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        jPanel1.add(statusPanelColor3);

        statusPanelColor4.setToolTipText(PropertyUtil.getValue("MainFrame.statusPanelColor4.toolTipText")); // NOI18N
        statusPanelColor4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                statusPanelColor4MouseClicked(evt);
            }
        });

        GroupLayout statusPanelColor4Layout = new GroupLayout(statusPanelColor4);
        statusPanelColor4.setLayout(statusPanelColor4Layout);
        statusPanelColor4Layout.setHorizontalGroup(
            statusPanelColor4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        statusPanelColor4Layout.setVerticalGroup(
            statusPanelColor4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        jPanel1.add(statusPanelColor4);

        statusPanelColor5.setToolTipText(PropertyUtil.getValue("MainFrame.statusPanelColor5.toolTipText")); // NOI18N
        statusPanelColor5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                statusPanelColor5MouseClicked(evt);
            }
        });

        GroupLayout statusPanelColor5Layout = new GroupLayout(statusPanelColor5);
        statusPanelColor5.setLayout(statusPanelColor5Layout);
        statusPanelColor5Layout.setHorizontalGroup(
            statusPanelColor5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        statusPanelColor5Layout.setVerticalGroup(
            statusPanelColor5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        jPanel1.add(statusPanelColor5);

        statusPanelColorClear.setToolTipText(PropertyUtil.getValue("MainFrame.statusPanelColorClear.toolTipText")); // NOI18N
        statusPanelColorClear.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                statusPanelColorClearMouseClicked(evt);
            }
        });

        GroupLayout statusPanelColorClearLayout = new GroupLayout(statusPanelColorClear);
        statusPanelColorClear.setLayout(statusPanelColorClearLayout);
        statusPanelColorClearLayout.setHorizontalGroup(
            statusPanelColorClearLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        statusPanelColorClearLayout.setVerticalGroup(
            statusPanelColorClearLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        jPanel1.add(statusPanelColorClear);

        statusPanelColorReset.setToolTipText(PropertyUtil.getValue("MainFrame.statusPanelColorReset.toolTipText")); // NOI18N
        statusPanelColorReset.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                statusPanelColorResetMouseClicked(evt);
            }
        });

        GroupLayout statusPanelColorResetLayout = new GroupLayout(statusPanelColorReset);
        statusPanelColorReset.setLayout(statusPanelColorResetLayout);
        statusPanelColorResetLayout.setHorizontalGroup(
            statusPanelColorResetLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );
        statusPanelColorResetLayout.setVerticalGroup(
            statusPanelColorResetLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        jPanel1.add(statusPanelColorReset);

        statusLinePanel.add(jPanel1);

        statusLabelCellCandidate.setText(PropertyUtil.getValue("MainFrame.statusLabelCellCandidate.text.cell")); // NOI18N
        statusLabelCellCandidate.setToolTipText(PropertyUtil.getValue("MainFrame.statusLabelCellCandidate.toolTipText")); // NOI18N
        statusLabelCellCandidate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                statusLabelCellCandidateMouseClicked(evt);
            }
        });
        statusLinePanel.add(statusLabelCellCandidate);

        jSeparator1.setOrientation(SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new Dimension(2, 17));
        statusLinePanel.add(jSeparator1);

        statusLabelLevel.setText(PropertyUtil.getValue("MainFrame.statusLabelLevel.text")); // NOI18N
        statusLabelLevel.setToolTipText(PropertyUtil.getValue("MainFrame.statusLabelLevel.toolTipText")); // NOI18N
        statusLinePanel.add(statusLabelLevel);

        jSeparator8.setOrientation(SwingConstants.VERTICAL);
        jSeparator8.setPreferredSize(new Dimension(2, 17));
        statusLinePanel.add(jSeparator8);

        progressLabel.setIcon(new ImageIcon(getClass().getResource("/img/invalid20.png"))); // NOI18N
        progressLabel.setText("null");
        progressLabel.setToolTipText(PropertyUtil.getValue("MainFrame.progressLabel.toolTipText")); // NOI18N
        statusLinePanel.add(progressLabel);

        jSeparator24.setOrientation(SwingConstants.VERTICAL);
        jSeparator24.setPreferredSize(new Dimension(2, 17));
        statusLinePanel.add(jSeparator24);

        statusLabelModus.setText(PropertyUtil.getValue("MainFrame.statusLabelModus.textPlay")); // NOI18N
        statusLabelModus.setToolTipText(PropertyUtil.getValue("MainFrame.statusLabelModus.toolTipText")); // NOI18N
        statusLinePanel.add(statusLabelModus);

        getContentPane().add(statusLinePanel, java.awt.BorderLayout.SOUTH);

        undoToolButton.setIcon(new ImageIcon(getClass().getResource("/img/undo.png"))); // NOI18N
        undoToolButton.setToolTipText(PropertyUtil.getValue("MainFrame.undoToolButton.toolTipText")); // NOI18N
        undoToolButton.setEnabled(false);
        undoToolButton.setRequestFocusEnabled(false);
        undoToolButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                undoToolButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(undoToolButton);

        redoToolButton.setIcon(new ImageIcon(getClass().getResource("/img/redo.png"))); // NOI18N
        redoToolButton.setToolTipText(PropertyUtil.getValue("MainFrame.redoToolButton.toolTipText")); // NOI18N
        redoToolButton.setEnabled(false);
        redoToolButton.setRequestFocusEnabled(false);
        redoToolButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                redoToolButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(redoToolButton);

        jSeparator9.setOrientation(SwingConstants.VERTICAL);
        jSeparator9.setMaximumSize(new Dimension(5, 32767));
        jToolBar1.add(jSeparator9);

        neuesSpielToolButton.setIcon(new ImageIcon(getClass().getResource("/img/hodoku02-32.png"))); // NOI18N
        neuesSpielToolButton.setToolTipText(PropertyUtil.getValue("MainFrame.neuesSpielToolButton.toolTipText")); // NOI18N
        neuesSpielToolButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                neuesSpielToolButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(neuesSpielToolButton);

        jSeparator12.setEnabled(false);
        jSeparator12.setMaximumSize(new Dimension(3, 0));
        jToolBar1.add(jSeparator12);

        levelComboBox.setToolTipText(PropertyUtil.getValue("MainFrame.levelComboBox.toolTipText")); // NOI18N
        levelComboBox.setMaximumSize(new Dimension(80, 20));
        levelComboBox.setMinimumSize(new Dimension(15, 8));
        levelComboBox.setPreferredSize(new Dimension(20, 10));
        levelComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                levelComboBoxActionPerformed(evt);
            }
        });
        jToolBar1.add(levelComboBox);

        jSeparator13.setMaximumSize(new Dimension(3, 0));
        jToolBar1.add(jSeparator13);

        jSeparator11.setOrientation(SwingConstants.VERTICAL);
        jSeparator11.setMaximumSize(new Dimension(5, 32767));
        jToolBar1.add(jSeparator11);

        redGreenToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/rgDeselected1.png"))); // NOI18N
        redGreenToggleButton.setSelected(true);
        redGreenToggleButton.setToolTipText(PropertyUtil.getValue("MainFrame.redGreenToggleButton.toolTipText")); // NOI18N
        redGreenToggleButton.setSelectedIcon(new ImageIcon(getClass().getResource("/img/rgSelected1.png"))); // NOI18N
        redGreenToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                redGreenToggleButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(redGreenToggleButton);

        f1ToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/f_1c.png"))); // NOI18N
        f1ToggleButton.setToolTipText(PropertyUtil.getValue("MainFrame.f1ToggleButton.toolTipText")); // NOI18N
        f1ToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                f1ToggleButtonActionPerformed1(evt);
            }
        });
        jToolBar1.add(f1ToggleButton);

        f2ToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/f_2c.png"))); // NOI18N
        f2ToggleButton.setToolTipText(PropertyUtil.getValue("MainFrame.f2ToggleButton.toolTipText")); // NOI18N
        f2ToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                f1ToggleButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(f2ToggleButton);

        f3ToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/f_3c.png"))); // NOI18N
        f3ToggleButton.setToolTipText(PropertyUtil.getValue("MainFrame.f3ToggleButton.toolTipText")); // NOI18N
        f3ToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                f1ToggleButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(f3ToggleButton);

        f4ToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/f_4c.png"))); // NOI18N
        f4ToggleButton.setToolTipText(PropertyUtil.getValue("MainFrame.f4ToggleButton.toolTipText")); // NOI18N
        f4ToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                f1ToggleButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(f4ToggleButton);

        f5ToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/f_5c.png"))); // NOI18N
        f5ToggleButton.setToolTipText(PropertyUtil.getValue("MainFrame.f5ToggleButton.toolTipText")); // NOI18N
        f5ToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                f1ToggleButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(f5ToggleButton);

        f6ToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/f_6c.png"))); // NOI18N
        f6ToggleButton.setToolTipText(PropertyUtil.getValue("MainFrame.f6ToggleButton.toolTipText")); // NOI18N
        f6ToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                f1ToggleButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(f6ToggleButton);

        f7ToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/f_7c.png"))); // NOI18N
        f7ToggleButton.setToolTipText(PropertyUtil.getValue("MainFrame.f7ToggleButton.toolTipText")); // NOI18N
        f7ToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                f1ToggleButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(f7ToggleButton);

        f8ToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/f_8c.png"))); // NOI18N
        f8ToggleButton.setToolTipText(PropertyUtil.getValue("MainFrame.f8ToggleButton.toolTipText")); // NOI18N
        f8ToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                f1ToggleButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(f8ToggleButton);

        f9ToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/f_9c.png"))); // NOI18N
        f9ToggleButton.setToolTipText(PropertyUtil.getValue("MainFrame.f9ToggleButton.toolTipText")); // NOI18N
        f9ToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                f1ToggleButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(f9ToggleButton);

        fxyToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/f_xyc.png"))); // NOI18N
        fxyToggleButton.setToolTipText(PropertyUtil.getValue("MainFrame.fxyToggleButton.toolTipText")); // NOI18N
        fxyToggleButton.setFocusable(false);
        fxyToggleButton.setHorizontalTextPosition(SwingConstants.CENTER);
        fxyToggleButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        fxyToggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fxyToggleButtonf1ToggleButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(fxyToggleButton);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);

        outerSplitPane.setDividerLocation(525);
        outerSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        outerSplitPane.setResizeWeight(1.0);
        outerSplitPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                outerSplitPanePropertyChange(evt);
            }
        });

        hintPanel.setBorder(BorderFactory.createTitledBorder(PropertyUtil.getValue("MainFrame.hintPanel.border.title"))); // NOI18N
        hintPanel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                hintPanelPropertyChange(evt);
            }
        });

        neuerHinweisButton.setMnemonic('n');
        neuerHinweisButton.setText(PropertyUtil.getValue("MainFrame.neuerHinweisButton.text")); // NOI18N
        neuerHinweisButton.setToolTipText(PropertyUtil.getValue("MainFrame.neuerHinweisButton.toolTipText")); // NOI18N
        neuerHinweisButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                neuerHinweisButtonActionPerformed(evt);
            }
        });

        hinweisAusfuehrenButton.setMnemonic('f');
        hinweisAusfuehrenButton.setText(PropertyUtil.getValue("MainFrame.hinweisAusfuehrenButton.text")); // NOI18N
        hinweisAusfuehrenButton.setToolTipText(PropertyUtil.getValue("MainFrame.hinweisAusfuehrenButton.toolTipText")); // NOI18N
        hinweisAusfuehrenButton.setEnabled(false);
        hinweisAusfuehrenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                hinweisAusfuehrenButtonActionPerformed(evt);
            }
        });

        solveUpToButton.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solveUpToButton.mnemonic").charAt(0));
        solveUpToButton.setText(PropertyUtil.getValue("MainFrame.solveUpToButton.text")); // NOI18N
        solveUpToButton.setToolTipText(PropertyUtil.getValue("MainFrame.solveUpToButton.toolTipText")); // NOI18N
        solveUpToButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                solveUpToButtonActionPerformed(evt);
            }
        });

        hinweisAbbrechenButton.setMnemonic('a');
        hinweisAbbrechenButton.setText(PropertyUtil.getValue("MainFrame.hinweisAbbrechenButton.text")); // NOI18N
        hinweisAbbrechenButton.setToolTipText(PropertyUtil.getValue("MainFrame.hinweisAbbrechenButton.toolTipText")); // NOI18N
        hinweisAbbrechenButton.setEnabled(false);
        hinweisAbbrechenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                hinweisAbbrechenButtonActionPerformed(evt);
            }
        });

        hinweisTextArea.setColumns(20);
        hinweisTextArea.setEditable(false);
        hinweisTextArea.setLineWrap(true);
        hinweisTextArea.setRows(5);
        hinweisTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(hinweisTextArea);

        GroupLayout hintPanelLayout = new GroupLayout(hintPanel);
        hintPanel.setLayout(hintPanelLayout);
        hintPanelLayout.setHorizontalGroup(
            hintPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, hintPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(hintPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(neuerHinweisButton)
                    .addComponent(solveUpToButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(hintPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(hinweisAusfuehrenButton)
                    .addComponent(hinweisAbbrechenButton)))
        );

        hintPanelLayout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] {hinweisAbbrechenButton, hinweisAusfuehrenButton, neuerHinweisButton, solveUpToButton});

        hintPanelLayout.setVerticalGroup(
            hintPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(hintPanelLayout.createSequentialGroup()
                .addGroup(hintPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(hinweisAusfuehrenButton, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
                    .addComponent(neuerHinweisButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(hintPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(hinweisAbbrechenButton)
                    .addComponent(solveUpToButton)))
            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
        );

        hintPanelLayout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {hinweisAbbrechenButton, hinweisAusfuehrenButton, neuerHinweisButton, solveUpToButton});

        outerSplitPane.setRightComponent(hintPanel);

        getContentPane().add(outerSplitPane, java.awt.BorderLayout.CENTER);

        dateiMenu.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.dateiMenuMnemonic").charAt(0));
        dateiMenu.setText(PropertyUtil.getValue("MainFrame.dateiMenu.text")); // NOI18N

        neuMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        neuMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.neuMenuItemMnemonic").charAt(0));
        neuMenuItem.setText(PropertyUtil.getValue("MainFrame.neuMenuItem.text")); // NOI18N
        neuMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                neuMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(neuMenuItem);
        dateiMenu.add(jSeparator39);

        loadPuzzleMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        loadPuzzleMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.loadMenuItemMnemonic").charAt(0));
        loadPuzzleMenuItem.setText(PropertyUtil.getValue("MainFrame.loadPuzzleMenuItem.text")); // NOI18N
        loadPuzzleMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loadPuzzleMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(loadPuzzleMenuItem);

        savePuzzleMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        savePuzzleMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.savePuzzleMenuItem.mnemonic").charAt(0));
        savePuzzleMenuItem.setText(PropertyUtil.getValue("MainFrame.savePuzzleMenuItem.text")); // NOI18N
        savePuzzleMenuItem.setEnabled(false);
        savePuzzleMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                savePuzzleMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(savePuzzleMenuItem);

        savePuzzleAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK));
        savePuzzleAsMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.saveAsMenuItemMnemonic").charAt(0));
        savePuzzleAsMenuItem.setText(PropertyUtil.getValue("MainFrame.savePuzzleAsMenuItem.text")); // NOI18N
        savePuzzleAsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                savePuzzleAsMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(savePuzzleAsMenuItem);

        loadConfigMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.loadConfigMenuItem.mnemonic").charAt(0));
        loadConfigMenuItem.setText(PropertyUtil.getValue("MainFrame.loadConfigMenuItem.text")); // NOI18N
        loadConfigMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loadConfigMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(loadConfigMenuItem);

        saveConfigAsMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.saveConfigAsMenuItem.mnemonic").charAt(0));
        saveConfigAsMenuItem.setText(PropertyUtil.getValue("MainFrame.saveConfigAsMenuItem.text")); // NOI18N
        saveConfigAsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveConfigAsMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(saveConfigAsMenuItem);
        dateiMenu.add(jSeparator38);

        seiteEinrichtenMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.seiteEinrichtenMenuItemMnemonic").charAt(0));
        seiteEinrichtenMenuItem.setText(PropertyUtil.getValue("MainFrame.seiteEinrichtenMenuItem.text")); // NOI18N
        seiteEinrichtenMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                seiteEinrichtenMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(seiteEinrichtenMenuItem);

        druckenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        druckenMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.druckenMenuItemMnemonic").charAt(0));
        druckenMenuItem.setText(PropertyUtil.getValue("MainFrame.druckenMenuItem.text")); // NOI18N
        druckenMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                druckenMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(druckenMenuItem);

        extendedPrintMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.extendedPrintMenuItem.mnemonic").charAt(0));
        extendedPrintMenuItem.setText(PropertyUtil.getValue("MainFrame.extendedPrintMenuItem.text")); // NOI18N
        extendedPrintMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                extendedPrintMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(extendedPrintMenuItem);

        speichernAlsBildMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.speichernAlsBildMenuItemMnemonic").charAt(0));
        speichernAlsBildMenuItem.setText(PropertyUtil.getValue("MainFrame.speichernAlsBildMenuItem.text")); // NOI18N
        speichernAlsBildMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                speichernAlsBildMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(speichernAlsBildMenuItem);
        dateiMenu.add(jSeparator37);

        spielEingebenMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.spielEingebenMenuItemMnemonic").charAt(0));
        spielEingebenMenuItem.setText(PropertyUtil.getValue("MainFrame.spielEingebenMenuItem.text")); // NOI18N
        spielEingebenMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                spielEingebenMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(spielEingebenMenuItem);

        spielEditierenMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("spielEditierenMenuItemMnemonic").charAt(0));
        spielEditierenMenuItem.setText(PropertyUtil.getValue("MainFrame.spielEditierenMenuItem.text")); // NOI18N
        spielEditierenMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                spielEditierenMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(spielEditierenMenuItem);

        spielSpielenMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.spielenMenuItemMnemonic").charAt(0));
        spielSpielenMenuItem.setText(PropertyUtil.getValue("MainFrame.spielSpielenMenuItem.text")); // NOI18N
        spielSpielenMenuItem.setEnabled(false);
        spielSpielenMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                spielSpielenMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(spielSpielenMenuItem);
        dateiMenu.add(jSeparator36);

        beendenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        beendenMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.beendenMenuItemMnemonic").charAt(0));
        beendenMenuItem.setText(PropertyUtil.getValue("MainFrame.beendenMenuItem.text")); // NOI18N
        beendenMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                beendenMenuItemActionPerformed(evt);
            }
        });
        dateiMenu.add(beendenMenuItem);

        jMenuBar1.add(dateiMenu);

        bearbeitenMenu.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.bearbeitenMenuMnemonic").charAt(0));
        bearbeitenMenu.setText(PropertyUtil.getValue("MainFrame.bearbeitenMenu.text")); // NOI18N

        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.undoMenuItemMnemonic").charAt(0));
        undoMenuItem.setText(PropertyUtil.getValue("MainFrame.undoMenuItem.text")); // NOI18N
        undoMenuItem.setEnabled(false);
        undoMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                undoMenuItemActionPerformed(evt);
            }
        });
        bearbeitenMenu.add(undoMenuItem);

        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        redoMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.redoMenuItemMnemonic").charAt(0));
        redoMenuItem.setText(PropertyUtil.getValue("MainFrame.redoMenuItem.text")); // NOI18N
        redoMenuItem.setEnabled(false);
        redoMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                redoMenuItemActionPerformed(evt);
            }
        });
        bearbeitenMenu.add(redoMenuItem);
        bearbeitenMenu.add(jSeparator35);

        copyCluesMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        copyCluesMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.copyCluesMenuItemMnemonic").charAt(0));
        copyCluesMenuItem.setText(PropertyUtil.getValue("MainFrame.copyCluesMenuItem.text")); // NOI18N
        copyCluesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                copyCluesMenuItemActionPerformed(evt);
            }
        });
        bearbeitenMenu.add(copyCluesMenuItem);

        copyFilledMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.copyFilledMenuItemMnemonic").charAt(0));
        copyFilledMenuItem.setText(PropertyUtil.getValue("MainFrame.copyFilledMenuItem.text")); // NOI18N
        copyFilledMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                copyFilledMenuItemActionPerformed(evt);
            }
        });
        bearbeitenMenu.add(copyFilledMenuItem);

        copyPmGridMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        copyPmGridMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.copyPmGridMenuItemMnemonic").charAt(0));
        copyPmGridMenuItem.setText(PropertyUtil.getValue("MainFrame.copyPmGridMenuItem.text")); // NOI18N
        copyPmGridMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                copyPmGridMenuItemActionPerformed(evt);
            }
        });
        bearbeitenMenu.add(copyPmGridMenuItem);

        copyPmGridWithStepMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.copyPmGridWithStepMenuItemMnemonic").charAt(0));
        copyPmGridWithStepMenuItem.setText(PropertyUtil.getValue("MainFrame.copyPmGridWithStepMenuItem.text")); // NOI18N
        copyPmGridWithStepMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                copyPmGridWithStepMenuItemActionPerformed(evt);
            }
        });
        bearbeitenMenu.add(copyPmGridWithStepMenuItem);

        copyLibraryMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.copyLibraryMenuItemMnemonic").charAt(0));
        copyLibraryMenuItem.setText(PropertyUtil.getValue("MainFrame.copyLibraryMenuItem.text")); // NOI18N
        copyLibraryMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                copyLibraryMenuItemActionPerformed(evt);
            }
        });
        bearbeitenMenu.add(copyLibraryMenuItem);

        copySSMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.copySSMenuItem.mnemonic").charAt(0));
        copySSMenuItem.setText(PropertyUtil.getValue("MainFrame.copySSMenuItem.text")); // NOI18N
        copySSMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                copySSMenuItemActionPerformed(evt);
            }
        });
        bearbeitenMenu.add(copySSMenuItem);

        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        pasteMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.pasteMenuItemMnemonic").charAt(0));
        pasteMenuItem.setText(PropertyUtil.getValue("MainFrame.pasteMenuItem.text")); // NOI18N
        pasteMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                pasteMenuItemActionPerformed(evt);
            }
        });
        bearbeitenMenu.add(pasteMenuItem);
        bearbeitenMenu.add(jSeparator34);

        restartSpielMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        restartSpielMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.restartSpielMenuItemMnemonic").charAt(0));
        restartSpielMenuItem.setText(PropertyUtil.getValue("MainFrame.restartSpielMenuItem.text")); // NOI18N
        restartSpielMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                restartSpielMenuItemActionPerformed(evt);
            }
        });
        bearbeitenMenu.add(restartSpielMenuItem);

        resetSpielMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.resetSpielMenuItemMnemonic").charAt(0));
        resetSpielMenuItem.setText(PropertyUtil.getValue("MainFrame.resetSpielMenuItem.text")); // NOI18N
        resetSpielMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                resetSpielMenuItemActionPerformed(evt);
            }
        });
        bearbeitenMenu.add(resetSpielMenuItem);
        bearbeitenMenu.add(jSeparator33);

        configMenuItem.setAccelerator(KeyStroke.getKeyStroke(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.configMenuItemAccelerator")));
        configMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.configMenuItemMnemonic").charAt(0));
        configMenuItem.setText(PropertyUtil.getValue("MainFrame.configMenuItem.text")); // NOI18N
        configMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configMenuItemActionPerformed(evt);
            }
        });
        bearbeitenMenu.add(configMenuItem);

        jMenuBar1.add(bearbeitenMenu);

        modeMenu.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.modeMenu.mnemonic").charAt(0));
        modeMenu.setText(PropertyUtil.getValue("MainFrame.modeMenu.text")); // NOI18N

        modeButtonGroup.add(playingMenuItem);
        playingMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.playingMenuItem.mnemonic").charAt(0));
        playingMenuItem.setSelected(true);
        playingMenuItem.setText(PropertyUtil.getValue("MainFrame.playingMenuItem.text")); // NOI18N
        playingMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                playingMenuItemActionPerformed(evt);
            }
        });
        modeMenu.add(playingMenuItem);

        modeButtonGroup.add(learningMenuItem);
        learningMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.learningMenuItem.mnemonic").charAt(0));
        learningMenuItem.setText(PropertyUtil.getValue("MainFrame.learningMenuItem.text")); // NOI18N
        learningMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                learningMenuItemActionPerformed(evt);
            }
        });
        modeMenu.add(learningMenuItem);

        modeButtonGroup.add(practisingMenuItem);
        practisingMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.practisingMenuItem.mnemonic").charAt(0));
        practisingMenuItem.setText(PropertyUtil.getValue("MainFrame.practisingMenuItem.text")); // NOI18N
        practisingMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                practisingMenuItemActionPerformed(evt);
            }
        });
        modeMenu.add(practisingMenuItem);

        jMenuBar1.add(modeMenu);

        optionenMenu.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.optionenMenuMnemonic").charAt(0));
        optionenMenu.setText(PropertyUtil.getValue("MainFrame.optionenMenu.text")); // NOI18N

        showCandidatesMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.showCandidatesMenuItemMnemonic").charAt(0));
        showCandidatesMenuItem.setText(PropertyUtil.getValue("MainFrame.showCandidatesMenuItem.text")); // NOI18N
        showCandidatesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showCandidatesMenuItemActionPerformed(evt);
            }
        });
        optionenMenu.add(showCandidatesMenuItem);

        showWrongValuesMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.showWrongValuesMenuItemMnemonic").charAt(0));
        showWrongValuesMenuItem.setText(PropertyUtil.getValue("MainFrame.showWrongValuesMenuItem.text")); // NOI18N
        showWrongValuesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showWrongValuesMenuItemActionPerformed(evt);
            }
        });
        optionenMenu.add(showWrongValuesMenuItem);

        showDeviationsMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.showDeviationsMenuItemMnemonic").charAt(0));
        showDeviationsMenuItem.setSelected(true);
        showDeviationsMenuItem.setText(PropertyUtil.getValue("MainFrame.showDeviationsMenuItem.text")); // NOI18N
        showDeviationsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showDeviationsMenuItemActionPerformed(evt);
            }
        });
        optionenMenu.add(showDeviationsMenuItem);

        showColorKuMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.showColorKuMenuItem.mnemonic").charAt(0));
        showColorKuMenuItem.setSelected(true);
        showColorKuMenuItem.setText(PropertyUtil.getValue("MainFrame.showColorKuMenuItem.text")); // NOI18N
        showColorKuMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showColorKuMenuItemActionPerformed(evt);
            }
        });
        optionenMenu.add(showColorKuMenuItem);
        optionenMenu.add(jSeparator32);

        colorButtonGroup.add(colorCellsMenuItem);
        colorCellsMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.colorCellsMenuItem.mnemonic").charAt(0));
        colorCellsMenuItem.setSelected(true);
        colorCellsMenuItem.setText(PropertyUtil.getValue("MainFrame.colorCellsMenuItem.text")); // NOI18N
        colorCellsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                colorCellsMenuItemActionPerformed(evt);
            }
        });
        optionenMenu.add(colorCellsMenuItem);

        colorButtonGroup.add(colorCandidatesMenuItem);
        colorCandidatesMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.colorCandidatesMenuItem.mnemonic").charAt(0));
        colorCandidatesMenuItem.setText(PropertyUtil.getValue("MainFrame.colorCandidatesMenuItem.text")); // NOI18N
        colorCandidatesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                colorCandidatesMenuItemActionPerformed(evt);
            }
        });
        optionenMenu.add(colorCandidatesMenuItem);
        optionenMenu.add(jSeparator31);

        levelMenu.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.levelMenuMnemonic").charAt(0));
        levelMenu.setText(PropertyUtil.getValue("MainFrame.levelMenu.text")); // NOI18N

        levelButtonGroup.add(levelLeichtMenuItem);
        levelLeichtMenuItem.setSelected(true);
        levelLeichtMenuItem.setText("Leicht"); // NOI18N
        levelLeichtMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                levelLeichtMenuItemActionPerformed(evt);
            }
        });
        levelMenu.add(levelLeichtMenuItem);

        levelButtonGroup.add(levelMittelMenuItem);
        levelMittelMenuItem.setText("Mittel"); // NOI18N
        levelMittelMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                levelMittelMenuItemActionPerformed(evt);
            }
        });
        levelMenu.add(levelMittelMenuItem);

        levelButtonGroup.add(levelKniffligMenuItem);
        levelKniffligMenuItem.setText("Schwer\n"); // NOI18N
        levelKniffligMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                levelKniffligMenuItemActionPerformed(evt);
            }
        });
        levelMenu.add(levelKniffligMenuItem);

        levelButtonGroup.add(levelSchwerMenuItem);
        levelSchwerMenuItem.setText("Unfair"); // NOI18N
        levelSchwerMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                levelSchwerMenuItemActionPerformed(evt);
            }
        });
        levelMenu.add(levelSchwerMenuItem);

        levelButtonGroup.add(levelExtremMenuItem);
        levelExtremMenuItem.setText("Extrem"); // NOI18N
        levelExtremMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                levelExtremMenuItemActionPerformed(evt);
            }
        });
        levelMenu.add(levelExtremMenuItem);

        optionenMenu.add(levelMenu);

        jMenuBar1.add(optionenMenu);

        raetselMenu.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.raetselMenuMnemonic").charAt(0));
        raetselMenu.setText(PropertyUtil.getValue("MainFrame.raetselMenu.text")); // NOI18N

        vageHintMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, java.awt.event.InputEvent.ALT_MASK));
        vageHintMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.vageHintMenuItemMnemonic").charAt(0));
        vageHintMenuItem.setText(PropertyUtil.getValue("MainFrame.vageHintMenuItem")); // NOI18N
        vageHintMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                vageHintMenuItemActionPerformed(evt);
            }
        });
        raetselMenu.add(vageHintMenuItem);

        mediumHintMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, java.awt.event.InputEvent.CTRL_MASK));
        mediumHintMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.mediumHintMenuItemMnemonic").charAt(0));
        mediumHintMenuItem.setText(PropertyUtil.getValue("MainFrame.mediumHintMenuItem.text")); // NOI18N
        mediumHintMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                mediumHintMenuItemActionPerformed(evt);
            }
        });
        raetselMenu.add(mediumHintMenuItem);

        loesungsSchrittMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        loesungsSchrittMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.loesungsSchrittMenuItemMnemonic").charAt(0));
        loesungsSchrittMenuItem.setText(PropertyUtil.getValue("MainFrame.loesungsSchrittMenuItem.text")); // NOI18N
        loesungsSchrittMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loesungsSchrittMenuItemActionPerformed(evt);
            }
        });
        raetselMenu.add(loesungsSchrittMenuItem);
        raetselMenu.add(jSeparator30);

        backdoorSearchMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.backdoorSearchMenuItem.mnemonic").charAt(0));
        backdoorSearchMenuItem.setText(PropertyUtil.getValue("MainFrame.backdoorSearchMenuItem.text")); // NOI18N
        backdoorSearchMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                backdoorSearchMenuItemActionPerformed(evt);
            }
        });
        raetselMenu.add(backdoorSearchMenuItem);

        historyMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.historyMenuItem.mnemonic").charAt(0));
        historyMenuItem.setText(PropertyUtil.getValue("MainFrame.historyMenuItem.text")); // NOI18N
        historyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                historyMenuItemActionPerformed(evt);
            }
        });
        raetselMenu.add(historyMenuItem);

        createSavePointMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.setSavePointMenuItem.mnemonic").charAt(0));
        createSavePointMenuItem.setText(PropertyUtil.getValue("MainFrame.createSavePointMenuItem.text")); // NOI18N
        createSavePointMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                createSavePointMenuItemActionPerformed(evt);
            }
        });
        raetselMenu.add(createSavePointMenuItem);

        restoreSavePointMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.restoreSavePointMenuItem.mnemonic").charAt(0));
        restoreSavePointMenuItem.setText(PropertyUtil.getValue("MainFrame.restoreSavePointMenuItem.text")); // NOI18N
        restoreSavePointMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                restoreSavePointMenuItemActionPerformed(evt);
            }
        });
        raetselMenu.add(restoreSavePointMenuItem);
        raetselMenu.add(jSeparator29);

        setGivensMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.setGivensMenuItem.mnemonic").charAt(0));
        setGivensMenuItem.setText(PropertyUtil.getValue("MainFrame.setGivensMenuItem.text")); // NOI18N
        setGivensMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setGivensMenuItemActionPerformed(evt);
            }
        });
        raetselMenu.add(setGivensMenuItem);
        raetselMenu.add(jSeparator28);

        alleHiddenSinglesSetzenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        alleHiddenSinglesSetzenMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.alleHiddenSinglesSetzenMenuItemMnemonic").charAt(0));
        alleHiddenSinglesSetzenMenuItem.setText(PropertyUtil.getValue("MainFrame.alleHiddenSinglesSetzenMenuItem.text")); // NOI18N
        alleHiddenSinglesSetzenMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                alleHiddenSinglesSetzenMenuItemActionPerformed(evt);
            }
        });
        raetselMenu.add(alleHiddenSinglesSetzenMenuItem);

        jMenuBar1.add(raetselMenu);

        ansichtMenu.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.ansichtMenuMnemonic").charAt(0));
        ansichtMenu.setText(PropertyUtil.getValue("MainFrame.ansichtMenu.text")); // NOI18N

        sudokuOnlyMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift control " + PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.sudokuOnlyMenuItemMnemonic").toUpperCase().charAt(0)));
        viewButtonGroup.add(sudokuOnlyMenuItem);
        sudokuOnlyMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.sudokuOnlyMenuItemMnemonic").charAt(0));
        sudokuOnlyMenuItem.setSelected(true);
        sudokuOnlyMenuItem.setText(PropertyUtil.getValue("MainFrame.sudokuOnlyMenuItem.text")); // NOI18N
        sudokuOnlyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sudokuOnlyMenuItemActionPerformed(evt);
            }
        });
        ansichtMenu.add(sudokuOnlyMenuItem);
        ansichtMenu.add(jSeparator20);

        summaryMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift control " + PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.summaryMenuItemMnemonic").toUpperCase().charAt(0)));
        viewButtonGroup.add(summaryMenuItem);
        summaryMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.summaryMenuItemMnemonic").charAt(0));
        summaryMenuItem.setText(PropertyUtil.getValue("MainFrame.summaryMenuItem.text")); // NOI18N
        summaryMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                summaryMenuItemActionPerformed(evt);
            }
        });
        ansichtMenu.add(summaryMenuItem);

        solutionMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift control " + PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solutionMenuItemMnemonic").toUpperCase().charAt(0)));
        viewButtonGroup.add(solutionMenuItem);
        solutionMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solutionMenuItemMnemonic").charAt(0));
        solutionMenuItem.setText(PropertyUtil.getValue("MainFrame.solutionMenuItem.text")); // NOI18N
        solutionMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                solutionMenuItemActionPerformed(evt);
            }
        });
        ansichtMenu.add(solutionMenuItem);

        allStepsMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift control " + PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.allStepsMenuItemMnemonic").toUpperCase().charAt(0)));
        viewButtonGroup.add(allStepsMenuItem);
        allStepsMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.allStepsMenuItemMnemonic").charAt(0));
        allStepsMenuItem.setText(PropertyUtil.getValue("MainFrame.allStepsMenuItem.text")); // NOI18N
        allStepsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                allStepsMenuItemActionPerformed(evt);
            }
        });
        ansichtMenu.add(allStepsMenuItem);

        cellZoomMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift control " + PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.cellZoomMenuItemMnemonic").toUpperCase().charAt(0)));
        viewButtonGroup.add(cellZoomMenuItem);
        cellZoomMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.cellZoomMenuItemMnemonic").charAt(0));
        cellZoomMenuItem.setText(PropertyUtil.getValue("MainFrame.cellZoomMenuItem.text")); // NOI18N
        cellZoomMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cellZoomMenuItemActionPerformed(evt);
            }
        });
        ansichtMenu.add(cellZoomMenuItem);
        ansichtMenu.add(jSeparator6);

        showHintPanelMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.showHintPanelMenuItem.mnemonic").charAt(0));
        showHintPanelMenuItem.setSelected(true);
        showHintPanelMenuItem.setText(PropertyUtil.getValue("MainFrame.showHintPanelMenuItem.text")); // NOI18N
        showHintPanelMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showHintPanelMenuItemActionPerformed(evt);
            }
        });
        ansichtMenu.add(showHintPanelMenuItem);

        showToolBarMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.showToolBarMenuItem.mnemonic").charAt(0));
        showToolBarMenuItem.setSelected(true);
        showToolBarMenuItem.setText(PropertyUtil.getValue("MainFrame.showToolBarMenuItem.text")); // NOI18N
        showToolBarMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showToolBarMenuItemActionPerformed(evt);
            }
        });
        ansichtMenu.add(showToolBarMenuItem);

        showHintButtonsCheckBoxMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.showHintButtonsCheckBoxMenuItem.mnemonic").charAt(0));
        showHintButtonsCheckBoxMenuItem.setText(PropertyUtil.getValue("MainFrame.showHintButtonsCheckBoxMenuItem.text")); // NOI18N
        showHintButtonsCheckBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showHintButtonsCheckBoxMenuItemActionPerformed(evt);
            }
        });
        ansichtMenu.add(showHintButtonsCheckBoxMenuItem);

        fullScreenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        fullScreenMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.fullScreenMenuItem.mnemonic").charAt(0));
        fullScreenMenuItem.setText(PropertyUtil.getValue("MainFrame.fullScreenMenuItem.text")); // NOI18N
        fullScreenMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fullScreenMenuItemActionPerformed(evt);
            }
        });
        ansichtMenu.add(fullScreenMenuItem);
        ansichtMenu.add(jSeparator23);

        resetViewMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.resetViewMenuItemMnemonic").charAt(0));
        resetViewMenuItem.setText(PropertyUtil.getValue("MainFrame.resetViewMenuItem.text")); // NOI18N
        resetViewMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                resetViewMenuItemActionPerformed(evt);
            }
        });
        ansichtMenu.add(resetViewMenuItem);

        jMenuBar1.add(ansichtMenu);

        helpMenu.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.helpMenu.mnemonic").charAt(0));
        helpMenu.setText(PropertyUtil.getValue("MainFrame.helpMenu.text")); // NOI18N

        keyMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.keyMenuItem.mnemonic").charAt(0));
        keyMenuItem.setText(PropertyUtil.getValue("MainFrame.keyMenuItem.text")); // NOI18N
        keyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                keyMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(keyMenuItem);
        helpMenu.add(jSeparator26);

        userManualMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.userManualMenuItem.mnemonic").charAt(0));
        userManualMenuItem.setText(PropertyUtil.getValue("MainFrame.userManualMenuItem.text")); // NOI18N
        userManualMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                userManualMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(userManualMenuItem);

        solvingGuideMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solvingGuideMenuItem.mnemonic").charAt(0));
        solvingGuideMenuItem.setText(PropertyUtil.getValue("MainFrame.solvingGuideMenuItem.text")); // NOI18N
        solvingGuideMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                solvingGuideMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(solvingGuideMenuItem);

        projectHomePageMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.projectHomePageMenuItem.mnemonic").charAt(0));
        projectHomePageMenuItem.setText(PropertyUtil.getValue("MainFrame.projectHomePageMenuItem.text")); // NOI18N
        projectHomePageMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                projectHomePageMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(projectHomePageMenuItem);
        helpMenu.add(jSeparator27);

        reportErrorMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.reportErrorMenuItem.mnemonic").charAt(0));
        reportErrorMenuItem.setText(PropertyUtil.getValue("MainFrame.reportErrorMenuItem.text")); // NOI18N
        reportErrorMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                reportErrorMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(reportErrorMenuItem);

        askQuestionMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.askQuestionMenuItem.mnemonic").charAt(0));
        askQuestionMenuItem.setText(PropertyUtil.getValue("MainFrame.askQuestionMenuItem.text")); // NOI18N
        askQuestionMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                askQuestionMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(askQuestionMenuItem);
        helpMenu.add(jSeparator25);

        aboutMenuItem.setMnemonic(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.aboutMenuItem.").charAt(0));
        aboutMenuItem.setText(PropertyUtil.getValue("MainFrame.aboutMenuItem.text")); // NOI18N
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void savePuzzleAsMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_savePuzzleAsMenuItemActionPerformed
        saveToFile(true);
    }//GEN-LAST:event_savePuzzleAsMenuItemActionPerformed

    private void loadPuzzleMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_loadPuzzleMenuItemActionPerformed
        loadFromFile(true);
    }//GEN-LAST:event_loadPuzzleMenuItemActionPerformed

    private void configMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configMenuItemActionPerformed
        new ConfigDialog(this, true, -1).setVisible(true);
        sudokuPanel.resetActiveColor();
        if (sudokuPanel.getActiveColor() != -1) {
            statusPanelColorResult.setBackground(Options.getInstance().getColoringColors()[sudokuPanel.getActiveColor()]);
        }
        sudokuPanel.setColorIconsInPopupMenu();
        check();
        fixFocus();
        sudokuPanel.repaint();
        repaint();
    }//GEN-LAST:event_configMenuItemActionPerformed

    private void statusLabelCellCandidateMouseClicked(MouseEvent evt) {//GEN-FIRST:event_statusLabelCellCandidateMouseClicked
        sudokuPanel.setColorCells(!sudokuPanel.isColorCells());
        check();
        fixFocus();
    }//GEN-LAST:event_statusLabelCellCandidateMouseClicked

    private void allStepsMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_allStepsMenuItemActionPerformed
        allStepsPanel.setSudoku(sudokuPanel.getSudoku());
        setSplitPane(allStepsPanel);
        //initializeResultPanels();
        repaint();
    }//GEN-LAST:event_allStepsMenuItemActionPerformed

    private void solutionMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_solutionMenuItemActionPerformed
        setSplitPane(solutionPanel);
        //initializeResultPanels();
        repaint();
    }//GEN-LAST:event_solutionMenuItemActionPerformed

    private void sudokuOnlyMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sudokuOnlyMenuItemActionPerformed
        splitPanel.setRight(null);
        if (getExtendedState() != JFrame.MAXIMIZED_BOTH) {
            if (splitPanel.getBounds().getWidth() < splitPanel.getBounds().getHeight()) {
                setSize(getWidth() + 1, getHeight());
            }
        }
    }//GEN-LAST:event_sudokuOnlyMenuItemActionPerformed

    private void summaryMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_summaryMenuItemActionPerformed
        setSplitPane(summaryPanel);
        //initializeResultPanels();
        repaint();
    }//GEN-LAST:event_summaryMenuItemActionPerformed

    private void speichernAlsBildMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_speichernAlsBildMenuItemActionPerformed
        WriteAsPNGDialog dlg = new WriteAsPNGDialog(this, true, bildSize, bildAufloesung, bildEinheit);
        dlg.setVisible(true);
        if (dlg.isOk()) {
            File bildFile = dlg.getBildFile();
            bildAufloesung = dlg.getAufloesung();
            bildSize = dlg.getBildSize();
            bildEinheit = dlg.getEinheit();
            int size = 0;
            switch (bildEinheit) {
                case 0:
                    size = (int) (bildSize / 25.4 * bildAufloesung);
                    break;
                case 1:
                    size = (int) (bildSize * bildAufloesung);
                    break;
                case 2:
                    size = (int) bildSize;
                    break;
            }
            if (bildFile.exists()) {
                // Override warning!
                MessageFormat msgf = new MessageFormat("");
                Object[] args = new Object[]{bildFile.getName()};
                msgf.applyPattern(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.file_exists"));
                String warning = msgf.format(args);
                String title = PropertyUtil.getProperty("intl/MainFrame", "MainFrame.hint");
                if (JOptionPane.showConfirmDialog(null, warning, title, JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            sudokuPanel.saveSudokuAsPNG(bildFile, size, bildAufloesung);
        }
    }//GEN-LAST:event_speichernAlsBildMenuItemActionPerformed

    private void druckenMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_druckenMenuItemActionPerformed
        if (job == null) {
            job = PrinterJob.getPrinterJob();
        }
        if (pageFormat == null) {
            pageFormat = job.defaultPage();
        }
        try {
            job.setPrintable(sudokuPanel, pageFormat);
            if (job.printDialog()) {
                job.print();
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, ex.toString(),
                    PropertyUtil.getProperty("intl/MainFrame", "MainFrame.error"), JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_druckenMenuItemActionPerformed

    private void seiteEinrichtenMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_seiteEinrichtenMenuItemActionPerformed
        if (job == null) {
            job = PrinterJob.getPrinterJob();
        }
        if (pageFormat == null) {
            pageFormat = job.defaultPage();
        }
        pageFormat = job.pageDialog(pageFormat);
    }//GEN-LAST:event_seiteEinrichtenMenuItemActionPerformed

    private void restartSpielMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_restartSpielMenuItemActionPerformed
        if (JOptionPane.showConfirmDialog(this, PropertyUtil.getProperty("intl/MainFrame", "MainFrame.start_new_game"),
                PropertyUtil.getProperty("intl/MainFrame", "MainFrame.start_new"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            sudokuPanel.setSudoku(sudokuPanel.getSudokuString(ClipboardMode.CLUES_ONLY));
            sudokuPanel.checkProgress();
            allStepsPanel.setSudoku(sudokuPanel.getSudoku());
            initializeResultPanels();
            repaint();
            setSpielen(true);
            check();
            fixFocus();
        }
}//GEN-LAST:event_restartSpielMenuItemActionPerformed

    private void beendenMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_beendenMenuItemActionPerformed
        formWindowClosed(null);
        System.exit(0);
    }//GEN-LAST:event_beendenMenuItemActionPerformed

    private void copyLibraryMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_copyLibraryMenuItemActionPerformed
        copyToClipboard(ClipboardMode.LIBRARY, false);
    }//GEN-LAST:event_copyLibraryMenuItemActionPerformed

    private void copyPmGridWithStepMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_copyPmGridWithStepMenuItemActionPerformed
        SolutionStep activeStep = sudokuPanel.getStep();
        if (activeStep == null) {
            JOptionPane.showMessageDialog(this, PropertyUtil.getProperty("intl/MainFrame", "MainFrame.no_step_selected"),
                    PropertyUtil.getProperty("intl/MainFrame", "MainFrame.error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        copyToClipboard(ClipboardMode.PM_GRID_WITH_STEP, false);
    }//GEN-LAST:event_copyPmGridWithStepMenuItemActionPerformed

    private void copyPmGridMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_copyPmGridMenuItemActionPerformed
        copyToClipboard(ClipboardMode.PM_GRID, false);
    }//GEN-LAST:event_copyPmGridMenuItemActionPerformed

    private void copyFilledMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_copyFilledMenuItemActionPerformed
        copyToClipboard(ClipboardMode.VALUES_ONLY, false);
    }//GEN-LAST:event_copyFilledMenuItemActionPerformed

    private void showDeviationsMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showDeviationsMenuItemActionPerformed
        sudokuPanel.setShowDeviations(showDeviationsMenuItem.isSelected());
        check();
        fixFocus();
    }//GEN-LAST:event_showDeviationsMenuItemActionPerformed

    private void neuesSpielToolButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_neuesSpielToolButtonActionPerformed
        // neues Spiel in der gewünschten Schwierigkeitsstufe erzeugen
        int actLevel = Options.getInstance().getActLevel();
        DifficultyLevel actDiffLevel = Options.getInstance().getDifficultyLevel(actLevel);
        if (Options.getInstance().getGameMode() == GameMode.LEARNING) {
            // in LEARNING ANY puzzle is accepted, that has at least one Training Step in it
            actDiffLevel = Options.getInstance().getDifficultyLevel(DifficultyType.EXTREME.ordinal());
        }
        String preGenSudoku = BackgroundGeneratorThread.getInstance().getSudoku(actDiffLevel, Options.getInstance().getGameMode());
        Sudoku2 tmpSudoku = null;
        if (preGenSudoku == null) {
            // no pregenrated puzzle available -> do it in GUI
            GenerateSudokuProgressDialog dlg = new GenerateSudokuProgressDialog(this, true, actDiffLevel, Options.getInstance().getGameMode());
            dlg.setVisible(true);
            tmpSudoku = dlg.getSudoku();
        } else {
            tmpSudoku = new Sudoku2();
            tmpSudoku.setSudoku(preGenSudoku, true);
            Sudoku2 solvedSudoku = tmpSudoku.clone();
            SudokuSolver solver = SudokuSolverFactory.getDefaultSolverInstance();
            solver.solve(actDiffLevel, solvedSudoku, true, null, false,
                    Options.getInstance().solverSteps, Options.getInstance().getGameMode());
            tmpSudoku.setLevel(solvedSudoku.getLevel());
            tmpSudoku.setScore(solvedSudoku.getScore());
        }
        if (tmpSudoku != null) {
            sudokuPanel.setSudoku(tmpSudoku, true);
            allStepsPanel.setSudoku(sudokuPanel.getSudoku());
            initializeResultPanels();
            addSudokuToHistory(tmpSudoku);
            sudokuPanel.clearColoring();
            sudokuPanel.setShowHintCellValue(0);
            sudokuPanel.setShowInvalidOrPossibleCells(false);
            if (Options.getInstance().getGameMode() == GameMode.LEARNING) {
                // solve the sudoku up until the first trainingStep
                Sudoku2 trainingSudoku = sudokuPanel.getSudoku();
                List<SolutionStep> steps = sudokuPanel.getSolver().getSteps();
                for (SolutionStep step : steps) {
                    if (step.getType().getStepConfig().isEnabledTraining()) {
                        break;
                    } else {
                        //System.out.println("doStep(): " + step.getType().getStepName());
                        sudokuPanel.getSolver().doStep(trainingSudoku, step);
                    }
                }
            }
            clearSavePoints();
            sudokuFileName = null;
            setTitleWithFile();
            check();
        }
        setSpielen(true);
        fixFocus();
    }//GEN-LAST:event_neuesSpielToolButtonActionPerformed

    private void levelComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_levelComboBoxActionPerformed
//        level = Options.getInstance().getDifficultyLevels()[levelComboBox.getSelectedIndex() + 1];
        Options.getInstance().setActLevel(Options.getInstance().getDifficultyLevels()[levelComboBox.getSelectedIndex() + 1].getOrdinal());
        BackgroundGeneratorThread.getInstance().setNewLevel(Options.getInstance().getActLevel());
        check();
        fixFocus();
    }//GEN-LAST:event_levelComboBoxActionPerformed

    private void levelExtremMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_levelExtremMenuItemActionPerformed
        setLevelFromMenu();
    }//GEN-LAST:event_levelExtremMenuItemActionPerformed

    private void levelSchwerMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_levelSchwerMenuItemActionPerformed
        setLevelFromMenu();
    }//GEN-LAST:event_levelSchwerMenuItemActionPerformed

    private void levelKniffligMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_levelKniffligMenuItemActionPerformed
        setLevelFromMenu();
    }//GEN-LAST:event_levelKniffligMenuItemActionPerformed

    private void levelMittelMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_levelMittelMenuItemActionPerformed
        setLevelFromMenu();
    }//GEN-LAST:event_levelMittelMenuItemActionPerformed

    private void levelLeichtMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_levelLeichtMenuItemActionPerformed
        setLevelFromMenu();
    }//GEN-LAST:event_levelLeichtMenuItemActionPerformed

    private void f1ToggleButtonActionPerformed1(ActionEvent evt) {//GEN-FIRST:event_f1ToggleButtonActionPerformed1
        f1ToggleButtonActionPerformed(evt);
    }//GEN-LAST:event_f1ToggleButtonActionPerformed1

    private void f1ToggleButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_f1ToggleButtonActionPerformed
        //System.out.println(evt);
        setToggleButton((JToggleButton) evt.getSource(), (evt.getModifiers() & KeyEvent.CTRL_MASK) != 0);
    }//GEN-LAST:event_f1ToggleButtonActionPerformed

    private void redGreenToggleButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_redGreenToggleButtonActionPerformed
        sudokuPanel.setInvalidCells(!sudokuPanel.isInvalidCells());
        sudokuPanel.repaint();
        check();
        fixFocus();
    }//GEN-LAST:event_redGreenToggleButtonActionPerformed

    private void mediumHintMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_mediumHintMenuItemActionPerformed
        getHint(1);
    }//GEN-LAST:event_mediumHintMenuItemActionPerformed

    private void vageHintMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_vageHintMenuItemActionPerformed
        getHint(0);
    }//GEN-LAST:event_vageHintMenuItemActionPerformed

    private void alleHiddenSinglesSetzenMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_alleHiddenSinglesSetzenMenuItemActionPerformed
        hinweisAbbrechenButtonActionPerformed(null);
        SolutionStep step = null;
        while ((step = sudokuPanel.getNextStep(true)) != null
                && (step.getType() == SolutionType.HIDDEN_SINGLE || step.getType() == SolutionType.FULL_HOUSE
                || step.getType() == SolutionType.NAKED_SINGLE)) {
            sudokuPanel.doStep();
        }
        sudokuPanel.abortStep();
        sudokuPanel.checkProgress();
        fixFocus();
        repaint();
    }//GEN-LAST:event_alleHiddenSinglesSetzenMenuItemActionPerformed

    private void hinweisAbbrechenButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_hinweisAbbrechenButtonActionPerformed
        abortStep();
//        sudokuPanel.abortStep();
//        hinweisTextArea.setText("");
//        hinweisAbbrechenButton.setEnabled(false);
//        hinweisAusfuehrenButton.setEnabled(false);
//        fixFocus();
    }//GEN-LAST:event_hinweisAbbrechenButtonActionPerformed

    private void hinweisAusfuehrenButtonActionPerformed(ActionEvent evt) {
        sudokuPanel.doStep();
        sudokuPanel.checkProgress();
        hinweisTextArea.setText("");
        hinweisAbbrechenButton.setEnabled(false);
        hinweisAusfuehrenButton.setEnabled(false);
        if (executeStepToggleButton != null) {
            executeStepToggleButton.setEnabled(false);
        }
        if (abortStepToggleButton != null) {
            abortStepToggleButton.setEnabled(false);
        }
        fixFocus();
    }

    private void loesungsSchrittMenuItemActionPerformed(ActionEvent evt) {
        getHint(2);
    }

    private void neuerHinweisButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_neuerHinweisButtonActionPerformed
        loesungsSchrittMenuItemActionPerformed(evt);
    }//GEN-LAST:event_neuerHinweisButtonActionPerformed

    private void neuMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_neuMenuItemActionPerformed
        neuesSpielToolButtonActionPerformed(null);
    }//GEN-LAST:event_neuMenuItemActionPerformed

    private void copyCluesMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_copyCluesMenuItemActionPerformed
        copyToClipboard(ClipboardMode.CLUES_ONLY, false);
    }//GEN-LAST:event_copyCluesMenuItemActionPerformed

    private void showWrongValuesMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showWrongValuesMenuItemActionPerformed
        sudokuPanel.setShowWrongValues(showWrongValuesMenuItem.isSelected());
        check();
        fixFocus();
    }//GEN-LAST:event_showWrongValuesMenuItemActionPerformed

    private void showCandidatesMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showCandidatesMenuItemActionPerformed
        if (!showCandidatesMenuItem.isSelected()) {
            // just set the flag and be done!
            sudokuPanel.setShowCandidates(showCandidatesMenuItem.isSelected());
        } else {
            // if no user candidates have been set, the internal flag is just toggled.
            // if user candidates have been set, further checks have to be made
            if (sudokuPanel.getSudoku().userCandidatesEmpty()) {
                // just set the flag and be done!
                sudokuPanel.setShowCandidates(showCandidatesMenuItem.isSelected());
            } else {
                // display a dialog, that lets the user choose, what to do
                boolean doYes = true;
                if (!sudokuPanel.getSudoku().checkUserCands()) {
                    // necessary candidates are missing!
                    int ret = JOptionPane.showConfirmDialog(null,
                            PropertyUtil.getProperty("intl/MainFrame", "MainFrame.candidatesMissing"),
                            PropertyUtil.getProperty("intl/MainFrame", "MainFrame.error"),
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    if (ret == JOptionPane.CANCEL_OPTION) {
                        // change the menu item!
                        showCandidatesMenuItem.setSelected(false);
                        fixFocus();
                        return;
                    } else if (ret == JOptionPane.YES_OPTION) {
                        doYes = true;
                    } else {
                        doYes = false;
                    }
                }
                if (doYes) {
                    // retain all changes to the user candidates
                    sudokuPanel.getSudoku().switchToAllCandidates();
                    sudokuPanel.getSolver().setSudoku(sudokuPanel.getSudoku());
                    sudokuPanel.checkProgress();
                    sudokuPanel.setShowCandidates(showCandidatesMenuItem.isSelected());
                } else {
                    // revert all changes
                    sudokuPanel.getSudoku().rebuildAllCandidates();
                    sudokuPanel.getSolver().setSudoku(sudokuPanel.getSudoku());
                    sudokuPanel.checkProgress();
                    sudokuPanel.setShowCandidates(showCandidatesMenuItem.isSelected());
                }
            }
        }
        check();
        fixFocus();
    }//GEN-LAST:event_showCandidatesMenuItemActionPerformed

    private void redoToolButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_redoToolButtonActionPerformed
        sudokuPanel.redo();
        allStepsPanel.setSudoku(sudokuPanel.getSudoku());
        allStepsPanel.resetPanel();
        check();
        fixFocus();
    }//GEN-LAST:event_redoToolButtonActionPerformed

    private void redoMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_redoMenuItemActionPerformed
        sudokuPanel.redo();
        allStepsPanel.setSudoku(sudokuPanel.getSudoku());
        allStepsPanel.resetPanel();
        check();
        fixFocus();
    }//GEN-LAST:event_redoMenuItemActionPerformed

    private void undoToolButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_undoToolButtonActionPerformed
        sudokuPanel.undo();
        allStepsPanel.setSudoku(sudokuPanel.getSudoku());
        allStepsPanel.resetPanel();
        check();
        fixFocus();
    }//GEN-LAST:event_undoToolButtonActionPerformed

    private void undoMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_undoMenuItemActionPerformed
        sudokuPanel.undo();
        allStepsPanel.setSudoku(sudokuPanel.getSudoku());
        allStepsPanel.resetPanel();
        check();
        fixFocus();
    }//GEN-LAST:event_undoMenuItemActionPerformed

    private void pasteMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_pasteMenuItemActionPerformed
        try {
            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable clipboardContent = clip.getContents(this);
            if ((clipboardContent != null) && (clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))) {
                String content = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
                setPuzzle(content);
                clearSavePoints();
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error pasting from clipboard", ex);
        }
        check();
        fixFocus();
    }//GEN-LAST:event_pasteMenuItemActionPerformed

private void outerSplitPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_outerSplitPanePropertyChange
    // if the hintPanel is to small, the horizontal divider is moved up
    if (!outerSplitPaneInitialized && outerSplitPane.getSize().getHeight() != 0
            && hintPanel.getSize().getHeight() != 0) {
        // adjust to minimum size of hintPanel to allow for LAF differences
        outerSplitPaneInitialized = true; // beware of recursion!
        int diff = (int) (hintPanel.getMinimumSize().getHeight() - hintPanel.getSize().getHeight());
        if (diff > 0) {
            resetHDivLocLoc = outerSplitPane.getDividerLocation() - diff - 5;
            outerSplitPane.setDividerLocation(resetHDivLocLoc);
//            System.out.println("Divider adjusted (" + (diff + 1) + ")!");
//            System.out.println("   absolut position: " + outerSplitPane.getDividerLocation());
        }
        outerSplitPaneInitialized = false;
//        System.out.println("outerSplitPaneinitialized = true!");
    }
//    System.out.println("gdl: " + outerSplitPane.getDividerLocation() + " (" +
//            outerSplitPaneInitialized + "/" + outerSplitPane.getSize().getHeight() + "/" +
//            hintPanel.getMinimumSize().getHeight() + "/" + hintPanel.getSize().getHeight() + "/" + resetHDivLocLoc);
    // if the window layout is reset, the horizontal divider is moved back to its
    // default location; since we dont know, how large toolbar and statu line are
    // in each and every laf, this value is too small and has to be
    // adjusted again!
    if (resetHDivLoc && outerSplitPane.getDividerLocation() != resetHDivLocLoc) {
        resetHDivLoc = false;
        if (System.currentTimeMillis() - resetHDivLocTicks < 1000) {
//            System.out.println("Reset adjusted!");
            outerSplitPane.setDividerLocation(resetHDivLocLoc);
            setSize(getWidth() + 1, getHeight());
        } else {
//            System.out.println("Reset: nothing done!");
        }
    }
}//GEN-LAST:event_outerSplitPanePropertyChange

private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
    try {
        if (!changingFullScreenMode) {
            writeOptionsWithWindowState(null);
        }
        changingFullScreenMode = false;
    } catch (FileNotFoundException ex) {
        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Can't write options", ex);
    }
}//GEN-LAST:event_formWindowClosed

private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    formWindowClosed(null);
}//GEN-LAST:event_formWindowClosing

private void solveUpToButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_solveUpToButtonActionPerformed
    if (sudokuPanel != null) {
        sudokuPanel.solveUpTo();
    }
    check();
    fixFocus();
}//GEN-LAST:event_solveUpToButtonActionPerformed

private void aboutMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
    new AboutDialog(this, true).setVisible(true);
    check();
    fixFocus();
}//GEN-LAST:event_aboutMenuItemActionPerformed

private void keyMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_keyMenuItemActionPerformed
    new KeyboardLayoutFrame().setVisible(true);
    check();
    fixFocus();
}//GEN-LAST:event_keyMenuItemActionPerformed

private void resetViewMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_resetViewMenuItemActionPerformed
    setWindowLayout(true);
    check();
    fixFocus();
    repaint();
}//GEN-LAST:event_resetViewMenuItemActionPerformed

private void hintPanelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_hintPanelPropertyChange
    //System.out.println("hintPanelPropertyChanged!");
    outerSplitPanePropertyChange(null);
}//GEN-LAST:event_hintPanelPropertyChange

private void spielEingebenMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_spielEingebenMenuItemActionPerformed
    // bestehendes Sudoku2 kann gelöscht werden, muss aber nicht
    if (sudokuPanel.getSolvedCellsAnz() != 0) {
        int antwort = JOptionPane.showConfirmDialog(this, PropertyUtil.getProperty("intl/MainFrame", "MainFrame.delete_sudoku"),
                PropertyUtil.getProperty("intl/MainFrame", "MainFrame.new_input"),
                JOptionPane.YES_NO_OPTION);
        if (antwort != JOptionPane.YES_OPTION) {
            // do nothing!
            return;
        }
    }
    sudokuPanel.setSudoku((String) null);
    sudokuPanel.checkProgress();
    allStepsPanel.setSudoku(sudokuPanel.getSudoku());
    resetResultPanels();
    sudokuPanel.setNoClues();
    hinweisAbbrechenButtonActionPerformed(null);
    setSpielen(false);
}//GEN-LAST:event_spielEingebenMenuItemActionPerformed

private void spielEditierenMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_spielEditierenMenuItemActionPerformed
    resetResultPanels();
    sudokuPanel.setNoClues();
    sudokuPanel.checkProgress();
    sudokuPanel.resetShowHintCellValues();
    hinweisAbbrechenButtonActionPerformed(null);
    setSpielen(false);
}//GEN-LAST:event_spielEditierenMenuItemActionPerformed

private void spielSpielenMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_spielSpielenMenuItemActionPerformed
    if (sudokuPanel.getSolvedCellsAnz() > 0) {
        sudokuPanel.setSudoku(sudokuPanel.getSudokuString(ClipboardMode.VALUES_ONLY));
        sudokuPanel.checkProgress();
        allStepsPanel.setSudoku(sudokuPanel.getSudoku());
        initializeResultPanels();
    }
    setSpielen(true);
}//GEN-LAST:event_spielSpielenMenuItemActionPerformed

private void resetSpielMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_resetSpielMenuItemActionPerformed
    if (JOptionPane.showConfirmDialog(this, PropertyUtil.getProperty("intl/MainFrame", "MainFrame.reset_game"),
            PropertyUtil.getProperty("intl/MainFrame", "MainFrame.reset"),
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        sudokuPanel.setSudoku(sudokuPanel.getSudokuString(ClipboardMode.CLUES_ONLY));
        sudokuPanel.checkProgress();
        allStepsPanel.setSudoku(sudokuPanel.getSudoku());
        allStepsPanel.resetPanel();
        repaint();
        setSpielen(true);
        check();
        fixFocus();
    }
}//GEN-LAST:event_resetSpielMenuItemActionPerformed

private void statusPanelColor1MouseClicked(MouseEvent evt) {//GEN-FIRST:event_statusPanelColor1MouseClicked
    coloringPanelClicked(0);
}//GEN-LAST:event_statusPanelColor1MouseClicked

private void statusPanelColor2MouseClicked(MouseEvent evt) {//GEN-FIRST:event_statusPanelColor2MouseClicked
    coloringPanelClicked(2);
}//GEN-LAST:event_statusPanelColor2MouseClicked

private void statusPanelColor3MouseClicked(MouseEvent evt) {//GEN-FIRST:event_statusPanelColor3MouseClicked
    coloringPanelClicked(4);
}//GEN-LAST:event_statusPanelColor3MouseClicked

private void statusPanelColor4MouseClicked(MouseEvent evt) {//GEN-FIRST:event_statusPanelColor4MouseClicked
    coloringPanelClicked(6);
}//GEN-LAST:event_statusPanelColor4MouseClicked

private void statusPanelColor5MouseClicked(MouseEvent evt) {//GEN-FIRST:event_statusPanelColor5MouseClicked
    coloringPanelClicked(8);
}//GEN-LAST:event_statusPanelColor5MouseClicked

private void statusPanelColorClearMouseClicked(MouseEvent evt) {//GEN-FIRST:event_statusPanelColorClearMouseClicked
    coloringPanelClicked(-1);
}//GEN-LAST:event_statusPanelColorClearMouseClicked

private void statusPanelColorResetMouseClicked(MouseEvent evt) {//GEN-FIRST:event_statusPanelColorResetMouseClicked
    coloringPanelClicked(-2);
}//GEN-LAST:event_statusPanelColorResetMouseClicked

private void colorCellsMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_colorCellsMenuItemActionPerformed
    sudokuPanel.setColorCells(true);
    check();
    fixFocus();
}//GEN-LAST:event_colorCellsMenuItemActionPerformed

private void colorCandidatesMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_colorCandidatesMenuItemActionPerformed
    sudokuPanel.setColorCells(false);
    check();
    fixFocus();
}//GEN-LAST:event_colorCandidatesMenuItemActionPerformed

private void cellZoomMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cellZoomMenuItemActionPerformed
    setSplitPane(cellZoomPanel);
    //initializeResultPanels();
    repaint();
}//GEN-LAST:event_cellZoomMenuItemActionPerformed

private void userManualMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_userManualMenuItemActionPerformed
    MyBrowserLauncher.getInstance().launchUserManual();
}//GEN-LAST:event_userManualMenuItemActionPerformed

private void solvingGuideMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_solvingGuideMenuItemActionPerformed
    MyBrowserLauncher.getInstance().launchSolvingGuide();
}//GEN-LAST:event_solvingGuideMenuItemActionPerformed

private void projectHomePageMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_projectHomePageMenuItemActionPerformed
    MyBrowserLauncher.getInstance().launchHomePage();
}//GEN-LAST:event_projectHomePageMenuItemActionPerformed

private void loadConfigMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_loadConfigMenuItemActionPerformed
    loadFromFile(false);
}//GEN-LAST:event_loadConfigMenuItemActionPerformed

private void saveConfigAsMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveConfigAsMenuItemActionPerformed
    saveToFile(false);
}//GEN-LAST:event_saveConfigAsMenuItemActionPerformed

private void historyMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_historyMenuItemActionPerformed
    GuiState state = new GuiState(sudokuPanel, sudokuPanel.getSolver(), solutionPanel);
    state.get(true);
    HistoryDialog dlg = new HistoryDialog(this, true);
    dlg.setVisible(true);
    String puzzle = dlg.getSelectedPuzzle();
    if (puzzle != null) {
        if (dlg.isDoubleClicked()) {
            // everything is already initialized, so dont do anything
        } else {
            // act like paste
            setPuzzle(puzzle);
        }
        clearSavePoints();
    } else {
        // restore everything
        setState(state);
    }
    state = null;
}//GEN-LAST:event_historyMenuItemActionPerformed

private void createSavePointMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_createSavePointMenuItemActionPerformed
    String defaultName =

            PropertyUtil.getProperty("intl/MainFrame", "MainFrame.createsp.default")
            + " " + (savePoints.size() + 1);
    String name = (String) JOptionPane.showInputDialog(this,
            PropertyUtil.getProperty("intl/MainFrame", "MainFrame.createsp.message"),
            PropertyUtil.getProperty("intl/MainFrame", "MainFrame.createsp.title"),
            JOptionPane.QUESTION_MESSAGE, null, null, defaultName);
    if (name != null) {
        GuiState state = new GuiState(sudokuPanel, sudokuPanel.getSolver(), solutionPanel);
        state.get(true);
        state.setName(name);
        state.setTimestamp(new Date());
        savePoints.add(state);
    }
}//GEN-LAST:event_createSavePointMenuItemActionPerformed

private void restoreSavePointMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_restoreSavePointMenuItemActionPerformed
    GuiState state = new GuiState(sudokuPanel, sudokuPanel.getSolver(), solutionPanel);
    state.get(true);
    RestoreSavePointDialog dlg = new RestoreSavePointDialog(this, true);
    dlg.setVisible(true);
    if (!dlg.isOkPressed()) {
        // restore everything
        setState(state);
    }
    state = null;
}//GEN-LAST:event_restoreSavePointMenuItemActionPerformed

private void playingMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_playingMenuItemActionPerformed
    setMode(GameMode.PLAYING, true);
    check();
}//GEN-LAST:event_playingMenuItemActionPerformed

private void learningMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_learningMenuItemActionPerformed
    setMode(GameMode.LEARNING, true);
    check();
}//GEN-LAST:event_learningMenuItemActionPerformed

private void practisingMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_practisingMenuItemActionPerformed
    setMode(GameMode.PRACTISING, true);
    check();
}//GEN-LAST:event_practisingMenuItemActionPerformed

private void backdoorSearchMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_backdoorSearchMenuItemActionPerformed
    new BackdoorSearchDialog(this, true, sudokuPanel).setVisible(true);
}//GEN-LAST:event_backdoorSearchMenuItemActionPerformed

private void setGivensMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_setGivensMenuItemActionPerformed
    SetGivensDialog dlg = new SetGivensDialog(this, true);
    dlg.setVisible(true);
    if (dlg.isOkPressed()) {
        String givens = dlg.getGivens();
        sudokuPanel.setGivens(givens);
    }
}//GEN-LAST:event_setGivensMenuItemActionPerformed

private void fullScreenMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_fullScreenMenuItemActionPerformed
    if (fullScreenMenuItem.isSelected()) {
        changingFullScreenMode = true;
        saveWindowStateInOptions();
        dispose();
        setUndecorated(true);
        setExtendedState(MAXIMIZED_BOTH);
        hintPanel.setVisible(false);
        jToolBar1.setVisible(true);
        showToolBarMenuItem.setEnabled(false);
        showHintPanelMenuItem.setEnabled(false);
        setVisible(true);
    } else {
        changingFullScreenMode = true;
        dispose();
        setUndecorated(false);
        setExtendedState(NORMAL);
//        hintPanel.setVisible(true);
//        jToolBar1.setVisible(true);
        showToolBarMenuItem.setEnabled(true);
        showHintPanelMenuItem.setEnabled(true);
        setWindowLayout(false);
        setVisible(true);
    }
    check();
    fixFocus();
}//GEN-LAST:event_fullScreenMenuItemActionPerformed

private void showHintPanelMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showHintPanelMenuItemActionPerformed
    Options.getInstance().setShowHintPanel(showHintPanelMenuItem.isSelected());
    hintPanel.setVisible(showHintPanelMenuItem.isSelected());
    if (Options.getInstance().isShowHintPanel()) {
        int horzDivLoc = Options.getInstance().getInitialHorzDividerLoc();
        if (horzDivLoc > getHeight() - 204) {
            horzDivLoc = getHeight() - 204;
            Options.getInstance().setInitialHorzDividerLoc(horzDivLoc);
        }
        outerSplitPane.setDividerLocation(horzDivLoc);
    }
}//GEN-LAST:event_showHintPanelMenuItemActionPerformed

private void showToolBarMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showToolBarMenuItemActionPerformed
    Options.getInstance().setShowToolBar(showToolBarMenuItem.isSelected());
    jToolBar1.setVisible(showToolBarMenuItem.isSelected());
}//GEN-LAST:event_showToolBarMenuItemActionPerformed

private void fxyToggleButtonf1ToggleButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_fxyToggleButtonf1ToggleButtonActionPerformed
    setToggleButton((JToggleButton) evt.getSource(), false);
}//GEN-LAST:event_fxyToggleButtonf1ToggleButtonActionPerformed

private void showHintButtonsCheckBoxMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showHintButtonsCheckBoxMenuItemActionPerformed
    Options.getInstance().setShowHintButtonsInToolbar(showHintButtonsCheckBoxMenuItem.isSelected());
    setShowHintButtonsInToolbar();
}//GEN-LAST:event_showHintButtonsCheckBoxMenuItemActionPerformed

private void extendedPrintMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_extendedPrintMenuItemActionPerformed
    new ExtendedPrintDialog(this, true).setVisible(true);
}//GEN-LAST:event_extendedPrintMenuItemActionPerformed

    private void copySSMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_copySSMenuItemActionPerformed
        copyToClipboard(null, true);
    }//GEN-LAST:event_copySSMenuItemActionPerformed

    private void savePuzzleMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_savePuzzleMenuItemActionPerformed
        if (sudokuFileName != null) {
            try {
                saveToFile(true, sudokuFileName, sudokuFileType);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.toString(), PropertyUtil.getProperty("intl/MainFrame", "MainFrame.error"), JOptionPane.ERROR_MESSAGE);
                sudokuFileName = null;
            }
        }
    }//GEN-LAST:event_savePuzzleMenuItemActionPerformed

    private void showColorKuMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_showColorKuMenuItemActionPerformed
        Options.getInstance().setShowColorKuAct(showColorKuMenuItem.isSelected());
        sudokuPanel.setShowColorKu();
        check();
        fixFocus();
    }//GEN-LAST:event_showColorKuMenuItemActionPerformed

    private void askQuestionMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_askQuestionMenuItemActionPerformed
        MyBrowserLauncher.getInstance().launchForum();
    }//GEN-LAST:event_askQuestionMenuItemActionPerformed

    private void reportErrorMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_reportErrorMenuItemActionPerformed
        MyBrowserLauncher.getInstance().launchTracker();
    }//GEN-LAST:event_reportErrorMenuItemActionPerformed

    /**
     * Adjusts icons for hint toggle buttons according to the mode (normal/ColorKu) and according
     * to the colors (necessary for color changes). Icons are created on the fly
     * as necessary.
     * @param on
     */
    private void prepareToggleButtonIcons(boolean on) {
        if (on) {
            for (int i = 0, lim = toggleButtons.length - 1; i < lim; i++) {
                if (toggleButtonImagesColorKu[i] == null || !toggleButtonImagesColorKu[i].getColor().equals(Options.getInstance().getColorKuColor(i + 1))) {
                    // create a new image (we need the image to access the color without needing yet another class)
                    toggleButtonImagesColorKu[i] = new ColorKuImage(TOGGLE_BUTTON_ICON_SIZE, Options.getInstance().getColorKuColor(i + 1));
                    toggleButtonIconsColorKu[i] = new ImageIcon(toggleButtonImagesColorKu[i]);
                }
                toggleButtonIcons[i] = toggleButtonIconsColorKu[i];
                emptyToggleButtonIcons[i] = emptyToggleButtonIconOrgColorKu;
            }
//            emptyToggleButtonIcon = emptyToggleButtonIconOrgColorKu;
        } else {
            for (int i = 0, lim = toggleButtons.length - 1; i < lim; i++) {
                toggleButtonIcons[i] = toggleButtonIconsOrg[i];
                emptyToggleButtonIcons[i] = emptyToggleButtonIconsOrg[i];
            }
//            emptyToggleButtonIcon = emptyToggleButtonIconOrg;
        }
    }

    /**
     * Gets a new hint for the sudoku if possible. Checks are made to ensure,
     * that hints are only displayed for valid puzzles.
     *
     * @param mode <code>0</code> for "vage hint", <code>1</code> for "concrete
     * hint" and <code>2</code> for "show next step".
     */
    private void getHint(int mode) {
        if (sudokuPanel.getSudoku().isSolved()) {
            JOptionPane.showMessageDialog(this, PropertyUtil.getProperty("intl/MainFrame", "MainFrame.already_solved"));
            return;
        }
        if (sudokuPanel.getSudoku().getStatus() == SudokuStatus.EMPTY
                || sudokuPanel.getSudoku().getStatus() == SudokuStatus.INVALID) {
            JOptionPane.showMessageDialog(this, PropertyUtil.getProperty("intl/MainFrame", "MainFrame.invalid_puzzle"));
            return;
        }
        if (!sudokuPanel.isShowCandidates()) {
            JOptionPane.showMessageDialog(this, PropertyUtil.getProperty("intl/MainFrame", "MainFrame.not_available"),
                    PropertyUtil.getProperty("intl/MainFrame", "MainFrame.hint"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (sudokuPanel.getSudoku().checkSudoku() == false) {
            JOptionPane.showMessageDialog(this,
                    PropertyUtil.getProperty("intl/MainFrame", "MainFrame.invalid_values_or_candidates"),
                    PropertyUtil.getProperty("intl/MainFrame", "MainFrame.hint"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SolutionStep step = sudokuPanel.getNextStep(false);
        if (mode == 0 || mode == 1) {
            sudokuPanel.abortStep();
            fixFocus();
            if (step != null) {
                int strMode = 0;
                String msg = PropertyUtil.getProperty("intl/MainFrame", "MainFrame.vage_hint");
                if (mode == 1) {
                    strMode = 1;
                    msg = PropertyUtil.getProperty("intl/MainFrame", "MainFrame.medium_hint");
                }
                JOptionPane.showMessageDialog(this,
                        PropertyUtil.getProperty("intl/MainFrame", "MainFrame.possible_step")
                        + step.toString(strMode), msg, JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        PropertyUtil.getProperty("intl/MainFrame", "MainFrame.dont_know"),
                        PropertyUtil.getProperty("intl/MainFrame", "MainFrame.error"),
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            if (step != null) {
                setSolutionStep(step, false);
            } else {
                hinweisTextArea.setText(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.dont_know"));
                hinweisTextArea.setCaretPosition(0);
            }
        }
        fixFocus();
        check();
    }

    /**
     * Sets a new mode ({@link GameMode#LEARNING}, {@link GameMode#PLAYING} or
     * {@link GameMode#PRACTISING}). If the new mode is "playing", no further
     * action is necessary. If the new mode is "learning" or "practising",
     * steps have to be selected.<br>
     * If a user tries to set "learning" or "practising", but doesnt select any steps,
     * "playing" is set.<br>
     * If the configuration dialog is cancelled, the mode is not changed.
     * @param newMode
     * @param showDialog
     */
    private void setMode(GameMode newMode, boolean showDialog) {
        if (newMode == GameMode.PLAYING) {
            Options.getInstance().setGameMode(newMode);
        } else {
            if (showDialog) {
                // show config dialog
                ConfigTrainingDialog dlg = new ConfigTrainingDialog(this, true);
                dlg.setVisible(true);
                if (!dlg.isOkPressed()) {
                    return;
                }
            }
            String techniques = Options.getInstance().getTrainingStepsString(true);
            if (techniques.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        PropertyUtil.getProperty("intl/MainFrame", "MainFrame.notechniques"),
                        PropertyUtil.getProperty("intl/MainFrame", "MainFrame.error"), JOptionPane.ERROR_MESSAGE);
                Options.getInstance().setGameMode(GameMode.PLAYING);
            } else {
                Options.getInstance().setGameMode(newMode);
            }
        }
    }

    /**
     * Sets a puzzle and initializes all views. Used by {@link #pasteMenuItemActionPerformed} and
     * {@link #historyMenuItemActionPerformed(ActionEvent)}. This
     * method should only be used if the puzzle is only available as String. If more state information
     * is saved, use {@link #setState(GuiState)} instead.
     * @param puzzle
     */
    public void setPuzzle(String puzzle) {
        try {
            sudokuPanel.setSudoku(puzzle);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error setting sudoku in SudokuPanel", ex);
        }
        allStepsPanel.setSudoku(sudokuPanel.getSudoku());
        initializeResultPanels();
        sudokuPanel.clearColoring();
        sudokuPanel.setShowHintCellValue(0);
        sudokuPanel.setShowInvalidOrPossibleCells(false);
        setSpielen(true);
        check();
        repaint();
    }

    /**
     * Restores a complete GUI state including puzzle (optionally with coloring
     * and selected step), solutions and summary. Used by {@link #loadFromFile(boolean)} and
     * {@link RestoreSavePointDialog}.<br>
     * @param state
     */
    public void setState(GuiState state) {
        state.set();
        summaryPanel.initialize(SudokuSolverFactory.getDefaultSolverInstance());
        allStepsPanel.setSudoku(sudokuPanel.getSudoku());
        allStepsPanel.resetPanel();
        setSolutionStep(state.getStep(), true);
        setSpielen(true);
        check();
        repaint();
    }

    /**
     * Adds a new sudoku to the creation history. The size of the history buffer
     * is adjusted accordingly. New sudokus are always inserted at the start of
     * the list and deleted from the end of the list, effectively turning the list in
     * a queue (the performance overhead can be ignored here).
     * @param sudoku
     */
    private void addSudokuToHistory(Sudoku2 sudoku) {
        Options.getInstance().addSudokuToHistory(sudoku);
    }

    /**
     * Old GuiStates remain in memory as long as they are not overwritten.
     * Since that can comsume quite a lot of memory, they should be nulled
     * out before clearing the list.
     */
    private void clearSavePoints() {
        for (int i = 0; i < savePoints.size(); i++) {
            savePoints.set(i, null);
        }
        savePoints.clear();
    }

    /**
     * Should be called only from {@link CellZoomPanel}.
     * @param colorNumber
     * @param isCell
     */
    public void setColoring(int colorNumber, boolean isCell) {
        sudokuPanel.setColorCells(isCell);
        coloringPanelClicked(colorNumber);
        check();
        fixFocus();
    }

    public void coloringPanelClicked(int colorNumber) {
        if (colorNumber == -1 || colorNumber == -2) {
            statusPanelColorResult.setBackground(Options.getInstance().getDefaultCellColor());
            sudokuPanel.setActiveColor(-1);
            if (colorNumber == -2) {
                sudokuPanel.clearColoring();
                repaint();
            }
        } else {
            statusPanelColorResult.setBackground(Options.getInstance().getColoringColors()[colorNumber]);
            sudokuPanel.setActiveColor(colorNumber);
        }
    }

    private void tabPaneMouseClicked(MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1) {
            //System.out.println("tab clicked: " + tabPane.getSelectedIndex());
            switch (tabPane.getSelectedIndex()) {
                case 0:
                    summaryMenuItem.setSelected(true);
                    break;
                case 1:
                    solutionMenuItem.setSelected(true);
                    break;
                case 2:
                    allStepsMenuItem.setSelected(true);
                    break;
                case 3:
                    cellZoomMenuItem.setSelected(true);
                    break;
            }
        }
    }

    private void saveWindowStateInOptions() {
        // save the complete window state
        Options o = Options.getInstance();
        o.setInitialXPos(getX());
        o.setInitialYPos(getY());
        o.setInitialHeight(getHeight());
        o.setInitialWidth(getWidth());
        // the horizontal divider pos mustnt be saved if the panel is not visible!
        if (o.isShowHintPanel()) {
            o.setInitialHorzDividerLoc(outerSplitPane.getDividerLocation());
        }
        o.setInitialDisplayMode(0); // sudoku only
        if (summaryMenuItem.isSelected()) {
            o.setInitialDisplayMode(1);
        }
        if (solutionMenuItem.isSelected()) {
            o.setInitialDisplayMode(2);
        }
        if (allStepsMenuItem.isSelected()) {
            o.setInitialDisplayMode(3);
        }
        if (cellZoomMenuItem.isSelected()) {
            o.setInitialDisplayMode(4);
        }
        o.setInitialVertDividerLoc(-1);
        if (o.getInitialDisplayMode() != 0) {
            splitPanel.getDividerLocation();
        }
    }

    private void writeOptionsWithWindowState(String fileName) throws FileNotFoundException {
        // save window state
//        System.out.println("writeOptionsWithWindowState(" + fileName + ") called!");
        saveWindowStateInOptions();
        if (fileName == null) {
            Options.getInstance().writeOptions();
        } else {
            Options.getInstance().writeOptions(fileName);
        }
    }

    private void setWindowLayout(boolean reset) {
        Options o = Options.getInstance();
        //System.out.println("initialHorzDividerLoc: " + o.initialHorzDividerLoc);

        if (reset) {
            o.setInitialDisplayMode(Options.INITIAL_DISP_MODE);
            o.setInitialHeight(Options.INITIAL_HEIGHT);
            o.setInitialHorzDividerLoc(Options.INITIAL_HORZ_DIVIDER_LOC);
            o.setInitialVertDividerLoc(Options.INITIAL_VERT_DIVIDER_LOC);
            o.setInitialWidth(Options.INITIAL_WIDTH);
            o.setShowHintPanel(Options.INITIAL_SHOW_HINT_PANEL);
            o.setShowToolBar(Options.INITIAL_SHOW_TOOLBAR);
        }
        //System.out.println("initialHorzDividerLoc: " + o.initialHorzDividerLoc);

        jToolBar1.setVisible(o.isShowToolBar());
        hintPanel.setVisible(o.isShowHintPanel());

        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension screenSize = t.getScreenSize();
//        System.out.println("setWindowLayout() - init: " + getWidth() + "/" + getHeight() + "/" + screenSize);
        int width = o.getInitialWidth();
        int height = o.getInitialHeight();
        int horzDivLoc = o.getInitialHorzDividerLoc();
//        System.out.println("soll: " + width + "/" + height + "/" + horzDivLoc);

        if (screenSize.height - 45 < height) {
            height = screenSize.height - 45;
        }
        if (horzDivLoc > height - 204) {
            horzDivLoc = height - 204;
            // can be used during program run, so has to be saved here
            Options.getInstance().setInitialHorzDividerLoc(horzDivLoc);
        }
        if (screenSize.width - 20 < width) {
            width = screenSize.width - 20;
        }
//        System.out.println("adjusted: " + width + "/" + height + "/" + horzDivLoc);
        setSize(width, height);
        switch (o.getInitialDisplayMode()) {
            case 0:
                splitPanel.setRight(null);
                sudokuOnlyMenuItem.setSelected(true);
                break;
            case 1:
                setSplitPane(summaryPanel);
                summaryMenuItem.setSelected(true);
                break;
            case 2:
                setSplitPane(solutionPanel);
                solutionMenuItem.setSelected(true);
                break;
            case 3:
                allStepsPanel.setSudoku(sudokuPanel.getSudoku());
                setSplitPane(allStepsPanel);
                allStepsMenuItem.setSelected(true);
                break;
            case 4:
                setSplitPane(cellZoomPanel);
                cellZoomMenuItem.setSelected(true);
        }
        if (o.getInitialVertDividerLoc() != -1) {
            splitPanel.setDividerLocation(o.getInitialVertDividerLoc());
        }
//        System.out.println("horzDivLoc: " + horzDivLoc);
        outerSplitPane.setDividerLocation(horzDivLoc);

        // doesnt work at reset sometimes -> adjust in PropertyChangeListener
        // doesnt work when going back from fullscreen mode either
//        if (reset) {
        outerSplitPaneInitialized = false;
        resetHDivLocLoc = horzDivLoc;
        resetHDivLocTicks = System.currentTimeMillis();
        resetHDivLoc = true;
        //System.out.println("reset: " + resetHDivLocLoc);
//        }
    }

    private void resetResultPanels() {
        summaryPanel.initialize(null);
        solutionPanel.initialize(null);
        allStepsPanel.resetPanel();
    }

    private void initializeResultPanels() {
        summaryPanel.initialize(sudokuPanel.getSolver());
        solutionPanel.initialize(sudokuPanel.getSolver().getSteps());
        allStepsPanel.resetPanel();
    }

    private void setSplitPane(JPanel panel) {
        if (!splitPanel.hasRight()) {
            splitPanel.setRight(tabPane);
        }
        tabPane.setSelectedComponent(panel);
    }

    private void setSpielen(boolean isSpielen) {
        eingabeModus = !isSpielen;
        if (isSpielen) {
            if (oldShowDeviationsValid) {
                showDeviationsMenuItem.setSelected(oldShowDeviations);
                oldShowDeviationsValid = false;
            }
        } else {
            oldShowDeviations = showDeviationsMenuItem.isSelected();
            oldShowDeviationsValid = true;
            showDeviationsMenuItem.setSelected(false);
        }
        showDeviationsMenuItemActionPerformed(null);

        vageHintMenuItem.setEnabled(isSpielen);
        mediumHintMenuItem.setEnabled(isSpielen);
        loesungsSchrittMenuItem.setEnabled(isSpielen);
        alleHiddenSinglesSetzenMenuItem.setEnabled(isSpielen);
        showDeviationsMenuItem.setEnabled(isSpielen);
        showColorKuMenuItem.setEnabled(isSpielen);

        spielSpielenMenuItem.setEnabled(!isSpielen);
        spielEditierenMenuItem.setEnabled(isSpielen);
    }

    public void setSolutionStep(SolutionStep step, boolean setInSudokuPanel) {
        if (setInSudokuPanel) {
            if (step == null) {
                sudokuPanel.abortStep();
            } else {
                sudokuPanel.setStep(step);
            }
        }
        if (step == null) {
            hinweisTextArea.setText("");
            hinweisAbbrechenButton.setEnabled(false);
            hinweisAusfuehrenButton.setEnabled(false);
            if (executeStepToggleButton != null) {
                executeStepToggleButton.setEnabled(false);
            }
            if (abortStepToggleButton != null) {
                abortStepToggleButton.setEnabled(false);
            }
        } else {
            hinweisTextArea.setText(step.toString());
            hinweisTextArea.setCaretPosition(0);
            hinweisAbbrechenButton.setEnabled(true);
            hinweisAusfuehrenButton.setEnabled(true);
            getRootPane().setDefaultButton(hinweisAusfuehrenButton);
            if (executeStepToggleButton != null) {
                executeStepToggleButton.setEnabled(true);
            }
            if (abortStepToggleButton != null) {
                abortStepToggleButton.setEnabled(true);
            }
        }
        fixFocus();
    }

    /**
     * Copy the current sudoku to the clipboard. If <code>simpleSudoku</code>
     * is set to <code>true</code>, the givensm the currently set cells and
     * a PM grid are copied.
     *
     * @param mode
     * @param simpleSudoku
     */
    private void copyToClipboard(ClipboardMode mode, boolean simpleSudoku) {
        String clipStr = "";
        if (simpleSudoku) {
            String dummy = sudokuPanel.getSudokuString(ClipboardMode.CLUES_ONLY);
            clipStr = SudokuUtil.getSSFormatted(dummy);
            clipStr += SudokuUtil.NEW_LINE;
            clipStr += SudokuUtil.NEW_LINE;
            dummy = sudokuPanel.getSudokuString(ClipboardMode.VALUES_ONLY);
            clipStr += SudokuUtil.getSSFormatted(dummy);
            clipStr += SudokuUtil.NEW_LINE;
            clipStr += SudokuUtil.NEW_LINE;
            dummy = sudokuPanel.getSudokuString(ClipboardMode.PM_GRID);
            clipStr += SudokuUtil.getSSPMGrid(dummy);
        } else {
            clipStr = sudokuPanel.getSudokuString(mode);
        }
        try {
            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection content = new StringSelection(clipStr);
            clip.setContents(content, null);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error writing to clipboard", ex);
        }
        fixFocus();
    }

    private void setToggleButton(JToggleButton button, boolean ctrlPressed) {
        if (button == null) {
            sudokuPanel.resetShowHintCellValues();
        } else {
            int index = 0;
            for (index = 0; index < toggleButtons.length; index++) {
                if (toggleButtons[index] == button) {
                    break;
                }
            }
            if (index == 9) {
                sudokuPanel.setShowHintCellValue(index + 1);
            } else {
                boolean isActive = sudokuPanel.getShowHintCellValues()[index + 1];
                if (ctrlPressed) {
                    sudokuPanel.getShowHintCellValues()[index + 1] = !isActive;
                    sudokuPanel.getShowHintCellValues()[10] = false;
                } else {
                    if (isActive) {
                        sudokuPanel.resetShowHintCellValues();
                    } else {
                        sudokuPanel.setShowHintCellValue(index + 1);
                    }
                }
            }
            sudokuPanel.checkIsShowInvalidOrPossibleCells();
//            if (button.isSelected()) {
//                sudokuPanel.setShowHintCellValue(index + 1);
//                sudokuPanel.setShowInvalidOrPossibleCells(true);
//            } else {
//                sudokuPanel.setShowHintCellValue(0);
//                sudokuPanel.setShowInvalidOrPossibleCells(false);
//            }
        }
        check();
        sudokuPanel.repaint();
        fixFocus();
    }

    /**
     * Save puzzles/configurations.
     * @param puzzle
     */
    private void saveToFile(boolean puzzle) {
        JFileChooser chooser = new JFileChooser(Options.getInstance().getDefaultFileDir());
        chooser.setAcceptAllFileFilterUsed(false);
        MyFileFilter[] filters = puzzleFileSaveFilters;
        if (!puzzle) {
            filters = configFileFilters;
        }
        for (int i = 0; i < filters.length; i++) {
            chooser.addChoosableFileFilter(filters[i]);
        }
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                String path = chooser.getSelectedFile().getPath();
                path = path.substring(0, path.lastIndexOf(File.separatorChar));
                Options.getInstance().setDefaultFileDir(path);
                MyFileFilter actFilter = (MyFileFilter) chooser.getFileFilter();
                int filterType = actFilter.getType();
                path = chooser.getSelectedFile().getAbsolutePath();
                if (!puzzle) {
                    // Options
                    if (!path.endsWith("." + configFileExt)) {
                        path += "." + configFileExt;
                    }
                } else {
                    if (filterType == 1) {
                        if (!path.endsWith("." + solutionFileExt)) {
                            path += "." + solutionFileExt;
                        }
                    } else if (filterType == 9) {
                        if (!path.endsWith("." + ssFileExt)) {
                            path += "." + ssFileExt;
                        }
                    } else {
                        if (!path.endsWith("." + textFileExt)) {
                            path += "." + textFileExt;
                        }
                    }
                }
                File checkFile = new File(path);
                if (checkFile.exists()) {
                    // Override warning!
                    MessageFormat msgf = new MessageFormat("");
                    Object[] args = new Object[]{checkFile.getName()};
                    msgf.applyPattern(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.file_exists"));
                    String warning = msgf.format(args);
                    String title = PropertyUtil.getProperty("intl/MainFrame", "MainFrame.hint");
                    if (JOptionPane.showConfirmDialog(null, warning, title, JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                saveToFile(puzzle, path, filterType);
//                } else {
//                    formatter.applyPattern(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.invalid_filename"));
//                    String msg = formatter.format(new Object[]{path});
//                    JOptionPane.showMessageDialog(this, msg,
//                            PropertyUtil.getProperty("intl/MainFrame", "MainFrame.error"), JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex2) {
                JOptionPane.showMessageDialog(this, ex2.toString(), PropertyUtil.getProperty("intl/MainFrame", "MainFrame.error"), JOptionPane.ERROR_MESSAGE);
                sudokuFileName = null;
            }
            setTitleWithFile();
        }
    }

    /**
     * Actually writes configurations and sudoku files. If <code>puzzle</code>
     * is <code>false</code>, a configuration is written. <code>filterType</code>
     * is one of the file types defined by {@link MyFileFilter}. If the method
     * is called via the "Save puzzle" menu item, <code>filterType</code> can be
     * 8 (generic text file). In this case a PM grid is written.
     *
     * @param puzzle
     * @param path
     * @param filterType
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void saveToFile(boolean puzzle, String path, int filterType) throws FileNotFoundException, IOException {
        sudokuFileName = path;
        if (!puzzle) {
            // Options
            writeOptionsWithWindowState(path);
        } else {
            // Sudoku2 und Lösung
            String newLine = System.getProperty("line.separator");
            if (filterType == 1) {
                sudokuFileType = 1;
                ZipOutputStream zOut = new ZipOutputStream(new FileOutputStream(path));
                zOut.putNextEntry(new ZipEntry("SudokuData"));
                XMLEncoder out = new XMLEncoder(zOut);
                out.writeObject(sudokuPanel.getSudoku());
                out.writeObject(SudokuSolverFactory.getDefaultSolverInstance().getAnzSteps());
                out.writeObject(SudokuSolverFactory.getDefaultSolverInstance().getSteps());
                out.writeObject(solutionPanel.getTitels());
                out.writeObject(solutionPanel.getTabSteps());
                out.writeObject(savePoints);
                out.close();
                zOut.flush();
                zOut.close();
            } else if (filterType == 9) {
                sudokuFileType = 9;
                // SimpleSudoku format (see comment in loadFromFile())
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path)));
                String clues = sudokuPanel.getSudokuString(ClipboardMode.CLUES_ONLY);
                out.println(SudokuUtil.getSSFormatted(clues));
                out.println();
                // additionally set cells
                Sudoku2 tmpSudoku = sudokuPanel.getSudoku();
                for (int i = 0; i < Sudoku2.LENGTH; i++) {
                    if (tmpSudoku.getValue(i) != 0 && !tmpSudoku.isFixed(i)) {
                        out.printf("I%02d%d%n", i, tmpSudoku.getValue(i));
                    }
                }
                // eliminated candidates
                for (int i = 0; i < Sudoku2.LENGTH; i++) {
                    if (tmpSudoku.getValue(i) == 0) {
                        for (int j = 1; j <= 9; j++) {
                            if (tmpSudoku.isValidValue(i, j) && !tmpSudoku.isCandidate(i, j)) {
                                out.printf("E%02d%03d%n", i, j);
                            }
                        }
                    }
                }
                out.close();
            } else {
                sudokuFileType = 8;
                if (filterType == 8) {
                    // generic text file: can occur when save is executed
                    filterType = 6;
                }
                BufferedWriter out = new BufferedWriter(new FileWriter(path));
                String line = "";
                switch (filterType) {
                    case 2:
                        line = sudokuPanel.getSudokuString(ClipboardMode.CLUES_ONLY);
                        break;
                    case 3:
                        line = sudokuPanel.getSudokuString(ClipboardMode.CLUES_ONLY_FORMATTED);
                        break;
                    case 4:
                        line = sudokuPanel.getSudokuString(ClipboardMode.PM_GRID);
                        break;
                    case 5:
                        line = sudokuPanel.getSudokuString(ClipboardMode.PM_GRID_WITH_STEP);
                        break;
                    case 6:
                        line = sudokuPanel.getSudokuString(ClipboardMode.CLUES_ONLY_FORMATTED);
                        line += newLine;
                        line += newLine;
                        line += sudokuPanel.getSudokuString(ClipboardMode.VALUES_ONLY_FORMATTED);
                        line += newLine;
                        line += newLine;
                        line += sudokuPanel.getSudokuString(ClipboardMode.PM_GRID);
                        break;
                    case 7:
                        line = sudokuPanel.getSudokuString(ClipboardMode.LIBRARY);
                        break;
                }
                out.write(line);
                out.close();
            }
        }
    }

    /**
     * Loads puzzles and/or configurations from files. loading either type
     * resets the mode to "playing".
     * @param puzzle
     */
    private void loadFromFile(boolean puzzle) {
        JFileChooser chooser = new JFileChooser(Options.getInstance().getDefaultFileDir());
        chooser.setAcceptAllFileFilterUsed(false);
        MyFileFilter[] filters = puzzleFileLoadFilters;
        if (!puzzle) {
            filters = configFileFilters;
        }
        for (int i = 0; i < filters.length; i++) {
            chooser.addChoosableFileFilter(filters[i]);
        }
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getPath();
            path = path.substring(0, path.lastIndexOf(File.separatorChar));
            Options.getInstance().setDefaultFileDir(path);
            path = chooser.getSelectedFile().getAbsolutePath();
            MyFileFilter filter = (MyFileFilter) chooser.getFileFilter();
            loadFromFile(path, filter.getType());
        }
    }

    /**
     * Loads a file
     * @param path
     * @param fileType 0 .. options, 1 .. sudoku from hsol, 8 .. sudoku from text file
     */
    @SuppressWarnings({"CallToThreadDumpStack", "unchecked"})
    private void loadFromFile(String path, int fileType) {
        try {
            sudokuFileName = path;
            sudokuFileType = fileType;
            if (fileType == 0) {
                // Options
                Options.readOptions(path);
                BackgroundGeneratorThread.getInstance().resetAll();
                sudokuFileName = null;
            } else if (fileType == 1) {
                // Puzzle
                ZipInputStream zIn = new ZipInputStream(new FileInputStream(path));
                zIn.getNextEntry();
                XMLDecoder in = new XMLDecoder(zIn);
                GuiState state = new GuiState(sudokuPanel, sudokuPanel.getSolver(), solutionPanel);
                // could be old file -> contains instance of Sudoku and not Sudoku2!
                Object sudokuTemp = in.readObject();
                if (sudokuTemp instanceof Sudoku2) {
                    // ok: new version!
                    state.setSudoku((Sudoku2) sudokuTemp);
                } else {
                    // old version: convert it!
                    Sudoku dummy = (Sudoku) sudokuTemp;
                    String sudokuTempLib = dummy.getSudoku(ClipboardMode.LIBRARY, null);
                    //System.out.println("sudokuTempLib: " + sudokuTempLib);
                    state.setSudoku(new Sudoku2());
                    state.getSudoku().setSudoku(sudokuTempLib, false);
                    state.getSudoku().setInitialState(dummy.getInitialState());
                    // contains another instance of Sudoku (solvedSudoku)
                    // that is not needed anymore
                    sudokuTemp = in.readObject();
                }
                state.setAnzSteps((int[]) in.readObject());
                state.setSteps((List<SolutionStep>) in.readObject());
                state.setTitels((List<String>) in.readObject());
                state.setTabSteps((List<List<SolutionStep>>) in.readObject());
                state.resetAnzSteps();
                try {
                    savePoints = (List<GuiState>) in.readObject();
                    for (int i = 0; i < savePoints.size(); i++) {
                        // internal fields must be set!
                        savePoints.get(i).initialize(sudokuPanel, SudokuSolverFactory.getDefaultSolverInstance(), solutionPanel);
                    }
                } catch (Exception ex) {
                    // when an older puzzle file is loaded, savepoints are not in the file or the format is incompatible
                    //ex.printStackTrace();
                    clearSavePoints();
                }
                in.close();
                setState(state);
                setMode(GameMode.PLAYING, true);
            } else if (fileType == 8) {
                // load from text file
                BufferedReader in = new BufferedReader(new FileReader(path));
                StringBuilder tmp = new StringBuilder();
                String line = null;
                while ((line = in.readLine()) != null) {
                    tmp.append(line);
                    tmp.append("\r\n");
                }
                in.close();
                setPuzzle(tmp.toString());
                clearSavePoints();
            } else if (fileType == 9) {
                // SimpleSudoku format: The givens followed by set cells and eliminated candidates
                // example:
//                     *-----------*
//                     |38.|...|5.6|
//                     |...|9..|...|
//                     |...|.4.|89.|
//                     |---+---+---|
//                     |.4.|6..|.3.|
//                     |9..|7.1|..4|
//                     |.7.|..8|.1.|
//                     |---+---+---|
//                     |.13|.6.|...|
//                     |...|..4|...|
//                     |2.5|...|.83|
//                     *-----------*
//
//                    I388
//                    I074
//                    I029
//                    I153
//                    I358
//                    I751
//                    E78007
//                    E78009
//                    E73009
                // Innc: nn - index of the cell, c - candidate
                // Ennccc: nn - index of the cell, ccc - candidate
                BufferedReader in = new BufferedReader(new FileReader(path));
                StringBuilder tmp = new StringBuilder();
                String line = null;
                while ((line = in.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        // all the givens are read -> abort reading
                        break;
                    }
                    tmp.append(line);
                    tmp.append("\r\n");
                }
                // first set the givens
                Sudoku2 tmpSudoku = new Sudoku2();
                tmpSudoku.setSudoku(tmp.toString());
                // now read set cells and eliminations
                while ((line = in.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    char recordType = line.charAt(0);
                    if (recordType == 'I') {
                        int index = Integer.parseInt(line.substring(1, 3));
                        int candidate = Character.digit(line.charAt(3), 10);
                        tmpSudoku.setCell(index, candidate);
                    } else if (recordType == 'E') {
                        int index = Integer.parseInt(line.substring(1, 3));
                        int candidate = Integer.parseInt(line.substring(3, 6));
                        tmpSudoku.delCandidate(index, candidate);
                    }
                }
                in.close();
                // everything read and decoded -> set the sudoku
                setPuzzle(tmpSudoku.getSudoku(ClipboardMode.LIBRARY));
                clearSavePoints();
            } else {
                formatter.applyPattern(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.invalid_filename"));
                String msg = formatter.format(new Object[]{path});
                JOptionPane.showMessageDialog(this, msg,
                        PropertyUtil.getProperty("intl/MainFrame", "MainFrame.error"), JOptionPane.ERROR_MESSAGE);
                sudokuFileName = null;
            }
        } catch (Exception ex2) {
            JOptionPane.showMessageDialog(this, ex2.toString(), PropertyUtil.getProperty("intl/MainFrame", "MainFrame.error"), JOptionPane.ERROR_MESSAGE);
            ex2.printStackTrace();
            sudokuFileName = null;
        }
        setTitleWithFile();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, "Error setting LaF", ex);
        }

        /*
            hint cw 230916
            java.awt.EventQueue.invokeLater()是将一个包含创建和显示GUI组件的代码块插入到AWT事件队列中的方法。
            使用该方法可以确保这些代码仅在AWT事件分派线程上执行，而不是在主线程上执行。
            AWT事件分派线程是一个专用的线程，负责处理GUI事件，例如用户界面操作、绘图和更新组件。

            因此，在调用GUI组件的构造函数后，调用java.awt.EventQueue.invokeLater()方法将确保GUI组件的创建和显示被插入到AWT事件队列中，
            以便在AWT事件分派线程上运行。
            如果不使用invokeLater()方法，GUI组件将在主线程上创建并显示，这可能会导致阻塞主线程，从而导致应用程序无响应。
         */
        java.awt.EventQueue.invokeLater(() -> new MainFrame(null).setVisible(true));
    }

    private void setTitleWithFile() {
        savePuzzleMenuItem.setEnabled(sudokuFileName != null);
        if (sudokuFileName == null) {
            setTitle(VERSION);
        } else {
            int index = sudokuFileName.lastIndexOf('\\') + 1;
            int index2 = sudokuFileName.lastIndexOf('/') + 1;
            if (index2 > index) {
                index = index2;
            }
            String fileName = sudokuFileName.substring(index);
            setTitle(VERSION + "  (" + fileName + ")");
        }
    }

    private void setLevelFromMenu() {
        int selected = 0;
        for (int i = 0; i < levelMenuItems.length; i++) {
            if (levelMenuItems[i].isSelected()) {
                selected = i + 1;
                break;
            }
        }
//        level = Options.getInstance().getDifficultyLevels()[selected];
        Options.getInstance().setActLevel(Options.getInstance().getDifficultyLevels()[selected].getOrdinal());
        BackgroundGeneratorThread.getInstance().setNewLevel(Options.getInstance().getActLevel());
        check();
        fixFocus();
    }

    public void stepAusfuehren() {
        hinweisAusfuehrenButtonActionPerformed(null);
    }

    public final void fixFocus() {
        sudokuPanel.requestFocusInWindow();
    }

    public SudokuPanel getSudokuPanel() {
        return sudokuPanel;
    }

    public SolutionPanel getSolutionPanel() {
        return solutionPanel;
    }

    public final void check() {
        if (sudokuPanel != null) {
            undoMenuItem.setEnabled(sudokuPanel.undoPossible());
            undoToolButton.setEnabled(sudokuPanel.undoPossible());
            redoMenuItem.setEnabled(sudokuPanel.redoPossible());
            redoToolButton.setEnabled(sudokuPanel.redoPossible());
            showCandidatesMenuItem.setSelected(sudokuPanel.isShowCandidates());
            showWrongValuesMenuItem.setSelected(sudokuPanel.isShowWrongValues());
            showDeviationsMenuItem.setSelected(sudokuPanel.isShowDeviations());
            showColorKuMenuItem.setSelected(Options.getInstance().isShowColorKuAct());
            prepareToggleButtonIcons(Options.getInstance().isShowColorKuAct());
            // either all ToggleButtons are set or none is
            if (toggleButtons[0] != null) {
                boolean[] remainingCandidates = sudokuPanel.getRemainingCandidates();
                for (int i = 0; i < remainingCandidates.length; i++) {
                    JToggleButton button = toggleButtons[i];
                    // change the standard icons
//                    if (button.getIcon() != emptyToggleButtonIcon && button.getIcon() != toggleButtonIcons[i]) {
//                        button.setIcon(toggleButtonIcons[i]);
//                    }
                    if (remainingCandidates[i]) {
//                        if (!button.isEnabled()) {
                            //button.setEnabled(true);
                        if (button.getIcon() != toggleButtonIcons[i]) {
                            button.setIcon(toggleButtonIcons[i]);
                        }
//                        }
                    } else {
//                        if (button.isEnabled()) {
//                            button.setSelected(false);
                            //button.setEnabled(false);
                        if (button.getIcon() != emptyToggleButtonIcons[i]) {
                            button.setIcon(emptyToggleButtonIcons[i]);
                        }
//                        }
                    }
                }
                for (int i = 0; i < toggleButtons.length; i++) {
                    if (toggleButtons[i].isEnabled()) {
                        if (sudokuPanel.getShowHintCellValues()[i + 1]) {
                            toggleButtons[i].setSelected(true);
                        } else {
                            toggleButtons[i].setSelected(false);
                        }
                    }
                }
            }
            redGreenToggleButton.setSelected(sudokuPanel.isInvalidCells());
            Sudoku2 sudoku = sudokuPanel.getSudoku();
            if (sudoku != null) {
                DifficultyLevel tmpLevel = sudoku.getLevel();
                if (tmpLevel != null) {
                    statusLabelLevel.setText(StepConfig.getLevelName(tmpLevel) + " (" + sudoku.getScore() + ")");
                } else {
                    statusLabelLevel.setText("-");
                }
                setProgressLabel();
            } else {
                // no puzzle loaded
                statusLabelLevel.setText(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.statusLabelLevel.text"));
            }
            if (sudokuPanel.isColorCells()) {
                colorCellsMenuItem.setSelected(true);
                statusLabelCellCandidate.setText(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.statusLabelCellCandidate.text.cell"));
            } else {
                colorCandidatesMenuItem.setSelected(true);
                statusLabelCellCandidate.setText(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.statusLabelCellCandidate.text.candidate"));
            }
            fixFocus();
        }
        // adjust mode menus and labels
        // Options.actLevel is always valid
        if (Options.getInstance().getActLevel() != -1) {
            if (Options.getInstance().getGameMode() != GameMode.PLAYING) {
                // we cant have a level that is easier than the easiest
                // selected training/practising puzzle -> we could never
                // find a new sudoku for that
                int tmpLevel = Options.getInstance().getActLevel();
                for (StepConfig act : Options.getInstance().getOrgSolverSteps()) {
                    if (act.isEnabledTraining() && act.getLevel() > tmpLevel) {
                        tmpLevel = act.getLevel();
                    }
                }
                if (tmpLevel != Options.getInstance().getActLevel()) {
//                    level = Options.getInstance().getDifficultyLevel(tmpLevel);
                    Options.getInstance().setActLevel(tmpLevel);
                }
            }
            int ord = Options.getInstance().getActLevel() - 1;
            if (levelMenuItems[ord] != null && levelComboBox.getItemCount() > ord) {
                levelMenuItems[ord].setSelected(true);
                levelComboBox.setSelectedIndex(ord);
            }
            int mOrdinal = Options.getInstance().getGameMode().ordinal();
            if (modeMenuItems != null && modeMenuItems[mOrdinal] != null) {
                modeMenuItems[mOrdinal].setSelected(true);
                String labelStr = modeMenuItems[mOrdinal].getText();
                if (labelStr.endsWith("...")) {
                    labelStr = labelStr.substring(0, labelStr.length() - 3);
                }
                if (Options.getInstance().getGameMode() != GameMode.PLAYING) {
                    labelStr += " (" + Options.getInstance().getTrainingStepsString(true) + ")";
                }
                statusLabelModus.setText(labelStr);
            }
            showHintButtonsCheckBoxMenuItem.setSelected(Options.getInstance().isShowHintButtonsInToolbar());
        }
        // repaint StatusPanels to adjust colors
        statusLinePanel.invalidate();
    }

    private boolean isStringFlavorInClipboard() {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
//        DataFlavor[] flavors = clip.getAvailableDataFlavors();
//        System.out.println("BEGIN DataFlavors");
//        for (DataFlavor df : flavors) {
//            System.out.println("   <" + df.getMimeType() + "> <" + df.getDefaultRepresentationClassAsString() + "> <" + df.getHumanPresentableName() + ">");
//        }
//        System.out.println("END DataFlavors");
        if (clip.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            return true;
        }
        return false;
    }

    private void adjustPasteMenuItem() {
        try {
            if (Main.OS_NAME.contains("mac")) {
                pasteMenuItem.setEnabled(true);
            } else {
                if (isStringFlavorInClipboard()) {
                    pasteMenuItem.setEnabled(true);
                } else {
                    pasteMenuItem.setEnabled(false);
                }
            }
        } catch (IllegalStateException ex) {
            // try again later
            clipTimer.start();
        }
    }

    @Override
    public void flavorsChanged(FlavorEvent e) {
        adjustPasteMenuItem();
    }

    private Image getIcon() {
        URL url = getClass().getResource("/img/hodoku02-32.png");
        //URL url = getClass().getResource("/img/hodoku02-16.png");
        return getToolkit().getImage(url);
    }

    public List<GuiState> getSavePoints() {
        return savePoints;
    }

    public void abortStep() {
        sudokuPanel.abortStep();
        hinweisTextArea.setText("");
        hinweisAbbrechenButton.setEnabled(false);
        hinweisAusfuehrenButton.setEnabled(false);
        if (executeStepToggleButton != null) {
            executeStepToggleButton.setEnabled(false);
        }
        if (abortStepToggleButton != null) {
            abortStepToggleButton.setEnabled(false);
        }
        fixFocus();
    }

    private void createProgressLabelImages() {
        try {
            progressImages[0] = new ImageIcon(getClass().getResource("/img/invalid20.png"));
            BufferedImage overlayImage = ImageIO.read(getClass().getResource("/img/ce1-20.png"));
            for (int i = 1; i < progressImages.length; i++) {
                BufferedImage act = new BufferedImage(20, 20, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D gAct = (Graphics2D) act.getGraphics();
                gAct.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                gAct.setColor(Options.getInstance().getDifficultyLevels()[i].getBackgroundColor());
                gAct.fillOval(2, 2, 16, 16);
                gAct.drawImage(overlayImage, 0, 0, null);
                progressImages[i] = new ImageIcon(act);
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error creating progressLabel images", ex);
        }
    }

    public void setProgressLabel() {
        if (sudokuPanel == null) {
            // do nothing
            return;
        }
        Sudoku2 sudoku = sudokuPanel.getSudoku();
        if (sudoku.getStatus() != SudokuStatus.VALID) {
            progressLabel.setIcon(progressImages[0]);
            progressLabel.setText("-");
            return;
        }
        //ok valid sudoku, currentLevel and currentScore must have been set
        // not necessarily!
        if (getCurrentLevel() != null) {
            progressLabel.setIcon(progressImages[getCurrentLevel().getOrdinal()]);
            double proc = getCurrentScore();
            int intProc = (int) (proc / sudoku.getScore() * 100);
            if (intProc > 100) {
                intProc = 100;
            }
            if (intProc < 1) {
                intProc = 1;
            }
            if (getCurrentScore() == 0) {
                intProc = 0;
            }
            progressLabel.setText(intProc + "%");
        }
    }

    /**
     * @return the currentLevel
     */
    public synchronized DifficultyLevel getCurrentLevel() {
        return currentLevel;
    }

    /**
     * @param currentLevel the currentLevel to set
     */
    public synchronized void setCurrentLevel(DifficultyLevel currentLevel) {
        this.currentLevel = currentLevel;
    }

    /**
     * @return the currentScore
     */
    public synchronized int getCurrentScore() {
        return currentScore;
    }

    /**
     * @param currentScore the currentScore to set
     */
    public synchronized void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    /**
     * Handles the display of the hint buttons in the toolbar.
     * If they are made visible the first time, they have to be created.
     *
     */
    private void setShowHintButtonsInToolbar() {
        if (vageHintToggleButton == null) {
            // create the buttons
            hintSeperator = new JSeparator();
            hintSeperator.setOrientation(SwingConstants.VERTICAL);
            hintSeperator.setMaximumSize(new Dimension(5, 32767));
            hintSeperator.setVisible(false);
            jToolBar1.add(hintSeperator);

//            vageHintToggleButton = new javax.swing.JToggleButton();
            vageHintToggleButton = new JButton();
            vageHintToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/vageHint.png")));
            vageHintToggleButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    hintToggleButtonActionPerformed(true);
                }
            });
            vageHintToggleButton.setToolTipText(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.vageHintToolButton.toolTipText")); // NOI18N
            vageHintToggleButton.setVisible(false);
            jToolBar1.add(vageHintToggleButton);

//            concreteHintToggleButton = new javax.swing.JToggleButton();
            concreteHintToggleButton = new JButton();
            concreteHintToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/concreteHint.png")));
            concreteHintToggleButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    hintToggleButtonActionPerformed(false);
                }
            });
            concreteHintToggleButton.setVisible(false);
            concreteHintToggleButton.setToolTipText(PropertyUtil.getProperty("intl/MainFrame", "MainFrame.concreteHintToolButton.toolTipText")); // NOI18N
            jToolBar1.add(concreteHintToggleButton);

            showNextStepToggleButton = new JButton();
            showNextStepToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/nextHint.png")));
            showNextStepToggleButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    loesungsSchrittMenuItemActionPerformed(null);
                }
            });
            showNextStepToggleButton.setToolTipText(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.neuerHinweisButton.toolTipText")); // NOI18N
            showNextStepToggleButton.setVisible(false);
            jToolBar1.add(showNextStepToggleButton);

            executeStepToggleButton = new JButton();
            executeStepToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/executeHint.png")));
            executeStepToggleButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    hinweisAusfuehrenButtonActionPerformed(null);
                }
            });
            executeStepToggleButton.setToolTipText(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.hinweisAusfuehrenButton.toolTipText")); // NOI18N
            executeStepToggleButton.setVisible(false);
            jToolBar1.add(executeStepToggleButton);

            abortStepToggleButton = new JButton();
            abortStepToggleButton.setIcon(new ImageIcon(getClass().getResource("/img/abortHint.png")));
            abortStepToggleButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evt) {
                    hinweisAbbrechenButtonActionPerformed(null);
                }
            });
            abortStepToggleButton.setToolTipText(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.hinweisAbbrechenButton.toolTipText")); // NOI18N
            abortStepToggleButton.setVisible(false);
            jToolBar1.add(abortStepToggleButton);
        }
        if (Options.getInstance().isShowHintButtonsInToolbar()) {
            hintSeperator.setVisible(true);
            vageHintToggleButton.setVisible(true);
            concreteHintToggleButton.setVisible(true);
            showNextStepToggleButton.setVisible(true);
            executeStepToggleButton.setVisible(true);
            abortStepToggleButton.setVisible(true);
            executeStepToggleButton.setEnabled(hinweisAusfuehrenButton.isEnabled());
            abortStepToggleButton.setEnabled(hinweisAusfuehrenButton.isEnabled());
        } else {
            hintSeperator.setVisible(false);
            vageHintToggleButton.setVisible(false);
            concreteHintToggleButton.setVisible(false);
            showNextStepToggleButton.setVisible(false);
            executeStepToggleButton.setVisible(false);
            abortStepToggleButton.setVisible(false);
        }
        check();
        fixFocus();
    }

    /**
     * Action event for hint buttons
     * @param isVage
     */
    private void hintToggleButtonActionPerformed(boolean isVage) {
        if (isVage) {
            vageHintMenuItemActionPerformed(null);
        } else {
            mediumHintMenuItemActionPerformed(null);
        }
    }

    public boolean isEingabeModus() {
        return eingabeModus;
    }

    class MyFileFilter extends FileFilter {

        private int type;

        MyFileFilter(int type) {
            this.type = type;
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String[] parts = f.getName().split("\\.");
            if (parts.length > 1) {
                String ext = parts[parts.length - 1];
                switch (type) {
                    case 0:
                        // Configuration Files
                        if (ext.equalsIgnoreCase(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.config_file_ext"))) {
                            return true;
                        }
                        break;
                    case 1:
                        // Puzzles with Solutions
                        if (ext.equalsIgnoreCase(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solution_file_ext"))) {
                            return true;
                        }
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        // Any kind of text file
                        if (ext.equalsIgnoreCase(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.text_file_ext"))) {
                            return true;
                        }
                        break;
                    case 9:
                        // SimpleSudoku files
                        if (ext.equalsIgnoreCase(PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.ss_file_ext"))) {
                            return true;
                        }
                        break;
                    default:
                        return false;
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            switch (type) {
                case 0:
                    return PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.config_file_descr");
                case 1:
                    return PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solution_file_descr");
                case 2:
                    return PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solution_file_descr_gal");
                case 3:
                    return PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solution_file_descr_gf");
                case 4:
                    return PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solution_file_descr_pm");
                case 5:
                    return PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solution_file_descr_pms");
                case 6:
                    return PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solution_file_descr_pmg");
                case 7:
                    return PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solution_file_descr_l");
                case 8:
                    return PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solution_file_descr_text");
                case 9:
                    return PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.solution_file_descr_ss");
                default:
                    return PropertyUtil.loadResourceFile("intl/MainFrame").getString("MainFrame.unknown_file_type");
            }
        }

        public int getType() {
            return type;
        }
    }

    class MyCaretListener implements CaretListener {

        private boolean inUpdate = false;

        @Override
        public void caretUpdate(CaretEvent e) {
            if (inUpdate) {
                // no recursion!
                return;
            }
            //System.out.println("caretUpdate(): " + e.getDot() + "/" + e.getMark());
            // if everything is highlighted, don't interfere or it will be impossible
            // to copy the step to the clipboard
            // try to identify the line
            String text = hinweisTextArea.getText();
            if (e.getDot() == 0 && e.getMark() == text.length()) {
                // do nothing!
                return;
            }
            //System.out.println(text);
            int dot = e.getDot() > e.getMark() ? e.getDot() : e.getMark();
            int line = 0;
            int start = 0;
            int end = 0;
            int act = -1;
            while (act < dot) {
                act = text.indexOf('\n', act + 1);
                if (act == -1) {
                    // ok, done
                    end = text.length() - 1;
                    break;
                } else if (act < dot) {
                    start = act;
                    line++;
                } else {
                    end = act;
                    break;
                }
            }
            if (end > 0) {
                //System.out.println("Found: start = " + start + ", end = " + end + ", line = " + line);
                inUpdate = true;
                if (line == 0) {
//                    hinweisTextArea.setSelectionStart(0);
//                    hinweisTextArea.setSelectionEnd(0);
                    sudokuPanel.setChainInStep(-1);
                } else {
                    hinweisTextArea.setSelectionStart(start + 1);
                    hinweisTextArea.setSelectionEnd(end);
                    sudokuPanel.setChainInStep(line - 1);
                }
                inUpdate = false;
            }
        }
    }

    // Variables declaration - do not modify //GEN-BEGIN:variables
    // 关于菜单项
    private JMenuItem aboutMenuItem;
    // 所有步骤菜单项
    private JRadioButtonMenuItem allStepsMenuItem;
    // 所有隐性单元格菜单项
    private JMenuItem alleHiddenSinglesSetzenMenuItem;
    // 视图菜单
    private JMenu ansichtMenu;
    // 提问菜单项
    private JMenuItem askQuestionMenuItem;
    // 后门搜索菜单项
    private JMenuItem backdoorSearchMenuItem;
    // 编辑菜单
    private JMenu bearbeitenMenu;
    // 退出菜单项
    private JMenuItem beendenMenuItem;
    // 单元格缩放菜单项
    private JRadioButtonMenuItem cellZoomMenuItem;
    // 颜色按钮组
    private ButtonGroup colorButtonGroup;
    // 候选数字颜色菜单项
    private JRadioButtonMenuItem colorCandidatesMenuItem;
    // 单元格颜色菜单项
    private JRadioButtonMenuItem colorCellsMenuItem;
    // 配置菜单项
    private JMenuItem configMenuItem;
    // 复制线索菜单项
    private JMenuItem copyCluesMenuItem;
    // 复制填充菜单项
    private JMenuItem copyFilledMenuItem;
    // 复制库菜单项
    private JMenuItem copyLibraryMenuItem;
    // 复制PM网格菜单项
    private JMenuItem copyPmGridMenuItem;
    // 复制带步骤的PM网格菜单项
    private JMenuItem copyPmGridWithStepMenuItem;
    // 复制SS菜单项
    private JMenuItem copySSMenuItem;
    // 创建保存点菜单项
    private JMenuItem createSavePointMenuItem;
    // 文件菜单
    private JMenu dateiMenu;
    // 打印菜单项
    private JMenuItem druckenMenuItem;
    // 扩展打印菜单项
    private JMenuItem extendedPrintMenuItem;
    // F1切换按钮
    private JToggleButton f1ToggleButton;
    // F2切换按钮
    private JToggleButton f2ToggleButton;
    // F3切换按钮
    private JToggleButton f3ToggleButton;
    // F4切换按钮
    private JToggleButton f4ToggleButton;
    // F5切换按钮
    private JToggleButton f5ToggleButton;
    // F6切换按钮
    private JToggleButton f6ToggleButton;
    // F7切换按钮
    private JToggleButton f7ToggleButton;
    // F8切换按钮
    private JToggleButton f8ToggleButton;
    // F9切换按钮
    private JToggleButton f9ToggleButton;
    // 全屏菜单项
    private JCheckBoxMenuItem fullScreenMenuItem;
    // Fxy切换按钮
    private JToggleButton fxyToggleButton;
    // 帮助菜单
    private JMenu helpMenu;
    // 提示面板
    private JPanel hintPanel;
    // 提示取消按钮
    private JButton hinweisAbbrechenButton;
    // 提示执行按钮
    private JButton hinweisAusfuehrenButton;
    // 提示文本区域
    private JTextArea hinweisTextArea;
    // 历史菜单项
    private JMenuItem historyMenuItem;
    // 菜单栏
    private JMenuBar jMenuBar1;
    // 面板1  （添加色块的面板：statusPanelColorReset、statusPanelColor1、statusPanelColor2...）
    private JPanel jPanel1;
    // 滚动面板1
    private JScrollPane jScrollPane1;
    // 分隔符1
    private JSeparator jSeparator1;
    // 分隔符11
    private JSeparator jSeparator11;
    // 分隔符12
    private JSeparator jSeparator12;
    // 分隔符13
    private JSeparator jSeparator13;
    // 分隔符20
    private JPopupMenu.Separator jSeparator20;
    // 分隔符23
    private JPopupMenu.Separator jSeparator23;
    // 分隔符24
    private JSeparator jSeparator24;
    // 分隔符25
    private JPopupMenu.Separator jSeparator25;
    // 分隔符26
    private JPopupMenu.Separator jSeparator26;
    // 分隔符27
    private JPopupMenu.Separator jSeparator27;
    // 分隔符28
    private JPopupMenu.Separator jSeparator28;
    // 分隔符29
    private JPopupMenu.Separator jSeparator29;
    // 分隔符30
    private JPopupMenu.Separator jSeparator30;
    // 分隔符31
    private JPopupMenu.Separator jSeparator31;
    // 分隔符32
    private JPopupMenu.Separator jSeparator32;
    // 分隔符33
    private JPopupMenu.Separator jSeparator33;
    // 分隔符34
    private JPopupMenu.Separator jSeparator34;
    // 分隔符35
    private JPopupMenu.Separator jSeparator35;
    // 分隔符36
    private JPopupMenu.Separator jSeparator36;
    // 分隔符37
    private JPopupMenu.Separator jSeparator37;
    // 分隔符38
    private JPopupMenu.Separator jSeparator38;
    // 分隔符39
    private JPopupMenu.Separator jSeparator39;
    // 分隔符6
    private JPopupMenu.Separator jSeparator6;
    // 分隔符8
    private JSeparator jSeparator8;
    // 分隔符9
    private JSeparator jSeparator9;
    // 工具栏1
    private JToolBar jToolBar1;
    // 快捷键菜单项
    private JMenuItem keyMenuItem;
    // 学习菜单项
    private JRadioButtonMenuItem learningMenuItem;
    // 难度级别组按钮
    private ButtonGroup levelButtonGroup;
    // 难度级别下拉框
    private JComboBox levelComboBox;
    // 极限难度菜单项
    private JRadioButtonMenuItem levelExtremMenuItem;
    // 困难难度菜单项
    private JRadioButtonMenuItem levelKniffligMenuItem;
    // 简单难度菜单项
    private JRadioButtonMenuItem levelLeichtMenuItem;
    // 难度菜单
    private JMenu levelMenu;
    // 中等难度菜单项
    private JRadioButtonMenuItem levelMittelMenuItem;
    // 困难难度菜单项
    private JRadioButtonMenuItem levelSchwerMenuItem;
    // 加载配置菜单项
    private JMenuItem loadConfigMenuItem;
    // 加载谜题菜单项
    private JMenuItem loadPuzzleMenuItem;
    // 解题步骤菜单项
    private JMenuItem loesungsSchrittMenuItem;
    // 中等提示菜单项
    private JMenuItem mediumHintMenuItem;
    // 模式按钮组
    private ButtonGroup modeButtonGroup;
    // 模式菜单
    private JMenu modeMenu;
    // 新游戏菜单项
    private JMenuItem neuMenuItem;
    // 新提示按钮
    private JButton neuerHinweisButton;
    // 新游戏工具按钮
    private JButton neuesSpielToolButton;
    // 选项菜单
    private JMenu optionenMenu;
    // 外部分隔面板
    private JSplitPane outerSplitPane;
    // 粘贴菜单项
    private JMenuItem pasteMenuItem;
    // 游玩菜单项
    private JRadioButtonMenuItem playingMenuItem;
    // 练习菜单项
    private JRadioButtonMenuItem practisingMenuItem;
    // 进度标签
    private JLabel progressLabel;
    // 项目主页菜单项
    private JMenuItem projectHomePageMenuItem;
    // 谜题菜单
    private JMenu raetselMenu;
    // 红绿切换按钮
    private JToggleButton redGreenToggleButton;
    // 撤销菜单项
    private JMenuItem redoMenuItem;
    // 撤销工具按钮
    private JButton redoToolButton;
    // 报告错误菜单项
    private JMenuItem reportErrorMenuItem;
    // 重置游戏菜单项
    private JMenuItem resetSpielMenuItem;
    // 重置视图菜单项
    private JMenuItem resetViewMenuItem;
    // 重新开始游戏菜单项
    private JMenuItem restartSpielMenuItem;
    // 恢复保存点菜单项
    private JMenuItem restoreSavePointMenuItem;
    // 另存为配置菜单项
    private JMenuItem saveConfigAsMenuItem;
    // 另存为谜题菜单项
    private JMenuItem savePuzzleAsMenuItem;
    // 保存谜题菜单项
    private JMenuItem savePuzzleMenuItem;
    // 页面设置菜单项
    private JMenuItem seiteEinrichtenMenuItem;
    // 设置初始数字菜单项
    private JMenuItem setGivensMenuItem;
    // 显示候选数字菜单项
    private JCheckBoxMenuItem showCandidatesMenuItem;
    // 显示彩色Ku菜单项
    private JCheckBoxMenuItem showColorKuMenuItem;
    // 显示偏差菜单项
    private JCheckBoxMenuItem showDeviationsMenuItem;
    // 显示提示按钮复选框菜单项
    private JCheckBoxMenuItem showHintButtonsCheckBoxMenuItem;
    // 显示提示面板菜单项
    private JCheckBoxMenuItem showHintPanelMenuItem;
    // 显示工具栏菜单项
    private JCheckBoxMenuItem showToolBarMenuItem;
    // 显示错误值菜单项
    private JCheckBoxMenuItem showWrongValuesMenuItem;
    // 解题菜单项
    private JRadioButtonMenuItem solutionMenuItem;
    // 解决到指定步骤按钮
    private JButton solveUpToButton;
    // 解题指南菜单项
    private JMenuItem solvingGuideMenuItem;
    // 保存为图片菜单项
    private JMenuItem speichernAlsBildMenuItem;
    // 编辑谜题菜单项
    private JMenuItem spielEditierenMenuItem;
    // 输入谜题菜单项
    private JMenuItem spielEingebenMenuItem;
    // 游玩谜题菜单项
    private JMenuItem spielSpielenMenuItem;
    /** 单元格候选数字状态标签 */
    private JLabel statusLabelCellCandidate;
    // 难度级别状态标签
    private JLabel statusLabelLevel;
    // 模式状态标签
    private JLabel statusLabelModus;
    // 状态栏单元格颜色
    private JPanel statusLinePanel;
    // 状态栏单元格颜色1
    private JPanel statusPanelColor1;
    // 状态栏单元格颜色2
    private JPanel statusPanelColor2;
    // 状态栏单元格颜色3
    private JPanel statusPanelColor3;
    // 状态栏单元格颜色4
    private JPanel statusPanelColor4;
    // 状态栏单元格颜色5
    private JPanel statusPanelColor5;
    // 状态栏单元格颜色清除
    private JPanel statusPanelColorClear;
    // 状态栏单元格颜色重置
    private JPanel statusPanelColorReset;
    // 状态栏单元格颜色结果
    private JPanel statusPanelColorResult;
    // 仅数独菜单项
    private JRadioButtonMenuItem sudokuOnlyMenuItem;
    // 摘要菜单项
    private JRadioButtonMenuItem summaryMenuItem;
    // 撤销菜单项
    private JMenuItem undoMenuItem;
    // 撤销工具按钮
    private JButton undoToolButton;
    // 用户手册菜单项
    private JMenuItem userManualMenuItem;
    // 模糊提示菜单项
    private JMenuItem vageHintMenuItem;
    // 视图按钮组
    private ButtonGroup viewButtonGroup;
    // End of variables declaration //GEN-END:variables

}

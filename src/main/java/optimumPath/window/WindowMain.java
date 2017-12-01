package optimumPath.window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.UIManager;
import javax.swing.JSlider;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;


import optimumPath.frame.*;
import optimumPath.opengl.*;
import optimumPath.object.Map;
import optimumPath.common.Point3d;
import optimumPath.JSON.*;
import optimumPath.algorithms.Algorithm;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.JRadioButtonMenuItem;
import java.awt.FlowLayout;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.SystemColor;
import javax.swing.SwingConstants;
import java.awt.Toolkit;


public class WindowMain extends JFrame {
	
	/**
	 * Auto generated
	 */
	private static final long serialVersionUID = -267010437272070514L;
	
	private Render render;
	private JsonWriteRead json;
	
	private JMenuBar menuBar;
	private JSlider sliderAnimSpeed;
	private JButton btnApply;
	private JSpinner spnLayer;
	private JRadioButton rdbtnPreview, rdbtnMapMod;
	private JComboBox cbAlgorithm, cbMetrics;
	private ToolBarButton btnNewMap, btnSaveMap, btnLoadMap, btnExit;
	private JMenuItem mntmExit, mntmLoadMap, mntmSaveMap, mntmNewMap;
	private JRadioButtonMenuItem mntmDrawObstacle, mntmDelObstacle, mntmCheckStartPoint, mntmCheckStopPoint;
	
	//Create a file chooser
	final private JFileChooser fc;
	private JFrame windowMain = this;
	private ToolBarToggleButton btnObst;
	private ToolBarToggleButton btnDelObst;
	private ToolBarToggleButton btnStart;
	private ToolBarToggleButton btnEnd;
	private ToolBarButton btnHelp;
	private ToolBarButton btnSaveScreen;
	private ToolBarButton btnCopyLayer;
	private ToolBarButton btnPasteLayer;
	private JPanel panelSafe;
	private JLabel label;
	private JComboBox cbZoneProhibited;
	private JCheckBox cboxAnimation;
	private final ButtonGroup bgToolbarMenu = new ButtonGroup();
	private final ButtonGroup bgPopupMenu = new ButtonGroup();
	private JSeparator separator_1;
	private JMenuItem mntmCopyLayer;
	private JMenuItem mntmPasteLayer;
	private JMenuItem mntmSaveScreen;
	private JSeparator separator_2;
	private JMenu mnHelp;
	private JMenuItem mntmHelp;
	private JMenuItem mntmAuthors;
	private JSeparator separator_3;
	private JMenuItem mntmClearLayer;
	private JMenuItem mntmClearMap;
	private JMenu mnNarzdzia;
	private JMenuItem mntmMapSettings;
	private JMenuItem mntmColorSettings;
	private JSeparator separator_4;
	private JPanel GLpanel;
	private JLabel txtAlg;
	private JLabel txtMetric;
	private JLabel txtPathLeng;
	private JLabel txtNumbRaster;
	private JLabel txtIteration;
	private JMenuItem mntmSavePath;
	private ToolBarButton btnSavePath;
	private JSpinner spnThick;
	private ToolBarButton btnStopAlg, btnStartAlg;
	
	/**
	 * G��wna aplikacja.
	 */
	
	public WindowMain(Render render) {
		super("Optymalna �cie�ka na mapie rastrowej w 3D");
		setIconImage(Toolkit.getDefaultToolkit().getImage("toolbar_icons/icon.png"));
		setResizable(false);
		setBounds(100, 100, 1041, 804);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.render = render;
		this.json = new JsonWriteRead();
		this.fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "JSON", "json");
		this.fc.setFileFilter(filter);
		this.render.setWindow(this);
		this.render.getCamera().setInitialCamera(this.render.getRenderMap());;
		
		initComponents();
		
		GLpanel.add(this.render.getGlcanvas(), BorderLayout.CENTER);
		GLpanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		createEvents();
		initLayerSpinner();
		setTxtAlgMetric();
		getResults();
		//wpi�cie okna opengl do JPanel
		//
	}

	/////////////////////////////////////////////////////////////////////
	// Metoda zawieraj�ca kod tworz�cy komponenty oraz ich inicjalizacje
	/////////////////////////////////////////////////////////////////////
	
	private void initComponents() {
		
		// usatwienie ToolTip oraz JPopupMenu jako komponent�w HeavyWeight
		// naprawia problem chowaj�cego si� menu i podpowiedzi przycisk�w pod glcanvas
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false); // for tooltips
		JPopupMenu.setDefaultLightWeightPopupEnabled(false); // for menus and pop-ups
		
		///////////////////////////////////////////////////
		// Menu
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFiles = new JMenu("Pliki");
		menuBar.add(mnFiles);
		
		mntmNewMap = new JMenuItem("Nowa mapa");
		mntmNewMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		mnFiles.add(mntmNewMap);
		
		mntmLoadMap = new JMenuItem("Wczytaj mape");
		mntmLoadMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mnFiles.add(mntmLoadMap);
		
		mntmSaveMap = new JMenuItem("Zapisz mape");
		mntmSaveMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		mnFiles.add(mntmSaveMap);
		
		mntmSavePath = new JMenuItem("Zapisz \u015Bcie\u017Ck\u0119");
		mnFiles.add(mntmSavePath);
		
		separator_2 = new JSeparator();
		mnFiles.add(separator_2);
		
		mntmExit = new JMenuItem("Zamknij");
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
		mnFiles.add(mntmExit);
		
		JMenu mnEdition = new JMenu("Edycja");
		menuBar.add(mnEdition);
		
		mntmDrawObstacle = new JRadioButtonMenuItem("Rysuj przeszkod\u0119");
		bgPopupMenu.add(mntmDrawObstacle);
		mnEdition.add(mntmDrawObstacle);
		
		mntmDelObstacle = new JRadioButtonMenuItem("Usu\u0144 przeszkod\u0119");
		bgPopupMenu.add(mntmDelObstacle);
		mnEdition.add(mntmDelObstacle);
		
		mntmCheckStartPoint = new JRadioButtonMenuItem("Zaznacz punkt startowy");
		bgPopupMenu.add(mntmCheckStartPoint);
		mnEdition.add(mntmCheckStartPoint);
		
		mntmCheckStopPoint = new JRadioButtonMenuItem("Zaznacz punkt ko\u0144cowy");
		bgPopupMenu.add(mntmCheckStopPoint);
		mnEdition.add(mntmCheckStopPoint);
		
		separator_1 = new JSeparator();
		mnEdition.add(separator_1);
		
		mntmCopyLayer = new JMenuItem("Kopiuj warstw\u0119");
		mntmCopyLayer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		mnEdition.add(mntmCopyLayer);
		
		mntmPasteLayer = new JMenuItem("Wklej warstw\u0119");
		mntmPasteLayer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		mnEdition.add(mntmPasteLayer);
		
		separator_3 = new JSeparator();
		mnEdition.add(separator_3);
		
		mntmClearLayer = new JMenuItem("Wyczy\u015B\u0107 warstw\u0119");
		mntmClearLayer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK));
		mnEdition.add(mntmClearLayer);
		
		mntmClearMap = new JMenuItem("Wyczy\u015B\u0107 map\u0119");
		mnEdition.add(mntmClearMap);
		
		mnNarzdzia = new JMenu("Narz\u0119dzia");
		menuBar.add(mnNarzdzia);
		
		separator_4 = new JSeparator();
		mnNarzdzia.add(separator_4);
		
		mntmSaveScreen = new JMenuItem("Zapisz widok");
		mnNarzdzia.add(mntmSaveScreen);
		
		JSeparator separator = new JSeparator();
		mnNarzdzia.add(separator);
		
		mntmMapSettings = new JMenuItem("Ustawienia mapy");
		mnNarzdzia.add(mntmMapSettings);
		
		mntmColorSettings = new JMenuItem("Ustawienia kolor\u00F3w");
		mnNarzdzia.add(mntmColorSettings);
		
		mnHelp = new JMenu("Pomoc");
		menuBar.add(mnHelp);
		
		mntmHelp = new JMenuItem("Pomoc");
		mnHelp.add(mntmHelp);
		
		mntmAuthors = new JMenuItem("Autorzy");
		mnHelp.add(mntmAuthors);
		
		JPanel contentPanel = new JPanel();
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panelOptions = new JPanel();
		panelOptions.setBorder(null);
		contentPanel.add(panelOptions, BorderLayout.EAST);
		panelOptions.setLayout(new BoxLayout(panelOptions, BoxLayout.Y_AXIS));
		
		///////////////////////////////////////////////////
		// Panel mapy
		
		JPanel panelMap = new JPanel();
		panelMap.setAlignmentY(Component.TOP_ALIGNMENT);
		panelMap.setBorder(new TitledBorder(null, "Mapa", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelOptions.add(panelMap);
		
		rdbtnPreview = new JRadioButton("Podgl\u0105d");
		rdbtnPreview.setSelected(true);
		rdbtnMapMod = new JRadioButton("Modyfikacja mapy");
		
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnMapMod);
		group.add(rdbtnPreview);
		
		spnLayer = new JSpinner();
		spnLayer.setEnabled(false);
		
		JLabel lblWarstwa = new JLabel("Warstwa:");
		
		
		GroupLayout gl_panelMap = new GroupLayout(panelMap);
		gl_panelMap.setHorizontalGroup(
			gl_panelMap.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelMap.createSequentialGroup()
					.addGap(20)
					.addGroup(gl_panelMap.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelMap.createSequentialGroup()
							.addComponent(lblWarstwa)
							.addGap(18)
							.addComponent(spnLayer, GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
						.addComponent(rdbtnMapMod)
						.addComponent(rdbtnPreview))
					.addContainerGap())
		);
		gl_panelMap.setVerticalGroup(
			gl_panelMap.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panelMap.createSequentialGroup()
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(rdbtnPreview)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(rdbtnMapMod)
					.addGap(18)
					.addGroup(gl_panelMap.createParallelGroup(Alignment.LEADING)
						.addComponent(lblWarstwa)
						.addComponent(spnLayer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		panelMap.setLayout(gl_panelMap);
		
		///////////////////////////////////////////////////
		// Panel algorytm�w
		
		JPanel panelAlg = new JPanel();
		panelAlg.setAlignmentY(Component.TOP_ALIGNMENT);
		panelAlg.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Algorytmy", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelAlg.setToolTipText("");
		panelOptions.add(panelAlg);
		
		cbAlgorithm = new JComboBox();
		cbAlgorithm.setModel(new DefaultComboBoxModel(new String[] {"Propagacja Fali", "A Star"}));
		
		JLabel lblWybrAlgorytmu = new JLabel("Wyb\u00F3r algorytmu:");
		
		JLabel lblWybrMetryki = new JLabel("Wyb\u00F3r metryki:");
		
		cbMetrics = new JComboBox();
		cbMetrics.setModel(new DefaultComboBoxModel(new String[] {"Manhatan", "Czebyszew"}));
		GroupLayout gl_panelAlg = new GroupLayout(panelAlg);
		gl_panelAlg.setHorizontalGroup(
			gl_panelAlg.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelAlg.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelAlg.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelAlg.createSequentialGroup()
							.addComponent(lblWybrAlgorytmu)
							.addGap(78))
						.addGroup(gl_panelAlg.createSequentialGroup()
							.addComponent(lblWybrMetryki)
							.addContainerGap(119, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, gl_panelAlg.createSequentialGroup()
							.addGroup(gl_panelAlg.createParallelGroup(Alignment.TRAILING)
								.addComponent(cbMetrics, Alignment.LEADING, 0, 183, Short.MAX_VALUE)
								.addComponent(cbAlgorithm, 0, 183, Short.MAX_VALUE))
							.addContainerGap())))
		);
		gl_panelAlg.setVerticalGroup(
			gl_panelAlg.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelAlg.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblWybrAlgorytmu)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(cbAlgorithm, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(lblWybrMetryki)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(cbMetrics, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(323, Short.MAX_VALUE))
		);
		panelAlg.setLayout(gl_panelAlg);
		
		panelSafe = new JPanel();
		panelSafe.setAlignmentY(Component.TOP_ALIGNMENT);
		panelSafe.setBorder(new TitledBorder(null, "Strefa bezpiecze\u0144stwa", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelOptions.add(panelSafe);
		
		label = new JLabel("Rodzaj strefy bezpiecze\u0144stwa:");
		
		cbZoneProhibited = new JComboBox();
		cbZoneProhibited.setModel(new DefaultComboBoxModel(new String[] {"Brak", "Wok\u00F3\u0142 przeszk\u00F3d", "Wok\u00F3\u0142 robota"}));
		
		spnThick = new JSpinner();
		spnThick.setModel(new SpinnerNumberModel(1, 1, 2, 1));
		
		JLabel lblWielkoStrefy = new JLabel("Wielko\u015B\u0107 strefy:");
		GroupLayout gl_panelSafe = new GroupLayout(panelSafe);
		gl_panelSafe.setHorizontalGroup(
			gl_panelSafe.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSafe.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelSafe.createParallelGroup(Alignment.LEADING)
						.addComponent(label, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
						.addComponent(cbZoneProhibited, 0, 207, Short.MAX_VALUE)
						.addGroup(gl_panelSafe.createSequentialGroup()
							.addComponent(lblWielkoStrefy)
							.addPreferredGap(ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
							.addComponent(spnThick, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		gl_panelSafe.setVerticalGroup(
			gl_panelSafe.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSafe.createSequentialGroup()
					.addContainerGap()
					.addComponent(label)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(cbZoneProhibited, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelSafe.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblWielkoStrefy)
						.addComponent(spnThick, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(103, Short.MAX_VALUE))
		);
		panelSafe.setLayout(gl_panelSafe);
		
		///////////////////////////////////////////////////
		// Panel animacji
		
		JPanel panelAnim = new JPanel();
		panelAnim.setBorder(new TitledBorder(null, "Animacja", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelOptions.add(panelAnim);
		
		btnApply = new JButton("Wykonaj algorytm");
		
		sliderAnimSpeed = new JSlider();
		sliderAnimSpeed.setMaximum(105);
		sliderAnimSpeed.setValue(55);
		sliderAnimSpeed.setMinimum(5);
		sliderAnimSpeed.setToolTipText("");
		sliderAnimSpeed.setSnapToTicks(true);
		sliderAnimSpeed.setPaintTicks(true);
		sliderAnimSpeed.setPaintLabels(true);
		sliderAnimSpeed.setMinorTickSpacing(10);
		
		JLabel lblSzybko = new JLabel("Szybko\u015B\u0107:");
		
		cboxAnimation = new JCheckBox("W\u0142\u0105cz animacje");
		cboxAnimation.setSelected(true);
		GroupLayout gl_panelAnim = new GroupLayout(panelAnim);
		gl_panelAnim.setHorizontalGroup(
			gl_panelAnim.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelAnim.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelAnim.createParallelGroup(Alignment.LEADING)
						.addComponent(btnApply, Alignment.TRAILING)
						.addComponent(sliderAnimSpeed, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
						.addComponent(lblSzybko)
						.addComponent(cboxAnimation))
					.addContainerGap())
		);
		gl_panelAnim.setVerticalGroup(
			gl_panelAnim.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelAnim.createSequentialGroup()
					.addGap(8)
					.addComponent(cboxAnimation)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblSzybko)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(sliderAnimSpeed, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(18)
					.addComponent(btnApply)
					.addGap(53))
		);
		panelAnim.setLayout(gl_panelAnim);
		
		JPanel panelMain = new JPanel();
		contentPanel.add(panelMain, BorderLayout.CENTER);
		panelMain.setLayout(new BorderLayout(0, 0));
		
		JPanel panelConsole = new JPanel();
		panelConsole.setBorder(new MatteBorder(2, 2, 2, 2, (Color) Color.LIGHT_GRAY));
		panelConsole.setBackground(SystemColor.control);
		panelMain.add(panelConsole, BorderLayout.SOUTH);
		
		JLabel lblNewLabel = new JLabel("D\u0142ugo\u015B\u0107 \u015Bcie\u017Cki: ");
		
		JLabel lblNewLabel_1 = new JLabel("Algorytm: ");
		
		JLabel lblNewLabel_2 = new JLabel("Metryka: ");
		
		JLabel lblNewLabel_3 = new JLabel("Ilo\u015B\u0107 iteracji: ");
		
		JLabel lblNewLabel_4 = new JLabel("Ilo\u015B\u0107 rastr\u00F3w \u015Bcie\u017Cki: ");
		
		txtAlg = new JLabel("");
		txtAlg.setHorizontalAlignment(SwingConstants.RIGHT);
		
		txtMetric = new JLabel("");
		txtMetric.setHorizontalAlignment(SwingConstants.RIGHT);
		
		txtPathLeng = new JLabel("");
		txtPathLeng.setHorizontalAlignment(SwingConstants.RIGHT);
		
		txtNumbRaster = new JLabel("");
		txtNumbRaster.setHorizontalAlignment(SwingConstants.RIGHT);
		
		txtIteration = new JLabel("");
		txtIteration.setHorizontalAlignment(SwingConstants.RIGHT);
		
		GroupLayout gl_panelConsole = new GroupLayout(panelConsole);
		gl_panelConsole.setHorizontalGroup(
			gl_panelConsole.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelConsole.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelConsole.createParallelGroup(Alignment.LEADING)
						.addComponent(lblNewLabel_1)
						.addComponent(lblNewLabel_2))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelConsole.createParallelGroup(Alignment.LEADING, false)
						.addComponent(txtMetric, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(txtAlg, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_panelConsole.createParallelGroup(Alignment.LEADING)
						.addComponent(lblNewLabel)
						.addComponent(lblNewLabel_4))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelConsole.createParallelGroup(Alignment.LEADING, false)
						.addComponent(txtNumbRaster, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(txtPathLeng, GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
					.addGap(32)
					.addComponent(lblNewLabel_3)
					.addGap(6)
					.addComponent(txtIteration, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(231, Short.MAX_VALUE))
		);
		gl_panelConsole.setVerticalGroup(
			gl_panelConsole.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panelConsole.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelConsole.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblNewLabel_3)
						.addComponent(txtIteration, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtAlg, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_1)
						.addComponent(lblNewLabel)
						.addComponent(txtPathLeng, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(gl_panelConsole.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_panelConsole.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblNewLabel_2))
						.addComponent(txtMetric, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_4)
						.addComponent(txtNumbRaster, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
					.addGap(13))
		);
		panelConsole.setLayout(gl_panelConsole);
		
		GLpanel = new JPanel();
		panelMain.add(GLpanel, BorderLayout.CENTER);
		
		
		///////////////////////////////////////////////////
		// Pasek narz�dzi
		
		//Dimension toolBarButtonSize = new Dimension(30, 30);
		JPanel panelTool = new JPanel();
		contentPanel.add(panelTool, BorderLayout.NORTH);
		panelTool.setLayout(new BorderLayout(0, 0));
		//panelTool.setMinimumSize(toolBarButtonSize);
		//panelTool.setPreferredSize(toolBarButtonSize);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
			
		panelTool.add(toolBar);
		
		btnNewMap = new ToolBarButton();
		btnNewMap.setToolTipText("Nowa mapa");
		btnSaveMap = new ToolBarButton();
		btnSaveMap.setToolTipText("Zapisz mape");
		btnLoadMap = new ToolBarButton();
		btnLoadMap.setToolTipText("Wczytaj mape");
		btnCopyLayer = new ToolBarButton();
		btnCopyLayer.setToolTipText("Kopiuj modyfikowan� warstw�");
		btnPasteLayer = new ToolBarButton();
		btnPasteLayer.setToolTipText("Wklej modyfikowan� warstw�");
		btnSavePath = new ToolBarButton();
		btnSavePath.setToolTipText("Zapisz \u015Bcie\u017Ck\u0119");
		btnSaveScreen = new ToolBarButton();
		btnSaveScreen.setToolTipText("Zapisz widok");
		btnStartAlg = new ToolBarButton();
		btnStartAlg.setToolTipText("Wykonaj algorytm");
		btnStopAlg = new ToolBarButton();
		btnStopAlg.setToolTipText("Zatrzymaj algorytm");
		btnHelp = new ToolBarButton();
		btnHelp.setToolTipText("Pomoc");
		btnExit = new ToolBarButton();
		btnExit.setToolTipText("Zamknij program");
		
		btnObst = new ToolBarToggleButton();
		bgToolbarMenu.add(btnObst);
		btnObst.setToolTipText("Rysuj przeszkod�");
		btnDelObst = new ToolBarToggleButton();
		bgToolbarMenu.add(btnDelObst);
		btnDelObst.setToolTipText("Usu� przeszkod�");
		btnStart = new ToolBarToggleButton();
		bgToolbarMenu.add(btnStart);
		btnStart.setToolTipText("Zaznacz punkt startowy");
		btnEnd = new ToolBarToggleButton();
		bgToolbarMenu.add(btnEnd);
		btnEnd.setToolTipText("Zaznacz punkt ko�cowy");
		
		bgToolbarMenu.setSelected(btnObst.getModel(), true);
		bgPopupMenu.setSelected(mntmDrawObstacle.getModel(), true);
		
		btnNewMap.setIcon(new ImageIcon("toolbar_icons/new.png"));
		btnSaveMap.setIcon(new ImageIcon("toolbar_icons/save.png"));
		btnLoadMap.setIcon(new ImageIcon("toolbar_icons/open.png"));
		
		btnObst.setIcon(new ImageIcon("toolbar_icons/obst.png"));
		btnDelObst.setIcon(new ImageIcon("toolbar_icons/delobst.png"));
		btnStart.setIcon(new ImageIcon("toolbar_icons/start.png"));
		btnEnd.setIcon(new ImageIcon("toolbar_icons/finish.png"));
		btnCopyLayer.setIcon(new ImageIcon("toolbar_icons/copy.png"));
		btnPasteLayer.setIcon(new ImageIcon("toolbar_icons/paste.png"));
		
		btnStartAlg.setIcon(new ImageIcon("toolbar_icons/perform.png"));
		btnStopAlg.setIcon(new ImageIcon("toolbar_icons/stop.png"));
		
		btnSavePath.setIcon(new ImageIcon("toolbar_icons/path.png"));
		btnSaveScreen.setIcon(new ImageIcon("toolbar_icons/camera.png"));
		btnHelp.setIcon(new ImageIcon("toolbar_icons/help.png"));
		btnExit.setIcon(new ImageIcon("toolbar_icons/exit.png"));
		
		toolBar.add(btnNewMap);
		toolBar.add(btnSaveMap);
		toolBar.add(btnLoadMap);
		toolBar.addSeparator();
		toolBar.add(btnObst);
		toolBar.add(btnDelObst);
		toolBar.add(btnStart);
		toolBar.add(btnEnd);
		toolBar.addSeparator();
		
		toolBar.add(btnCopyLayer);
		toolBar.add(btnPasteLayer);
		toolBar.addSeparator();
		toolBar.add(btnSaveScreen);
		toolBar.add(btnSavePath);
		toolBar.addSeparator();
		toolBar.add(btnStartAlg);
		toolBar.add(btnStopAlg);
		toolBar.addSeparator();
		toolBar.add(btnHelp);
		toolBar.add(btnExit);
		
	}
	
	public void initLayerSpinner() {
		int maxLayer = render.getRenderMap().getSizeZ() - 1;
		spnLayer.setModel(new SpinnerNumberModel(0, 0, maxLayer, 1));
		//spnLayer.setValue(Integer.valueOf(0));
	}

	public void setOffsetMap() {
		int layer = ((Integer)spnLayer.getValue()).intValue();
		render.setOffsetLayer(layer);
	}
	
	
	public int getSelectedToolbarButton() {
		if (bgToolbarMenu.isSelected(btnObst.getModel()))
			return 0;
		if (bgToolbarMenu.isSelected(btnDelObst.getModel()))
			return 1;
		if (bgToolbarMenu.isSelected(btnStart.getModel()))
			return 2;
		if (bgToolbarMenu.isSelected(btnEnd.getModel()))
			return 3;
		return -1;
	}
	
	private void bindGroupPopup() {
		mntmDrawObstacle.setSelected(btnObst.isSelected());
		mntmDelObstacle.setSelected(btnDelObst.isSelected());
		mntmCheckStartPoint.setSelected(btnStart.isSelected());
		mntmCheckStopPoint.setSelected(btnEnd.isSelected());
	}
	
	private void bindGroupToolbar() {
		btnObst.setSelected(mntmDrawObstacle.isSelected());
		btnDelObst.setSelected(mntmDelObstacle.isSelected());
		btnStart.setSelected(mntmCheckStartPoint.isSelected());
		btnEnd.setSelected(mntmCheckStopPoint.isSelected());
	}
	
	public void setTxtAlgMetric() {
		txtAlg.setText(cbAlgorithm.getSelectedItem().toString());
		txtMetric.setText(cbMetrics.getSelectedItem().toString());
	}
	
	public void getResults() {
		double length = Math.round(render.getRenderMap().getLengthPath() * 100.0) / 100.0;
		txtIteration.setText(((Integer)render.getRenderMap().getNumberIteration()).toString());
		txtNumbRaster.setText(((Integer)render.getRenderMap().getNumberRasterPath()).toString());
		txtPathLeng.setText(((Double)length).toString());
	}
	
	///////////////////////////////////////////////////////////////////
	// Metoda zawieraj�ca kod tworz�cy event'y komponet�w
	///////////////////////////////////////////////////////////////////

	private void createEvents() {
		btnNewMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				WindowNewMap newMap = new WindowNewMap();
				int loactionX = getX() + (getWidth() - newMap.getWidth())/2;
				int loactionY = getY() + (getHeight() - newMap.getHeight())/2;
				newMap.setLocation(loactionX, loactionY);
				newMap.setModal(true);
				newMap.setVisible(true);
				
				if (!newMap.isOk())
					return;
				
				Point3d size = newMap.getSizeMap();
				
				int sizeX = (int)size.getX();
				int sizeY = (int)size.getY();
				int sizeZ = (int)size.getZ();
				
				render.getRenderMap().setSize(sizeX, sizeY, sizeZ);
				render.getRenderMap().initMap();
				render.getEditionMap().clearClipboard();
				
				rdbtnPreview.doClick();
				initLayerSpinner();
				setOffsetMap();
				render.getCamera().setInitialCamera(render.getRenderMap());
				
			}
		});
		
		btnSaveMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fc.showSaveDialog(windowMain);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
			    	String filePath = fc.getSelectedFile().getPath();
			    	//This is where a real application would open the file.
			    	if (fc.getFileFilter().getDescription() == "JSON Files" && !filePath.endsWith(".JSON"))
			    		filePath += ".JSON";
			    	
			    	System.out.println("Saving: " + filePath);
			    	int outputMap[][][] = render.getRenderMap().rasterMapToIntMap();
			    	int sizeX = render.getRenderMap().getSizeX();
			    	int sizeY = render.getRenderMap().getSizeY();
			    	int sizeZ = render.getRenderMap().getSizeZ();
			    	json.printMap(outputMap, sizeZ, sizeY, sizeX);
			    	json.writeMapToJSON(filePath, outputMap, sizeZ, sizeY, sizeX);
				} else {
			    	System.out.println("Save command cancelled by user.");
			   	}
			}
		});
		
		btnLoadMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fc.showOpenDialog(windowMain);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String filePath = fc.getSelectedFile().getPath();
			    	//This is where a real application would open the file.
			        System.out.println("Opening: " + filePath);
			        int outputMap[][][] = json.loadMapFromJSON(filePath);
			        int sizeX = json.getSizeXfromJSON();
			        int sizeY = json.getSizeYfromJSON();
			        int sizeZ = json.getSizeZfromJSON();
			        json.printMap(outputMap, sizeZ, sizeY, sizeX);
			        render.getRenderMap().intMapToRasterMap(outputMap, sizeX, sizeY, sizeZ);
					initLayerSpinner();
					setOffsetMap();
					render.getEditionMap().clearClipboard();
					rdbtnPreview.doClick();
					render.getCamera().setInitialCamera(render.getRenderMap());
			    } else {
			    	System.out.println("Open command cancelled by user.");
			    }
			}
		});
		
		btnSaveScreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG (*.png)", "PNG", "png");
				fc.setFileFilter(filter);
				
				int returnVal = fc.showSaveDialog(windowMain);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
			    	String filePath = fc.getSelectedFile().getPath();
			    	//This is where a real application would open the file.
			    	if (fc.getFileFilter().getDescription() == "PNG (*.png)" && !filePath.endsWith(".png"))
			    		filePath += ".png";
			    	
			    	System.out.println("Saving: " + filePath);
			    	render.saveImage(filePath);
				} else {
			    	System.out.println("Save command cancelled by user.");
			   	}
			}
		});
		
		btnSavePath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fc.showSaveDialog(windowMain);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
			    	String filePath = fc.getSelectedFile().getPath();
			    	//This is where a real application would open the file.
			    	if (fc.getFileFilter().getDescription() == "JSON Files" && !filePath.endsWith(".JSON"))
			    		filePath += ".JSON";
			    	
			    	if(!render.getRenderMap().getPathShift().isEmpty() && render.getRenderMap().getAlgorithm() != null) {
			    		json.writePathToJSON(filePath, render.getRenderMap().getAlgorithm().getPath());
			    		System.out.println("Saving: " + filePath);
			    	} else
			    		System.out.println("�cie�ka nie istnieje!");
			
				} else {
			    	System.out.println("Save command cancelled by user.");
			   	}
			}
		});
		
		btnCopyLayer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!render.isMapCreation())
					return;
				
				int layer = ((Integer)spnLayer.getValue()).intValue();
				render.getEditionMap().copyLayer(render.getRenderMap(), layer);
			}
		});
		
		btnPasteLayer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!render.isMapCreation())
					return;
				
				int layer = ((Integer)spnLayer.getValue()).intValue();
				render.getEditionMap().pasteLayer(render.getRenderMap(), layer);
			}
		});
		
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		rdbtnPreview.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spnLayer.setEnabled(false);
				render.setMapCreation(false);
				render.getCamera().loadPrevCamera();
				render.getCamera().clearPrevCamera();
			}
		});
		
		rdbtnMapMod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				spnLayer.setEnabled(true);
				render.setMapCreation(true);
				render.getRenderMap().resetPath();
				render.getRenderMap().resetForbidden();
				if (!render.getCamera().isPrevCamera())
					render.getCamera().saveActualCamera();
				
				int layer = ((Integer)spnLayer.getValue()).intValue();
				int width = render.getGlcanvas().getWidth();
				int height = render.getGlcanvas().getHeight();
				render.getCamera().setCameraCenterLayer(render.getRenderMap(), layer, width, height);
			}
		});
		
		spnLayer.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				System.out.println("Zmieniono warstwe!");
				setOffsetMap();
				
				int layer = ((Integer)spnLayer.getValue()).intValue();
				int width = render.getGlcanvas().getWidth();
				int height = render.getGlcanvas().getHeight();
				render.getCamera().setCameraCenterLayer(render.getRenderMap(), layer, width, height);
				GLpanel.requestFocus();
			}
		});
		
		btnApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				render.setAnimation(cboxAnimation.isSelected());
				render.getRenderMap().setSpeedAnimation(sliderAnimSpeed.getValue());
				render.getRenderMap().resetPath();
				render.getRenderMap().resetForbidden();
				boolean isChebyshev = false;
				int zoneProhibited = cbZoneProhibited.getSelectedIndex();
				int thick = ((Integer)spnThick.getValue()).intValue();
				
				if (cbMetrics.getSelectedIndex() == 1)
					isChebyshev = true;
				if (cbAlgorithm.getSelectedIndex() == 1) {
					render.getRenderMap().performAStar(isChebyshev, zoneProhibited, thick);
					render.setAStar(true);
				} else {
					render.getRenderMap().performWavePropagation(isChebyshev, zoneProhibited, thick);
					render.setAStar(false);
				}
			}
		});
		
		btnStartAlg.addActionListener(btnApply.getActionListeners()[0]);
		
		btnStopAlg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				render.getRenderMap().getAlgorithm().setStopAlgorithm(true);
			}
		});
		
		sliderAnimSpeed.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				render.getRenderMap().setSpeedAnimation(sliderAnimSpeed.getValue());
			}
		});
		
		mntmClearLayer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!render.isMapCreation())
					return;
				
				int layer = ((Integer)spnLayer.getValue()).intValue();
				render.getEditionMap().clearLayer(render.getRenderMap(), layer);
			}
		});
		
		mntmClearMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				render.getRenderMap().clearMap();
			}
		});
		
		mntmAuthors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WindowAbout About = new WindowAbout();
				int loactionX = getX() + (getWidth() - About.getWidth())/2;
				int loactionY = getY() + (getHeight() - About.getHeight())/2;
				About.setLocation(loactionX, loactionY);
				About.setModal(true);
				About.setVisible(true);
			}
		});
		
		mntmMapSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WindowMapSettings mapSettings = new WindowMapSettings();
				Map refMap = render.getRenderMap();
				
				int loactionX = getX() + (getWidth() - mapSettings.getWidth())/2;
				int loactionY = getY() + (getHeight() - mapSettings.getHeight())/2;
				mapSettings.setLocation(loactionX, loactionY);
				
				mapSettings.getSpnRaster().setValue(Double.valueOf(refMap.getSizeRaster()));
				mapSettings.getSpnSizeX().setValue(Integer.valueOf(refMap.getSizeX()));
				mapSettings.getSpnSizeY().setValue(Integer.valueOf(refMap.getSizeY()));
				mapSettings.getSpnSizeZ().setValue(Integer.valueOf(refMap.getSizeZ()));
				mapSettings.setModal(true);
				mapSettings.setVisible(true);
				
				if (!mapSettings.isOk())
					return;
				
				Point3d size = mapSettings.getSizeMap();
				refMap.setSizeRaster(mapSettings.getSizeRaster());
				refMap.reshapeMap((int)size.getX(), (int)size.getY(), (int)size.getZ());
				render.getEditionMap().clearClipboard();
				
				rdbtnPreview.doClick();
				initLayerSpinner();
				setOffsetMap();
				
				render.getCamera().setInitialCamera(render.getRenderMap());
			}
		});
		
		mntmColorSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WindowColorSettings colorSettings = new WindowColorSettings(render.getMaterials());
				
				int loactionX = getX() + (getWidth() - colorSettings.getWidth())/2;
				int loactionY = getY() + (getHeight() - colorSettings.getHeight())/2;
				colorSettings.setLocation(loactionX, loactionY);
				colorSettings.setModal(true);
				colorSettings.setVisible(true);
			}
		});
		
		cbAlgorithm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setTxtAlgMetric();
				getResults();
			}
		});
		
		cbMetrics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTxtAlgMetric();
				getResults();
			}
		});
		
		mntmNewMap.addActionListener(btnNewMap.getActionListeners()[0]);
		mntmSaveMap.addActionListener(btnSaveMap.getActionListeners()[0]);
		mntmLoadMap.addActionListener(btnLoadMap.getActionListeners()[0]);
		mntmSaveScreen.addActionListener(btnSaveScreen.getActionListeners()[0]);
		mntmExit.addActionListener(btnExit.getActionListeners()[0]);
		
		mntmCopyLayer.addActionListener(btnCopyLayer.getActionListeners()[0]);
		mntmPasteLayer.addActionListener(btnPasteLayer.getActionListeners()[0]);
		mntmSavePath.addActionListener(btnSavePath.getActionListeners()[0]);

		ActionListener GroupPopupListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				bindGroupToolbar();
				render.getEditionMap().setSelectedOption(getSelectedToolbarButton());
			}
		};
		
		ActionListener GroupToolbarListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				bindGroupPopup();
				render.getEditionMap().setSelectedOption(getSelectedToolbarButton());
			}
		};
		
		mntmDrawObstacle.addActionListener(GroupPopupListener);
		mntmDelObstacle.addActionListener(GroupPopupListener);
		mntmCheckStartPoint.addActionListener(GroupPopupListener);
		mntmCheckStopPoint.addActionListener(GroupPopupListener);
	
		btnObst.addActionListener(GroupToolbarListener);
		btnDelObst.addActionListener(GroupToolbarListener);
		btnStart.addActionListener(GroupToolbarListener);
		btnEnd.addActionListener(GroupToolbarListener);
	}

	public JPanel getGLpanel() {
		return GLpanel;
	}
}

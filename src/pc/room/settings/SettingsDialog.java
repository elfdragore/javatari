// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package pc.room.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import parameters.Parameters;
import pc.room.Room;
import atari.network.ConnectionStatusListener;
import atari.network.RemoteReceiver;
import atari.network.RemoteTransmitter;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class SettingsDialog extends JDialog implements ConnectionStatusListener {

	public static void main(String[] args) {
		try {
			SettingsDialog dialog = new SettingsDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SettingsDialog(Room room) {
		this.room = room;
		buildGUI();
		setEscActionListener();
		buildKeyFieldsList();
		setControlsKeyListener();
		setMultiplayerDefaults();
		mainTabbedPane.setSelectedIndex(3);
		mainTabbedPane.setEnabledAt(0, Parameters.MULTIPLAYER_UI);
	}

	@Override
	public void connectionStatusChanged() {
		if (isVisible()) refreshMultiplayerImages();
	}
		
	@Override
	public void setVisible(boolean state) {
		setupConnectionStatulsListeners();
		initNewKeys();
		refreshContols();
		refreshMultiplayer();
		mainTabbedPaneChanged();
		super.setVisible(state);
	}
	
	private void refreshContols() {
		keyP0Up.setText(KeyNames.get(newKEY_P0_UP));
		keyP0Down.setText(KeyNames.get(newKEY_P0_DOWN));
		keyP0Left.setText(KeyNames.get(newKEY_P0_LEFT));
		keyP0Right.setText(KeyNames.get(newKEY_P0_RIGHT));
		keyP0Button.setText(KeyNames.get(newKEY_P0_BUTTON));
		keyP0Button2.setText(KeyNames.get(newKEY_P0_BUTTON2));
		keyP1Up.setText(KeyNames.get(newKEY_P1_UP));
		keyP1Down.setText(KeyNames.get(newKEY_P1_DOWN));
		keyP1Left.setText(KeyNames.get(newKEY_P1_LEFT));
		keyP1Right.setText(KeyNames.get(newKEY_P1_RIGHT));
		keyP1Button.setText(KeyNames.get(newKEY_P1_BUTTON));
		keyP1Button2.setText(KeyNames.get(newKEY_P1_BUTTON2));	
		for (JTextField field : keyFieldsList)
			field.setBackground(field.getText().trim().isEmpty() ? Color.YELLOW : Color.WHITE);
		defaultsB.setVisible(true);
		okB.setVisible(true);
		cancelB.setText("Cancel");
	}
	
	private void refreshMultiplayer() {
		if (room == null) {
			modeL.setText("STANDALONE MODE");
			modeL.setForeground(new Color(0, 127, 0));
			serverStartB.setText("");
			serverStartB.setEnabled(false);
			serverPortTf.setEditable(false);
			serverPortTf.setText("");
			clientConnectB.setText("");
			clientConnectB.setEnabled(false);
			clientServerAddressTf.setEditable(false);
			standaloneConsoleL.setVisible(true);
			networkL.setVisible(false);
			serverConsoleL.setVisible(false);
			clientConsoleL.setVisible(false);
			defaultsB.setVisible(false);
			okB.setVisible(false);
			cancelB.setText("Close");
			return;
		}
		boolean serverMode = room.isServerMode();
		boolean clientMode = room.isClientMode();
		modeL.setText(serverMode ? "P1 SERVER MODE" : clientMode ? "P2 CLIENT MODE" : "STANDALONE MODE");
		modeL.setForeground(serverMode ? Color.BLUE : clientMode ? Color.RED : new Color(0, 127, 0));
		serverStartB.setText(serverMode ? "STOP" : "START");
		serverStartB.setEnabled(!clientMode);
		serverPortTf.setEditable(!serverMode && !clientMode);
		clientConnectB.setText(clientMode ? "DISCONNECT" : "CONNECT");
		clientConnectB.setEnabled(!serverMode);
		clientServerAddressTf.setEditable(!serverMode && !clientMode);
		refreshMultiplayerImages();
		defaultsB.setVisible(!serverMode && !clientMode);
		okB.setVisible(false);
		cancelB.setText("Close");
	}

	private void refreshMultiplayerImages() {
		boolean serverMode = room.isServerMode();
		boolean clientMode = room.isClientMode();
		boolean clientConnected = serverMode && room.serverCurrentConsole().remoteTransmitter().isClientConnected();
		boolean connectedToServer = clientMode && room.clientCurrentConsole().remoteReceiver().isConnected();
		standaloneConsoleL.setVisible(!serverMode && !clientMode);
		networkL.setVisible(serverMode || clientMode);
		serverConsoleL.setVisible(serverMode || connectedToServer);
		clientConsoleL.setVisible(clientMode || clientConnected);
	}

	private void setControlsKeyListener() {
		KeyAdapter lis = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				setControlKey(e);
			}};
		for (JTextField field : keyFieldsList)
			field.addKeyListener(lis);
	}

	private void setControlKey(KeyEvent e) {
		int code = e.getKeyCode();
		if (!KeyNames.hasName(code)) return;
		unsetControlKey(code);
		Object comp = e.getSource();
		if (comp == keyP0Up) newKEY_P0_UP = code;
		if (comp == keyP0Down) newKEY_P0_DOWN = code;
		if (comp == keyP0Left) newKEY_P0_LEFT = code;
		if (comp == keyP0Right) newKEY_P0_RIGHT = code;
		if (comp == keyP0Button) newKEY_P0_BUTTON = code;
		if (comp == keyP0Button2) newKEY_P0_BUTTON2 = code;
		if (comp == keyP1Up) newKEY_P1_UP = code;
		if (comp == keyP1Down) newKEY_P1_DOWN = code;
		if (comp == keyP1Left) newKEY_P1_LEFT = code;
		if (comp == keyP1Right) newKEY_P1_RIGHT = code;
		if (comp == keyP1Button) newKEY_P1_BUTTON = code;
		if (comp == keyP1Button2) newKEY_P1_BUTTON2 = code;
		refreshContols();
	}

	private void unsetControlKey(int code) {
		if (newKEY_P0_UP == code) newKEY_P0_UP = -1;
		if (newKEY_P0_DOWN == code) newKEY_P0_DOWN = -1;
		if (newKEY_P0_LEFT == code) newKEY_P0_LEFT = -1;
		if (newKEY_P0_RIGHT == code) newKEY_P0_RIGHT = -1;
		if (newKEY_P0_BUTTON == code) newKEY_P0_BUTTON = -1;
		if (newKEY_P0_BUTTON2 == code) newKEY_P0_BUTTON2 = -1;
		if (newKEY_P1_UP == code) newKEY_P1_UP = -1;
		if (newKEY_P1_DOWN == code) newKEY_P1_DOWN = -1;
		if (newKEY_P1_LEFT == code) newKEY_P1_LEFT = -1;
		if (newKEY_P1_RIGHT == code) newKEY_P1_RIGHT = -1;
		if (newKEY_P1_BUTTON == code) newKEY_P1_BUTTON = -1;
		if (newKEY_P1_BUTTON2 == code) newKEY_P1_BUTTON2 = -1;
	}
	
	private void acceptKeyChanges() {
		Parameters.KEY_P0_UP      = newKEY_P0_UP;
		Parameters.KEY_P0_DOWN    = newKEY_P0_DOWN;
		Parameters.KEY_P0_LEFT    = newKEY_P0_LEFT;
		Parameters.KEY_P0_RIGHT   = newKEY_P0_RIGHT;
		Parameters.KEY_P0_BUTTON  = newKEY_P0_BUTTON;
		Parameters.KEY_P0_BUTTON2 = newKEY_P0_BUTTON2;
		Parameters.KEY_P1_UP      = newKEY_P1_UP;
		Parameters.KEY_P1_DOWN    = newKEY_P1_DOWN;
		Parameters.KEY_P1_LEFT    = newKEY_P1_LEFT;
		Parameters.KEY_P1_RIGHT   = newKEY_P1_RIGHT;
		Parameters.KEY_P1_BUTTON  = newKEY_P1_BUTTON;
		Parameters.KEY_P1_BUTTON2 = newKEY_P1_BUTTON2;	
		Parameters.savePreferences();
		room.controls().initJoystickKeys();
	}

	private void buildKeyFieldsList() {
		keyFieldsList = Arrays.asList(new JTextField[] {
			keyP0Up, keyP0Down, keyP0Left, keyP0Right, keyP0Button, keyP0Button2,
			keyP1Up, keyP1Down,	keyP1Left, keyP1Right, keyP1Button, keyP1Button2
		});
		
	}

	private void initNewKeys() {
		newKEY_P0_UP      = Parameters.KEY_P0_UP; 
		newKEY_P0_DOWN    = Parameters.KEY_P0_DOWN;
		newKEY_P0_LEFT    = Parameters.KEY_P0_LEFT;
		newKEY_P0_RIGHT   = Parameters.KEY_P0_RIGHT;
		newKEY_P0_BUTTON  = Parameters.KEY_P0_BUTTON;
		newKEY_P0_BUTTON2 = Parameters.KEY_P0_BUTTON2;
		newKEY_P1_UP      = Parameters.KEY_P1_UP;
		newKEY_P1_DOWN    = Parameters.KEY_P1_DOWN;
		newKEY_P1_LEFT    = Parameters.KEY_P1_LEFT;
		newKEY_P1_RIGHT   = Parameters.KEY_P1_RIGHT;
		newKEY_P1_BUTTON  = Parameters.KEY_P1_BUTTON;
		newKEY_P1_BUTTON2 = Parameters.KEY_P1_BUTTON2;
	}

	private void setControlsKeysDefaults() {
		newKEY_P0_UP      = Parameters.DEFAULT_KEY_P0_UP; 
		newKEY_P0_DOWN    = Parameters.DEFAULT_KEY_P0_DOWN;
		newKEY_P0_LEFT    = Parameters.DEFAULT_KEY_P0_LEFT;
		newKEY_P0_RIGHT   = Parameters.DEFAULT_KEY_P0_RIGHT;
		newKEY_P0_BUTTON  = Parameters.DEFAULT_KEY_P0_BUTTON;
		newKEY_P0_BUTTON2 = Parameters.DEFAULT_KEY_P0_BUTTON2;
		newKEY_P1_UP      = Parameters.DEFAULT_KEY_P1_UP;
		newKEY_P1_DOWN    = Parameters.DEFAULT_KEY_P1_DOWN;
		newKEY_P1_LEFT    = Parameters.DEFAULT_KEY_P1_LEFT;
		newKEY_P1_RIGHT   = Parameters.DEFAULT_KEY_P1_RIGHT;
		newKEY_P1_BUTTON  = Parameters.DEFAULT_KEY_P1_BUTTON;
		newKEY_P1_BUTTON2 = Parameters.DEFAULT_KEY_P1_BUTTON2;
		refreshContols();
	}

	private void setMultiplayerDefaults() {
		serverPortTf.setText(String.valueOf(Parameters.SERVER_SERVICE_PORT));
	}

	private void setupConnectionStatulsListeners() {
		if (room == null) return;
		if (room.isServerMode()) room.serverCurrentConsole().remoteTransmitter().addConnectionStatusListener(this);
		if (room.isClientMode()) room.clientCurrentConsole().remoteReceiver().addConnectionStatusListener(this);
	}
	
	private void setEscActionListener() {
		KeyStroke escKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		Action escapeAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelAction();
			}
 			private static final long serialVersionUID = 1L;
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escKeyStroke, "ESC");
		getRootPane().getActionMap().put("ESC", escapeAction);		
	}

	private void mainTabbedPaneChanged() {
		if (!buildFinished) return;
		switch (mainTabbedPane.getSelectedIndex()) {
			case 0:
				refreshMultiplayer(); break;
			case 1:
				refreshContols(); break;
			default:
				defaultsB.setVisible(false);
				okB.setVisible(false);
				cancelB.setText("Close");
		}
	}

	private void officialWebPageAction() {
		if (!Desktop.isDesktopSupported()) return;
		try {
			Desktop desktop = Desktop.getDesktop();
			if (!desktop.isSupported(Desktop.Action.BROWSE)) return;
			setVisible(false);
			desktop.browse(new URI(Parameters.OFFICIAL_WEBSITE));
		} catch (Exception e) {
			// Give up
		}
	}
	
	private void serverStartAction() {
		if (!room.isServerMode()) {		// Will try to START
			room.morphToServerMode();
			setupConnectionStatulsListeners();
			try {
				RemoteTransmitter transmitter = room.serverCurrentConsole().remoteTransmitter();
				String portString = serverPortTf.getText().trim();
				try {
					if (portString.isEmpty()) transmitter.start();
					else transmitter.start(Integer.valueOf(portString));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid port number: " + portString);
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Could not start Server:\n" + ex, "javatari P1 Server", JOptionPane.ERROR_MESSAGE);
				room.morphToStandaloneMode();
			}
		} else {	// Will try to STOP
			try {
				RemoteTransmitter transmitter = room.serverCurrentConsole().remoteTransmitter();
				transmitter.stop();
				room.morphToStandaloneMode();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Error stopping Server:\n" + ex, "javatari P1 Server", JOptionPane.ERROR_MESSAGE);
			}
		}			
		refreshMultiplayer();
	}
	
	private void clientConnectAction() {
		if (!room.isClientMode()) {		// Will try to CONNECT
			room.morphToClientMode();
			setupConnectionStatulsListeners();
			String serverAddress = "";
			try {
				RemoteReceiver receiver = room.clientCurrentConsole().remoteReceiver();
				serverAddress = clientServerAddressTf.getText().trim();
				receiver.connect(serverAddress);
				cancelAction();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Connection failed: " + serverAddress + "\n" + ex, "javatari P2 Client", JOptionPane.ERROR_MESSAGE);
				room.morphToStandaloneMode();
			}
		} else {	// Will try to DISCONNECT 
			try {
				RemoteReceiver receiver = room.clientCurrentConsole().remoteReceiver();
				receiver.disconnect();
				room.morphToStandaloneMode();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Error disconnecting from Server:\n" + ex, "javatari P2 Client", JOptionPane.ERROR_MESSAGE);
			}
		}
		refreshMultiplayer();
	}
	
	private void defaultsAction() {
		switch(mainTabbedPane.getSelectedIndex()) {
			case 0:
				setMultiplayerDefaults(); break;
			case 1:
				setControlsKeysDefaults();
		}
	}

	private void okAction() {
		acceptKeyChanges();
		setVisible(false);
	}
	
	private void cancelAction() {
		setVisible(false);
	}

	private void buildGUI() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(SettingsDialog.class.getResource("/pc/screen/images/Favicon.png")));
		setTitle("javatari");
		setResizable(false);
		setSize(492, 324);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
			mainTabbedPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					mainTabbedPaneChanged();
				}
			});
			mainTabbedPane.setFont(new Font("Arial", Font.PLAIN, 13));
			mainTabbedPane.setBackground(UIManager.getColor("TabbedPane.background"));
			contentPanel.add(mainTabbedPane, BorderLayout.CENTER);
			
			JPanel panel_1 = new JPanel();
			mainTabbedPane.addTab("Multiplayer", null, panel_1, null);
			panel_1.setLayout(null);
			
			clientConsoleL = new JLabel("");
			clientConsoleL.setIcon(new ImageIcon(SettingsDialog.class.getResource("/pc/room/settings/images/ServerClientConsole.png")));
			clientConsoleL.setBounds(316, 124, 139, 94);
			panel_1.add(clientConsoleL);
			
			serverConsoleL = new JLabel("");
			serverConsoleL.setIcon(new ImageIcon(SettingsDialog.class.getResource("/pc/room/settings/images/ServerClientConsole.png")));
			serverConsoleL.setBounds(12, 124, 139, 94);
			panel_1.add(serverConsoleL);
			
			networkL = new JLabel("");
			networkL.setIcon(new ImageIcon(SettingsDialog.class.getResource("/pc/room/settings/images/Network.png")));
			networkL.setBounds(116, 73, 237, 98);
			panel_1.add(networkL);
			
			JLabel lblNewLabel_1 = new JLabel("P1 Server");
			lblNewLabel_1.setFont(new Font("Arial", Font.BOLD, 16));
			lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
			lblNewLabel_1.setBounds(24, 13, 100, 20);
			panel_1.add(lblNewLabel_1);
			
			serverStartB = new JButton("START");
			serverStartB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					serverStartAction();
				}
			});
			serverStartB.setBounds(20, 36, 108, 26);
			panel_1.add(serverStartB);
			
			clientConnectB = new JButton("CONNECT");
			clientConnectB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clientConnectAction();
				}
			});
			clientConnectB.setBounds(341, 36, 108, 26);
			panel_1.add(clientConnectB);
			
			clientServerAddressTf = new JTextField();
			clientServerAddressTf.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clientConnectAction();
				}
			});
			clientServerAddressTf.setFont(new Font("Arial", Font.PLAIN, 12));
			clientServerAddressTf.setBounds(337, 87, 117, 22);
			panel_1.add(clientServerAddressTf);
			clientServerAddressTf.setColumns(10);
			
			JLabel lblServerAddressport = new JLabel("Server address [:port]");
			lblServerAddressport.setHorizontalAlignment(SwingConstants.CENTER);
			lblServerAddressport.setFont(new Font("Arial", Font.PLAIN, 12));
			lblServerAddressport.setBounds(337, 70, 117, 15);
			panel_1.add(lblServerAddressport);
			
			serverPortTf = new JTextField();
			serverPortTf.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					serverStartAction();
				}
			});
			serverPortTf.setFont(new Font("Arial", Font.PLAIN, 12));
			serverPortTf.setHorizontalAlignment(SwingConstants.RIGHT);
			serverPortTf.setColumns(10);
			serverPortTf.setBounds(43, 87, 62, 22);
			panel_1.add(serverPortTf);
			
			JLabel lblPort = new JLabel("Server port");
			lblPort.setHorizontalAlignment(SwingConstants.CENTER);
			lblPort.setFont(new Font("Arial", Font.PLAIN, 12));
			lblPort.setBounds(43, 70, 62, 15);
			panel_1.add(lblPort);
			
			JLabel lblPClient = new JLabel("P2 Client");
			lblPClient.setHorizontalAlignment(SwingConstants.CENTER);
			lblPClient.setFont(new Font("Arial", Font.BOLD, 16));
			lblPClient.setBounds(345, 13, 100, 20);
			panel_1.add(lblPClient);
			
			modeL = new JLabel("P1 SERVER MODE");
			modeL.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			modeL.setFont(new Font("Arial", Font.BOLD, 13));
			modeL.setOpaque(true);
			modeL.setHorizontalAlignment(SwingConstants.CENTER);
			modeL.setBounds(160, 36, 149, 26);
			panel_1.add(modeL);
			
			standaloneConsoleL = new JLabel("");
			standaloneConsoleL.setIcon(new ImageIcon(SettingsDialog.class.getResource("/pc/room/settings/images/StandaloneConsole.png")));
			standaloneConsoleL.setBounds(118, 74, 202, 146);
			panel_1.add(standaloneConsoleL);
			{
				controlsPanel = new JPanel();
				mainTabbedPane.addTab("Controls", null, controlsPanel, null);
				controlsPanel.setLayout(null);
				{
					JLabel lblNewLabel = new JLabel("");
					lblNewLabel.setIcon(new ImageIcon(SettingsDialog.class.getResource("/pc/room/settings/images/Joystick.png")));
					lblNewLabel.setBounds(64, 76, 75, 90);
					controlsPanel.add(lblNewLabel);
				}
				
				keyP0Up = new JTextField();
				keyP0Up.setBackground(Color.WHITE);
				keyP0Up.setEditable(false);
				keyP0Up.setFont(new Font("Arial", Font.PLAIN, 11));
				keyP0Up.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0Up.setText("UP");
				keyP0Up.setBounds(81, 54, 40, 20);
				controlsPanel.add(keyP0Up);
				keyP0Up.setColumns(10);
				
				keyP0Right = new JTextField();
				keyP0Right.setBackground(Color.WHITE);
				keyP0Right.setEditable(false);
				keyP0Right.setFont(new Font("Arial", Font.PLAIN, 11));
				keyP0Right.setText("RIGHT");
				keyP0Right.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0Right.setColumns(10);
				keyP0Right.setBounds(138, 115, 40, 20);
				controlsPanel.add(keyP0Right);
				
				keyP0Left = new JTextField();
				keyP0Left.setBackground(Color.WHITE);
				keyP0Left.setEditable(false);
				keyP0Left.setFont(new Font("Arial", Font.PLAIN, 11));
				keyP0Left.setText("LEFT");
				keyP0Left.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0Left.setColumns(10);
				keyP0Left.setBounds(23, 115, 40, 20);
				controlsPanel.add(keyP0Left);
				
				keyP0Down = new JTextField();
				keyP0Down.setBackground(Color.WHITE);
				keyP0Down.setEditable(false);
				keyP0Down.setFont(new Font("Arial", Font.PLAIN, 11));
				keyP0Down.setText("DOWN");
				keyP0Down.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0Down.setColumns(10);
				keyP0Down.setBounds(81, 168, 40, 20);
				controlsPanel.add(keyP0Down);
				
				JLabel lblRight = new JLabel("Right");
				lblRight.setFont(new Font("Arial", Font.PLAIN, 12));
				lblRight.setHorizontalAlignment(SwingConstants.CENTER);
				lblRight.setBounds(139, 99, 38, 14);
				controlsPanel.add(lblRight);
				
				JLabel lblLeft = new JLabel("Left");
				lblLeft.setFont(new Font("Arial", Font.PLAIN, 12));
				lblLeft.setHorizontalAlignment(SwingConstants.CENTER);
				lblLeft.setBounds(25, 99, 36, 14);
				controlsPanel.add(lblLeft);
				
				JLabel lblDown = new JLabel("Down");
				lblDown.setFont(new Font("Arial", Font.PLAIN, 12));
				lblDown.setHorizontalAlignment(SwingConstants.CENTER);
				lblDown.setBounds(82, 189, 38, 14);
				controlsPanel.add(lblDown);
				
				JLabel lblUp = new JLabel("Up");
				lblUp.setFont(new Font("Arial", Font.PLAIN, 12));
				lblUp.setHorizontalAlignment(SwingConstants.CENTER);
				lblUp.setBounds(82, 38, 38, 14);
				controlsPanel.add(lblUp);
				
				keyP0Button = new JTextField();
				keyP0Button.setBackground(Color.WHITE);
				keyP0Button.setEditable(false);
				keyP0Button.setFont(new Font("Arial", Font.PLAIN, 11));
				keyP0Button.setText("SPC");
				keyP0Button.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0Button.setColumns(10);
				keyP0Button.setBounds(23, 66, 40, 20);
				controlsPanel.add(keyP0Button);
				
				JLabel lblFire = new JLabel("Fire 1");
				lblFire.setFont(new Font("Arial", Font.PLAIN, 12));
				lblFire.setHorizontalAlignment(SwingConstants.CENTER);
				lblFire.setBounds(24, 50, 38, 14);
				controlsPanel.add(lblFire);
				
				JLabel lblPlayer = new JLabel("Player 1");
				lblPlayer.setFont(new Font("Arial", Font.BOLD, 16));
				lblPlayer.setHorizontalAlignment(SwingConstants.CENTER);
				lblPlayer.setBounds(64, 13, 74, 20);
				controlsPanel.add(lblPlayer);
				
				JLabel lblFire_1 = new JLabel("Fire 2");
				lblFire_1.setFont(new Font("Arial", Font.PLAIN, 12));
				lblFire_1.setHorizontalAlignment(SwingConstants.CENTER);
				lblFire_1.setBounds(139, 50, 38, 14);
				controlsPanel.add(lblFire_1);
				
				keyP0Button2 = new JTextField();
				keyP0Button2.setBackground(Color.WHITE);
				keyP0Button2.setEditable(false);
				keyP0Button2.setFont(new Font("Arial", Font.PLAIN, 11));
				keyP0Button2.setText("DEL");
				keyP0Button2.setHorizontalAlignment(SwingConstants.CENTER);
				keyP0Button2.setColumns(10);
				keyP0Button2.setBounds(138, 66, 40, 20);
				controlsPanel.add(keyP0Button2);
				
				JTextPane txtpnAltJ_1 = new JTextPane();
				txtpnAltJ_1.setOpaque(false);
				txtpnAltJ_1.setEditable(false);
				txtpnAltJ_1.setFont(new Font("Arial", Font.PLAIN, 12));
				txtpnAltJ_1.setText("ALT + J : Swap P1<>P2\r\nALT + L : Toggle Paddles");
				txtpnAltJ_1.setBounds(163, 171, 145, 36);
				controlsPanel.add(txtpnAltJ_1);
				
				JLabel label = new JLabel("");
				label.setIcon(new ImageIcon(SettingsDialog.class.getResource("/pc/room/settings/images/Joystick.png")));
				label.setBounds(334, 76, 75, 90);
				controlsPanel.add(label);
				
				keyP1Up = new JTextField();
				keyP1Up.setText("T");
				keyP1Up.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1Up.setFont(new Font("Arial", Font.PLAIN, 11));
				keyP1Up.setEditable(false);
				keyP1Up.setColumns(10);
				keyP1Up.setBackground(Color.WHITE);
				keyP1Up.setBounds(351, 54, 40, 20);
				controlsPanel.add(keyP1Up);
				
				keyP1Right = new JTextField();
				keyP1Right.setText("H");
				keyP1Right.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1Right.setFont(new Font("Arial", Font.PLAIN, 11));
				keyP1Right.setEditable(false);
				keyP1Right.setColumns(10);
				keyP1Right.setBackground(Color.WHITE);
				keyP1Right.setBounds(408, 115, 40, 20);
				controlsPanel.add(keyP1Right);
				
				keyP1Left = new JTextField();
				keyP1Left.setText("F");
				keyP1Left.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1Left.setFont(new Font("Arial", Font.PLAIN, 11));
				keyP1Left.setEditable(false);
				keyP1Left.setColumns(10);
				keyP1Left.setBackground(Color.WHITE);
				keyP1Left.setBounds(293, 115, 40, 20);
				controlsPanel.add(keyP1Left);
				
				keyP1Down = new JTextField();
				keyP1Down.setText("G");
				keyP1Down.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1Down.setFont(new Font("Arial", Font.PLAIN, 11));
				keyP1Down.setEditable(false);
				keyP1Down.setColumns(10);
				keyP1Down.setBackground(Color.WHITE);
				keyP1Down.setBounds(351, 168, 40, 20);
				controlsPanel.add(keyP1Down);
				
				JLabel label_1 = new JLabel("Right");
				label_1.setHorizontalAlignment(SwingConstants.CENTER);
				label_1.setFont(new Font("Arial", Font.PLAIN, 12));
				label_1.setBounds(409, 99, 38, 14);
				controlsPanel.add(label_1);
				
				JLabel label_2 = new JLabel("Left");
				label_2.setHorizontalAlignment(SwingConstants.CENTER);
				label_2.setFont(new Font("Arial", Font.PLAIN, 12));
				label_2.setBounds(295, 99, 36, 14);
				controlsPanel.add(label_2);
				
				JLabel label_3 = new JLabel("Down");
				label_3.setHorizontalAlignment(SwingConstants.CENTER);
				label_3.setFont(new Font("Arial", Font.PLAIN, 12));
				label_3.setBounds(352, 189, 38, 14);
				controlsPanel.add(label_3);
				
				JLabel label_4 = new JLabel("Up");
				label_4.setHorizontalAlignment(SwingConstants.CENTER);
				label_4.setFont(new Font("Arial", Font.PLAIN, 12));
				label_4.setBounds(352, 38, 38, 14);
				controlsPanel.add(label_4);
				
				keyP1Button = new JTextField();
				keyP1Button.setText("A");
				keyP1Button.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1Button.setFont(new Font("Arial", Font.PLAIN, 11));
				keyP1Button.setEditable(false);
				keyP1Button.setColumns(10);
				keyP1Button.setBackground(Color.WHITE);
				keyP1Button.setBounds(293, 66, 40, 20);
				controlsPanel.add(keyP1Button);
				
				JLabel label_5 = new JLabel("Fire 1");
				label_5.setHorizontalAlignment(SwingConstants.CENTER);
				label_5.setFont(new Font("Arial", Font.PLAIN, 12));
				label_5.setBounds(294, 50, 38, 14);
				controlsPanel.add(label_5);
				
				JLabel lblPlayer_1 = new JLabel("Player 2");
				lblPlayer_1.setHorizontalAlignment(SwingConstants.CENTER);
				lblPlayer_1.setFont(new Font("Arial", Font.BOLD, 16));
				lblPlayer_1.setBounds(334, 13, 74, 20);
				controlsPanel.add(lblPlayer_1);
				
				JLabel label_7 = new JLabel("Fire 2");
				label_7.setHorizontalAlignment(SwingConstants.CENTER);
				label_7.setFont(new Font("Arial", Font.PLAIN, 12));
				label_7.setBounds(409, 50, 38, 14);
				controlsPanel.add(label_7);
				
				keyP1Button2 = new JTextField();
				keyP1Button2.setHorizontalAlignment(SwingConstants.CENTER);
				keyP1Button2.setFont(new Font("Arial", Font.PLAIN, 11));
				keyP1Button2.setEditable(false);
				keyP1Button2.setColumns(10);
				keyP1Button2.setBackground(Color.WHITE);
				keyP1Button2.setBounds(408, 66, 40, 20);
				controlsPanel.add(keyP1Button2);
				
				JLabel lbldoubleclickToChange = new JLabel("(double-click to change)");
				lbldoubleclickToChange.setToolTipText("");
				lbldoubleclickToChange.setHorizontalAlignment(SwingConstants.CENTER);
				lbldoubleclickToChange.setFont(new Font("Arial", Font.PLAIN, 12));
				lbldoubleclickToChange.setBounds(169, 16, 133, 15);
				controlsPanel.add(lbldoubleclickToChange);
			}
			
			JPanel panel = new JPanel();
			mainTabbedPane.addTab("Help", null, panel, null);
			panel.setLayout(null);
			
			JTextPane txtpnAltJ = new JTextPane();
			txtpnAltJ.setOpaque(false);
			txtpnAltJ.setEditable(false);
			txtpnAltJ.setFont(new Font("Arial", Font.PLAIN, 12));
			txtpnAltJ.setBounds(18, 10, 78, 203);
			panel.add(txtpnAltJ);
			txtpnAltJ.setText("CTR + 1-0 :\r\nALT + 1-0 :\r\n\r\nALT + ENT :\r\nALT + V :\r\nALT + R :\r\nALT + Q :\r\n\r\nALT + D :\r\nALT + C :\r\nALT + P :\r\nALT + F :\r\nTAB :");
			
			JTextPane txtpnFullscreenNtsc = new JTextPane();
			txtpnFullscreenNtsc.setOpaque(false);
			txtpnFullscreenNtsc.setEditable(false);
			txtpnFullscreenNtsc.setText("Save State\r\nLoad State\r\n\r\nFullscreen\r\nNTSC / PAL\r\nCRT Modes\r\nFilter\r\n\r\nDebug Modes\r\nCollisions\r\nPause\r\nNext Frame\r\nFast Speed");
			txtpnFullscreenNtsc.setFont(new Font("Arial", Font.PLAIN, 12));
			txtpnFullscreenNtsc.setBounds(95, 10, 92, 203);
			panel.add(txtpnFullscreenNtsc);
			
			JTextPane txtpnAltF = new JTextPane();
			txtpnAltF.setOpaque(false);
			txtpnAltF.setEditable(false);
			txtpnAltF.setText("ALT + F1 :\r\n\r\nALT + F5 :\r\nALT + F6 :\r\nF7 :\r\n");
			txtpnAltF.setFont(new Font("Arial", Font.PLAIN, 12));
			txtpnAltF.setBounds(218, 10, 71, 81);
			panel.add(txtpnAltF);
			
			JTextPane txtpnFryConsoleLoad = new JTextPane();
			txtpnFryConsoleLoad.setOpaque(false);
			txtpnFryConsoleLoad.setEditable(false);
			txtpnFryConsoleLoad.setText("Fry Console\r\n\r\nLoad Cartridge\r\nwith no Power Cycle\r\nRemove Cartridge");
			txtpnFryConsoleLoad.setFont(new Font("Arial", Font.PLAIN, 12));
			txtpnFryConsoleLoad.setBounds(287, 10, 127, 81);
			panel.add(txtpnFryConsoleLoad);
			
			JTextPane txtpnCtraltArrows = new JTextPane();
			txtpnCtraltArrows.setOpaque(false);
			txtpnCtraltArrows.setEditable(false);
			txtpnCtraltArrows.setText("CTR-ALT + Arrows :\r\nCTR-SHT + Arrows :\r\nALT-SHT + Arrows :\r\n\r\nBACKSPACE :\r\n");
			txtpnCtraltArrows.setFont(new Font("Arial", Font.PLAIN, 12));
			txtpnCtraltArrows.setBounds(218, 130, 121, 81);
			panel.add(txtpnCtraltArrows);
			
			JTextPane txtpnAlsoPossibleDo = new JTextPane();
			txtpnAlsoPossibleDo.setOpaque(false);
			txtpnAlsoPossibleDo.setEditable(false);
			txtpnAlsoPossibleDo.setText("Drag/Drop or Copy/Paste of files and URLs");
			txtpnAlsoPossibleDo.setFont(new Font("Arial", Font.PLAIN, 12));
			txtpnAlsoPossibleDo.setBounds(218, 100, 246, 21);
			panel.add(txtpnAlsoPossibleDo);
			
			JTextPane txtpnDisplayOriginDisplay = new JTextPane();
			txtpnDisplayOriginDisplay.setOpaque(false);
			txtpnDisplayOriginDisplay.setEditable(false);
			txtpnDisplayOriginDisplay.setText("Display Origin\r\nDisplay Size\r\nDisplay Scale\r\n\r\nDisplay Defaults");
			txtpnDisplayOriginDisplay.setFont(new Font("Arial", Font.PLAIN, 12));
			txtpnDisplayOriginDisplay.setBounds(343, 130, 100, 81);
			panel.add(txtpnDisplayOriginDisplay);
			{
				JPanel panel_2 = new JPanel();
				panel_2.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				panel_2.setBackground(UIManager.getColor("Panel.background"));
				mainTabbedPane.addTab("About", null, panel_2, null);
				panel_2.setLayout(null);
				{
					JLabel lblNewButton = new JLabel("");
					lblNewButton.setBounds(29, 23, 162, 158);
					panel_2.add(lblNewButton);
					lblNewButton.setIcon(new ImageIcon(SettingsDialog.class.getResource("/pc/room/settings/images/LogoAbout.png")));
					lblNewButton.setPreferredSize(new Dimension(200, 250));
					lblNewButton.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
				}
				
				JLabel lblVerion = new JLabel(Parameters.VERSION);
				lblVerion.setHorizontalAlignment(SwingConstants.CENTER);
				lblVerion.setFont(new Font("Arial", Font.PLAIN, 13));
				lblVerion.setBounds(68, 187, 85, 14);
				panel_2.add(lblVerion);
				
				JLabel lblCreate = new JLabel("Paulo Augusto Peccin");
				lblCreate.setHorizontalAlignment(SwingConstants.CENTER);
				lblCreate.setFont(new Font("Arial", Font.PLAIN, 15));
				lblCreate.setBounds(256, 62, 143, 18);
				panel_2.add(lblCreate);
				
				JLabel lblCreated = new JLabel("created by");
				lblCreated.setHorizontalAlignment(SwingConstants.CENTER);
				lblCreated.setFont(new Font("Arial", Font.PLAIN, 13));
				lblCreated.setBounds(259, 39, 137, 21);
				panel_2.add(lblCreated);
				{
					JLabel lblOfficialHomepage = new JLabel("official homepage:");
					lblOfficialHomepage.setHorizontalAlignment(SwingConstants.CENTER);
					lblOfficialHomepage.setFont(new Font("Arial", Font.PLAIN, 13));
					lblOfficialHomepage.setBounds(259, 125, 137, 21);
					panel_2.add(lblOfficialHomepage);
				}
				{
					JButton lblHttpjavatariotg = new JButton("http://javatari.org");
					lblHttpjavatariotg.setFocusPainted(false);
					lblHttpjavatariotg.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							officialWebPageAction();
						}
					});
					lblHttpjavatariotg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					lblHttpjavatariotg.setBorder(null);
					lblHttpjavatariotg.setContentAreaFilled(false);
					lblHttpjavatariotg.setBorderPainted(false);
					lblHttpjavatariotg.setHorizontalAlignment(SwingConstants.CENTER);
					lblHttpjavatariotg.setForeground(Color.BLUE);
					lblHttpjavatariotg.setFont(new Font("Arial", Font.PLAIN, 15));
					lblHttpjavatariotg.setBounds(274, 149, 107, 18);
					panel_2.add(lblHttpjavatariotg);
				}
				{
					JLabel lblppeccin = new JLabel("@ppeccin");
					lblppeccin.setHorizontalAlignment(SwingConstants.CENTER);
					lblppeccin.setFont(new Font("Arial", Font.PLAIN, 13));
					lblppeccin.setBounds(259, 82, 137, 19);
					panel_2.add(lblppeccin);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new BorderLayout(0, 0));
			{
				
				JPanel panel = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel.getLayout();
				flowLayout.setAlignment(FlowLayout.LEFT);
				buttonPane.add(panel, BorderLayout.WEST);
				
				defaultsB = new JButton("Defaults");
				defaultsB.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						defaultsAction();
					}
				});
				panel.add(defaultsB);
				
				JPanel panel_1 = new JPanel();
				buttonPane.add(panel_1, BorderLayout.EAST);
				okB = new JButton("  OK  ");
				panel_1.add(okB);
				okB.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okAction();
					}
				});
				okB.setActionCommand("OK");
				{
					cancelB = new JButton("Cancel");
					panel_1.add(cancelB);
					cancelB.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							cancelAction();
						}
					});
					cancelB.setActionCommand("Cancel");
				}
				// getRootPane().setDefaultButton(okButton);
			}
		}
		setLocationRelativeTo(null);
		buildFinished = true;
	}

	
	private final Room room;

	private final JPanel contentPanel = new JPanel();
	private JPanel controlsPanel;
	private JTextField
		keyP0Up, keyP0Down, keyP0Left, keyP0Right, keyP0Button, keyP0Button2, 
		keyP1Up, keyP1Down, keyP1Left, keyP1Right, keyP1Button, keyP1Button2;

	private List<JTextField> keyFieldsList;

	private int
		newKEY_P0_UP, newKEY_P0_DOWN, newKEY_P0_LEFT, newKEY_P0_RIGHT, newKEY_P0_BUTTON, newKEY_P0_BUTTON2,
		newKEY_P1_UP, newKEY_P1_DOWN, newKEY_P1_LEFT, newKEY_P1_RIGHT, newKEY_P1_BUTTON, newKEY_P1_BUTTON2;

	private boolean buildFinished = false;
	
	private static final long serialVersionUID = 1L;
	private JTextField clientServerAddressTf;
	private JTextField serverPortTf;
	private JButton serverStartB;
	private JButton clientConnectB;
	private JLabel modeL;
	private JTabbedPane mainTabbedPane;
	private JButton defaultsB;
	private JButton okB;
	private JButton cancelB;
	private JLabel standaloneConsoleL;
	private JLabel serverConsoleL;
	private JLabel clientConsoleL;
	private JLabel networkL;

}

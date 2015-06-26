

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import eu.telecom.sudparis.sa.serial.SerialConnection;
import eu.telecom.sudparis.sa.serial.SerialConnectionListener;
import eu.telecom.sudparis.sa.serial.SerialDumpConnection;
import eu.telecom.sudparis.sa.serial.TCPClientConnection;
import eu.telecom.sudparis.sa.serial.UDPConnection;

/**
 * IP Sensor Collect
 * @author	Son Han
 * @date	2014/04/17
 */

public class IPSensorCollect implements SerialConnectionListener {

	public static final String WINDOW_TITLE = "IP Sensor Collect";
	
	public static String SENSOR_ADDR = "aaaa:0:0:0:1202:74:cb13:f90b";
		
	private JFrame window;
	private JTextArea rawDataText;
	
	private SerialConnection serialConnection;
	private boolean hasSerialOpened;
	private boolean hasStarted = false;
	
	GradientPanel statusPanel = new GradientPanel();
	
	private class GradientPanel extends JPanel {

	    private Color color = new Color(0, 255, 255);
	    
	    public void setColor(Color color) {
	        this.color = color;
	        
	    }
	    @Override
	    public void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        Graphics2D g2d = (Graphics2D) g;
	        Color color2 = color.darker();
	        int w = getWidth();
	        int h = getHeight();
	        GradientPaint gp = new GradientPaint(0, 0, color, 0, h, color2);
	        g2d.setPaint(gp);
	        g2d.fillRect(0, 0, w, h);
	    }
	}
	
	public IPSensorCollect() {
		// JFrame.setDefaultLookAndFeelDecorated(true);
		// JDialog.setDefaultLookAndFeelDecorated(true);
		Rectangle maxSize = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getMaximumWindowBounds();

		/* Create and set up the window */
		window = new JFrame(WINDOW_TITLE);
		window.setLocationByPlatform(true);
		if (maxSize != null) {
			window.setMaximizedBounds(maxSize);
		}
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		rawDataText = new JTextArea("Raw Data");
		rawDataText.setEditable(false);
		DefaultCaret caret = (DefaultCaret) rawDataText.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBackground(Color.WHITE);
		contentPanel.add(new JScrollPane(rawDataText), BorderLayout.CENTER);		
	
		statusPanel.setPreferredSize(new Dimension(400, 100));
		
		contentPanel.add(statusPanel, BorderLayout.SOUTH);
		
		window.getContentPane().add(contentPanel, BorderLayout.CENTER);
		window.setSize(400, 500);
	}

	public void start(SerialConnection connection) {
		if (hasStarted) {
			throw new IllegalStateException("already started");
		}
		hasStarted = true;
		this.serialConnection = connection;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				window.setVisible(true);
			}
		});
		connectToSerial();
	}

	protected void connectToSerial() {
		if (serialConnection != null && !serialConnection.isOpen()) {
			String comPort = serialConnection.getComPort();
			if (comPort == null && serialConnection.isMultiplePortsSupported()) {
				// comPort = MoteFinder.selectComPort(window);
			}
			if (comPort != null || !serialConnection.isMultiplePortsSupported()) {
				serialConnection.open(comPort);
			}
		}
	}

	public void stop() {
		if (serialConnection != null) {
			serialConnection.close();
		}
		//window.setVisible(false);
	}

	protected class ConnectSerialAction extends AbstractAction implements
			Runnable {

		private static final long serialVersionUID = 1L;

		private boolean isRunning;

		public ConnectSerialAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			if (!isRunning) {
				isRunning = true;
				new Thread(this, "serial").start();
			}
		}

		public void run() {
			try {
				if (serialConnection != null) {
					if (serialConnection.isOpen()) {
						serialConnection.close();
					} else {
						connectToSerial();
					}
				} else {
					JOptionPane.showMessageDialog(window,
							"No serial connection configured", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} finally {
				isRunning = false;
			}
		}

	}

	// -------------------------------------------------------------------
	// SerialConnection Listener
	// -------------------------------------------------------------------

	@Override
	public void serialData(SerialConnection connection, String line) {
		if (line.length() == 0 || line.charAt(0) == '#') {
			// Ignore empty lines, comments, and annotations.
			return;
		}
		
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		
		String[] strs = line.split(" "); // [value, address]
		
		if (strs.length > 1){
			rawDataText.setText(rawDataText.getText() 
					+ "\n  [" + dateFormat.format(date)  + "] "
					+ line);
			int val = Integer.valueOf(strs[0]);
			statusPanel.setColor(new Color(0, val, val));
			//statusPanel.setColor(new Color(0, 0, 0, 1 - (float)val / 255));
			statusPanel.repaint();
			
		}
		System.out.println("Serial: " + line);
	}

	@Override
	public void serialOpened(SerialConnection connection) {

	}

	@Override
	public void serialClosed(SerialConnection connection) {
		String prefix;
		if (hasSerialOpened) {
			// serialConsole.addSerialData("*** Serial connection terminated ***");
			prefix = "Serial connection terminated.\n";
			hasSerialOpened = false;
		} else {
			prefix = "Failed to connect to " + connection.getConnectionName()
					+ '\n';
		}
		if (!connection.isClosed()) {
			if (connection.isMultiplePortsSupported()) {
				String options[] = { "Retry", "Search for connected nodes",
						"Cancel" };
				int value = JOptionPane
						.showOptionDialog(
								window,
								prefix
										+ "Do you want to retry or search for connected nodes?",
								"Reconnect to serial port?",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options,
								options[0]);
				if (value == JOptionPane.CLOSED_OPTION || value == 2) {
					// exit();
				} else {
					String comPort = connection.getComPort();
					if (value == 1) {

						if (comPort == null) {
							// exit();
						}
					}
					// Try to open com port again
					if (comPort != null) {
						connection.open(comPort);
					}
				}
			} else {
				// JOptionPane.showMessageDialog(window,
				// prefix, "Serial Connection Closed",
				// JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// -------------------------------------------------------------------
	// Main
	// -------------------------------------------------------------------

	public static void main(String[] args) {
		boolean useSerialOutput = true;
		String host = null;
		String command = null;
		String comPort = null;
		int port = -1;
		for (int i = 0, n = args.length; i < n; i++) {
			String arg = args[i];
			if (arg.length() == 2 && arg.charAt(0) == '-') {
				switch (arg.charAt(1)) {
				case 'a':
					if (i + 1 < n) {
						host = args[++i];
						int pIndex = host.indexOf(':');
						if (pIndex > 0) {
							port = Integer.parseInt(host.substring(pIndex + 1));
							host = host.substring(0, pIndex);
						}
					} else {
						usage(arg);
					}
					break;
				case 's': // sensor address
					if (i + 1 < n) {
						SENSOR_ADDR = args[++i];
					} else {
						usage(arg);
					}
					break;
				
				case 'h':
					usage(null);
					break;
				default:
					usage(arg);
					break;
				}
			} else if (comPort == null) {
				comPort = arg;
			} else {
				usage(arg);
			}
		}

		IPSensorCollect server = new IPSensorCollect();
		SerialConnection serialConnection;
		if (host != null) {
			if (port <= 0) {
				port = 60001;
			}
			serialConnection = new TCPClientConnection(server, host, port);
		} else if (port > 0) {
			serialConnection = new UDPConnection(server, port);
		} else {
			serialConnection = new SerialDumpConnection(server);
		}

		if (comPort != null) {
			serialConnection.setComPort(comPort);
		}
		if (!useSerialOutput) {
			serialConnection.setSerialOutputSupported(false);
		}
		server.start(serialConnection);
	}

	private static void usage(String arg) {
		if (arg != null) {
			System.err.println("Unknown argument '" + arg + '\'');
		}
		System.err.println("Usage: java SerialReader [-a host:port] [-p port] [-1 ipaddr] [-2 ipaddr] [COMPORT]");
		System.err.println("       -a : Connect to specified host:port");
		System.err.println("       -p : Read data from specified UDP port");
		System.err.println("       -1 : Sensor 1 IP address");
		System.err.println("       -2 : Sensor 2 IP address");
		System.err.println("   COMPORT: The serial port to connect to");
		System.exit(arg != null ? 1 : 0);
	}
}

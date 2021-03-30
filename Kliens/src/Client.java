import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.JRadioButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JDesktopPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.UIManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JTextPane;
import javax.swing.JFormattedTextField;

public class Client extends JFrame {

	private JPanel contentPane;
	JPanel onlineUsers_pnl;
	static TextArea messages_area;
	JPanel panel_1;
	static TextField msgField;
	static Button send_btn;
	
	//initialize Client's username
	private static String u_name = "";
	private static String to_user = "";
	private static String filePath = "";
	// initialize socket and input output streams 
    private static Socket socket            = null; 
    //private DataInputStream  input   = null; 
    private static DataOutputStream out     = null;
    private static DataInputStream socket_in       =  null;
    
    private static Package message;
    private final JDesktopPane Usernamewindow = new JDesktopPane();
    private JTextField username_field;
    private JTextField priv_totextfield;
    private JTextField group_textfield;
    private JFormattedTextField filePathField;
	/**
	 * Launch the application.
	*/
    
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client frame = new Client();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		System.out.println("Please input a username");
        while (u_name == "") {
            //line = scn.nextLine();
        	System.out.print("Waiting...\r");
        	if(u_name != "") {
        		break;
        	}
        }
        System.out.println("Your username is: " + u_name);
        // establish a connection 
        try
        { 
            socket = new Socket("127.0.0.1", 13000); 
            System.out.println("Connected"); 

            // takes input from terminal 
            //input  = new DataInputStream(System.in); 

            // sends output to the socket 
            out = new DataOutputStream(socket.getOutputStream());
            
            // takes input from the client socket 
            socket_in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        } 
        catch(UnknownHostException u) 
        { 
            System.out.println(u); 
        } 
        catch(IOException i) 
        { 
            System.out.println(i); 
        }
        sendCredentials();
         message = new Package(); 
        // readMessage thread 
        Thread readMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() { 
  
                while (true) { 
                    try { 
                        // read the message sent to this client
                    	byte b[] = new byte[1024];
                    	int bytesRead = socket_in.read(b);
                    	String message = new String(b,StandardCharsets.UTF_8);
                    	Package pack;
                    	pack = processMessage(message);
                    	if(pack.getType() == '0') {
                    		messages_area.append("\n"+ pack.getMessage());
                    	}
                    	else if(pack.getType() == '1') {
                    		String line = "\n<Private> " + pack.getMessage();
                    		messages_area.append(line);
                    		System.out.println(pack.getMessage());
                    	}
                    	else if(pack.getType() == '2') {
                    		messages_area.append("\n<Group> " + pack.getMessage());
                    		System.out.println(pack.getMessage());
                    	}
                    	else if(pack.getType() == '3') {
                    		messages_area.append("\nFile recieved!");
                    		//makeFile()
                    	}
                    	else if(pack.getType() == '4') {
                    		messages_area.append("\n"+ pack.getMessage());
                    	}
                    	
                    } catch (IOException e) { 
  
                        e.printStackTrace(); 
                    } 
                } 
            } 
        }); 
  
        readMessage.start(); 
	}
	
	public static void sendCredentials() {
		Package message1 = new Package();
		// keep reading until "exit" is input 
	    Thread sendCredentials = new Thread(new Runnable()  
	    { 
	        @Override
	        public void run() { 
	            // read the message to deliver.
	        	message1.setType('4');
	            message1.setMessage("");
	            message1.setSize(0);
	            message1.setFrom(u_name);
	            message1.printPackage();
	             		try { 
	                        // write on the output stream 
	                            byte[] bytes = message1.makePackage().getBytes();
	                            out.write(bytes);
	                            out.flush();
	                        } 
	                        catch (IOException exeption) { 
	                        	exeption.printStackTrace();
	                        } 
	             	}
	    });
	    sendCredentials.start();
	}
	
	public static Package processMessage(String message) {
		Package pack = new Package();
		char tipus = message.charAt(0);
		pack.setType(tipus);
		String m = message.substring(1);
		pack.setMessage(m);
		return pack;
	}
	
	public void sendMSG() {
	    Thread sendMessage = new Thread(new Runnable()  
	    { 
	        @Override
	        public void run() { 
	            // read the message to deliver. 
	            String line = msgField.getText();
	            message.setMessage(line);
	            message.setSize(line.length());
	            message.setFrom(u_name);
	            message.setTo(to_user);
	            message.printPackage();
	            msgField.setText("");
	            if(message.getType() == '0') {
	            	messages_area.append("\nTe: "+line);
	            	try { 
		            	// write on the output stream 
		            	byte[] bytes = /*line.getBytes("ASCII");*/message.makePackage().getBytes();
		                System.out.println(bytes.toString());
		                out.write(bytes);
		                out.flush();
		            	} 
		            catch (IOException exeption) { 
		            	exeption.printStackTrace();
		            } 
            	}
            	else if(message.getType() == '1') {
            		messages_area.append("\nTe -> "+ to_user + ": "+line);
            		try { 
    	            	// write on the output stream 
    	            	byte[] bytes = /*line.getBytes("ASCII");*/message.makePackage().getBytes();
    	                System.out.println(bytes.toString());
    	                out.write(bytes);
    	                out.flush();
    	            	} 
    	            catch (IOException exeption) { 
    	            	exeption.printStackTrace();
    	            } 
            	}
            	else if(message.getType() == '2') {
            		messages_area.append("\nTe -> Group(" + to_user + "): " + line);
            		try { 
    	            	// write on the output stream 
    	            	byte[] bytes = /*line.getBytes("ASCII");*/message.makePackage().getBytes();
    	                System.out.println(bytes.toString());
    	                out.write(bytes);
    	                out.flush();
    	            	} 
    	            catch (IOException exeption) { 
    	            	exeption.printStackTrace();
    	            } 
            	}
            	else if(message.getType() == '3') {
            		messages_area.append("\nSending file...");
            		
            	}
	            
	        }
	    });
	    sendMessage.start();
	}
	
	private static void importFile(File source, String from, String to, char type) throws IOException{
		InputStream is = null;
		try {
			is = new FileInputStream(source);
			byte[] b = new byte[2048];
			int length;
			is.read(b);
			String p = "";
			p += type + "\r\n";
			p += b.length + "\r\n";
			p += from + "\r\n";
			p += to + "\r\n";
			byte [] a = p.getBytes();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
			outputStream.write( a );
			outputStream.write( b );
			byte buff[] = outputStream.toByteArray( );
			out.write(buff);
			out.flush();
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
		finally {
			is.close();
		}
	}
	
	private static void makeFile(File dest, String message) throws IOException {
		byte [] b = message.getBytes();
		OutputStream os = null;
		InputStream is = null;
		try {
			os = new FileOutputStream(dest);
			int length;
			while((length = is.read(b)) > 0) {
				os.write(b,0,length);
			}
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
		finally {
			is.close();
			os.close();
		}
	}
	
	/*private static void copyFileUsingStream(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}*/
	
	/**
	 * Create the frame.
	 */
	public Client() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				Usernamewindow.setVisible(true);
			}
		});
		setForeground(SystemColor.desktop);
		setTitle("ChatWindowApplication");
		setBackground(Color.LIGHT_GRAY);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 599);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.controlDkShadow);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		/*JPanel*/ Usernamewindow.setBounds(269, 153, 400, 209);
		 contentPane.add(Usernamewindow);
		 Usernamewindow.setLayout(null);
		 Usernamewindow.setVisible(false);
		 
		 username_field = new JTextField();
		 username_field.addKeyListener(new KeyAdapter() {
		 	@Override
		 	public void keyPressed(KeyEvent e) {
		 		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
		 			u_name = username_field.getText();
			 		Usernamewindow.setVisible(false);
				}
		 	}
		 });
		 username_field.setFont(new Font("Tahoma", Font.PLAIN, 24));
		 username_field.setBounds(42, 84, 307, 46);
		 Usernamewindow.add(username_field);
		 username_field.setColumns(10);
		 
		 JButton usernameset_btn = new JButton("Ok");
		 usernameset_btn.addActionListener(new ActionListener() {
		 	public void actionPerformed(ActionEvent e) {
		 		u_name = username_field.getText();
		 		Usernamewindow.setVisible(false);
		 	}
		 });
		 usernameset_btn.setBounds(107, 140, 181, 46);
		 Usernamewindow.add(usernameset_btn);
		 
		 JTextPane txtpnPleaseEnterA = new JTextPane();
		 txtpnPleaseEnterA.setFont(new Font("Tahoma", Font.PLAIN, 20));
		 txtpnPleaseEnterA.setText("Please enter a username!");
		 txtpnPleaseEnterA.setBackground(UIManager.getColor("Desktop.background"));
		 txtpnPleaseEnterA.setBounds(21, 36, 354, 38);
		 Usernamewindow.add(txtpnPleaseEnterA);
		 onlineUsers_pnl = new JPanel();
		onlineUsers_pnl.setBorder(new EtchedBorder(EtchedBorder.LOWERED, SystemColor.textHighlight, null));
		onlineUsers_pnl.setBackground(SystemColor.activeCaptionBorder);
		onlineUsers_pnl.setBounds(10, 22, 211, 530);
		contentPane.add(onlineUsers_pnl);
		
		JList list = new JList();
		onlineUsers_pnl.add(list);
		
		/*TextArea*/ messages_area = new TextArea();
		messages_area.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 12));
		messages_area.setText("Chat is open...\n");
		messages_area.setBackground(SystemColor.controlHighlight);
		messages_area.setEditable(false);
		messages_area.setBounds(231, 22, 545, 466);
		contentPane.add(messages_area);
		
		/*JPanel*/ panel_1 = new JPanel();
		panel_1.setBackground(SystemColor.activeCaptionBorder);
		panel_1.setBounds(231, 494, 545, 58);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		/*TextField*/ msgField = new TextField();
		msgField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendMSG();
				}
			}
		});
		msgField.setBounds(10, 10, 435, 21);
		panel_1.add(msgField);
		
		/*Button*/ send_btn = new Button("Send");
		send_btn.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
             	 sendMSG();
          	}
         });
		send_btn.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
		send_btn.setActionCommand("SendMsg");
		send_btn.setBounds(456, 10, 79, 26);
		panel_1.add(send_btn);
		
		JComboBox type_picker = new JComboBox();
		type_picker.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch(type_picker.getSelectedIndex()) {
					case 0:
						message.setType('0');
						priv_totextfield.setVisible(false);
						group_textfield.setVisible(false);
						filePathField.setVisible(false);
						to_user = "";
						break;
					case 1:
						message.setType('1');
						priv_totextfield.setVisible(true);
						group_textfield.setVisible(false);
						filePathField.setVisible(false);
						to_user = "";
						break;
					case 2:
						message.setType('2');
						priv_totextfield.setVisible(false);
						group_textfield.setVisible(true);
						filePathField.setVisible(false);
						to_user = "";
						break;
					case 3:
						message.setType('3');
						priv_totextfield.setVisible(true);
						group_textfield.setVisible(false);
						filePathField.setVisible(true);
						to_user = "";
						break;
				}
			}
		});
		type_picker.setFont(new Font("Tahoma", Font.PLAIN, 16));
		type_picker.setModel(new DefaultComboBoxModel(new String[] {"Message to Everyone", "Private Message", "Group Message", "File Transfer"}));
		type_picker.setBounds(10, 37, 183, 21);
		panel_1.add(type_picker);
		
		priv_totextfield = new JTextField();
		priv_totextfield.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					to_user = priv_totextfield.getText();
				}
			}
		});
		priv_totextfield.setBounds(215, 37, 155, 19);
		panel_1.add(priv_totextfield);
		priv_totextfield.setColumns(10);
		priv_totextfield.setVisible(false);
		
		group_textfield = new JTextField();
		group_textfield.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					if(to_user == "") {
						to_user = group_textfield.getText();
					}
					else {
						to_user += "," + group_textfield.getText();
					}
					group_textfield.setText("");
					System.out.println(to_user);
				}
			}
		});
		group_textfield.setBounds(215, 37, 155, 19);
		panel_1.add(group_textfield);
		group_textfield.setColumns(10);
		
		filePathField = new JFormattedTextField();
		filePathField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					filePath = filePathField.getText();
				}
				filePathField.setText("");
				System.out.println(filePath);
			}
		});
		filePathField.setBounds(380, 37, 155, 19);
		panel_1.add(filePathField);
		group_textfield.setVisible(false);
	}
}

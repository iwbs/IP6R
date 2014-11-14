package com.joe.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.joe.entity.Account;
import com.joe.util.TextAreaOutputStream;


public class GUI extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 39780992847797862L;
	
	private Logger logger = null;
	
	private DataHandler dataHandler;
	private Socket socket;
	private Properties prop;
	
	private JTextField captchaTF;
	private JLabel captchaLBL;
	private Account accountEntry;
	private JLabel sendSMSLBL;
	private JTextField sendSMSTF;
	private JTextField recvSMSTF;
	private JTextField proxyURLTF;
	private JTextField proxyPortTF;
	private JCheckBox proxyCB;
	private JTextArea partNoJTA;
	private JLabel appleIdLBL;
	private JTextField timeTF;
	private JTextField phoneIPTF;
	private JTextField phonePortTF;
	private JButton CPBtn;
	private JRadioButton autoBtn;
	private JRadioButton manualBtn;
	private JRadioButton specificTSBtn;
	private JRadioButton allTSBtn;
	private JCheckBox timeslotModeCB;
	private JTextArea shopJTA;
	private JTextField FRPollIntervalTF;
	private JTextField SRPollIntervalTF;
	private JLabel govIdLBL;
	private JLabel phoneNoLBL;

	/**
	 * Create the panel.
	 */
	public GUI() {
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(450, 470));

		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(null);
		upperPanel.setPreferredSize(new Dimension(450, 320));

		captchaLBL = new JLabel("Captcha");
		captchaLBL.setBounds(10, 11, 152, 62);
		upperPanel.add(captchaLBL);

		captchaTF = new JTextField();
		captchaTF.setBounds(10, 84, 152, 20);
		captchaTF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataHandler.loginWithCaptcha(e.getActionCommand());
			}
		});
		upperPanel.add(captchaTF);

		add(upperPanel, BorderLayout.PAGE_START);
		
		JButton preCaptchaBtn = new JButton("preCaptcha");
		preCaptchaBtn.setBounds(172, 41, 93, 23);
		preCaptchaBtn.setMargin(new Insets(1,1,1,1));
		preCaptchaBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preloadCaptcha();
			}
		});
		upperPanel.add(preCaptchaBtn);
		
		JButton startBtn = new JButton("Start");
		startBtn.setBounds(270, 41, 70, 23);
		startBtn.setMargin(new Insets(1,1,1,1));
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startDataHandler();
			}
		});
		upperPanel.add(startBtn);
		
		CPBtn = new JButton("Connect phone");
		CPBtn.setBounds(344, 41, 96, 23);
		CPBtn.setMargin(new Insets(1,1,1,1));
		CPBtn.setEnabled(false);
		CPBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new Socket(phoneIPTF.getText(), Integer.parseInt(phonePortTF.getText()));
					logger.info("Phone connected");
				} catch (UnknownHostException exp) {
					System.out.println("Unknown host");
				} catch (IOException exp) {
					System.out.println("IO Exception");
					return;
				}
			}
		});
		upperPanel.add(CPBtn);
		
		manualBtn = new JRadioButton("Manual");
		manualBtn.setBounds(10, 111, 70, 23);
		manualBtn.setSelected(true);
		manualBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				phoneIPTF.setEnabled(false);
				phonePortTF.setEnabled(false);
				CPBtn.setEnabled(false);
			}
		});
		upperPanel.add(manualBtn);
		
		autoBtn = new JRadioButton("Auto");
		autoBtn.setBounds(82, 111, 70, 23);
		autoBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				phoneIPTF.setEnabled(true);
				phonePortTF.setEnabled(true);
				CPBtn.setEnabled(true);
				
				logger.info("Auto mode selected, clearing smsCode value");
				try {
					recvSMSTF.setText("");
					prop.setProperty("smsCode", "");

					File file = new File("config.properties");
					FileOutputStream fileOut = new FileOutputStream(file);
					prop.store(fileOut, "");
					fileOut.close();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		});
		upperPanel.add(autoBtn);
		
		ButtonGroup group = new ButtonGroup();
	    group.add(manualBtn);
	    group.add(autoBtn);
		
		sendSMSTF = new JTextField();
		sendSMSTF.setBounds(10, 203, 152, 20);
		sendSMSTF.setEditable(false);
		upperPanel.add(sendSMSTF);
		
		recvSMSTF = new JTextField();
		recvSMSTF.setBounds(10, 234, 152, 20);
		recvSMSTF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataHandler.verifySMS(e.getActionCommand());
			}
		});
		upperPanel.add(recvSMSTF);
		
		sendSMSLBL = new JLabel("SMS code");
		sendSMSLBL.setBounds(10, 172, 152, 20);
		upperPanel.add(sendSMSLBL);
		
		proxyCB = new JCheckBox("Use Proxy");
		proxyCB.setBounds(168, 11, 93, 23);
		proxyCB.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                	proxyURLTF.setEnabled(true);
                	proxyPortTF.setEnabled(true);
                }else{
                	proxyURLTF.setEnabled(false);
                	proxyPortTF.setEnabled(false);
                }
            }
        });
		upperPanel.add(proxyCB);
		
		proxyURLTF = new JTextField();
		proxyURLTF.setText("proxy.pccw.com");
		proxyURLTF.setBounds(267, 11, 110, 20);
		proxyURLTF.setEnabled(false);
		upperPanel.add(proxyURLTF);
		
		proxyPortTF = new JTextField();
		proxyPortTF.setText("8080");
		proxyPortTF.setBounds(382, 11, 58, 20);
		proxyPortTF.setEnabled(false);
		upperPanel.add(proxyPortTF);
		
		JLabel partNoLBL = new JLabel("Parts & Shops:");
		partNoLBL.setBounds(172, 78, 89, 14);
		upperPanel.add(partNoLBL);

		timeTF = new JTextField();
		timeTF.setBounds(172, 203, 268, 20);
		timeTF.setEditable(false);
		upperPanel.add(timeTF);
		timeTF.setColumns(10);
		
		partNoJTA = new JTextArea();
		partNoJTA.setEditable(false);
		partNoJTA.setBounds(172, 103, 268, 58);
		partNoJTA.setLineWrap(true);
		upperPanel.add(partNoJTA);
		
		appleIdLBL = new JLabel("");
		appleIdLBL.setBounds(240, 265, 200, 14);
		upperPanel.add(appleIdLBL);
		
		govIdLBL = new JLabel("");
		govIdLBL.setBounds(240, 290, 96, 14);
		upperPanel.add(govIdLBL);
		
		phoneNoLBL = new JLabel("");
		phoneNoLBL.setBounds(344, 290, 96, 14);
		upperPanel.add(phoneNoLBL);

		phoneIPTF = new JTextField();
		phoneIPTF.setBounds(10, 141, 96, 20);
		phoneIPTF.setText("192.168.3.100");
		phoneIPTF.setEnabled(false);
		upperPanel.add(phoneIPTF);
		
		phonePortTF = new JTextField();
		phonePortTF.setBounds(116, 141, 46, 20);
		phonePortTF.setText("8080");
		phonePortTF.setEnabled(false);
		upperPanel.add(phonePortTF);
		
		specificTSBtn = new JRadioButton("Specific timeslot");
		specificTSBtn.setBounds(172, 171, 121, 23);
		specificTSBtn.setSelected(true);
		specificTSBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timeTF.setEnabled(true);
			}
		});
		upperPanel.add(specificTSBtn);
		
		allTSBtn = new JRadioButton("Any timeslot");
		allTSBtn.setBounds(295, 171, 110, 23);
		allTSBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timeTF.setEnabled(false);
			}
		});
		upperPanel.add(allTSBtn);
		
		ButtonGroup TSGroup = new ButtonGroup();
		TSGroup.add(specificTSBtn);
		TSGroup.add(allTSBtn);
		
		timeslotModeCB = new JCheckBox("Request timeslot before every order");
		timeslotModeCB.setBounds(172, 233, 268, 23);
		upperPanel.add(timeslotModeCB);
		
		shopJTA = new JTextArea();
		shopJTA.setEditable(false);
		shopJTA.setBounds(267, 74, 173, 23);
		upperPanel.add(shopJTA);
		
		JLabel FRPollIntervalLBL = new JLabel("1st round interval(ms):");
		FRPollIntervalLBL.setBounds(10, 265, 152, 14);
		upperPanel.add(FRPollIntervalLBL);
		
		JLabel SRPollIntervalLBL = new JLabel("2nd/+ round interval(ms):");
		SRPollIntervalLBL.setBounds(10, 290, 152, 14);
		upperPanel.add(SRPollIntervalLBL);
		
		FRPollIntervalTF = new JTextField();
		FRPollIntervalTF.setBounds(172, 263, 58, 20);
		FRPollIntervalTF.setText("1000");
		upperPanel.add(FRPollIntervalTF);

		SRPollIntervalTF = new JTextField();
		SRPollIntervalTF.setBounds(172, 287, 58, 20);
		SRPollIntervalTF.setText("3500");
		upperPanel.add(SRPollIntervalTF);
		
		JTextArea textArea = new JTextArea();
		textArea.setBounds(10, 115, 430, 194);
		textArea.setEditable(false);

		PrintStream con = new PrintStream(new TextAreaOutputStream(textArea, 300));
		System.setOut(con);
		System.setErr(con);

		add(new JScrollPane(textArea), BorderLayout.CENTER);
		
		logger = Logger.getLogger(GUI.class);
	}
	
	private void getSMSResult(final String input){
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				BufferedReader fromServer = null;
				PrintWriter toServer = null;

				try {
					fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
					toServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
				} catch (IOException ex) {
					System.out.println("IO Exception");
				}
				
				toServer.println(input);
					
				try {
					String recvMsg = fromServer.readLine();
					String[] msg = recvMsg.split(" ");
					String finalCode = msg[msg.length-1];
					
					recvSMSTF.setText(finalCode);
					dataHandler.verifySMS(finalCode);
					
					prop.setProperty("smsCode", finalCode);

					File file = new File("config.properties");
					FileOutputStream fileOut = new FileOutputStream(file);
					prop.store(fileOut, "");
					fileOut.close();
				} catch (IOException ex) {
					System.out.println("Applet receive failed:");
				}

				toServer.close();
			}
		};
		(new Thread(runnable)).start();
	}
	
	public void setProfile(Properties prop){
		this.prop = prop;
		
		accountEntry = new Account();
		accountEntry.setId(prop.getProperty("appleId"));
		accountEntry.setPassword(prop.getProperty("password"));
		accountEntry.setFirstName(prop.getProperty("firstName"));
		accountEntry.setLastName(prop.getProperty("lastName"));
		accountEntry.setGovtIdType(prop.getProperty("govtIdType"));
		accountEntry.setGovtId(prop.getProperty("govtId"));
		accountEntry.setPhoneNumber(prop.getProperty("phone"));
		accountEntry.setTargetProductListStr(prop.getProperty("parts"));
		accountEntry.setTargetProductList(Arrays.asList(prop.getProperty("parts").split(",")));
		accountEntry.setTimeslot(prop.getProperty("timeslot"));
		accountEntry.setTargetShopList(Arrays.asList(prop.getProperty("shops").split(",")));
		
		recvSMSTF.setText(prop.getProperty("smsCode"));
		shopJTA.setText(accountEntry.getTargetShopList().toString());
		partNoJTA.setText(accountEntry.getTargetProductList().toString());
		timeTF.setText(accountEntry.getTimeslot());
		
		appleIdLBL.setText(accountEntry.getId());
		govIdLBL.setText(accountEntry.getGovtIdType());
		phoneNoLBL.setText(accountEntry.getPhoneNumber());
	}
	
	public void initDataHandler(){
		dataHandler = new DataHandler(this);
	}
	
	public void preloadCaptcha(){
		dataHandler.preloadCaptcha();
	}

	public void startDataHandler(){
		dataHandler.getLoginPage();
	}
	
	public void closeDataHandler(){
		dataHandler.close();
	}
	
	public Account getAccount(){
		return accountEntry;
	}
	
	public void setCaptchaImage(InputStream is){
		try {
			Image image = ImageIO.read(is);
			captchaLBL.setIcon(new ImageIcon(image));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setSendSMSCodeLabel(String base64){
		try {
			byte[] btDataFile = Base64.decodeBase64(base64);
			BufferedImage image = (BufferedImage)ImageIO.read(new ByteArrayInputStream(btDataFile));

			String result = "";
	        try {
	        	Tesseract instance = Tesseract.getInstance(); // JNA Interface Mapping
	        	instance.setTessVariable("tessedit_char_whitelist", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-");
	        	result = instance.doOCR(image).trim().replaceAll(" ", "")
	        			.replaceAll("1T", "TT");	//imperfect OCR...
	        } catch (TesseractException e) {
	            System.err.println(e.getMessage());
	        }
	        
	        if(autoBtn.isSelected()){
	        	getSMSResult(result);
	        }
			
			// recognizes both characters and barcodes
			sendSMSTF.setText(result);
			
			Image icon = image.getScaledInstance(sendSMSLBL.getWidth(), sendSMSLBL.getHeight(), Image.SCALE_SMOOTH);
			sendSMSLBL.setIcon(new ImageIcon(icon));
			
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    clipboard.setContents(new StringSelection(result), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setSendSMSCode(String sendSMSCode){
		sendSMSTF.setText(sendSMSCode);
	}
	
	public String getCaptchaText(){
		return captchaTF.getText();
	}
	
	public boolean useProxy(){
		return proxyCB.isSelected();
	}
	
	public String getProxyURL(){
		return proxyURLTF.getText();
	}
	
	public int getProxyPort(){
		return Integer.parseInt(proxyPortTF.getText());
	}
	
	public boolean specificTimeslotOnly(){
		return specificTSBtn.isSelected();
	}
	
	public boolean alwaysRequestTimeslot(){
		return timeslotModeCB.isSelected();
	}
	
	public int getFRPollInterval(){
		return Integer.parseInt(FRPollIntervalTF.getText());
	}
	
	public int getSRPollInterval(){
		return Integer.parseInt(SRPollIntervalTF.getText());
	}
}

package com.cse.gcmaclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TableLayout chatScrollView;
	private TextView groupNametextView;
	private EditText chatMessageEditText;
	Button sendButton;

	static final int BUFFER_SIZE = 516;
	String Username = "meuru";
	static final String Blank = "0";
	
	private Communicator sender;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// initialize components
		chatScrollView = (TableLayout) findViewById(R.id.chatTableScrollView);
		groupNametextView = (TextView) findViewById(R.id.groupNametextView);
		chatMessageEditText = (EditText) findViewById(R.id.chatMessageEditText);
		sendButton = (Button) findViewById(R.id.sendButton);

		// add click listeners to the buttons
		sendButton.setOnClickListener(sendButtonListener);
		sender = new Communicator();
		sender.execute("I"); 		//Initialize the user
	}

	public OnClickListener sendButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (chatMessageEditText.getText().length() > 0) {
				sender.execute("M");
				chatMessageEditText.setText("");
				// MessageToInput.setText("");
			}

		}
	};

	// this methods handles all the communication

	private void updateCommunicationHistory() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Communicator class is responsible for sending messages to the remote
	 * server.
	 * 
	 * @author Meuru Muthuthanthri
	 * 
	 */
	private class Communicator extends AsyncTask<String, String, String> {

		public static final String SERVERIP = "10.100.7.171";
		public static final int SERVERPORT = 9999;

		private InetAddress serverAddr;
		private DatagramSocket socket;

		public Communicator() {
			super();
			try {
				serverAddr = InetAddress.getByName(SERVERIP);
				socket = new DatagramSocket();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		protected String doInBackground(String... args) {
			try {
				
				switch (args[0].charAt(0)){
				case 'I' :
					initializeUser();
					break;
				case 'M' :
					sendChatMessage();
					break;
				
				}				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * Sends a UDP packet using the datagram socket
		 * 
		 * @param packetMsg
		 *            The message required to send
		 * @throws IOException
		 */
		private void sendPacket(byte[] packetMsg) throws IOException {
			DatagramPacket packet = new DatagramPacket(packetMsg,
					packetMsg.length, serverAddr, SERVERPORT);
			socket.send(packet);
		}

		private byte[] makePacket(int PacketType, String Username,
				String Message) {

			// This method creates a packet from a packet type, user name, and
			// message
			// Packet type => 1 = initializing user name
			// 2 = leave message
			// 3 = chat message

			byte[] data = new byte[512];

			byte[] UsernameA = Username.getBytes();
			byte[] MessageA = Message.getBytes();

			int i = 0;
			// TRY MAKING A STRING AND CONVERING TO A BYTE ARRAY

			data[i++] = (byte) PacketType;
			data[i++] = (byte) 'R';
			data[i++] = (byte) 'E';
			data[i++] = (byte) UsernameA.length;
			for (int n = 0; n < UsernameA.length; n++)
				data[i++] = UsernameA[n];
			data[i++] = (byte) MessageA.length;
			for (int n = 0; n < MessageA.length; n++)
				data[i++] = MessageA[n];

			return data;
		}

		private void initializeUser() throws IOException {
			int packetType = 1;
			byte[] bytePacket = makePacket(packetType, Username, Blank);
			sendPacket(bytePacket);
		}

		private void sendChatMessage() throws IOException {
			int packetType = 3;
			String messageTo = "";
			String message = chatMessageEditText.getText().toString();
			byte[] bytePacket = makePacket(packetType, messageTo, message);
			sendPacket(bytePacket);
		}

		private void sendleaveMessage() throws IOException {
			int packetType = 2;
			byte[] bytePacket = makePacket(packetType, Blank, Blank);
			sendPacket(bytePacket);
		}

		
	}

}

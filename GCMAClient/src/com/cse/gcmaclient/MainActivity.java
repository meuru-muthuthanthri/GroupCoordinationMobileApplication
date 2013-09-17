package com.cse.gcmaclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * The main GUI class
 * @author Meuru Muthuthanthri
 *
 */
public class MainActivity extends Activity {
	public static final String SERVERIP = "10.8.108.181";
	public static final int SERVERPORT = 9999;
	public static final String MESSAGE = "com.example.GCMA.CHAT";
	public static final String MESAGE_TYPE = "com.example.GCMA.TYPE";

	public static DatagramSocket Socket;
	public static Handler comHandler;
	private TableLayout chatScrollView;
	private TextView groupNametextView;
	private EditText chatMessageEditText;
	Button sendButton;

	static final int BUFFER_SIZE = 516;
	String Username = "meuru";
	static final String BLANK = "0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// initialize components
		chatScrollView = (TableLayout) findViewById(R.id.chatTableScrollView);
		groupNametextView = (TextView) findViewById(R.id.groupNametextView);
		chatMessageEditText = (EditText) findViewById(R.id.chatMessageEditText);
		sendButton = (Button) findViewById(R.id.sendButton);

		try {
			Socket = new DatagramSocket(9191);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// add click listeners to the buttons
		sendButton.setOnClickListener(sendButtonListener);

		initilaizeUser(Username);
		comHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (!(msg.getData().getString(CommunicationReceiver.NAME)
						.equals(Username))) {
					insertChatMsgInScrollView(
							msg.getData().getString(
									CommunicationReceiver.MESSAGE),
							msg.getData().getString(CommunicationReceiver.NAME));
				}
			}
		};

		Thread communicationThread = new CommunicationReceiver(comHandler);
		communicationThread.start();
	}

	/**
	 * Sends communication server the initialization message, containing the
	 * username. The server keeps the IP address and the port number matched to
	 * the username for references
	 * 
	 * @param username
	 *            User name of the application user
	 */
	private void initilaizeUser(String username) {
		// Uses the syncTask to initialize user on the server in background
		new Communicator().execute(username);
	}

	public OnClickListener sendButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (chatMessageEditText.getText().length() > 0) {
				String chatMessage = chatMessageEditText.getText().toString();
				sendMesage(chatMessage);
				insertChatMsgInScrollView(chatMessage, "Me");

				chatMessageEditText.setText("");
			}

		}
	};

	/**
	 * Inserts the received message from the server to the scrollview of the
	 * main activity
	 * 
	 * @param chatMessage
	 *            the message received from the server
	 * @param user
	 *            the person who send the message
	 */
	public void insertChatMsgInScrollView(String chatMessage, String user) {
		// Get the LayoutInflator service
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View newCommunicationRow = inflater.inflate(R.layout.communication_row,
				null);
		TextView newchatTextView = (TextView) newCommunicationRow
				.findViewById(R.id.chatTextView);
		newchatTextView.setText(user + " : " + chatMessage);
		// newchatTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
		chatScrollView.addView(newCommunicationRow);
	}

	/**
	 * Sends a chat message to the server using AsyncTask in background
	 * 
	 * @param chatMessage
	 *            The chat message
	 */
	private void sendMesage(String chatMessage) {
		Intent intent = new Intent(MainActivity.this, CommunicationSender.class);
		intent.putExtra(MESSAGE, chatMessage);
		intent.putExtra(MESAGE_TYPE, "Chat");
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onDestroy() {
		// sendleaveMessage();
	}

	/**
	 * Communicator class is responsible for sending messages to the remote
	 * server.
	 * 
	 * @author Meuru Muthuthanthri
	 * 
	 */
	private class Communicator extends AsyncTask<String, String, String> {

		private InetAddress serverAddr;

		public Communicator() {
			super();
			try {
				serverAddr = InetAddress.getByName(SERVERIP);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		protected String doInBackground(String... args) {
			try {
				initializeUser(args[0]);
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
			Socket.send(packet);
		}

		/**
		 * Creates data packets to send to the server
		 * 
		 * @param PacketType
		 *            type of the packet 1 = initializing user name 2 = leave
		 *            message 3 = chat message
		 * @param Username
		 *            person who sends the packet
		 * @param Message
		 *            message sent within the data packet
		 * @return
		 */
		private byte[] makePacket(int PacketType, String Username,
				String Message) {
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

		private void initializeUser(String username) throws IOException {
			int packetType = 1;
			byte[] bytePacket = makePacket(packetType, username, BLANK);
			sendPacket(bytePacket);
		}

		private void sendleaveMessage() throws IOException {
			int packetType = 2;
			byte[] bytePacket = makePacket(packetType, BLANK, BLANK);
			sendPacket(bytePacket);
		}
	}

}

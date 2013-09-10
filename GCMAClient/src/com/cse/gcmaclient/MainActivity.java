package com.cse.gcmaclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
	private DatagramSocket communicationSocket;
	DatagramPacket FinalPacket;
	// ChatPacket PacketToServer;
	String MachinePort;
	String Machine;
	String Port;
	String Username = "meuru";
	String MessageTo;
	String Message;
	String serverAddress = "localhost";
	String Blank = "0";
	int PacketType;
	int MessageLength;
	int UsernameLength;
	int MessageToLength;
	int PortInt = 9999;
	int MachineLength;
	int PortLength;
	byte[] FinalBytePacket;
	byte[] data;
	byte[] PacketTypeByte = new byte[1];
	byte[] ULengthByte;
	byte[] UsernameByte;
	byte[] MessageToLengthByte;
	byte[] MessageToByte;
	byte[] MessageLengthByte;
	byte[] MessageByte;
	byte[] ResvByte;
	byte[] BlankByte = new byte[1];
	String UIStatus = new String("Not Set Yet\n");

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

		establishCommunication();
		// updateCommunicationHistory();
	}

	public OnClickListener sendButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (chatMessageEditText.getText().length() > 0) {
				sendChatMessage();

				// Update Interface States
				chatMessageEditText.setText("");
				// MessageToInput.setText("");
			}

		}
	};

	// this methods handles all the communication
	private void establishCommunication() {
		try {
			// create the master thread for the new client interface
			communicationSocket = new DatagramSocket();
		} catch (SocketException se) {
			System.err.println(se);
		}

		// start the message receiver

		initializeUserName();
	}

	private void initializeUserName() {
		PacketType = 1;
		FinalBytePacket = MakePacket(PacketType, Username, Blank);
		SendPacket(FinalBytePacket);

	}

	public byte[] MakePacket(int PacketType, String Username, String Message) {

		// This method creates a packet from a packet type, user name, and
		// message
		// Packet type => 1 = initializing user name
		// 2 = leave message
		// 3 = chat message

		data = new byte[512];

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

	public void SendPacket(byte[] SendingBytePacket) {

		// method which sends the packet that was created out to the server

		try {
			InetAddress serverIP;
			// serverIP = InetAddress.getByName(serverAddress);
			serverIP = InetAddress.getByName("10.8.108.200");
			PortInt = 9999;
			// System.out.println("Server IP : "+ serverIP.toString());
			FinalPacket = new DatagramPacket(SendingBytePacket,
					SendingBytePacket.length, serverIP, PortInt);
			System.out.println(" Trying to send packet port : "
					+ FinalPacket.getPort() + " serverIP : "
					+ FinalPacket.getAddress().toString());
			System.out.println("datagram port :"
					+ communicationSocket.getPort());
			communicationSocket.send(FinalPacket);

		} catch (UnknownHostException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private void sendChatMessage() {
		PacketType = 3;
		setValues();
		FinalBytePacket = MakePacket(PacketType, MessageTo, Message);
		SendPacket(FinalBytePacket);
	}

	private void sendleaveMessage() {
		PacketType = 2;
		FinalBytePacket = MakePacket(PacketType, Blank, Blank);
		SendPacket(FinalBytePacket);
	}

	private void setValues() {
		MessageTo = "";
		Message = chatMessageEditText.getText().toString();
	}

	private void updateCommunicationHistory() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

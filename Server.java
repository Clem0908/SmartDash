import java.net.SocketException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

import java.util.Arrays;
import java.util.Scanner;

import java.lang.Runtime;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;

import java.io.IOException;
import java.net.InetSocketAddress;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {

	private static int fromFloatBytesToInt(byte b0, byte b1, byte b2, byte b3) {
		
		int asInt = (b0 & 0xFF)
		    | ((b1 & 0xFF) << 8)
		    | ((b2 & 0xFF) << 16)
		    | ((b3 & 0xFF) << 24);
		float asFloat = (int) Float.intBitsToFloat(asInt);
		double conv = asFloat * 3.6;
		int speed = (int) conv;

		return speed;
	}

	public static void main(String args[]) throws Exception, IOException 
	{

		System.out.print("Live for Speed OutGauge port : ");
		Scanner scanner = new Scanner(System.in);
		int lfs_port = scanner.nextInt();
		System.out.print("Smartphone listening port : ");
		int server_port = scanner.nextInt();
		System.out.print("Smartphone IP address : ");
		String smartphone_ip = new String();
		smartphone_ip = scanner.next();

		DatagramSocket lfs_ds = new DatagramSocket(lfs_port);
		byte data[] = new byte[96];
		DatagramPacket lfs_dp = new DatagramPacket(data,data.length);
		byte lfs_data[] = new byte[96];

		lfs_ds.receive(lfs_dp);
		lfs_ds.close();
		lfs_data = lfs_dp.getData();

		int speed = fromFloatBytesToInt(lfs_data[12],lfs_data[13],lfs_data[14],lfs_data[15]);

		DatagramSocket server_ds = new DatagramSocket(server_port);
		server_ds.setBroadcast(true);
		byte data1[] = new byte[96];
		for(int i = 0; i < 96; i++) {
			data1[i] = data[i];
		}
		DatagramPacket server_dp = new DatagramPacket(data1,data1.length,Inet4Address.getByName(smartphone_ip),server_port);
		server_ds.send(server_dp);
		server_ds.close();

		while(true)
		{
			lfs_ds = new DatagramSocket(lfs_port);
			lfs_dp = new DatagramPacket(data,data.length);
			 
			lfs_ds.receive(lfs_dp);
			lfs_ds.close();
			
			server_ds = new DatagramSocket(server_port);
			server_ds.setBroadcast(true);

			for(int i = 0; i < 96; i++) {
				data1[i] = data[i];
			}

			server_dp = new DatagramPacket(data1,data1.length,Inet4Address.getByName(smartphone_ip),server_port);
			server_ds.send(server_dp);
			server_ds.close();
			lfs_data = lfs_dp.getData();

			speed = fromFloatBytesToInt(lfs_data[12],lfs_data[13],lfs_data[14],lfs_data[15]);
		}
	}	
	
}

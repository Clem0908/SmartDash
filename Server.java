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

import java.io.File;
import java.io.FileWriter;

public class Server {

	private int lfs_port;
	private int server_port;
	private String smartphone_ip;

	public Server(int lfs_port, int server_port, String smartphone_ip) {
		
		this.lfs_port = lfs_port;
		this.server_port = server_port;
		this.smartphone_ip = smartphone_ip;
	}
	
	public int getLfsPort() {
		return this.lfs_port;
	}

	public int getServerPort() {
		return this.server_port;
	}

	public String getSmartphoneIp() {
		return this.smartphone_ip;
	}

	private static Server confFile() throws IOException {
		
		File confFile = new File("./Server.conf");
		
		if(confFile.exists()) {

			System.out.println("Server.conf found - using these settings: ");

			Scanner scanner = new Scanner(confFile);
			int lfs_port = scanner.nextInt();
			System.out.print("Live for Speed 'cfg.txt' OutGauge port: "+lfs_port+" | ");
			
			int server_port = scanner.nextInt();
			System.out.print("Port to set on the Android device: "+server_port+" | ");
			
			String smartphone_ip = new String();
			smartphone_ip = scanner.next();
			System.out.println("IP to set on the Android device: "+smartphone_ip);
			
			Server s = new Server(lfs_port,server_port,smartphone_ip);
			
			return s;

		} else {
			
			System.out.println("Server.conf not found - asking settings and creating one...");

			System.out.print("Live for Speed OutGauge port ? (e.g. 30001): ");
			Scanner scanner = new Scanner(System.in);
			int lfs_port = scanner.nextInt();
			
			System.out.print("Android device port to send ? (e.g. 30002): ");
			int server_port = scanner.nextInt();
			
			System.out.print("Android device IP address ? (e.g. 192.168.0.255): ");
			String smartphone_ip = new String();
			smartphone_ip = scanner.next();

			confFile.createNewFile();	
			FileWriter confFileWriter = new FileWriter(confFile);
			
			String confString = new String(String.valueOf(lfs_port)+"\n"+String.valueOf(server_port)+"\n"+smartphone_ip);
			confFileWriter.write(confString);
			confFileWriter.close();

			Server s = new Server(lfs_port,server_port,smartphone_ip);
			
			return s;
		}

	}

	public static void main(String args[]) throws Exception, IOException 
	{
		Server s = confFile();
		System.out.println("\nRunning...");	

		int lfs_port = s.getLfsPort();
		int server_port = s.getServerPort();
		String smartphone_ip = s.getSmartphoneIp();

		DatagramSocket lfs_ds = new DatagramSocket(lfs_port);
		byte data[] = new byte[96];
		DatagramPacket lfs_dp = new DatagramPacket(data,data.length);

		lfs_ds.receive(lfs_dp);
		lfs_ds.close();


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
		}
	}	
	
}

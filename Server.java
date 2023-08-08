import java.net.SocketException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Arrays;
import java.lang.Runtime;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Inet4Address;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Server
{

	public static void main(String args[]) throws Exception, IOException 
	{

		int lfs_port = 60008;

		DatagramSocket lfs_ds = new DatagramSocket(lfs_port);
		byte data[] = new byte[96];
		DatagramPacket lfs_dp = new DatagramPacket(data,data.length);
		byte lfs_data[] = new byte[96];

		lfs_ds.receive(lfs_dp);
		lfs_ds.close();
		lfs_data = lfs_dp.getData();

		int asInt = (lfs_data[12] & 0xFF)
		    | ((lfs_data[13] & 0xFF) << 8)
		    | ((lfs_data[14] & 0xFF) << 16)
		    | ((lfs_data[15] & 0xFF) << 24);
		float asFloat = (int) Float.intBitsToFloat(asInt);
		double conv = asFloat * 3.6;
		int speed = (int) conv;

		String fileSpeedPath = "./lfs/speed.txt";
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileSpeedPath), StandardCharsets.UTF_8);
		writer.write("");
		writer.write(String.valueOf(speed));
		writer.close();

		DatagramSocket server_ds = new DatagramSocket(60009);
		server_ds.setBroadcast(true);
		byte data1[] = new byte[96];
		for(int i = 0; i < 96; i++) {
			data1[i] = data[i];
		}
		DatagramPacket server_dp = new DatagramPacket(data1,data1.length,Inet4Address.getByName("192.168.0.15"),60009);
		server_ds.send(server_dp);
		server_ds.close();

		while(true)
		{
			lfs_ds = new DatagramSocket(lfs_port);
			lfs_dp = new DatagramPacket(data,data.length);
			 
			lfs_ds.receive(lfs_dp);
			lfs_ds.close();
			
			server_ds = new DatagramSocket(60009);
			server_ds.setBroadcast(true);
			for(int i = 0; i < 96; i++) {
				data1[i] = data[i];
			}
			server_dp = new DatagramPacket(data1,data1.length,Inet4Address.getByName("192.168.0.15"),60009);
			server_ds.send(server_dp);
			server_ds.close();
			lfs_data = lfs_dp.getData();

			asInt = (lfs_data[12] & 0xFF)
			    | ((lfs_data[13] & 0xFF) << 8)
			    | ((lfs_data[14] & 0xFF) << 16)
			    | ((lfs_data[15] & 0xFF) << 24);
			asFloat = (int) Float.intBitsToFloat(asInt);
			conv = asFloat * 3.6;
			speed = (int) conv;

			writer = Files.newBufferedWriter(Paths.get(fileSpeedPath), StandardCharsets.UTF_8);
			writer.write("");
			writer.write(String.valueOf(speed));
			writer.close();
			System.out.println(speed+" km/h");

		}
	}	
	
}

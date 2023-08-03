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
import java.net.Inet4Address;

public class Server {

public static void main(String args[]) throws Exception {

	int port = 60008;
	DatagramSocket lfs_ds = new DatagramSocket(port,Inet4Address.getByName("127.0.0.1"));
	
	if(lfs_ds == null)
	{
		System.out.println("Unable to create the socket");
		return;
	}

	byte data[] = new byte[96];
	DatagramPacket lfs_dp = new DatagramPacket(data,data.length);

	DatagramSocket server = new DatagramSocket(60009);

	while(lfs_ds.isBound())
	{
		lfs_ds.receive(lfs_dp);

		if(server == null)
		{
			System.out.println("Unable to create the socket");
			return;
		}

		server.setBroadcast(true);
		byte data_s[] = new byte[96];
		
		for(int i = 0; i < 96; i++)
		{
			data_s[i] = data[i];
		}

		DatagramPacket server_p = new DatagramPacket(data_s,data_s.length,Inet4Address.getByName("192.168.0.254"),60009);
		server.send(server_p);

	}
		
	lfs_ds.close();
	server.close();
	

	/*DatagramSocket lfs_ds = new DatagramSocket(60008,Inet4Address.getByName("127.0.0.1"));
	byte buffer_dp[] = new byte[128];
	DatagramPacket lfs_dp = new DatagramPacket(buffer_dp,buffer_dp.length,Inet4Address.getByName("127.0.0.1"),60008);
	byte lfs_data[] = new byte[128];
    
	while(lfs_ds.isBound())
	{
		//Recevoir de LFS
		lfs_ds.receive(lfs_dp);
		lfs_data = lfs_dp.getData();

	    int asInt = (lfs_data[12] & 0xFF)
		    | ((lfs_data[13] & 0xFF) << 8)
		    | ((lfs_data[14] & 0xFF) << 16)
		    | ((lfs_data[15] & 0xFF) << 24);
	    float asFloat = Float.intBitsToFloat(asInt);
	    asInt = (lfs_data[16] & 0xFF)
		    | ((lfs_data[17] & 0xFF) << 8)
		    | ((lfs_data[18] & 0xFF) << 16)
		    | ((lfs_data[19] & 0xFF) << 24);
	    asFloat = Float.intBitsToFloat(asInt);

		lfs_ds.close(); 

		DatagramSocket server_ds = new DatagramSocket(60009,Inet4Address.getByName("192.168.0.255"));
		byte buffer_dp1[] = new byte[128];

    		DatagramPacket server_dp = new DatagramPacket(buffer_dp1, buffer_dp1.length);
		System.out.println("server");
		if(server_ds == null) { System.out.println("null client");}

		server_ds.receive(server_dp);
		System.out.println("grecu");

		byte data1[] = new byte[128];
		data1 = server_dp.getData();
		String ret = new String();
		for(int i = 0; i < 6; i++) {
			ret += (char) data1[i];
		}
		System.out.println("Android : "+ret);
		
		p.setPort(60009);
		server.send(p);

		server.close();
	s = new DatagramSocket(60008);

    }

    Inet4Address addr1 = (Inet4Address) InetAddress.getByName("192.168.0.15");
    DatagramSocket s1 = new DatagramSocket(60007, addr1);
    byte data1[] = new byte[128];
    DatagramPacket p1 = new DatagramPacket(data1, 128);
    s1.connect(addr1,60007);
    while(!s1.isConnected() && !s1.isBound()) {

	    s1.connect(addr1,60007);
    }
    System.out.println("We are connected and binded");

    System.out.println("Display 1: " + Arrays.copyOfRange(data,59,73).toString());

*/
}

}

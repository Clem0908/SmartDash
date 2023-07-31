import java.net.SocketException;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class Main {

public static void main(String args[]) throws Exception {

    Inet4Address addr = (Inet4Address) InetAddress.getByName("127.0.0.1");
    DatagramSocket s = new DatagramSocket(60008, addr);
    byte buffer[] = new byte[128];
    DatagramPacket p = new DatagramPacket(buffer, buffer.length);
    byte data[] = new byte[128];

    s.receive(p);
    int taille = p.getLength();
    data = p.getData();
    String car = new String();
    for(int i = 4; i < 8; i++) {
        car += (char) data[i];
    }
    System.out.println("Voiture : "+car);
    System.out.println("Drapaux : "+data[8]+data[9]);
    System.out.println("Rapport : "+data[10]);
    System.out.println("PLID : "+data[11]);
    int asInt = (data[12] & 0xFF)
            | ((data[13] & 0xFF) << 8)
            | ((data[14] & 0xFF) << 16)
            | ((data[15] & 0xFF) << 24);
    float asFloat = Float.intBitsToFloat(asInt);
    System.out.println("Vitesse : "+asFloat*3.6);
    asInt = (data[16] & 0xFF)
            | ((data[17] & 0xFF) << 8)
            | ((data[18] & 0xFF) << 16)
            | ((data[19] & 0xFF) << 24);
    asFloat = Float.intBitsToFloat(asInt);
    System.out.println("RPMs : "+asFloat);

    //System.out.println("Display 1: " + Arrays.copyOfRange(data,59,73).toString());


}

}

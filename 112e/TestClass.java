import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import ru.geekbrains.chat.network.TCPConnection;
import ru.geekbrains.chat.network.TCPConnectionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
public class TestClass {
    public String getInfo(){
        return "This text of class.";
    }
}
public class NewServlet extends HttpServlet {
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
    }
    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        String name = request.getParameter("name");
        String surname = request.getParameter("surname");
        PrintWriter pw = response.getWriter();
        pw.println("<html>");
        pw.println("<h1> HELLO, " + name + " " + surname + " </h1>");
        pw.println("</html>");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/testJsp.jsp");
        dispatcher.forward(request, response);
    }
}
public class TestASClient {
	public static void main(String[] args) throws InterruptedException {	
		try(Socket socket = new Socket("localhost", 3345);	
				BufferedReader br =new BufferedReader(new InputStreamReader(System.in));
				DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
				DataInputStream ois = new DataInputStream(socket.getInputStream());	)
		{
			System.out.println("Client connected to socket.");
			System.out.println();
			System.out.println("Client writing channel = oos & reading channel = ois initialized.");	
				while(!socket.isOutputShutdown()){					
					if(br.ready()){					
			System.out.println("Client start writing in channel...");
			Thread.sleep(1000);
			String clientCommand = br.readLine();			
			oos.writeUTF(clientCommand);
			oos.flush();
			System.out.println("Clien sent message " + clientCommand + " to server.");
			Thread.sleep(1000);		
			if(clientCommand.equalsIgnoreCase("quit")){			
				System.out.println("Client kill connections");
				Thread.sleep(2000);				
				if(ois.available()!=0)		{	
					System.out.println("reading...");
					String in = ois.readUTF();
					System.out.println(in);
							}			
				break;				
			}		
			System.out.println("Client wrote & start waiting for data from server...");			
			Thread.sleep(2000);		
			if(ois.available()!=0)		{									
			System.out.println("reading...");
			String in = ois.readUTF();
			System.out.println(in);
					}			
				}
			}
			System.out.println("Closing connections & channels on clentSide - DONE.");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
public class ChatServer implements TCPConnectionListener {
    public static void main(String[] args) {
        new ChatServer();
    }
    private final ArrayList<TCPConnection> connections = new ArrayList<>();
    private ChatServer() {
        System.out.println("Server running...");
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    System.out.println("TCPConnection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        sendToAllConnections("Client connection: " + tcpConnection);
    }
    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
    }
    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnection: " + tcpConnection);
    }
    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }
    private void sendToAllConnections(String value) {
        System.out.println(value);
        final int cos = connections.size();
        for (int i = 0; i < cos; i++) {
            connections.get(i).sendString(value);
        }
    }
}
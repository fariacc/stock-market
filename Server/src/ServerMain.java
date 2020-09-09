
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import implementation.ServidorImplementation;
import interfaceRmi.Server;

public class ServerMain {
	static String SERVIDOR = "SERVIDOR";
	static String IP = "127.0.0.1";
	static int PORT = 1099;

	public static void main(String[] args) throws RemoteException {
		Registry nameServer = LocateRegistry.createRegistry(PORT);
		Server servidor = null;
		try {
			servidor = new ServidorImplementation();
			nameServer.bind(SERVIDOR, servidor);
			System.out.println("Servidor inicializado");
		} catch (Exception e) {
			System.out.println("Erro ao inicializar servidor");
			e.printStackTrace();
		}
	}
}

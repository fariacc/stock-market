package implementation;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import classes.Acao;
import interfaceRmi.Cliente;
import interfaceRmi.Server;

public class ClienteImplementation extends UnicastRemoteObject implements Cliente {

	private static final long serialVersionUID = 1L;

	Server servidor;

	/**
	 * Construtor do cliente
	 */
	public ClienteImplementation(Server servidor) throws AccessException, RemoteException, NotBoundException {
		this.servidor = servidor;
	}

	/**
	 * Callback para notificacao de acao do interesse com preco no limite menor ou maior
	 */
	@Override
	public void notificar(Acao acaoLimite) throws RemoteException {
		System.out.println();
		System.out.println("*** Nova acao de seu interesse ***");
		System.out.println("Codigo: " + acaoLimite.getCodigo().toString());
		System.out.println("Quantidade disponivel: " + acaoLimite.getQuantidade());
		System.out.println("Preco unitario: R$ " + acaoLimite.getPreco() + ",00");
		System.out.println();
	}

}

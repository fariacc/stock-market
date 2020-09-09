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
	 * Callback para notificacoes do cliente
	 */
	@Override
	public void notificar(String mensagem, Acao acao) throws RemoteException {
		System.out.println();
		System.out.println(mensagem);
		System.out.println("Codigo: " + acao.getCodigo().toString());
		System.out.println("Quantidade negociada: " + acao.getQuantidade());
		System.out.println("Preco unitario: R$ " + acao.getPreco() + ",00");
		System.out.println();
	}

}

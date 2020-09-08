package interfaceRmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import classes.Acao;

public interface Cliente extends Remote {
	public void notificar(Acao acao) throws RemoteException;
}
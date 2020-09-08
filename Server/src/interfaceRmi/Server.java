package interfaceRmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import classes.Acao;
import classes.Interesse;

public interface Server extends Remote {
	
	public List<Acao> consultarAcoes() throws RemoteException;
	
	public List<Acao> consultarAcaoEspecifica(String codigo) throws RemoteException;

	public String comprarAcao(Acao acao) throws RemoteException;

	public String cadastrarAcao(Acao acao) throws RemoteException;

	//tem q implementar o venderAcao
	public String removerAcao(Acao acao) throws RemoteException;
	
	public List<Interesse> consultarInteresses(Cliente referencia) throws RemoteException;
	
	public String registrarInteresse(Interesse interesse) throws RemoteException;

	public String removerInteresse(Interesse interesse) throws RemoteException;

}

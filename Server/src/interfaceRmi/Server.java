package interfaceRmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import classes.Acao;
import classes.Interesse;

public interface Server extends Remote {
	
	public List<Acao> consultarCarteira(Cliente cliente) throws RemoteException;
	
	public List<Acao> consultarCarteiraAcaoEspecifica(Cliente cliente, String codigo) throws RemoteException;
	
	public String cadastrarAcaoCarteira(Acao acao) throws RemoteException;
	
	public String removerAcaoCarteira(Cliente cliente, String codigo) throws RemoteException;

	public String comprarAcao(Acao acao) throws RemoteException;
	
	public String venderAcao(Acao acao) throws RemoteException;
	
	public List<Interesse> consultarInteresses(Cliente cliente) throws RemoteException;
	
	public String registrarInteresse(Interesse interesse) throws RemoteException;

	public String removerInteresse(Cliente cliente, String codigo) throws RemoteException;
	
	public List<Acao> obterCotacoes(Cliente cliente) throws RemoteException;
	
	public List<Acao> obterCotacaoAcaoEspecifica(Cliente cliente, String codigo) throws RemoteException;
	
	public String cadastrarAcaoCotacoes(Acao acao) throws RemoteException;
	
	public String removerCotacaoAcaoEspecifica(Cliente cliente, String codigo) throws RemoteException;

}

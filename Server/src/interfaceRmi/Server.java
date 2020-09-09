package interfaceRmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import classes.Acao;
import classes.Interesse;

public interface Server extends Remote {
	
	public List<Acao> consultarCarteira() throws RemoteException;
	
	public List<Acao> consultarCarteiraAcaoEspecifica(String codigo) throws RemoteException;
	
	public String cadastrarAcaoCarteira(Acao acao) throws RemoteException;
	
	public String removerAcaoCarteira(Acao acao) throws RemoteException;

	public String comprarAcao(Cliente cliente, Acao acao) throws RemoteException;
	
	public String venderAcao(Cliente cliente, Acao acao) throws RemoteException;
	
	public List<Interesse> consultarInteresses(Cliente referencia) throws RemoteException;
	
	public String registrarInteresse(Interesse interesse) throws RemoteException;

	public String removerInteresse(Interesse interesse) throws RemoteException;
	
	public List<Acao> obterCotacoes() throws RemoteException;
	
	public List<Acao> obterCotacaoAcaoEspecifica(String codigo) throws RemoteException;
	
	public String cadastrarAcaoCotacoes(Cliente cliente, Acao acao) throws RemoteException;
	
	public String removerCotacaoAcaoEspecifica(Acao acao) throws RemoteException;

}

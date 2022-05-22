package rede;

import model.Mensagem;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import view.TelaServidor;

public class SocketServidor {
    private int porta = 20100;
    static List<ThreadCliente> threadsClientes;
    private ServerSocket socketServidor;
    
    public static TelaServidor tela;
    
    public void iniciarServidor(TelaServidor tela) throws Exception {
        threadsClientes = new ArrayList();
        
        System.out.println("Criando servidor...");
        socketServidor = new ServerSocket(porta);
        
        System.out.println("Servidor criado, iniciando seu escritor");
        EscritorDeSocketServidor escritor = new EscritorDeSocketServidor();
        Thread threadEscritor = new Thread(escritor);        
        threadEscritor.start();
        
        this.tela = tela;
        while (true) {
            System.out.println("Escutando porta " + porta + " esperando clientes.");
            ThreadCliente threadCliente = new ThreadCliente(socketServidor.accept());
              
            System.out.println("Cliente conectando...");
               
            if (threadCliente == null) { 
                System.out.println("Thread de cliente não foi criada corretamente.");
                continue;
            }
                
            threadsClientes.add(threadCliente);
            threadCliente.start();
        }
        
    }
    
    public void enviarMensagem(Mensagem msg) {
        msg.setHora(LocalTime.now());
        
        try {
            EscritorDeSocketServidor.emitirMensagem(msg);
        } catch (Exception e) {
            System.out.println("ERRO (enviarMensagem SocketServidor): " + e.getMessage());
        }
        
    }
    
    static String retornarNomeDosClientes() {
        String msg = "";
        int quantiaUsuarios = threadsClientes.size();
        
        if (quantiaUsuarios > 1) {
            for (int i = 0; i < quantiaUsuarios - 1; i++) {
                msg += threadsClientes.get(i).nome + ",";
            }
        }
        
        if (quantiaUsuarios > 0) msg += threadsClientes.get(quantiaUsuarios - 1).nome;
        
        return msg;
    }
    
    static void removerCliente(ThreadCliente tc, boolean emitirMensagem) {
        int quantiaUsuarios = threadsClientes.size();
        
        System.out.println("Usuarios registrados: " + quantiaUsuarios + " -> " + (quantiaUsuarios - 1));
        threadsClientes.remove(tc);
        
        if (!emitirMensagem) return;
        
        try {
            EscritorDeSocketServidor.emitirMensagem(new Mensagem("AtualizarOnline", retornarNomeDosClientes()));
            EscritorDeSocketServidor.emitirMensagem(new Mensagem("Mensagem", tc.nome + " desconectou-se"));
        } catch (Exception e) {
            System.out.println("ERRO (removerCliente): " + e.getMessage());
        }
    }
    
    public void encerrarServidor() {
        /*
        for (ThreadCliente tc : threadsClientes) {
            tc.encerrarConexao(false); // Está dando exceção de Modificação concorrente do arraylist threadClientes para emitir mensagem (ignorar emissão então)
        }
        */
        
        // Força bruta
        while (threadsClientes.size() > 0) {
            for (int i = 0; i < threadsClientes.size(); i++) {
                threadsClientes.get(i).encerrarConexao(false);
            } 
        }
        
        try {
            socketServidor.close();
        } catch (Exception e) {
            System.out.println("ERRO (Fechar socket do servidor): " + e.getMessage());
        }
    }
}

class ThreadCliente extends Thread{
    public String nome = "<Sem nome>";
    Socket socket;
    ObjectInputStream in;
    ObjectOutputStream out;
    LeitorDeSocketCliente leitor;
    boolean encerrandoConexao = false;
    
    public ThreadCliente(Socket socket) {
        this.socket = socket;
        
        try {
            out = new ObjectOutputStream(socket.getOutputStream()); // Out SEMPRE primeiro
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());            
        } catch (Exception e) {
            System.out.println("ERRO CONSTRUTOR THREAD: " + e.getMessage());
        }
        
        System.out.println("(Construtor) Thread cliente criada para " + nome);
    }
    
    public void run() {
        try {
            this.nome = in.readUTF(); // Recebe primeiramente o nome do usuário.
            Mensagem msg;
            // Avisa conexão para arrumar a aba online
            msg = new Mensagem("AtualizarOnline", SocketServidor.retornarNomeDosClientes());
            EscritorDeSocketServidor.emitirMensagem(msg); 
            
            System.out.println("Cliente (" + nome + ") configurado com sucesso\n" + nome + " conectou-se");
            
            // Envia mensagem alertando conexão
            msg = new Mensagem("Mensagem", nome + " conectou-se", LocalTime.now()); // Nunca esqueça de preencher os 3 valores antes do cliente receber!
            EscritorDeSocketServidor.emitirMensagem(msg); 
            
        } catch (Exception e) {
            System.out.println("ERRO THREAD CLIENTE: " + e.getMessage());
        
        }
        
        leitor = new LeitorDeSocketCliente(this, in);
        leitor.run();
    }
    
    public void encerrarConexao() {
        this.encerrarConexao(true);
    }
    
    public void encerrarConexao(boolean emitirMensagem) {
        if (encerrandoConexao) return;
        encerrandoConexao = true;
        
        try {
            System.out.println("(1/3) Fechando out");
            out.close();
            System.out.println("(2/3) Fechando in");
            in.close();
            System.out.println("(3/3) Fechando socket");
            socket.close(); 
        } catch (Exception e) {
            System.out.println("ERRO (encerrarConexao do threadCliente): " + e.getMessage());
        }
        
        SocketServidor.removerCliente(this, emitirMensagem);
    }
}

class LeitorDeSocketCliente extends Thread{
    ThreadCliente tc;
    ObjectInputStream in;
    
    public LeitorDeSocketCliente(ThreadCliente tc, ObjectInputStream in) {
        this.tc = tc;
        this.in = in;
    }
    
    public void run() {
        try {
            while (true) {
                Mensagem msg = (Mensagem)in.readObject();   
                LocalTime horaAtual = LocalTime.now();
                msg.setHora(horaAtual);
                
                System.out.println(msg.retornarMensagemFormatada());                
                EscritorDeSocketServidor.emitirMensagem(msg);
            }
        } catch (Exception e) {
            System.out.println("ERRO LEITOR: " + e.getMessage());
            System.out.println("Retirando thread devido ao erro.");
            tc.encerrarConexao();
        }
    }
}

class EscritorDeSocketServidor extends Thread {    
    public void run() {
        try {
            Mensagem msg;
            BufferedReader inUsuario = new BufferedReader(new InputStreamReader(System.in)); // Obter dados do usuário para enviar
            
            while (true) {
                System.out.println("Digite sua mensagem:");
                msg = new Mensagem("Mensagem", "(SERVIDOR): " + inUsuario.readLine());
                msg.setHora(LocalTime.now());
                
                //System.out.println(msg.retornarMensagemFormatada());                
                emitirMensagem(msg);
            }
        } catch (Exception e) {
            System.out.println("ERRO ESCRITOR: " + e.getMessage());
        }
    }
    
    public static void emitirMensagem(Mensagem msg) throws Exception {  
        if (SocketServidor.threadsClientes.size() > 0) {
            for (ThreadCliente tc : SocketServidor.threadsClientes) {
                ObjectOutputStream out = tc.out;
                out.writeObject(msg);
                out.flush();
            }
        }
        
        SocketServidor.tela.interpretarMensagem(msg);
    }
}

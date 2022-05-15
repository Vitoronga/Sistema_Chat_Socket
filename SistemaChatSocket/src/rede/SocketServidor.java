package rede;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketServidor {
    private static int porta = 20100;
    static List<ThreadCliente> threadsClientes;
    
    public static void main(String[] args) {
        threadsClientes = new ArrayList();
        
        try {
            System.out.println("Criando servidor...");
            ServerSocket socketServidor = new ServerSocket(porta);
            
            System.out.println("Servidor criado, iniciando seu escritor");
            EscritorDeSoqueteServer escritor = new EscritorDeSoqueteServer();            
            Thread threadEscritor = new Thread(escritor);
            threadEscritor.start();
            
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
                
                //System.out.println("Cliente conectou-se.");
            }
        } catch (Exception e) {
            System.out.println("ERRO SERVIDOR: " + e.getMessage());
        }
        
    }
}

class ThreadCliente extends Thread{
    public String nome = "Unnamed";
    Socket socket;
    BufferedReader in; // Leitor de input recebido do socket, instanciado de um canal de entrada que é instanciado baseado no canal de fluxo de entrada de dados do soquete.
    PrintWriter out; // Emissor de output enviado para o socket, instanciado de um canal de saída que é instanciado baseado no canal de fluxo de saída de dados do soquete.
    LeitorDeSoqueteCliente leitor;
    //public EscritorDeSoqueteServer escritor;
    
    public ThreadCliente(Socket socket) {
        this.socket = socket;
        
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (Exception e) {
            System.out.println("ERRO CONSTRUTOR CLIENTE: " + e.getMessage());
        }
        
        System.out.println("(Construtor) Thread cliente criada para " + nome);
    }
    
    private void obterNome() throws Exception{ 
        this.nome = in.readLine(); // Recebe primeiramente o nome do usuário.
    }
    
    public void run() {
        try {
            obterNome();
            System.out.println("Cliente (" + nome + ") configurado com sucesso\n" + nome + " conectou-se");
            EscritorDeSoqueteServer.emitirMensagem(nome + " conectou-se");
        } catch (Exception e) {
            System.out.println("ERRO THREAD CLIENTE: " + e.getMessage());
        }
        
        leitor = new LeitorDeSoqueteCliente(this, in);
        //escritor = new EscritorDeSoqueteServer(out);
        
        //Thread leitorThread = new Thread(leitor);
        //leitorThread.start();
        leitor.run();
        
        //escritor.start();
    }
}

class LeitorDeSoqueteCliente {
    ThreadCliente tc;
    BufferedReader recebidor;
    
    public LeitorDeSoqueteCliente(ThreadCliente tc, BufferedReader recebidor) {
        this.tc = tc;
        this.recebidor = recebidor;
    }
    
    public void run() {
        try {
            while (true) {
                String msg = tc.nome + ": " + recebidor.readLine();
                System.out.println(msg);
                
                EscritorDeSoqueteServer.emitirMensagem(msg);
            }
        } catch (Exception e) {
            System.out.println("ERRO LEITOR: " + e.getMessage());
        }
    }
}

class EscritorDeSoqueteServer extends Thread {
    /*
    PrintWriter emissor;
    
    public EscritorDeSoqueteServer (PrintWriter emissor) {
        this.emissor = emissor;
    }
    */
    
    public void run() {
        try {
            String msg;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); // Obter dados do usuário para enviar
            
            while (true) {
                System.out.println("Digite sua mensagem:");
                msg = "Servidor: " + in.readLine();
                System.out.println(msg);
                
                emitirMensagem(msg);
            }
        } catch (Exception e) {
            System.out.println("ERRO ESCRITOR: " + e.getMessage());
        }
    }
    
    public static void emitirMensagem(String msg) throws Exception {
        for (ThreadCliente tc : SocketServidor.threadsClientes) {
            PrintWriter emissor = new PrintWriter(new OutputStreamWriter(tc.socket.getOutputStream()));
            emissor.println(msg);
            emissor.flush();
        }       
    }
}

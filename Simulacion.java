
import java.util.Random;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.JOptionPane;

public class Simulacion {

    public static final int MAXSERVERS = 2;

    public static void main(String[] args) {
        Simulacion sim = new Simulacion();
        sim.run();
    }

    private class Cliente {

        // Identificador unico para cada cliente
        private int identificador;
        // Tiempo en el cual llega el cliente al sistema
        private int tiempoLlegada;

        public Cliente(int identificador, int tiempoLlegada) {
            this.identificador = identificador;
            this.tiempoLlegada = tiempoLlegada;
        }

        public int getIdentificador() {
            return identificador;
        }

        public void setIdentificador(int identificador) {
            this.identificador = identificador;
        }

        public int getTiempoLlegada() {
            return tiempoLlegada;
        }

        public void setTiempoLlegada(int tiempoLlegada) {
            this.tiempoLlegada = tiempoLlegada;
        }
    }

    private class Evento {

        // Identificador del tipo de evento que es
        // 0 = Llegada
        // 1 = Salida
        private int tipo;
        // Tiempo en que se ejecuta el evento
        private int tiempo;

        public Evento(int tipo, int tiempo) {
            this.tipo = tipo;
            this.tiempo = tiempo;
        }

        public int getTipo() {
            return tipo;
        }

        public void setTipo(int tipo) {
            this.tipo = tipo;
        }

        public int getTiempo() {
            return tiempo;
        }

        public void setTiempo(int tiempo) {
            this.tiempo = tiempo;
        }
    }

    // Longitud de la cola actualmente
    private int queueLength;
    // Estado de los servers. 
    // False es que no estan ocupados
    // True es que estan ocupados
    private boolean[] servers;
    // Gente a la cual se ha atendido (salio del sistema)
    private int genteAtendida;
    // Tiempo actual de relog
    private int tiempoDeReloj;
    // Lista de Eventos
    private LinkedList<Evento> listaEventos;
    // Cola de eventos por procesar
    private Queue<Cliente> colaEspera;
    // Cantidad de servidores que hay disponibles
    private int servDisponibles;
    //Numero de clientes que han entrado en el sistema
    private int numClientes;
    // Estadisticas
    private int tiempoEnColaTotal;

    void run() {
        /*Declarar datos */
        servers = new boolean[MAXSERVERS];
        listaEventos = new LinkedList<>();
        colaEspera = new LinkedList<>();
        /*Interfaz grafica*/
        mostrarInterfaz();
    }

    void mostrarInterfaz() {
        boolean repetir = true;
        while (repetir) {
            String clientesProcesar = JOptionPane.showInputDialog(null, "Ingrese el número de clientes que serán atendidos en la simulación.");
            if (clientesProcesar != null) {
                //cantidad de clientes que se deben procesar 
                int cantidadClientes = Integer.parseInt(clientesProcesar);
                instanciaSimulacion(cantidadClientes);
                int Button = JOptionPane.YES_NO_OPTION;
                Button = JOptionPane.showConfirmDialog(null, "Simulación Finalizada, desea ver las estadísticas?", "Simulación", Button);
                if (Button == JOptionPane.YES_OPTION) {
                    /*Mostrar Estadisticas */
                    JOptionPane.showMessageDialog(null, "Estadisticas: ");
                    /*Agregar*/
                }
                int Button2 = JOptionPane.YES_NO_OPTION;
                Button2 = JOptionPane.showConfirmDialog(null, "Desea realizar otra simulación?", "Simulación", Button2);
                if (Button2 != JOptionPane.YES_OPTION) {
                    repetir = false;
                }
            } else {
                repetir = false;
            }
        }
    }

    void instanciaSimulacion(int cantidadClientes) {
        // Iniciar variables para esta instancia de la simulacion
        for (int index = 0; index < MAXSERVERS; index++) {
            servers[index] = false;
        }
        numClientes = tiempoEnColaTotal = queueLength = tiempoDeReloj = genteAtendida = 0;
        servDisponibles = MAXSERVERS;
        listaEventos.clear();
        colaEspera.clear();

        //Primera llegada en tiempo 0
        listaEventos.add(new Evento(0, 0));

        while (genteAtendida != cantidadClientes) {
            //Sacar el siguiente Evento
            Evento siguiente = listaEventos.pop();
            //Mover el tiempo de reloj
            this.tiempoDeReloj = siguiente.getTiempo();
            //Procesar el evento
            revisarEvento(siguiente);
        }
    }

    void revisarEvento(Evento actual) {
        // Si el evento es 0, es llegada. Si es 1, es salida.
        switch (actual.getTipo()) {
            case 0:
                procesarLlegada();
                break;
            case 1:
                procesarSalida();
                break;
        }
    }

    void insertarEvento(Evento even) {
        /* :c */
    }

    void generarLlegada() {
        // Genera el numero aleatorio
        Random rand = new Random();
        float val = rand.nextFloat();
        int tiempoNuevo = tiempoDeReloj;
        // Revisa cual es el tiempo que debe poner, con respecto a la funcion acumulada
        if (val < 0.40) {
            tiempoNuevo++;
        } else {
            if (val < 0.75) {
                tiempoNuevo += 2;
            } else {
                tiempoNuevo += 3;
            }
        }
        insertarEvento(new Evento(0, tiempoNuevo));
    }

    int generarSalida() {
        // Genera el numero aleatorio
        Random randomN = new Random();
        float number = randomN.nextFloat();
        int time = tiempoDeReloj;
        // Revisa cual es el tiempo que debe poner, con respecto a la funcion acumulada
        if (number < 0.10) {
            time += 2;
        } else {
            if (number < 0.35) {
                time += 3;
            } else {
                if (number < 0.75) {
                    time += 4;
                } else {
                    if (number < 0.95) {
                        time += 7;
                    } else {
                        time += 10;
                    }
                }

            }
        }
        insertarEvento(new Evento(1, time));
        return time;
    }

    void procesarLlegada() {
        //Agrega que llego un cliente
        numClientes++;
        //Revisa si hay servidores disponibles
        if (servDisponibles == 0) {
            // Si no hay, lo agrega a la cola de esperar
            queueLength++;
            colaEspera.add(new Cliente(numClientes, tiempoDeReloj));
        } else {
            // En caso contrario, lo mete a ser atendido
            boolean encontroServer = false;
            for (int s = 0; s < MAXSERVERS && !encontroServer;) {
                if (servers[s] == false) {
                    servers[s] = true;
                    encontroServer = true;
                }
                s++;
            }

            // Agrega la salida al servidor
            generarSalida();
            // Actualizar la cantidad de servidores disponibles
            servDisponibles--;
        }
        generarLlegada();
    }

    void procesarSalida() {
        //Agregar que se termino otro cliente
        genteAtendida++;
        // Revisar si hay alguien en la cola
        if (queueLength > 0) {
            // Si aun hay personas, atender al siguiente en cola
            queueLength--;
            Cliente atendido = colaEspera.remove();
            tiempoEnColaTotal += tiempoDeReloj - atendido.tiempoLlegada;
            generarSalida();
        } else {
            // Si no hay personas en cola, poner el servidor en desocupado.
            boolean encontroServer = false;
            for (int s = 0; s < MAXSERVERS && !encontroServer;) {
                if (servers[s] == true) {
                    servers[s] = false;
                    encontroServer = true;
                }
                s++;
            }
        }
    }
}

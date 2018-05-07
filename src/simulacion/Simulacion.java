package simulacion;

import java.util.Random;
import java.util.LinkedList;
import java.util.ListIterator;
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
        private float tiempoLlegada;

        public Cliente(int identificador, float tiempoLlegada) {
            this.identificador = identificador;
            this.tiempoLlegada = tiempoLlegada;
        }

        public int getIdentificador() {
            return identificador;
        }

        public void setIdentificador(int identificador) {
            this.identificador = identificador;
        }

        public float getTiempoLlegada() {
            return tiempoLlegada;
        }

        public void setTiempoLlegada(float tiempoLlegada) {
            this.tiempoLlegada = tiempoLlegada;
        }
    }

    private class Evento {

        // Identificador del tipo de evento que es
        // 0 = Llegada
        // 1 = Salida
        private int tipo;
        // Tiempo en que se ejecuta el evento
        private float tiempo;

        public Evento(int tipo, float tiempo) {
            this.tipo = tipo;
            this.tiempo = tiempo;
        }

        public int getTipo() {
            return tipo;
        }

        public void setTipo(int tipo) {
            this.tipo = tipo;
        }

        public float getTiempo() {
            return tiempo;
        }

        public void setTiempo(float tiempo) {
            this.tiempo = tiempo;
        }
    }

    // Longitud de la cola actualmente
    private int queueLength;
    // Estado de los servers. 
    // False es que no estan ocupados
    // True es que estan ocupados
    private boolean[] servers;
    // Tiempo actual de relog
    private float tiempoDeReloj;
    // Lista de Eventos
    private LinkedList<Evento> listaEventos;
    // Cola de eventos por procesar
    private Queue<Cliente> colaEspera;
    // Cantidad de servidores que hay disponibles
    private int servDisponibles;
    // Gente a la cual se ha atendido (salio del sistema)
    private int genteAtendida;
    //Numero de clientes que han entrado en el sistema
    private int numClientes;
    //Numero de clientes que pasaron por la cola
    private int numClientesCola;
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
            String clientesProcesar = JOptionPane.showInputDialog(null, "Ingrese el numero de clientes que serán atendidos en la simulacion.");
            if (clientesProcesar != null) {
                //cantidad de clientes que se deben procesar 
                int cantidadClientes = Integer.parseInt(clientesProcesar);
                instanciaSimulacion(cantidadClientes);
                int Button = JOptionPane.YES_NO_OPTION;
                Button = JOptionPane.showConfirmDialog(null, "Simulacion Finalizada, desea ver las estadisticas?", "Simulacion", Button);
                if (Button == JOptionPane.YES_OPTION) {
                    /*Mostrar Estadisticas */
                    JOptionPane.showMessageDialog(null, "Estadisticas: \n"
                            + "Tiempo promedio en cola: " + (tiempoEnColaTotal / (double) numClientesCola) + "\n"
                            + "Personas que quedaban en cola: " + queueLength);
                    /*Agregar*/
                }
                int Button2 = JOptionPane.YES_NO_OPTION;
                Button2 = JOptionPane.showConfirmDialog(null, "Desea realizar otra simulacion?", "Simulacion", Button2);
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
		tiempoDeReloj = 0;
        numClientes = tiempoEnColaTotal = queueLength = genteAtendida = numClientesCola = tiempoEnColaTotal = 0;
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
        //Si la lista est� vac�a, meto el evento de primero
        if (listaEventos.isEmpty()) {
            listaEventos.add(even);
        } else {
            boolean ordenado = false; //Me indica si ya la ordene
            ListIterator<Evento> itr = listaEventos.listIterator(); //Iterador para recorrer la lista
            //Mientras no haya llegado al final de la lista y no haya encontrado la posicion correcta
            while (itr.hasNext() && !ordenado) {
                Evento evenItr = itr.next();	//Posicion actual a procesar
                //Si el tiempo es mayor, nada mas pongo el evento antes
                if (evenItr.getTiempo() > even.getTiempo()) {
                    itr.previous(); //Me devuelvo, ya que next() me hace pasarme de donde queria ponerlo
                    itr.add(even);
                    ordenado = true;
                } //Si los tiempos son iguales tengo que poner las salidas antes de las llegadas
                else if (evenItr.getTiempo() == even.getTiempo()) {
                    itr.previous();
                    //Si el evento que voy a colocar es una salida, simplemente lo pongo de primero dentro de los eventos con el mismo tiempo que el
                    if (even.getTipo() == 1) {
                        itr.add(even);
                    } else {
                        //Si no, me muevo en la lista hasat encontrar uno de tipo llegada, el final de los tipo salida o el fin de la lista
                        while (itr.hasNext() && evenItr.getTipo() != 0 && evenItr.getTiempo() == even.getTiempo()) {
                            //Tener cuidado al mover el iterador ya que next() siempre mueve la posicion del mismo
                            itr.next();
                            if (itr.hasNext()) {
                                evenItr = itr.next();
                                itr.previous();
                            }
                        }
                        itr.add(even);
                    }
                    ordenado = true;
                }
            }
            //Si no encontre a alguien con tiempo mayor, lo meto al final de la lista
            if (!ordenado) {
                itr.add(even);
            }
        }
    }

    void generarLlegada() {
        // Genera el numero aleatorio
        Random rand = new Random();
        float val = rand.nextFloat();
        float tiempoNuevo = tiempoDeReloj;
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

    void generarSalida() {
        // Genera el numero aleatorio
        Random randomN = new Random();
        float number = randomN.nextFloat();
        float time = tiempoDeReloj;
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
            numClientesCola++;
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
            numClientesCola++;
            tiempoEnColaTotal += tiempoDeReloj - atendido.getTiempoLlegada();
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
            servDisponibles++;
        }
    }
}

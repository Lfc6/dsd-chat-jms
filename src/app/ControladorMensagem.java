package app;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ControladorMensagem {

	public static final String ENDERECO = "tcp://localhost:61616";
	public static final String TAG_MENSAGEM_SERVIDOR = "servidor";

	public static final String TAG_MENSAGEM_LOGIN = "login";

	public static final String TAG_MENSAGEM_GLOBAL = "global";

	public static final String PROPRIEDADE_TEXTO = "texto";
	public static final String PROPRIEDADE_ID_DESTINO = "id_destino";
	public static final String PROPRIEDADE_ID = "id";

	private String idUSuario;

	public ControladorMensagem(String idUSuario) {
		this.idUSuario = idUSuario;
	}

	/**
	 * Envia uma mensagem
	 * 
	 * @param texto
	 *            Texto da mensagem
	 * @param tag
	 *            Tag usada para inserir a mensagem na fila
	 * @param idUsuarioDestino
	 *            Parametro usado exclusivamente pelo cliente para informar que esta
	 *            mensagem � apenas para um cliente
	 */
	public void enviarMensagem(final String texto, final String tag, final String idUsuarioDestino) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {

					ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ENDERECO);

					Connection connection = connectionFactory.createConnection();
					connection.start();

					Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

					Destination destination = session.createQueue(tag);

					MessageProducer producer = session.createProducer(destination);
					producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

					Message mensagem = session.createMessage();
					mensagem.setStringProperty(PROPRIEDADE_ID, ControladorMensagem.this.idUSuario);
					mensagem.setStringProperty(PROPRIEDADE_TEXTO, texto);

					if (idUsuarioDestino != null) {
						mensagem.setStringProperty(PROPRIEDADE_ID_DESTINO, idUsuarioDestino);
					}

					producer.send(mensagem);

					session.close();
					connection.close();
				} catch (Exception e) {
					System.out.println("Caught: " + e);
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void receberMensagem(final MensagemRecebida event) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {

					ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ENDERECO);

					Connection connection = connectionFactory.createConnection();
					connection.start();

					Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

					Destination destination = session.createQueue(ControladorMensagem.this.idUSuario);

					MessageConsumer consumer = session.createConsumer(destination);

					consumer.setMessageListener(new MessageListener() {

						@Override
						public void onMessage(Message message) {
							if (message != null) {
								event.recebida(message);
							}
						}
					});

				} catch (Exception e) {
					System.out.println("Caught: " + e);
					e.printStackTrace();
				}
			}
		}).start();
	}
}

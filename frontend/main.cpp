#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include <QTcpSocket>
#include <QHostAddress>

class SimpleClient : public QObject {
    Q_OBJECT
    Q_PROPERTY(bool connected READ isConnected NOTIFY connectedChanged)
public:
    explicit SimpleClient(QObject *parent = nullptr) : QObject(parent) {
        connect(&socket, &QTcpSocket::connected, this, &SimpleClient::onConnected);
        connect(&socket, &QTcpSocket::disconnected, this, &SimpleClient::onDisconnected);
        connect(&socket, &QTcpSocket::readyRead, this, &SimpleClient::onReadyRead);
        connect(&socket, &QTcpSocket::errorOccurred, this, &SimpleClient::onErrorOccurred);
    }

public slots:
    void connectToServer(const QString &host) {
        socket.abort();
        socket.connectToHost(host, 5000);
        emit statusMessage(tr("Connecting to %1:%2...").arg(host).arg(5000));
    }

    void disconnectFromServer() {
        if (socket.state() != QTcpSocket::UnconnectedState)
            socket.disconnectFromHost();
    }

    void sendMessage(const QString &msg) {
        if (socket.state() == QTcpSocket::ConnectedState) {
            socket.write((msg + "\n").toUtf8());
            socket.flush();
        }
    }

    bool isConnected() const { return socket.state() == QTcpSocket::ConnectedState; }

private slots:
    void onConnected() {
        const QString peer = socket.peerName().isEmpty() ? socket.peerAddress().toString() : socket.peerName();
        emit statusMessage(tr("Connected to %1:%2").arg(peer).arg(socket.peerPort()));
        emit connectedChanged();
    }

    void onDisconnected() {
        emit statusMessage(tr("Disconnected"));
        emit connectedChanged();
    }

    void onReadyRead() {
        const QString msg = QString::fromUtf8(socket.readAll());
        emit messageReceived(msg);
    }

    void onErrorOccurred(QTcpSocket::SocketError) {
        emit statusMessage(tr("Socket error: %1").arg(socket.errorString()));
        emit connectedChanged();
    }

signals:
    void messageReceived(QString msg);
    void connectedChanged();
    void statusMessage(QString msg);

private:
    QTcpSocket socket;
};


int main(int argc, char *argv[])
{
    QGuiApplication app(argc, argv);

    QQmlApplicationEngine engine;
    SimpleClient client;
    
    engine.rootContext()->setContextProperty("client", &client);
    
    QObject::connect(
        &engine,
        &QQmlApplicationEngine::objectCreationFailed,
        &app,
        []() { QCoreApplication::exit(-1); },
        Qt::QueuedConnection);
    engine.loadFromModule("Frontend", "Main");
    return app.exec();
}

#include "main.moc"

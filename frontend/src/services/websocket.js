import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const WEBSOCKET_URL = 'http://localhost:8080/ws';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.connected = false;
    this.subscriptions = {};
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 3000;
  }

  /**
   * Ket noi den WebSocket server
   * @param {Function} onConnect - callback khi ket noi thanh cong
   * @param {Function} onError - callback khi co loi
   */
  connect(onConnect, onError) {
    const socket = new SockJS(WEBSOCKET_URL);
    this.stompClient = Stomp.over(socket);

    // tat debug log
    this.stompClient.debug = null;

    this.stompClient.connect(
      {},
      () => {
        this.connected = true;
        this.reconnectAttempts = 0;
        console.log('WebSocket connected');
        if (onConnect) onConnect();
      },
      (error) => {
        this.connected = false;
        console.error('WebSocket error:', error);

        // thu ket noi lai
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++;
          console.log(
            `Thu ket noi lai lan ${this.reconnectAttempts}/${this.maxReconnectAttempts}...`
          );
          setTimeout(() => {
            this.connect(onConnect, onError);
          }, this.reconnectDelay);
        }

        if (onError) onError(error);
      }
    );
  }

  /**
   * Ngat ket noi WebSocket
   */
  disconnect() {
    if (this.stompClient && this.connected) {
      // huy tat ca subscription
      Object.keys(this.subscriptions).forEach((key) => {
        this.subscriptions[key].unsubscribe();
      });
      this.subscriptions = {};

      this.stompClient.disconnect(() => {
        this.connected = false;
        console.log('WebSocket disconnected');
      });
    }
  }

  /**
   * Subscribe vao 1 topic/queue
   * @param {string} destination - dia chi subscribe (vd: /topic/room.1)
   * @param {Function} callback - ham xu ly khi nhan message
   * @returns {string} subscription id
   */
  subscribe(destination, callback) {
    if (!this.stompClient || !this.connected) {
      console.error('WebSocket chua ket noi!');
      return null;
    }

    const subscription = this.stompClient.subscribe(destination, (payload) => {
      const data = JSON.parse(payload.body);
      callback(data);
    });

    this.subscriptions[destination] = subscription;
    return destination;
  }

  /**
   * Huy subscribe 1 topic/queue
   * @param {string} destination - dia chi can huy subscribe
   */
  unsubscribe(destination) {
    if (this.subscriptions[destination]) {
      this.subscriptions[destination].unsubscribe();
      delete this.subscriptions[destination];
    }
  }

  /**
   * Gui message den server
   * @param {string} destination - dia chi gui (vd: /app/chat.sendMessage/1)
   * @param {Object} body - noi dung message
   * @param {Object} headers - headers bo sung (mac dinh {})
   */
  send(destination, body, headers = {}) {
    if (!this.stompClient || !this.connected) {
      console.error('WebSocket chua ket noi!');
      return;
    }

    this.stompClient.send(destination, headers, JSON.stringify(body));
  }

  /**
   * Kiem tra trang thai ket noi
   * @returns {boolean}
   */
  isConnected() {
    return this.connected && this.stompClient !== null;
  }
}

// export singleton instance
const websocketService = new WebSocketService();
export default websocketService;
